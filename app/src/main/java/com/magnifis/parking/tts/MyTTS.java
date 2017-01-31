package com.magnifis.parking.tts;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.magnifis.parking.App;
import com.magnifis.parking.Config;
import com.magnifis.parking.MainActivity;
import com.magnifis.parking.OperationTracker;
import com.magnifis.parking.R;
import com.magnifis.parking.VR;
import com.magnifis.parking.suzie.SuziePopup;
import com.magnifis.parking.suzie.SuzieService;
import com.magnifis.parking.utils.Utils;
import com.robinlabs.utils.BaseUtils;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.telephony.TelephonyManager;
import android.util.Log;


public class MyTTS implements TextToSpeech.OnInitListener {
	
	public static final int SYSTEM_VOICE=-1, FEMALE_VOICE=0, MALE_VOICE=1;

    @Override
    public void onInit(int status) {
    }
    
    public static interface IPlayableUrl {
        String getPlayableUrl();
    }
    
    public static class PlayableUrl extends Wrapper implements IPlayableUrl {
    	
    	final private String url;
    	
        public PlayableUrl(Object txt, String url) {
			super(txt);
			this.url=url;
		}

		public String getPlayableUrl() {
        	return url;
        }	
    }
    
    public static interface IMyTtsInstance {
    	void setMyTTS(MyTTS mytts);
    }

    public static interface ForBubbles {
		String getForBubbles();
	}
	
	public static interface OnSaidListener {
		void onSaid(boolean fAborted);
	}
	
	public static interface OnSaidListenerEx {
		void onSaid(boolean fAborted, boolean fByMenu);
	}
	
	public static interface IWrapper {
        Object getWrapped();
	}
	
	public static interface IBubblesInMainActivityOnly {
		boolean isBubblesInMainActivityOnly();
	}
	
	public static interface IBubblesInButtonOnly {
		boolean isBubblesInButtonOnly();
	}
	
	public static interface IControlsBubblesAlone {
		boolean isControllingBubblesAlone();
	}
	
	public static interface IShowInNewBubble {
		boolean shouldShowInNewBubble();
		boolean shouldShowNewBubbleAfter();
	}
	
	public static interface IHideBubbles {
		boolean shouldHideBubbles();
	}
	
	public static interface ISleep {
		long getTimeToSleep();
	}
	
	public static long getTimeToSleep(Object s) {
	   ISleep sl=Wrapper.findInterface(s, ISleep.class);
	   return sl==null?0:sl.getTimeToSleep();	
	}
	
	public static boolean shouldHideBubbles(Object s) {
		IHideBubbles nb=Wrapper.findInterface(s, IHideBubbles.class);
		return nb==null||nb.shouldHideBubbles();
	}
	
	public static boolean shouldShowInNewBubble(Object s) {
	  IShowInNewBubble nb=Wrapper.findInterface(s, IShowInNewBubble.class);
	  return nb!=null&&nb.shouldShowInNewBubble();
	}
	
	public static boolean shouldShowNewBubbleAfter(Object s) {
		  IShowInNewBubble nb=Wrapper.findInterface(s, IShowInNewBubble.class);
		  return nb!=null&&nb.shouldShowNewBubbleAfter();
		}
	
	public static boolean isForBubblesInMainActivityOnly(Object s) {
	  IBubblesInMainActivityOnly bma=Wrapper.findInterface(s, IBubblesInMainActivityOnly.class);
	  return bma!=null&&bma.isBubblesInMainActivityOnly();
	}
	
	public static boolean isForBubblesInButtonOnly(Object s) {
		  IBubblesInButtonOnly bma=Wrapper.findInterface(s, IBubblesInButtonOnly.class);
		  return bma!=null&&bma.isBubblesInButtonOnly();
		}
		
	public static boolean isControllingBubblesAlone(Object s) {
	  IControlsBubblesAlone cba=Wrapper.findInterface(s, IControlsBubblesAlone.class);
	  return cba!=null && cba.isControllingBubblesAlone() ;
	}
	
	public static class Wrapper implements OnStringSpeakListener, IWrapper, 
	   IBubblesInMainActivityOnly, IBubblesInButtonOnly, IControlsBubblesAlone, IShowInNewBubble,
	   IHideBubbles, ISleep, OnSaidListenerEx

    {
		
		protected long timeToSleep=0;
		
		public Wrapper setTimeToSleep(long timeToSleep) {
		   this.timeToSleep=timeToSleep;
		   return this;
		}
		
		@Override
		public long getTimeToSleep() {
			return timeToSleep;
		}

		protected boolean 
		          bubblesInMainActivityOnly=false, controllingBubblesAlone=false,
		          bubblesInButtonOnly = false,  showInNewBubble=false, shouldHideBubbles=true,
				  showNewBubbleAfter=false
				  ; 
		
		public Wrapper setShowInASeparateBubble() {
			showNewBubbleAfter=true;
			showInNewBubble=true;
			return this;
		}
		
		public Wrapper setShowNewBubbleAfter(boolean showNewBubbleAfter) {
			this.showNewBubbleAfter = showNewBubbleAfter;
			return this;
		}

		@Override
		public boolean shouldShowNewBubbleAfter() {
			return showNewBubbleAfter;
		}

		@Override
		public boolean shouldHideBubbles() {
			return shouldHideBubbles;
		}

		public Wrapper setShouldHideBubbles(boolean shouldHideBubbles) {
			this.shouldHideBubbles = shouldHideBubbles;
			return this;
		}

		@Override
		public boolean shouldShowInNewBubble() {
			return showInNewBubble;
		}
		
		public Wrapper setShowInNewBubble(boolean showInNewBubble) {
			this.showInNewBubble = showInNewBubble;
			return this;
		}

		@Override
		public boolean isControllingBubblesAlone() {
			return controllingBubblesAlone;
		}

		@Override
		public boolean isBubblesInMainActivityOnly() {
			return bubblesInMainActivityOnly;
		}

		@Override
		public boolean isBubblesInButtonOnly() {
			return bubblesInButtonOnly;
		}

		public Wrapper setBubblesInMainActivityOnly(boolean bubblesInMainActivityOnly) {
			this.bubblesInMainActivityOnly = bubblesInMainActivityOnly;
			return this;
		}
		
		public Wrapper setBubblesInButtonOnly(boolean bubblesInButtonOnly) {
			this.bubblesInButtonOnly = bubblesInButtonOnly;
			return this;
		}
		
		public Wrapper setControllingBubblesAlone(boolean controllingBubblesAlone) {
			this.controllingBubblesAlone = controllingBubblesAlone;
			return this;
		}

		public static <T> T findInterface(Object obj,Class<T> c) {
			if (obj==null) return null;
			if (Utils.isSubclassOf(obj.getClass(), c)) return (T)obj;
			if (obj instanceof IWrapper) {
				Object wrapped=((IWrapper)obj).getWrapped();
				for (;;) {
					if (wrapped==null) break;
					if (Utils.isSubclassOf(wrapped.getClass(), c)) return (T)wrapped;
					if (!(wrapped instanceof IWrapper)) break;
					wrapped=((IWrapper) wrapped).getWrapped();
				}
			}
			return null;		
		}
		
		public static Object getMostWrapped(Object obj) {
			if (obj!=null&&(obj instanceof IWrapper)) {
				Object wrapped=((IWrapper)obj).getWrapped();
				for (;;) {
					if (wrapped==null||!(wrapped instanceof IWrapper)) break;
					wrapped=((IWrapper) wrapped).getWrapped();
				}
				return wrapped;
			}
			return obj;
		}
		
		private final Object obj;
		
		@Override
		public Object getWrapped() { return obj;  }
		
		@Override
		public String toString() {
            if (obj instanceof Integer)
                return App.self.getString((Integer)obj);

            try {
                return obj.toString();
            } catch (Exception e) {}

            return "";
		}
		
		public Wrapper(Object obj) {
		   this.obj=obj;
		   if (obj!=null) {
			   timeToSleep=MyTTS.getTimeToSleep(obj);
			   shouldHideBubbles=MyTTS.shouldHideBubbles(obj);
			   showInNewBubble=MyTTS.shouldShowInNewBubble(obj);
			   bubblesInMainActivityOnly=MyTTS.isForBubblesInMainActivityOnly(obj);
			   bubblesInButtonOnly=MyTTS.isForBubblesInButtonOnly(obj);
			   controllingBubblesAlone=MyTTS.isControllingBubblesAlone(obj);
			   showNewBubbleAfter=MyTTS.shouldShowNewBubbleAfter(obj);
		   }
		}

		@Override
		public void onSaid(boolean fAborted) {
			if (obj instanceof OnSaidListener) ((OnSaidListener)obj).onSaid(fAborted); else
			if (obj instanceof Runnable&&!fAborted) ((Runnable)obj).run();
		}
		

		@Override
		public void onSaid(boolean fAborted, boolean fByMenu) {
		  if (obj instanceof OnSaidListenerEx) ((OnSaidListenerEx)obj).onSaid(fAborted, fByMenu); 
		  else
		     onSaid(fAborted&&!fByMenu);
		}

		@Override
		public void onToSpeak() {
			if (obj instanceof OnStringSpeakListener) ((OnStringSpeakListener)obj).onToSpeak();
		}

		public Object getMostWrapped() {
			return getMostWrapped(this);
		}
		
	}
	
	public static class BubblesInMainActivityOnly extends Wrapper {
		public BubblesInMainActivityOnly(Object obj) {
			super(obj);
			super.setBubblesInMainActivityOnly(true);
		}
	}
	
	public static class BubblesInButtonOnly extends Wrapper {
		public BubblesInButtonOnly(Object obj) {
			super(obj);
			super.setBubblesInButtonOnly(true);
		}
	}
	
	public static class WithoutBubbles extends Wrapper {
		public WithoutBubbles(Object obj) {
			super(obj);
			super.setControllingBubblesAlone(true);
		}
	}
	
	protected static class SuspendMessage extends Wrapper {

		public SuspendMessage(Object obj) {
			super(obj);
			setControllingBubblesAlone(true);
			setShowInNewBubble(true);
		}
		

		@Override
		public void onSaid(boolean fAborted) {
			super.onSaid(fAborted);
			try {
			  App.self.unregisterReceiver(br);
			} catch(Throwable t) {};
			AudioManager am=App.self.getAudioManager();
			am.setStreamVolume(AudioManager.STREAM_MUSIC,musicVolume,0);
		}
		
		int musicVolume;
		float maxRingerVolume, maxMusicVolume;
		AudioManager am=null;
		BroadcastReceiver br=new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
			   	  stopIt();
			}
		};

		@SuppressLint("NewApi") @Override
		public void onToSpeak() {
			super.onToSpeak();
			am=App.self.getAudioManager();
			musicVolume=am.getStreamVolume(AudioManager.STREAM_MUSIC);
			maxRingerVolume=am.getStreamMaxVolume(AudioManager.STREAM_RING);
			maxMusicVolume=am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			////////////////////////////////////////
			float ringerVolume=am.getStreamVolume(AudioManager.STREAM_RING), ringVolumeLevel=ringerVolume/maxRingerVolume;
			int vl=Math.round(ringVolumeLevel*maxMusicVolume);
			am.setStreamVolume(AudioManager.STREAM_MUSIC, vl, 0);		
	        App.self.registerReceiver(br, new IntentFilter("android.media.VOLUME_CHANGED_ACTION"));
		}
		
		
		
	}
	
	public static interface OnStringSpeakListener extends  OnSaidListener {
		void onToSpeak();
	};
	
	public static String TAG="MyTTS";
	
	private volatile TTS tts=null;
	
	private ArrayList que=new ArrayList();
	
	private void que_add_with_conversion(Object obj) {
		que.add(checkLang(obj));
	}
	
	public void removeFromQue(Object o) {
		synchronized(MyTTS.class) {
		  que.remove(o);
		}
	}
	
	private static volatile MyTTS instance=null;
	
	
	private Runnable completion=null;
	
	public void setCompletionListener(Runnable r) {
		Log.d(TAG,"setCompletionListener "+(r!=null));
		completion=r;
	}
	
	private boolean suspended=false;
	
	protected Object inProcess=null;
	
	public static void suspend() {
		suspend(null);
	}
	
	// stops current speaking only
	public static void stopIt() {
		synchronized(MyTTS.class) {
			if (instance!=null&&instance.isSpeaking()&&(instance.inProcess instanceof SuspendMessage)) {
				TTS tts=instance.tts;
				if (tts!=null) {
					instance.tts_stop();
				}
			}
		}
	}
	
	public static void suspend(Object sayBefore) {
		suspend(sayBefore,null);
	}
	
	@SuppressWarnings("unchecked")
	public static void suspend(Object sayBefore,  final String showBefore) {
		synchronized(MyTTS.class) {
			if (instance != null && instance.sleeping != null) 
				MyTTS.class.notify();
		}
		
		synchronized(MyTTS.class) {
			if (instance!=null) {
				/*if (!instance.suspended)*/ {
				  if (instance.inProcess!=null) {
					if (sayBefore==null) {
					  if (!instance.suspended) {
					    instance.suspended=true;
					    instance.tts.stop();
					  }
					} 
					else {
					   instance.que.add(
							0,
							new SuspendMessage(sayBefore) {
								Object savedCurrent = instance.inProcess;
								
								@Override
								public void onSaid(boolean fAborted) {
									Log.d(TAG,"suspend: onSaid");
									super.onSaid(fAborted);

									synchronized(MyTTS.class) {
									   instance.inProcess=savedCurrent;
									}
								}
								
								@Override
								public void onToSpeak() {
									instance.suspended=true;
									if (showBefore!=null)  
										showAnswerBubble(new Wrapper(showBefore).setShowInNewBubble(true));
									super.onToSpeak();
								}							   
							}
					   );
					   
					   if (instance.suspended)
						  instance._say(instance.inProcess=instance.que.remove(0)); // ??? bug here	
					   else
						   instance.tts.stop();
					}
				  }
				}
			} 
			else if (sayBefore!=null) {
				Log.d(TAG,"sayBefore!=null");
				new MyTTS(App.self,
					new SuspendMessage(sayBefore) {
						  
					  @Override
					  public void onToSpeak() {
						Log.d(TAG,"suspend: onToSpeak");
						if (showBefore!=null)  showAnswerBubble(new Wrapper(showBefore).setShowInNewBubble(true));
						super.onToSpeak();
					  }	
					  
					  @Override
					  public void onSaid(boolean fAborted) {
						Log.d(TAG,"suspend: onSaid");
						super.onSaid(fAborted);
						if (instance!=null) instance.inProcess=null;
					  }				
				    }
				);
				instance.suspended=true;
			}
		}
	};
	
	public static void execAfterTheSpeech(Runnable r) {
		execAfterTheSpeech(r, false);
	}
	
	public static boolean isSpeaking() {
       try  {
    	 MyTTS i=instance;
	     return i!=null&&i.inProcess!=null;
       } catch (Throwable t) {}
       return false;
	}
	
	public static void execAfterTheSpeech(Runnable r, boolean ifNotSetOnly) {
		synchronized(MyTTS.class) {
			if (instance==null) r.run(); else {
				if (!ifNotSetOnly||instance.completion==null) instance.setCompletionListener(r);
				if (!isSpeaking()) {
					Runnable co=instance.completion;
					instance.completion=null;
					co.run();
				}
			}
		}
	}
	
	private void selfSpeakableStop() {
		Object ip=inProcess;
		if (ip!=null&&ip instanceof SelfSpeakable) ((SelfSpeakable)ip).abort();
	}
	
	private void tts_stop() {
		selfSpeakableStop();
		tts.stop();
	}
	
	public static void resume() {
		synchronized(MyTTS.class) {
			if (instance!=null&&instance.suspended) {
				instance.suspended=false;
  				if (instance.inProcess!=null&&instance.inProcess instanceof SuspendMessage) {  
				   instance.tts_stop(); return;
				 }
  			     new Thread () {
  			       @Override
  			       public void run() {
  			    	  MyTTS i=instance;
  			    	  if (i!=null) {
  			    		Object ip=i.inProcess;
  			    		if (ip!=null) i._say(ip);
  			    	  }
  			       }
  			     }.start();
  			     
			   }
			}
		
	}

	public static void shutdown() {
		abort();
	}
	
	public static boolean abort() {
		return abort(null,false,true);
	}
	
	public static boolean abortWithoutUnlock() {
		return abort(null,false,false);
	}
	
	public static boolean abort(Runnable handler) {
		return abort(handler,false,true);
	}
	
	public static boolean abort(Runnable handler, boolean fByMenuButton) {
		return abort(handler,fByMenuButton,true);
	}
	
	boolean fAbort=false, fByMenuButton=false;
	Runnable abortHandler=null;
	
	public static boolean abort(Runnable handler, boolean fByMenuButton, boolean fUnlock) {
		Log.d(TAG,"abort");
		synchronized(MyTTS.class) {
			if (instance!=null) {
				instance.fByMenuButton=fByMenuButton;
				instance.fAbort=true;
				
				if (instance.completion!=null) {
				  instance.completion=null;
				  // a dirty trick
				  if (fUnlock) {
					  Log.d(OperationTracker.TAG,"dirty");
					  App.self.voiceIO.getOperationTracker().release();
				  }
				 
				  //
				}
                VR.unMute();
				instance.que.clear();
				
				if (instance.sleeping==null) {
					Object ip=instance.inProcess;
					if (ip!=null&&ip instanceof SelfSpeakable) {
						((SelfSpeakable)ip).abort();
					}

					if (instance.tts!=null) {
						instance.tts_stop();
						if (instance.tts!=null) instance.tts.shutdown();
					}
					instance=null;
					if (handler!=null) handler.run();
				} else {
					instance.abortHandler=handler;
					instance.sleeping.setPriority(Thread.MAX_PRIORITY);
					//instance.sleeping.interrupt();
					MyTTS.class.notify();
				}
				
				return true;
			}
	    }
		return false;
	}
	
	public static boolean interrupt() {
		synchronized(MyTTS.class) {
			if (instance!=null) {
				instance.que.clear();
				instance.tts_stop();
				return true;
			}
		}
		return false;
	}
	
	public static void syncSayFromGUI(final Context context,final Object s) {
		Utils.runInGuiAndWait(
		  context, 
		  new Runnable() {
			@Override
			public void run() {
				speakText(s);
			}
		  }
		);
	}
	
	public static void sayFromGUI(final Context ctx,final Object s) {
		Utils.runInMainUiThread(
		  ctx,
		  new Runnable() {
			@Override
			public void run() {
				speakText(s);
			}
		  }
		);
	}
	     
	private void handleEmptyText() {
	  Log.e(TAG,"attempt to say empty text");
	}
	
	private static boolean useGoogleVoice(String lang) {
		return 
			   !"ru".equals(lang)||
			   Config.use_google_online_voice_in_russian||
			   !App.self.isInRussianMode()
				;
	}

	// check string
	// creates media player with sound
	// otherwise returns same string
	private Object checkLang(final Object obj) {
		
		if (obj == null) return null;
		
		
		//::;
        IPlayableUrl ipu = Wrapper.findInterface(obj, IPlayableUrl.class);
        if (ipu!=null) {
    		try {
      		  TtsMediaPlayer mp=new TtsMediaPlayer(
      				this,
      				ipu.getPlayableUrl(),
      				obj
      		  );	
      		  return mp;
      		} catch(Throwable ttx) {
      			Log.e(TAG, ttx.getMessage());
      		}         	
        	
        }
		
				
		ITextInLang itl=Wrapper.findInterface(obj, ITextInLang.class);
		
		if (itl==null) return obj;
		
		String lang = itl.getLang();
		
		if (!MyTTS.useGoogleVoice(lang)) return obj;
		
		
		if (_voiceFromSettings == SYSTEM_VOICE) {
		   return obj;
		} 
	
		String text = itl.getText();
		
		
	    Object mp=createPlayer(obj,text,lang);

	    return mp==null?obj:mp;
    }
	
	private TtsMediaPlayer createPlayer(Object obj,String text,String lang)  {
		try {
		  return new TtsMediaPlayer(
				this,
				"http://translate.google.com/translate_tts?tl=" + lang + "&q=" + URLEncoder.encode(text,"UTF-8"),
				obj
		  );	
		} catch(Throwable t) {
			Log.e(TAG, "speak in lang: "+t.getMessage());
		}
		return null;
	}
	

	
	public static boolean hasUnsupporedLetters(CharSequence cs) {
	  if (cs!=null) for (int i=0;i<cs.length();i++) {
		char c=cs.charAt(i);
		if (Utils.isNotEng(c)&&!Utils.isRus(c)) return true;  
	  }
	  return false;
	}
	
	static private Object limitTextInLangToNWords(Object o,String lang,int N) {
	   if (o==null) return null;
	   if (o instanceof Object[]) {
		 ArrayList al=new ArrayList();
		 for (Object x:(Object[])o) {
		   Object r=limitTextInLangToNWords(x,lang,N);
		   if (r instanceof Object[]) 
			 for (Object z:(Object[])r) al.add(z);
		   else
			 al.add(r);
		 }
		 return al.toArray();
	   }
	   if (!(o instanceof TextInLang)) return o;
	   TextInLang til=(TextInLang)o;
	   if (!lang.equals(til.getLang())) return o;
	   String text=til.getText();
	   int wc=BaseUtils.countWords(text);
	   if (wc>N) {
		  ArrayList<TextInLang> parts=new ArrayList();
		  for (int ic=0;ic<text.length();) {
	         StringBuilder sb=new StringBuilder();
	         boolean inWord=false;
	         for (int cnt=0;ic<text.length();ic++) {
	        	char c=text.charAt(ic);
	        	if (BaseUtils.isSpace(c)) { 
	        		if (inWord) {
	        		  inWord=false;
	        		  if (cnt>=N) break;
	        		}
	            } else {
	              if (!inWord) cnt++;
	        	  inWord=true;
	            }
	        	sb.append(c);
	         }
	         parts.add(new TextInLang(lang,sb.toString()));
		  }
		  return parts.toArray();
	   }
	   return til;
	}
	
	/**
	 * 
	 * @param s
	 * @return Array of String / TextInLang
	 */
	
	static private  boolean isRus(int c, boolean russianMode) {   
	   if (Utils.isRus(c)) return true;
	   if (russianMode&&!(Character.isLetter(c)||Character.isSpaceChar(c))) return true;
	   return false;
	}
	
	static private Object [] breakStringByLanguage(String x) {
		if (x==null) return null;

        boolean mode_rus = App.self.isInRussianMode();
		
		if (!Utils.isEmpty(x)) {
		   for (int i=0;i<x.length();i++) 
			   if (isRus(x.charAt(i), mode_rus)) {
				 ArrayList al=new ArrayList();
		         if (i>0) 
		            al.add(new TextInLang("en", x.substring(0,i)));
		         
		         boolean rus=true; int start=i;
		         
		         for (;i<x.length();i++) {
		        	 char c=x.charAt(i); boolean irus=isRus(c,rus);
		        	 if (Character.isLetter(c)||irus) {
		        		 if (irus) {
		        			 if (!rus) {
		        				 al.add(/*new TextInLang("en",*/ x.substring(start,i)/*)*/);
		        				 start=i;
		        				 rus=true;
		        			 }
		        		 } else {
		        			 if (rus) {
		        				 al.add(new TextInLang("ru", x.substring(start,i)));
		        				 start=i;
		        				 rus=false;
		        			 }
		        		 }
		        	 }
		         }
		         
		         if (start<x.length()) {
		        	 String tail=x.substring(start,x.length());
		        	 al.add(rus?new TextInLang("ru", tail):/*new TextInLang("en",*/ tail/*)*/);
		         }
		         
		         return al.toArray();
			   }
		}

        if (mode_rus)
            return new Object[] {new TextInLang("en", x)};
        else
            return new Object[] {x};
	}
	
	public static interface SelfSpeakable {
		void speak();
		boolean abort();
	}
	
	public static interface ITextInLang {
		public String getLang();
		public String getText(); 
	}
	
	public static class TextInLang implements ITextInLang {
		protected String lang;
		protected String text;
		

		public String getLang() {
			return lang;
		}

		public void setLang(String lang) {
			this.lang = lang;
		}

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}

		public TextInLang(String lang, Object text) {
			this.lang = lang;
			if (text instanceof Integer)
				this.text = App.self.getString((Integer)text);
			else
				this.text = text.toString();
		}
		
		@Override
		public String toString() {
			return text;
		}
	}

    /** Speak and show text. Supports English and Russian languages. */
    public static void speakText(final Object s) {
        if (App.self.isInPhoneConversation())
            return;

        speakTextInternal(s);
    }

    public static void speakTextInConversation(final Object s) {
        speakTextInternal(s);
    }

	/** Speak and show text. Supports English and Russian languages. */
	private static void speakTextInternal(final Object s) {

		if ((s==null) || (s.toString()==null)) {
			Log.e(TAG,"attempt to say empty text");
			return;
		}
		
		Log.d(TAG,"speakText = "+s);

		Object wrapped=Wrapper.getMostWrapped(s);
		
		if (wrapped instanceof TextInLang)
			_speakText(s); 
		else {
		   String ss;
		   Log.d(TAG,"speakText vr.stopped");
		   if (wrapped instanceof Integer)
		     ss=App.self.getString((Integer)wrapped);
		   else
			 ss=s.toString();
		   
		   Object [] parts=breakStringByLanguage(ss);
		   if ((Config.use_google_online_voice_in_russian || !App.self.isInRussianMode()) &&
				   !((!MainActivity.dontUseAcapella) || AnTTS.fineSystemRussian)) 
			    parts=(Object [])limitTextInLangToNWords(parts,"ru",8);
		   		   
		   if (!Utils.isEmpty(parts)) {
			   if ((parts.length==1) && !(parts[0] instanceof TextInLang)) {
				   _speakText((s instanceof Integer)?ss:s);
			   } 
			   else 
				   for (int i=0;i<parts.length;i++) {
					   Object part=parts[i];
					   final boolean last=i==parts.length-1;
					   Wrapper obj= (i==0) // first
							? new Wrapper(part) {
							@Override
							public void onSaid(boolean fAborted) {
								super.onSaid(fAborted);
								if (fAborted||last) {
									if (s instanceof OnSaidListener) 
										((OnSaidListener)s).onSaid(fAborted); 
									else if (s instanceof Runnable&&!fAborted) 
										((Runnable)s).run();
								}
							}

							@Override
							public void onToSpeak() {
								super.onToSpeak();
								if (s instanceof OnStringSpeakListener) 
									((OnStringSpeakListener)s).onToSpeak();
							}
							  
						  }
					 : new Wrapper(part) {
							@Override
							public void onSaid(boolean fAborted) {
								super.onSaid(fAborted);
								if (fAborted||last) {
									if (s instanceof OnSaidListener) ((OnSaidListener)s).onSaid(fAborted); else
									if (s instanceof Runnable&&!fAborted) ((Runnable)s).run();
								}
							}
						  }
				   ;

				  IBubblesInMainActivityOnly bma=Wrapper.findInterface(s, IBubblesInMainActivityOnly.class);
				  if (bma!=null) {
					  obj.setBubblesInMainActivityOnly(bma.isBubblesInMainActivityOnly());
				  }
				  IBubblesInButtonOnly bb=Wrapper.findInterface(s, IBubblesInButtonOnly.class);
				  if (bb!=null) {
					  obj.setBubblesInButtonOnly(bb.isBubblesInButtonOnly());
				  }
				  IControlsBubblesAlone cba=Wrapper.findInterface(s, IControlsBubblesAlone.class);
				  if (cba!=null) {
					  obj.setControllingBubblesAlone(cba.isControllingBubblesAlone());
				  }
				  if (i==0) {
					  IShowInNewBubble nb=Wrapper.findInterface(s, IShowInNewBubble.class);
					  if (nb!=null) {
						  obj.setShowInNewBubble(nb.shouldShowInNewBubble());
					  }
				  }
				  if (last) {
					  IHideBubbles hb=Wrapper.findInterface(s, IHideBubbles.class);
					  if (hb!=null) 
						  obj.setShouldHideBubbles(hb.shouldHideBubbles());
					  IShowInNewBubble nb=Wrapper.findInterface(s, IShowInNewBubble.class);
					  if (nb!=null) 
						  obj.setShowNewBubbleAfter(nb.shouldShowNewBubbleAfter());
					  obj.setTimeToSleep(getTimeToSleep(s));
				  }
				   
			     _speakText(obj);
		      }
		   }
		}
		
	}

	// s: string or int)eger or TextInLang
	// in this case (if found language modifier) local TTS not used and request will send to google translate
	private static void _speakText(Object s) {
		
		synchronized(MyTTS.class) {
		   if (instance==null)
			   new MyTTS(App.self, s);
		   else
			   if (s==null) 
				   instance.handleEmptyText();
			   else
				   instance.que_add_with_conversion(s);
		}
		//return instance;
	}
	
	private void beforeToSay(Object obj) {
		if (obj instanceof OnStringSpeakListener) {
			((OnStringSpeakListener)obj).onToSpeak();
		}		
	}
	
	public static void showAnswerBubble(Object s) {
		CharSequence w=Wrapper.findInterface(s, CharSequence.class);
		CharSequence ss=(w!=null)?w:(Utils.firstUpper(s.toString(), false).toString());
		
		MainActivity m = MainActivity.get();
		if (!isForBubblesInButtonOnly(s))
			if (m != null) {
				if (shouldShowInNewBubble(s)) m.bubblesBreak("#main"); //imp
				m.bubleAnswer(ss);
				if (MainActivity.isOnTop()) return;
			}
		
		if (!isForBubblesInMainActivityOnly(s))
			SuzieService.answerBubble(ss);
	}

	private void _say(Object s) {
		beforeToSay(s);

		if (!isControllingBubblesAlone(s)) showAnswerBubble(s);
		
		SuziePopup.resetAutoHideTimer();

        if (App.self.isInSilentMode()) {
            onCompletion();
            return;
        }

        VR vr = VR.get();
        if (vr != null) {
            vr.abort();
            vr.mute();
        }
//
        IPlayableUrl ipu = Wrapper.findInterface(s, IPlayableUrl.class);        
        if (ipu!=null) {        	  
    		try {
    		  TtsMediaPlayer mp=new TtsMediaPlayer(
    				this,
    				ipu.getPlayableUrl(),
    				s
    		  );	
    		  mp.speak();
    		  return;
    		} catch(Throwable ttx) {
    			Log.e(TAG, ttx.getMessage());
    		}        	  
        }
//      
        
        if (s instanceof SelfSpeakable) {
			((SelfSpeakable)s).speak();
		} else {
			String lang=getLangFromSpeakable(s);
			if (tts.setLanguage(lang)) {
			  tts.speak(s.toString());
			} else {
			  createPlayer(s, Utils.getString(s), lang).speak();
			}
		}
	}
	
	private String getLangFromSpeakable(Object s) {
		TextInLang til=Wrapper.findInterface(s, TextInLang.class);
		return til==null?"en":til.getLang();	
	}
	
	private void hideBubbles(Object _inProcess) {
	   if (shouldHideBubbles(_inProcess))
		SuziePopup.hideBubles(false, false);
	   IShowInNewBubble nb=Wrapper.findInterface( _inProcess, IShowInNewBubble.class);
	   if (nb!=null&&nb.shouldShowNewBubbleAfter()) {
		   MainActivity ma=MainActivity.get();
		   if (ma!=null) ma.bubblesBreak("#main");
	   } 
	}
	
	Thread sleeping=null;
	
	private void justSleep(long to) {;
		try {
		   sleeping=Thread.currentThread();
           MyTTS.class.wait(to);
		} catch (InterruptedException e) {
		} finally {
		   sleeping=null;
		}
	}
	
	protected boolean dontShutMeDown=false;
	
	protected void onCompletion() {
		Log.d(TAG, "onUtteranceCompleted");
		
		SuziePopup.resetAutoHideTimer();
		
		synchronized(MyTTS.class) {
			Object _inProcess=inProcess;
			if (inProcess!=null) {
				if (shouldShowNewBubbleAfter(inProcess)) {
				   MainActivity ma=MainActivity.get();
				   if (ma!=null) ma.bubblesBreak("#main");
				}
				
				long tts=getTimeToSleep(inProcess);
				if (inProcess instanceof OnSaidListenerEx) {
					if (tts!=0&&!fAbort) justSleep(tts);
					((OnSaidListenerEx)inProcess).onSaid(fAbort, fByMenuButton);
				} else
				if (inProcess instanceof OnSaidListener) {
					if (tts!=0&&!fAbort) justSleep(tts);
					((OnSaidListener)inProcess).onSaid(fAbort&&!fByMenuButton);
				} else if (inProcess instanceof Runnable) { 
					if (tts!=0&&!fAbort) justSleep(tts);
					((Runnable)inProcess).run();
				}
				if (dontShutMeDown) {
					dontShutMeDown=false;
					return;
				}
			}
			if (suspended) {
				if (que.isEmpty()) {
					if (tts!=null) {
						tts.stop();
						tts.shutdown();
						tts=null;

                        VR.unMute();

                        hideBubbles(_inProcess);
					}
					instance=null;
				}
				if (abortHandler!=null) abortHandler.run();
			   return;
			}
			if (!que.isEmpty()) {
		      Log.d(TAG, "!que.isEmpty()");
		      inProcess=que.remove(0);
		//      suspended=inProcess instanceof SuspendMessage;
		      _say(inProcess);
			  return;
			}
			inProcess=null;
			//tm.listen(psl, PhoneStateListener.LISTEN_NONE);
			if (tts!=null) try {
			  tts.stop();
			  tts.shutdown();
			  tts=null;

                VR.unMute();

                //	  if (!isControllingBubblesAlone(_inProcess))
			     hideBubbles(_inProcess);
			  
			} catch(Throwable t) {
				t.printStackTrace();
			}
			if (fAbort) {
				if (abortHandler!=null) abortHandler.run();
				return; 
			}
			instance=null;
		}
		Runnable comp=completion;
		completion=null;
		if (comp!=null) {
			comp.run();
		}
        VR.unMute();
	}
	
	TelephonyManager tm;
	
	public void initAndroidTts(
		final Context context, 
		final Object obj, 
		final String lang,
		final Runnable onLangNotAvailable
	) {
		  new AnTTS(context) {
				@Override
				protected void onInit() {
                    MyTTS.this.tts = this;
					if (!Utils.isEmpty(lang)) {
						   if (!setLanguage(lang)) {
							 if (useGoogleVoice(lang))
								 onLangNotAvailable.run();
							 else
								 onCompletion();
							 return;
							
						   }
					}
					 _say(obj);
				}	
				@Override
				protected void onAllCompleted() {
				   onCompletion();		  
				}			  
		  };	
	}
	
	private static int _voiceFromSettings = 0;

	public static void setVoice(int voice) {
       _voiceFromSettings = voice;
	}
	
	public static boolean usesSystemVoice() {
	   return _voiceFromSettings==SYSTEM_VOICE;
	}
	
	public static boolean usesFemaleVoice() {
	   return _voiceFromSettings==FEMALE_VOICE;
	}
	
	public static boolean usesMaleVoice() {
	   return _voiceFromSettings==MALE_VOICE;
    }
	
	public static int getVoice() {
		return _voiceFromSettings;
	}
	
	// { Alyona voice
	private Boolean _alyonaVoiceExpired=null;
	private static boolean _alyonaExpirationPhraseSaid=false;
	public  boolean isAlyonaVoiceExpired() {
		return false;
	}
	// } Alyona voice
	
	private MyTTS(final Context context,  final Object t) {

		  synchronized(MyTTS.class) {
			  instance=this;
		  }

		  IMyTtsInstance  mti=Wrapper.findInterface(t, IMyTtsInstance.class);		
	      if (mti!=null) mti.setMyTTS(this);	 
	      
		  final Object text=checkLang(t);
          inProcess=t;
          
          final String lang=getLangFromSpeakable(text); 
          
          Runnable lna=new Runnable() {
			@Override
			public void run() {
				Object mp=createPlayer(t, Utils.getString(text), lang);
				MyTTS.this._say(mp==null?text:mp);	
			}
          };
          
		  initAndroidTts(context,text,lang,lna); 
		 

    }

	public static void switchVoicesIfCan() {
		if (instance!=null&&instance.tts!=null) {
			instance.tts.switchVoice();
		}
	}

	private static String getLang(int c) {
		if ((c >  0x0041) &&  (c < 0x005a))	return "english";
		if ((c >  0x0061) &&  (c < 0x007a))	return "english";
		if ((c >  0x0400) &&  (c < 0x04ff))	return "russian";
		if ((c >  0x0590) &&  (c < 0x05ff))	return "hebrew";
			
		return null;
	}

	// returns null if detected language equals to settings
	// otherwise returns name of language detected: "russian", "english" or "hebrew"
	public static String detectLanguage(String string) {
		
		if (Utils.isEmpty(string))
			return null;
		
		// calculate frequency of each language
		HashMap<String, Integer> freq = new HashMap<String, Integer>();
		for(int i=0; i<string.length(); i++) {
			String lang = getLang(string.charAt(i));
			Integer f = freq.get(lang);
			if (f == null)
				freq.put(lang, 1);
			else
				freq.put(lang, ++f);
		}

		// find max
		int max = 0;
		String max_lang = null;
        
		for (Map.Entry<String,Integer> e : freq.entrySet())
		{
			if (e.getValue() > max) {
				max = e.getValue();
				max_lang = e.getKey();
			}
		}

		// if language equals to settings or not detected, return null
		if (Utils.isEmpty(max_lang) || max_lang.equals(App.self.getString(R.string.P_lang)))
			max_lang = null;
		
		return max_lang;
	}

}
