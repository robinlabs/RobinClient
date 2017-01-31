package com.magnifis.parking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.LeadingMarginSpan;
import android.text.style.StyleSpan;

import com.magnifis.parking.cmd.SendCmdHandler;
import com.magnifis.parking.feed.SmsFeedController;
import com.magnifis.parking.messaging.Addressable;
import com.magnifis.parking.messaging.Message;
import com.magnifis.parking.model.DelegateAgentPhone;
import com.magnifis.parking.model.Understanding;
import com.magnifis.parking.tts.MyTTS;
import com.magnifis.parking.utils.Utils;
import static com.magnifis.parking.utils.Utils.*;

import java.util.List;

public class SMSReceiver extends BroadcastReceiver {
	
	final static String  TAG=SMSReceiver.class.getSimpleName();
	
	public final static String SMS_ARRIVED="com.magnifis.parking.SMS_ARRIVED";
	
	public static boolean anyNewSms() {
	   return newSmsCounter>0;
	}

	public static void resetSmsCounter() {
	   newSmsCounter=0;
	}
	
	private static int newSmsCounter=0;
	
	public SMSReceiver() {
		// TODO Auto-generated constructor stub
	}

	private void startReadingSms(final Intent intent,   Message m,  final List<Message> messageList) {
		++newSmsCounter;
		
		if (m!=null) {
		  Addressable ad=m.getSender();
		  if (ad!=null) {
			  final String phoneNumber=ad.getAddress();
			  if (!isEmpty(phoneNumber)) {
				  Utils.runInBgThread(
					new Runnable() {

						@Override
						public void run() {
							final String agent=DelegateAgentPhone.getAgentByPhone(phoneNumber, App.self);
							if (!isEmpty(agent)) {
								
								final String phones[]=DelegateAgentPhone.getPhoneNumbers(agent, App.self);
								
								final StringBuilder 
								  sp=new StringBuilder(Utils.getString(R.string._from_)).append(' ').append(agent).append('\n')
								      .append(SmsFeedController.getSmsPartForSpeach(messageList,SmsFeedController.PLAY_MODE_BODY_ONLY));
								
								SpannableString rcps=new SpannableString(agent);
								rcps.setSpan(new StyleSpan( Typeface.BOLD ), 0, rcps.length(),  Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
								
								String tBody=Utils.toString(SmsFeedController.getSmsPartToShow(messageList, SmsFeedController.PLAY_MODE_BODY_ONLY));
								
								final boolean emailRequired=tBody.contains("with the email address");
								
								SpannableString msgby=new SpannableString(tBody);
								msgby.setSpan(new LeadingMarginSpan.Standard(12, 12), 0, msgby.length(),  Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
								msgby.setSpan(new StyleSpan( Typeface.ITALIC ), 0, msgby.length(),  Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
								
								final SpannableStringBuilder 
								  sh=new SpannableStringBuilder(Utils.getString(R.string._from_)).append(' ').append(rcps).append('\n')
								      .append(msgby)
								;
								
								final String aid="@"+agent;
								
								MainActivity.wakeUp();
								
								App.self.voiceIO.getOperationTracker().queOperation(
										  new Runnable() {

											@Override
											public void run() {
												Object w=new MyTTS.Wrapper(
              											new Output.Arg() {
              					                  			
              												@Override
              												public Object toSpeech() {
              													// TODO Auto-generated method stub
              													return sp;
              												}
              			
              												@Override
              												public Object toShow() {
              													// TODO Auto-generated method stub
              													return sh;
              												}

              												@Override
              												public void onSaid(boolean fAborted) {
              													super.onSaid(fAborted);
              													MainActivity.get().bubblesBreak(aid);
              													if (!fAborted&&emailRequired) try {
              														Intent it = new Intent();
              														it.setClass(App.self, MainActivity.class);
              														it.setAction(MainActivity.INTERPRET_UNDERSTANDING);
              														Understanding u = new Understanding()
              														    .setCommandByCode(Understanding.CMD_DELEGATE_AGENT)
              														    .setAction("sms")
              														    .setMessage(App.self.getGmailAccountName())
              														    .setDontSaveAgentPhones(true)
              														    .setPhoneNumbers(phones);
              														it.putExtra(MainActivity.EXTRA_UNDERSTANDING, u);
              														Utils.startActivityFromNowhere(it);
              													} catch (Throwable t) {
              														;
              													}
              												}
              												
              											}
              									  ).setShowInASeparateBubble();
												
												
												MainActivity ma=MainActivity.get();
												if (ma.addTab(aid, aid)==null) return;
												ma.setCurrentTab(aid);
												ma.bubblesBreak(aid);
												ma.bubleAnswer(sh, aid);
                                                Output.sayAndShow(App.self.getActiveActivity(), w);
												
											}
											  
										  },
										  true
							    );
								/*
								Output.sayAndShowFromGui(MainActivity.get(),
									  new MyTTS.Wrapper(
											new Output.Arg() {
			
												@Override
												public Object toSpeech() {
													// TODO Auto-generated method stub
													return sp;
												}
			
												@Override
												public Object toShow() {
													// TODO Auto-generated method stub
													return sh;
												}

												@Override
												public void onSaid(boolean fAborted) {
													super.onSaid(fAborted);
													if (!fAborted&&emailRequired) try {
														Intent it = new Intent();
														it.setClass(App.self, MainActivity.class);
														it.setAction(MainActivity.INTERPRET_UNDERSTANDING);
														Understanding u = new Understanding()
														    .setCommandByCode(Understanding.CMD_DELEGATE_AGENT)
														    .setAction("sms")
														    .setMessage(App.self.getGmailAccountName())
														    .setDontSaveAgentPhones(true);
														it.putExtra(MainActivity.EXTRA_UNDERSTANDING, u);
														Utils.startActivityFromNowhere(it);
													} catch (Throwable t) {
														;
													}
												}
												
											}
									  ).setShowInASeparateBubble()
								  , false
								);
								*/
								return;
							}
							///////////////////////////////
						    if (!App.self.shouldSpeakIncomingSms()) return;
							Intent it=new Intent(SMS_ARRIVED);
							it.putExtra(Intent.EXTRA_INTENT, intent);
							
							if (MainActivity.isOnTop()||
									( Utils.getRobinTaskIndex(App.self, 2)!=null && Utils.isForegroundActivity(App.self, SmsActivity.class.getName()))) {
								//it.setClass(App.self, MainActivity.class);
								it.putExtra(MainActivity.HANDLE_VIA_MAIN_ACTIVITY, true);
							}
							
							it.setClass(App.self,  Config.use_sms_notification_service?SmsNotificationService.class:SmsActivity.class);
							
							if (Config.use_sms_notification_service)
							   App.self.startService(it);
							else {
								it.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
								Utils.startActivityFromNowhere(it);
							}
							///////////////////////////////
						}
						
					}
				  );
			  }
		  }	
		}
		/*
		Intent it=new Intent(SMS_ARRIVED);
		it.putExtra(Intent.EXTRA_INTENT, intent);
		
		if (MainActivity.isOnTop()||
				( Utils.getRobinTaskIndex(App.self, 2)!=null && Utils.isForegroundActivity(App.self, SmsActivity.class.getName()))) {
			//it.setClass(App.self, MainActivity.class);
			it.putExtra(MainActivity.HANDLE_VIA_MAIN_ACTIVITY, true);
		}
		
		it.setClass(App.self,  Config.use_sms_notification_service?SmsNotificationService.class:SmsActivity.class);
		
		if (Config.use_sms_notification_service)
		   App.self.startService(it);
		else {
			it.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
			Utils.startActivityFromNowhere(it);
		}
		*/
	}
	
	@Override
	public void onReceive(Context context, final Intent intent) {
		Log.d(TAG, "onReceive");
        SmsFeedController.pauseBeforeReading = true;

        final List<Message> messageList = SmsFeedController.from(intent);

        if(messageList==null){return;}

        final Message m =messageList.get(0);
		// if send sms window active...
		if (m != null && SendCmdHandler.isActive()) {
			/*
			String phone1 = SendCmdHandler.getPhone();
			String phone2 = m.getSender().getAddress();
			if (!Utils.isEmpty(phone1) && !Utils.isEmpty(phone2) && PhoneNumberUtils.compare(phone1,phone2)) {
				SendCmdHandler.showAnswer(m.getBody());
				App.self.setLastMessageRead(m);
				smsIntents.remove(0);
			}
			else*/
				SendCmdHandler.runAfter(new Runnable() {
					
					@Override
					public void run() {
					       startReadingSms(intent, m, messageList);
					}
				});
			return;
		}
	    startReadingSms(intent, m, messageList);
	}

}
