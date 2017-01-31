package com.magnifis.parking;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;

import com.magnifis.parking.cmd.etc.CmdHandlerHolder;
import com.magnifis.parking.cmd.i.OnBeforeListeningHandler;
import com.magnifis.parking.db.RobinDB;
import com.magnifis.parking.model.LearnedAnswer;
import com.magnifis.parking.pref.PrefConsts;
import com.magnifis.parking.suzie.SuzieHints;
import com.magnifis.parking.suzie.SuziePopup;
import com.magnifis.parking.toast.ToastController;
import com.magnifis.parking.tts.MyTTS;
import com.magnifis.parking.utils.StringConstants;
import com.magnifis.parking.utils.Utils;
import com.magnifis.parking.voice.AtntVR;
import com.magnifis.parking.voice.CapioVR;
import com.magnifis.parking.voice.GoogleVR;
import com.magnifis.parking.voice.VoiceRecognitionConfig;
import com.robinlabs.ivr.google.GoogleVoiceInterceptor;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.magnifis.parking.utils.Utils.isEmpty;

/*
   This class using for speech recognition
   Using:
   		1) implement in MainActivity method
	    	@Override
	    	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	
	    		if (requestCode == VR.VOICE_RECOGNITION_REQUEST_CODE) {
	    		
	    			if (resultCode == RESULT_OK || resultCode == RESULT_FIRST_USER)
	            		ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
	            	else
	            		showError("???");
	            
	        	}
	
	        	super.onActivityResult(requestCode, resultCode, data);
	    	}
	    	
	    2) implement IAnimator
	    
	    3) call methods:
	    		open() - initialize voice recognition (call in MainActivity.OnCreate())
	    		close() - deinitialize voice recognition (call in MainActivity.OnDestroy())
	    		
	    		start() - start speech recognition
	    		abort() - cancel speech recognition without notification of results
	    		stop() - finishes speech recognition with notification of results

				// this method must call if user long press bluetooth button (Voice Command)
				// fixed bug: recognition hung after long press bluetooth button if call any SCO function
				// this method disables call SCO function while recognition work  
				markCallByVoiceCommand() {

				// if standart microphone active, this method deactivates it by sending intent ADVANCE to MainActivity
				// if uses advanced recognition this method calls abort
	    		killMicrophone() - cancel recognition
	    		
	 	TESTS FOR STANDART MODE
	 	
	 		1 simple start (main button) and stop (main button during listen) and abort (back key during listen)
	 		2 fast clicking start, stop, abort, start, stop, abort, start, stop, abort, start, stop, abort....
	 		3 start from widget
	 		4 incoming call 
	 		5 outgoing call  
	 		6 check radio in background
	 		7 mp3 player in background
	 			
	 		
	 	TESTS FOR BLUETOOTH
	 	
	 		bluetooth1: with SCO and a2DP modes (VLADIMIR's JAWBONE)
	 		bluetooth2: with SCO mode only (SERGEY's NOKIA)
	 		bluetooth3: with A2DP mode only (ZEEV's headphones)
	 		bluetooth4: in car (SERGEY's hyundai i35 with build in video and navigation)
	 		
	 	 	10 connect bluetooth while robin waiting
	 		11 disconnect bluetooth while robin waiting
	 		12 start robin with long press bluetooth key
	 		13 when robin ready press bluetooth button - robin must listen 
	 		14 when robin listen press bluetooth button - robin must stop
	 		15 press bluetooth after call - robin must activate and listen
	 		16 start robin when bluetooth already connected
	 		17 repeat base tests for standard mode 

	 
	 KNOWN BUGS:
	 		1 after incoming call, long press on bluetooth button -> recognizer hung. i need restart robin.
	 		2 if device in sleep mode bluetooth button not work. after sleep mode end robin start listen.
	 		3 in my car tts speech hear in phone not in car 
	 		
 */
@SuppressLint("NewApi")
public abstract class VR extends BroadcastReceiver implements  MediaPlayer.OnCompletionListener, OnSharedPreferenceChangeListener {

	public final static String TAG_SPEECH="Speech";
	public static final String ADVANCE="com.magnifis.parking.ADVANCE";
	
	// stream to use with text to speech service
	// can't using streams:
	// MUSIC - music channel - mutes by VR module
	// DTMF  - radio channel - mutes by VR module

	// STREAM             NORMAL MODE            BLUETOOTH MODE
	// ---------------------------------------------------
	// SYSTEM             OK                     volume to low                         not work on planshet zeev's
	// VOICE_CALL         volume to low          OK
	// ALARM              OK                     duplicate sound (speaker and phones) and sometimes volume to low (when speaking after voice recognition)
	// NOTIFICATION       OK                     duplicate sound (speaker and phones) and sometimes volume to low (when speaking after voice recognition)
	// RING               OK                     duplicate sound (speaker and phones) and sometimes volume to low (when speaking after voice recognition)

	// found new problems with channels NOTIFICATION, RING - headphones not work, in vibrate mode no sound 
	// new problem with channel SYSTEM - volume to low in headphones

	// RESUME: USING ONLY MUSIC CHANNEL IN NORMAL MODE AND VOICE CALL IN BLUETOOTH MODE 
	
	// on the basis of the above we using streams:
	public static int TtsAudioStreamForNormalMode = getDefStream();

    private static int getDefStream() {
        // on Natasha's device lolipop says that version is lower than 21 ????
    	// @Ilya's response: above comment not true, Natasha's device version is 22 as of 5/2015
        //if (Utils.isAndroid5orAbove)
            return AudioManager.STREAM_MUSIC;
        /*else
            return AudioManager.STREAM_RING;*/
    }

    public static int TtsAudioStreamForBluetooth = AudioManager.STREAM_VOICE_CALL;

	public static int TtsAudioStream = TtsAudioStreamForNormalMode;

    public static int RESULT_RUN_INTENT = 100;
	
	TheRecognitionListener recognitionListener=null;
	
	protected Silence silence = new Silence();

    // if packageName allowed standard microphone
    public static VR create(Object activity, IAnimator animator, boolean standart_microphone, boolean voice_command) {

        Log.d(TAG_SPEECH, "VR.CREATE " + activity + ", " + animator);

        VR result = get();

        if (result == null) {
            Log.d(TAG_SPEECH, "CREATED NEW VR");

            int vrEngine = VoiceRecognitionConfig.getEngine();
            switch (vrEngine) {
                case 0:
                    result = new GoogleVR(activity, animator);
                    break;
                case 1:
                    result = new CapioVR(activity, animator);
                    break;
                case 2:
                    result = new AtntVR(activity, animator);
                    break;
            }

        } else {
            Log.d(TAG_SPEECH, "USING EXISTING VR");
            result.activity = activity;

            if (result.animator != null) {
                final IAnimator oldAnimator = result.animator;
                Utils.runInMainUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                if (oldAnimator != null)
                                    oldAnimator.showDone();
                            }
                        }
                );
            }

            result.animator = animator;
            if (result.isOpened())
                if (result.standart_microphone != standart_microphone)
                    result.close();
                else if (result.isStarted())
                    result.abort();
        }


        if (voice_command)
            result.markCallByVoiceCommand();

        if (!result.isOpened())
            result.open(standart_microphone);

        return result;
    }

    protected class Silence {
		final static long MAX_SILENCE_LENGTH=3000l; // ms
		final static float SILENCE_LEVEL=5;
		//final static int NONE_SCILENCE_COUNTER1_TRESHOLD=4,NONE_SCILENCE_COUNTER2_TRESHOLD=20;
		volatile protected Long silenceStart = null;
		volatile private int noneSilenceCount1=0, noneSilenceCount2=0;
	}

	private static WeakReference<VR> selfWr=null;
	
	protected MediaPlayer soundThinking = null;
	protected MediaPlayer soundButtonClicked = null; // when clicked bluetooth button
	protected MediaPlayer soundStartBlueTooth = null;
	protected MediaPlayer soundStartCommon = null;
	protected MediaPlayer soundFinished = null;
	volatile protected boolean soundFinishedPlayed;
	private Timer soundTimer = null; // when mic sound is off, using timer to call finalization function 
	
	private Timer unmuteTimer = new Timer();
	private Timer soundFinishedTimer = new Timer();
    protected PowerManager.WakeLock wakeLock = null;
	
	public static VR get() {
		return selfWr==null?null:selfWr.get();
	}
	
	// for simple recognition
    public static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
    
    public IAnimator animator = null;
    public Object activity = null;
	SpeechRecognizer sr = null;
	
	protected State state = new State();
	
	public static final int CLOSED = 1;
	public static final int STOPPED = 2; // equal to OPENED
	public static final int STOPPED_WAITING_SCO = 3;
	public static final int STOPPED_CANCEL_SCO = 4;
	public static final int STARTING_WAITING_SCO = 5;
	public static final int STARTING_COMMON = 6;
	public static final int STARTING_BLUETOOTH = 7;
	public static final int STARTED = 8;
	public static final int STOPPING = 9;
	public static final int ABORTING = 10;
	
	protected static final int SCO_UNKNOWN = 0;
	protected static final int SCO_CONNECTED = 1;
	protected static final int SCO_CONNECTING = 2;
	protected static final int SCO_DISCONNECTED = 3;

	protected static final int BT_UNKNOWN = 0;
	protected static final int BT_CONNECTED = 1;
	protected static final int BT_DISCONNECTED = 2;
	public static final String EXTRA_RESULT_NO_REPLACEMENT = "EXTRA_RESULT_NO_REPLACEMENT";
	
	// flag: listen in free form next time
	// after listening form automatically return to normal mode
	public static boolean useFreeForm = false;
	protected static boolean realFreeForm = false;
	// flag for internal use: start again after recognition? 
	protected static boolean autoRestartRecognition = false;
	// flag for internal use: do replace after recognition? 
	protected static String autoRestartWithLanguage = null;
	// full text from all sentences
	protected static String fullText = null; 

	// flag: listen in this language next time
	// after listening form automatically return to normal mode
	// sample: russian, english
	public static String useLanguage = null;
	
	// flag: after listen auto replace teaching next time
	// after listening form automatically return to normal mode
	public static boolean useReplaceTeaching = true;
	// flag for internal use: do replace after recognition? 
	protected static boolean realReplaceTeaching = true;
	
	// default language for VR on android
	protected static String defLanguage = "??";

	protected class State { 
		// IS SCO MODE TO BLUETOOTH DEVICE CONNECTED ? 
		public volatile int scoConnectState = SCO_UNKNOWN;
		public volatile Long scoConnectStart = null;
		public final static long MAX_SCO_CONNECT_TIME = 3000;
		// IS BLUETOOTH DEVICE CONNECTED ?
		public volatile int btConnectState = BT_UNKNOWN;
		// some devices not works with sco. 
		// if device get us AUDIO ERROR, we using a2DP mode
		// but default we try start in sco mode
		public volatile boolean deviceUsedSco = true; 
		public volatile int state = CLOSED;
		public volatile boolean restartRecognitionAfterStop = false;
		public volatile boolean ringActive = false;
	}
	
	public static interface IAnimator {
		
		// black screen and orange mic
		// initializing...
		void showBegin();

		// orange mic
		// ready to listen
		// hides progress!
		void showReadyToBegin();
		
		// orange mic with level
		// speech detected and show DB level
		void showListening(float db);
		
		// black screen and orange mic
		// recognizing...
		void showThinking();
		
		// blue mic
		// waiting user
		// hides progress!
		void showDone();

		void showError();

		void hideError();
		
	}
	
	public static interface IVoiceResults2 {
		  void onRmsChanged(float rmsdB);
		  void onReadyForSpeech();
		  void onEndOfSpeech();
		  void onBluetoothConnected();
		  void onBluetoothDisconnected();
		  void onVoiceComplete(String s);
	}

	synchronized public boolean isOpened() {
		return state.state != CLOSED;
	}
	
	// returns true if device bluetooth connected with microphone
	synchronized public boolean isBlueToothConnected() {
		return state.btConnectState == BT_CONNECTED;
	}
	
	// this method must call if user long press bluetooth button (Voice Command)
	// fixed bug: recognition hung after long press bluetooth button if call any SCO function
	// this method disables call SCO function while recognition work  
	public synchronized void markCallByVoiceCommand() {
    	Log.d(TAG_SPEECH, "MARK SCO RESTART (fix bug)");
    	
		AudioManager am = (AudioManager) App.self.getSystemService(Context.AUDIO_SERVICE);
		//mute();
		//am.setMode(AudioManager.MODE_IN_COMMUNICATION);
    	//Log.d(TAG_SPEECH, "IN COMMUNICATION");

		soundButtonClicked.start();
	}
	
	// an alias to make a client code more readable 
	public static boolean isListening() {
		VR vr = get();
		if (vr == null)
			return false;
		else
			return vr.isStarted();
	}
	
	// warning: this method says: recognizer working? (starting and stopping - is working states) 
	synchronized public boolean isStarted() {
		return (state.state == STARTING_BLUETOOTH) || (state.state == STARTING_COMMON)
				|| (state.state == STARTED) || (state.state == STOPPING);
	}


	
	public Object getReceiverObject() {
		return activity;
	}

	public VR(Object activity, IAnimator animator) {
		selfWr = new WeakReference<VR>(this);
		this.animator = animator;
		this.activity = activity;

        SphinxRecornizer.start();

		if (Utils.isAndroid22orAbove)
			audioFocusChangeListener = new AudioFocusChangeListener();
		
		soundTimer = new Timer();
		
		soundStartBlueTooth = new MediaPlayer(); 	
		try {
    		soundStartBlueTooth.setDataSource(App.self, Uri.parse("android.resource://"+App.self.getPackageName()+"/"+ R.raw.mic_wakeup_sound));
    		soundStartBlueTooth.prepare();
    		soundStartBlueTooth.setOnCompletionListener(this);
		} catch (Exception e) {
		}

		soundButtonClicked = new MediaPlayer(); 	
		try {
			soundButtonClicked.setDataSource(App.self, Uri.parse("android.resource://"+App.self.getPackageName()+"/"+ R.raw.none));
			soundButtonClicked.setAudioStreamType(TtsAudioStreamForBluetooth);
			soundButtonClicked.prepare();
			soundButtonClicked.setLooping(true);			
		} catch (Exception e) {
		}

		soundStartCommon = new MediaPlayer(); 	
		try {
			soundStartCommon.setDataSource(App.self, Uri.parse("android.resource://"+App.self.getPackageName()+"/"+ R.raw.mic_wakeup_sound));
			soundStartCommon.prepare();
			soundStartCommon.setOnCompletionListener(this);
		} catch (Exception e) {
		}
		
		soundFinished = new MediaPlayer(); 	
		try {
			soundFinished.setDataSource(App.self, Uri.parse("android.resource://"+App.self.getPackageName()+"/"+ R.raw.mic_wakeup_sound));
			soundFinished.prepare();
		} catch (Exception e) {
		}

        soundThinking = new MediaPlayer();
        try {
            soundThinking.setDataSource(App.self, Uri.parse("android.resource://"+App.self.getPackageName()+"/"+ R.raw.wait_sound));
            //soundThinking.setAudioStreamType(TtsAudioStream);//AudioManager.STREAM_VOICE_CALL);
            soundThinking.prepare();
            soundThinking.setLooping(true);
        } catch (Exception e) {
        }

		App.self.registerReceiver(this, new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED)); // for new systems (from version 14)
		App.self.registerReceiver(this, new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
		App.self.registerReceiver(this, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));
        App.self.registerReceiver(this, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
		App.self.getPrefs().registerOnSharedPreferenceChangeListener(this);
		
		// advise to event: call, and end call
		App.self.getTelephonyManager().listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
		
		try {
			rdb = RobinDB.getInstance(App.self);
			if (rdb != null) {
				qaList = rdb.getAll(LearnedAnswer.class);
				if (qaList == null)
					qaList = new ArrayList<LearnedAnswer>();
			}
		} catch(Throwable t) {
			Log.d(TAG_SPEECH, "rdb", t);
		}

        PowerManager pm = (PowerManager) App.self.getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "vr");
	}
	
	protected boolean useBt=Config.bt && Utils.isAndroid4orAbove && App.self.getBooleanPref(R.string.PfBluetoothSupport);
	public boolean standart_microphone;

    // open voice recognition
	public synchronized void open(boolean standart_microphone) {

		// for testing with standart microphone
		//standart_microphone = true;
		
		Log.d(TAG_SPEECH, "open...");
		logState(0);

		if (state.state != CLOSED) {
			Log.d(TAG_SPEECH, "open: INVALID STATE !!!!!!!!!!!!");
			return;
		}
				
		this.standart_microphone = standart_microphone;
		if (!this.standart_microphone) {
			if (Config.oldmic||!Utils.isClassAvailable("android.speech.RecognitionListener"))
				this.standart_microphone=true;
		}
		
		logState(STOPPED);
		
		if (!this.standart_microphone) {
			if (recognitionListener==null) recognitionListener=new TheRecognitionListener(); 
			sr = SpeechRecognizer.createSpeechRecognizer(App.self, findSpeechRecognizer());
			if (sr != null) {
				sr.setRecognitionListener(recognitionListener);
				Log.d(TAG_SPEECH, "open: using advanced recognition");
			}
			else
				Log.d(TAG_SPEECH, "open: FAILED CREATE SPEECH RECOGNIZER");
		}
		else
			Log.d(TAG_SPEECH, "open: using standart recognition");
		
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
		

		// detect VR language
		Intent intent = new Intent(RecognizerIntent.ACTION_GET_LANGUAGE_DETAILS);
		App.self.sendOrderedBroadcast(intent, null, this, null, Activity.RESULT_OK, null, null);
		
		detectIfBluetoothConnected();
		
		Log.d(TAG_SPEECH, "open: ok");
   }

    protected boolean hasBluetoothConnected() {
        if (!Utils.isAndroid4orAbove)
            return false;

        try {
            int state = BluetoothAdapter.getDefaultAdapter().getProfileConnectionState(android.bluetooth.BluetoothProfile.HEADSET);
            return state == BluetoothProfile.STATE_CONNECTED;
        } catch (Exception e) {
        }

        return false;
    }
    // close voice recognition
	protected synchronized void detectIfBluetoothConnected() {
		
		////////////////////////////////////////////////////////////////////////////////////
		// check if any bluetooth device connected
		/*
		if (Utils.isAndroid4orAbove) {
            BluetoothAdapter.getDefaultAdapter().getProfileProxy(App.self, new BluetoothProfile.ServiceListener() {
                @Override
                public void onServiceConnected(int profile, BluetoothProfile proxy) {
                    if (profile != BluetoothProfile.HEADSET)
                        return;

                    List<BluetoothDevice> devices = proxy.getConnectedDevices();
                    if (devices != null && devices.size() > 0)
                        for(int i=0; i < devices.size(); i++)
                            if (devices.get(i).getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.AUDIO_VIDEO) {
                                switchToBluetoothMode();
                                return;
                            }
                    switchToNormalMode();
                }

                @Override
                public void onServiceDisconnected(int profile) {

                }
            }, BluetoothProfile.HEADSET);
        }

                */
        if (hasBluetoothConnected())
        	switchToBluetoothMode();
        else
        	switchToNormalMode();
        
	}

    // close voice recognition
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
		
    	if (sr != null) {
    		sr.destroy();
    		sr = null;
    	}
		
		Log.d(TAG_SPEECH, "close: ok");
	}
	
	synchronized public void start(boolean askToInstallRecognizer) {
		
		Log.d(TAG_SPEECH, "start...");
		logState(0);
		
		SuziePopup.resetAutoHideTimer();
		
		if (state.ringActive || App.self.isInPhoneConversation()) {
			Log.d(TAG_SPEECH, "start: ring active!!");
			return;
		}
		
       OnBeforeListeningHandler oblh = CmdHandlerHolder.getOnBeforeListeningHandler();
       if (oblh!=null) oblh.onBeforeListening();
			
		if (findSpeachRecognizer() == null) {
			Log.d(TAG_SPEECH, "start: speech recognizer not found");
			if (!askToInstallRecognizer || !(activity instanceof Activity)) {
				Log.d(TAG_SPEECH, "start: fail");
				return;
			}
			Utils.runInMainUiThread(
                new Runnable() {

      			@Override
      			public void run() {
      	        	  final AlertDialog ad = Utils.showAlert((Context)activity, null, R.string.P_YOU_NEED_GVS);
      	        	  ad.getButton(AlertDialog.BUTTON_NEUTRAL)
      	        	  		.setOnClickListener(
      	        	    		new View.OnClickListener() {
      	    						@Override
      	    						public void onClick(View arg0) {
      	    							ad.cancel();
      	    							Launchers.lookAtMarketFor((Context)activity,"com.google.android.voicesearch");
      	    						}
      	        	    		}
      	        	     );
      	        	  
      	        	  MyTTS.sayFromGUI((Activity)activity, R.string.P_YOU_NEED_GVS);
      			}
              	  
                } 
              );
			Log.d(TAG_SPEECH, "start: fail");
			return;
		}
		
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
			Utils.runInMainUiThread(new Runnable() {
				
				@Override
				public void run() {
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

					startRecognizition();

					if (animator != null)
						animator.showDone();
					
				}
			});
			
		}
		else {
			
			int oldMute = mute.muteState;
			
			mute();
			
			if (useBt && isBlueToothConnected() && state.deviceUsedSco 
					&& state.state == STARTING_WAITING_SCO)
				logState(STARTING_WAITING_SCO);
			else 
				startNow();
		}
    	
		Log.d(TAG_SPEECH, "start: ok");
	}

	// finishes speech recognizing and send notify with results
	synchronized public void stop() {
		
		// TODO: restore!
		Log.w(TAG_SPEECH, "stop...");
		// Log.d(TAG_SPEECH, "stop...");
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
		
		Utils.runInMainUiThread(
			  new Runnable() {
					@Override
					public void run() {

						if (sr != null) {
							// advanced recognition
							Log.d(TAG_SPEECH, "stop listening");
							sr.stopListening();
						}
						else
							// standart microphone
							onResults(null);
						
						if (animator != null)
							animator.showDone();
					}
			  }
		);
		
		Log.d(TAG_SPEECH, "stop: ok");
	}

	// cancel speech recognizing without notify of results!
	synchronized public void abort() {

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
		
		Utils.runInMainUiThread(
			  new Runnable() {
					@Override
					public void run() {

						if (sr != null) {
							// advanced recognition
							Log.d(TAG_SPEECH, "stop listening");
							sr.stopListening();
						}
						else
							// standart microphone
							onResults(null);
						
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

	synchronized public void killMicrophone() {
    	if (sr != null)
    		abort();
     	else if (activity instanceof Activity) {
     		Intent it=new Intent(ADVANCE);
     		it.setClass(App.self, activity.getClass());
     		((Activity)activity).startActivity(it); 	
     	}
	}
	
	/**************************************************************************************************************/
	/************************ PROTECTED METHODS ********************************************************************/
	/**************************************************************************************************************/
	// activate bluetooth mode: speak to phones !
    private void switchToBluetoothMode() {
    	
    	if (state.btConnectState == BT_CONNECTED)
    		return;
    	
		Log.d(TAG_SPEECH, "Switch to bluetooth mode");
		
    	state.btConnectState = BT_CONNECTED;

    	AudioManager am = (AudioManager) App.self.getSystemService(Context.AUDIO_SERVICE);

		// switch to stream alarm - work good in normal mode!!!
		TtsAudioStream = TtsAudioStreamForBluetooth;
		
 	    // unmute voice stream and set maximum volume
 	    // stream voice_call uses by tts !
 	    am.setStreamMute(TtsAudioStream, false);
 	    //am.setStreamVolume(TtsAudioStream,	am.getStreamMaxVolume(TtsAudioStream), 0);

        if (Mute.muteState != Mute.UNMUTED)
		    am.setMode(AudioManager.MODE_IN_COMMUNICATION);
    	Log.d(TAG_SPEECH, "IN COMMUNICATION");
 	    
 	    // set hardware volume changed our stream!
 	    if (activity instanceof Activity)
 	    	((Activity)activity).setVolumeControlStream(TtsAudioStream);
 	    
 	    logState(0);
 	    
 	    if (mute.muteState != Mute.UNMUTED)
 	    	startSco();
	}

    // switch to normal mode (without bluetooth device). speak to speakerphone!
	private void switchToNormalMode() {

    	if (state.btConnectState == BT_DISCONNECTED)
    		return;
    	
    	Log.d(TAG_SPEECH, "Switch to normal mode");
    	
    	state.btConnectState = BT_DISCONNECTED;

    	AudioManager am = (AudioManager) App.self.getSystemService(Context.AUDIO_SERVICE);

		// switch to stream alarm - work good in normal mode!!!
		TtsAudioStream = TtsAudioStreamForNormalMode;
		
 	    // unmute voice stream and set maximum volume
 	    // stream voice_call uses by tts !
 	    am.setStreamMute(TtsAudioStream, false);
 	    //am.setStreamVolume(TtsAudioStream,	am.getStreamMaxVolume(TtsAudioStream), 0);
 	    
		am.setMode(AudioManager.MODE_NORMAL);
    	Log.d(TAG_SPEECH, "NORMAL");
 	    
 	    // set hardware volume changed our stream!
 	    if (activity instanceof Activity)
 	    	((Activity)activity).setVolumeControlStream(TtsAudioStream);
 	    
 	    logState(0);
	}

	protected void startSound(final MediaPlayer mp) {

		// for bluetooth play finish sound only
		if (isBlueToothConnected()) {
			
			if (mp == soundFinished)
				mp.start();
			else
	    		// start timer to call completion method
				soundFinishedTimer.schedule(new TimerTask() {
					@Override
					public void run() {
			    		onCompletion(mp);
					}
	    		}, 50);
				
			return;
		}
		
		// for versions higher or equal 16, sound included in system
		// for versions lower 16, using settings: "play start and finish sound"
    	if ((android.os.Build.VERSION.SDK_INT >= 16)
    			|| App.self.getBooleanPref("InputReadySoundOff")) {

    		// start timer to call completion method
    		/*soundTimer.schedule(new TimerTask() {
				@Override
				public void run() {*/
		    		onCompletion(mp);
				/*}
    		}, 0);*/
    	}
    	else
    		// start mic sound
    		mp.start();
    	
	}
	synchronized protected void startNow() {
		
		fullText = null;

        wakeLock.acquire();
        wakeLock.release();

		if (animator != null)
			Utils.runInMainUiThread(new Runnable() {
				@Override
				public void run() {
					if (animator != null)
						animator.hideError();
				}
			});

    	if (isBlueToothConnected()) {
			logState(STARTING_BLUETOOTH);
    		Log.d(TAG_SPEECH, "Play mic sound start bluetooth");
			startSound(soundStartBlueTooth);
    	}
    	else
    	{
			logState(STARTING_COMMON);
    		Log.d(TAG_SPEECH, "Play mic sound start common");
			startSound(soundStartCommon);
    	}
	}
	
	synchronized protected void stopSco() {
        final AudioManager am = (AudioManager) App.self.getSystemService(Context.AUDIO_SERVICE);

		if (!useBt) {
            am.setBluetoothA2dpOn(true);
            Log.d(TAG_SPEECH, "A2DP ON");
			return;
        }
		
		if (state.scoConnectState != SCO_CONNECTED) {
            am.setBluetoothA2dpOn(true);
            Log.d(TAG_SPEECH, "A2DP ON");
			return;
        }
		
		logState(STOPPED_CANCEL_SCO);
		
		am.stopBluetoothSco();
		Log.d(TAG_SPEECH, "SCO STOP");
		
		if (am.isBluetoothScoOn()) {
			am.setBluetoothScoOn(false);
			Log.d(TAG_SPEECH, "SCO OFF");
		}
    	state.scoConnectState = SCO_DISCONNECTED;

    	am.setBluetoothA2dpOn(true);
		Log.d(TAG_SPEECH, "A2DP ON");
	}

    synchronized protected boolean startSco() {


        // check: connect SCO hung?
        if ((state.scoConnectState == SCO_CONNECTING) && (System.currentTimeMillis() - state.scoConnectStart > State.MAX_SCO_CONNECT_TIME)) {
            Log.d(TAG_SPEECH, "SCO CONNECT HUNG !!!!!!!!!!!!!!!!!!!!!!!!!");
            return false;
        }

        if ((state.scoConnectState != SCO_DISCONNECTED) && (state.scoConnectState != SCO_UNKNOWN))
            return false;

        if (state.btConnectState != BT_CONNECTED)
            return false;

        if (!useBt)
            return false;

        if (!state.deviceUsedSco)
            return false;

        final AudioManager am = (AudioManager) App.self.getSystemService(Context.AUDIO_SERVICE);

        // Commented out (temp?) - reportedly breaks BT functionality  
        // if(!am.isWiredHeadsetOn())return false;

        if (am.isBluetoothScoOn()) {
            Log.d(TAG_SPEECH, "SCO OFF");
            am.setBluetoothScoOn(false);
        }
        if (!am.isBluetoothScoOn()) {
            Log.d(TAG_SPEECH, "SCO ON");
            am.setBluetoothScoOn(true);
        }

        logState(STOPPED_WAITING_SCO);

        // for new systems (version > 14)
        am.startBluetoothSco();

        //state.scoConnectState = SCO_CONNECTING;
        //state.scoConnectStart = System.currentTimeMillis();

        Log.d(TAG_SPEECH, "SCO START");

        return true;
    }

    protected static Intent findSpeachRecognizer() {
		
		PackageManager pm = App.self.getPackageManager();

		Intent it = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		it.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, Launchers.class
				.getPackage().getName());

		List<ResolveInfo> rsi = pm.queryIntentActivities(new Intent(
				RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);

		if ((rsi != null) && (rsi.size() > 0)) {
			for (ResolveInfo ri : rsi) {
				ActivityInfo ai = ri.activityInfo;
				if (ai.name!=null&&ai.name.contains(".google.")) {
					String s = ai.loadLabel(pm).toString().toLowerCase();

					if (s.contains("voice search")) {
						Log.d(TAG_SPEECH, s);
						it.setClassName(ai.packageName, ai.name);
						return it;
					}
				}
			}
			ActivityInfo ai = rsi.get(0).activityInfo;
			it.setPackage(ai.packageName);
			// it.setClassName(ai.packageName, ai.name);
			return it;
		}

		return null;
	}
	
	protected void startRecognizition() {
		
		Log.d(TAG_SPEECH, "startRecognizition...");
		logState(0);
		
		Utils.runInMainUiThread(
				  new Runnable() {
						@Override
						public void run() {

							// if some speaking now, than stop !!!
							MyTTS.abortWithoutUnlock();

							// prepare intent
							final Intent vri = findSpeachRecognizer();//new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
							vri.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, MainActivity.class.getPackage().getName());
							vri.putExtra(RecognizerIntent.EXTRA_PROMPT, App.self.getString(R.string.Robin_Prompt));
							
							/*****************************************************************************/

							realReplaceTeaching = useReplaceTeaching && !useFreeForm;
							useReplaceTeaching = true;
							
							/*****************************************************************************/

							if (useFreeForm) {
								vri.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
								vri.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
								vri.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
								//autoRestartRecognition = true; // using serial dictation
								autoRestartRecognition = false; 
								autoRestartWithLanguage = useLanguage;
							}
							else {
								vri.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);
								vri.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 10);
								vri.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
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
								vri.putExtra(RecognizerIntent.EXTRA_LANGUAGE, newLang);
                                // bug fix: https://code.google.com/p/android/issues/detail?id=75347
                                vri.putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", new String[]{});
								Log.d(TAG_SPEECH, "SET LANGUAGE "+newLang);
							}
							else
								Log.d(TAG_SPEECH, "LANGUAGE NOT SET BECOUSE VR USES "+defLanguage);
							
							useLanguage = null;
							/*****************************************************************************/
							
							//vri.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, test); 
							//vri.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, test); // 10 min
							//vri.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, test);
							
							//test *= 10; 
							//float[] confidence = {0.9f, 0.8f, 0.7f, 0.6f, 0.5f, 0.4f, 0.3f, 0.2f, 0.1f, 0.01f};
							//vri.putExtra(RecognizerIntent.EXTRA_CONFIDENCE_SCORES, confidence); 
		
							if (sr != null) {
								sr.startListening(vri);
								Log.d(TAG_SPEECH, "startRecognizition: start listening");
							} else {
								if (activity instanceof Activity)
									((Activity) activity).startActivityForResult(vri, VOICE_RECOGNITION_REQUEST_CODE);
								else {
									PendingIntent pi = PendingIntent.getActivity(App.self, VOICE_RECOGNITION_REQUEST_CODE, vri, PendingIntent.FLAG_UPDATE_CURRENT);
									try {
										pi.send();
									} catch (CanceledException e) {
									}
								}
								logState(STOPPED);
								Log.d(TAG_SPEECH, "startRecognizition: sent start intent");
							}							
						}
				  }
			);
		
		Log.d(TAG_SPEECH, "startRecognizition: ok");
	}
	
	protected static ComponentName findSpeechRecognizer() {
		PackageManager pm = App.self.getPackageManager();
		Intent it = new Intent("android.speech.RecognitionService");
		List<ResolveInfo> ss=pm.queryIntentServices(it, PackageManager.GET_SERVICES);
        if ((ss != null) && (ss.size() > 0)) {
        	for (ResolveInfo ri:ss) {
        	   ServiceInfo si=ri.serviceInfo;
        	   if (si.name.contains("google")) return new ComponentName(si.packageName,si.name);
        	}
        	ServiceInfo si=ss.get(0).serviceInfo;
        	return new ComponentName(si.packageName,si.name);
        }
		
		return null;
	}

	protected void logState(int newState) {

		if (newState != 0)
			state.state = newState;
		
		String s;
		
		if (state.state == ABORTING)
			s = "ABORTING";
		else if (state.state == CLOSED)
			s = "CLOSED";
		else if (state.state == STOPPED)
			s = "STOPPED";
		else if (state.state == STARTED)
			s = "STARTED";
		else if (state.state == STOPPED_WAITING_SCO)
			s = "STOPPED_WAITING_SCO";
		else if (state.state == STOPPED_CANCEL_SCO)
			s = "STOPPED_CANCEL_SCO";
		else if (state.state == STARTING_WAITING_SCO)
			s = "STARTING_WAITING_SCO";
		else if (state.state == STARTING_BLUETOOTH)
			s = "STARTING_BLUETOOTH";
		else if (state.state == STARTING_COMMON)
			s = "STARTING_COMMON";
		else if (state.state == STOPPING)
			s = "STOPPING";
		else
			s = "UNKNOWN ???????????????????";

		if (newState == 0) {
			if (state.scoConnectState == SCO_CONNECTED)
				s += " SCO_CONNECTED";
			else if (state.scoConnectState == SCO_CONNECTING)
				s += " SCO_CONNECTING";
			else if (state.scoConnectState == SCO_DISCONNECTED)
				s += " SCO_DISCONNECTED";
			else s += " SCO_UNKNOWN";

			if (state.btConnectState == BT_CONNECTED)
				s += " BT_CONNECTED";
			else if (state.btConnectState == BT_DISCONNECTED)
				s += " BT_DISCONNECTED";
			else s += " BT_UNKNOWN";

			Log.d(TAG_SPEECH, "now state " + s);
		}
		else
			Log.d(TAG_SPEECH, "SET STATE " + s);

        if (newState == STOPPED)
            unMute();
	}
	
	
	/**************************************************************************************************************/
	/************************ NOTIFICATION ************************************************************************/
	/**************************************************************************************************************/
		
	// notification from Audio manager
    @Override
    synchronized public void onReceive(Context context, Intent it) {
    	
    	if (it.getAction() == RecognizerIntent.ACTION_GET_LANGUAGE_DETAILS) {
    		Bundle results = getResultExtras(true);
    		String s = results.getString(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE);
    		if (!Utils.isEmpty(s))
    			defLanguage = s;
			return;
    	}

        if ((it.getAction() == Intent.ACTION_HEADSET_PLUG))
        {
            int headSetState = it.getIntExtra("state", 0);      //get the headset state property
            //int hasMicrophone = it.getIntExtra("microphone", 0);//get the headset microphone property
            if (headSetState == 0)
                switchToNormalMode();
            else
                switchToBluetoothMode();
            return;
        }

        if ((it.getAction() == AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED) || (it.getAction() == AudioManager.ACTION_SCO_AUDIO_STATE_CHANGED)) {

    		int as=it.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1111);
    		int prevScoConnectState = state.scoConnectState;
            
    		switch (as) {
            
            case AudioManager.SCO_AUDIO_STATE_CONNECTED:

            	Log.d(TAG_SPEECH, "SCO_AUDIO_STATE_CONNECTED");
            	
        		logState(0);

            	state.scoConnectState = SCO_CONNECTED;
            	
            	state.deviceUsedSco = true; // by default we try work with device in SCO mode
            	
            	if (!isOpened())
            		return;
            	
            	if (state.state == STOPPED_WAITING_SCO)
            		logState(STOPPED);
            	
            	if (state.state == STARTING_WAITING_SCO)
            		startNow();
            	
            	return;
            	
            case AudioManager.SCO_AUDIO_STATE_ERROR:
            	Log.d(TAG_SPEECH, "SCO_AUDIO_STATE_ERROR");
            	
        		logState(0);
        		
            	state.scoConnectState = SCO_DISCONNECTED;

            	if (!isOpened())
            		return;
            	
            	state.deviceUsedSco = false;
            	
            	if (state.state == STOPPED_WAITING_SCO 
            			|| state.state == STOPPED_CANCEL_SCO
            			|| state.state == STARTING_WAITING_SCO)
            		logState(STOPPED);

            	
            	return;
            	
            case AudioManager.SCO_AUDIO_STATE_CONNECTING:
            	Log.d(TAG_SPEECH, "SCO_AUDIO_STATE_CONNECTING");
            	
        		logState(0);
        		/*
        		if (state.scoConnectState != SCO_CONNECTING) {
        			state.scoConnectState = SCO_CONNECTING;
        			state.scoConnectStart = System.currentTimeMillis();
        		}*/
        		
        		return;
            	
            case AudioManager.SCO_AUDIO_STATE_DISCONNECTED:

            	Log.d(TAG_SPEECH, "SCO_AUDIO_STATE_DISCONNECTED");
            	
        		logState(0);
        		
            	state.scoConnectState = SCO_DISCONNECTED;

            	if (!isOpened())
            		return;
            	
            	// because bluetooth not connected
            	if (state.btConnectState != BT_CONNECTED) {
            		logState(STOPPED);
            	}
            	// because my request STOP SCO
            	else if (state.state == STOPPED_CANCEL_SCO) {
            		logState(STOPPED);
            	}
            	// because my request START SCO fails
            	else if (state.state == STOPPED_WAITING_SCO 
            			|| state.state == STARTING_WAITING_SCO) {
                	state.deviceUsedSco = false;
            		logState(STOPPED);
            	}
            	else {
            		// here two variants:
            		// 1) bluetooth just disconnected
            		// 2) button pressed
            		// solution to detect 1 or 2:
            		// try to connect bluetooth again, if successfully - start listening
                	Log.d(TAG_SPEECH, "BLUETOOTH BUTTON CLICKED ? ");
            		startSco();
        			logState(STARTING_WAITING_SCO);
            	}
            	
           		return;
    		}
    		
    		return;
    	}
  
    	if (it.getAction() == BluetoothDevice.ACTION_ACL_CONNECTED) {

        	Log.d(TAG_SPEECH, "ACTION_ACL_CONNECTED");
        	
    		logState(0);

            BluetoothDevice device = it.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (device == null)
                return;

            if (device.getBluetoothClass().getMajorDeviceClass() != BluetoothClass.Device.Major.AUDIO_VIDEO)
                return;

    		switchToBluetoothMode();
    		
        	state.deviceUsedSco = true; // by default we try work with device in SCO mode
        	
        	if (state.state == CLOSED)
        		return;

        	abort();
        	
    		return;

    	}
    	
    	if (it.getAction() == BluetoothDevice.ACTION_ACL_DISCONNECTED) {

        	Log.d(TAG_SPEECH, "ACTION_ACL_DISCONNECTED");
        	
    		logState(0);

            BluetoothDevice device = it.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (device == null)
                return;

            if (device.getBluetoothClass().getMajorDeviceClass() != BluetoothClass.Device.Major.AUDIO_VIDEO)
                return;

            switchToNormalMode();

        	if (state.state == CLOSED)
        		return;

        	abort();
    		stopSco();
    		
    		return;

    	}
    }
	
	// calls when Speech recognizer reports on ERROR !!!
	synchronized public void onError(int error) {

		switch (error) {
		
		// no audio device
		case SpeechRecognizer.ERROR_AUDIO:
			final AudioManager am = (AudioManager) App.self.getSystemService(Context.AUDIO_SERVICE);

			Log.d(TAG_SPEECH, "ERROR AUDIO");
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
        	
			return; // !!!! dont call onResults !!!!
			
		// client calls stop recognition
		case SpeechRecognizer.ERROR_CLIENT:

			Log.d(TAG_SPEECH, "ERROR CLIENT (CANCEL RECOGNITION BY CLIENT)");
			break;
			
		// no programm permissions
		case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:

			Log.d(TAG_SPEECH, "ERROR INSUFFICIENT PROGRAMM PERMISSIONS");
			ToastController.showSimpleToast("ERROR INSUFFICIENT PROGRAMM PERMISSIONS", 0);
			break;
			
		// no internet connection 
		case SpeechRecognizer.ERROR_NETWORK:
		case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:

            Log.d(TAG_SPEECH, "!!!!!!!!!!! ****************** ERROR NETWORK ************** !!!!!!!!!!!!! ");

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
		case SpeechRecognizer.ERROR_SERVER:
		case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
            Log.d(TAG_SPEECH, "!!!!!!!!!!! ****************** VOICE RECOGNIZER BUSY ************** !!!!!!!!!!!!! ");

            // initiated by user
			// sometimes, when user cancel recognition may be error
			if (state.state == STOPPING)
				break;
			
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
			
		default:
	    	Log.d(TAG_SPEECH, "ERROR CODE "+error);
			ToastController.showSimpleToast("ERROR CODE "+error, 0);
			break;
		}
		
		onResults(null);
	}

	// Woow! Speech recognizer ready to listen!!!
	synchronized public void onReadyForSpeech(Bundle params) {

    	Log.d(TAG_SPEECH, "onReadyForSpeech...");
		logState(0);

		// if using bluetooth, stop playing sound after pressing button
		if (soundButtonClicked.isPlaying())
			soundButtonClicked.pause();
		if (soundThinking.isPlaying())
			soundThinking.pause();
		/*
		if ((state.state != STARTING_COMMON) && (state.state != STARTING_BLUETOOTH)) {
	    	Log.d(TAG_SPEECH, "onReadyForSpeech: INVALID STATE !!!!!!!!!!!!");
			return;
		}*/
		
		if (animator != null)
			animator.showReadyToBegin();

       	logState(STARTED);
       	silence.silenceStart = null;
    	
    	Log.d(TAG_SPEECH, "onReadyForSpeech: ok");
	}

	// Speech recognizer reports, that recognizing finished and give us phrase
	synchronized public void onResults(final Bundle results) {
		
    	Log.d(TAG_SPEECH, "onResults...");
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
		    	ArrayList<String> list = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
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
		    	ArrayList<String> old_list = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (old_list == null || old_list.size() == 0)
                    res = Activity.RESULT_CANCELED;
                else {
                    ArrayList<String> new_list = old_list;
                    if (realReplaceTeaching)
                        new_list = teachReplaceTeaching(old_list);

                    it.putExtra(EXTRA_RESULT_NO_REPLACEMENT, old_list);

                    if (new_list == null) {
                        res = RESULT_RUN_INTENT;
                        new_list = old_list;
                    }
                    else {
                        res = Activity.RESULT_OK;
                    }
                    Log.d(TAG_SPEECH, "results: "+new_list.get(0));
                    it.putExtra(RecognizerIntent.EXTRA_RESULTS, new_list);
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
	
	static float maxDB = 10;

	// notify on change DB level in microphone
	public void onRmsChanged(final float rmsdB) {
		
		// TODO: remove 
		Log.w(TAG_SPEECH, "onRmsChanged---->");
		
		if (state.state != STARTED)
			return;
		
		// TODO: remove 
		Log.w(TAG_SPEECH, "onRmsChanged: exit, state != STARTED");
				
		
		if (animator != null) {
            if (rmsdB > maxDB)
                maxDB = rmsdB;

            animator.showListening(rmsdB);
        }
		
		//Log.d(TAG_SPEECH, String.valueOf(rmsdB));
		
		// for free form don't use auto stop
		if (realFreeForm) { 
			Log.w(TAG_SPEECH, "onRmsChanged: exit (realFreeForm)");
			return;
		}
			
		long ctime = System.currentTimeMillis();
		if (silence.silenceStart == null)
			silence.silenceStart = ctime;
			
		if (rmsdB > Silence.SILENCE_LEVEL)
			silence.silenceStart = ctime;
		else {
			if(ctime - silence.silenceStart > Silence.MAX_SILENCE_LENGTH) {
	        	Log.d(TAG_SPEECH, "detected silence =============================");
	        	stop();
			}
		}
	}

	// notify when speech recognizer detect silence after the speech
	synchronized public void onEndOfSpeech() {

    	Log.d(TAG_SPEECH, "onEndOfSpeech...");
		logState(0);

		if ((state.state == STOPPED) || (state.state == STOPPING) || (state.state == ABORTING)) {
	    	Log.d(TAG_SPEECH, "onEndOfSpeech: exit");
			return;
		}
		
		if (state.state != STARTED) {
	    	Log.d(TAG_SPEECH, "onEndOfSpeech: INVALID STATE !!!!!!!!!!!!!!!!!!");
			return;
		}
		
		if (animator != null)
			animator.showThinking();

		if (!soundFinishedPlayed) {
			soundFinishedPlayed = true;
    		Log.d(TAG_SPEECH, "Play mic sound start finished");
			startSound(soundFinished);
		}
		
    	Log.d(TAG_SPEECH, "onEndOfSpeech: ok");
	}

	// notify: finished mic sound
	@Override
	synchronized public void onCompletion(MediaPlayer mp) {
		
		if (mp == soundStartCommon) {
			Log.d(TAG_SPEECH, "Finished mic sound start common");
			logState(0);
			if (state.state == STARTING_COMMON)
				startRecognizition();
			else
				if ((state.state == ABORTING) || (state.state == STOPPING) || (state.state == STOPPED))
					Log.d(TAG_SPEECH, "Finished mic sound exit");
				else
					Log.d(TAG_SPEECH, "Finished mic sound INVALID STATE !!!!!!!!!!!!!!");
		}
		
		if (mp == soundStartBlueTooth) {
			Log.d(TAG_SPEECH, "Finished mic sound start bluetooth");
			logState(0);
			if (state.state == STARTING_BLUETOOTH) {
				startRecognizition();
			}
			else if ((state.state == ABORTING) || (state.state == STOPPING) || (state.state == STOPPED))
					Log.d(TAG_SPEECH, "Finished mic sound exit");
				else
					Log.d(TAG_SPEECH, "Finished mic sound INVALID STATE !!!!!!!!!!!!!!");
		}
		
	}

	synchronized static public boolean isRinging() {
		VR vr = get();
		if (vr == null)
			return false;
		return vr.state.ringActive;
	}
	
	synchronized private void onPhoneStartFinish(int state) {
		switch (state) {
		case TelephonyManager.CALL_STATE_RINGING:
			Log.d(TAG_SPEECH, "RING DETECTED !!!!!!!!!!!!!!");
			VR.this.state.ringActive = true;
			abort();
			
			SuzieHints.disableHints();
			break;
		case TelephonyManager.CALL_STATE_OFFHOOK:
			Log.d(TAG_SPEECH, "OFFHOOK DETECTED !!!!!!!!!!!!!!");
			SuziePopup.hideBubles(false, true);
			break;
		case TelephonyManager.DATA_DISCONNECTED:
			if (VR.this.state.ringActive) {
				Log.d(TAG_SPEECH, "RING FINISHED !!!!!!!!!!!!!!");
				VR.this.state.ringActive = false;
				
				SuzieHints.disableHints();
			}
			break;
		}
	}
	
	private PhoneStateListener listener = new PhoneStateListener() {
		public void onCallStateChanged(int state, String incomingNumber) {
			super.onCallStateChanged(state,incomingNumber);
			VR.this.onPhoneStartFinish(state);
		}
	};
	
	class TheRecognitionListener extends GoogleVoiceInterceptor {
		
		public TheRecognitionListener() {
		   super(
		     App.self.tpx,
		     StringConstants.speech_audio_logger_url()
		   );
		}
		
		boolean enabled = true;

		@Override
		public void onReadyForSpeech(Bundle params) {
	    	Log.d(TAG_SPEECH, "VR.onReadyForSpeech");
	    	super.onReadyForSpeech(params);
			if (enabled)
				VR.this.onReadyForSpeech(params);
		}
		
		ByteArrayOutputStream baos=null;

		@Override
		public void onBeginningOfSpeech() {
	    	Log.d(TAG_SPEECH, "VR.onBeginningOfSpeech");
	    	super.onBeginningOfSpeech();
		}

		@Override
		public void onRmsChanged(float rmsdB) {
			super.onRmsChanged(rmsdB);
			if (enabled)
				VR.this.onRmsChanged(rmsdB);
		}

		@Override
		public void onBufferReceived(byte[] buffer) {
	    	Log.d(TAG_SPEECH, "VR.onBufferReceived");
	    	super.onBufferReceived(buffer);
		}

		@Override
		public void onEndOfSpeech() {
	    	Log.d(TAG_SPEECH, "VR.onEndOfSpeech");
	    	super.onEndOfSpeech();
			if (enabled)
				VR.this.onEndOfSpeech();
		}

		@Override
		public void onError(int error) {
	    	Log.d(TAG_SPEECH, "VR.onError");
	    	super.onError(error);
			if (enabled)
				VR.this.onError(error);
		}

		@Override
		public void onResults(Bundle results) {
	    	Log.d(TAG_SPEECH, "VR.onResults");
	    	super.onResults(results);
			if (enabled)
				VR.this.onResults(results);
		}

		@Override
		public void onPartialResults(Bundle partialResults) {
	    	Log.d(TAG_SPEECH, "VR.onPartialResults");
	    	super.onPartialResults(partialResults);
	    	if (enabled)
	    		VR.this.onPartialResults(partialResults);
		}

		@Override
		public void onEvent(int eventType, Bundle params) {
	    	Log.d(TAG_SPEECH, "VR.onEvent");
	    	super.onEvent(eventType, params);
		}
		
	};
	
	synchronized public void onPartialResults(Bundle results) {
		Log.d(TAG_SPEECH, "Notify on partial results "+activity+"...");

		if (results==null)
			return;
		
		try {
			final Method mOnActivityResult = this.activity.getClass().getDeclaredMethod(
					  "onActivityResult", 
				       int.class, int.class, Intent.class
				   ) ;
			mOnActivityResult.setAccessible(true);
			
	    	
	    	String result = fullText;
	    	ArrayList<String> list = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
	    	if (list != null && list.size() > 0) {
	    		if (Utils.isEmpty(result))
	    			result = list.get(0);
	    		else
	    			result += " " + list.get(0);
	    	}
	    	if (Utils.isEmpty(result))
	    		return;
	    	
		    list = new ArrayList<String>();
		    list.add(result);

			final Intent it = new Intent();
    		it.putExtra(RecognizerIntent.EXTRA_RESULTS, list);

			mOnActivityResult.invoke(activity, VOICE_RECOGNITION_REQUEST_CODE, Activity.RESULT_FIRST_USER, it);
			
			Log.d(TAG_SPEECH, "Notify on partial results ok");
			
		} catch (Exception e) {
		}
	}

	/*********************************************************************************************************************************************/
	/**********************  MUTING **************************************************************************************************************/
	/*********************************************************************************************************************************************/
	
	protected static Mute mute = new Mute();
	
	protected static class Mute { 
		final static int MUTED = 0;
		final static int UNMUTED = 1;
		final static int WAITING = 2; // waiting 3 second to set state UNMUTED
		public static int muteState = UNMUTED;
	}
	
	private synchronized void tryUnMuteNow() {
		
		/*if (App.self.getActiveActivity() != null) {
			Mute.muteState = Mute.MUTED;
			Log.d(TAG_SPEECH, "unmuting canceled - found activity!");
			return;
		}*/
		
		if (isStarted()) {
			Mute.muteState = Mute.MUTED;
			Log.d(TAG_SPEECH, "unmuting canceled - vr active!");
			return;
		}
	
		if (Mute.muteState != Mute.WAITING) {
			Mute.muteState = Mute.MUTED;
			Log.d(TAG_SPEECH, "unmuting canceled!");
			return;
		}
		
		if (MyTTS.isSpeaking()) {
			
			// start timer to unmute
            /*
			unmuteTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					tryUnMuteNow();
				}
			}, 1000);*/
			Mute.muteState = Mute.MUTED;
			Log.d(TAG_SPEECH, "unmuting canceled!");
			return;
		}
		
		Mute.muteState = Mute.UNMUTED;

		final AudioManager am = (AudioManager) App.self.getSystemService(Context.AUDIO_SERVICE);
		
		if (Utils.isAndroid22orAbove) {
			// abandom audio focus to notify other app to continue playing
			am.abandonAudioFocus(audioFocusChangeListener);
			Log.d(TAG_SPEECH, "abandoned audio focus!");
		}
		
		// some app playing music always and don't listen event audio focus!
		// we unmuting this apps
		am.setStreamMute(AudioManager.STREAM_DTMF, false);
		//am.setStreamMute(AudioManager.STREAM_MUSIC, false);
		Log.d(TAG_SPEECH, "unmuted all!");

        am.setMode(AudioManager.MODE_NORMAL);

		stopSco();

        SphinxRecornizer.start();
	}

    static public synchronized void unMute() {

        if (Mute.muteState != Mute.MUTED)
            return;

        Mute.muteState = Mute.WAITING;

        Log.d(TAG_SPEECH, "unmute: task scheduled");

//        if (selfWr != null)
//            selfWr.get().tryUnMuteNow();

        if (selfWr != null)
            try {
                selfWr.get().unmuteTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (selfWr != null)
                            selfWr.get().tryUnMuteNow();
                    }
                }, 10000);
            } catch (Exception e)
            { e.printStackTrace(); }
    }

    public synchronized void mute() {

        startSco();

        if (Mute.muteState == Mute.MUTED)
            return;

        if (Mute.muteState == Mute.WAITING) {
            Mute.muteState = Mute.MUTED;
            unmuteTimer.cancel();
            unmuteTimer = new Timer();
            Log.d(TAG_SPEECH, "unmuting canceled");
            return;
        }

        Mute.muteState = Mute.MUTED;

        final AudioManager am = (AudioManager) App.self.getSystemService(Context.AUDIO_SERVICE);

        // request audio focus to notify other app to pause playing
        if (Utils.isAndroid22orAbove) {
            am.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            Log.d(TAG_SPEECH, "requested audio focus!");
        }

        // some app playing music always and don't listen event audio focus!
        // we muting this apps!
//        am.setStreamMute(AudioManager.STREAM_DTMF, true);
        //am.setStreamMute(AudioManager.STREAM_MUSIC, true);

        if (state.btConnectState == BT_CONNECTED)
            am.setMode(AudioManager.MODE_IN_COMMUNICATION);

        SphinxRecornizer.stop();

        Log.d(TAG_SPEECH, "all muted!");
    }

    private AudioFocusChangeListener audioFocusChangeListener = null;
	class AudioFocusChangeListener implements AudioManager.OnAudioFocusChangeListener {
		
		@Override
		// this method notifies on request audio focus by other app
		public void onAudioFocusChange(int focusChange) {
			if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
				Log.d(TAG_SPEECH, "*** audio focus changed: LOSS");
				return;
			}
			if (focusChange == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
				Log.d(TAG_SPEECH, "*** audio focus changed: REQUEST FAILED");
				return;
			}
			if (focusChange == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
				Log.d(TAG_SPEECH, "*** audio focus changed: REQUEST GRANTED");
				return;
			}
			if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
				Log.d(TAG_SPEECH, "*** audio focus changed: GAIN");
				return;
			}
			if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
				Log.d(TAG_SPEECH, "*** audio focus changed: LOSS_TRANSIENT");
				return;
			}
			if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
				Log.d(TAG_SPEECH, "*** audio focus changed: LOSS_TRANSIENT_CAN_DUCK");
				return;
			}
			Log.d(TAG_SPEECH, "audio focus changed: "+focusChange);
		};
	}

	// start sound tick tack in bluetooth mode while robin thinking
	public void startSoundWaiting() {
		if (!isBlueToothConnected())
			return;

        if (soundThinking != null)
            soundThinking.start();
	}

	public void stopSoundWaiting() {
        if (soundThinking != null)
		    soundThinking.pause();
	}

	// update preferences
	private void updatePrefs() {
		Boolean useBt2 = App.self.shouldUseBluetooth();
		
		if (useBt == useBt2)
			return;
		
		Log.d(TAG_SPEECH, "CONFIG BLUETOOTH: "+useBt2);
		
		if (useBt2) {
			useBt = useBt2;
			startSco();
		}
		else {
			stopSco();
			useBt = useBt2;
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (PrefConsts.PF_BLUETOOTH.equals(key)) updatePrefs();
	}

	/*************************************************************************************************************************/
	/***********************   TEACHING  *************************************************************************************/
	/*************************************************************************************************************************/
	
	protected RobinDB rdb = null;

	// questions and answers
	private List<LearnedAnswer> qaList = null;

    private String escape(String s) {
        if (s == null || s.length() < 1)
            return "''";

        return "'"+s.replace("'", "''")+"'";
    }
	public void teachAdd(LearnedAnswer qa) {
		
		// correct query
		String q1 = qa.getQuestion();
		if (Utils.isEmpty(q1))
			return;
		q1 = q1.trim();
		if (Utils.isEmpty(q1))
			return;
		q1 = q1.toLowerCase();
		q1 = q1.replace("'", "");
		qa.setQuestion(q1);
		
		/****************************************/

		// correct answer
		String r1 = qa.getAnswer();
		if (Utils.isEmpty(r1))
			r1 = "";
		r1 = r1.trim();
	//	r1 = r1.replace("'", "");
		qa.setAnswer(r1);
		
		/****************************************/

		if (q1.toLowerCase().equalsIgnoreCase(r1))
			return;

		// find duplicates
		Boolean updated = false;
		if (qaList != null)
		for (int i = 0; i < qaList.size(); i++) {
			String q = qaList.get(i).getQuestion();
			String r = qaList.get(i).getAnswer();
            int s = qaList.get(i).getSay();
			if (q1.contains(q) && (q1.length() < q.length() * 2)) {
				if (r1.equalsIgnoreCase(r))
					return;
				
				qaList.set(i, qa);
				if (rdb != null) {
					//rdb.update(LearnedAnswer.class, "answer=? where question=?", r1,q,s);
					rdb.db.execSQL("update answers set answer = "+ escape(r1) + ", add_say = '"+s+"' where question = " + escape(q));
				}
				
				updated = true;
				break;
			}
		}

		if (!updated) {
			if (qaList != null)
				qaList.add(qa);
	
			if (rdb != null) {
                /*
                LearnedAnswer la = new LearnedAnswer();
                la.setAnswer(r1).setQuestion(q1).setSay(qa.getSay());
				rdb.save(la);
				*/
                rdb.db.execSQL("create table if not exists answers ( question TEXT primary key, answer TEXT, add_say INTEGER)");
				rdb.db.execSQL("insert into answers (question, answer, add_say) values ("+escape(q1)+", "+escape(r1)+", '"+qa.getSay()+"')");
			}
		}
		
		StringBuilder url = new StringBuilder(App.self.getString(R.string.teaching_url));
		try {
			url.append("&t=");
			url.append(URLEncoder.encode(App.self.android_id));
			url.append("&q=");
			url.append(URLEncoder.encode(qa.getQuestion(),"UTF-8"));
			url.append("&a=");
			url.append(URLEncoder.encode(qa.getAnswer(),"UTF-8"));
			url.append("&d=");
			url.append(URLEncoder.encode(App.self.getGmailAccountName(), "UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		final String goto_url = url.toString();

		new Thread("save teaching result") {
			@Override
			public void run() {
				try {
					HttpURLConnection  x = (HttpURLConnection)new URL(goto_url).openConnection();
				    x.getInputStream();
				    x.disconnect();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	// delete one of: (question and answer) or (query and replace)
	public void teachDel(String q1, String r1) {
		
		// correct query
		if (Utils.isEmpty(q1))
			return;
		q1 = q1.trim();
		if (Utils.isEmpty(q1))
			return;
		q1 = q1.toLowerCase();
	//	q1 = q1.replace("'", "");
		
		/****************************************/

		// correct answer
		if (Utils.isEmpty(r1))
			r1 = "";
		r1 = r1.trim();
	//	r1 = r1.replace("'", "");
		
		/****************************************/

		// question and answer 
		if (qaList != null)
			for (int i = 0; i < qaList.size(); i++) {
				String q = qaList.get(i).getQuestion();
				String r = qaList.get(i).getAnswer();
				if (q1.contains(q) && (q1.length() < q.length() * 2)) {
					qaList.remove(i);
					if (rdb != null) {
						//rdb.db.execSQL("delete from answers where question='"+q+"' and answer='"+r+"'");		
						rdb.delete(LearnedAnswer.class, "question=? and answer=?", q, r);
					}
					return;
				}
			}
	}

	public RobinDB teachGetRdb() {
		return rdb;
	}

	public void teachSetRdb(RobinDB rdb) {
		this.rdb = rdb;
	}

	public void teachClear() {

		if (qaList != null)
			qaList.clear();

		if (rdb != null) {
            try {
			    rdb.empty(LearnedAnswer.class);
            } catch (Exception e) {}
		}

	}

	// replace teaching
	public ArrayList<String> teachReplaceTeaching(List<String> list) {
    	ArrayList<String> res = new ArrayList<String>();
    	if (list != null && list.size() > 0) {
			for (int i = 0; i < list.size(); i++) {
				String s = list.get(i);
				s = s.toLowerCase();

				if (!isEmpty(qaList)) {
                    String s2 = s.replace("'", "");
                    for (LearnedAnswer x : qaList) {
                        // if found question and length of phrase is not too
                        // long...
                        if (s2.contains(x.getQuestion())
                                && (s2.length() < x.getQuestion().length() * 2) && !Utils.isEmpty(x.getAnswer())) {
                            if (x.getAnswer().contains("#Intent")) {
                                s = "";
                                try {
                                    Intent it = Intent.parseUri(x.getAnswer(), 0);
                                    Utils.startActivityFromNowhere(it);
                                } catch (Exception e) {
                                }
                                return null;
                            } else if (x.getSay() != 0)
                                s = App.self.getString(R.string.P_cmd_say) + " " + x.getAnswer();
                            else
                                s = x.getAnswer();
                            break;
                        }
                    }
                }
				
				res.add(s);
			}
    	}
    	return res;
	}
}
