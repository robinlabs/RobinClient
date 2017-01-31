package com.magnifis.parking.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.Service;
import android.app.PendingIntent.CanceledException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StatFs;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.magnifis.parking.App;
import com.magnifis.parking.Config;
import com.magnifis.parking.Log;
import com.magnifis.parking.MainActivity;
import com.magnifis.parking.MultiAsyncTask;
import com.magnifis.parking.ProximityWakeUp;
import com.magnifis.parking.R;
import com.magnifis.parking.RunningInActivity;
import com.robinlabs.utils.BaseUtils;

public class Utils extends BaseUtils {
	static final String TAG=Utils.class.getSimpleName();
	
	public static float convertDpToPixel(float dp){
	    Resources resources = App.self.getResources();
	    DisplayMetrics metrics = resources.getDisplayMetrics();
	    float px = dp * (metrics.densityDpi / 160f);
	    return px;
	}	
	/*
	 * @return "true" if was interrupted
	 */
	public static boolean sleep_wo_exception(long millis) {
		try {
			Thread.sleep(millis);
			return false;
		} catch (InterruptedException t) {}
		return true;
	}
	
	
	public static boolean isPlayStoreUrl(String u) {
		return !isEmpty(u)&&((u.startsWith("market")/*||u.contains("://play.google.com")*/));
	}
	
	
	public static Spannable styledString(CharSequence msg, int flags) {
		if (!isEmpty(msg)) {
			Spannable sp=new SpannableString(msg);
			sp.setSpan(new StyleSpan(flags), 0, msg.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            return sp;			
		}
		return null;
	}
	
	public static boolean anyBgBitmap(View v) {
		if (v!=null) {
		   Drawable d=v.getBackground();
		   return (d!=null)&&(d instanceof BitmapDrawable);
		}
		return false;
	}
	
	public static boolean isAppInstalled(String packageName) {
		PackageManager pm = App.self.getPackageManager();
		boolean isInstalled = false;
		try {
			pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
			isInstalled = true;
		} catch (PackageManager.NameNotFoundException e) {
			isInstalled = false;
		}
		
		return isInstalled;
	}
	
	public static boolean doesTopActivityHandleIntent(String intentAction) {
		return doesActivityHandleIntent(Utils.getTopActivity().getPackageName(),intentAction);
	}
	
	public static boolean doesActivityHandleIntent(ComponentName cn, String intentAction) {
		return cn==null?false:doesActivityHandleIntent(cn.getPackageName(),intentAction);
	}
	
	public static boolean doesActivityHandleIntent(String packageName, String intentAction) {
		if (!isEmpty(packageName)) {
			PackageManager pm = App.self.getPackageManager();

			List<ResolveInfo> rsi = pm.queryIntentActivities(new Intent(intentAction), 0);

			if (rsi == null) return false;

			if (rsi.size() < 1)
				return false;

			for (ResolveInfo ri : rsi) {
				if (ri.activityInfo.name!=null 
						&& ri.activityInfo.name.contains(packageName))
					return true;
			}
		}
		return false;
	}
	
	public static boolean startActivityFromNowhere(Intent it) {
		PendingIntent pi = PendingIntent.getActivity(App.self, 0, it, PendingIntent.FLAG_UPDATE_CURRENT);
		try {
			pi.send(); 
			return true;
		} catch (CanceledException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static boolean isService(ComponentName cn) {
	   if (cn!=null) {
		  String clsn=cn.getClassName();
		  if (!isEmpty(clsn)) {
		    Class c=Utils.tryToLoad(clsn);
		    return c!=null&&isSubclassOf(c,Service.class);
		  }
	   }
	   return false;
	}
	
	public static boolean isSubclassOf(Class c, Class base) {
		try {
		  c.asSubclass(base);
		  return true;
		} catch(Throwable t) {}
		return false;
	}
	
	public static boolean isBooleanPrefNotSet(String key) {
		SharedPreferences p=App.self.getPrefs();
		return p.getBoolean(key, true)&&!p.getBoolean(key, false);
	}
	
	public static boolean isBooleanPrefSet(String key) {
		SharedPreferences p=App.self.getPrefs();
		boolean a=!p.getBoolean(key, true), b=p.getBoolean(key, false);
		return a||b;
	}
	

	static public CharSequence firstUpper(CharSequence s, boolean addQuotes) {
		if (isEmpty(s)) return s;
		
		if (s instanceof Spanned) {
			SpannableStringBuilder sb=new SpannableStringBuilder(s);
			sb.replace(0, 1, Character.toString(Character.toUpperCase(s.charAt(0))));
			if (addQuotes) {
			  sb.insert(0, "\u201c");
			  sb.insert(sb.length(), "\u201d");
			}
			return sb;
		} else {
			StringBuilder sb=new StringBuilder();
			if (addQuotes)
				sb.append('\u201c'); // start quote 

			sb.append(Character.toUpperCase(s.charAt(0)));
			if (s.length()>1) sb.append(s.subSequence(1,s.length()));

			if (addQuotes)
				sb.append('\u201d'); // end quote 

			return sb;
		}
	}
	
	public static  String getString(Object o, String ...macros) {
		if (o==null) return null;
		if (o instanceof Integer) return expandMacros(App.self.getString((Integer)o),macros);
		return expandMacros(o.toString(),macros);
	}
	
	public static String expandMacros(String s,String ...macros) {
	   if (macros!=null&&macros.length>=2) for (int i=0;i<macros.length;i+=2) {
		  s=simpleReplaceAll(s,"${"+macros[i]+"}",macros[i+1]).toString();
	   }
	   return s;
	}
	
	
	public static MultiAsyncTask runInBgThread(final Runnable r) {
	  return runInBgThread(r,null);
	}
	
	public static MultiAsyncTask runInBgThread(final Runnable r, final IAfterThatHandler afterThat) {
	  if (inGuiThread()) {
		 return new MultiAsyncTask<Object,Object,Throwable>() {

			@Override
			protected Throwable doInBackground(Object... params) {
				try {
				  r.run();
				} catch(Throwable t) {
				  return t;
				}
				return null;
			}

		
			@Override
			protected void onCancelled() {
				if (afterThat!=null) afterThat.onAfterThat(null);
			}

			@Override
			protected void onPostExecute(Throwable result) {
				super.onPostExecute(result);
				if (afterThat!=null) afterThat.onAfterThat(result);
			}
			
			
			 
		 }.multiExecute();
	  } else {
		 Throwable t=null;
		 try {
		   r.run();
		 } catch(Throwable t0) { t=t0;  }
		 if (afterThat!=null) afterThat.onAfterThat(t);
	  }
	  return null;
	}
	
	
	public static boolean inGuiThread() {
		return Looper.myLooper()!=null;
	}
	
	public static boolean inMainGuiThread() {
		Looper ml=Looper.myLooper();
		return ml!=null&&ml==Looper.getMainLooper();
	}
	

	static public byte [] obj2bytes(Object obj) {
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		try {
			ObjectOutputStream oo=new ObjectOutputStream(baos);
			oo.writeObject(obj);
			oo.flush();
		} catch (IOException e) {
           Log.e(Utils.class.getCanonicalName(), " -- ",e);
		}
		return baos.toByteArray();
	}
	
	@SuppressWarnings("unchecked")
	static public <T> T bytes2obj(byte bytes[]) {
		ByteArrayInputStream bais=new ByteArrayInputStream(bytes);
		try {
			ObjectInputStream oo=new ObjectInputStream(bais);
			return (T)oo.readObject();
		} catch (Throwable t) {
           Log.e(Utils.class.getCanonicalName(), " -- ",t);
		}
		return null;
	}	
	
	
	public static Field getInternalAndroidResFld(String rClass,String rName) {
		try {
			Class arC = Class.forName("com.android.internal.R$"+rClass);
			if (arC!=null) {
				Log.d(TAG, arC.toString()+" found ");
				
				return arC.getField(rName);
			}

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static int [] getInternalAndroidResIds(String rClass,String rName) {
		try {
			Field f=getInternalAndroidResFld(rClass,rName);
			if (f!=null) return (int[])f.get(null);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static Integer getInternalAndroidResId(String rClass,String rName) {
		try {
			Field f=getInternalAndroidResFld(rClass,rName);
			if (f!=null) return f.getInt(null);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static Throwable runInGuiAndWait(Context ac,final Runnable r) {
	   final Throwable rv[]={null};
	   if (inGuiThread()) {
		   try {
			   r.run();
		   } catch(Throwable t) {
			   rv[0]=t; 
		   }  
	   } else if (ac!=null) {
		   final boolean done[]={false};
		   final Object so=new Object();
		   
		   Runnable rr=			  
		   new Runnable() {

				@Override
				public void run() {
				  try {
				  	r.run();
				  } catch(Throwable t) {
					 rv[0]=t; 
				  }
				  synchronized(so) {
					done[0]=true;
					so.notify(); 
				  }
				}		 
		   };
		   
		   runInMainUiThread(ac,rr);

		   synchronized(so) {
			   try {
				   if (!done[0]) so.wait();
			   } catch (InterruptedException e) {
			   } 
		   }	
	   }
					  
	   return rv[0];
	}
	
	public static void runInMainUiThread(Context ctx, Runnable r) {
		if (ctx!=null&&ctx instanceof Activity) 
			((Activity)ctx).runOnUiThread(r);
	    else
	    	runInMainUiThread(r);
	}
	
	// warning: returns when runnable finished !!!
	public static void runInMainUiThread(Runnable r) {
        if (inMainGuiThread())
          r.run();
        else
		  new Handler(Looper.getMainLooper()).post(r);
	}

	public static Throwable runInGuiAndWait(Runnable r) {
	   return runInGuiAndWait((Handler)null,r);
	}
	
	public static Throwable runInGuiAndWait(Handler h, final Runnable r) {
		   final Throwable rv[]={null};
		   if (inGuiThread()) {
			   try {
				   r.run();
			   } catch(Throwable t) {
				   rv[0]=t; 
			   }  
		   } else {
			   final boolean customLooper=h==null;
			   if (customLooper) {
				  Looper.prepare();
				  h=new Handler();
			   }
			   final boolean done[]={false};
			   final Object so=new Object();
			   h.post(
					   new Runnable() {

							@Override
							public void run() {
							  try {
							  	r.run();
							  } catch(Throwable t) {
								 rv[0]=t; 
							  }
							  synchronized(so) {
								done[0]=true;
								if (customLooper)
								  Looper.myLooper().quit();
								else
								  so.notify(); 
							  }
							}		 
					   }				   
			   );

			   synchronized(so) {
				   try {
					   if (!done[0]) {
						   if (customLooper)
							  Looper.loop();
						   else
						      so.wait();
					   }
				   } catch (InterruptedException e) {
				   } 
			   }	
		   }
						  
		   return rv[0];
		}
	
	
	public static Throwable runInGuiAndWait(View v, final Runnable r) {
		final Throwable rv[]={null};
		if (inGuiThread()) {
			try {
				r.run();
			} catch(Throwable t) {
				rv[0]=t; 
			}  
		} else {
			final boolean done[]={false};
			final Object so=new Object();
			v.post(
					new Runnable() {

						@Override
						public void run() {
							try {
								r.run();
							} catch(Throwable t) {
								rv[0]=t; 
							}
							synchronized(so) {
								done[0]=true;
								so.notify(); 
							}
						}		 
					}				   
					);

			synchronized(so) {
				try {
					if (!done[0]) {
						so.wait();
					}
				} catch (InterruptedException e) {
				} 
			}	

		}  
		return rv[0];
	}
	
	
	
	public static BitmapDrawable getImgResourceDrawable(String path) {
		if (isEmpty(path)) return null;
		try  {
		  return  new BitmapDrawable(App.self.getResources(),Utils.class.getResource(path).openStream());
		} catch(Throwable t) {
		  t.printStackTrace();
		}
		return null;
	}
	
	public static Bitmap getImgResource(String path) {
		if (isEmpty(path)) return null;
		try  {
		  return BitmapFactory.decodeStream(Utils.class.getResource(path).openStream());
		} catch(Throwable t) {
		  t.printStackTrace();
		}
		return null;
	}
	
	public static String getRawText(Context ctx, int id) {
		   InputStream raw = ctx.getResources().openRawResource(id);

		    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

		    try {
		        int i = raw.read();
		        while (i != -1) {
		            byteArrayOutputStream.write(i);
		            i = raw.read();
		        }
		        raw.close();
		    } catch (IOException e) {
		        e.printStackTrace();
		    }


		    try {
				return byteArrayOutputStream.toString("UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		    return null;
	}
	
	
	
	public static boolean isExternalStorageAvailable() {
		String state = Environment.getExternalStorageState();
		Log.v(TAG, "storage state is " + state);
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			File esd=Environment.getExternalStorageDirectory(); 
			return esd.canWrite(); 

		}
		return false;
	}
	
	public static String externalStorageAvailableString() {
	   if (isExternalStorageAvailable()) {
		   StatFs st=new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
		   double mbsz=(st.getFreeBlocks()/1024.)*(st.getBlockSize()/1024.) ;
           return String.format("%.2f mb", mbsz);
	   }
	   return null;
	}
	
    public static boolean validateZipFile(File file) {
        if (!file.exists()) return false;
        try {
        	ZipFile zip = new ZipFile(file);
        	try {
        		Enumeration<? extends ZipEntry> ens=zip.entries();
        		CRC32 crc = new CRC32();
        		byte buf[]=new byte[4096];
        		while (ens.hasMoreElements()) {
        			ZipEntry ze=ens.nextElement();
        			if (!ze.isDirectory()) {
        				long ecrc=ze.getCrc();
        				if (ecrc==-1) continue;
        				crc.reset();
        				InputStream is=zip.getInputStream(ze);
        				try {
        					for (;;) {
        						int sz=is.read(buf);
        						if (sz==0) break;
        						crc.update(buf, 0, sz);
        					}
        				} finally {
        					is.close();
        				}
        				if (crc.getValue() != ecrc) {
        					Log.e(TAG, "CRC does not match for entry: "
        							+ ze.getName());
        					Log.e(TAG, "In file: " + file.getCanonicalPath());
        					return false;
        				}          
        			}
        		}
        	} finally {
        		zip.close();
        	}
        } catch(Throwable t) {}
        return true;
    }

    static public AlertDialog askYesOrContinue(
            Activity act,Object t,Object m, Object continueLabel,
            final Runnable onYes,
            final Runnable onContinue) {

        Context ctx = contextTheme(act);

        AlertDialog.Builder adb=prepareConfirmation(
                ctx,t,m
        );
        adb.setPositiveButton(
                android.R.string.yes,
                new AlertDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onYes.run();
                    }
                }
        );
        adb.setNegativeButton(
                getString(continueLabel),
                new AlertDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }
        );
        AlertDialog dlg=adb.create();

        dlg.show();
        dlg.setOnCancelListener(
                new OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        onContinue.run();
                    }
                }
        );

        return dlg;

    }


    static public AlertDialog askYesOrExit(
		final Activity ctx,Object t,Object m,
		final Runnable onYes
	) {
		AlertDialog.Builder adb=prepareConfirmation(
				ctx,t,m
		);
		adb.setPositiveButton(
		  android.R.string.yes,
	         new AlertDialog.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
				   onYes.run();
				}
	         }		
		);
		adb.setNegativeButton(
		    "Exit",
			new AlertDialog.OnClickListener() {
			   @Override
			   public void onClick(DialogInterface dialog, int which) {
				   dialog.cancel();   
			   }
		    }
		);
		AlertDialog dlg=adb.create();
	
	    dlg.show();

        dlg.setOnCancelListener(
         new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
                ProximityWakeUp.reset();
				//ProximityWakeUp.stop(ctx);
				ctx.finish();
			}
         }
       );
        
        return dlg;
		
	}
	
	static public AlertDialog.Builder prepareConfirmation(Context ctx,Object t,Object m) {
		AlertDialog.Builder adb=new AlertDialog.Builder(ctx)
        .setIcon(android.R.drawable.ic_menu_info_details)
        .setNegativeButton(android.R.string.no, 
                new OnClickListener() {
    				public void onClick(DialogInterface arg0, int arg1) {
    					arg0.cancel();
    				}  
                }              		  
        );
        
		if (t!=null) {
			  if (t instanceof String) adb.setTitle((String)t); else adb.setTitle((Integer)t);
		} 
			  else adb.setTitle(android.R.string.dialog_alert_title);
		
		 if (m!=null) {
			 if (m instanceof String) adb.setMessage(m.toString()); else
				 if (m instanceof Integer) adb.setMessage((Integer)m); else
					 if (m instanceof View) adb.setView((View)m);
		 }
		return adb;
	}
	
	static public AlertDialog askConfirmation(
			Context ctx,Object t,Object m, OnClickListener onPositiveButton
	 ) {
        ctx = contextTheme(ctx);
		AlertDialog.Builder adb= prepareConfirmation(ctx,t,m);
		adb.setPositiveButton(android.R.string.yes,onPositiveButton);
		AlertDialog dlg=adb.create();
		
	    dlg.show();
	    return dlg;
	}

    public static Context contextTheme(Context context) {
        if (android.os.Build.VERSION.SDK_INT > 10) {
            context = new ContextThemeWrapper(context, android.R.style.Theme_Holo_Light_Dialog);
        }
        return context;
    }

	
	static public AlertDialog showAlert(Context ctx,Object t,Object m) {
		return showAlert(ctx,t,m,android.R.drawable.ic_dialog_info,android.R.string.ok);
	}
	
	static public AlertDialog showAlert(Context ctx,Object t,Object m,Integer icId,Integer btnTitle) {
		
		
		AlertDialog.Builder adb=new AlertDialog.Builder(ctx)
          .setNeutralButton(btnTitle, 
        	 new OnClickListener() {
				public void onClick(DialogInterface arg0, int arg1) {
					arg0.cancel();
				}  
            }
           );
		
		if (icId!=null) adb.setIcon(android.R.drawable.ic_dialog_info);
		if (m!=null) {
		  if (m instanceof String) adb.setMessage(m.toString()); else
		  if (m instanceof Integer) adb.setMessage((Integer)m); else
	      if (m instanceof View) adb.setView((View)m);
		}
		
		
		if (t!=null) {
		  if (t instanceof String) adb.setTitle((String)t); else adb.setTitle((Integer)t);
		} 
		  else adb.setTitle(android.R.string.dialog_alert_title);
		
		
		AlertDialog dlg=adb.create();
		
	    dlg.show();
	    return dlg;
	}	

	

	public static SpannableString underline(String name) {
	  SpannableString nameUnderline = new SpannableString(name);
	  nameUnderline.setSpan(new UnderlineSpan(), 0, nameUnderline.length(), 0);
	  return nameUnderline;
	}

	
	static public String md5(String s) { 
		return md5(s,"ISO8859_1");
	}
	
	
	
	static public byte[] rawMd5(String s) {  
       return rawMd5(s,"UTF-8");
	}
    static public byte[] rawMd5(String s,String enc) {  
		try {  
			// Create MD5 Hash  
			MessageDigest digest = java.security.MessageDigest.getInstance("MD5");  
			digest.update(s.getBytes(enc));  
 			
			return digest.digest();

		} catch (Throwable e) {  
           Log.e(TAG, " -- ", e);
		}  
		return null;  
	}

	
	static public String md5(String s,String enc) {  
		try {  
			return hexDec(rawMd5(s,enc));
		} catch (Throwable e) {  
           Log.e(TAG, " -- ", e);
		}  
		return null;  
	}
	
	static public String md5(File f) {
	  if (f!=null&&f.exists()) try {
		 FileInputStream fis=new FileInputStream(f);
		 MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
		 byte buf[]=new byte[4096]; boolean any=false;
		 while (fis.available()>0) {
		   int sz=fis.read(buf);
		   if (sz<=0) break; any=true;
		   digest.update(buf, 0, sz);
		 }
		 if (any) return hexDec(digest.digest());
		 fis.close();
	  } catch(Throwable t) {
		Log.e(TAG, " -- ",t);
	  }
	  return null;
	}	
	
  
  public static void removeSubviews(ViewGroup vg, Class c) {
	 ArrayList<View> a=new ArrayList<View>();
	 for (int i=0;i<vg.getChildCount();i++) {
		View v=vg.getChildAt(i);
		if (v.getClass().equals(c)) a.add(v);
	 }
	 for (View v:a) vg.removeView(v);
  }
  public static void setOrHide(TextView tv, CharSequence t) {
	 if (isEmpty(t)) {
		tv.setVisibility(View.GONE);
	 } else {
		tv.setText(t);
		tv.setVisibility(View.VISIBLE); 
	 }
  }
  
	public static void hideSomething(final View v, final Runnable callback) {
		/*
		Animation a = AnimationUtils.loadAnimation(v.getContext(),
				R.anim.hide_something);
				*/
		Animation a = AnimationUtils.makeOutAnimation(v.getContext(), true);
		a.reset();
		a.setAnimationListener(new AnimationListener() {
			public void onAnimationEnd(Animation arg0) {
				v.setVisibility(View.GONE); 
				if (callback!=null) callback.run();
			}

			public void onAnimationRepeat(Animation arg0) {
			}

			public void onAnimationStart(Animation arg0) {
			}
		});
		v.startAnimation(a);	
	}
	
	public static void hideSomething(View v) {
		 hideSomething(v,null);
	}
	
	public static void showSomething(View v) {
		showSomething(v,null);
	}
	
	public static void showSomething(final View v, final Runnable callback) {
		/*
		Animation a = AnimationUtils.loadAnimation(v.getContext(),
				R.anim.show_something);*/
		
		Animation a=AnimationUtils.makeInAnimation(v.getContext(), false);
		a.reset();
		
		a.setAnimationListener(new AnimationListener() {
			public void onAnimationEnd(Animation arg0) {
				if (callback!=null) callback.run();
			}

			public void onAnimationRepeat(Animation arg0) {
			}

			public void onAnimationStart(Animation arg0) {
			//	v.setVisibility(View.VISIBLE);
			}
		});
		v.startAnimation(a);

	}	
	
	final public static boolean isArmPlatform=android.os.Build.CPU_ABI.toLowerCase().startsWith("arm");
    final public static boolean isAndroid5orAbove= android.os.Build.VERSION.SDK_INT >= 21;
	final public static boolean isAndroid4orAbove= android.os.Build.VERSION.SDK_INT >= 14;
	final public static boolean isAndroid41orAbove= android.os.Build.VERSION.SDK_INT >= 16;
	final public static boolean isAndroid23orAbove= android.os.Build.VERSION.SDK_INT >= 9;
	final public static boolean isAndroid22orAbove= android.os.Build.VERSION.SDK_INT >= 8;
	final public static boolean isAndroid233orAbove= android.os.Build.VERSION.SDK_INT >= 10;
	final public static boolean isAndroid3orAbove= android.os.Build.VERSION.SDK_INT >= 11;
	final public static boolean isAndroid6orAbove= android.os.Build.VERSION.SDK_INT >= 23;
	
	public static boolean isAmong(Date d, Pair<Date,Date> fromTo) {
		return isAmong(d,fromTo.first,fromTo.second);
	}
	
	public static boolean isAmong(Date d, Date from, Date to) {
	   return (from.before(d)||from.equals(d))&&(to.after(d)||to.equals(d));
	}
	
	public static long daysBetween(Date startDate, Date endDate) { 
	   Calendar cal1=Calendar.getInstance(),cal2=Calendar.getInstance();
	   cal1.setTime(startDate);
	   cal2.setTime(endDate);
	   return daysBetween(cal1,cal2);
	}
	
	public static Pair<Date,Date> dayStartEnd(Date event) {
		return dayStartEnd(event.getTime());
	}
	
	public static Pair<Date,Date> dayStartEnd(long eventTime) {
		Calendar cal=Calendar.getInstance();
		cal.setTimeInMillis(eventTime);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date dayBegin=cal.getTime();
		
		cal.add(Calendar.DAY_OF_MONTH, 1);
		cal.add(Calendar.SECOND,-1);
		Date dayEnd=cal.getTime();
		
		return new Pair<Date,Date>(dayBegin,dayEnd);
	}
	
	
      
    public static Integer indexOf(Cursor cc, String colname) {
    	int ix=cc.getColumnIndex(colname);
    	if (ix>=0) return ix;
    	return null;
    } 
    
  
    
    public static CharSequence formatMessageDate(Date dat) {
    	return formatMessageDate(dat, true);
    }
    
    
    public static boolean isToday(Date d) {
       return isAmong(d,dayStartEnd(System.currentTimeMillis()));
    }
    
    public static CharSequence formatMessageDate(Date dat, boolean withTime) {
    	if (dat==null) return null;
    	
    	Calendar cal = new GregorianCalendar();
		cal.setTimeInMillis(System.currentTimeMillis());

		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		Date todayStart=new Date(cal.getTime().getTime());
		
		Calendar todayCal=(Calendar) cal.clone();
		
		
		
		cal.add(Calendar.DAY_OF_MONTH, -1);
		Date yesterdayStart=new Date(cal.getTime().getTime());
		
		
		cal=(Calendar)todayCal.clone();
		cal.add(Calendar.DAY_OF_MONTH, 1);
		Date tomorrowStart=new Date(cal.getTime().getTime());		
		
		cal.add(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.MILLISECOND, -1);
		Date tomorrowEnd=new Date(cal.getTime().getTime());
		
		Log.d(TAG," tdd "+cal.getTime().toLocaleString());
		
		boolean 
	  	  today=(dat.after(todayStart)||dat.equals(todayStart))&&dat.before(tomorrowStart),
		  tomorrow=((dat.after(tomorrowStart)||dat.equals(tomorrowStart)))&&dat.before(tomorrowEnd),
		  yesterday=(dat.after(yesterdayStart)||dat.equals(yesterdayStart))&&dat.before(todayStart);
				
		StringBuilder sb=new StringBuilder();
		
		if (today) sb.append(App.self.getString(R.string.utils_today) + " "); else
	    if (yesterday) sb.append(App.self.getString(R.string.utils_yesterday) + " "); else
        if (tomorrow)  sb.append(App.self.getString(R.string.utils_tomorrow) + " "); else
		{
			sb.append(new SimpleDateFormat("dd/MM/yyyy ").format(dat));
		}
		
		if (withTime) {
		  sb.append(' ');
		  sb.append(App.self.getString(R.string.utils_at));
		  sb.append(' ');
		  
		  
		  cal.setTime(dat);
		  sb.append(new SimpleDateFormat(cal.get(Calendar.MINUTE)==0?"K a":"K m a").format(dat));

		}
		
		return sb;
    }
    
    public static boolean isEmpty(Bundle bl) {
    	return bl==null||bl.isEmpty();
    }
    
    public static void dump(String tag,Collection c) {
       Log.d(tag, dump(c).toString());
    }
    
    public static void dump(String tag,Intent it) {
      dump(tag, it.getExtras());
    }
    
    public static void dump(String tag, Bundle bl) {
        if (bl!=null) for (String key:bl.keySet()) {
      	Object o=bl.get(key);
          Log.d(tag,key+"="+(o==null?"null":o.toString()));
        }
    }
    
	public static ViewGroup.LayoutParams cloneLayoutParams(ViewGroup.LayoutParams layoutParams) {
		if (layoutParams!=null) {
			// clone layoutParams
		    //Class c=layoutParams.getClass();
		    try {
		    	Class c=layoutParams.getClass();
		    	Constructor cs=null;
		    	if (layoutParams instanceof ViewGroup.MarginLayoutParams)
				    cs=c.getConstructor(ViewGroup.MarginLayoutParams.class);
		    	else
				    cs=c.getConstructor(ViewGroup.LayoutParams.class);
		    	
				ViewGroup.LayoutParams lp=(ViewGroup.LayoutParams)cs.newInstance(layoutParams);
				if (layoutParams instanceof RelativeLayout.LayoutParams) {
					RelativeLayout.LayoutParams lp0=(RelativeLayout.LayoutParams)layoutParams,
					  lp1=(RelativeLayout.LayoutParams)lp;
					int rules[]=lp0.getRules();
					if (!isEmpty(rules)) for (int i=0;i<rules.length;i++) lp1.addRule(i,rules[i]);
					lp1.alignWithParent=lp0.alignWithParent;
				} 
				Log.d(TAG, lp.toString());
				return lp;
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}
	
	
	public static int getTopTaskId(Context context) {
		ActivityManager am=(ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null)
            return -1;
		List<ActivityManager.RunningTaskInfo> tti=am.getRunningTasks(1);
		if (!isEmpty(tti)) {
			Log.d(TAG,"!empty");
			return tti.get(0).id;
		}
		return -1;
	}
	
	public static ActivityManager.RecentTaskInfo getTopTask() {
		ActivityManager.RecentTaskInfo rti=null;
		ActivityManager am=App.self.getActivityManager();
        if (am == null)
            return null;
		List<ActivityManager.RecentTaskInfo> tti=am.getRecentTasks(3, ActivityManager.RECENT_WITH_EXCLUDED);
		if (tti!=null) for (int i=0;i<tti.size();i++) {
		   ActivityManager.RecentTaskInfo ti=tti.get(i);
		   if (ti!=null) {
			   ComponentName cn=ti.origActivity;
			   rti = ti;
			   if (cn==null&&ti.baseIntent!=null) {
				   cn=ti.baseIntent.getComponent();
				   rti.origActivity=cn;
			   }
			   if (cn!=null) {
				   if (App.class.getPackage().getName().equals(cn.getPackageName())) {
					   if (MainActivity.class.getCanonicalName().equals(cn.getClassName())) break;
					   continue;
				   }
			   }
			   break; 
		   }
		}
		return rti;
	}
		
	public static ComponentName getTopActivity() {
		ComponentName topActivity = null;
		ActivityManager am=App.self.getActivityManager();
        if (am == null)
            return null;
		List<ActivityManager.RunningTaskInfo> tti=am.getRunningTasks(1);
		if (tti!=null) for (int i=0;i<tti.size();i++) {
		   ActivityManager.RunningTaskInfo ti=tti.get(i);
		   if (ti != null && ti.topActivity != null) {
			   topActivity = ti.topActivity;
			   break; 
		   }
		}
		
		return topActivity; 
	}
	
	public static Integer getRobinTaskIndex(Context context, int maxTopTasks) {
		ActivityManager am=App.self.getActivityManager();
        if (am == null)
            return null;
		List<ActivityManager.RunningTaskInfo> tti=am.getRunningTasks(maxTopTasks);
		if (tti!=null) for (int i=0;i<tti.size();i++) {
			ActivityManager.RunningTaskInfo ti=tti.get(i);
			ComponentName cn=ti.baseActivity;
			if (cn!=null) {
				if (MainActivity.class.getCanonicalName().equals(cn.getClassName()))
					return i;
			}
		}
		return null;
	}
	
	static public boolean isForegroundPackage() {
		return isForegroundPackage(App.class.getPackage().getName());
	}
	
	static public boolean isForegroundPackage(String pkName) {
		return isInForeground0(App.self, pkName, true)/*||isInForeground1(App.self, pkName, true)*/; 
	}
	
	static public boolean isForegroundActivity(Context service, String activityName) {
		return isInForeground0(service, activityName, false)/*||isInForeground1(service, activityName, false)*/; 
	}
	
	static private boolean isInForeground0(Context context, String activityName, boolean fPkOnly) {	
		ActivityManager am=App.self.getActivityManager();
        if (am == null)
            return false;
		List<ActivityManager.RunningTaskInfo> tti=am.getRunningTasks(1);
		if (tti!=null) for (int i=0;i<tti.size();i++) {
			ActivityManager.RunningTaskInfo ti=tti.get(i);
			ComponentName cn=ti.baseActivity;
			if (cn!=null) {
				if (fPkOnly) {
					  if (activityName.equals(cn.getPackageName()))
							return true;
				} else
				  if (activityName.equals(cn.getClassName()))
					return true;
			}
		}
		return false;
	}
	
	static private boolean isInForeground1(Context context, String activityName, boolean fPkOnly) {	
		ActivityManager am=App.self.getActivityManager();
        if (am == null)
            return false;
		List<ActivityManager.RecentTaskInfo> tti=am.getRecentTasks(1, 0);
		if (tti!=null) for (int i=0;i<tti.size();i++) {
			ActivityManager.RecentTaskInfo ti=tti.get(i);
			ComponentName cn=ti.baseIntent.getComponent();
			if (cn!=null) {
				if (fPkOnly) {
					  if (activityName.equals(cn.getPackageName()))
							return true;
				} else
				  if (activityName.equals(cn.getClassName()))
					return true;
			}
		}
		return false;
	}
	
	public static String getForegroundPackage() {
		ActivityManager am=App.self.getActivityManager();
        if (am == null)
            return null;
		List<ActivityManager.RunningTaskInfo> tti=am.getRunningTasks(1);
		if (tti == null) 
			return null;
		for (int i=0;i<tti.size();i++) {
			ActivityManager.RunningTaskInfo ti=tti.get(i);
			if (ti.topActivity != null)
				return ti.topActivity.getPackageName();
		}
		return null;
	}
	
	public static boolean isHomePackage(String packageName) {
		if (isEmpty(packageName))
			return false;
		
		PackageManager pm = App.self.getPackageManager();

		Intent it = new Intent(Intent.ACTION_MAIN);
		it.addCategory(Intent.CATEGORY_HOME);
		List<ResolveInfo> ss=pm.queryIntentActivities(it, PackageManager.GET_ACTIVITIES);
        if ((ss != null) && (ss.size() > 0)) {
        	for (ResolveInfo ri:ss)
        		if (!isEmpty(ri.activityInfo.packageName) && ri.activityInfo.packageName.equals(packageName))
        			return true;
        }
		
		return false;
	}

	public static boolean isMyPackage(String packageName) {
		if (isEmpty(packageName))
			return false;

		return packageName.startsWith(App.self.getPackageName());
	}

    public static CharSequence dump(Cursor cr) {
        StringBuilder sb=new StringBuilder();
        int count=cr.getCount();
        if (count>0&&cr.moveToFirst()) {
     	   sb.append("----------------\ncount="+count);
     	   sb.append('\n');
     	   do {
     		  for (int i=0;i<cr.getColumnCount();i++) {
     			  sb.append(cr.getColumnName(i));
     			  sb.append('=');
     			  sb.append(cr.isNull(i)?"null":cr.getString(i));
     			  sb.append('\n');
     		  }
     		  sb.append(">>>end record--------------------\n");
            } while (cr.moveToNext());
        };
        sb.append(">>>end dump--------------------\n");
        return sb;
     }
    
	public interface TextInputDialogResult {
		public void onDialogOK(String result);
		public void onDialogClose();
	};    	

	static public void showTextInputDialog(Context ctx, final Object title, final Object defValue, final TextInputDialogResult result) {	
	  new RunningInActivity(ctx) {
		  @Override
		  public void run() {
              try {
                  String t = "";
                  if (title != null)
                      if (title instanceof String)
                          t = (String) title;
                      else
                          t = App.self.getString((Integer) title);

                  AlertDialog.Builder adb = prepareConfirmation(activity, t, null);

                  final EditText tv = new EditText(activity);

                  if (defValue != null)
                      if (defValue instanceof String)
                          tv.setText((String) defValue);
                      else
                          tv.setText(App.self.getString((Integer) defValue));

                  adb.setPositiveButton(
                          android.R.string.yes,
                          new DialogInterface.OnClickListener() {

                              @Override
                              public void onClick(final DialogInterface dialog, int which) {

                                  String s = tv.getText().toString();

                                  if (!isEmpty(s) && !isEmpty(s = s.trim())) {
                                      result.onDialogOK(s);
                                  }
                              }

                          }
                  );
                  adb.setView(tv);

                  final AlertDialog optionalDialog = adb.create();

                  optionalDialog.setOnDismissListener(
                          new OnDismissListener() {

                              @Override
                              public void onDismiss(DialogInterface arg0) {
                                  result.onDialogClose();
                                  if (usingProxyActivity) activity.finish();
                              }

                          }
                  );

                  optionalDialog.show();
              } catch (Exception e) { e.printStackTrace(); }
		  }
	  };
	}

	public static String readFileFromUrl(String jsFuncUrl) {

		String contents = "";
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new URL(jsFuncUrl).openStream())); 

			String line;
			while ((line = br.readLine()) != null) {
				contents += line;
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
        }

        return contents;
    }
	
	public static StringBuilder getTextFromStream(InputStream is) {
		StringBuilder content = null;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(is)); 
			String line;
			while ((line = br.readLine()) != null) {
				if (content==null) 
				  content=new StringBuilder(line);
				else {
				  content.append(line);
				}
				content.append('\n');
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
        }
        return content;
    }

    // update file user dictionary on sd card
    public static void updateUserDictionary() {

        // storage/sdcard0/.MagnifisRobin/acattsandroid/voices/russian-alyona-lf-22khz/rur/NLP
        updateExternalFile("ru/user.userdico", "/.MagnifisRobin/acattsandroid/voices/russian-alyona-lf-22khz/rur/NLP/user.userdico");
        updateExternalFile("en/user.userdico", "/.MagnifisRobin/acattsandroid/voices/usenglish-heather-lf-22khz/enu/NLP/user.userdico");

    }

    // update file on sd card
    // src - path in assets
    // dst - path on sd card
    public static void updateExternalFile(String src, String dst){
        AssetManager assetManager = App.self.getAssets();
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(src);
            String dst2 = Environment.getExternalStorageDirectory() + java.io.File.separator + dst;
            File outFile = new File(dst2);
            Utils.createFolderForFile(outFile);
            if (outFile.exists()) {
                if (outFile.length() != in.available()) {
                    out = new FileOutputStream(outFile);
                    copyFile(in, out);
                    out.flush();
                    out.close();
                }
            }
            else {
                //outFile.mkdirs();
                outFile.createNewFile();
                out = new FileOutputStream(outFile);
                copyFile(in, out);
                out.flush();
                out.close();
            }
            in.close();
            in = null;
            out = null;
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

    String fileMd5(InputStream inputStream){
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        DigestInputStream dis = new DigestInputStream(inputStream, md);
        byte[] digest = md.digest();
        return digest.toString();
    }


}
