package com.magnifis.parking.suzie;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.speech.RecognizerIntent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.format.DateUtils;
import android.util.Log;

import com.magnifis.parking.Abortable;
import com.magnifis.parking.AborterHolder;
import com.magnifis.parking.App;
import com.magnifis.parking.Communitainment;
import com.magnifis.parking.Config;
import com.magnifis.parking.MainActivity;
import com.magnifis.parking.MultipleEventHandler;
import com.magnifis.parking.MultipleEventHandler.EventSource;
import com.magnifis.parking.ProgressIndicatorHolder;
import com.magnifis.parking.R;
import com.magnifis.parking.RequestFormers;
import com.magnifis.parking.ResultProxyActivity;
import com.magnifis.parking.Robin;

import com.magnifis.parking.SmsActivity;
import com.magnifis.parking.UserLocationProvider;
import com.magnifis.parking.VR;
import com.magnifis.parking.VoiceIO;
import com.magnifis.parking.ads.AdManager;
import com.magnifis.parking.cmd.etc.CmdHandlerHolder;
import com.magnifis.parking.cmd.i.ActivityResultHandler;
import com.magnifis.parking.cmd.i.IHandlesAbortInAnyCase;
import com.magnifis.parking.cmd.i.IIntentHandler;
import com.magnifis.parking.cmd.i.LocalCommandHandler;
import com.magnifis.parking.db.RobinDB;
import com.magnifis.parking.model.DlStat;
import com.magnifis.parking.model.PushAd;
import com.magnifis.parking.model.RobinProps;
import com.magnifis.parking.model.Understanding;
import com.magnifis.parking.model.UnderstandingStatus;
import com.magnifis.parking.phonebook.CalleeAssocEngine;
import com.magnifis.parking.phonebook.PhoneBook;

import com.magnifis.parking.tts.MyTTS;
import com.magnifis.parking.utils.Analytics;
import com.magnifis.parking.utils.Utils;

import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.magnifis.parking.VoiceIO.fireOpes;
import static com.magnifis.parking.VoiceIO.sayAndShow;

/*
 
  service to show and hide floating button
  use methods: showSuzie, hideSuzie, sendSuzie
 
 */
public class SuzieService extends Service
        implements ProgressIndicatorHolder, AborterHolder {

    Analytics analytics;

    final static String TAG = "suzie";

    private TelephonyManager _telephonyMgr = null;

    private PhoneStateListener _phoneStateListener = null;
    private SuziePopup _suzieButton;

    public SuziePopup getSuziePopup() {
        return _suzieButton;
    }

    @SuppressWarnings("unused")
    private Handler handler;

    public static enum ApplicationMonitorAction {
        ABORT, OPEN_SUZIE, CLOSE_SUZIE, BUSY_SUZIE, FREE_SUZIE,
        START_IN_FOREGROUND,
        PUT_TO_BACKGROUND,
        CANCEL_CALLEE_ASSOCIATION,
        ANSWER_BUBBLE,
        MESSAGE_BUBBLE,
        SWITCH_CAR_MODE, CAR_MODE_OFF, CAR_MODE_ON,
        INITIALIZATION, NEVER_ALERT_CAR_MODE, SHUT_DOWN,
        NOTIFY_COMMUNITAINMENT, RUN_COMMUNITAINMENT_FROM_PLAY_BUTTON, CANCEL_COMMUNITAINMENT, RUN_COMMUNITAINMENT_FROM_CONTENT_INTENT, NEVER_ALERT_COMMUNITAINMENT
    }
    // commands from tray
    //public static final String SWITCH_BUTTON = "com.magnifis.parking.SWITCH_BUTTON";
    //public static final String SWITCH_NOTIFY_CALLS = "com.magnifis.parking.SWITCH_NOTIFY_CALLS";
    //public static final String SWITCH_NOTIFY_SMS = "com.magnifis.parking.SWITCH_NOTIFY_SMS";
    //public static final String SWITCH_HAND = "com.magnifis.parking.SWITCH_HAND";
    //public static final String CONFIRM_CLOSE_TRAY = "com.magnifis.parking.CONFIRM_CLOSE_TRAY";

    public static final String WO_ANIMATION = "WO_ANIMATION";

    PhoneBook phoneBook = null;

    @Override
    public void onCreate() {
        handler = new Handler();

        _suzieButton = new SuziePopup(this);

        sendBroadcast(new Intent("COM.MAGNIFIS.PARKING.SERVICE_UP"), null);

        analytics = new Analytics(this);

        Log.d(TAG, "service created " + this);
    }

    @Override
    public void onDestroy() {
        if (_telephonyMgr != null && _phoneStateListener != null) {
            _telephonyMgr.listen(_phoneStateListener, PhoneStateListener.LISTEN_NONE);
            _phoneStateListener = null;
        }

        Log.d(TAG, "service destroyed " + this + " !!!!!!!!!!!!!!!!!!!!!! ");
    }

	/*
    static public void stop() {
		Intent it = new Intent();
		it.setClass(App.self, SuzieService.class);
		App.self.stopService(it);
	}
	*/


    static public void startInForeground(Notification nfn, int id) {
        if (nfn == null)
            return;

        Intent serviceIntent = new Intent();
        serviceIntent.putExtra(NOTIFICATION, nfn);
        serviceIntent.putExtra(NOTIFICATION_ID, id);
        serviceIntent.setClass(App.self, SuzieService.class);
        serviceIntent.setAction(ApplicationMonitorAction.START_IN_FOREGROUND.name());
        App.self.startService(serviceIntent);
    }

    static public void stopRunningInForeground() {
        if (App.self.isServiceRunning(SuzieService.class)) {
            Intent serviceIntent = new Intent();
            serviceIntent.setClass(App.self, SuzieService.class);
            serviceIntent.setAction(ApplicationMonitorAction.PUT_TO_BACKGROUND.name());
            App.self.startService(serviceIntent);
        }
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        processInternalIntent(intent);

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    final public static String
            BUBBLE_TEXT = "BUBBLE_TEXT",
            NOTIFICATION = "nfn",
            NOTIFICATION_ID = "nfn_id",
            CALLEE_ASSOCIATION = "CALLEE_ASSOCIATION";


    public static void cancelCalleeAssociation(String phone, CalleeAssocEngine.Association assoc) {
        Intent it = new Intent(ApplicationMonitorAction.CANCEL_CALLEE_ASSOCIATION.name());
        it.setClass(App.self, SuzieService.class);
        it.putExtra(CALLEE_ASSOCIATION, assoc);
        it.putExtra(Intent.EXTRA_PHONE_NUMBER, phone);
        App.self.startService(it);
    }

    /**
     * show floating button
     * if button disabled in setting - nothing will shown
     */
    static public void showSuzieNow() {
        if (!Config.floatingButton || !Robin.isRobinRunning)
            return;

        Intent serviceIntent = new Intent();
        serviceIntent.setClass(App.self, SuzieService.class);
        serviceIntent.setAction(ApplicationMonitorAction.OPEN_SUZIE.name());
        App.self.startService(serviceIntent);
    }

    private static volatile CharSequence _bubble_answer_text = null;
    private static volatile SuziePopup.BubleMessage _bubble_message_text = null;

    public static void answerBubble(CharSequence s) {
        Intent it = new Intent(ApplicationMonitorAction.ANSWER_BUBBLE.name());
        it.setClass(App.self, SuzieService.class);
        //it.putExtra(BUBBLE_TEXT, s);
        _bubble_answer_text = s;
        App.self.startService(it);
    }

    public static void messageBubble(SuziePopup.BubleMessage s) {
        Intent it = new Intent(ApplicationMonitorAction.MESSAGE_BUBBLE.name());
        it.setClass(App.self, SuzieService.class);
        //it.putExtra(BUBBLE_TEXT, s);
        _bubble_message_text = s;
        App.self.startService(it);
    }

    // hide floating button
    static public void hideSuzie(SuziePopup.HideAnimation animation) {
        if (!Config.floatingButton)
            return;

        Intent it = new Intent();
        it.setClass(App.self, SuzieService.class);
        it.setAction(ApplicationMonitorAction.CLOSE_SUZIE.name());
        it.putExtra(WO_ANIMATION, animation.toString());
        App.self.startService(it);
    }

    private void _hideSuzie(SuziePopup.HideAnimation animation) {
        _suzieButton.hideSuzie(animation);
    }

    private void showSuzie() {
        if (_suzieButton == null)
            _suzieButton = new SuziePopup(this);

        _suzieButton.showSuzie();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        _suzieButton.updateOrientation();
    }

    // hide floating button
    static public void sendSuzie(ApplicationMonitorAction a) {
        if (!Config.floatingButton)
            return;

        Intent serviceIntent = new Intent();
        serviceIntent.setClass(App.self, SuzieService.class);
        serviceIntent.setAction(a.name());
        App.self.startService(serviceIntent);
    }

    static public boolean isSuzieVisible() {
        SuziePopup s = SuziePopup.get();
        if (s == null)
            return false;

        return s.isVisible();
    }

 

    //NfyOff nfyOff=new NfyOff(this);

    /**
     * @param intent
     * @return
     * @throws JSONException
     */
    private boolean processInternalIntent(final Intent intent) {
        if (intent == null) {
            return false;
        }

        String actionName = intent.getAction();
        if (actionName == null) {
            return false;
        }

        IIntentHandler inh = CmdHandlerHolder.getIntentHandler();
        if (inh != null && inh.onNewIntent(intent)) return true;


        if (ResultProxyActivity.ACTION_ACTIVITY_RESULT.equals(actionName)) {
            onActivityResult(
                    intent.getIntExtra(ResultProxyActivity.ACTIVITY_RESULT_REQUEST_CODE, 0),
                    intent.getIntExtra(ResultProxyActivity.ACTIVITY_RESULT_RESULT_CODE, 0),
                    (Intent) intent.getParcelableExtra(ResultProxyActivity.ACTIVITY_RESULT_INTENT)
            );
            return true;
        }

        if (MainActivity.INTERPRET_UNDERSTANDING.equals(actionName)) {
            Log.d(TAG, MainActivity.INTERPRET_UNDERSTANDING);
            Understanding u = (Understanding) intent
                    .getSerializableExtra(MainActivity.EXTRA_UNDERSTANDING);
            if (u != null)
                interpretMagnifisUnderstanding(u);
            return true;
        }

        if (Intent.ACTION_PACKAGE_ADDED.equals(actionName)) {
            Log.d(TAG, "ACTION_PACKAGE_ADDED:");
            Utils.dump(TAG, intent);

            int uid = intent.getIntExtra(Intent.EXTRA_UID, 0);
            if (uid != 0) {
                final PackageManager pm = App.self.getPackageManager();
                final String n = pm.getNameForUid(uid);
                if (!Utils.isEmpty(n)) try {
                    new Thread("Adv Tracker") {

                        @Override
                        public void run() {
                            RobinDB rdb = RobinDB.getInstance(App.self);
                            if (rdb != null) {
                                PushAd pa = rdb.getBy(n, PushAd.class);
                                if (pa != null) {
                                    Log.d(TAG, "found ");

                                    AdManager.sendToServer(n, AdManager.TRACKING_STATUS_CONVERTED);

                                    rdb.delete(PushAd.class, "package=?", n);

                                    // wait for launch
                                    int maxChecks = 5 * 60;
                                    boolean started = false;
                                    while (maxChecks-- > 0) {
                                        try {
                                            sleep(1000);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }

                                        if (Utils.isForegroundPackage(pa.getPushAdPackage())) {
                                            started = true;
                                            break;
                                        }
                                    }

                                    // start app
                                    if (!started) {
                                        Intent it = pm.getLaunchIntentForPackage(pa.getPushAdPackage());
                                        Utils.startActivityFromNowhere(it);
                                    }

                                    // save to DB
                                    if (started)
                                        AdManager.sendToServer(pa.getPushAdPackage(), AdManager.TRACKING_STATUS_STARTED);
                                    else
                                        AdManager.sendToServer(pa.getPushAdPackage(), AdManager.TRACKING_STATUS_STARTED_BY_ROBIN);


                                }
                            }
                        }

                    }.start();

                } catch (Throwable t) {
                }
            }
            return true;
        }

        if (MainActivity.WAKE_UP.equals(actionName)) {
            Log.i(TAG, "SuzieService: WAKE UP!");

            if (SuziePopup.isVisible()) {
                SuziePopup sp = _suzieButton;
                if (sp != null)
                    sp.onButtonClick();
            }
            return true;
        }

        Log.d(TAG, "Service intent: " + actionName);

        /*
        if (CONFIRM_CLOSE_TRAY.equals(actionName)) {
            App.self.hideNfyScreen();
            NfyOff.nfyOffWoConfirmation();
            return true;
        }

        if (SWITCH_BUTTON.equals(actionName)) {
            App.self.hideNfyScreen();
            MyTTS.abort();
            VR vr = VR.get();
            if (vr != null)
                vr.abort();
            Abortable a = CmdHandlerHolder.getAbortableCommandHandler();
            if (a != null)
                a.abort(0);
            if (App.self.switchBooleanPref(PrefConsts.PF_FL_BUTTON)) {
                showSuzie();
                MyTTS.speakText(R.string.P_tray_robin_hint_on);
                analytics.trackButtonPress("tray_floating_button_on");
            } else {
//			     MyTTS.speakText(new MyTTS.WithoutBubbles(R.string.P_tray_robin_hint_off));
                _hideSuzie(SuziePopup.HideAnimation.None);
                analytics.trackButtonPress("tray_floating_button_off");
            }
            if (App.self.shouldPlaceActivationIcon())
                Tray.placeActivationIcon();
            return true;
        }

        if (SWITCH_NOTIFY_CALLS.equals(actionName)) {
            MyTTS.abort();
            SharedPreferences.Editor prefsEditor = App.self.getPrefs().edit();
            if (!App.self.getBooleanPref("speakCallerName")) {
                MyTTS.speakText(R.string.P_tray_phone_hint_on);
                prefsEditor.putBoolean("speakCallerName", true);
                analytics.trackButtonPress("tray_notify_calls_on");
            } else {
//                MyTTS.speakText(R.string.P_tray_phone_hint_off);
                prefsEditor.putBoolean("speakCallerName", false);
                analytics.trackButtonPress("tray_notify_calls_off");
            }
            prefsEditor.commit();
            return true;
        }
        if (SWITCH_NOTIFY_SMS.equals(actionName)) {
            MyTTS.abort();
            SharedPreferences.Editor prefsEditor = App.self.getPrefs().edit();
            if (!App.self.getBooleanPref("SMSnotify")) {
                MyTTS.speakText(R.string.P_tray_sms_hint_on);
                prefsEditor.putBoolean("SMSnotify", true);
                analytics.trackButtonPress("tray_notify_sms_on");
            } else {
//                    MyTTS.speakText(R.string.P_tray_sms_hint_off);
                prefsEditor.putBoolean("SMSnotify", false);
                analytics.trackButtonPress("tray_notify_sms_off");
            }
            prefsEditor.commit();
            return true;
        }

        if (SWITCH_HAND.equals(actionName)) {
            //App.self.hideNfyScreen();
            MyTTS.abort();
            boolean f=!App.self.shouldUseProximitySensor();

            if(f){
                analytics.trackButtonPress("tray_notify_hand_wave_on");
                MyTTS.speakText(R.string.P_tray_hand_hint_on);
            }else{
                analytics.trackButtonPress("tray_notify_hand_wave_off");
            }

            App.self.setBooleanPref(PrefConsts.PF_HAND_WAVING, f);

            if (App.self.shouldUseProximitySensor()) {
                scs[0]=ProximityWakeUp.condStart();
            } else {
                ProximityWakeUp.stopIt(scs);
            }
            return true;
        }
        */

        ApplicationMonitorAction actCode = null;
        try {
            actCode = ApplicationMonitorAction.valueOf(actionName);
        } catch (Throwable t) {
        }

        if (actionName != null) {
            (new Analytics(App.self)).trackEvent("intent", "intent_action_fired", actionName);
        }

        if (actCode != null) switch (actCode) {

            case INITIALIZATION: {
                // to get country while app starting
                UserLocationProvider.readLocationPoint();

                try {
                    RobinDB.getInstance(this)
                            .insertOrIgnore(
                                    new RobinProps()
                                            .setKey("installation_timestamp")
                                            .setValue(Long.toString(System.currentTimeMillis()))
                            );
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                // send user email to server
                String s = App.self.getGmailAccountName();
                if (!Utils.isEmpty(s) && !App.self.getPrefs().getBoolean("spam", false)) {
                    SharedPreferences prefs = App.self.getPrefs();
                    SharedPreferences.Editor prefsEditor = prefs.edit();
                    prefsEditor.putBoolean("spam", true);
                    prefsEditor.commit();

                    StringBuilder url = new StringBuilder(getString(R.string.teaching_url));
                    try {
                        url.append("&spam=");
                        url.append(URLEncoder.encode(s, "UTF-8"));
                        url.append("&country=");
                        url.append(URLEncoder.encode(UserLocationProvider.getCountry()));
                        url.append("&androidID=");
                        url.append(URLEncoder.encode(App.self.android_id));
                        url.append("&lang=");
                        String newLocale = "en";
                        try {
                            newLocale = Locale.getDefault().getLanguage().toLowerCase().substring(0, 2);
                        } catch (Exception e) {
                        }
                        url.append(URLEncoder.encode(newLocale));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    final String goto_url = url.toString();

                    new Thread("save user email") {
                        @Override
                        public void run() {
                            try {
                                HttpURLConnection x = (HttpURLConnection) new URL(goto_url).openConnection();
                                x.getInputStream();
                                x.disconnect();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                }

            }
            break;
            case CANCEL_CALLEE_ASSOCIATION: {
                CalleeAssocEngine.Association assoc = (CalleeAssocEngine.Association) intent.getSerializableExtra(CALLEE_ASSOCIATION);
                String phone = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
                if (assoc != null && phone != null) {
                    Log.d(TAG, "should cancel assoc");
                    assoc.clear(phone);
                }
                ;
            }
            break;
            case ANSWER_BUBBLE: {
                if (_suzieButton == null)
                    _suzieButton = new SuziePopup(this);
                _suzieButton.bubleAnswer(/*intent.getCharSequenceExtra(BUBBLE_TEXT)*/_bubble_answer_text);
            }
            break;
            case MESSAGE_BUBBLE: {
                if (_suzieButton == null)
                    _suzieButton = new SuziePopup(this);
                _suzieButton.bubleMessage(/*intent.getCharSequenceExtra(BUBBLE_TEXT)*/_bubble_message_text);
            }
            break;
            case SWITCH_CAR_MODE: {
                boolean cond = !App.self.isInCarMode();
                Bundle params = intent.getExtras();
                if (params != null) {// explicit on/off flag
                    cond = params.getBoolean("flag", cond);
                }

                App.self.switchCarMode(cond, false);
            }
            break;
            case CAR_MODE_ON: {
                App.self.switchCarMode(true, true);
            }
            break;
            case CAR_MODE_OFF: {
                App.self.switchCarMode(false, true);
            }
            break;
            case OPEN_SUZIE:
                showSuzie();
                break;
            case ABORT:
                abortOperation(0);
                break;
            case CLOSE_SUZIE:
                _hideSuzie(SuziePopup.HideAnimation.valueOf(intent.getStringExtra(WO_ANIMATION)));
                break;
            case BUSY_SUZIE: {
                if (_suzieButton != null)
                    _suzieButton.showBusy();
            }
            break;
            case FREE_SUZIE: {
                if (_suzieButton != null)
                    _suzieButton.hideBusy();
            }
            break;
            case PUT_TO_BACKGROUND: {
                stopForeground(true);
                _hideSuzie(SuziePopup.HideAnimation.None);
            }
            break;
            case START_IN_FOREGROUND: {
                Notification nfn = intent.getParcelableExtra(NOTIFICATION);
                if (nfn != null) {
                    startForeground(intent.getIntExtra(NOTIFICATION_ID, 0), nfn);

                    if (phoneBook == null) phoneBook = PhoneBook.getInstance();
                    if (App.self.shouldUseSuzie())
                        SuzieService.showSuzieNow();
                }
            }
            break;
        /*
            case SWITCH_NOTIFY_CALLS_AND_SMS: {
                MyTTS.abort();
                SharedPreferences.Editor prefsEditor = App.self.getPrefs().edit();
                MyTTS.speakText(R.string.P_tray_sms_cals_hint_on);
                prefsEditor.putBoolean("SMSnotify", true);
                prefsEditor.putBoolean("speakCallerName", true);
                prefsEditor.commit();
            }
            break;
            */
            case NEVER_ALERT_CAR_MODE: {
                NotificationManager notificationManager =
                        (NotificationManager) App.self.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(Robin.OPEN_NOTIFICATION);

                Robin.shrdPrfs(App.self).edit().putLong(Robin.SP_LAST_CAR_MODE_ALERTS_NOTIFICATION, System.currentTimeMillis() + 3 * DateUtils.WEEK_IN_MILLIS).commit();
            }
            break;
            case SHUT_DOWN: {
                NotificationManager notificationManager =
                        (NotificationManager) App.self.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(Robin.OPEN_NOTIFICATION);

                Robin.shutdown();
            }
            break;

            //communitainment
            case NOTIFY_COMMUNITAINMENT: {
                Communitainment.notification(App.self);
            }
            break;
            case RUN_COMMUNITAINMENT_FROM_CONTENT_INTENT:
            case RUN_COMMUNITAINMENT_FROM_PLAY_BUTTON: {
                NotificationManager notificationManager =
                        (NotificationManager) App.self.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(Communitainment.COMMUNITAINMENT_NOTIFICATION);
                Communitainment.onCommunitainmentCalled(App.self);
            }
            break;
            case CANCEL_COMMUNITAINMENT: {
                NotificationManager notificationManager =
                        (NotificationManager) App.self.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(Communitainment.COMMUNITAINMENT_NOTIFICATION);
            }
            break;

        }
        return true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    ////////////////// SVIP
    class ProgressBarStopper extends MultipleEventHandler {
        @Override
        protected void onCompletion() {
            Log.d(TAG, "SuzieService.sendSuzie(SuzieService.ApplicationMonitorAction.FREE_SUZIE);");
            SuzieService.sendSuzie(SuzieService.ApplicationMonitorAction.FREE_SUZIE);
        }

        public EventSource newEventSource() {
            if (counter == 0) {
                Log.d(TAG, "SuzieService.sendSuzie(SuzieService.ApplicationMonitorAction.BUSY_SUZIE);");
                SuzieService.sendSuzie(SuzieService.ApplicationMonitorAction.BUSY_SUZIE);
            }
            return super.newEventSource();
        }

    }

    ;

    private ProgressBarStopper progressBarStopper = new ProgressBarStopper();

    @Override
    public EventSource showProgress() {
        return progressBarStopper.newEventSource();
    }


    public void onActivityResult(int requestCode, int resultCode, Intent it) {
        Log.d(TAG, "onActivityResult");
        ActivityResultHandler arh = CmdHandlerHolder.getActivityResultHandler();
        if (arh == null || !arh.onActivityResult(requestCode, resultCode, it)) ;
        switch (requestCode) {
            case VR.VOICE_RECOGNITION_REQUEST_CODE:
                if (it == null)
                    return;
                ArrayList<String> matches = it.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                ArrayList<String> old_matches = it.getStringArrayListExtra(VR.EXTRA_RESULT_NO_REPLACEMENT);

                LocalCommandHandler commandHandler = CmdHandlerHolder.getLocalCommandHandler();

                if (commandHandler != null) {
                    if (commandHandler.onVoiceInput(matches, resultCode == Activity.RESULT_FIRST_USER))
                        return;
                }

                if (matches == null)
                    return;

                String q = "";
                for (int i = 0; i < matches.size(); i++) {
                    q = matches.get(i);
                    Log.d(TAG + ".onActivityResult: ", q);
                    break;
                }
                if (_suzieButton != null) {
                    // partial results!
                    if (resultCode == Activity.RESULT_FIRST_USER)
                        ;//_suzieButton.bubleQueryPartial(q);
                    else
                        _suzieButton.bubleQuery(q, matches, old_matches);

                    // duplicate in MainActivity
                    MainActivity m = MainActivity.get();
                    if (m != null) {
                        // partial results
                        if (resultCode == Activity.RESULT_FIRST_USER)
                            m.bubbleQueryPartial(q);
                        else
                            m.bubbleQuery(q, matches, old_matches);
                    }

                    if (resultCode == Activity.RESULT_FIRST_USER)
                        return;

                    if (resultCode == VR.RESULT_RUN_INTENT) {
                        m.bubleAnswer(getString(R.string.P_launching_intent));
                        return;
                    }

                    Log.d(TAG, "SuzieVoiceInputProcessor: received VR results");


                    // TODO: for scripted scenarios only, keep in comments otherwise
//            		 if (MainActivity.get().isScripted && MainActivity.get().scriptedSeq != null) {
//            			 Outcome pair = MainActivity.get().scriptedSeq.getNext();
//            			 matches = new ArrayList<String>();
//            			 matches.add(pair.mgQuery);
//            		 }


                    Activity act = App.self.getActiveActivity();
                    if (act instanceof SmsActivity) {

                        ((SmsActivity) act).onActivityResult(requestCode, resultCode, it);

                    } else {
                        doUnderstanding(matches);
                    }
                }
        }
    }

    UnderstandingStatus us = UnderstandingStatus.getInstance();

    void doUnderstanding(final List<String> matches) {

        if (App.self.robin().canHandleResponseOffline(matches)) {
            return;
        }

        EventSource es = showProgress();
        try {
            SuzieUnderstandigProcessor up = new SuzieUnderstandigProcessor(this, es);
            VoiceIO.setCurrentUP(up);
            up.execute(
                    RequestFormers.createMagnifisUnderstandingRqUrl(this, matches),
                    null,
                    null
            );
        } catch (Throwable t) {
            es.fireEvent();
            t.printStackTrace();
        }
    }

    public void interpretMagnifisUnderstanding(Understanding u) {
        Log.d(TAG, "interpretMagnifisUnderstanding");
        MultipleEventHandler.EventSource es = showProgress();
        try {
            SuzieUnderstandigProcessor up = new SuzieUnderstandigProcessor(this,
                    es);
            VoiceIO.setCurrentUP(up);
            up.execute(u);
        } catch (Throwable e) {
            es.fireEvent();
            e.printStackTrace();
        }
    }


    private WeakReference<Runnable> aborter = null;

    @Override
    public void setAborter(Runnable abo) {
        aborter = new WeakReference<Runnable>(abo);
    }

    @Override
    public boolean abortOperation(int flags) {
        return abortOperation(false, flags);
    }

    boolean abortOperation(boolean fByMenuButton, int flags) {
        boolean ok = false;
        App.self.voiceIO.setAdvance(null);
        System.gc();
        if (aborter != null) {
            Runnable abo = aborter.get();
            if (ok = (abo != null)) {
                abo.run();
                aborter = null;
            }
        }
        boolean ok2 = MyTTS.abort(new Runnable() {

            @Override
            public void run() {
                fireOpes();
            }

        }, fByMenuButton);

        App.self.voiceIO.getOperationTracker().release(); // dirty

        boolean ok3 = VR.isListening();

        VR vr = VR.get();
        if (vr != null) vr.abort();


        if (ok2) flags |= Abortable.TTS_ABORTED;


        Abortable ab = CmdHandlerHolder.getAbortableCommandHandler();
        if (ab != null) {
            if (((flags & Abortable.BY_FB_CLICK) == 0) || (ab instanceof IHandlesAbortInAnyCase)) {
                ab.abort(flags);
                return true;
            }
        }


        return ok || ok2 || ok3;
    }


    public void onSuzieClick() {
        Log.d(TAG, "onSuzieClick()");

        VR vr = VR.get();

        vr.stop();

        //if (abortOperation(Abortable.BY_FB_CLICK)) return;

        App.self.voiceIO.listen(false);
        //doUnderstanding( Arrays.asList(new String [] {"call alex"}));
    }

}