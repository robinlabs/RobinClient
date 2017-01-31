package com.magnifis.parking.cmd;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.StyleSpan;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.EditText;

import com.magnifis.parking.Abortable;
import com.magnifis.parking.App;
import com.magnifis.parking.Config;
import com.magnifis.parking.Launchers;
import com.magnifis.parking.Log;
import com.magnifis.parking.MainActivity;
import com.magnifis.parking.Output;
import com.magnifis.parking.R;
import com.magnifis.parking.Robin;
import com.magnifis.parking.SendSmsActivity;
import com.magnifis.parking.VR;
import com.magnifis.parking.VoiceIO;
import com.magnifis.parking.bubbles.SpannedTextBubbleContent;
import com.magnifis.parking.cmd.etc.CmdHandlerHolder;
import com.magnifis.parking.cmd.i.ClientStateInformer;
import com.magnifis.parking.cmd.i.IHandlesAbortInAnyCase;
import com.magnifis.parking.cmd.i.LocalCommandHandler;
import com.magnifis.parking.cmd.i.MagReplyHandler;
import com.magnifis.parking.cmd.i.OnBeforeListeningHandler;
import com.magnifis.parking.cmd.i.OnListeningAbortedHandler;
import com.magnifis.parking.cmd.i.OnOrientationHandler;
import com.magnifis.parking.cmd.i.OnResumeHandler;
import com.magnifis.parking.db.RobinDB;
import com.magnifis.parking.feed.SmsFeedController;
import com.magnifis.parking.messaging.Message;
import com.magnifis.parking.model.ContactRecord;
import com.magnifis.parking.model.DelegateAgentPhone;
import com.magnifis.parking.model.MagReply;
import com.magnifis.parking.model.Understanding;
import com.magnifis.parking.model.UnderstandingStatus;
import com.magnifis.parking.phonebook.PhoneBook;
import com.magnifis.parking.suzie.SuziePopup;
import com.magnifis.parking.suzie.SuzieService;
import com.magnifis.parking.tts.MyTTS;
import com.magnifis.parking.utils.Analytics;
import com.magnifis.parking.utils.Translit;
import com.magnifis.parking.utils.Utils;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import static com.robinlabs.utils.BaseUtils.isEmpty;
import static com.robinlabs.utils.BaseUtils.urlencode;

/*
 * sending SMS and EMAIL
 * to activate this module, say:
 * TEXT 
 * TEXT PHONE_NUMBER
 * TEXT RECEIVER_NAME
 * TEXT RECEIVER_NAME TEXT_TO_SEND
 * REPLY (from main window or floating button after command READ NEW MESSAGES)
 * REPLY (from window incoming message)
 * NOTE
 * 
 * behavior of this module depends of CAR MODE and FLAG "smsRepeatDictationText"
 * if CAR MODE is active or this flag is set, then dictation text will told 
 * 
 * */
public class SendCmdHandler 
  extends CmdHandlerBase
  implements 
  	 LocalCommandHandler, 
     Abortable, MagReplyHandler, 
     ClientStateInformer, OnListeningAbortedHandler ,
     OnBeforeListeningHandler,
     OnResumeHandler,
     OnOrientationHandler,
     IHandlesAbortInAnyCase
{
	final static String TAG=SendCmdHandler.class.getSimpleName();
	
	private int state = 0;
	private int silenceCounter=0;
	private int interactionCounter=0;
	private Runnable runAfterInactivate = null;
	
	// wating for name dictation after command "TEXT"
	private final static int DICTATE_NAME = 1;
	
	// waiting for text or command "cancel, clear, delete, send"
	private final static int DICTATE_MSG = 2;
	
	// waiting for yes/no after command (TEXT RECEIVER TEXT_TO_SEND)
	private final static int YES_NO = 3;
	
	// used outside
	public final static int DICTATION_MAX_COUNTER = 2;
	
	public enum AdType {ad_none, ad_vocal, ad_silent};
	
	// full text of dictation
	private class TextState {
		
		String text = "";
		
		// current selection and cursor position
		int sel_start = 0;
		int sel_end = 0;
		
		// new block (marked as bold)
		int new_start = -1;
		int new_end = -1;
	}
	private TextState text_now = new TextState();
	//private TextState text_prev = new TextState();
	private Stack<TextState> text_undo = new Stack<TextState>(); 
	private String last_edit_text = "";
	
	// each iteration through server ?
	private static final boolean DO_DICTATION_THROUGH_SERVER = true;
	
    private  void setState(int newState) {
    	switch (newState) {
		case DICTATE_NAME:
			state = DICTATE_NAME;
			break;
		case DICTATE_MSG:
			state = DICTATE_MSG;
			break;
		case YES_NO:
			state = YES_NO;
			break;
		default:
		    state=0;
			break;
		}
    }
    
	public SendCmdHandler(Context context) {
		super(context);	
		selfWr = new WeakReference<SendCmdHandler>(this);
	}

	
	@Override
	public String getClientStateName() {
		/*
		if (isTimeoutTooLong()) {
			inactivate();
			Log.d(TAG, " deactivating by timeout ");
			return null;
		}*/
		
    	switch (state) {
		case DICTATE_NAME:
			 return ClientStateInformer.SN_DICTATE_NAME;
		case DICTATE_MSG:
             return ClientStateInformer.SN_DICTATE_MSG_NEW;
		case YES_NO:
            return ClientStateInformer.SN_YES_NO;
		}
    	return null;
	}
	
	private Understanding sendCommandUnderstanding=null;
	//private MagReply sendCommandReply=null;

	/********
  <!-- dictate name -->
<reply>
<query>vasya</query>
<gps_location>
<lat>31.843073</lat>
<lon>35.242203</lon>
</gps_location>
<command>dlg_dictation</command>
<message>vasya</message>
<query_interpretation/>
</reply>

    ****/
	@Override
	public boolean handleReplyInBg(MagReply reply) {
		Understanding understanding=reply.getUnderstanding(); 
		
		touchLastInteractionTime();
		boolean nameDictation=state==DICTATE_NAME;
		 
		if (state == YES_NO)
			return true;
		switch(understanding.getCommandCode()) {
		case Understanding.CMD_REPLY:
            (new Analytics(App.self)).trackEvent("sms", "starting reply", null);

            if (state==0&&sendCommandUnderstanding==null) {
				 understanding.setCommandByCode(Understanding.CMD_SEND);
				 understanding.setAction("sms");
				 understanding.setNumber(true);
				   
				 recipient = understanding.getDescription();
				 // reply to email?
				 if (Utils.isEmpty(recipient)) {
					 Message m = App.self.getLastMessageRead();
					 if (m!=null) {
						 if (!Config.allow_to_reply_sms_multiple_times)
							 App.self.setLastMessageRead(null);
						 recipient = m.getSender().getAddress();
					 }
				 }
				 
				 // description in format: "email <name of recipient>" or "name of recipient <email>"
				 if (!Utils.isEmpty(recipient) && recipient.contains("@")) {
					 if (recipient.contains("<")) {
						 int i = recipient.indexOf("<");
						 String recipientName = recipient.substring(i+1);
						 recipient = recipient.substring(0, i).trim();
						 if (!Utils.isEmpty(recipientName)) {
							 recipientName = recipientName.replace('>', ' ').trim();
							 if (!Utils.isEmpty(recipientName)) {
								 if (recipientName.contains("@")) {
									 String swap = recipientName;
									 recipientName = recipient;
									 recipient = swap;
								 }
								 recipientRecord = new ContactRecord();
								 recipientRecord.setName(recipientName);
								 recipientRecord.setPhone(recipient);
								 recipientRecord.loadIcon(null);
							 }
						 }
					 }
					 else
						 recipientRecord = null;
				 }
				 // phone
				 else {
					 if (Utils.isEmpty(recipient)) {
						 fc = SmsFeedController.getInstance();
						 if (fc != null)
							 recipient = fc.getLastMessageSender();
						 
						 if (Utils.isEmpty(recipient)) {
							 inactivate();
							 return true;
						 }
					 }
					 recipientRecord = PhoneBook.getContactWithBestName(recipient);
                     if (recipientRecord != null)
					    recipientRecord.loadIcon(null);
				 }

				 understanding.setDescription(recipient);

                 recipientSource = recipient;

				 return handleReplyInBg(reply);
			 }
			 inactivate();
			 return false;
		case Understanding.CMD_DICTATE_NEW:
			 return true;
		case Understanding.CMD_DICTATE:
			if (!nameDictation) return true;
		case Understanding.CMD_SEND:
            (new Analytics(App.self)).trackEvent("sms", "starting new sms", null);

            if (nameDictation) {
				understanding.fixContactNamesDictation();
				sendCommandUnderstanding.setContactNames(understanding.getContactNames());
				reply.setUnderstanding(understanding=sendCommandUnderstanding);
			} else {
			  if (!"sms".equalsIgnoreCase(understanding.getAction())) {
				inactivate();
				return false;
			  }
			  sendCommandUnderstanding=understanding;
			}
			if (understanding.isPhoneNumberGiven()) {
				recipient=understanding.getDescription();
                recipientSource = recipient;
			} else if (!isEmpty(understanding.getContactNames())) {
				selectRecepientInBg(reply);
			}
			return true;
		}
		Utils.runInGuiAndWait(
		  context, 
		  new Runnable() {
			@Override
			public void run() {
				VoiceIO.fireOpes();
			}
		  }
		);
		abortTtsThenShutdown();
		return false;
	}
	
	private boolean isMail() {
		 return !Utils.isEmpty(recipient) && recipient.contains("@");
	}
	
	@Override
	public boolean handleReplyInUI(MagReply reply) {
		Understanding understanding=reply.getUnderstanding(); 
		if (state == YES_NO) {
			silenceCounter = 0;
			if (understanding.getCommandCode() == Understanding.CMD_YES) {
				setState(DICTATE_MSG);
				sendSMS(recipient, recipientRecord);
			}
			else {
				setState(DICTATE_MSG);
				//full_text.clear();
				if (isMail())
					MyTTS.speakText(new MyTTS.WithoutBubbles(R.string.P_mail_help_say_continue)); 
				else
					MyTTS.speakText(new MyTTS.WithoutBubbles(R.string.P_sms_help_say_continue)); 
				showMessage(null, true);
				VR.useFreeForm = true;
				VoiceIO.listenAfterTheSpeech();
				keyboardVisible = false;
				bgVisible = (App.self.getActiveActivity() != null) && (App.self.getActiveActivity() instanceof MainActivity);
			}
			return true;
		}
		switch(understanding.getCommandCode()) {
		case Understanding.CMD_DICTATE:
			return true;
		case Understanding.CMD_DICTATE_NEW:
			String a = understanding.getAction();
			if (Utils.isEmpty(a))
				a = "unknown";
			doCommand(a, understanding.getMessage(), understanding.getPos());
			return true;
		case Understanding.CMD_NO:
		    MyTTS.speakText(new MyTTS.WithoutBubbles(R.string.P_CANCELLING));
			inactivate();
			VoiceIO.fireOpes();
			return true;
		case Understanding.CMD_SEND:
/*
            if (Robin.isVocaInstalled()) {
                String save_recipient = recipient;
                String name = null;
                if (recipientRecord != null) {
                    name = recipientRecord.getName();
                }
                inactivate();
                Robin.startVoca(save_recipient, name);
                return true;
            }
*/
            if (recipient==null) {
				MyTTS.speakText(R.string.P_SAY_RECEIVER_NAME);
				setState(DICTATE_NAME);
				VoiceIO.listenAfterTheSpeech();
				return true;
			}

			startDictation();
			
			CmdHandlerHolder.setCommandHandler(this);
			
			return true;
		default:
			;
		}
		return false;
	}	

	// add string to full text at current cursor position and move cursor
	private void addText(String text) {
		
		if (Utils.isEmpty(text))
			return;
		
		TextState t = new TextState();
		
		// delete selected
		if (text_now.sel_start < text_now.sel_end) {
			String new_text = "";
			if (text_now.sel_start > 0)
				new_text = text_now.text.substring(0, text_now.sel_start);
			if (text_now.sel_end <= text_now.text.length())
				new_text += text_now.text.substring(text_now.sel_end);
			
			text_now.text = new_text;
			text_now.sel_end = text_now.sel_start;
		}
		
		int insert_pos = text_now.sel_end; 
		
		// add space in tail and move cursor
		if (insert_pos > 0) {
			t.text += text_now.text.substring(0, insert_pos);
			if (!isMarkToRemoveSpaceBefore(text.charAt(0)) && text.charAt(0) != ' ' && t.text.charAt(t.text.length()-1) != ' ')
					t.text += " ";
		}
		
		t.new_start = t.text.length();
		t.text += text;
		t.new_end = t.text.length();
		
		t.sel_start = t.text.length();
		t.sel_end = t.text.length();
		
		if (insert_pos < text_now.text.length()) {
			String end_part = text_now.text.substring(insert_pos);
			if (t.text.charAt(t.text.length()-1) != ' ' && end_part.charAt(0) != ' ')
				t.text += " ";
			t.text += end_part;
		}
		
		text_now = t;
	}
	
	private void doCommand(String command, String text, String newTextPos) {
		interactionCounter++;
		
		text_undo.push(text_now);
		
		if (command.equals("dict_replace")) {
			state = DICTATE_MSG;

			text_now = new TextState();
			text_now.text = text;
			text_now.sel_start = text.length();
			text_now.sel_end = text.length();
			text_now.new_start = 0; 
			text_now.new_end = text_now.sel_end;
			if (!Utils.isEmpty(newTextPos)) {
				try {
					String[] x = newTextPos.split(",");
					text_now.new_start = Integer.parseInt(x[0]); 
					text_now.new_end = Integer.parseInt(x[1]); 
				} catch (Exception e) {}
			}
			
			if (text_now.text.length() == 0)
				if (isMail())
					MyTTS.speakText(new MyTTS.WithoutBubbles(R.string.P_mail_all_deleted));
				else
					MyTTS.speakText(new MyTTS.WithoutBubbles(R.string.P_sms_all_deleted));
			else {
				String s = formatText(text_now.text);
				if (isMail())
					MyTTS.speakText(new MyTTS.WithoutBubbles(App.self.getString(R.string.P_mail_part_deleted)+" "+s));
				else
					MyTTS.speakText(new MyTTS.WithoutBubbles(App.self.getString(R.string.P_sms_part_deleted)+" "+s));
			}

			showMessage(null, true);
			VR.useFreeForm = true;
			VoiceIO.listenAfterTheSpeech();
			return;
		}
		
		if (command.equals("dict_undo") || command.equals("dict_delete")) {
			state = DICTATE_MSG;

			text_undo.pop();
			if (text_undo.size() > 0)
				text_now = text_undo.pop();
			else
				text_now = new TextState();
				
			if (text_now.text.length() == 0)
				if (isMail())
					MyTTS.speakText(new MyTTS.WithoutBubbles(R.string.P_mail_all_deleted));
				else
					MyTTS.speakText(new MyTTS.WithoutBubbles(R.string.P_sms_all_deleted));
			else {
				String s = formatText(text_now.text);
				if (isMail())
					MyTTS.speakText(new MyTTS.WithoutBubbles(App.self.getString(R.string.P_mail_part_deleted)+" "+s));
				else
					MyTTS.speakText(new MyTTS.WithoutBubbles(App.self.getString(R.string.P_sms_part_deleted)+" "+s));
			}

			showMessage(null, true);
			VR.useFreeForm = true;
			VoiceIO.listenAfterTheSpeech();
			return;
		}
		
		if (command.equals("dict_clear")) {
			state = DICTATE_MSG;
			
			text_now = new TextState();
			
			if (isMail())
				MyTTS.speakText(new MyTTS.WithoutBubbles(R.string.P_mail_all_deleted));
			else
				MyTTS.speakText(new MyTTS.WithoutBubbles(R.string.P_sms_all_deleted));
			showMessage(null, true);
			VR.useFreeForm = true;
			VoiceIO.listenAfterTheSpeech();
			return;
		}
		
		if (command.equals("dict_cancel")) {
			abort(0);
			
			return;
		}
		
		if (command.equals("dict_send")) {
			sendSMS(recipient, recipientRecord);
			
			return;
		}
		
		UnderstandingStatus.get().waitingForConfirmation=true;
	
		if (!Utils.isEmpty(text)) {
			if (App.self.isInCarMode() || App.self.getBooleanPref("smsRepeatDictationText"))
				MyTTS.speakText(new MyTTS.WithoutBubbles(text));
			addText(text);
		}
		
		if (!App.self.getBooleanPref(CFG_HELP_SAYED) && false) {
			if (text_now.text.length() == 0)
				if (isMail())
					MyTTS.speakText(new MyTTS.WithoutBubbles(R.string.P_mail_help_say_start));
				else
					MyTTS.speakText(new MyTTS.WithoutBubbles(R.string.P_sms_help_say_start));
			else
				if (isMail())
					MyTTS.speakText(new MyTTS.WithoutBubbles(R.string.P_mail_help_say_continue));
				else
					MyTTS.speakText(new MyTTS.WithoutBubbles(R.string.P_sms_help_say_continue));
			App.self.setBooleanPref(CFG_HELP_SAYED, true);
		}
		VR.useFreeForm = true;
		VoiceIO.listenAfterTheSpeech();

		showMessage(null, true);
	}
	
	public static void startReplyTo(String phone) {
		final MagReply reply = new MagReply();
		Understanding u = new Understanding(Understanding.CMD_REPLY);
		reply.setUnderstanding(u);
	    u.setAction("sms");
	    u.setNumber(true);
	    u.setDescription(phone);
	    
		Intent it=new Intent(MainActivity.INTERPRET_UNDERSTANDING);
		it.putExtra(MainActivity.EXTRA_UNDERSTANDING,u);
		PendingIntent pi = null;
		
		if (MainActivity.isOnTop() || !App.self.shouldUseSuzie()) {
			it.setClass(App.self, MainActivity.class);
			pi = PendingIntent.getActivity(App.self, 0, it, PendingIntent.FLAG_UPDATE_CURRENT);
		}
		else {
			SuzieService.showSuzieNow();
			it.setClass(App.self, SuzieService.class);
			pi = PendingIntent.getService(App.self, 0, it, PendingIntent.FLAG_UPDATE_CURRENT);
		}
		
		try {
			pi.send(); 
		} catch (CanceledException e) {
			e.printStackTrace();
		}
		
	}
	
	private String recipient=null;
    private String recipientSource=null;

	private String answer=null;
	
	private ContactRecord recipientRecord=null;
	
	private static final String CFG_HELP_SAYED = "sms_send_help_sayed"; 

	private CalleeSelectionHandler csh=null;
	
	public String getContactNameToSay(ContactRecord cr) {
		return csh==null?cr.getName():csh.getContactNameToSay(cr);
	}
	
	private void condHideMainActivity() {
		if (MainActivity.get() != null) {
			// show home screen
			Intent it = new Intent(Intent.ACTION_MAIN);
			it.addCategory(Intent.CATEGORY_HOME);
			Utils.startActivityFromNowhere(it);
		}	
	}
	
	private void condShowMainActivity() {
		if (MainActivity.get() != null) {
			Intent it = new Intent(MainActivity.WAKE_UP);
			it.setClass(context, MainActivity.class);
			Utils.startActivityFromNowhere(it);
		}			
	}
	
	/***********************************************************************************************************/
	private void startDictation() {

        SuziePopup.disableBubles();
		SuzieService.showSuzieNow();
		
		setState(DICTATE_MSG);
		keyboardVisible = false;
		bgVisible = (App.self.getActiveActivity() != null) && (App.self.getActiveActivity() instanceof MainActivity);
		
		String recip=DelegateAgentPhone.getAgentByPhone(recipient, context);
		if (isEmpty(recip)) {
			if (recipientRecord != null)
				recip = getContactNameToSay(recipientRecord);
			condHideMainActivity();
		} 
		
		silenceCounter = 0;
		interactionCounter = 0;
		
		text_now = new TextState();
		text_undo.clear();

		
		if (isEmpty(recip)) recip = Utils.phoneNumberToSpeech(recipient).toString();

		if (sendCommandUnderstanding != null && sendCommandUnderstanding.getMessage() != null) {
			
			addText(sendCommandUnderstanding.getMessage());

			if (App.self.isInCarMode() || App.self.getBooleanPref("smsRepeatDictationText") || true) {
				if (isMail()) {
					MyTTS.speakText(new MyTTS.WithoutBubbles(App.self.getString(R.string.P_mail_say_message_for_confirm_car_part1) + " " + recip));
					MyTTS.speakText(new MyTTS.WithoutBubbles(sendCommandUnderstanding.getMessage()));
					MyTTS.speakText(new MyTTS.WithoutBubbles(App.self.getString(R.string.P_mail_help_say_continue)));
				}
				else {
					MyTTS.speakText(new MyTTS.WithoutBubbles(App.self.getString(R.string.P_sms_say_message_for_confirm_car_part1) + " " + recip));
					MyTTS.speakText(new MyTTS.WithoutBubbles(sendCommandUnderstanding.getMessage()));
					MyTTS.speakText(new MyTTS.WithoutBubbles(App.self.getString(R.string.P_sms_help_say_continue)));
				}
			}
			else {
				if (isMail())
					MyTTS.speakText(new MyTTS.WithoutBubbles(App.self.getString(R.string.P_mail_say_message_for_confirm)));
				else
					MyTTS.speakText(new MyTTS.WithoutBubbles(App.self.getString(R.string.P_sms_say_message_for_confirm)));
			}
			//setState(YES_NO);
			//showMessage(null, false);
			//VR.useFreeForm = true;
			//VoiceIO.listenAfterTheSpeech();
			//return;
			
			/*
			if (!App.self.getBooleanPref(CFG_HELP_SAYED)) {
				MyTTS.speakText(new MyTTS.WithoutBubbles(App.self.getString(R.string.P_send_help_say)));
				App.self.setBooleanPref(CFG_HELP_SAYED, true);
			}*/
		}
		else {
			if (isMail())
				MyTTS.speakText(new MyTTS.WithoutBubbles(App.self.getString(R.string.P_mail_say_message_for) + " " + recip));
			else
				MyTTS.speakText(new MyTTS.WithoutBubbles(App.self.getString(R.string.P_sms_say_message_for) + " " + recip));
		}
		
		showMessage(null, true, recip);
		VR.useFreeForm = true;
		VoiceIO.listenAfterTheSpeech();
	}
	
	/***********************************************************************************************************/
	private void selectRecepientInBg(MagReply reply) {
		csh=new CalleeSelectionHandler(context) {
			
			@Override
			public boolean isInSmsMode() {
			  return true;
			}
			
			@Override
			protected void giveUp() {
			  Log.d(TAG, "give up");
			  super.giveUp();
			  SendCmdHandler.this.abortTtsThenShutdown();
			}
			
			@Override
			public boolean clearsCalleeAssociationsManually() {
				return true;
			}

			@Override
			public boolean performFinalAction(ContactRecord r, String cnames[]) {
				recipientRecord=r;
                if (recipientRecord != null)
				    recipientRecord.loadIcon(null);
				return super.performFinalAction(r,cnames);
			}

			@Override
			public boolean performFinalAction(String phoneNumber) {
				touchLastInteractionTime();
				recipient=phoneNumber;
				/*
                if (Robin.isVocaInstalled()) {
                    String save_recipient = recipient;
                    String name = null;
                    if (recipientRecord != null) {
                        name = recipientRecord.getName();
                    }
                    inactivate();
                    SendCmdHandler.this.inactivate();
                    Robin.startVoca(save_recipient, name);
                }
                else*/ {
                    startDictation();
                    inactivate();
                }
                return false;
			}

			@Override
			public String getPerformByNumberString(String number) {
                return null; // we does not support this mode here
			}

			@Override
			public String getActionName() {
				return App.self.getString(R.string.P_text_hint_for_sms);
			}

			@Override
			public Pair<Object[], String> getPerformByContactRecordOutput(ContactRecord r, String q) {
				return null;
			}
		};
		CmdHandlerHolder.pushCommandHandler(csh);
	    sendCommandUnderstanding.setCommandByCode(Understanding.CMD_CALL);
	    csh.handleReplyInBg(reply);		
	}
	
	private Translit translit=Translit.getHebRus();
	
	public static Spannable formatMessageBody(String s) {
	  return Utils.styledString(Utils.firstUpper(s, true),  android.graphics.Typeface.ITALIC);
	}
	
	SmsFeedController fc=null;
	
	boolean waitingForNfy=false;
	
	private  void sendSMS(String recipient, ContactRecord recipientRecord) {
		//String save_recipient = recipient;
		if (isMail()) {
            MyTTS.speakText(R.string.P_mail_finished);
        }
		else {
            MyTTS.speakText(R.string.P_sms_finished);
         /*
            AdType adType = shouldAdvertizeVoca(); 
            if (adType != AdType.ad_none) {
            	if (AdType.ad_vocal == adType) { 
	                if (Math.round(Math.random()) == 1) {
	                    MyTTS.speakText(R.string.try_voca1);
	                }
	                else {
	                    MyTTS.speakText(R.string.try_voca2);
	                }
            	}
                Robin.installVoca();
            }
            */
        }
		inactivate();
	
		if (context instanceof MainActivity) {
		  boolean agentMode=false;
		  String cn=DelegateAgentPhone.getAgentByPhone(recipient, context);
		  if (isEmpty(cn)) cn=recipientRecord!=null?recipientRecord.getName():null; else agentMode=true;
		  if (isEmpty(cn)) cn=recipient;
		  
		  if (agentMode) {
			  
			  /*
			  {
			  			  SpannableString rcps=new SpannableString(cn);
			  rcps.setSpan(new StyleSpan( Typeface.BOLD ), 0, rcps.length(),  Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
				  SpannableStringBuilder ssb=new SpannableStringBuilder("Sending sms to ");
				  ssb.append(rcps);
				  MainActivity.get().bubleAnswer(ssb);
			  }
			  */

			  final SpannableStringBuilder ssb=new SpannableStringBuilder("to ");
			  SpannableString rcps=new SpannableString(cn);
			  rcps.setSpan(new StyleSpan( Typeface.BOLD ), 0, rcps.length(),  Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
			  ssb.append(rcps);			  
			  ssb.append('\n');
			  SpannableString msgby=new SpannableString(formatText(text_now.text));
			  msgby.setSpan(new LeadingMarginSpan.Standard(12, 12), 0, msgby.length(),  Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
			  msgby.setSpan(new StyleSpan( Typeface.ITALIC ), 0, msgby.length(),  Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
			  ssb.append(msgby);
			  
			  final String agentLabel="@"+cn;
			  
			  Utils.runInMainUiThread(
				 new Runnable() {

					@Override
					public void run() {
						  MainActivity.get().addTab(agentLabel, agentLabel);
						  MainActivity.get().bubbleQuery(
						       new SpannedTextBubbleContent(ssb) {
				
								@Override
								public boolean isMenuRestricted() {
									return true;
								}
						    	   
						       }
						    ,
						    agentLabel
						  );	
					}
					 
					 
				 }	  
			  );
			  
			  
		  } /*else*/ {
		  
			  SpannableStringBuilder ssb=new SpannableStringBuilder("Sending sms to ");
			  SpannableString rcps=new SpannableString(cn);
			  rcps.setSpan(new StyleSpan( Typeface.BOLD ), 0, rcps.length(),  Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
			  ssb.append(rcps);
			  ssb.append('\n');
			  SpannableString msgby=new SpannableString(formatText(text_now.text));
			  msgby.setSpan(new LeadingMarginSpan.Standard(12, 12), 0, msgby.length(),  Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
			  msgby.setSpan(new StyleSpan( Typeface.ITALIC ), 0, msgby.length(),  Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
			  ssb.append(msgby);
			  MyTTS.Wrapper w=new MyTTS.Wrapper(ssb).setShowInASeparateBubble();
			  w.setShowInASeparateBubble();
	          MainActivity.get().bubleAnswer(w);
          
		  }
		}
		
		fc=SmsFeedController.getInstance();
		if (!Utils.isEmpty(recipient) && recipient.contains("@")) {
			Intent it = new Intent();
			it.setAction(Intent.ACTION_SENDTO);
			it.setType("text/plain");
			it.setData(Uri.parse("mailto:?to="+recipient+"&subject="+urlencode(App.self.getString(R.string.P_note))+"&body="+urlencode(formatText(text_now.text))));
			Launchers.startNestedActivity(App.self, it);
		} else {
			fc.sendSms(recipient, formatText(text_now.text));
		}
		waitingForNfy=true;
		VoiceIO.fireOpes();
        (new Analytics(App.self)).trackEvent("sms", "send !", null);
	}


	/*
	 * Makes the decision whether Voca ad should be played, based on ad history so far.
	 * Side effect: stored updated history in shared preferences. 
	 */
	private AdType shouldAdvertizeVoca() {
		
		if (Robin.isVocaInstalled())
			return AdType.ad_none; 
			
		final String constNumVocaAds = "num_voca_ads_sofar"; 
		SharedPreferences prefs = App.self.getPrefs();
		int numVocaAdsSoFar = Math.max(0, prefs.getInt(constNumVocaAds, 0)); 
		int oneOutOf = (3 + numVocaAdsSoFar); 
		Random rand = new Random(); 
		if (rand.nextInt(oneOutOf) != 1)
			return AdType.ad_none; 
		
		// ad should be played ==> update history 
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putInt(constNumVocaAds, numVocaAdsSoFar+1);
        prefsEditor.commit();
		
        // do a vocal ad only 1 out of 3 times 
        if (numVocaAdsSoFar % 3 == 0)
        	return AdType.ad_vocal;
        
        // if played more than once, just advertise silently  
        return AdType.ad_silent; 
	}


	@Override
	public void abort(int flags) {
		Log.d(TAG, "abort");
		if (isMail())
			MyTTS.speakText(R.string.P_mail_canceled);
		else {
            MyTTS.speakText(R.string.P_sms_canceled);
            /*
            long rand = Math.round(Math.random() * 3);
            if (!Robin.isVocaInstalled() && (rand == 1)) {
                if (Math.round(Math.random() * 1) == 1) {
                    MyTTS.speakText(R.string.try_voca1);
                }
                else {
                    MyTTS.speakText(R.string.try_voca2);
                }
                Robin.installVoca();
            }
            */
        }
		if (csh != null && recipientRecord != null && interactionCounter == 1)
			csh.clearCalleeAssociations(recipientRecord, false);
		shutdown();
	}
	
	private void abortTtsThenShutdown() {
		MyTTS.abort();
		shutdown();		
	}
	
	//private boolean requires_inactivation=true;
	
	private void inactivate() {
	  Log.d(TAG, "inactivate");
	  if (true) {
		//requires_inactivation=false;
		showMessage(null, false);
		CmdHandlerHolder.removeCommandHandler(this);
		
		SendSmsActivity s = SendSmsActivity.get();
		if (s != null)
			s.showMessage(null);
		
		MainActivity m = MainActivity.get();
		if (m != null)
			m.showMessage(null);
		
		SuziePopup.enableBubles();
		
		SendSmsActivity sa = SendSmsActivity.get();
		if (sa != null)
			sa.finish();
		
		VoiceIO.fireOpes();
		
		recipient = null;
        recipientSource = null;
		
		if (runAfterInactivate != null) {
			Runnable r = runAfterInactivate;
			runAfterInactivate = null;
			r.run();
		}

        (new Analytics(App.self)).trackEvent("sms", "finish", null);
	  }
	}
	
	public static boolean isActive() {
		if (get() == null)
			return false;
		
		if (get().recipient == null)
			return false;
			
		return true;
	}
	
	public static String getText() {
		if (get() == null)
			return null;
		
		if (get().recipient == null)
			return null;
			
		return get().text_now.text;
	}
	
	public static void runAfter(Runnable r) {
		if (get() == null)
			return;
		
		if (get().recipient == null)
			return;
			
		get().runAfterInactivate = r;
	}
	
	protected void shutdown() {
		inactivate();
		VR.get().killMicrophone();
	}

	@Override
	public void onListeningAbortedByBackKeyPressed() {
		//MyTTS.speakText(new MyTTS.WithoutBubbles(R.string.P_press_the_back_key__textsting));
	}

	@Override
	public void onBeforeListening() {
	   if (state==DICTATE_MSG) {
		   Log.d(TAG, "onBeforeListening, state==DICTATE_MSG");
		   VR.useFreeForm=true;
		   if (sendCommandUnderstanding!=null)
		      VR.useLanguage=sendCommandUnderstanding.getLanguage();
	   }
		
	}

	@Override
	public boolean onVoiceInput(List<String> matches, boolean partial) {
		
		if (partial) {
			if (matches == null || matches.size() == 0 || Utils.isEmpty(matches.get(0)))
				return true;
			
			showMessage(matches.get(0), true);
			
			return true;
		}
		
    	/*****************************************************************************************/
    	// waiting for dictation or confirmation...
		if (state == DICTATE_MSG) {
			if (matches == null || matches.size() == 0 || Utils.isEmpty(matches.get(0))) {
				if (silenceCounter++ >= 1) {
					/*
					if (isMail())
						MyTTS.speakText(R.string.P_mail_canceled);
					else
						MyTTS.speakText(R.string.P_sms_canceled);
					inactivate();
					*/
					return true;
				}
				else {
					if (text_now.text.length() == 0)
						if (isMail())
							MyTTS.speakText(new MyTTS.WithoutBubbles(R.string.P_mail_help_say_start));
						else
							MyTTS.speakText(new MyTTS.WithoutBubbles(R.string.P_sms_help_say_start));
					else
						if (isMail())
							MyTTS.speakText(new MyTTS.WithoutBubbles(R.string.P_mail_help_say_continue));
						else
							MyTTS.speakText(new MyTTS.WithoutBubbles(R.string.P_sms_help_say_continue));
					App.self.setBooleanPref(CFG_HELP_SAYED, true);
					VR.useFreeForm = true;
					VoiceIO.listenAfterTheSpeech();
					return true;
				}
			}
			
			silenceCounter = 0;

			if (DO_DICTATION_THROUGH_SERVER)
				return false;
			
			String command = "unknown";
			if (checkVoiceCommand(matches, R.string.P_mail_cmd_delete, R.string.P_sms_cmd_delete))
				command = "dict_delete";
			else if (checkVoiceCommand(matches, R.string.P_mail_cmd_clear, R.string.P_sms_cmd_clear))
				command = "dict_clear";
			else if (checkVoiceCommand(matches, R.string.P_mail_cmd_cancel, R.string.P_sms_cmd_cancel))
				command = "dict_cancel";
			else if (checkVoiceCommand(matches, R.string.P_mail_cmd_finish, R.string.P_sms_cmd_finish))
				command = "dict_send";
			doCommand(command, matches.get(0), null);
			
			return true;
		}

		if (state == DICTATE_NAME) {
			if (matches == null || matches.size() == 0 || Utils.isEmpty(matches.get(0)))
				abort(0);
		}
					
		return false;
	}

	private boolean isMarkToRemoveSpaceBefore(char x) {
		if (x == ':' || x == '.' || x == ';' || x == ',' || x == '!' || x == '?')
			return true;
		else
			return false;
	}
	
	private boolean isMarkToUpperCase(char x) {
		if (x == '.' || x == '!' || x == '?')
			return true;
		else
			return false;
	}
	
	private String formatText(String s) {
		String itogo = s;
		if (Utils.isEmpty(itogo))
			return itogo;

		String itogo2 = "";
		boolean waitingMark = false;
		for (int i = 0; i<itogo.length(); i++) {
			Character x = itogo.charAt(i);
			if (waitingMark) {
				if (isMarkToUpperCase(x))
					waitingMark = false;
			}
			// waiting char to upper
			else {
				if (Character.isLetter(x)) {
					x = Character.toUpperCase(x);
					waitingMark = true;
				}
			}
			itogo2 += x;
		}
		return itogo2;
	}
	
	boolean isClickInSelection(EditText te, float x, float y) {
		if (te.getSelectionStart() >= te.getSelectionEnd())
			return false;
		
		if (te.getLayout() == null)
			return false;
			
		int [] location = new int[2];
		te.getLocationOnScreen(location);
		x -= location[0];
		y -= location[1];

		// find line
        y -= te.getTotalPaddingTop();
        y = Math.max(0.0f, y);
        y = Math.min(te.getHeight() - te.getTotalPaddingBottom() - 1, y);
        y += te.getScrollY();
        int line = te.getLayout().getLineForVertical(Math.round(y));
        
        // find pos in line
        x -= te.getTotalPaddingLeft();
        x = Math.max(0.0f, x);
        x = Math.min(te.getWidth() - te.getTotalPaddingRight() - 1, x);
        x += te.getScrollX();
        int pos = te.getLayout().getOffsetForHorizontal(line, x);

        Log.d("CLICK", "x y "+Math.round(x)+" "+Math.round(y)+
        		" sel: "+te.getSelectionStart()+" "+te.getSelectionEnd()+
        		" count: "+te.length()+" click: "+line+" "+pos);
        
        if (pos > te.length())
        	return false;
        
        if (pos < te.getSelectionStart())
        	return false;
        
        if (pos > te.getSelectionEnd())
        	return false;
        
        return true;
	}
	
	boolean waitingUp = false;
	
	OnTouchListener mOnEditTouch = new OnTouchListener() {
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {

			// ignore clicks on empty text
			if (Utils.isEmpty(text_now.text))
				return true;
			
			if (event.getAction() == MotionEvent.ACTION_DOWN) {

				// click on selection deletes text 
		    	if (isClickInSelection((EditText) v, event.getRawX(), event.getRawY())) {
		    		mOnEditClick.onClick(v);
		    		waitingUp = true;
		    		return true;
		    	}
		    	
			}
			
			if (event.getAction() == MotionEvent.ACTION_UP && waitingUp) {

				waitingUp = false;
	    		return true;
		    	
			}
			return false;
		}
	};
	
	// on edit click - select word or delete selected word
	OnClickListener mOnEditClick = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			if (recipient == null)
				return;
			
	    	EditText te = (EditText) v;
	    	int i1 = te.getSelectionStart();
	    	int i2 = te.getSelectionEnd();
	    	String s = te.getText().toString();
	    	
	    	// selection at end
	    	if (i1 >= s.length()) {
	    		i1 = s.length();
	    		i2 = i1;
	    	}
	    	else
	    	// selection empty
	    	if (i1 == i2) {

	    		// if this space, shift left
	    		if (i1 > 0 && s.charAt(i1) == ' ') {
	    			i1--;
	    			i2--;
	    		}
	    			
	    		// select word!
		    	while (i1 > 0 && s.charAt(i1) != ' ') i1--;
		    	if (i1 > 0)
		    		i1++;
		    	while (i2 < s.length() && s.charAt(i2) != ' ') i2++;
		    	if (i1 > i2)
		    		return;
		    	
	    	}
	    	// selection not empty
	    	else {
	    		// delete selection!
	    		if (i1 > 0 && s.charAt(i1-1) == ' ')
	    			i1--;
		    	s = s.substring(0, i1) + s.substring(i2);
		    	
		    	// remove double spaces
		    	int i = 0;
		    	while (i < s.length()-1) {
		    		if (s.charAt(i) == ' ' && s.charAt(i+1) == ' ' && i != i1-1) {
		    			s = s.substring(0, i) + s.substring(i+1);
		    			if (i < i1)
		    				i1--;
		    		}
		    		i++;
		    	}
		    	i2 = i1;
	    	}
	    	
	    	while (!Utils.isEmpty(s) && s.charAt(0) == ' ') {
	    		s = s.substring(1);
	    		if (i1 > 0) {
	    			i1--;
	    			i2--;
	    		}
	    	}
	    	
	    	text_undo.push(text_now);
	    	
			text_now = new TextState();
	    	text_now.text = s;
	    	text_now.sel_start = i1;
	    	text_now.sel_end = i2;
	    	text_now.new_start = -1;
	    	text_now.new_end = -1;
	    	
	    	showMessage(null, true);
	    	
	    	//if (!VR.isListening() && !keyboardVisible)
	    		//VR.get().start(false);
		}
	};
	
	// on edit changed by keyboard - update internal variables
	OnClickListener mOnEditChanged = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			if (recipient == null)
				return;

			EditText te = (EditText) v;
	    	int i1 = te.getSelectionStart();
	    	int i2 = te.getSelectionEnd();
	    	String s = te.getText().toString();
	    	if (i1 < 0)
	    		i1 = 0;
	    	if (i1 > s.length())
	    		i1 = s.length();
	    	if (i2 < 0)
	    		i2 = 0;
	    	if (i2 > s.length())
	    		i2 = s.length();
	    	
	    	if (last_edit_text.equalsIgnoreCase(s))
	    		return;
	    	
	    	//if (true)
	    		//text_undo.push(text_now);
	    	
	    	TextState ts = new TextState();
	    	if (text_undo.size() > 0)
	    		ts = text_undo.peek();
	    	
	    	text_now = new TextState();
	    	text_now.text = s;
	    	text_now.sel_start = i1;
	    	text_now.sel_end = i2;
	    	text_now.new_start = -1;//findDifferenceFromStart(text_now.text, ts.text);
	    	text_now.new_end = -1;//findDifferenceFromEnd(text_now.text, ts.text);
	    	
	    	//showMessage(null, true);
		}
	};
	
	OnClickListener mOnSendButtonClick = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if (recipient == null)
				return;
			
			MyTTS.abort();
			sendSMS(recipient, recipientRecord);
			condShowMainActivity();
		}
	};
	
	OnClickListener mOnCancelButtonClick = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if (recipient == null) return;
			
			if (recipientRecord!=null)
			   csh.clearCalleeAssociations(recipientRecord, true/*remove existing associations too*/);
			
			abort(0);
			condShowMainActivity();
		}
	};
	
	boolean keyboardVisible = false;
	boolean bgVisible = false;
	
	OnClickListener mOnKeyboardButtonClick = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if (recipient == null)
				return;
			
			keyboardVisible = !keyboardVisible;
			if (VR.isListening())
				VR.get().abort();
			
			showMessage(null, true);
		}
	};
	
	public static String getPhone() {
		SendCmdHandler sch = get();
		if (sch == null || !isActive())
			return null;
		if (sch.recipientRecord == null)
			return sch.recipient;
		else
			return sch.recipientRecord.getPhone();
	}

    public static String getRecipient() {
        SendCmdHandler sch = get();
        if (sch == null || !isActive())
            return null;
        if (sch.recipientRecord == null)
            return sch.recipient;
        else
            return sch.recipientRecord.getName();
    }

    protected int findDifferenceFromEnd(String s, String text) {
		if (Utils.isEmpty(s) || Utils.isEmpty(text))
			return 0;
				
		int i = 0;
		while (true) {
			if (i >= s.length() || i >= text.length())
				break;
			if (Character.toUpperCase(s.charAt(s.length()-i-1)) != Character.toUpperCase(text.charAt(text.length()-i-1)))
				break;
			i++;
		}

		return s.length()-i;
	}

	protected int findDifferenceFromStart(String s, String text) {
		if (Utils.isEmpty(s) || Utils.isEmpty(text))
			return 0;
				
		int i = 0;
		while (true) {
			if (i >= s.length() || i >= text.length())
				break;
			if (Character.toUpperCase(s.charAt(i)) != Character.toUpperCase(text.charAt(i)))
				break;
			i++;
		}

		return i;
	}

	public static void showAnswer(String text) {
		SendCmdHandler sch = get();
		if (sch == null || !isActive() || Utils.isEmpty(text))
			return;

		sch.keyboardVisible = false;
		sch.answer = text;
		sch.showMessage(null, true);
	}
	
	// show current message to send
	
	public void showMessage(String partialText, boolean showHelp) {
		showMessage(partialText, showHelp, null );
	}
	
	public void showMessage(String partialText, boolean showHelp, String recip) {
		if (recip==null) {
			recip=DelegateAgentPhone.getAgentByPhone(recipient, context);
			if (isEmpty(recip)) {
				if (recipientRecord == null) {
					if (isEmpty(recip))  recip = recipient;
				} else
					recip = getContactNameToSay(recipientRecord);
			}
		}
		
		if (Utils.isEmpty(recip))
			return;
		
		recip = recip.trim();
		
		if (Utils.isEmpty(recip))
			return;
		
		SuziePopup.BubleMessage bm = new SuziePopup.BubleMessage();

		/******************** recipient ***********************/
		if (answer != null)
			bm.answer.append(answer);
		
		bm.name.append(Utils.firstUpper(recip, false));
		if (recipientRecord != null) {
			bm.addr.append(recipientRecord.getPhone());
            bm.icon = recipientRecord.getIcon();
        }
		
		/******************** text ***********************/
		int insert_pos = text_now.sel_end;
		String itogo = text_now.text;
		
		// if partial text is empty - use changes from last undo
		if (!Utils.isEmpty(text_now.text) && !Utils.isEmpty(partialText)) {
			if (insert_pos > 0)
				partialText = " " + partialText;
			else
				partialText = partialText + " ";
			
			// delete selected
			if (text_now.sel_start < text_now.sel_end) {
				if (text_now.sel_start > 0)
					itogo = text_now.text.substring(0, text_now.sel_start);
				if (text_now.sel_end <= text_now.text.length())
					itogo += text_now.text.substring(text_now.sel_end);
				
				insert_pos = text_now.sel_start;
			}					
		}
		
		if (!Utils.isEmpty(partialText)) {
			// itogo.insert(insert_pos, partialText);
			if (insert_pos >= itogo.length())
				itogo += partialText;
			else {
				String s;
				if (insert_pos > 0)
					s = itogo.substring(0, insert_pos);
				else
					s = "";
				s += partialText;
				s += itogo.substring(insert_pos);
				itogo = s;
			}
			
			itogo = formatText(itogo);
		}
		else
			itogo = formatText(itogo);
		
		bm.text.append(itogo);
		
		// mark HOT text as bold
		if (!Utils.isEmpty(partialText)) {
			int insert_end = insert_pos + partialText.length();
			if (bm.text.charAt(insert_pos) == ' ')
				insert_pos++;
			if (insert_pos < bm.text.length()) {
				bm.sel_start = insert_pos;
				bm.sel_end = insert_end;
				bm.text.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 
						insert_pos, insert_end, 
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
		else
			// mark NEW text as bold
			if (showHelp)
				if (text_now.new_start >= 0 
				&& text_now.new_end > text_now.new_start 
				&& text_now.new_end <= text_now.text.length()) {
					bm.text.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 
							text_now.new_start, text_now.new_end, 
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
		
		// selection
		bm.sel_start = text_now.sel_start;
		bm.sel_end = text_now.sel_end;
		if (!Utils.isEmpty(partialText)) {
			bm.sel_start += partialText.length();
			if (text_now.text.length() > 0)
				bm.sel_start++;
			bm.sel_end = bm.sel_start;
		}
		
		// if text is empty write hint
		if (bm.text.length() == 0) {
			if (!keyboardVisible) {
				if (isMail())
					bm.text.append(App.self.getString(R.string.P_mail_help_waiting));
				else
					bm.text.append(App.self.getString(R.string.P_sms_help_waiting));
				
				bm.text.setSpan(new ForegroundColorSpan(android.graphics.Color.GRAY), 
						0, bm.text.length(), 
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
		else
			// add space at end
			if (bm.text.charAt(bm.text.length()-1) != ' ') {
				bm.text.append(' ');
				if (bm.sel_start >= bm.text.length()-1) {
					bm.sel_start = bm.text.length();
					bm.sel_end = bm.text.length();
				}
			}
		
		/******************** hint ***********************/
		if (showHelp && false) {
			String help;
			if (isMail())
				help = App.self.getString(R.string.P_mail_help_show);
			else
				help = App.self.getString(R.string.P_sms_help_show);
			bm.hint.append(help);
			/*
			bm.hint.setSpan(new ForegroundColorSpan(android.graphics.Color.GRAY), 
					bm.hint.length()-help.length(), bm.hint.length(), 
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			bm.hint.setSpan(new TextAppearanceSpan(context, android.R.style.TextAppearance_Small), 
					bm.hint.length()-help.length(), bm.hint.length(), 
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					*/
		}

		/**********************************************/
		
		bm.onEditTouch = mOnEditTouch;
		bm.onEditClick = mOnEditClick;
		bm.onEditChanged = mOnEditChanged;
		bm.onSendButtonClick = mOnSendButtonClick;
		bm.onCancelButtonClick = mOnCancelButtonClick;
		bm.onKeyboardButtonClick = mOnKeyboardButtonClick;
		bm.keyboardVisible = keyboardVisible;
		bm.bgVisible = bgVisible;
		
		if (bm.sel_start > bm.text.length()) {
			bm.sel_start = bm.text.length();
			bm.sel_end = bm.sel_start;
		}
		
		last_edit_text = bm.text.toString();

		//MainActivity m = MainActivity.get();
		
		if (keyboardVisible) {
			SendSmsActivity s = SendSmsActivity.get();
			if (s == null || !s.isActive) {
				Intent it=new Intent(SendSmsActivity.SEND_SMS);
				it.setClass(App.self, SendSmsActivity.class);
				it.putExtra("bg", bgVisible);
				PendingIntent pi = PendingIntent.getActivity(App.self, 0, it, PendingIntent.FLAG_UPDATE_CURRENT);
				
				try {
					pi.send(); 
				} catch (CanceledException e) {
					e.printStackTrace();
				}
				
			}
			
			if (s != null)
				s.showMessage(bm);
		}
		else {
			SuzieService.messageBubble(bm);
		}
	}

	private boolean checkVoiceCommand(List<String> matches, int cmdVarListMail, int cmdVarListSMS) {
		String strVarList;
		if (isMail())
			strVarList = App.self.getString(cmdVarListMail);
		else
			strVarList = App.self.getString(cmdVarListSMS);
		if (Utils.isEmpty(strVarList))
			return false;
		
		String[] arrVarList = strVarList.toLowerCase().split("/");
		
		for (String s : matches) {
			if (Utils.isEmpty(s))
				continue;
			
			for (String s2 : arrVarList) {
				if (!Utils.isEmpty(s2) && s.toLowerCase().equals(s2))
					return true;
			}
		}
		return false;
	}
	
	private static WeakReference<SendCmdHandler> selfWr=null;
	
	public static SendCmdHandler get() {
		return selfWr==null?null:selfWr.get();
	}

	@Override
	public void onResume() {
		// show home screen
		condHideMainActivity();
		
		// show state
		showMessage(null, true);
	}

	@Override
	public void OnOrientationChanged() {
		// show state
		showMessage(null, true);
	}
	
}
