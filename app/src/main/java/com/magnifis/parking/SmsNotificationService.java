package com.magnifis.parking;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.w3c.dom.Element;

import com.magnifis.parking.cmd.SendCmdHandler;
import com.magnifis.parking.cmd.i.ClientStateInformer;
import com.magnifis.parking.cmd.i.UnderstandingHandler;
import com.magnifis.parking.feed.SmsFeedController;
import com.magnifis.parking.messaging.Addressable;
import com.magnifis.parking.messaging.Message;
import com.magnifis.parking.model.QueryInterpretation;
import com.magnifis.parking.model.Understanding;
import com.magnifis.parking.suzie.SuzieService;
import com.magnifis.parking.suzie.SuzieService.ApplicationMonitorAction;
import com.magnifis.parking.toast.MagToast;
import com.magnifis.parking.toast.ToastBase;
import com.magnifis.parking.tts.MyTTS;
import com.magnifis.parking.utils.Translit;
import com.magnifis.parking.utils.Utils;
import com.magnifis.parking.views.ProgressSpinner;
import com.magnifis.parking.views.SmsAlertView;

import static com.magnifis.parking.tts.MyTTS.speakText;
import static com.magnifis.parking.utils.Utils.*;
import static com.robinlabs.utils.BaseUtils.isEmpty;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.IntentService;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognizerIntent;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;

public class SmsNotificationService extends Service implements TextMessageQue {
	final static String TAG=SmsNotificationService.class.getSimpleName();
	
	final static 
	  int MODE_NEW_SMS=0, MODE_CONFIRM_READING=1, MODE_READING_CONFIRMED=2,
	      MODE_CONFIRM_REPLY=3, MODE_REPLY_CONFIRMED=4, MODE_DICTATE_MESSAGE=5, 
	      MODE_CONFIRM_THAT_MESSAGE_IS_RIGHT=6
	  ;
	
	final static String ACTION_COMPOSER="com.magnifis.parking.COMPOSER";
	
	final static int DICTATION_MAX_COUNTER = SendCmdHandler.DICTATION_MAX_COUNTER;
	
	private int mode=MODE_NEW_SMS;
	
	VR vr=null;
	ProgressSpinner mSpinner = null;
	
	private SmsFeedController feedController=null;
	private Object fcSO=new Object();
	
	private SmsFeedController getController() {
	    synchronized(fcSO) {	
	      if (feedController==null) feedController=SmsFeedController.getInstance();
	    }
		return feedController;
	}

	@Override
	public void onDestroy() {
        SuzieService.showSuzieNow();
		super.onDestroy();
	}
	
	boolean instant, handle_via_main_activity;
	
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	boolean _onCreate=true;

	@Override
	public int onStartCommand(final Intent it, int flags, int startId) {
		if (it!=null) {
			Log.d(TAG,"onStartCommand");
			if (_onCreate) {
				instant=App.self.shouldReadSmsInstantly();
				handle_via_main_activity=it.getBooleanExtra(MainActivity.HANDLE_VIA_MAIN_ACTIVITY, false);	
			}
			if (!handle_via_main_activity) 
				//VR.createSuzie(SmsNotificationService.class);
			    Utils.runInMainUiThread(
					new Runnable() {
						@Override
						public void run() {
							handleIntent(it,_onCreate);
						}
					}
			);
			_onCreate=false;
			return Service.START_STICKY;
		}
		return super.onStartCommand(it, flags, startId);
	}
	
	/**
	 * @see android.app.Activity#onCreate(Bundle)
	 */
	
	private List<Intent> smsIntents=new ArrayList<Intent>();
	/*
	private void playSms() {  
		if (!isEmpty(smsIntents)) {
			  List<Message> lst=SmsFeedController.from(smsIntents.get(0));
			  if (!isEmpty(lst)) {
				  //getController().play(lst,true,null);
				  //fc.read(1, null, true, false, null);
				  getController().play(lst,true,null,SmsFeedController.PLAY_MODE_NORMAL);
			  }
			}		
	}
	*/
	
	private StringBuilder getSmsToSpeak() {
		return getSmsPartToSpeak(SmsFeedController.PLAY_MODE_NORMAL);
	}
	
	private StringBuilder getSmsToShow() {
		return getSmsPartToShow(SmsFeedController.PLAY_MODE_NORMAL);
	}
	
	private Message getCurrentSms() {
		if (!isEmpty(smsIntents)) {
			  List<Message> lst=SmsFeedController.from(smsIntents.get(0));
			  if (!isEmpty(lst)) return lst.get(0);
			}			
	   return null;	
	}
	
	private StringBuilder getSmsPartToSpeak(int part) {
		if (!isEmpty(smsIntents))
		      return SmsFeedController.getSmsPartForSpeach(SmsFeedController.from(smsIntents.get(0)), part );
	   return null;
	}
	
	private Addressable getSmsSender() {
		if (!isEmpty(smsIntents)) {
			  List<Message> lst=SmsFeedController.from(smsIntents.get(0));
			  if (!isEmpty(lst)) {
				  return lst.get(0).getSender();
			  }
			}			
	   return null;		
	}
	
	private StringBuilder getSmsPartToShow(int part) {
		   if (!isEmpty(smsIntents)) 
				  return SmsFeedController.getSmsPartToShow(SmsFeedController.from(smsIntents.get(0)), part );
		   return null;
		}	

	
	private StringBuilder getSmsHeaderToSpeak() {
		return getSmsPartToSpeak(SmsFeedController.PLAY_MODE_HEADER_ONLY);
	}
	
	private StringBuilder getSmsHeaderToShow() {
		return getSmsPartToShow(SmsFeedController.PLAY_MODE_HEADER_ONLY);
	}
	
	private StringBuilder getSmsBodyToSpeak() {
		return getSmsPartToSpeak(SmsFeedController.PLAY_MODE_BODY_ONLY);
	}
	
	private StringBuilder getSmsBodyToShow() {
		return getSmsPartToShow(SmsFeedController.PLAY_MODE_BODY_ONLY);
	}
	
	void cancelWaitingForListeningResults() {
		if (vr!=null) {
			  if (listeningTimeoutTask!=null) {
				  listeningTimeoutTask.cancel();
				  listeningTimeoutTask=null;
			  }
			  vr.killMicrophone();
			  vr=null;
			}		
	}
	
	void handleIntent(Intent it, final boolean fOnCreate) {
		if (App.self.isInSilentModeOrConversation()) {
			if (fOnCreate) doFinish(); 
			return;
		}
		
		if (MainActivity.VR_RESULTS.equals(it.getAction())) {
			onActivityResult(VR.VOICE_RECOGNITION_REQUEST_CODE, Activity.RESULT_OK, it);
			return;
		}
		
		if (ACTION_COMPOSER.equals(it.getAction())) {
		   MyTTS.abortWithoutUnlock();
		   composeTextMessage(dictatedMessage, getCurrentSms().getSender().getAddress());
		   return;
		}
		
		
		if (RESULT.equals(it.getAction())) {
			Utils.dump(TAG, it);
			onActivityResult(
			    VR.VOICE_RECOGNITION_REQUEST_CODE,
			    it.getIntExtra(MainActivity.LISTENING_RESULT_CODE,0),
			    (Intent)it.getParcelableExtra(MainActivity.LISTENING_RESULT)
			);
			return;
		}
		
		if (SmsFeedController.INTENT_SENT_OR_FAILED.equals(it.getAction())) {
			getController().handleSendingStatusIntent(this,this, it);
			return;
		}
		
		if (!SMSReceiver.SMS_ARRIVED.equals(it.getAction())) {
			if (fOnCreate) doFinish(); 
			return;
		}
		
		 cancelWaitingForListeningResults();
		
		 synchronized(smsIntents) {
		    smsIntents.add((Intent)it.getParcelableExtra(Intent.EXTRA_INTENT));
		    if (smsIntents.size()>1) return;
		 }
		  /*
		 if (!handle_via_main_activity&&fOnCreate) {
		   new VRHeatingUp(
			  new Runnable() {
				@Override
				public void run() {
					playSingleSmsAndAfter(true);
				}
			  }
		   ); 
		 } else*/
		   startNewSmsProcessing();
	}
	
	private void sendMessageAndAfter() {
		final Addressable sender=getSmsSender();
		Output.sayAndShow(
		   this, 
		   new MyTTS.OnSaidListener() {

			@Override
			public void onSaid(boolean fAborted) {
			  if (fAborted)	handleNextOrDie(); else {
				  Intent nfyIntent=new Intent(SmsFeedController.INTENT_SENT_OR_FAILED);
				  nfyIntent.putExtra(
					 SmsFeedController.COMPONENT, 
					 new ComponentName(SmsNotificationService.this,SmsNotificationService.class)
				  );
				  getController().sendSms(sender.getAddress(), dictatedMessage, nfyIntent);
			  }
			}

			@Override
			public String toString() {
				return getString(R.string.mainactivity_onpostexecute_texting)
				   +" "
				   + translit.process(sender.getSynteticDisplayName(true));
			}
		  }
		);
	
	}
	
	private Translit translit=Translit.getHeb();
	
	private void replyConfirmed() {
		
		fireOpes();
		doFinish();
		
		Addressable sender=getSmsSender();
		if (sender != null)
			SendCmdHandler.startReplyTo(sender.getAddress());
		
		/*
		String lang = null; 
		if (!isEmpty(smsIntents)) {
			  List<Message> lst=SmsFeedController.from(smsIntents.get(0));
			  if (!isEmpty(lst))
				  lang = SmsFeedController.getSmsLanguage(lst.get(0));
			}			

		if (Utils.isEmpty(lang))
			Output.sayAndShow(this, R.string.P_SAY_YOUR_MSG);
		else
			Output.sayAndShow(this, App.self.getString(R.string.P_SAY_YOUR_MSG_LANG) + " " + lang);
				
	   final String lang2 = lang;
	   
	   mode=MODE_DICTATE_MESSAGE;
	   MyTTS.execAfterTheSpeech(
		 new Runnable() {
			@Override
			public void run() {
			  listenForDictation(lang2);	
			}
		 }
	   );
	   */
	}
	
	private void playConfirmed() {
	   playSingleSmsAndAfter(false);
	}
	
	private void confirmReadingOrFinishSingleSmsProcessing(boolean shouldConfirmReading) {
		  Log.d(TAG,"confirmReadingOrFinishSingleSmsProcessing");
		  synchronized(smsIntents) {
			if (smsIntents.isEmpty()) {
				fireOpes();
				doFinish();
				return;
			}
		  }
		  if (instant) {
			 if (smsIntents.size()>1) {
				startNewSmsProcessing(); return;
			 } 
			 fireOpes();
			 doFinish();
		  } else {
			  mode=MODE_CONFIRM_READING;
		      if (!shouldConfirmReading&&!instant) {
		    	// confirm reading of a next message
				Utils.runInMainUiThread(
				  new Runnable() {

					@Override
					public void run() {
						Output.sayAndShow(SmsNotificationService.this,R.string.mainactivity_new_message);
						MyTTS.execAfterTheSpeech(
								  new Runnable() {
									@Override
									public void run() {
										listenForConfirmation();
									}
								  }
					    );								
						
					}
					  
				  }
				);
			 } else {
				// confirm reading of a first message
				listenForConfirmation();
			 }
		}
	}
	
	private void confirmDesireToReplyAndAfter() {
		Log.d(TAG, "confirmDesireToReplyAndAfter");
		mode=MODE_CONFIRM_REPLY;
		listenForConfirmation();
	}
	
	private void playSingleSmsAndAfter(
	  final boolean shouldConfirmReading
	) {
		hideSmsAlert();
		
		boolean voiced_mode=!App.self.isInSilentMode();
		
		final boolean locked=App.self.isPhoneLocked()||App.self.isInPhoneConversation(),
				      lockedNotMA=locked&&!handle_via_main_activity;

		StringBuilder toSay=new StringBuilder(), toShow=new StringBuilder();
		
		if (shouldConfirmReading) {
			  MainActivity m = MainActivity.get();
			  if (m != null)
				  m.bubblesBreak("#main"); //imp
        //      toSay.append(" . ");
		}
		
		final boolean fReadMessage=instant||!shouldConfirmReading;
		
		if (fReadMessage) {
			if (instant) {
			   toShow.append(getSmsToShow());
			   toSay.append(" . ");
			   toSay.append(getSmsToSpeak());
			} else {
			   toShow.append(getSmsBodyToShow());
			   toSay.append(getSmsBodyToSpeak());
			}
			if (!lockedNotMA && App.self.shouldSpeakIncomingSms()) {
				toSay.append(" . ");
				toSay.append(getString(R.string.P_want_to_reply));
			}
		} else 
		if (!instant) {
			toShow.append(getSmsHeaderToShow());
			toSay.append(getSmsHeaderToSpeak());
		}
		
		if (isEmpty(toShow)) toShow=toSay;
		
		MyTTS.Wrapper x=new MyTTS.Wrapper(toSay) {
			@Override
			public void onSaid(boolean fAborted) {
				super.onSaid(fAborted);
				Log.d(SmsNotificationService.TAG,"mark as read");
				if (!fAborted&&(fReadMessage||(!instant))) App.self.setLastMessageRead(getCurrentSms());
				
				if (!App.self.isPhoneLocked()&&(currentAlert!=null)) {
					final SmsAlertView av=currentAlert.getContentView();

					av.post(
							new Runnable() {
								@Override
								public void run() {
									if (!handle_via_main_activity)
										av.setUpperPartVisibility(false);
								}
							}
					);
				}
			}
		};
		
		showSmsAlert(getCurrentSms());
		
		if (voiced_mode) {
		  MyTTS.speakText(x);
		  MyTTS.execAfterTheSpeech(
			new Runnable() {
				@Override
				public void run() {
				   if (App.self.isPhoneLocked()) {
					   /*
					   if (handle_via_main_activity) {
						   MainActivity.wakeUp(true);
					   }
					   */
					   if (!handle_via_main_activity)
					      return;
					    
					  // return;
				   }
				   if (fReadMessage) {
				     confirmDesireToReplyAndAfter();
				   } else
				     confirmReadingOrFinishSingleSmsProcessing(shouldConfirmReading);
				}
			}
		  );
		}
	}
	
	private Timer     timer=new Timer();
	private TimerTask listeningTimeoutTask=null;
	
	boolean in_listening=false;
	
	private void listen(long timeout, boolean useFreeForm, String lang) {
		 if (!handle_via_main_activity) {
			 vr = VR.get();
			 
			 in_listening=true;
			 VR.useFreeForm=useFreeForm;
			 VR.useLanguage=lang;
			 vr.start(true);	
			 if (timeout>0)
			    timer.schedule(
					 listeningTimeoutTask=new TimerTask() {
						 @Override
						 public void run() {
							 vr.killMicrophone();
							 listeningTimeoutTask=null;
							 in_listening=false;
						 }
					 }, 
					 timeout
			 );		
		 } else {
			Intent it = new Intent(MainActivity.SAY_SHOW_AND_LISTEN), rs = new Intent(
					RESULT);
			it.setClass(this, MainActivity.class);
			rs.setClass(this, this.getClass());
			rs.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
			it.putExtra(MainActivity.LISTEN, rs);
			it.putExtra(MainActivity.WORK_WITHOUT_QUE, workWithQue);
			if (timeout > 0)
				it.putExtra(MainActivity.LISTEN_TIMEOUT, timeout);
			it.putExtra(MainActivity.LISTEN_USE_FREE_FORM, useFreeForm);
			it.putExtra(MainActivity.LISTEN_LANG, lang);
			Utils.startActivityFromNowhere(it);
			in_listening = true;
		 }
		
	}
	
	private void listenForDictationConfirmation() {
	   listen(0,false,null);
	}
	
	private void listenForConfirmation() {
	   listen(TIMEOUT_FOR_CONFIRMATION,false,null);
	}
	
	private void listenForDictation(String lang) {
	   listen(TIMEOUT_FOR_DICTATION,true,lang);
	}
	
	private long TIMEOUT_FOR_CONFIRMATION=15000l, TIMEOUT_FOR_DICTATION=0;
	
	static final String RESULT = "com.magnifis.parking.RecogtitionResult";
	
	
	public void onActivityResult(int requestCode, int resultCode, Intent it) {
		Log.d(TAG, "onActivityResult");
    	switch (requestCode) {
    	case MainActivity.TASK_TERMINATE_REQUEST_CODE:
    		handleNextOrDie();
    		break;
    	case VR.VOICE_RECOGNITION_REQUEST_CODE:
    		// ignore partial results!
    		if (resultCode == Activity.RESULT_FIRST_USER)
    			return;
            if (resultCode == VR.RESULT_RUN_INTENT)
                return;
    		in_listening=false;
    		hideSmsAlert();
    		cancelWaitingForListeningResults();
    		Log.d(TAG, "VR_LOOKUP_REQUEST_CODE");
    		if (resultCode==Activity.RESULT_OK&&it!=null) {
    			List<String> matches = it.getStringArrayListExtra(
    					RecognizerIntent.EXTRA_RESULTS
    			);
    			if (!isEmpty(matches)) {
    				switch(mode) {  
    				case MODE_CONFIRM_REPLY:
    				case MODE_CONFIRM_READING:
    				case MODE_CONFIRM_THAT_MESSAGE_IS_RIGHT:
    				  askForMagnifisYesNo(matches);
    				  break;
    				case MODE_DICTATE_MESSAGE:
    				  askForMagnifisDictation(matches);
    				  break;
    				}
    			    Log.d(TAG,"We have a voice command");
    			    return;
    			}
    		}
    		switch(mode) {
    		case MODE_DICTATE_MESSAGE:
    		  handleNextOrDieVerbose(); break;
    		default:
    		  handleNextOrDie();
    		}
    		return;
    	}
	}	
	

	
	private XMLFetcher<Understanding> fetcher=null;
	
	private void showProgress() {
		if (handle_via_main_activity) {
		} else {
		//	SuzieService.sendSuzie(ApplicationMonitorAction.BUSY_SUZIE);
		}
		/*
	   if (mSpinner==null) mSpinner=new ProgressSpinner(SmsNotificationService.this);
	   mSpinner.show();		
	   */
	}
	
	private void hideProgress() {
		if (handle_via_main_activity) {
		} else {
		//	SuzieService.sendSuzie(ApplicationMonitorAction.FREE_SUZIE);
		}
		/*
	  if (mSpinner!=null) {
 		mSpinner.dismiss();
		mSpinner=null;		
	  }*/
	}
	
	private String dictatedMessage=null;
	
	private void confirmThatDicationIsRight() {
		MyTTS.speakText(R.string.P_YOU_SAID);
		Output.sayAndShow(this, dictatedMessage);
		MyTTS.speakText(R.string.P_is_that_right);
		MyTTS.execAfterTheSpeech(
		  new Runnable() {
			@Override
			public void run() {
			  mode=MODE_CONFIRM_THAT_MESSAGE_IS_RIGHT;
			  listenForDictationConfirmation();
			}
		  }		
		);
	}
	
	private void askForMagnifisDictation(List<String> ss) {
		  askForMagnifisUnderstanding(
			ss,
			ClientStateInformer.SN_DICTATE_MSG,
			new UnderstandingHandler() {
				@Override
				public boolean handleUnderstanding(Understanding u) {
					if (u.getCommandCode()==Understanding.CMD_DICTATE) {
						dictatedMessage=u.getMessage();
						if (isEmpty(dictatedMessage)) {
                            handleNextOrDie();
						} else 
							confirmThatDicationIsRight();
				    } else {
				    	if (handle_via_main_activity) {
				    	// TODO: forward the understanding 
				    	// to the main packageName

				        }
				    	handleNextOrDieVerbose();
				    }
					return true;
				}
			}
		 );
	} 
	
	private  void askForMagnifisYesNo(List<String> ss) {
		  askForMagnifisUnderstanding(
			ss,
			ClientStateInformer.SN_YES_NO,
			new UnderstandingHandler() {

				@Override
				public boolean handleUnderstanding(Understanding u) {
					if (u!=null) {
						int cmd=u.getCommandCode();
						if (cmd==Understanding.CMD_READ&&
								Utils.isOneFrom(mode,MODE_CONFIRM_READING,MODE_CONFIRM_REPLY)) 
						{
					        playConfirmed();
					        return true;				
						} else
						if (cmd==Understanding.CMD_REPLY&&
								Utils.isOneFrom(mode,MODE_CONFIRM_READING,MODE_CONFIRM_REPLY)) 
						{
					    	replyConfirmed();
					    	return true;					
						} else
						if (cmd==Understanding.CMD_YES||
						    cmd==Understanding.CMD_DO_IT
						) {
						   Log.d(TAG, "we have a magnifis response");
						   switch (mode) {
						      case MODE_CONFIRM_READING:
						        playConfirmed();
						        return true;
						      case MODE_CONFIRM_REPLY:
						    	replyConfirmed();
						    	return true;
						      case MODE_CONFIRM_THAT_MESSAGE_IS_RIGHT:
						    	sendMessageAndAfter();
						    	return true;
						   }
						   return true;
					    } else if (
					      mode==MODE_CONFIRM_THAT_MESSAGE_IS_RIGHT&&
					      cmd==Understanding.CMD_NO
					    ) {
					      if (u.isCancel())
					    	handleNextOrDieVerbose();
					      else {
					    	if (++dictation_counter<DICTATION_MAX_COUNTER)
					          replyConfirmed(); else {
					        	    Addressable sender=getCurrentSms().getSender();
									speakText(R.string.P_YOU_CAN_SEND_MESSAGE);
									composeTextMessage(
										dictatedMessage,
										sender.getAddress()
									);
					          }
					      }
					      return true;
					    } else {
					    	if (cmd==Understanding.CMD_NO) {
					    	   handleNextOrDieVerbose(mode!=MODE_CONFIRM_READING);
					    	} else {
					    	   MyTTS.speakText(R.string.P_YES_OR_NO);
					    	   MyTTS.execAfterTheSpeech(
					    	    new Runnable() {
								 @Override
								 public void run() {
									if (mode==MODE_CONFIRM_THAT_MESSAGE_IS_RIGHT)
									  listenForDictationConfirmation();
									else
									  listenForConfirmation();
									
								 }
					    	    }
					     	   );
					    	}
					    }
					} else
						handleNextOrDie();
					return true;
				}
				
			}
		  );
	}	
	
	private  void  askForMagnifisUnderstanding(
		final List<String> ss,
		final String clientState,
	    final UnderstandingHandler uh
	) {
		   showProgress();
	       try {
	    	   fetcher=new XMLFetcher<Understanding>() {
				
				@Override
				protected InputStream invokeRequest(URL u, String pd, String ref, String userAgent)throws IOException {
					return super.invokeRequest( 
							  RequestFormers.createMagnifisUnderstandingRqUrl(
									  null,
									  ss,
									  clientState
							 ),						
							pd, ref, userAgent
				);
				}

				@SuppressLint("NewApi") protected Understanding consumeXmlData(Element root) {
					if (root!=null) try {
					    return Xml.setPropertiesFrom(root, Understanding.class);
					} catch (Throwable e) {
					   e.printStackTrace();
					   fireOpes();
					   doFinish();
				    }
					return null;
			    }
				
				

				@Override
				protected void onCancelled() {
					hideProgress();
					super.onCancelled();
					fireOpes();
					doFinish();
				}

				@Override
				protected void onPostExecute(Understanding u) {
					super.onPostExecute(u);
					hideProgress();
					uh.handleUnderstanding(u);
					//handleNextOrDie();
				}
				
				
			  };
			  fetcher.execute(  
				  null,
				  null,
				  null
			  );
		   } catch (Throwable e) {
			  hideProgress();
			  e.printStackTrace();
		   };
	}	
	
	int dictation_counter=0;
	
	private void handleNextOrDieVerbose(boolean verbose) {
		if (verbose) 
			handleNextOrDieVerbose();
		else
			handleNextOrDie();
	}
	
	private void handleNextOrDieVerbose() {
		MyTTS.speakText(R.string.P_CANCELLING);
		MyTTS.execAfterTheSpeech(
		  new Runnable() {

			@Override
			public void run() {
				 handleNextOrDie();				
			}
			  
		  }
		);
	}
	
	private void composeTextMessage(String smsBody, String phone) {
		Intent it = new Intent();
		it.setAction(Intent.ACTION_SENDTO);
		it.setType("text/plain");
		
		String advert = App.self.getString(R.string.adv_for_sms);
		if (!App.self.shouldAdvertInSms())
			advert = ""; 
		if (smsBody!=null) {
			if (smsBody.length() + advert.length() < 140)
				smsBody += "\n" + advert;  
		} else {
			smsBody = "\n" + advert;
		}
		it.putExtra("sms_body", smsBody);
  
		StringBuilder sb=new StringBuilder("sms:");
		if (phone!=null) sb.append(phone);
		it.setData(Uri.parse(sb.toString()));
		
		ResultProxyActivity.startActivityFromServiceForResult(
		   it,
		   MainActivity.TASK_TERMINATE_REQUEST_CODE, 
		   SmsNotificationService.class
		 );
	}
	
	private boolean handleNextOrDie() {
		Log.d(TAG, "handleNextOrDie");
		fireOpes();
		mode=MODE_NEW_SMS;
		synchronized(smsIntents) {
			if (smsIntents.size()>1) {
				smsIntents.remove(0);
				Utils.runInMainUiThread(
				   new Runnable() {
					  @Override
					  public void run() {
						dictation_counter=0; dictatedMessage=null;
						startNewSmsProcessing();
					  }
				   }
				);
				return true;
			} else
				doFinish();
		}
		return false;
	}
	
	boolean workWithQue=true;
	
	void fireOpes() {
		  if (handle_via_main_activity&&workWithQue) {
			  VoiceIO.fireOpes();
		  }
	}
	
	void runWhenInactive(Runnable r, boolean fromGui) {
	  if (handle_via_main_activity&&workWithQue) {
		  /*
		 MainActivity ma=MainActivity.get();
		 if (ma!=null) {
			 ma.queOperation(r, fromGui);
			 return;
		 }
		 */
		  App.self.voiceIO.getOperationTracker().queOperation(r, fromGui);
		  return;
	  }
	  r.run();
	}
	
	void startNewSmsProcessing() {
		runWhenInactive(
		  new Runnable() {	
			@Override
			public void run() {
		       playSingleSmsAndAfter(true);
			}
		  },
		  true
		);
	}
	
	@Override
	public void queTextMessage(Context ctx,final Object o) {
		Output.sayAndShow(
				ctx, 
				new MyTTS.Wrapper(o) {

					@Override
					public void onSaid(boolean fAborted) {
						super.onSaid(fAborted);
						handleNextOrDie();
					}

				}
		);				
	}
	
	/*
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			MyTTS.abortWithoutUnlock();
			return handleNextOrDie();
		}
		return super.onKeyDown(keyCode, event);
	}
	*/
	
	MagToast currentAlert=null;
	
	void hideSmsAlert() {
		MagToast mt=currentAlert;
		if (mt!=null) {
			mt.hide();
			currentAlert=null;
		}
	}
	
	MagToast showSmsAlert(final Message m) {
		final MagToast tc[]={null};
		Utils.runInGuiAndWait(
				new Runnable() {
					
					@Override
					public void run() {
						SmsAlertView pv=App.self.createFromLayout(R.layout.sms_alert);
						pv.setData(
					      m,
				          // on mic
				          //shouldHandleInSilentMode()
				            //?
				            new Runnable() {
					    	  @Override
					    	  public void run() {
					    		 hideSmsAlert();
					    		 MyTTS.abortWithoutUnlock();
					    		 replyConfirmed();
					    	  }
					        }
					      //:null
					      ,
				          // on Text
						  new Runnable() {
							@Override
							public void run() {  
							   hideSmsAlert();
							   /*
							   MyTTS.abortWithoutUnlock();
							   composeTextMessage(dictatedMessage, m.getSender().getAddress());
							   */
							   try {
								   Intent it=new Intent(ACTION_COMPOSER);
								   it.setClass(App.self, SmsNotificationService.class);
								   startService(it);
							   } catch(Throwable t) {}
							}
						  },
						  // on close
						  new Runnable() {
							@Override
							public void run() {
								hideSmsAlert();
								MyTTS.abortWithoutUnlock();
								handleNextOrDie();
							}
						  }		  
						);
						ToastBase.LayoutOptions lo= new ToastBase.LayoutOptions();
						lo.contentPadding=new Rect(0,40,0,0);
						tc[0]=new MagToast(pv,lo,null, false);
					    tc[0].show();
					}
				}
		);
		return (currentAlert=tc[0]);
	}
	
	private void doFinish() {
	   Log.d(TAG,"doFinish");
	   if (handle_via_main_activity) MainActivity.cancelListeningFor();
	   stopSelf();		
	}


}
