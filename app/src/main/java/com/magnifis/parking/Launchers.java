package com.magnifis.parking;


import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import com.magnifis.parking.model.DoublePoint;
import com.magnifis.parking.phonebook.PhoneBook;
import com.magnifis.parking.suzie.SuzieHints;
import com.magnifis.parking.utils.Langutils;
import com.magnifis.parking.utils.Translit;
import com.magnifis.parking.utils.Utils;

import static com.magnifis.parking.utils.Utils.*;
import static com.robinlabs.utils.BaseUtils.isEmpty;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class Launchers {
	
	final static String TAG=Launchers.class.getSimpleName();
	
	public static void launchPlayStore(Context ctx,String url) {
		  if (!isEmpty(url)) {
			 Uri uri=Uri.parse(url);
			 if (uri!=null) {
		        if ("market".equals(uri.getScheme()) ||
		        	uri.getHost().startsWith("play.google")) {
		          Intent it=new Intent(Intent.ACTION_VIEW);
		          it.setData(uri);
		          ComponentName cns[]=getIntentActivities(it,/*"com.android.vending"*/".google.");
		          if (!isEmpty(cns)) {
		        	  it.setComponent(cns[0]);
		        	  Launchers.startNestedActivity(ctx, it);
		        	  return;
		          }
		        }
		        launchBrowser(ctx,url);
			 }
		  }
		}
		
		public static ComponentName [] getIntentActivities(Intent it, String substr) {
			PackageManager pm=App.self.getPackageManager();
			List<ResolveInfo> ris=pm.queryIntentActivities(it, 0);
			if (!isEmpty(ris)) {
			   ArrayList<ComponentName> cns=new ArrayList<ComponentName>();
			   for (ResolveInfo r:ris) {
				   ActivityInfo ai=r.activityInfo;
				   if (ai!=null) {
					   String n=ai.name, p=ai.packageName;
					   if (!(isEmpty(p)||isEmpty(n))) {
						   if (!isEmpty(substr)&&!(n.contains(substr)||p.contains(substr))) continue;
						   cns.add(new ComponentName(p,n));
					   }
				   }
			   }
			   if (!cns.isEmpty())
				  return cns.toArray(new ComponentName[cns.size()]);
			}
			return null;
		}

	public static void showContacts(Context ctx) {
		Intent it = new Intent();
		it.setAction(Intent.ACTION_VIEW);
		it.setType(ContactsContract.Contacts.CONTENT_TYPE);
		startNestedActivity(ctx,it);
	}
	
	public static void shareTextPlain(Context ctx, final String subj, final String body) {
		Intent it= new Intent();
		it.setAction(Intent.ACTION_SEND);
		it.setType("text/plain");
		it.putExtra(
			Intent.EXTRA_TEXT, 
			body	+ " " + 
					App.self.getString(R.string.share_robin_url)
		);
		it.putExtra(Intent.EXTRA_SUBJECT, subj);
		startNestedActivity(
			ctx,
			it,it,false,
			new IntentLauncher(ctx) {

				@Override
				public void launch(Intent it, boolean modifyAnywhere) {
                    ComponentName cn=it.getComponent();
                    // "com.facebook.katana"
                    if (modifyAnywhere||
                    	(cn!=null&&!isEmpty(cn.getPackageName())&&cn.getPackageName().contains("facebook"))
                    ) {
                		MainActivity.get().fbHelper.shareAppToFeed(subj,body);
                		return;
                    }
					super.launch(it,modifyAnywhere);
				}
				
			},
			null
		);
	}
	
	public static void shareRobin(Context ctx) {
		Intent it= new Intent();
		it.setAction(Intent.ACTION_SEND);
		it.setType("text/plain");
		it.putExtra(Intent.EXTRA_TEXT, App.self.getString(R.string.launchers_like_robin) + " " + 
															App.self.getString(R.string.share_robin_url));
		it.putExtra(Intent.EXTRA_SUBJECT, App.self.getString(R.string.share_robin_subj));
		startNestedActivity(
			ctx,
			it,it,false,
			new IntentLauncher(ctx) {

				@Override
				public void launch(Intent it, boolean modifyAnywhere) {
                    ComponentName cn=it.getComponent();
                    // "com.facebook.katana"
                    if (modifyAnywhere||
                    	(cn!=null&&!isEmpty(cn.getPackageName())&&cn.getPackageName().contains("facebook"))
                    ) {
                		MainActivity.get().fbHelper.shareAppToFeed(
                				App.self.getString(R.string.share_robin_subj)
                		);
                		return;
                    }
					super.launch(it,modifyAnywhere);
				}
				
			},
			null
		);
	}

	public static  void composeTextMessage(Context ctx,boolean doAdvert) {
		composeTextMessage(ctx, (String)null, doAdvert);
	}
	
	public static  void composeTextMessage(Context ctx,String smsBody, String phone) {
		composeTextMessage(ctx,smsBody,phone,App.self.shouldAdvertInSms());
	}
	
	public static  void composeTextMessage(Context ctx, String smsBody, String phone, boolean doAdvert) {
		Intent it = new Intent();
		it.setAction(Intent.ACTION_SENDTO);
		it.setType("text/plain");
		
		String advert = App.self.getString(R.string.adv_for_sms);
		
		boolean nobody=isEmpty(smsBody);
		
		if (nobody) smsBody="";
		
		if (doAdvert) {
			if (nobody) {
				smsBody = "\n" + advert;
			} else {
				if (smsBody.length() + advert.length() < 140)
					smsBody += "\n" + advert; 
			}
		}
		
		it.putExtra("sms_body", smsBody);

		StringBuilder sb=new StringBuilder("sms:");
		if (phone!=null) sb.append(phone);
		it.setData(Uri.parse(sb.toString()));
		startNestedActivity(ctx, it);
	}
	
	public static  void composeTextMessage(Context ctx,String smsBody, boolean doAdvert) {
		composeTextMessage(ctx,smsBody, null, doAdvert);
	}
	
	public static  void composeHtmlEmail(Context ctx, Object subj ,String body) {
		composeHtmlEmail(ctx, subj,Html.fromHtml(body));
	}	
	
	public static  void composeHtmlEmail(Context ctx,Object subj ,Spanned body, Uri ...extras) {
		if (subj instanceof Integer) subj=MainActivity.get().getString((Integer)subj);
		Intent it = new Intent();
		it.setAction(Intent.ACTION_SENDTO);
		it.setType("text/plain");		
		
		Intent emailIntent = new Intent(android.content.Intent.ACTION_SENDTO);
		emailIntent.setType("text/html");
		emailIntent.setData(Uri.parse("mailto:"));
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subj.toString());
		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);
		if (!isEmpty(extras)&&extras[0]!=null)
			emailIntent.putExtra(Intent.EXTRA_STREAM, extras);
		startNestedActivity(ctx,emailIntent);
	}
	
	public static  void composeEmail(Context ctx) {
		composeEmail(
			ctx,
			"",
			"\n ----- \n"+App.self.getString(R.string.adv_for_mail)			
		);
	}
	
	public static  void composeEmail(Context ctx, Object subj,String body) {
		if (subj instanceof Integer) subj=App.self.getString((Integer)subj);
		Intent it = new Intent();
		it.setAction(Intent.ACTION_SENDTO);
		it.setType("text/plain");
		it.setData(Uri.parse("mailto:?subject="+urlencode(subj.toString())+"&body="+urlencode(body)));
		startNestedActivity(ctx,it);
	}

	
	@SuppressLint("NewApi")
	public static void killAllProcesses() {
		Intent it = new Intent(Intent.ACTION_MAIN);
		it.addCategory(Intent.CATEGORY_HOME);
		Utils.startActivityFromNowhere(it);

		final ActivityManager am = (ActivityManager)App.self.getSystemService(Context.ACTIVITY_SERVICE);
		if (am == null)
			return;
		List<ActivityManager.RunningTaskInfo> tti=am.getRunningTasks(100);
		if (tti!=null) for (int i=0;i<tti.size();i++) {
		   ActivityManager.RunningTaskInfo ti=tti.get(i);
		   if (ti != null && ti.topActivity != null && !Utils.isMyPackage(ti.topActivity.getPackageName())) {
			   if (android.os.Build.VERSION.SDK_INT > 7)
				   am.killBackgroundProcesses(ti.topActivity.getPackageName());
			   else
				   am.restartPackage(ti.topActivity.getPackageName());
		   }
		}
}
	
	@SuppressLint("NewApi")
	public static void killProcess(final String packageName) {

		if (Utils.isEmpty(packageName))
			return;
		
		if (Utils.isMyPackage(packageName))
			return;
		
		Intent it = new Intent(Intent.ACTION_MAIN);
		it.addCategory(Intent.CATEGORY_HOME);
		Utils.startActivityFromNowhere(it);

		final ActivityManager am = (ActivityManager)App.self.getSystemService(Context.ACTIVITY_SERVICE);
		if (am == null)
			return;
		if (android.os.Build.VERSION.SDK_INT > 7)
			am.killBackgroundProcesses(packageName);
		else
			am.restartPackage(packageName);
	}
	
	@SuppressLint("NewApi")
	public static boolean killProcessLike(final String text) {

		if (Utils.isEmpty(text))
			return false;
		
		Translit t = Translit.getHebRus();

		String s = text.toLowerCase();
		s = t.process(s);
		s = Langutils.normalize_phonetics(s);
		
		Log.d("kill", "KILL: FIND "+text);
		   
		final ActivityManager am = (ActivityManager)App.self.getSystemService(Context.ACTIVITY_SERVICE);
		if (am == null)
			return false;
		final PackageManager pm = App.self.getPackageManager();
		if (pm == null)
			return false;
		
		List<ActivityManager.RunningTaskInfo> tti=am.getRunningTasks(100);
		String foundPackage = null;
		String foundDomain = null;
		double foundLikeness = 0;
		
		if (tti!=null) for (int i=0;i<tti.size();i++) {
			
		   ActivityManager.RunningTaskInfo ti=tti.get(i);
		   if (ti == null)
			   continue;
		   if (ti.baseActivity == null)
		   	continue;

		   String testPackage = ti.baseActivity.getPackageName();
		   String testClass = ti.baseActivity.getClassName();
		   if (Utils.isEmpty(testPackage))
			   continue;
		   
		   testPackage = testPackage.toLowerCase();
		   String Likeness = "";
		   /*
		   String [] testDomains = testPackage.split("\\.");
		   if (testDomains.length > 0)
			   Likeness = testDomains[0]+".";
		   for (int k=1; k<testDomains.length; k++) {
			   double testLikeness = Langutils.likeness(Langutils.normalize_phonetics(testDomains[k]), s);
			   Likeness += testDomains[k]+"("+Math.round(10*testLikeness)+").";
			   if ((testLikeness > foundLikeness) && (testLikeness > 0.9f)) {
				   foundLikeness = testLikeness;
				   foundPackage = testPackage; 
				   foundDomain = testDomains[k];
			   }
		   }	
		   */		   
		   String testTitle = testPackage;
			try {
				ApplicationInfo ai;
				ai = pm.getApplicationInfo(testPackage, 0);
				if (ai != null) {
					CharSequence cs = ai.loadLabel(pm).toString();
					if (cs != null)
						testTitle = cs.toString();		
				}
			} catch (NameNotFoundException e) {}

			if (!Utils.isEmpty(testTitle)) {
			   testTitle = testTitle.toLowerCase();
			   testTitle = t.process(testTitle);
			   double testLikeness = Langutils.likeness(Langutils.normalize_phonetics(testTitle), s);
			   Likeness += " "+testTitle+"("+Math.round(10*testLikeness)+")";
			   if ((testLikeness > foundLikeness) && (testLikeness > 0.9f) && !Utils.isMyPackage(testPackage)) {
				   foundLikeness = testLikeness;
				   foundPackage = testPackage; 
				   foundDomain = testTitle;
			   }
		   }
		   Log.d("kill", "KILL: found "+Likeness);
		}

		Log.d("kill", "KILL: KILL "+foundPackage);
		if (foundPackage == null)
			return false;
		
		Intent it = new Intent(Intent.ACTION_MAIN);
		it.addCategory(Intent.CATEGORY_HOME);
		Utils.startActivityFromNowhere(it);
		
		if (android.os.Build.VERSION.SDK_INT > 7)
			am.killBackgroundProcesses(foundPackage);
		else
			am.restartPackage(foundPackage);
		
		return true;
	}
	
	@SuppressLint("NewApi")
	public static boolean runProcessLike(final String text) {

		if (Utils.isEmpty(text))
			return false;
		
		Translit t = Translit.getHebRus();
		
		String s = text.toLowerCase();
		s = t.process(s);
		s = Langutils.normalize_phonetics(s);
		
		Log.d("run", "RUN: FIND "+text);
		   
		final ActivityManager am = (ActivityManager)App.self.getSystemService(Context.ACTIVITY_SERVICE);
		if (am == null)
			return false;
		final PackageManager pm = App.self.getPackageManager();
		if (pm == null)
			return false;
		
		String foundPackage = null;
		String foundClass = null;
		double foundLikeness = 0;
		
		for(AppCacheEntry ti:getAppCache()) {
			
		   if (ti == null)
			   continue;

		   String testPackage = ti.pkName;
		   String testClass = ti.actName;
		   String testTitle = ti.appName;
		   if (Utils.isEmpty(testPackage) || Utils.isMyPackage(testPackage))
			   continue;
		   
		   String Likeness = "";
		   double packageLikeness = 0;
		   if (ti.pkNameNormalizedArray.length > 0)
			   Likeness = ti.pkNameArray[0]+".";
		   for (int k=1; k<ti.pkNameNormalizedArray.length; k++) {
			   double testLikeness = Langutils.likeness(ti.pkNameNormalizedArray[k], s);
			   if (!Utils.isEmpty(ti.appNameNormalized) && ti.pkNameNormalizedArray[k].equals(ti.appNameNormalized)) {
				   Likeness += ti.pkNameArray[k]+"(-).";
				   testLikeness = 0;
			   }
			   else {
				   Likeness += ti.pkNameArray[k]+"("+Math.round(10*testLikeness)+").";
				   if (testLikeness > packageLikeness && testLikeness > 0.9f)
					   packageLikeness = testLikeness;
			   }
		   }	
		   		
		   double totalLikeness = packageLikeness;
		   if (!Utils.isEmpty(testTitle)) {
			   double titleLikeness = Langutils.likeness(ti.appNameNormalized, s);

			   if (titleLikeness > 0.9f)
				   totalLikeness += titleLikeness * 2;
			   
			   Likeness += " "+testTitle+"("+Math.round(10*titleLikeness)+")";
		   }
		   if (totalLikeness > foundLikeness) {
			   foundLikeness = totalLikeness;
			   foundPackage = testPackage; 
			   foundClass = testClass;
		   }
		   
		   if (totalLikeness > 0)
			   Log.d("run", "RUN: found "+Math.round(10*totalLikeness)+" "+Likeness);
		}

		Log.d("run", "RUN: RUN "+foundPackage);
		if (foundPackage == null)
			return false;
		
		Intent it=new Intent(Intent.ACTION_MAIN);
		it.addCategory(Intent.CATEGORY_LAUNCHER);
		it.setClassName(foundPackage, foundClass);
		it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Utils.startActivityFromNowhere(it);
		
		return true;
	}
	
	/** Send email to your self - for Note purposes */
	public static  void composeEmailToYourself(Context ctx) {
		String recipient = App.self.getGmailAccountName();
		String subject = App.self.getString(R.string.P_note);
		String body = "";
		
		Intent it = new Intent();
		it.setAction(Intent.ACTION_SENDTO);
		it.setType("text/plain");
		it.setData(Uri.parse("mailto:" + recipient + "?subject="+urlencode(subject)+"&body="+urlencode(body)));
		startNestedActivity(ctx, it);
	}

	
	public static  void composeFeedback(Context ctx) {
		composeFeedback(ctx,null,null);
	}
	
	public static  void composeFeedback(Context ctx, CharSequence withText, File withFile) {
		//MyTTS.interrupt();
		//MainActivity.speakText(R.string.P_provide_feedback);
		Intent it = new Intent();
		it.setAction(Intent.ACTION_SENDTO);
		it.setType("text/plain");
		StringBuilder sb=new StringBuilder("");
		if (!isEmpty(withText))
			sb.append(withText);
		sb.append("\n\n------------------\n");
		sb.append(android.os.Build.MODEL);
		sb.append(" (");
		sb.append(android.os.Build.DEVICE);
		sb.append(")\nAndroid Version: ");
		sb.append(android.os.Build.VERSION.RELEASE);
		//sb.append("\nBuild: ");
		//sb.append(android.os.Build.DISPLAY);
		sb.append("\nResolution: ");
		sb.append(App.self.getDspResolutionString());
		PackageInfo pki=App.self.getPackageInfo();
		sb.append("\nAndroID: ");
		sb.append(App.self.android_id);
		sb.append("\nClient Version: ");
		sb.append(pki.versionName);
		sb.append(" (");
		sb.append(pki.versionCode);
		sb.append(")");
		//sb.append("\nFree HD space: ");
		//String strsz=externalStorageAvailableString();
		//sb.append(strsz==null?" not mounted ":strsz);
		sb.append("\nProximity sensor: ");
		sb.append(App.self.anyProximitySensor()?"1":"0");
		sb.append("\nLocale: ");
		sb.append(Locale.getDefault().toString());
		sb.append("\nTimezone: ");
		sb.append(TimeZone.getDefault().getDisplayName());
		if (App.isBluetoothEnabled()) sb.append("\nBT is enabled");
		 
		it.setData(Uri.parse("mailto:info@magnifis.com?subject=" + urlencode(App.self.getString(R.string.P_feedback)) + 
							"&body="+ urlencode(sb.toString())));
	
		//it.putExtra(Intent.EXTRA_TEXT,sb.toString());
		
		if (withFile!=null) {
			it.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(withFile));
		}
		
	
		if (App.self.getBooleanPref("qaTesting")) { 
			
			String phoneBookCsvPath = PhoneBook.getInstance().exportAsCvsFile(); 
			if (!Utils.isEmpty(phoneBookCsvPath)) {
				it.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + phoneBookCsvPath)); 
				//it.setType("text/csv"); 
			} else 
				Log.i(TAG, "Phone book not ready yet to be serialized..."); 
		}
				
		startNestedActivity(ctx, it);
	}
	
	public static void openDialer(Context ctx) {
		startNestedActivity(ctx,new Intent(Intent.ACTION_DIAL));
	}
	
	public static void dial(Context ctx, String phone) {
		Intent i = new Intent(Intent.ACTION_DIAL, isEmpty(phone)?null:Uri.parse("tel:" + phone));
		startNestedActivity(ctx,i);		
	}
	
	public static void directdial(Context ctx,String phone) {
		Intent i = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone));
		startNestedActivity(ctx,i);		
	}
	
	public static void lookAtMarketFor(Context ctx, String pkg) {
		Intent it = new Intent(Intent.ACTION_VIEW,Uri.parse("market://details?id="+pkg));
		_startNestedActivity(ctx,it);
	}
	
	public static void youtube(Context ctx, String query) {
		try {
            Intent it = new Intent(Intent.ACTION_SEARCH);
            it.setPackage("com.google.android.youtube");
            if (query != null)
                it.putExtra("query", query);
            startNestedActivity(ctx,it);
            //PackageManager pm=App.self.getPackageManager();
			//_startNestedActivity(ctx,pm.getLaunchIntentForPackage("com.google.android.youtube"));
			return;
		} catch(Throwable t) {}

		Intent it=new Intent();
		it.setAction(Intent.ACTION_VIEW);
		it.setData(Uri.parse("https://youtube.com/results?search_query="+query));
		startNestedActivity(ctx,it);		
	}
	/*
	public static void youtube(Context ctx, String videoId) {
		Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoId));
    	startNestedActivity(ctx,i);
	}*/
	
	public static  void facebook(Context ctx) {
		Intent it = new Intent();
		it.setAction(Intent.ACTION_VIEW);
		
		it.setData(Uri.parse("fb://feed/"));
		
		//it.setPackage("com.facebook.katana");
		try {
			_startNestedActivity(ctx,it);
			return;
		} catch(Throwable t) {}
		it.setPackage(null);
		it.setData(Uri.parse("http://facebook.com"));
		startNestedActivity(ctx,it);
	}
	
	public static  void composeTwitt(Context ctx) {
		Intent it = new Intent();
		it.setAction(Intent.ACTION_SEND);
		it.putExtra(Intent.EXTRA_TEXT, "");
		it.setType("text/plain");
	    it.setPackage("com.twitter.android");
		try {
			_startNestedActivity(ctx,it);
			return;
		} catch(Throwable t) {}
		it.setPackage(null);
		it.setAction(Intent.ACTION_VIEW);
		it.setData(Uri.parse("https://twitter.com/"));
		startNestedActivity(ctx,it);
	}
	
	public static void launchAgenda(Context ctx, Date forTime) {
		if (forTime==null) forTime=new Date();
		
		Intent it=new Intent(Intent.ACTION_VIEW);
		if (isAndroid4orAbove) { 
			String uri = CalReminding.calendarUri() + "time/" + forTime.getTime(); 
			it.setData(Uri.parse(uri));
			Log.i(TAG, "Launching calendar via uri: " + uri); 
		} else {
			String cls = "com.android.calendar.LaunchActivity"; 
			it.putExtra("beginTime", forTime.getTime());
			it.setClassName("com.android.calendar",cls);
			Log.i(TAG, "Launching calendar via " + cls);
			try {
			   _startNestedActivity(ctx, it);
			   return;
			} catch(Throwable t) {			
			}
			it.setClassName("com.google.android.calendar","com.android.calendar.AgendaActivity");
		}
		try {
		  _startNestedActivity(ctx, it);
		} catch(Throwable t) {			
		}

	}
	
	
	public static void launchBrowser(Context ctx, String u) {
		Intent it=new Intent(Intent.ACTION_VIEW);
		it.setData(Uri.parse(u));
		/*
        Intent it=new Intent(ctx, Web.class);
        it.putExtra("URL", u);
        it.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        */
        startNestedActivity(ctx, it);
	}
	
	public static void launchBrowser(Context ctx) {
        /*
		Intent it=new Intent(Intent.ACTION_MAIN),
		       tst=new Intent(Intent.ACTION_VIEW)
		;
		tst.setData(Uri.parse("http://google.com"));
		startNestedActivity(ctx,it,tst);
		*/
        launchBrowser(ctx, "http://google.com");
	}
	
	public static int launchMusicTuner(Context ctx,String dsc) {
	   return launchMusicTuner(ctx,dsc,null);
	}

	public static int launchMusicTuner(Context ctx,String dsc, String packageName) {
		// Intent it = new Intent(android.provider.MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH);
		Intent it = new Intent("android.media.action.MEDIA_PLAY_FROM_SEARCH");
		if (packageName!=null) it.setPackage(packageName);
        if (isEmpty(dsc)) {
        	return startNestedActivity(ctx,new Intent(Intent.ACTION_MAIN), it);
        } else {
		    it.putExtra(SearchManager.QUERY, dsc);
		    int res=startNestedActivity(ctx,it);
		    if (res==0) {
		    	it.setAction(Intent.ACTION_SEARCH);
		    	it.setPackage("com.google.android.youtube");
		    	res=startNestedActivity(ctx,it);
		    }
		    return res;
        }
	}

	
	public static void launchCamera(Context ctx) {
		Intent it = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA),
			   testIt= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		startNestedActivity(ctx,it,testIt);
	}
	
	public static void _startNestedActivity(Context ctx,Intent it) {
		_startNestedActivity(ctx, it,MainActivity.TASK_TERMINATE_REQUEST_CODE);
    }
	
	public static void _startNestedActivity(Context ctx, Intent it, int actTermCode) {
		if (ctx==null||!(ctx instanceof Activity)) {
		  Utils.startActivityFromNowhere(it);
		} else {
		   _startNestedActivity((Activity)ctx,it,actTermCode);
		}
	}
	
	public static void _startNestedActivity(Activity act,Intent it) {
		 _startNestedActivity(act,it,MainActivity.TASK_TERMINATE_REQUEST_CODE);
	}
	
	public static void _startNestedActivity(Activity ma,Intent it, int actTermCode) {
	    ma.startActivityForResult(it, actTermCode);
	    //boolean useProximitySensor=App.self.anyProximitySensor()&&App.self.shouldUseProximitySensor();
	    
	    //if  (useProximitySensor) ProximityWakeUp.start(ma, true);
	}
	
	public static int startNestedActivity(Context ctx,Intent it) {
		return startNestedActivity(ctx,it,it);
	}
	
	public static void startNestedActivity(Context ctx , Intent it, boolean useCheckbox) {
		startNestedActivity(ctx,it,it,useCheckbox, new IntentLauncher(ctx),null);
	}
	
	public static void startNestedActivityWithCode(final Context ctx,Intent it, final int actTermCode, SuccessFailure sf) {
		startNestedActivity(
		  ctx,it,it,true, 
		  new IntentLauncher(ctx) {
			@Override
			public void launch(Intent it, boolean modifyAnywhere)  {
				if (ctx instanceof Service) {
				  ResultProxyActivity.startActivityFromServiceForResult(
					 it, actTermCode, ctx.getClass()
				  );
				} else
				  _startNestedActivity(ctx,it,actTermCode);
			}			  
		  },
		  sf
		);
	}
	
	public static int startNestedActivity(Context ctx, Intent it, Intent testIntent) {	
		return startNestedActivity(ctx,it,testIntent, true, new IntentLauncher(ctx),null);
	}
	
	public static int startNestedActivity(
			   final Context ctx,
			   final Intent it,
			   final Intent testIntent,
			   final boolean useCheckbox,
			   final IntentLauncher ila,
			   final SuccessFailure sf
	)  {
	  final int rv[]={-1};
	  Activity ac=((ctx!=null)&&(ctx instanceof Activity))?(Activity)ctx:null;	  
	  
	  Runnable r=new Runnable() {
			@Override
			public void run() {
				rv[0]=_startNestedActivity(
				  ctx,it,testIntent,useCheckbox,ila,sf
				);
			}
		  };
	  Throwable t=(ac==null)?Utils.runInGuiAndWait((Handler)null,r):Utils.runInGuiAndWait(ac,r);
	  if (t!=null&& t instanceof RuntimeException) throw (RuntimeException)t;
	  return rv[0];
	}
	
	public static int _startNestedActivity(
	   Context ctx,
	   Intent it, Intent testIntent, boolean useCheckbox,
	   IntentLauncher ila,
	   SuccessFailure sf
	) {	
		PackageManager pm=App.self.getPackageManager();
		
		SuzieHints.disableHints();
		
		List<ResolveInfo> ri=pm.queryIntentActivities(testIntent, 0);
		int ins=((ri==null)?0:ila.shouldInsert(ri)),sz=((ri==null)?ins:(ins+ri.size()));
		if (sz>0) {
		  if (sz==1) {
			 if (!isEmpty(ri)) {
			   ResolveInfo r=ri.get(0);
			   it.setClassName(r.activityInfo.packageName, r.activityInfo.name);
			 }
			 ila.launch(it,isEmpty(ri));
		  } else {
			  if (useCheckbox&&launchDefaultIfCan(ctx,it,testIntent)) return sz;			  
			  pickApp(ctx,ri,it,testIntent,useCheckbox,sf,ila);
		  }
		}
		return sz;
	}
	
	public static class AppCacheEntry {
	   public String getPkName() {
			return pkName;
		}

		public void setPkName(String pkName) {
			this.pkName = pkName;
		}

		public String getActName() {
			return actName;
		}

		public void setActName(String actName) {
			this.actName = actName;
		}

		public String getAppName() {
			return appName;
		}

		public void setAppName(String appName) {
			this.appName = appName;
		}

	    protected String pkName, actName, appName, appNameNormalized;
	    String [] pkNameArray, pkNameNormalizedArray;
	    
	    public AppCacheEntry(String pkName, String actName, String appName) {
	    	this.pkName=pkName;
	    	this.actName=actName;
	    	this.appName=appName;
	    	
	    	Translit t = Translit.getHebRus();
	    	
	    	appNameNormalized = appName.toLowerCase();
	    	appNameNormalized = t.process(appNameNormalized);
	    	appNameNormalized = Langutils.normalize_phonetics(appNameNormalized);
	    	
	    	pkNameArray = pkName.split("\\.");
	    	pkNameNormalizedArray = new String[pkNameArray.length];
	    	for (int k=0; k<pkNameArray.length; k++) {
	    		pkNameNormalizedArray[k] = pkNameArray[k].toLowerCase();
	    		pkNameNormalizedArray[k] = t.process(pkNameNormalizedArray[k]);
	    		pkNameNormalizedArray[k] = Langutils.normalize_phonetics(pkNameNormalizedArray[k]);
	    	}
	    }
	    
	    public ResolveInfo getResolveInfo(PackageManager pm) {
	      Intent it=new Intent(Intent.ACTION_MAIN);
		  it.addCategory(Intent.CATEGORY_LAUNCHER);
		  it.setClassName(pkName, actName);
		  List<ResolveInfo> rii=pm.queryIntentActivities(it, 0);
	      return isEmpty(rii)?null:rii.get(0);
	    }
	    
	    public ComponentName getComponentName() {
	      return new ComponentName(pkName,actName);
	    }
	    
	    public Drawable getIcon(PackageManager pm) {
			try {
				return pm.getActivityIcon(getComponentName());
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
	    }
	    
	}
	
	public static AppCacheEntry [] getAppsToCache() {
		Intent it=new Intent(Intent.ACTION_MAIN);
		it.addCategory(Intent.CATEGORY_LAUNCHER);

		PackageManager pm= App.self.getPackageManager();
		List<ResolveInfo> rii=pm.queryIntentActivities(it, 0);
        if (!isEmpty(rii)) {
        	AppCacheEntry ens[]=new AppCacheEntry [rii.size()];
        	for (int i=0;i<rii.size();i++) {
        		ResolveInfo r=rii.get(i);
        		String lbl = r.activityInfo.packageName;
        		try {
        			lbl=r.loadLabel(pm).toString();
        		} catch(Throwable t) {
        			t.printStackTrace();
        		}
        		ens[i] = new AppCacheEntry(
        			r.activityInfo.packageName, r.activityInfo.name,
        			lbl
        		);
        	}
        	return ens;
        }
		return null;
	}
	
	static private volatile AppCacheEntry appCache[]=null; 
	
	public static void releaseAppCache() {
		synchronized(AppCacheEntry.class) {
  		  appCache=null;
		}
	}
	
	public static AppCacheEntry []getAppCache() {
		synchronized(AppCacheEntry.class) {
			if (appCache!=null) return 	appCache;
			for (;;) try {
				if (appCacheLoader==null||!appCacheLoader.isAlive()) _loadAppCache();
				AppCacheEntry.class.wait();
				return 	appCache;
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (Throwable t) {
				t.printStackTrace();
				return null;
			}
		}		
	}
	
	public static void loadAppCache() {
		synchronized(AppCacheEntry.class) {
			if (appCache==null&&(appCacheLoader==null||!appCacheLoader.isAlive()))  _loadAppCache();
		}
	} 
	
	private static Thread appCacheLoader=null;
	
	private static void _loadAppCache() {
		appCacheLoader=new Thread() {
		  @Override
		  public void run() {
			  appCache=null;
			  appCache=getAppsToCache();
			  synchronized(AppCacheEntry.class) {
				  appCacheLoader=null;
				  AppCacheEntry.class.notify();	
			  }
		  }
		};
		//t.setPriority(Thread.MIN_PRIORITY);
		appCacheLoader.start();
	}
	
	public static void getLaunchAppByNameList(String appName, List<AppCacheEntry> al) {
		appName=trim(appName.toLowerCase());
		String appNameNSP=removeSpaces(appName.replace('\'', ' ').replace(" is ", "s"));

		for (AppCacheEntry ace:getAppCache()) {
			String anlc=trim(ace.getAppName().toLowerCase().replace('\u00A0',' '));
			///////////////////////////////
			/*
			if (anlc.endsWith(appName)) {
				Log.d(TAG,"flashlight="+Integer.toHexString(anlc.charAt(0)));
			}
			*/
			////////////////////////////////
			if (
			    stringContainsPhrase(anlc,appName)||
				stringContainsPhrase(breakCamalCase(ace.getAppName()).toLowerCase(),appName)||
				stringContainsPhrase(ace.getPkName().toLowerCase(),appName)
			) 
			   al.add(ace);
			else {
				String anlcNSP=removeSpaces(anlc);
				int ix=anlcNSP.indexOf(appNameNSP);
				if (ix>=0) {
				  // OK, look at details
					List<Integer> spp=spacePositions(anlc.replace('\'', ' ').replace(" is ", "s"));
					if ((ix==0)||spp.contains(ix)) {
					  ix=ix+appNameNSP.length();
					  if ((ix==anlcNSP.length())||spp.contains(ix)) al.add(ace);
					}
			    }
			}
		}
	}
	
	public static void launchAppFromList(
			Context ctx,
			List<AppCacheEntry> al,
			final SuccessFailure sf
	) {
		Intent it=new Intent(Intent.ACTION_MAIN);
		it.addCategory(Intent.CATEGORY_LAUNCHER);
		it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PackageManager pm=App.self.getPackageManager();
	
		if (al.isEmpty()) {
			if (sf!=null) sf.onFailure();
		} else if (al.size()==1) {
			ResolveInfo r=al.get(0).getResolveInfo(pm);
			it.setClassName(r.activityInfo.packageName, r.activityInfo.name);
			try {
				_startNestedActivity(ctx,it);	
				if (sf!=null) sf.onSuccess();
			} catch(Throwable t) {}
			if (sf!=null) sf.onFailure();
		} else {
			PickAppListItem ar[]=new PickAppListItem[al.size()];
			for (int i=0;i<al.size();i++) ar[i]=new PickAppListItem(al.get(i),pm);
			pickApp(ctx,ar,it,it,false,sf,new IntentLauncher(ctx));
		}

	}
	
	/*
	public static void lauchAppByName(
		String appName,
		final SuccessFailure sf
	) {
		lauchAppFromList(getLauchAppByNameList(appName),sf);
	}
	*/
	
	public static String[] getDefaultLauncher(Intent it, Intent tstIt) {
		CharSequence key=encodePrefName(it,tstIt);
		SharedPreferences sPrefs = App.self.getPrefs();
		String app=sPrefs.getString(key.toString(), null);
		if (app!=null) return app.split(":");
		return null;
	}
	
	public static void setDefaultLauncher(Intent it, Intent tstIt) {
		CharSequence key=encodePrefName(it, tstIt);
		SharedPreferences sPrefs = App.self.getPrefs();
		Editor ed=sPrefs.edit();
		ed.putString(key.toString(), it.getComponent().getPackageName()+":"+it.getComponent().getClassName());
		ed.commit();
	}

	
	public static boolean launchDefaultIfCan(Context ctx,Intent it, Intent tstIt) {
	  String app[]=getDefaultLauncher(it,tstIt);
	  if (app!=null) try {
		  it.setClassName(app[0], app[1]);
		  _startNestedActivity(ctx,it);
		  return true;
	  } catch(Throwable t) {
         // should clean the default
	  }
	  return false;	
	}
	
	public static void resetDefaults() {
		SharedPreferences sPrefs = App.self.getPrefs();

		Map<String,?> allPrefs=sPrefs.getAll();
		
		if (allPrefs==null) return; 
		
		List<String> toRemove=new ArrayList<String>();
		
		for (String k:allPrefs.keySet())
		  if (k.startsWith("launcher:")) toRemove.add(k);
		
		if (!toRemove.isEmpty()) {
		   Editor ed=sPrefs.edit();
		   for (String k:toRemove) ed.remove(k);
		   ed.commit();
		}
	}
	
	private static CharSequence encodePrefName(Intent it, Intent testIt) {
	  StringBuilder sb=new StringBuilder("launcher");
	  sb.append(':');
	  if (it.getAction()!=null) sb.append(it.getAction());
	  sb.append(':');
	  if (it.getType()!=null) sb.append(it.getType());
	  else if (testIt!=null&&testIt.getType()!=null)
		  sb.append(testIt.getType());
	  sb.append(':');
	  if (it.getData()!=null) {
		sb.append(it.getScheme());
		sb.append(':');
		if (it.getData().getHost()!=null) sb.append(it.getData().getHost());
	  } else if (testIt!=null&&testIt.getData()!=null) {
		  sb.append(testIt.getScheme());
		  sb.append(':');		  
	  }
	  return sb;
	}
	
	public static IntentFilter createFilter(Intent it) throws MalformedMimeTypeException {
		IntentFilter inf=new IntentFilter(it.getAction());
		if (it.getType()!=null) inf.addDataType(it.getType());
		if (it.getData()!=null) {
			inf.addDataScheme(it.getScheme());
		}
		return inf;
	}
	
	public static class PickAppListItem {
		public final String name;
		public final Drawable icon;
		public final String packageName;
		public final String className;
		
		public PickAppListItem(AppCacheEntry ace, PackageManager pm) {
			this(ace.getAppName(),ace.getIcon(pm), ace.getPkName(), ace.getActName());
		}
		
		private PickAppListItem(String text, Drawable icon, String packageName, String className) {
			this.name = text;
			this.icon = icon;
			this.packageName = packageName;
			this.className = className;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
	
	static void pickApp(
			Context ctx,
			List<ResolveInfo> apps,
			Intent it,
			Intent testIt,
			boolean useCheckbox,
			SuccessFailure sf,
			IntentLauncher ila
		) {
	
		AlertDialog.Builder builder = new AlertDialog.Builder(App.self);
		builder.setTitle(App.self.getString(R.string.launchers_select_activity));
		
		PackageManager pm = App.self.getPackageManager();

		// Form those activities into an array for the list adapter
		final PickAppListItem[] items = new PickAppListItem[apps.size()];
		int i = 0;
		for (ResolveInfo resInfo : apps) {
			String context = resInfo.activityInfo.packageName;
			String packageClassName = resInfo.activityInfo.name;
			
			CharSequence label = resInfo.loadLabel(pm);
			Drawable icon = resInfo.loadIcon(pm);
			items[i] = new PickAppListItem(label.toString(), icon, context, packageClassName);
			i++;
		}
	
		pickApp(ctx,items, it, testIt, useCheckbox, sf, ila);
	}
	
	public static class IntentLauncher {
	   final protected Context context;
		
	   public IntentLauncher(Context ctx) {
		   this.context=ctx;
	   }
		
	   public int shouldInsert(List<ResolveInfo> ri) {
		  return 0;
	   }
	   
	   public PickAppListItem[] modify(PickAppListItem[] items) {
		   return items;
	   }
	   
	   public void launch(Intent it) {
           launch(it, false);
	   }
		
	   public void launch(Intent it, boolean modifyAnywhere) {
		   _startNestedActivity(context,it);
	   }
	};
	
	static void pickApp(
			Context ctx,
			final PickAppListItem[] items,
			final Intent it,
			final Intent testIt,
			final boolean useCheckbox,
			final SuccessFailure sf,
			final IntentLauncher ila
		) {

		    new RunningInActivity(ctx) {
				@Override
				public void run() {
					
					Log.d(Launchers.TAG,"pickApp bp1");

					AlertDialog.Builder builder = new AlertDialog.Builder(activity);
					builder.setTitle(App.self.getString(R.string.launchers_select_activity));
					
					ArrayAdapter<PickAppListItem> adapter = new ArrayAdapter<PickAppListItem>(
						activity,
						android.R.layout.select_dialog_item,
						android.R.id.text1,
						items
					) {

						public View getView(int position, View convertView, ViewGroup parent) {
							// User super class to create the View
							View v = super.getView(position, convertView, parent);
							TextView tv = (TextView)v.findViewById(android.R.id.text1);

							// Put the icon drawable on the TextView (support various screen densities)
							DisplayMetrics dsm= App.self.getResources().getDisplayMetrics();
							
							int dpS = (int) (32 * dsm.density  / 0.5f);
							items[position].icon.setBounds(0, 0, dpS, dpS);
							tv.setCompoundDrawables(items[position].icon, null, null, null);

							// Add margin between image and name (support various screen densities)
							int dp5 = (int) (5 * dsm.density   / 0.5f);
							tv.setCompoundDrawablePadding(dp5);

							return v;
						}
					};	
					
					final CheckBox checkbox = new CheckBox(activity);
					if (useCheckbox) {
					  checkbox.setText(App.self.getString(R.string.launchers_use_by_default));
					  builder.setView(checkbox);
					}
					
					final boolean anySelection[]={false};
					
					builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
						   Intent it1=it;
						
						   @Override
						   public void onClick(DialogInterface dialog, int which) {
							   anySelection[0]=true;
							   dialog.dismiss();
							   if (testIt==null||testIt.equals(it))
							      it1.setClassName(items[which].packageName, items[which].className);
							   else {
								  PackageManager pm=App.self.getPackageManager();
								  if (Intent.ACTION_MAIN.equals(it.getAction())) {
									it1=pm.getLaunchIntentForPackage(items[which].packageName);
								  } else
							        it1.setPackage(items[which].packageName);
							   }
							   try {
								 ila.launch(it1);
							     if (sf!=null) sf.onSuccess();
							     if (useCheckbox&&checkbox.isChecked())  setDefaultLauncher(it1, testIt);
							   } catch(Throwable t) {
								   t.printStackTrace();
							   }
							   if (sf!=null) sf.onFailure();
						   }
						  }
					);
					Log.d(Launchers.TAG,"pickApp bp2");
			 		AlertDialog ad=builder.create();
			 		
			 		Log.d(Launchers.TAG,"pickApp bp3");
			 		ad.setOnDismissListener(
			 		  new OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialog) {
			              if (!anySelection[0]&&sf!=null) sf.onCancel();
			              if (usingProxyActivity) activity.finish();
						}
			 		  }
			 		);
			 		Log.d(Launchers.TAG,"pickApp bp4");
			 		ad.show();					
			 		Log.d(Launchers.TAG,"pickApp bp5");
				}
		    };

	}
	
	public static void launchPhoneSettings(Context ctx) {
		Intent it=new Intent(android.provider.Settings.ACTION_SETTINGS);
		startNestedActivity(ctx,it);
	}
	
	public static void launchGpsSettings(Context ctx) {
		Intent it= new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		startNestedActivity(ctx,it);
	}
	
	public static void launchWifiSettings(Context ctx) {
		Intent it= new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
		startNestedActivity(ctx,it);
	}
	
	public static void launchAirplaneSettings(Context ctx) {
		Intent it= new Intent(android.provider.Settings.ACTION_AIRPLANE_MODE_SETTINGS);
		startNestedActivity(ctx,it);
	}
	
	public static void launchBatteryUsage(Context ctx) {
		Intent it = new Intent(Intent.ACTION_POWER_USAGE_SUMMARY);				
		startNestedActivity(ctx,it);
	}	
	
	public static void launchBluetoothSettings(Context ctx) {
		Intent it= new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
		startNestedActivity(ctx,it);
	}	
	
	public static boolean launchGpsNavigator(Context ctx, final DoublePoint dp) {
		String ntw=App.self.getString(R.string.NtWaze),
			   //ntt=App.self.getString(R.string.NtTelenav),
		       nto=App.self.getString(R.string.NtOther)
			   ;
		String navType=App.self.getStringPref(R.string.PfNavigatorType);
		/*
		if (ntt.equals(navType)) {
			
		} else*/
		if (ntw.equals(navType)) 
			return launchWaze(ctx,dp);
		else
		if (nto.equals(navType)) 
		    return launchOther(ctx,dp);	
	    else
			return launchGoogleNavigator(ctx,dp);
	}
	
    public static boolean launchOther(Context ctx,final DoublePoint dp) {
		Intent it=new Intent();
		it.setAction(Intent.ACTION_VIEW);
		String u="geo:" + dp.getLat() + "," + dp.getLon();
		Log.d(TAG+".launchWazeNavigator: ",u);
		it.setData(Uri.parse(u));
		try {
			startNestedActivity(ctx,it);
			return true;
		} catch(Throwable x) { 
			Log.e(TAG+".launchWazeNavigator: ","exception launching navigation: " + x);
		}
		return false;
      }
	
	public static boolean launchWaze(Context ctx, DoublePoint dp) {
		Intent it=getWazeLaunchIntent(dp);
		try {
			startNestedActivity(ctx, it);
			return true;
		} catch(Throwable x) { 
			Log.e(TAG+".launchWazeNavigator: ","exception launching navigation: " + x);
		}
		return false;
	}
  
	public static boolean launchGoogleNavigator(Context ctx, DoublePoint dp) {
		Intent it=new Intent();
		it.setAction(Intent.ACTION_VIEW);
		String u="google.navigation:q=" + dp.getLat() + "," + dp.getLon();
		Log.d(TAG+".launchGoogleNavigator: ",u);
		it.setData(Uri.parse(u));
		try {
			startNestedActivity(ctx,it);
			return true;
		} catch(Throwable x) { 
			Log.e(TAG+".launchGoogleNavigator: ","exception launching navigation: " + x);
		}
		return false;
	}
		  
	public static boolean launchSamsungNavigator(Context ctx, String person, String place) {
		Intent it = new Intent();
		it.setAction(Intent.ACTION_VIEW);
		String u = "google.navigation:";
		/*
		DoublePoint location=MainActivity.get().getLastKnownLocation();
		if (location!=null) {
			u = u + "q=" + location.getLat() + "," + location.getLon();
			u = u + "&typ=indoor";
		} else {
			u = u + "typ=indoor";
		}
		*/
		u = u + "q=37.385532,-121.927536";
		u = u + "&typ=indoor";		
		if (!isEmpty(person)) {
			u = u + "&perso=" + person;
		} else if (!isEmpty(place)) {
			u = u + "&plac=" + place;
		}
		Log.d(TAG + ".launchSamsungNavigator: ", u);
		it.setData(Uri.parse(u));
		try {
			startNestedActivity(ctx, it);
			return true;
		} catch (Throwable x) {
			Log.e(TAG + ".launchSamsungNavigator: ", "exception launching navigation: " + x);
		}
		return false;
	}	
	
	private static boolean isCallable(Intent intent) {  
        List<ResolveInfo> list = App.self.getPackageManager().queryIntentActivities(intent,   
            PackageManager.MATCH_DEFAULT_ONLY);  
        return (list != null && !list.isEmpty());  
	}  	
	
	private static Intent getWazeLaunchIntent(DoublePoint dp) {
		Intent it=new Intent();
		it.setAction(Intent.ACTION_VIEW);
		it.setPackage("com.waze");
		String u="geo:" + dp.getLat() + "," + dp.getLon();
		it.setData(Uri.parse(u));
		return it;
	}
	
	public static boolean isWazeInstalled(DoublePoint dp) {
		Intent wazeIt = getWazeLaunchIntent(dp);
		return isCallable(wazeIt);
	}
}
