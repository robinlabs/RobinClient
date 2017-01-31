package com.magnifis.parking;

import static com.magnifis.parking.VoiceIO.condListenAfterTheSpeech;
import static com.magnifis.parking.VoiceIO.listenAfterTheSpeech;
import static com.magnifis.parking.utils.Utils.isEmpty;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;


import android.util.Log;

import com.magnifis.parking.cmd.i.OnBeforeListeningHandler;
import com.magnifis.parking.model.QueryInterpretation;
import com.magnifis.parking.suzie.SuzieService;
import com.magnifis.parking.tts.MyTTS;
import com.magnifis.parking.utils.Utils;

public class VoiceIO {
	
	final static String TAG=VoiceIO.class.getName();
	
	public static void sayFromGui(String s) {
		MyTTS.sayFromGUI(App.self,s);
	}
	
	public static void sayFromGui(Integer s) {
		MyTTS.sayFromGUI(App.self,s);
	}
	
	public static void sayAndShowFromGui(Object s) {
	  if (s!=null)
		Output.sayAndShowFromGui(App.self, s, false);
	}
	
	public static void sayAndShowFromGui(Object s, boolean switchVoices) {
	  if (s != null) 
		Output.sayAndShowFromGui(App.self, s, switchVoices);
		
	}
	

	public static void sayShowFromGuiThenComplete(Object s) {
		sayFromGui(s,App.self.voiceIO.rrCondListen, true);
	}
	
	public static void sayAndShow(Object s) {
       Output.sayAndShow(App.self, s);
	}
	
	public static void sayAndShow(QueryInterpretation s) {
	    if (s!=null) s.sayAndShow(null);
	}
	
	public static void sayAndShowDifferent(String text, String toastExtra) {
		Output.sayAndShow(App.self, text + toastExtra, text, false); 
	}

	public static void sayFromGui(final Object sayAndShow, final Runnable runnable, final boolean andShow) {
		Output.sayAndShowFromGui(App.self, sayAndShow, runnable, andShow, false);
	}
	

	public static void listenAfterTheSpeech() {
	   MyTTS.execAfterTheSpeech(App.self.voiceIO.rrListen, true);
	}

	public static void condListenAfterTheSpeech() {
	   VR vr=VR.get();
	   if ((vr!=null&&vr.isBlueToothConnected()) || App.self.isInCarMode()) 
		 listenAfterTheSpeech();
	   else
	     MyTTS.execAfterTheSpeech(App.self.voiceIO.rrCondListen,true);
	}
	
	public static void runGuiAfterTheSpeach(final Runnable r) {
	   MyTTS.execAfterTheSpeech(
	      new Runnable() {
			@Override
			public void run() {
				Utils.runInMainUiThread(r);		
			}
		  }
	   );
	}
	
    public static void playTextAlerts(String[] alerts, String intro) {
		if (!isEmpty(alerts)) {			
	        boolean first=true;
			for (String alert : alerts) {
				if (first) {
				  first=false;
				  // pre-pand intro to the first update
				  alert=intro + "\n\n" +  alert;
				}
				else
				  alert= "\n" +  alert;
				Output.sayAndShow(App.self,new MyTTS.Wrapper(alert).setShowInNewBubble(true), true);
			}
		}
	}
    
	private Advance advance = null;
	
	public Advance getAdvance() {
		return advance;
	}
	
	private OperationTracker operationTracker = new OperationTracker(1, true);
	

	public OperationTracker getOperationTracker() {
		return operationTracker;
	}

	public void setAdvance(final Advance adv) {
		if ((advance = adv) == null)
			return;
		new Thread("setAdvance") {
			@Override
			public void run() {
				try {
					sleep(adv.getTimeout());
					VR vr=VR.get();
					if (vr.isListening()) {
						if (advance != null)
							vr.killMicrophone();
					} else
						advance = null;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	public void runAdvance() {
		Log.d(TAG,"runAdvance()");
		final Runnable adv = advance;
		if (adv != null) {
			advance = null;
			Log.d(TAG,"runAdvance() -- wait for");
			if (operationTracker.tryAcquire()) {
				Log.d(TAG,"runAdvance() -- run");
				adv.run();
			}
		}
	}
    
   
	private Runnable rrListen = new Runnable() {
		public void run() {
			_fireOpes();
			if (MainActivity.isOnTop()||SuzieService.isSuzieVisible()/* should be isSuzieActive*/)
			  synchronized (MyTTS.class) {
				if (MyTTS.isSpeaking())
					listenAfterTheSpeech();
				else
					listen();
			 }
		}
	};
	
	private boolean shouldListenAfterCommand=false;
	
	
	public boolean isShouldListenAfterCommand() {
		return shouldListenAfterCommand;
	}

	public void setShouldListenAfterCommand(boolean shouldListenAfterCommand) {
		this.shouldListenAfterCommand = shouldListenAfterCommand;
	}

	private Runnable rrCondListen = new Runnable() {
		public void run() {
			// _fireOpes();
			if (MainActivity.get() == null && !SuzieService.isSuzieVisible())
				return;
			if (shouldListenAfterCommand || advance != null) {
				synchronized (MyTTS.class) {
					if (MyTTS.isSpeaking())
						condListenAfterTheSpeech();
					else {
						_fireOpes();
						listen();
					}
				}
			} else
				_fireOpes();
		}
	};
	
	public static void fireOpes() {
		MyTTS.execAfterTheSpeech(new Runnable() {
			public void run() {
               App.self.voiceIO._fireOpes();
			}
		}, true // only if listen after the speach has not been called
		);
	}

	public void _fireOpes() {
		Log.d(TAG, "_fireOpes");
		operationTracker.release();
	}
	
	public static void listen() {
		App.self.voiceIO.listen(false);
	}

	public void listen(final boolean askToInstall) {
		Log.d(TAG,"listen");
		VR vr=VR.get();
		shouldListenAfterCommand = false;
		vr.start(askToInstall);
		if (listeningTimeout>0)  startListeningTimeout();
	}

	private Timer     listeningTimeoutTimer=new Timer();
	private long      listeningTimeout=0;
	private Object    listeningTimeoutSO=new Object();
	private TimerTask listeningTimeoutTask=null;
	
	
	
	public long getListeningTimeout() {
		return listeningTimeout;
	}

	public void setListeningTimeout(long listeningTimeout) {
		this.listeningTimeout = listeningTimeout;
	}

	public void cancelListeningTimeout() {
		listeningTimeout=0;
		if (listeningTimeoutTask!=null) {
			listeningTimeoutTask.cancel();
			listeningTimeoutTask=null;
		}
	}
	
	public void startListeningTimeout() {
		 listeningTimeoutTimer.schedule(
				 listeningTimeoutTask=new TimerTask() {
					 @Override
					 public void run() {
						 synchronized(listeningTimeoutSO) {
						   listeningTimeout=0;
						   VR.get().killMicrophone();
						   listeningTimeoutTask=null;
						 }
					 }
				 }, 
				 listeningTimeout
		 );				
	}	
	
	
	private WeakReference<UnderstandingProcessorBase> rMFetcher = null;
	
	public static void setCurrentUP(UnderstandingProcessorBase up) {
		App.self.voiceIO.rMFetcher=new WeakReference<UnderstandingProcessorBase>(up);
	}
	
	public static <T extends UnderstandingProcessorBase> T getCurrentUP() {
		WeakReference<UnderstandingProcessorBase> rMFetcher=App.self.voiceIO.rMFetcher;
		return (T)(rMFetcher==null?null:rMFetcher.get());
	}
	
	public  static void interruptPendingRequest() {
		UnderstandingProcessorBase up=getCurrentUP();
		if (up != null) up.cancel(true);
	}
}
