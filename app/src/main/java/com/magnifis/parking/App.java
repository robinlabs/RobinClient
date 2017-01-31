package com.magnifis.parking;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.Application;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.Service;
import android.app.UiModeManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.magnifis.parking.billing.Billing;
import com.magnifis.parking.messaging.Message;
import com.magnifis.parking.model.DoublePoint;
import com.magnifis.parking.model.Understanding;
import com.magnifis.parking.pref.PrefConsts;
import com.magnifis.parking.pref.PrefDefaults;
import com.magnifis.parking.suzie.RequiresSuzie;
import com.magnifis.parking.suzie.SuzieHints;
import com.magnifis.parking.suzie.SuziePopup;
import com.magnifis.parking.suzie.SuzieService;
import com.magnifis.parking.tts.MyTTS;
import com.magnifis.parking.utils.MeasurementSystem;
import com.magnifis.parking.utils.ParserContext;
import com.magnifis.parking.utils.StateStore;
import com.magnifis.parking.utils.Utils;
import com.magnifis.parking.widgets.Widget_NfyMenu_Provider;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Hashtable;
import java.util.Locale;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import static com.magnifis.parking.utils.Utils.dump;
import static com.magnifis.parking.utils.Utils.isEmpty;

// MobiDeal SDK
//import com.mobideal.android.api.MobiDeal;
//import android.database.DatabaseErrorHandler;

public class App extends Application {

	static final String TAG=App.class.getName();
	
	public ScheduledThreadPoolExecutor tpx=new ScheduledThreadPoolExecutor(5);
	
	
    //Robin robin;
    private MovementRequester mr;
    private Robin robin;

    public void setRokuContext(ParserContext parserContext) {
        this.parserContext = parserContext;
    }

    public ParserContext getRokuContext() {
        return this.parserContext;
    }

    public ParserContext parserContext = null;

    public static boolean isBluetoothEnabled() {
		try {
		   BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
		   return (ba!=null)&&ba.isEnabled();
		} catch(Throwable t) {}
		return false;
	}

	public UiModeManager getUiModeManager() {
		return (UiModeManager)getSystemService(UI_MODE_SERVICE);
	}

	public AlarmManager getAlarmManager() {
		return  (AlarmManager)getSystemService(Context.ALARM_SERVICE);
	}

	@SuppressLint("NewApi")
	public boolean isInCarMode() {
        if (Config.car_mode_system && Utils.isAndroid22orAbove)
            return getUiModeManager().getCurrentModeType()==Configuration.UI_MODE_TYPE_CAR;
        else
		    return getBooleanPref("carMode");
	}

	@SuppressLint("NewApi")
	private void _setInCarMode(SharedPreferences.Editor prefsEditor,boolean carMode) {
			if (Config.car_mode_system && Utils.isAndroid22orAbove)   {
			    UiModeManager umm=getUiModeManager();
			    if (carMode) umm.enableCarMode(0); else umm.disableCarMode(0);
			} else
				prefsEditor.putBoolean("carMode", carMode);
 }

	public void switchCarMode(boolean carMode, boolean silently) {
		switchCarMode(carMode, true, silently);
	}

	//private volatile boolean skip_car_mode_speech=false;

	private String _savedCarModeOptName(String key) {
		return "cmo_"+key;
	}

	public void switchCarMode(boolean carMode, boolean fSet, boolean silently) {

		//if (fSet) MyTTS.abort();

		if (carMode) {
			SuziePopup sp = SuziePopup.get();
			if (sp != null)
				sp.returnToScreen();
            SharedPreferences.Editor prefsEditor = getPrefs().edit();
			if (fSet)
                _setInCarMode(prefsEditor, true);
            prefsEditor.commit();

			if (fSet && !silently) {
				MyTTS.speakText(R.string.P_tray_car_hint_on1);
			}
		} else {
			SuziePopup sp = SuziePopup.get();
			if (sp != null)
				sp.startAutoHideTimer();
			if (fSet) {
                SharedPreferences.Editor prefsEditor = getPrefs().edit();
                _setInCarMode(prefsEditor, false);
                prefsEditor.commit();
            }
			if (fSet && !silently) MyTTS.speakText(R.string.P_tray_car_hint_off);
		}

        SuziePopup sp = SuziePopup.get();
        if (sp != null)
            sp.checkButtonSize();

		Widget_NfyMenu_Provider.updateWidgets();
	}

	public void hideNfyScreen() {
		Object sbservice = getSystemService( "statusbar" );
        Class<?> statusbarManager;
		try {
			statusbarManager = Class.forName( "android.app.StatusBarManager" );
			try { // b4 4.3
	          Method hidesb = statusbarManager.getMethod( "collapse" );
	          hidesb.invoke( sbservice );
	          return;
			} catch(Throwable t) {}
			try { // after 4.3
		      Method hidesb = statusbarManager.getMethod( "collapsePanels" );
		      hidesb.invoke( sbservice );
			} catch(Throwable t) {}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private  Hashtable<Integer,RunningInActivity> toRunInActivity=new Hashtable<Integer,RunningInActivity>();

	public RunningInActivity getToRunInActivity(Integer key) {
		return toRunInActivity.get(key);
	}

	public void setToRunInActivity(RunningInActivity _toRunInActivity) {
		toRunInActivity.put(System.identityHashCode(_toRunInActivity), _toRunInActivity);
	}

	public void clearToRunInActivity(RunningInActivity _toRunInActivity) {
		toRunInActivity.remove(_toRunInActivity);
	}

	public boolean isGreetingDisabled() {
		return App.self.getBooleanPref("greetingOff");
	}

	public Props getProps() {
	  return Props.getInstance(this);
	}

	public int getCountExecution() {
		try {
			return Integer.parseInt(getProps().getProperty(MainActivity.COUNT_EXECUTION, "0"));
		} catch (Throwable t) {
		}
		return 0;
	}

	private Message lastMessageRead=null;

	protected boolean inLandscape;

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		boolean f=isInLanscapeMode();
		if (inLandscape!=f) {
			Log.d(TAG,"rotation");
			inLandscape=f;
			scaler.initScaling(getStatusBarHeight());
		}

		Log.d(TAG, "onConfigurationChanged");
	}

	public Message getLastMessageRead() {
		return lastMessageRead;
	}

	public void setLastMessageRead(Message lastMessageRead) {
		this.lastMessageRead = lastMessageRead;
	}

	//////////////////////////////////////////////////////////////////
	public <T extends View> T createFromLayout(int layoutId) {
		LayoutInflater inflater=LayoutInflater.from(this);
		return (T)inflater.inflate(layoutId, null);
	}

	///////////////////////////////////////////////////////////////

	public  String getSimCountryIso() {
	  try {
	    TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
	    if (tm!=null) return tm.getSimCountryIso();
	  } catch(Throwable t) {}
	  return null;
	}

	public boolean shouldUseSuzie() {
	   return Config.floatingButton&&getSuzieButtonEnabled();
	}

	public boolean getSuzieButtonEnabled() {
	   return getBooleanPref(PrefConsts.PF_FL_BUTTON) || !SuziePopup.bublesEnabled || Config.roku_version;
	}

	public String getStringPref(String key) {
	   if (Utils.isOneFrom(key, Config.hiddenPrefs))
					   return prefsDefaults.getStringDefault(key);
	   return getPrefs().getString(key, prefsDefaults.getStringDefault(key));
	}

	public void setStringPref(String key, String value) {
	   	Editor ed=getPrefs().edit();
	   	ed.putString(key, value);
	   	ed.commit();
	}

	public void setStringPref(int key, String value) {
		setStringPref(getString(key),value);
	}

	public String getStringPref(int key) {
	   return getStringPref(getString(key));
	}

	public int getIntPref(String key) {
	   if (Utils.isOneFrom(key, Config.hiddenPrefs))
			   return prefsDefaults.getIntDefault(key);
	   return getPrefs().getInt(key, prefsDefaults.getIntDefault(key));
	}

	public int getIntPref(int key) {
	   return getIntPref(App.self.getString(key));
	}

	public void setBooleanPref(int key, boolean value) {
		setBooleanPref(getString(key),value);
	}

	public boolean switchBooleanPref(String key) {
		boolean val=!getBooleanPref(key);
		Editor ed=getPrefs().edit();
		ed.putBoolean(key, val);
		ed.commit();
		return val;
	}

	public boolean switchBooleanPref(int key) {
		return switchBooleanPref(getString(key));
	}

	public void setBooleanPref(String key, boolean value) {
		Editor ed=getPrefs().edit();
		ed.putBoolean(key, value);
		ed.commit();
	}

	public boolean getBooleanPref(String key) {
	   if (Utils.isOneFrom(key, Config.hiddenPrefs))
		   return prefsDefaults.getBooleanDefault(key);
	   boolean bv = getPrefs().getBoolean(key, prefsDefaults.getBooleanDefault(key));
	   return bv;
	}

	public boolean getBooleanPref(int key) {
	   return getBooleanPref(getString(key));
	}

	public boolean shouldUseProximitySensor() {
	   return Robin.isRobinRunning && App.self.getBooleanPref("handwaving");
	}

	public boolean shouldAdvertInSms() {
	  return !getBooleanPref(PfSmsExcludeSignature);
	}

	public boolean shouldReadSmsInstantly() {
		return getBooleanPref(PrefConsts.PF_SMS_READ_INSTANTLY);
	}

	public boolean shouldSpellSmsSenderPhones() {
		return getBooleanPref(PrefConsts.PF_SMS_SPELL_UNKNOWN_PHONES);
	}

	public boolean shouldSpellCallerPhones() {
		return getBooleanPref(PrefConsts.PF_CALLER_SPELL_UNKNOWN_PHONES);
	}

	public boolean shouldUseBluetooth() {
		return Config.bt && Utils.isAndroid4orAbove && getBooleanPref(PrefConsts.PF_BLUETOOTH);
	}

	public boolean shouldSpeakIncomingSms() {
		return getBooleanPref(PrefConsts.PF_SMS_NOTIFY) && Robin.isRobinRunning;
	}

	public boolean isPhoneLocked() {
	   KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
	   return km.inKeyguardRestrictedInputMode();
	}

	public boolean isScreenOn() {
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		if (pm == null)
			return false;
		return pm.isScreenOn();
	}

	public boolean isInSilentModeOrConversation() {
		return isInPhoneConversation()||isInSilentMode();
	}

	public boolean isInPhoneConversation() {
		TelephonyManager tm=(TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getCallState()!=TelephonyManager.CALL_STATE_IDLE;
	}

	public boolean isInSilentMode() {
		AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

		switch (am.getRingerMode()) {
		    case AudioManager.RINGER_MODE_SILENT:
		    case AudioManager.RINGER_MODE_VIBRATE:
               return true;
		}

		return false;
	}




	public String getPhoneCountryCode() {
		try {
			TelephonyManager tm=(TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
			if (tm!=null) {
				return tm.getNetworkCountryIso();
			}
		} catch(Throwable t) {}
		return null;
	}


    @Override
	public SQLiteDatabase openOrCreateDatabase(String name, int mode, CursorFactory factory) {
    	SQLiteDatabase db=null;
    	try {
    	  db=super.openOrCreateDatabase(name, mode, factory);
    	} catch(Throwable t) {
           Log.d(TAG,"the system can not create databse on sdcard");
    	}
    	if (db==null&&name.indexOf(File.separator)>=0) try {
    		File f=new File(name);
    		if (Utils.createFolderForFile(f)!=null) {
    			db=SQLiteDatabase.openOrCreateDatabase(name, factory);
    		}
    	} catch(Throwable t) {
      	  t.printStackTrace();
      	}
    	return db;
	}
/*
	@SuppressLint("NewApi") @Override
	public SQLiteDatabase openOrCreateDatabase(String name, int mode, CursorFactory factory, DatabaseErrorHandler errorHandler) {
    	SQLiteDatabase db=null;
    	try {
    	  db=super.openOrCreateDatabase(name, mode, factory, errorHandler);
    	} catch(Throwable t) {
           Log.d(TAG,"the system can not create databse on sdcard");
    	}
    	if (db==null&&name.indexOf(File.separator)>=0) try {
    		File f=new File(name);
    		if (Utils.createFolderForFile(f)!=null) {
    			db=SQLiteDatabase.openOrCreateDatabase(name, factory, errorHandler);
    		}
    	} catch(Throwable t) {
      	  t.printStackTrace();
      	}
    	return db;
    }
*/
	public String getVoiceMailNumber(){
    	TelephonyManager tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
    	String phoneNumber = tMgr.getVoiceMailNumber();
    	return phoneNumber;
    }

	private WeakReference<Activity> activeActivity=null;
	private Object activeActivitySO=new Object();

	public void setActiveActivity(Activity act) {
		synchronized(activeActivitySO) {
		  activeActivity=new WeakReference<Activity>(act);

            act.setVolumeControlStream(VR.TtsAudioStream);
/*
			VR vr = VR.get();
			if (activeActivity != null && vr != null)
				vr.mute();
*/
		  if (isSuzieRequied()) {
			  Log.d(SuziePopup.TAG, "SUZIE shown when activated "+act);
              SuzieService.showSuzieNow();
		  }
          else {
              Log.d(SuziePopup.TAG, "SUZIE hidden when activated "+act);
              if (activeActivity != null && (act instanceof MainActivity))
                SuzieService.hideSuzie(SuziePopup.HideAnimation.ToMainActivity);
              else
                SuzieService.hideSuzie(SuziePopup.HideAnimation.None);
          }
		}
	}

	public void removeActiveActivity(Activity act) {
		synchronized(activeActivitySO) {
			if (act==getActiveActivity()) {
				activeActivity=null;

				SuzieHints.disableHints();
				/*
				VR vr = VR.get();
				if (getActiveActivity() == null && vr != null)
					vr.unMute();
				*/
				if (shouldUseSuzie()) {
					Log.d(SuziePopup.TAG, "SUZIE shown after close "+act);
                    SuzieService.showSuzieNow();
				}
                else {
                    Log.d(SuziePopup.TAG, "SUZIE hidden after close "+act);
                    SuzieService.hideSuzie(SuziePopup.HideAnimation.None);
                }
			}
		}
	}

	public Activity getCurrentActivity() {
		Activity ac=getActiveActivity();
		return ac==null?MainActivity.get():ac;
	}

	public Activity getActiveActivity() {
		synchronized(activeActivitySO) {
		  return activeActivity==null?null:activeActivity.get();
		}
	}

    public boolean isMainActivityActive() {
        return MainActivity.isActive();
    }

    public void notifyStopActivity(Activity act) {
	}

	// App object takes a very small amount of memory
	public static App self;

	public boolean isInLanscapeMode() {
		return getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE;
	}

	public int toPxSize(int dpSize) {
		return (int) (dpSize * getResources().getDisplayMetrics().density  / 0.5f);
	}

	public int [] getShortLongDspSizes() {
	   DisplayMetrics dm=getDisplayMetrics();
	   return new int [] {
		  Math.min(dm.widthPixels, dm.heightPixels),
		  Math.max(dm.widthPixels, dm.heightPixels)
	   };
	}

	public DisplayMetrics getDisplayMetrics() {
	   return getResources().getDisplayMetrics();
	}


	public String getDspResolutionString() {
	  DisplayMetrics dm=getDisplayMetrics();
	  return dm.widthPixels+"x"+dm.heightPixels+" dpi="+dm.densityDpi;
	}


	public boolean isReleaseBuild() {
		PackageInfo packageInfo = getPackageInfo(PackageManager.GET_SIGNATURES);
		if (!isEmpty(packageInfo.signatures)) {
			String signature = new String(packageInfo.signatures[0].toByteArray());
			return !signature.contains("Android Debug");
		}
		return false;
	}



	public boolean isReleaseBuild=false, isVwVersion=false, useOnlyGmail=false, isSamsungVersion=false, useBt=false;
	private String lastLocale = null;
	private Locale lastLoc = null;


	private TheDownloaderClient theDownloaderClient=null;
	private Object theDownloaderClientSO=new Object();

	public TheDownloaderClient createExpansionDownloderClient() {
		synchronized(theDownloaderClientSO) {
		  if (theDownloaderClient==null) theDownloaderClient=new TheDownloaderClient();
		  return theDownloaderClient;
		}
	}

	public boolean expDownloadingInProgress() {
		TheDownloaderClient edc=theDownloaderClient;
		return edc!=null&&edc.isWorking();
	}

	public TheDownloaderClient getExpansionDownloderClient() {
		synchronized(theDownloaderClientSO) {
		  return theDownloaderClient;
		}
	}

	public void releaseExpansionDownloderClient() {
		synchronized(theDownloaderClientSO) {
		   if (theDownloaderClient!=null) {
			   theDownloaderClient.abortDownloadAndReleaseTheService();
			   theDownloaderClient=null;
		   }
		}
	}

	public String android_id = "id_uninitialized_yet";

	public int getStatusBarHeight() {
		int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0)
			return getResources().getDimensionPixelSize(resourceId);

		return 0;
	}

	public Scaler scaler=new Scaler();

	private StateStore<Understanding> widgetStateSS;

	public StateStore<Understanding> getWidgetSS() {
		return widgetStateSS;
	}

	public int MT_GMAIL, MT_YAHOO;
	public int powerStatus = 0;

	public ActivityManager getActivityManager() {
		return (ActivityManager)App.self.getSystemService(Context.ACTIVITY_SERVICE);
	}

	public AudioManager getAudioManager() {
	  return (AudioManager)getSystemService(Context.AUDIO_SERVICE);
	}

	public TelephonyManager getTelephonyManager() {
	  return (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
	}

	private String PfSmsExcludeSignature;
	private PrefDefaults prefsDefaults=null;

	@Override
	public void onCreate() {
		super.onCreate();
		MT_GMAIL=getInt(R.integer.MtGmail);
		MT_YAHOO=getInt(R.integer.MtYahoo);
		prefsDefaults=new PrefDefaults(R.xml.preferences);
		widgetStateSS=new StateStore<Understanding>("widget_state");
		android_id=Secure.getString(getContentResolver(),Secure.ANDROID_ID);
		isReleaseBuild=isReleaseBuild();
		isVwVersion=Config.vwversion;
		isSamsungVersion=Config.samsungversion;
		useBt=Config.bt;
		useOnlyGmail=Config.gmail_only;
		setLocale(getResources());
		if (isReleaseBuild) Log.d(TAG, "release build");
		inLandscape=isInLanscapeMode();
		scaler.initScaling(getStatusBarHeight());
		PfSmsExcludeSignature = getString(R.string.PfSmsExcludeSignature);

		/*if (App.self.shouldPlaceActivationIcon())
			Tray.placeActivationIcon();*/


		// if first start: select lang in settings
		if (!isStringPrefDefined("lang")) {
			// save new settings
			SharedPreferences prefs = getPrefs();
			SharedPreferences.Editor prefsEditor = prefs.edit();
			prefsEditor.putString("lang", getString(R.string.P_shortlang));
			prefsEditor.commit();
		}

		if (Utils.isBooleanPrefNotSet(PrefConsts.PF_METRIC_SYSTEM))
			setBooleanPref(PrefConsts.PF_METRIC_SYSTEM, MeasurementSystem.detectIfMetricSystem());


        Utils.updateUserDictionary();

        UserLocationProvider.start();

        // set myTTS voice
		MyTTS.setVoice(getCurrentVoice());

		// Mobideal SDK
		// final String mobidealSdkKey = "298153e4-eed4-4d1b-be4b-6f692091581f";
		// boolean debug = true; // TODO: change prior to release
		// MobiDeal.getInstance().init(getApplicationContext(), mobidealSdkKey, debug);
		/*
		try {
		   new MainActivity();
		} catch(Throwable t) {
			t.printStackTrace();
		}*/

		//SuzieService.showSuzie(null);

		SuzieService.sendSuzie(SuzieService.ApplicationMonitorAction.INITIALIZATION);


        // turn on car mode detection
        // Check for Google Play services
        if (0 == GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) && Utils.isAndroid41orAbove){
            if (mr == null){
                mr = new MovementRequester(this);
            }
            // Pass the update request to the requester object
            mr.requestUpdates();
        }

        // billing
        if (Config.billing) {
            mBilling = new Billing();
            mBilling.start(null);
        }
    }

    public Billing mBilling = null;

	public NotificationManager getNotificationManager() {
	  return (NotificationManager)getSystemService(Activity.NOTIFICATION_SERVICE);
	}

    static public void activateApp() {
        if (App.self.isInPhoneConversation()
            || App.self.isPhoneLocked()
            || !App.self.isScreenOn()
            || !Robin.isRobinRunning
            )
            return;

        
        if (SuzieService.isSuzieVisible()) {
	        VR vr = VR.get();
	        if (vr != null)
	            vr.start(false);//startSoundWaiting();
        }

        Vibrator v = (Vibrator) self.getSystemService(Context.VIBRATOR_SERVICE);

        if (v != null) v.vibrate(50);

        if (SuzieService.isSuzieVisible()) {
            return;
        }

        Intent it = new Intent(MainActivity.WAKE_UP);

        it.setClass(App.self, MainActivity.class);

        Activity ac=App.self.getActiveActivity();
        com.magnifis.parking.Log.i(TAG, "Firing up MainActivity mic...");
        if (ac!=null) {
            ac.startActivity(it);
            return;
        } 

        Utils.startActivityFromNowhere(it);
    }

	@Override
	public Resources getResources() {
		Resources res=super.getResources();
		setLocale(res);
		return res;
	}


	public Locale getResLocale() {
	   Configuration c = getResources().getConfiguration();
	   return c==null?null:c.locale;
	}

	public String getLanguage() {
	  return getStringPref("lang");
	}

	public void setLanguage(String lang) {
       setStringPref("lang", lang);
       reloadResources();
	}

	public void reloadResources() {
	   setLocale(getResources());
	}

	public boolean setCurrentVoice(int voice) {
	  MyTTS.setVoice(voice);
	  String key=Utils.getString(R.string.PfVoiceType);
      String sVoice=getPrefs().getString(key,null);
      boolean ok=sVoice==null;
      if (!ok) try {
    	ok=Integer.parseInt(sVoice)!=voice;
      } catch (Throwable t) {}
      if (ok) { // do set
    	 Editor ed=getPrefs().edit();
    	 ed.putString(key, Integer.toString(voice));
    	 ed.commit();
    	 return true;
      }
      return false;
	}

	public boolean isStringPrefDefined(Object key) {
		return getPrefs().getString(Utils.getString(key), null)!=null;
	}

	public int getCurrentVoice() {
	  try {
	    return Integer.parseInt(getStringPref(R.string.PfVoiceType));
	  } catch( Throwable t) {}
	  return MyTTS.FEMALE_VOICE;
	}

	public boolean isInRussianMode() {
	   String lng=getLanguage();
	   if (lng!=null&&lng.length()>=2) {
		  lng=lng.toLowerCase();
		  return lng.charAt(0)=='r'&&lng.charAt(1)=='u';
	   }
	   return false;
	}

	public void setLocale(Resources res) {

		String newLocale = null;
		Locale newLoc=null;

		try {
			newLocale = Locale.getDefault().getLanguage().toLowerCase().substring(0, 2);
		} catch (Exception e) { newLocale = "en"; }

		String x = App.self.getPrefs().getString("lang", newLocale);
		if (!Utils.isEmpty(x))
			newLocale = x;

		if (newLocale.equals("en")) {
			Locale sysloc=Locale.getDefault();
			if (sysloc != null) {
				String lng=sysloc.getLanguage();
				if (lng!=null && lng.toLowerCase().startsWith("en"))
					newLoc = sysloc;
			}
			if (newLoc == null)
				newLoc = Locale.US;
		}
		else {
			newLoc = new Locale(newLocale);
			if (newLoc == null || Utils.isEmpty(newLoc.getLanguage()))
				newLoc = Locale.US;
		}

		newLocale = newLoc.getLanguage().toLowerCase().substring(0, 2);

		Configuration config = res.getConfiguration();

		//if (newLocale.equals(config.locale.getLanguage()))
			//return;

		lastLocale = newLocale;
		lastLoc = newLoc;

		if (config==null) {
		  config = new Configuration();
		  Log.d(TAG, "********* NEW LOCALE "+newLocale);
		} else {
	//	  Log.d(TAG, "********* UPDATE LOCALE "+newLocale);
		  if (config.locale!=null&&config.locale.equals(newLoc))
			  return;
		}

		Locale.setDefault(newLoc);
		config.locale = newLoc;

		res.updateConfiguration(config,res.getDisplayMetrics());

        robin = new Robin();
	}

	private static void loadMissedClassed() {
	  try {
		Class c=Utils.tryToLoad("android.security.MessageDigest");
		if (c==null) {
			URL cu=App.class.getResource("/res/mdlib.jar");
			if (cu!=null) {
				URLClassLoader ucl=new URLClassLoader(new URL[] {cu});
				try {
					c=ucl.loadClass("android.security.MessageDigest");
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	  } catch(Throwable x) {}
	}

	public App() {
		self=this;
		Log.d(TAG, "*********************** created *********************************");

		//loadMissedClassed();
		/*
	    Log.d(TAG,"android.security.MessageDigest "+tryToLoad("android.security.MessageDigest"));
	    try {
			App.class.forName("com.google.android.maps.MapActivity");
		} catch (ClassNotFoundException e) {
			Throwable t=e.getCause();
			if (t!=null) {
				Log.d(TAG,"cause");
				t.printStackTrace();
			}
		}*/
	}


	public DoublePoint  getUserLocationDP() {
		Location loc=getUserLocation();
		return loc==null?null:new DoublePoint(loc);
	}

	public Location getUserLocation() {
        return UserLocationProvider.queryLocation().location;
    }

	public boolean anyProximitySensor() {
		SensorManager mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
    	return mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)!=null;
	}

	public void getPrefDefaults() {

	}


	public SharedPreferences getPrefs() {
		return PreferenceManager
				.getDefaultSharedPreferences(this.getBaseContext());
	}

	public boolean hasPermission(String permName) {
		PackageManager pm=getPackageManager();
		return pm.checkPermission(permName, getPackageName())==PackageManager.PERMISSION_GRANTED;
	}

	public PackageInfo getPackageInfo() {
		return getPackageInfo(0);
	}

	public PackageInfo getPackageInfo(int flags) {
		PackageManager pm=getPackageManager();
		try {
			return pm.getPackageInfo(getPackageName(), flags);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Account [] getGoogleAccounts() {
	  AccountManager am = AccountManager.get(this);
	  return  am.getAccountsByType(Consts.AT_GOOGLE);
	}


	public static final String GOOGLE_SCOPE =
			//"oauth2:https://mail.google.com/mail/feed/atom";
			"oauth2:https://mail.google.com/" ;
			//"mail_service";
	
	

	/*
	private static final String[] FEATURES_MAIL = {
	        "service_mail"
	};*/

	public String peekGoogleToken(Account account) {

		AccountManager am = AccountManager.get(this);
		AccountManagerFuture<Bundle> accountManagerFuture=
				am.getAuthToken(account,GOOGLE_SCOPE,false,null,null);
        Bundle authTokenBundle;
		try {
			authTokenBundle = accountManagerFuture.getResult();
			if (authTokenBundle!=null) {
				dump(TAG, authTokenBundle);
			   return authTokenBundle.getString(AccountManager.KEY_AUTHTOKEN);
			}
		} catch (OperationCanceledException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AuthenticatorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return null;
	}

	public void invalidateGoogleToken(String authToken) {
	   AccountManager am = AccountManager.get(this);
	   am.invalidateAuthToken(Consts.AT_GOOGLE, authToken);
	}

	public String updateGoogleToken(Account account, Activity activity, boolean invalidateToken) {
	    String authToken ="null";
	    try{
	    	Bundle options=new Bundle();
	    	//options.putString("client_id", "1041048675389.apps.googleusercontent.com");
	    	//options.putString("client_secret", "SzbcmimbKx0fYgonS1iQvU3p");

	    	AccountManager am = AccountManager.get(this);
	        AccountManagerFuture<Bundle> accountManagerFuture;
	        if (activity ==null) {//this is used when calling from an interval thread
	            accountManagerFuture = am.getAuthToken(account,GOOGLE_SCOPE,false,null,null);
	        } else{
	            accountManagerFuture = am.getAuthToken(account,GOOGLE_SCOPE,options, activity,null,null);
	        }
	        Bundle authTokenBundle = accountManagerFuture.getResult();
	        authToken = authTokenBundle.getString(AccountManager.KEY_AUTHTOKEN);
	        if (invalidateToken) {
	            am.invalidateAuthToken(Consts.AT_GOOGLE, authToken);
	            authToken = updateGoogleToken(account,null, false);
	        }
	    }catch(Exception e) {
	        e.printStackTrace();
	    }
	    return authToken;
	}

	public Integer getIntPreference(String id) {
		SharedPreferences prefs=getPrefs();
		return prefs.contains(id)?prefs.getInt(id, -1):null;
	}

	public boolean isServiceRunning(Class<? extends Service> sc) {
	    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (sc.getName().equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}

	public int getInt(int id) {
	   Resources res=this.getResources();
	   return res.getInteger(id);
	}

	public int getMailAccountType() {
		if (!useOnlyGmail) {
			SharedPreferences prefs=getPrefs();
			try {
				return prefs.getInt(getString(R.string.PfMailType), MT_GMAIL);
			} catch(Throwable t) {}
		}
		return MT_GMAIL;
	}


	public Account getGmailAccount() {
		SharedPreferences sp=getPrefs();
		String key=getString(R.string.PfGmailAccount);
		String gma=getStringPref(key);

		Account acs[]=getGoogleAccounts();
		if (!isEmpty(acs)) {
			if (!isEmpty(gma)) for (Account ac:acs) if (gma.equals(ac.name)) return ac;
			gma=acs[0].name;
			Editor ed=sp.edit();
			ed.putString(key, gma);
			ed.commit();
			return acs[0];
		}
		gma=null;
		Editor ed=sp.edit();
		ed.remove(key);
		ed.commit();

		return null;
	}

	public String getGmailAccountName() {
        Account ac=getGmailAccount();
        return ac==null?null:ac.name;
	}

	public boolean isSuzieRequied() {
		Activity aa=getActiveActivity();
		return aa == null||((aa instanceof RequiresSuzie)&&((RequiresSuzie)aa).isRequiringSuzie());
	}


    public VoiceIO voiceIO=new VoiceIO();

    public boolean isCharging() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, ifilter);
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean bCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
        return bCharging;
    }

    public Robin robin() {

        if(robin==null){
            robin = new Robin();
        }

        return robin;
    }
}
