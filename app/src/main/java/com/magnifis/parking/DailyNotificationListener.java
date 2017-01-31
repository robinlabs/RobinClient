package com.magnifis.parking;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by oded on 1/16/14.
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class DailyNotificationListener extends NotificationListenerService {


    static SharedPreferences storedNotifications(Context context) {
        return context.getSharedPreferences("NOTIFICATIONS_DATA", MODE_PRIVATE);
    }

    public static String getAll(Context context) {

        String result = null;

        List<String> notifications = new ArrayList<String>((Collection<? extends String>) storedNotifications(context).getAll().values());

        for (String n : notifications) {
            if (n == null) continue;
            if (n.matches("(?i).*\\d\\d\\d.*")) continue;

            if (result == null) {
                result = "Your recent notifications: \n\n";
            }
            result += n + " .. \n\n";
        }

        storedNotifications(context).edit().clear().commit();

        return result;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        documentNotification(sbn);
        logRaw(sbn);
    }

    private void logRaw(StatusBarNotification sbn) {

        Bundle bundle = sbn.getNotification().extras;
        for (String key : bundle.keySet()) {
            Object value = bundle.get(key);
            Log.d("josn_bundle", String.format("%s %s (%s)", key,
                    value.toString(), value.getClass().getName()));

            JSONObject j = new JSONObject();
            try {
                j.put(key, value);
            } catch (JSONException e) {
                Log.e("josn_bundle_error", String.format("%s %s (%s)", key,
                        value.toString(), value.getClass().getName()));
            }
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        documentNotification(sbn);
        logRaw(sbn);
    }

    void documentNotification(StatusBarNotification sbn) {

        String appName = getAppLabel(sbn.getPackageName());
        for(String pkg:blockedPackages()){
            if(sbn.getPackageName().equals(pkg))return;
        }

        Bundle extras = sbn.getNotification().extras;

        String title = extras.getString(Notification.EXTRA_TITLE);
        CharSequence notificationText = extras.getCharSequence(Notification.EXTRA_TEXT);
        CharSequence[] notificationLines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES);
//        CharSequence notificationSubText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT);

        String stringToSpeak = appName +" : "+title;

        if (notificationText != null) {
            stringToSpeak += ":" + notificationText;
        }

        if (notificationLines != null) {
            String lines = "";
            for (CharSequence line : notificationLines) {
                lines += line + ", ";
            }

            stringToSpeak += " : " + lines;
        }

        storedNotifications(getApplicationContext()).edit().putString("" + sbn.getId(), stringToSpeak).commit();

        Log.d("notification_test", "notification_test_" + stringToSpeak);
    }

    private List<String> blockedPackages() {
        ArrayList<String> b = new ArrayList<String>();
        b.add("android");
        b.add("com.magnifis.parking");
        return b;
    }


    String getAppLabel(String packageName){
        PackageManager pm = getPackageManager();
        ApplicationInfo ai;
        try {
            ai = pm.getApplicationInfo(packageName, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            ai = null;
        }
        final String applicationName = (String) (ai != null ? pm.getApplicationLabel(ai) : "System");
        return applicationName;
    }

}
