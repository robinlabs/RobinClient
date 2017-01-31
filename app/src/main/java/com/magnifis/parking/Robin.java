package com.magnifis.parking;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.magnifis.parking.model.Understanding;
import com.magnifis.parking.pref.PasswordPreference;
import com.magnifis.parking.pref.PrefConsts;
import com.magnifis.parking.suzie.SuzieService;
import com.magnifis.parking.tts.MyTTS;
import com.magnifis.parking.utils.Analytics;
import com.magnifis.parking.utils.ClientParser;
import com.magnifis.parking.utils.Utils;
import com.robinlabs.persona.User;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.magnifis.parking.VoiceIO.sayAndShow;

/**
 * Created by oded on 1/28/14.
 */
public class Robin {

    private final Context context;
    private final PendingIntent wakeUpIntent;
    public Integer expectedResponse;

    int min = 0;
    final int MISSED_CALLS = 0;
    final int MEETINGS = 1;
    final int READ_EMAIL = 2;
    final int SET_EVENT = 3;
    final int VOCA = 4;
    final int LISTEN_MUSIC = 5;
    final int READ_NEWS = 6;
//    final int REPORT_TRAFFIC = 7;
    int max = 6;

    Map<Integer, String> actionMap;
    private Integer requestedCommand;

    public Robin() {
        this.context = App.self;

        actionMap = new HashMap<Integer, String>();

        actionMap.put(MISSED_CALLS, App.self.getString(R.string.suggest_recent_calls));
        actionMap.put(MEETINGS, App.self.getString(R.string.suggest_agenda));
        actionMap.put(READ_EMAIL, App.self.getString(R.string.suggest_gmail));
        actionMap.put(SET_EVENT, App.self.getString(R.string.suggest_event));
        //actionMap.put(LETS_MEET, App.self.getString(R.string.suggest_sms));
        actionMap.put(LISTEN_MUSIC, App.self.getString(R.string.suggest_music));
        actionMap.put(READ_NEWS, App.self.getString(R.string.suggest_news));
        actionMap.put(VOCA, App.self.getString(R.string.suggest_voca));
//        actionMap.put(REPORT_TRAFFIC, App.self.getString(R.string.suggest_traffic));

        wakeUpIntent = PendingIntent.getActivity(App.self, 0,
                new Intent(MainActivity.WAKE_UP,
                        null, App.self, MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT
        );
    }

    public String getNameOfUser() {
//        App.self.getGmailAccount().name
        String userName = null;
        /*try {
            userName = user.getString(User.NICK_NAME);
        } catch (JSONException e) {
            e.printStackTrace();
        }*/

        if (userName == null) {
            userName = App.self.getStringPref("learn:macro:username");
        }

        if (userName == null) {
            userName = getProfileUsername();
        }

        if (userName == null) {
            userName = context.getString(R.string.default_username);
        } /*else {
            try {
                user.put(User.FULL_NAME, userName);
                user.save();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }*/

        return userName;
    }

    public static void installVoca() {
        Launchers.launchPlayStore(App.self,
        		//"http://bit.ly/vocaly"); 
        		"https://play.google.com/store/apps/details?id=com.robinlabs.voca&referrer=utm_source%3Drobinapp%26utm_term%3Dsms"); 
        		//"market://details?id=com.robinlabs.voca");
    }

    public static boolean isVocaInstalled() {
        try {
            PackageManager pm = App.self.getPackageManager();
            pm.getPackageInfo("com.robinlabs.voca", PackageManager.GET_ACTIVITIES);
            int i=pm.getApplicationEnabledSetting("com.robinlabs.voca");
            return i==PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean startVoca(final String phone, final String name) {
        try {
            PackageManager pm = App.self.getPackageManager();
            pm.getPackageInfo("com.robinlabs.voca", PackageManager.GET_ACTIVITIES);

            if (phone == null || phone.length() < 1) {
                MyTTS.speakText(R.string.start_voca1);
                Intent it = pm.getLaunchIntentForPackage("com.robinlabs.voca");
                it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Utils.startActivityFromNowhere(it);
            }
            else {
                if (name == null) {
                    MyTTS.speakText(R.string.start_voca2);
                }
                else {
                    MyTTS.speakText(App.self.getString(R.string.start_voca3)+" "+name);
                }
                MyTTS.execAfterTheSpeech(new Runnable() {
                    @Override
                    public void run() {
                        Uri uri = Uri.parse("smsto:"+phone);
                        Intent it = new Intent(Intent.ACTION_SENDTO, uri);
                        it.setPackage("com.robinlabs.voca");
                        it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        Utils.startActivityFromNowhere(it);
                    }
                });
            }
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public int getNumberOfTimesUserSaidHello() {
        return shrdPrfs(context).getInt(ShrdPrfs.HI_COUNT, 0);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public String getProfileUsername() {

        if (Build.VERSION.SDK_INT < 14) {
            return null;
        }

        Cursor c;
        if (ContactsContract.Profile.CONTENT_URI == null) {
            return null;
        }

        c = context.getContentResolver().query(ContactsContract.Profile.CONTENT_URI, null, null, null, null);

        int count = 0;
        if (c == null) {
            return null;
        }
        count = c.getCount();
        String[] columnNames = c.getColumnNames();
        boolean b = c.moveToFirst();
        int position = c.getPosition();
        if (count == 1 && position == 0) {
            for (int j = 0; j < columnNames.length; j++) {
                String columnName = columnNames[j];
                String columnValue = c.getString(c.getColumnIndex(columnName));

                if (columnName.equals(ContactsContract.Profile.DISPLAY_NAME)) {

                    String[] actualName = null;
                    if (columnValue != null) {
                        actualName = columnValue.split(" ");
                    }

                    if (actualName != null && actualName.length > 1) {
                        return actualName[0];
                    }

                    return columnValue;
                }
            }
        }
        c.close();

        return null;
    }

    public String sayHello() {
        return context.getString(R.string.hello) + " " + getNameOfUser();
    }

    public String suggestSomething() {

        //count that the user said hello
        shrdPrfs(context).edit().putInt(ShrdPrfs.HI_COUNT, getNumberOfTimesUserSaidHello() + 1).commit();

        expectedResponse = Understanding.CMD_YES;

        int rand = min + (int) (Math.random() * ((max - min) + 1));
        if (rand == VOCA && (isVocaInstalled() || App.self.getBooleanPref("suggested_voca"))) {
            rand = READ_NEWS;
        }
        if (rand == VOCA) {
            App.self.setBooleanPref("suggested_voca", true);
        }

        requestedCommand = rand;

        String randomCommand = actionMap.get(rand);

        if (User.isPersonaOn) {
            JSONObject commute = null;
            try {
//                User user = App.self.user();TODO:bring back, when persona comes back
//            user.get(User.USER_OBJECT);
//                commute = user.getJSONObject(User.COMMUTE);TODO:bring back, when persona comes back
                if (commute == null) return randomCommand;

                Integer commuteTime = commute.getInt(User.TIME);

                if (commuteTime == null) return randomCommand;

                if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) == commuteTime) {
//                    randomCommand = actionMap.get(REPORT_TRAFFIC);
                }
            } catch (org.json.JSONException e) {
                e.printStackTrace();
            }
        }


        return randomCommand;
    }

    public boolean handleResponse(int commandCode) {
        if (commandCode == Understanding.CMD_YES
                && requestedCommand != null) {
            runRequestedCommand();
            expectedResponse = null;
            return true;
        } else {
            expectedResponse = null;
        }
        return false;
    }

    @SuppressLint({"InlinedApi", "NewApi"})
    private void runRequestedCommand() {
        switch (requestedCommand) {
            case MISSED_CALLS:
                sayAndShow(new DailyUpdate(context).getMissedCallsYouDidNotReply());
                break;
            case MEETINGS:
                sayAndShow(new DailyUpdate(context).getCalendarEvents());
                break;
            case READ_EMAIL:
                String pass = PasswordPreference.getDecoded("gmailPassword");
                if (pass == null || pass.length() < 1) {
                    sayAndShow(context.getString(R.string.give_me_password));
                    break;
                }
                (new DailyUpdate(context)).getGmail();
                sayAndShow(context.getString(R.string.fetch_email));
                break;
            case SET_EVENT:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {

                    Calendar beginCalendar = Calendar.getInstance();
                    beginCalendar.set(Calendar.HOUR_OF_DAY, 8);
                    Calendar endCalendar = Calendar.getInstance();
                    endCalendar.set(Calendar.HOUR_OF_DAY, 9);

                    Intent intent = new Intent(Intent.ACTION_INSERT)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            .setData(CalendarContract.Events.CONTENT_URI)
                            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginCalendar.getTimeInMillis())
                            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endCalendar.getTimeInMillis())
                            .putExtra(CalendarContract.Events.TITLE, context.getString(R.string.meeting_subject))
                            .putExtra(CalendarContract.Events.DESCRIPTION, context.getString(R.string.meeting_body))
                            .putExtra(CalendarContract.Events.EVENT_LOCATION, context.getString(R.string.meeting_location));
//                            .putExtra(Intent.EXTRA_EMAIL, "rowan@example.com,trevor@example.com");
                    Utils.startActivityFromNowhere(intent);
                }
                break;
            /*
            case LETS_MEET:
                String shareBody = context.getString(R.string.sms_to_friend);
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                sharingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Utils.startActivityFromNowhere(sharingIntent);
                break;
                */
            case LISTEN_MUSIC:
                Intent musicIntent = new Intent(MediaStore.INTENT_ACTION_MUSIC_PLAYER);
                musicIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Utils.startActivityFromNowhere(musicIntent);
                break;
            case READ_NEWS:
                (new DailyUpdate(context)).getNews();
                break;
            case VOCA:
                installVoca();
                break;
        }
    }
/*
    после корректного выхода их робина
    при повторном его запуске он перестает работать
    скорее всего потомучто объект App используется существующий
    и его метод OnCreate не вызывается
    */

    public static void start() {
        if (isRobinRunning)
            return;

        NotificationManager notificationManager = (NotificationManager) App.self
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(Robin.OPEN_NOTIFICATION);
        SphinxRecornizer.open();
        ProximityWakeUp.start();
        showStickyNotification(0);
        isRobinRunning = true;
    }

    public static void shutdown() {
        if (!isRobinRunning)
            return;

        MainActivity ma = MainActivity.get();
        if (ma != null)
            ma.finish();
        ProximityWakeUp.stop();
        NotificationManager notificationManager = (NotificationManager) App.self.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(Robin.STICKY_NOTIFICATION);
        notificationManager.cancel(Robin.OPEN_NOTIFICATION);
        SuzieService.stopRunningInForeground();
        SphinxRecornizer.close();
        isRobinRunning = false;
    }

    public static SharedPreferences shrdPrfs(Context context) {
        return context.getSharedPreferences("GENERAL_ROBIN_SHARED_PREFS", context.MODE_PRIVATE);
    }

    public boolean canHandleResponseOffline(Collection<String> matches) {

        List<String> speechList;
        try {
            speechList = (List<String>) matches;
        } catch (ClassCastException e) {
            return false;
        }

        for( String match: speechList) {
            match = match.toLowerCase();
            if (match.equals("debug mode on")) {
                shrdPrfs(context).edit().putBoolean(ShrdPrfs.DEBUG_MODE, true).commit();
                sayAndShow("debugging on");
                return true;
            } else if (match.equals("debug mode off")) {
                shrdPrfs(context).edit().putBoolean(ShrdPrfs.DEBUG_MODE, false).commit();
                sayAndShow("debugging off");
                return true;
            } else if (match.equals("testing mode on")) {
                shrdPrfs(context).edit().putBoolean(ShrdPrfs.TESTING_MODE, true).commit();
                sayAndShow("testing on");
                return true;
			} else if (match.equals("testing mode off")) {
				shrdPrfs(context).edit().putBoolean(ShrdPrfs.TESTING_MODE, false).commit();
				sayAndShow("testing off");
				return true;
			} else if (match.equals("debug mode")) {
                if (isDebugMode()) {
                    sayAndShow("debugging on");
                } else if (isTestingMode()) { 
                	 sayAndShow("testing on");
                } else {
                    sayAndShow("debugging off");
                }
                return true;
            } else if (Config.roku_version && (match.contains("roku") || match.contains("rocu") || match.contains("roka") || match.contains("roca") || match.contains("procol"))) {
                final String s = match;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        new ClientParser(s) {
                            @Override
                            public void run(Uri uri, String text) {
                                if (text == null) {
                                    text = "Now this, is TV!";
                                }
                                sayAndShow(text);

                                if (uri != null) {
                                    Intent it = new Intent(Intent.ACTION_VIEW);
                                    it.setData(uri);
                                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    App.self.startActivity(it);
                                }

                            }
                        };
                    }
                }).start();
                return true;
            }
        }
        return false;
    }

    public boolean isDebugMode() {
        return shrdPrfs(context).getBoolean(ShrdPrfs.DEBUG_MODE, false);
    }
    public boolean isTestingMode() {
        return shrdPrfs(context).getBoolean(ShrdPrfs.TESTING_MODE, true); // IN TEST MODE by default!!! 
    }

    public static boolean isRobinRunning = false;
    static boolean stickyNotificationIconState = false;

    static public void showStickyNotification(int icon) {

        boolean startService = icon == 0;
        if (icon == 0)
            icon = R.drawable.logo_icon_blue;

        Intent shutDownRobin = new Intent(MainActivity.SHUT_DOWN, null, App.self, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(App.self, 0, shutDownRobin, 0);

        NotificationCompat.Builder stickyNotification = null;
        String info = "";
        if (App.self.getBooleanPref(PrefConsts.PF_SMS_NOTIFY))
            info += " & " + App.self.getString(R.string.app_notification_read_sms);
        if (App.self.getBooleanPref("speakCallerName2"))
            info += " & " + App.self.getString(R.string.app_notification_caller_id);
        if (!"off".equals(App.self.getStringPref("voiceactivation")))
            info += " & " + App.self.getString(R.string.app_notification_voice_activation);
        if (App.self.getBooleanPref("handwaving"))
            info += " & " + App.self.getString(R.string.app_notification_hand_waving);
        if (info.length() > 3)
            info = info.substring(3);
        else
            info = App.self.getString(R.string.app_notification_default);

        stickyNotification = new NotificationCompat.Builder(App.self)
            .setSmallIcon(icon)
            .setContentIntent(App.self.robin().wakeUpIntent)
            .setOngoing(true)
            .setPriority(2)
            .setContentText(info)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, App.self.getString(R.string.app_notification_shutdown), pi);

        NotificationManager notificationManager =
                (NotificationManager) App.self.getSystemService(Context.NOTIFICATION_SERVICE);

        if (icon == R.drawable.logo_icon_blue)
            stickyNotification.setContentTitle(App.self.getString(R.string.app_notification_title1));
        else
            stickyNotification.setContentTitle(App.self.getString(R.string.app_notification_title2));

        if (startService)
            SuzieService.startInForeground(stickyNotification.getNotification(), STICKY_NOTIFICATION);
        else
            notificationManager.notify(STICKY_NOTIFICATION, stickyNotification.getNotification());
    }

    static long lastChangeNotificationTime = 0;

    static public void changeNotification(boolean red) {

        long t = System.currentTimeMillis();

        int icon;

        if (red) {
            if (t-lastChangeNotificationTime < 500)
                return;
            if (stickyNotificationIconState)
                icon = R.drawable.logo_icon_red1;
            else
                icon =  R.drawable.logo_icon_red2;
            stickyNotificationIconState = !stickyNotificationIconState;
        }
        else
            icon = R.drawable.logo_icon_blue;

        //NotificationManager notificationManager = (NotificationManager) App.self.getSystemService(Context.NOTIFICATION_SERVICE);
        //notificationManager.notify(STICKY_NOTIFICATION, stickyNotification.getNotification());
        //SuzieService.startInForeground(stickyNotification.getNotification(), STICKY_NOTIFICATION);
        showStickyNotification(icon);

        lastChangeNotificationTime = t;
    }

    public static int STICKY_NOTIFICATION = 1982;
    public static int OPEN_NOTIFICATION = 1987;

    public static class ShrdPrfs {
        public static String HI_COUNT = "HI_COUNT";
        public static String DEBUG_MODE = "DEBUG_MODE";
        public static String TESTING_MODE = "TESTING_MODE";
    }

    static public final String SP_LAST_CAR_MODE_ALERTS_NOTIFICATION = "SP_LAST_CAR_MODE_ALERTS_NOTIFICATION";

    void showNotification() {
        if (!Utils.isAndroid41orAbove)
            return;

        if (!isDebugMode() && isRobinRunning)
            return;

        try {

            Log.d("activityRecog", "notification");

            long CAR_MODE_NOTIFICATION_INTERVAL;
            if (isDebugMode()) {
                CAR_MODE_NOTIFICATION_INTERVAL = 300000;
            } else {
                CAR_MODE_NOTIFICATION_INTERVAL = 1000 * 60 * 60 * 6;

            }


            SharedPreferences sp = shrdPrfs(context);

            long lastNotification = sp.getLong(SP_LAST_CAR_MODE_ALERTS_NOTIFICATION, 0l);
            long now = System.currentTimeMillis();

            if (now - CAR_MODE_NOTIFICATION_INTERVAL < lastNotification) {
                Log.d("activityRecog", "notification_canceled");
                return;
            } else {
                Log.d("activityRecog", "notification_approved");
                sp.edit().putLong(SP_LAST_CAR_MODE_ALERTS_NOTIFICATION, System.currentTimeMillis()).commit();
            }


            Intent shutDownRobin = new Intent(SuzieService.ApplicationMonitorAction.SHUT_DOWN.name(),
                    null, App.self, SuzieService.class);
            PendingIntent dismissPendingIntent = PendingIntent.getService(App.self, 0, shutDownRobin, 0);

            Intent dismissIntentForever = new Intent(SuzieService.ApplicationMonitorAction.NEVER_ALERT_CAR_MODE.name(),
                    null, App.self, SuzieService.class);
            PendingIntent dismissPendingIntentForever = PendingIntent.getService(App.self, 0, dismissIntentForever, 0);

            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(context)
                            .setSound(uri)
                            .setContentTitle(context.getString(R.string.spoken_alerts_title))
                            .setSmallIcon(R.drawable.logo_icon)
                            .setAutoCancel(true)
                            .setContentIntent(wakeUpIntent)
                            .setContentText(context.getString(R.string.spoken_alerts_description))
                            .setPriority(2)
                            .addAction(R.drawable.ic_action_done, context.getString(R.string.spoken_alerts_on), wakeUpIntent)
                            .addAction(android.R.drawable.ic_menu_close_clear_cancel, context.getString(R.string.spoken_alerts_off), dismissPendingIntent)
                            .addAction(android.R.drawable.stat_sys_warning, context.getString(R.string.spoken_alerts_never), dismissPendingIntentForever);

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(OPEN_NOTIFICATION, builder.getNotification());

            (new Analytics(App.self)).trackEvent("notification", "notification_pop", "car_activity_detected");

        } catch (Exception e) {
        }
    }

}
