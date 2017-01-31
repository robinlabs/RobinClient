package com.magnifis.parking;

import static com.magnifis.parking.tts.MyTTS.speakText;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.magnifis.parking.toast.ToastController;
import com.magnifis.parking.tts.MyTTS;
import com.magnifis.parking.utils.Utils;
import com.robinlabs.utils.BaseUtils;

public class Output {
	final static String TAG=Output.class.getSimpleName();
	
	public static class Arg implements MyTTS.OnStringSpeakListener, MyTTS.IWrapper  {
	

		@Override
		public Object getWrapped() {
			Object o=toShow();
			return o==null?toSpeech():o;
		}

		public static Arg andShow(final Object o) {
			return new Arg() {

				@Override
				public Object toSpeech() {
					return o;
				}

				@Override
				public Object toShow() {
					return o;
				}
				
				@Override 
				public String toString() {
					return o.toString();
				}
				
			};
		}
		
		public static Arg andShow(final Object sh, final Object sp) {
			return new Arg() {

				@Override
				public Object toSpeech() {
					return sp;
				}

				@Override
				public Object toShow() {
					return sh;
				}
				
				@Override 
				public String toString() {
					return sp.toString();
				}
				
			};
		}
		
		public static Arg say(final Object o) {
			return new Arg() {

				@Override
				public Object toSpeech() {
					return o;
				}

			};
		}
		
		
	
		@Override
		public void onToSpeak() {
			Object w=getWrapped();
			if (w!=null) {
				MyTTS.OnStringSpeakListener osl=MyTTS.Wrapper.findInterface(w, MyTTS.OnStringSpeakListener.class);
				if (osl!=null) osl.onToSpeak();
			}	
		}

		public void onSaid(boolean fAborted) {
			Object w=getWrapped();
			if (w!=null) {
				MyTTS.OnSaidListener osl=MyTTS.Wrapper.findInterface(w, MyTTS.OnSaidListener.class);
				if (osl!=null) osl.onSaid(fAborted);
			}
		}
		
		public Object toSpeech() {
		  return null;	
		}
		
		public Object toShow() {
		  return null;	
		}	
		
		@Override
		public String toString() {
			Object o=toShow();
			return Utils.getString(o==null?toSpeech():o);
		}
	
	}

	private static ToastController toast = null;
	
	
	public static void sayAndShow(final Context act, Object s) {
		sayAndShow(act, s, false);
	}

	public static void sayAndShowFromGui(final Context act, final Object s,
			final boolean switchVoices) {
		Utils.runInMainUiThread(
		  act,
		  new Runnable() {
			@Override
			public void run() {
				sayAndShow(act, s, switchVoices);
			}
		});
	}

	public static void sayAndShowFromGui(final Context act, final Object sayAndShow, final Runnable runnable, final boolean andShow, final boolean switchVoices) {
		   sayAndShowFromGui(act, sayAndShow, "", runnable, andShow, switchVoices);
	}

	public static void sayAndShowFromGui(final Context act, final Object sayAndShow, final String query, final Runnable runnable, final boolean andShow, final boolean switchVoices) {
		   Utils.runInMainUiThread(
			  act,
			  new Runnable() {
				public void run() {
					String ss=(sayAndShow instanceof Integer)?(App.self.getString((Integer)sayAndShow)):sayAndShow.toString();
					if (runnable!=null) MyTTS.execAfterTheSpeech(runnable);
					if (andShow) {
						sayAndShow(act, ss, ss, query ,false);
					} else {
						MyTTS.speakText(sayAndShow);
					}
				}
			  }
		   );	
	}
	
	public static void sayAndShow(Context act, Object s, boolean switchVoices) {
		sayAndShow(act, s, null, switchVoices);
	}

	public static void sayAndShow(final Context act, final Object toShow,
			final Object toSpeak, final boolean switchVoices) {
		sayAndShow(act, toShow, toSpeak, "", switchVoices);
	}
	
	public static void sayOnlyOrSayAndShow(
		    Context act, 
			boolean switchVoices,
			String q,
			final MyTTS.OnSaidListener afterAll,
			Object ...args 
	) {
	  if (!BaseUtils.isEmpty(args)) {
		  //int li=args.length-1;
		  Object aa[]=new Object[args.length];
		  for (int i=0;i<args.length;i++) {
			  final Object x=args[i];
			  final boolean last=i==args.length-1;
			  aa[i]=new Arg() {

					@Override
					public void onSaid(boolean fAborted) {
						if (x instanceof MyTTS.OnSaidListener) ((MyTTS.OnSaidListener)x).onSaid(fAborted);
						if (fAborted||last)
						  afterAll.onSaid(fAborted);
					}

					@Override
					public Object toSpeech() {
						return (x instanceof Arg)?((Arg)x).toSpeech():x;
					}

					@Override
					public Object toShow() {
						return (x instanceof Arg)?((Arg)x).toShow():x;
					}
					
					@Override
					public Object getWrapped() {
						return x;
					}
					  
			};
		  }
		  sayOnlyOrSayAndShow(act,switchVoices,q,aa);
	  }	
	}
	
	public static void sayOnlyOrSayAndShow(
	    Context act, 
		final boolean switchVoices,
		final String q,
		Object ...args 
	) {
	  if (args!=null) for (Object o:args) {
		  if (o instanceof Arg) {
			 final Arg a=(Arg)o;
			 if (a.toShow()==null) 
				speakText(a);
			 else 
				sayAndShow(act,a,a.toSpeech(), q, switchVoices);
		  } else
			 sayAndShow(act,o,o,q,switchVoices);
	  }
	}	
	
	
	public static void sayAndShow(final Context act, final Object toShow,
			final Object toSpeak, final String query, final boolean switchVoices) {
		if (toShow == null)
			return;
		
		if (toast!=null) toast.abort();
		
		final Object ss = (toShow instanceof Integer) ? (App.self
				.getString((Integer) toShow)) : toShow;

		final String sp = 
		   toSpeak==null
		     ?ss.toString()
		     :((toSpeak instanceof Integer) ? (App.self
				.getString((Integer) toSpeak)) : toSpeak.toString());
		
		
		MyTTS.OnStringSpeakListener sw = new MyTTS.Wrapper(toShow) {	

			@Override
			public boolean isControllingBubblesAlone() {
				return true;
			}

			@Override
			public String toString() {
				return sp;
			}

			@Override
			public void onSaid(boolean fAborted) {
				Log.d(TAG,"onSaid -- "+fAborted);
				//SuziePopup.hideBubles(false, false);
				
				
				    Runnable r=MyTTS.Wrapper.findInterface(toShow, Runnable.class);

					if ((r!=null)&&!fAborted)
						r.run();
					else if (toShow instanceof MyTTS.OnSaidListener)
						((MyTTS.OnSaidListener) toShow).onSaid(fAborted);
			
			}

			@Override
			public void onToSpeak() {
				Log.d(TAG,"onToSpeak");
				MyTTS.showAnswerBubble(ss);

				if (switchVoices)
					MyTTS.switchVoicesIfCan();

				if (toShow instanceof MyTTS.OnStringSpeakListener)
					((MyTTS.OnStringSpeakListener) toShow).onToSpeak();
				
				
			}
		};
		speakText(sw);
	}	
    
 
	public static void show(final Context act, final Object s, final String query) {
		if (toast!=null) toast.abort();
		if (s!=null) {
		  final String ss = (s instanceof Integer) ? (App.self.getString((Integer) s)) : s.toString();
		  toast=new ToastController(act, ss, query, false);
		}
	}
	
	public static void sayWithVoiceSwitch(final Context act, final Object s) {
		if (s == null)
			return;
		final String ss = (s instanceof Integer) ? (App.self.getString((Integer) s)) : s.toString();
		MyTTS.OnStringSpeakListener sw = new MyTTS.OnStringSpeakListener() {
			@Override
			public String toString() {
				return ss;
			}

			@Override
			public void onSaid(boolean fAborted) {
				if (s instanceof Runnable)
					((Runnable) s).run();
			}

			@Override
			public void onToSpeak() {
					MyTTS.switchVoicesIfCan();
			}
		};
		speakText(sw);
	}
	
	public static void sayAndShow(final View v, final Drawable bg, final boolean switchVoices) {
		
		if (toast!=null) toast.abort();
		
		MyTTS.OnStringSpeakListener sw = new MyTTS.Wrapper(v) {
			ToastController tc = null;
			

			@Override
			public String toString() {
				return v.toString();
			}

			@Override
			public void onSaid(boolean fAborted) {
				Log.d(TAG,"onSaid -- "+fAborted);
				if (tc!=null) {
					tc.abort(); tc=null;
					if (v instanceof Runnable&&!fAborted)
						((Runnable) v).run();
					else if (v instanceof MyTTS.OnSaidListener)
						((MyTTS.OnSaidListener) v).onSaid(fAborted);
				}
			}

			@Override
			public void onToSpeak() {
				Log.d(TAG,"onToSpeak");
				if (tc==null) {
					if (switchVoices)
						MyTTS.switchVoicesIfCan();
					tc = new ToastController(v,null,bg);
					toast = tc;
					if (v instanceof MyTTS.OnStringSpeakListener)
						((MyTTS.OnStringSpeakListener) v).onToSpeak();
				
				}
			}
		};
		speakText(sw);
	}	
}
