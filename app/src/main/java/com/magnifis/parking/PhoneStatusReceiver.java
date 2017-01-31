package com.magnifis.parking;

import static com.robinlabs.utils.BaseUtils.isEmpty;

import java.util.Timer;
import java.util.TimerTask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;

import com.magnifis.parking.phonebook.CalleeAssocEngine;
import com.magnifis.parking.phonebook.PhoneBook;
import com.magnifis.parking.suzie.SuziePopup;
import com.magnifis.parking.suzie.SuzieService;
import com.magnifis.parking.tts.MyTTS;
import com.magnifis.parking.utils.Utils;


public class PhoneStatusReceiver  extends BroadcastReceiver  {
    final static String TAG=PhoneStatusReceiver.class.getSimpleName();
    
    private static int previousCallState=TelephonyManager.CALL_STATE_IDLE;
    
    private static String activeNumber=null; 
    private static Long outgoingCallBegin=null;
    private static CalleeAssocEngine.Association assocToCancel=null;
    
    public  static void setAssociationToClear(CalleeAssocEngine.Association assoc) {
    	assocToCancel=assoc;
    }

    public static void acceptCall()
    {
        Intent buttonDown = new Intent(Intent.ACTION_MEDIA_BUTTON);
        KeyEvent ke = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK);
        buttonDown.putExtra(Intent.EXTRA_KEY_EVENT, ke);
        App.self.sendOrderedBroadcast(buttonDown, "android.permission.CALL_PRIVILEGED");

        Intent buttonUp = new Intent(Intent.ACTION_MEDIA_BUTTON);
        buttonUp.putExtra(Intent.EXTRA_KEY_EVENT,
                new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK));
        App.self.sendOrderedBroadcast(buttonUp, "android.permission.CALL_PRIVILEGED");
    }

	@Override
	public void onReceive(Context context, Intent intent) {
		String action=intent.getAction();
		Log.d(TAG, "onReceive: "+action);
		
		if (Intent.ACTION_NEW_OUTGOING_CALL.equals(action)) {
			Log.d(TAG,Intent.ACTION_NEW_OUTGOING_CALL);
			String number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
			if (number != null) {
				Log.d(TAG, "outgoingCall: "+number);
				activeNumber=number;
				outgoingCallBegin=System.currentTimeMillis();
			}
		} 
		else if ("android.app.action.ENTER_CAR_MODE".equals(action)) {
			App.self.switchCarMode(true,false);
		} 
		else if ("android.app.action.EXIT_CAR_MODE".equals(action)) {
			App.self.switchCarMode(false,false);	
		} 
		else {
            if ("android.intent.action.PHONE_STATE".equals(action)) {
                final String incomingNumber = intent.getStringExtra("incoming_number");
                final TelephonyManager tm = App.self.getTelephonyManager();
                int cs = tm.getCallState();
                String number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
                Log.d(TAG, "cs=" + cs + "<=" + previousCallState + " (" + (number == null ? "" : number) + ") ");
                Utils.dump(TAG, intent);

                switch (cs) {
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (activeNumber == null)
                            activeNumber = incomingNumber;
                        else
                            return;

                        try {
                            VR vr = VR.get();
                            if (vr != null) vr.abort();
                            SuziePopup sp = SuziePopup.get();
                            if (sp != null)
                                sp.bubleMessage(null);
                            SendSmsActivity sa = SendSmsActivity.get();
                            if (sa != null)
                                sa.showMessage(null);
                            MainActivity ma = MainActivity.get();
                            if (ma != null)
                                ma.showMessage(null);

                        } catch (Throwable t) {
                            t.printStackTrace();
                        }

                        if (App.self.getBooleanPref("speakCallerName2")) {
                            if ((Utils.isEmpty(incomingNumber) ||
                                    (Config.dont_notify_on_calls_in_silent_or_vibro_mode && App.self.isInSilentMode()) ||
                                    !Robin.isRobinRunning) && !App.self.isInCarMode()) {
                                Log.d(TAG, "empty path");
                                MyTTS.suspend();
                            } else {
                                new Thread(TAG) {

                                    boolean isRinging() {
                                        return tm.getCallState() == TelephonyManager.CALL_STATE_RINGING;
                                    }

                                    boolean isInConversation() {
                                        return tm.getCallState() == TelephonyManager.CALL_STATE_OFFHOOK;
                                    }

                                    @Override
                                    public void run() {
                                        Log.d(TAG, "start thread");
                                        String n = null;
                                        if (!Utils.isEmpty(incomingNumber))
                                            n = PhoneBook.getBestContactName(incomingNumber);

                                        boolean roc = isRinging() || isInConversation();

                                        if (isEmpty(n))
                                            if (App.self.shouldSpellCallerPhones() && !isEmpty(incomingNumber))
                                                n = Utils.phoneNumberToSpeech(incomingNumber) + " ";
                                            else
                                                n = App.self.getString(R.string.P_unfamiliar);

                                        String s = App.self.getString(R.string.P_callfrom) + " " + n;
                                    /*
                                    if (App.self.isInCarMode())
                                        s += " " + App.self.getString(R.string.P_callautoaccept);
                                        */

                                        //boolean callWhileConversation=previousCallState!=TelephonyManager.CALL_STATE_IDLE;

                                        if (roc) {
                                            Log.d(TAG, "my tts");
                                            // clear TTS queue
                                            MyTTS.abort();

                                            // speak the name of the caller (or unknown number) and do not show it
                                            MyTTS.speakTextInConversation(new MyTTS.WithoutBubbles(s));

                                        /*
                                        if (App.self.isInCarMode())
                                            MyTTS.execAfterTheSpeech(new Runnable() {
                                                @Override
                                                public void run() {
                                                    acceptCall();
                                                }
                                            });
                                            */

                                            // if still ringing tell the name for a second time
                                            if (isRinging() && !App.self.isInCarMode())
                                                MyTTS.speakTextInConversation(new MyTTS.WithoutBubbles(s));
                                        }
                                    }

                                }.start();
                            }
                        }
                        break;

                    case TelephonyManager.CALL_STATE_IDLE:
                        if (!(outgoingCallBegin == null || assocToCancel == null || activeNumber == null)) {
                            Log.d(TAG, "outgoingCallBegin != null");
                            if ((System.currentTimeMillis() - outgoingCallBegin) < 10000l) {
                                SuzieService.cancelCalleeAssociation(activeNumber, assocToCancel);
                            }
                        }
                        outgoingCallBegin = null;
                        assocToCancel = null;
                        activeNumber = null;
                        MyTTS.resume();
                        break;

                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        MyTTS.abort();//stopIt();

                        turnOnSpeaker(10);
                        break;

                    case TelephonyManager.DATA_SUSPENDED:
                        if (SuziePopup.get() != null)
                            if (SuziePopup.get().mag != null)
                                SuziePopup.get().mag.hideError();
                        if (MainActivity.get() != null)
                            if (MainActivity.get().micAnimator != null)
                                MainActivity.get().micAnimator.hideError();
                        break;

                    default:
                        Log.d(TAG, "cs=" + cs);
                }

                previousCallState = cs;
            }
        }
	}

    void turnOnSpeaker(final int attemptCount) {
        if (!App.self.getBooleanPref("car_mode_option_speaker"))
            return;

        if (!App.self.isInCarMode())
            return;

        if ((VR.get() != null && VR.get().isBlueToothConnected()))
            return;

        AudioManager am = (AudioManager) App.self.getSystemService(Context.AUDIO_SERVICE);
        am.setSpeakerphoneOn(true);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);

        if (attemptCount == 0)
            return;

        Log.d(TAG, "SPEAKER ON ATTEMPT "+attemptCount);
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                turnOnSpeaker(attemptCount-1);
            }
        }, 100);
    }
}
