package com.magnifis.parking.voice;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;

import com.att.android.speech.ATTSpeechError;
import com.att.android.speech.ATTSpeechError.ErrorType;
import com.att.android.speech.ATTSpeechActivity;
import com.att.android.speech.ATTSpeechResult;
import com.att.android.speech.ATTSpeechService;
import com.att.android.speech.ATTSpeechAudioLevelListener;
import com.att.android.speech.ATTSpeechResultListener;
import com.att.android.speech.ATTSpeechStateListener;
import com.att.android.speech.ATTSpeechErrorListener;
import com.magnifis.parking.App;
import com.magnifis.parking.Config;
import com.magnifis.parking.Launchers;
import com.magnifis.parking.Log;
import com.magnifis.parking.MainActivity;
import com.magnifis.parking.R;
import com.magnifis.parking.SuccessFailure;
import com.magnifis.parking.VR;
import com.magnifis.parking.cmd.etc.CmdHandlerHolder;
import com.magnifis.parking.cmd.i.OnBeforeListeningHandler;
import com.magnifis.parking.suzie.SuziePopup;
import com.magnifis.parking.toast.ToastController;
import com.magnifis.parking.tts.MyTTS;
import com.magnifis.parking.utils.OACredentials;
import com.magnifis.parking.utils.Utils;

/**
 * Created by oded on 7/3/14.
 */
public class AtntVR extends VR implements
  ATTSpeechAudioLevelListener, 
  ATTSpeechResultListener, 
  ATTSpeechStateListener, 
  ATTSpeechErrorListener 
{
	
    final static String
       CLIENT_ID="k7ac7vmolg4inqojt1xtevrgcdytha01",
       CLINET_SECRET="xy00zqzh71ipbohecidnqpbwm7ktfjcx";
    
    final static URI SPEECH_URI=URI.create("https://api.att.com/speech/v3/speechToText");
	
	final private OACredentials oct=new OACredentials("https://api.att.com/oauth/token", CLIENT_ID, CLINET_SECRET, "SPEECH,STTC"); 
	
	private ATTSpeechService speechSvc=null;

    public AtntVR(Object activity, IAnimator animator) {
        super(activity, animator);
        oct.keepItHot();
        if (activity!=null&&activity instanceof Activity) 
        	speechSvc=ATTSpeechService.getSpeechService((Activity)activity);
        Log.d(VR.TAG_SPEECH,"Created Atnt Voice recognizer");
    }

	@Override
	public synchronized void open(boolean standart_microphone) {
		// for testing with standart microphone
		//standart_microphone = true;
		
		Log.d(TAG_SPEECH, "ATT.open...");
		logState(0);

		if (state.state != CLOSED) {
			Log.d(TAG_SPEECH, "open: INVALID STATE !!!!!!!!!!!!");
			return;
		}
				
		this.standart_microphone = standart_microphone;
		if (!this.standart_microphone) {
			if (Config.oldmic||speechSvc==null)
				this.standart_microphone=true;
		}
		
		logState(STOPPED);
		
		if (speechSvc!=null) {
		  speechSvc.setSpeechResultListener(this);
		  speechSvc.setSpeechErrorListener(this); 
		  speechSvc.setAudioLevelListener(this);
		  speechSvc.setSpeechStateListener(this);
		  speechSvc.setRecognitionURL(SPEECH_URI); 
		  speechSvc.setShowUI(false);
		  speechSvc.setSpeechContext("Generic");
		}
/*
     BusinessSearch
    Gaming
    Generic
    QuestionAndAnswer
    SMS
    SocialMedia
    TV
    VoiceMail
    WebSearch
 */
		

		if (animator != null)
			Utils.runInMainUiThread(
					  new Runnable() {
							@Override
							public void run() {
								if (animator != null)
									animator.showDone();
							}
					  }
				);
		
		detectIfBluetoothConnected();
		
	}
	

	@Override
	protected void startRecognizition() {
		Log.d(TAG_SPEECH, "startRecognizition...");
		logState(0);
		/*
		Utils.runInMainUiThread(
				  new Runnable() {
						@Override
						public void run() {
*/
							// if some speaking now, than stop !!!
							MyTTS.abortWithoutUnlock();
							
							/*****************************************************************************/

							realReplaceTeaching = useReplaceTeaching && !useFreeForm;
							useReplaceTeaching = true;
							
							/*****************************************************************************/

							if (useFreeForm) {
								/*
								vri.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
								vri.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
								vri.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
								//autoRestartRecognition = true; // using serial dictation
								 * 
								 */
								speechSvc.setSpeechContext("Generic");
								autoRestartRecognition = false; 
								autoRestartWithLanguage = useLanguage;
							}
							else {
								/*
								vri.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);
								vri.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 10);
								vri.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
								*/
								speechSvc.setSpeechContext(/*"WebSearch"*/"Generic"); // does not work properly w/WebSearch
								autoRestartRecognition = false;
							}
							realFreeForm = useFreeForm;
							useFreeForm = false;							
							
							/*****************************************************************************/
							String newLang = null;
							if (Utils.isEmpty(useLanguage)) {
								Log.d(TAG_SPEECH, "DEFAULT LANGUAGE "+App.self.getString(R.string.P_lang));
								newLang = App.self.getString(R.string.P_shortlang);
							}
							else {
								Log.d(TAG_SPEECH, "LANGUAGE "+useLanguage);
								newLang = useLanguage.substring(0, 2);
							}
							if (Utils.isEmpty(newLang))
								newLang = "en";
							if (newLang.equals("en"))
								newLang = "en-US";
							// add language parameter only if not equal to default
							if (!newLang.substring(0, 2).equals(defLanguage.substring(0,2))) {
								/*
								//vri.putExtra(RecognizerIntent.EXTRA_LANGUAGE, newLang);
								Map<String,String> hm=speechSvc.getRequestHeaders();//new HashMap<String,String>();
								hm.put("Content-Language", newLang);
								if (!newLang.startsWith("en")) speechSvc.setSpeechContext("Generic");
								speechSvc.setRequestHeaders(hm);
								Log.d(TAG_SPEECH, "SET LANGUAGE "+newLang);
								*/
							} else {
							    Log.d(TAG_SPEECH, "LANGUAGE NOT SET BECOUSE VR USES "+defLanguage);
							}
							
							useLanguage = null;
							/*****************************************************************************/
							
							//vri.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, test); 
							//vri.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, test); // 10 min
							//vri.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, test);
							
							//test *= 10; 
							//float[] confidence = {0.9f, 0.8f, 0.7f, 0.6f, 0.5f, 0.4f, 0.3f, 0.2f, 0.1f, 0.01f};
							//vri.putExtra(RecognizerIntent.EXTRA_CONFIDENCE_SCORES, confidence); 
		
							if (!standart_microphone) {
								oct.doWithToken(
										  new SuccessFailure<String>() {
											@Override
											public void onSuccess(String token) {
												speechSvc.setBearerAuthToken(token);
												speechSvc.startListening();
												Log.d(TAG_SPEECH, "startRecognizition: start listening");
											}
											
										  }
							    );
							} else {
								/*
								if (activity instanceof Activity)
									((Activity) activity).startActivityForResult(vri, VOICE_RECOGNITION_REQUEST_CODE);
								else {
									PendingIntent pi = PendingIntent.getActivity(App.self, VOICE_RECOGNITION_REQUEST_CODE, vri, PendingIntent.FLAG_UPDATE_CURRENT);
									try {
										pi.send();
									} catch (CanceledException e) {
									}
								}
								*/
								logState(STOPPED);
								Log.d(TAG_SPEECH, "startRecognizition: sent start intent");
							}							
	/*					}
				  }
			);
		*/
		Log.d(TAG_SPEECH, "startRecognizition: ok");
	}

	@Override
	public synchronized void start(boolean askToInstallRecognizer) {
		
		Log.d(TAG_SPEECH, "ATT.start... "+state.state);
		logState(0);
		
		SuziePopup.resetAutoHideTimer();
		
		if (state.ringActive || App.self.isInPhoneConversation()) {
			Log.d(TAG_SPEECH, "start: ring active!!");
			return;
		}
		
       OnBeforeListeningHandler oblh = CmdHandlerHolder.getOnBeforeListeningHandler();
       if (oblh!=null) oblh.onBeforeListening();
		
		if (state.state == CLOSED) {
			Log.d(TAG_SPEECH, "start: NOT OPENED !!!");
			return;
		}

		if ((state.state != STOPPED) 
				&& (state.state != STOPPING) 
				&& (state.state != STOPPED_WAITING_SCO) 
				&& (state.state != STOPPED_CANCEL_SCO) 
				&& (state.state != ABORTING)) {
			Log.d(TAG_SPEECH, "start: INVALID STATE");
			return;
		}
		
		soundFinishedPlayed = false;
		
		// if stop not finished eat, then wait!!!
		if ((state.state == STOPPING) || (state.state == ABORTING)) {
			//state.restartRecognitionAfterStop = true;
			Log.d(TAG_SPEECH, "start: recognizer busy - RECREATE VR");
			startRecognizition();					
			if (animator != null) 
				Utils.runInMainUiThread(
				   new Runnable() {
					   @Override
					   public void run() {
						  animator.showDone();
					   }
				   }
				);
		} else {
				
			mute();
			
			if (useBt && isBlueToothConnected() && state.deviceUsedSco 
					&& state.state == STARTING_WAITING_SCO)
				logState(STARTING_WAITING_SCO);
			else 
				startNow();
		}
    	
		Log.d(TAG_SPEECH, "start: ok");
	}

	@Override
	public synchronized void close() {

		Log.d(TAG_SPEECH, "close...");
		logState(0);
		
		if ((state.state == STARTED) 
				|| (state.state == STARTING_COMMON) 
				|| (state.state == STARTING_BLUETOOTH) 
				|| (state.state == STARTING_WAITING_SCO)) {
			Log.d(TAG_SPEECH, "close: CORRECT STATE");
			abort();
		}

		if ((state.state != STOPPED) 
				&& (state.state != STOPPING) 
				&& (state.state != STOPPED_WAITING_SCO) 
				&& (state.state != STOPPED_CANCEL_SCO) 
				&& (state.state != ABORTING)) {
			Log.d(TAG_SPEECH, "close: INVALID STATE !!!!!!!!!!!!");
			return;
		}
		
		logState(CLOSED);

		if (soundButtonClicked.isPlaying())
			soundButtonClicked.pause();
		if (soundThinking.isPlaying())
			soundThinking.pause();
		
		//App.selfunregisterReceiver(this);

		//AudioManager am = (AudioManager) App.selfgetSystemService(Context.AUDIO_SERVICE);
		//am.setMode(AudioManager.MODE_NORMAL);
    	//Log.d(TAG_SPEECH, "NORMAL");
		
    	if (speechSvc != null) {
    		speechSvc.cancel();
    	//	speechSvc = null;
    	}
    	
		
		Log.d(TAG_SPEECH, "close: ok");
	}

	@Override
	public synchronized void stop() {
		Log.d(TAG_SPEECH, "stop...");
		logState(0);

		if ((state.state == STOPPING) || (state.state == ABORTING) || (state.state == STARTING_COMMON)) {
			abort();
			return;
		}
		
		if (state.state != STARTED) {
			Log.d(TAG_SPEECH, "stop: INVALID STATE !!!!!!!!!");
			return;
		}
		
		logState(STOPPING);
		state.restartRecognitionAfterStop = false;
		
		if (!soundFinishedPlayed) {
			soundFinishedPlayed = true;
			Log.d(TAG_SPEECH, "Play mic sound finished");
			startSound(soundFinished);
		}
		
		if (speechSvc != null) {
			// advanced recognition
			Log.d(TAG_SPEECH, "stop listening");
			try {
			  speechSvc.stopListening();
			} catch(Throwable t) {}
		}
		else
			// standart microphone
			onResults(null);
		
		Utils.runInMainUiThread(
			  new Runnable() {
					@Override
					public void run() {
						if (animator != null)
							animator.showDone();
					}
			  }
		);
		
		Log.d(TAG_SPEECH, "stop: ok");
	}

	@Override
	public synchronized void abort() {

		Log.d(TAG_SPEECH, "abort...");
		logState(0);
		
		if ((state.state == STOPPED) 
				|| (state.state == STOPPED_WAITING_SCO)
				|| (state.state == STOPPED_CANCEL_SCO)
				|| (state.state == CLOSED)) {
			Log.d(TAG_SPEECH, "abort: already stopped");
			return;
		}

		if ((state.state != STARTED)
				&& (state.state != STARTING_COMMON) 
				&& (state.state != STARTING_BLUETOOTH) 
				&& (state.state != STARTING_WAITING_SCO)
				&& (state.state != STOPPING) 
				&& (state.state != ABORTING)) {
			Log.d(TAG_SPEECH, "abort: INVALID STATE !!!!!!!!!!!!!!!");
			return;
		}
		
		useFreeForm = false;
		realFreeForm = false;
		useLanguage = null;
		useReplaceTeaching = true;
		
		if ((state.state == STOPPING) || (state.state == ABORTING)) {

			logState(STOPPED);
			state.restartRecognitionAfterStop = false;

			if (animator != null)
				Utils.runInMainUiThread(
						  new Runnable() {
								@Override
								public void run() {
									if (animator != null)
										animator.showDone();
								}
						  }
					);
			
			return;
		}
		
		logState(ABORTING);
		state.restartRecognitionAfterStop = false;
		
		if (!soundFinishedPlayed) {
			soundFinishedPlayed = true;
			Log.d(TAG_SPEECH, "Play mic sound finished");
			startSound(soundFinished);
		}
		
		if (speechSvc != null) {
			// advanced recognition
			Log.d(TAG_SPEECH, "stop listening");
			try {
			  speechSvc.cancel();
			} catch(Throwable t) {
				
			}
		}
		else
			// standart microphone
			onResults(null);
		
		Utils.runInMainUiThread(
			  new Runnable() {
					@Override
					public void run() {
/*
						if (speechSvc != null) {
							// advanced recognition
							Log.d(TAG_SPEECH, "stop listening");
							try {
							  speechSvc.stopListening();
							} catch(Throwable t) {
								
							}
						}
						else
							// standart microphone
							onResults(null);
	*/					
						if (animator != null)
							animator.showDone();
						
						// if not started eat, we can stop fast!!!
						if ((state.state == STARTING_COMMON) 
								|| (state.state == STARTING_BLUETOOTH) 
								|| (state.state == STARTING_WAITING_SCO)) {
							Log.d(TAG_SPEECH, "abort: stop fast!");
							logState(STOPPED);
						}
					}
			  }
		);
		
		Log.d(TAG_SPEECH, "abort: ok");
	}
	
	///// ATT LISTENER METHODS:

	@Override
	public void onStateChanged(SpeechState ss) {
	  switch(ss) {
	  case IDLE:
		  Log.d(TAG_SPEECH, "VR.ATT.IDLE");
		//  onReadyForSpeech(null);
		  break;
	  case INITIALIZING:
		  Log.d(TAG_SPEECH, "VR.ATT.INITIALIZING");
    	  break;
	  case RECORDING:
		  Log.d(TAG_SPEECH, "VR.ATT.RECORDING");
		  if (state.state!=STARTED)
	         onReadyForSpeech(null);
	      break;
	  case PROCESSING:
	      Log.d(TAG_SPEECH, "VR.ATT.PROCESSING");
		  onEndOfSpeech();
	      break;
	  case ERROR:
		  Log.d(TAG_SPEECH, "VR.ATT.ERROR");
	  }
	}

	@Override
	public void onResult(ATTSpeechResult results) {
   	   Log.d(TAG_SPEECH, "ATT.onResults...");
	   logState(0);

       wakeLock.acquire();
       wakeLock.release();

		SuziePopup.resetAutoHideTimer();
		
		if (soundButtonClicked.isPlaying())
			soundButtonClicked.pause();
		if (soundThinking.isPlaying())
			soundThinking.pause();
		
		if (state.ringActive) {
	    	logState(STOPPED);
	    	Log.d(TAG_SPEECH, "onResults: RING ACTIVE");
	    	return;
		}
		
		if (state.state == STOPPED) {
	    	Log.d(TAG_SPEECH, "onResults: exit");
			return;
		}

		if ((state.state != ABORTING) && (state.state != STOPPING) && (state.state != STARTED) 
				&& (state.state != STARTING_BLUETOOTH) && (state.state != STARTING_COMMON)) {
	    	Log.d(TAG_SPEECH, "onResults: INVALID STATE !!!!!!!!!!!!!!!!");
			return;
		}

   	if (((state.state == STOPPING) || (state.state == ABORTING)) && state.restartRecognitionAfterStop) {
   		
	    	Log.d(TAG_SPEECH, "onResults: *** AUTO RESTART RECOGNITION AFTER FINISH ****");

	    	startNow();
	    	
	    	Log.d(TAG_SPEECH, "onResults: ok");
	    	
	    	return;
   	}
   	
   	boolean notify = state.state != ABORTING;
   	
   	logState(STOPPED);

		if (!notify)
			return;

		if (!soundFinishedPlayed) {
			soundFinishedPlayed = true;
   		Log.d(TAG_SPEECH, "Play mic sound start finished");
			if (results != null) 
				startSound(soundFinished);
		}
		
		Log.d(TAG_SPEECH, "notify animator "+animator);
		
		if (animator != null)
			animator.showDone();			
	    
	    if (autoRestartRecognition) {
			String result = null;
		    if (results!=null) {
		        List<String> list= results.getTextStrings();
		    	if (list != null && list.size() > 0)
		    		result = list.get(0);
		    }
		    ArrayList<String> list = new ArrayList<String>();
		    
		    if (!Utils.isEmpty(result)) {
			    list.add(result);
			    
		    	useFreeForm = true;
		    	useLanguage = autoRestartWithLanguage;
		    	startRecognizition();

		    	Bundle b = new Bundle();
		    	b.putStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION, list);
		    	onPartialResults(b);
		    	
	    		if (Utils.isEmpty(fullText))
	    			fullText = result;
	    		else
	    			fullText += " " + result;
		    }
		    else {
			    list.add(fullText);

			    useFreeForm = false;
		    	useLanguage = defLanguage;
		    	fullText = null;

				final Intent it = new Intent();
	    		it.putExtra(RecognizerIntent.EXTRA_RESULTS, list);
	    		it.putExtra(EXTRA_RESULT_NO_REPLACEMENT, list);

	    		Log.d(TAG_SPEECH, "Notify on results "+activity+"...");
				
				try {
					final Method mOnActivityResult = this.activity.getClass().getDeclaredMethod(
							  "onActivityResult", 
						       int.class, int.class, Intent.class
						   ) ;
					mOnActivityResult.setAccessible(true);
					
					mOnActivityResult.invoke(activity, VOICE_RECOGNITION_REQUEST_CODE, Activity.RESULT_OK, it);
					
					Log.d(TAG_SPEECH, "Notify on results ok");
					
				} catch (Exception e) {
				}
		    }
	    }
	    else {
	    
		   final Intent it = new Intent();
           int res;

		    if (results==null)
               res = Activity.RESULT_CANCELED;
           else {
		       List<String> old_list = results.getTextStrings();
               if (old_list == null || old_list.size() == 0)
                   res = Activity.RESULT_CANCELED;
               else {
                   List<String> new_list = old_list;
                   if (realReplaceTeaching)
                       new_list = teachReplaceTeaching(old_list);

                   it.putExtra(EXTRA_RESULT_NO_REPLACEMENT, Utils.toArrayList(old_list));

                   if (new_list == null) {
                       res = RESULT_RUN_INTENT;
                       new_list = old_list;
                   }
                   else {
                       res = Activity.RESULT_OK;
                   }
                   Log.d(TAG_SPEECH, "results: "+new_list.get(0));
                   it.putExtra(RecognizerIntent.EXTRA_RESULTS, Utils.toArrayList(new_list));
               }
		    }
		    
			Log.d(TAG_SPEECH, "Notify on results "+activity+"...");
	
			try {
				final Method mOnActivityResult = this.activity.getClass().getDeclaredMethod(
						  "onActivityResult", 
					       int.class, int.class, Intent.class
					   ) ;
				mOnActivityResult.setAccessible(true);
				
				mOnActivityResult.invoke(activity, VOICE_RECOGNITION_REQUEST_CODE, res, it);
				
				Log.d(TAG_SPEECH, "Notify on results ok");
				
			} catch (Exception e) {
			}
	    }
		
	}

	@Override
	public void onAudioLevel(int arg0) {
		float db=(float) (arg0==0?0.:(6.*Math.log10(arg0)));
		Log.d(TAG_SPEECH, "ATT.onAudioLevel="+arg0+" "+db);
		// 0-21 db expected
		if (state.state==STARTED) onRmsChanged(db);
	}

	@Override
	public void onError(ATTSpeechError arg0) {
		ErrorType error = arg0.getType();
		Log.d(TAG_SPEECH, "ATT.ERROR "+arg0.getMessage());
/*****
   USER_CANCELED;
   PARAMETER_ERROR;
   CAPTURE_FAILED;
   BELOW_MINIMUM_LENGTH;
   INAUDIBLE;
   CONNECTION_ERROR;
   RESPONSE_ERROR;
   SERVER_ERROR;
   OTHER_ERROR;		
 */
		
		switch (error) {
		// no audio device
		case INAUDIBLE: case CAPTURE_FAILED:
		//case SpeechRecognizer.ERROR_AUDIO:
			Log.d(TAG_SPEECH, "ATT.ERROR AUDIO");
			/*
			logState(0);
			
        	if (!state.deviceUsedSco)
        		return;
        	
			// try resolve audio problem
			// on some devices not work SCO mode
			// in this case switch to A2DP 
        	Log.d(TAG_SPEECH, "starting in a2dp mode >>>>>>>>>>>>>>>>>> ");
        	
        	stopSco();
        	
        	// set flag, that device not work with SCO !!!
        	state.deviceUsedSco = false;        	
			
			logState(STARTING_BLUETOOTH);

			startRecognizition();
			
        	Log.d(TAG_SPEECH, "starting in a2dp mode finished <<<<<<<<<<<<<<<<<< ");
        	*/
			return; // !!!! dont call onResults !!!!
			
		// client calls stop recognition
		case USER_CANCELED:
		//case SpeechRecognizer.ERROR_CLIENT:

			Log.d(TAG_SPEECH, "ATT.ERROR CLIENT (CANCEL RECOGNITION BY CLIENT)");
			break;
			
		// no programm permissions
			/*
		case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:

			Log.d(TAG_SPEECH, "ERROR INSUFFICIENT PROGRAMM PERMISSIONS");
			ToastController.showSimpleToast("ERROR INSUFFICIENT PROGRAMM PERMISSIONS", 0);
			break;
			*/
		// no internet connection 
		case CONNECTION_ERROR:
		//case SpeechRecognizer.ERROR_NETWORK:
		//case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:

            Log.d(TAG_SPEECH, "ATT.!!!!!!!!!!! ****************** ERROR NETWORK ************** !!!!!!!!!!!!! ");

            // initiated by user
			// sometimes, when user cancel recognition may be error
			if (state.state == STOPPING)
				break;
			
			if (animator != null && state.state == STARTED && false)
				Utils.runInMainUiThread(new Runnable() {
					@Override
					public void run() {
						MyTTS.speakText(R.string.mainactivity_onpostexecute_network_not_connected);
						if (animator != null)
							animator.showError();
					}
				});

			break;
			
		// VR busy
			/*
		case SpeechRecognizer.ERROR_SERVER:
		case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
		*/
		case SERVER_ERROR: 
            Log.d(TAG_SPEECH, "ATT.!!!!!!!!!!! ****************** VOICE RECOGNIZER BUSY ************** !!!!!!!!!!!!! ");

            // initiated by user
			// sometimes, when user cancel recognition may be error
			if (state.state == STOPPING)
				break;
			/*
			if (recognitionListener!=null)
				recognitionListener.enabled = false;

			// reinitialize
			recognitionListener=new TheRecognitionListener();
			if (sr != null)
				sr.destroy();
			sr = SpeechRecognizer.createSpeechRecognizer(App.self, findSpeechRecognizer());
			if (sr != null) {
				sr.setRecognitionListener(recognitionListener);
				Log.d(TAG_SPEECH, "open: using advanced recognition");
			}
			else
				Log.d(TAG_SPEECH, "open: FAILED CREATE SPEECH RECOGNIZER");
*/
			startRecognizition();

			if (animator != null)
				Utils.runInMainUiThread(
						  new Runnable() {
								@Override
								public void run() {
									if (animator != null)
										animator.showDone();
								}
						  }
					);
			
			return; // !!!! dont call onResults !!!!
			/*
		// text not recognized
		case SpeechRecognizer.ERROR_NO_MATCH:

			Log.d(TAG_SPEECH, "ERROR NO MATCH (NO TEXT ON INPUT DETECTED)");
			// there normal situation
			break;
			
		// long speech ?
		case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:

			Log.d(TAG_SPEECH, "ERROR SPEECH TIMEOUT");
			// there normal situation
			break;
			*/
		default:
	    	Log.d(TAG_SPEECH, "ATT.ERROR CODE "+error);
			ToastController.showSimpleToast("ERROR CODE "+error, 0);
			break;
		}
		
		onResults(null);		
	}
    
}
