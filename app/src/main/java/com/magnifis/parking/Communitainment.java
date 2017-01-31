package com.magnifis.parking;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateUtils;

import com.magnifis.parking.model.DoublePoint;
import com.magnifis.parking.model.MagNews;
import com.magnifis.parking.suzie.SuzieService;
import com.magnifis.parking.utils.Utils;

import org.w3c.dom.Document;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Principal;
import java.util.Calendar;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by oded on 6/23/15.
 */
public class Communitainment {

    private final static int NOTIFICATION_HOUR_OF_DAY = 7;

    public final static int COMMUNITAINMENT_NOTIFICATION = 812198299;

    public static void setCommunitainmentAlarm(Context context) {

        if (!App.self.getBooleanPref("daily_news_update")) {
            return;
        }

        Calendar calendar = Calendar.getInstance();
        if (calendar.get(Calendar.HOUR_OF_DAY) >= NOTIFICATION_HOUR_OF_DAY && !App.self.robin().isDebugMode()) {
            calendar.add(Calendar.DATE, 1);
        }
        calendar.set(Calendar.HOUR_OF_DAY, NOTIFICATION_HOUR_OF_DAY);
//        calendar.set(Calendar.MINUTE, 00);
//        calendar.set(Calendar.SECOND, 00);

        long interval = AlarmManager.INTERVAL_DAY;

        if (App.self.robin().isDebugMode()) {
            interval = DateUtils.MINUTE_IN_MILLIS;
        }

        ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).setRepeating(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(), interval,
                pendingIntent(SuzieService.ApplicationMonitorAction.NOTIFY_COMMUNITAINMENT));
    }


    public static void onCommunitainmentCalled(Context context) {
        context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        Log.d("COMMUNITAINMENT", "called");
        new AsyncCommunitainment(context).execute();
        context.startActivity(new Intent(context, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    static String[] getNews() {
        DoublePoint location = UserLocationProvider.readLocationPoint();
        URL u = RequestFormers.createTrafficOrNewsRequest(location, location, true, "technology", null);

        InputStream is = null;
        try {
            is = invokeRequest(u, null, null, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (is != null) {
            Document doc = Xml.loadXmlFile(is);
            if (doc != null) {
                MagNews mt = Xml.setPropertiesFrom(
                        doc.getDocumentElement(), MagNews.class);
                if (mt != null) return mt.getItems();
            }
        }
        return null;
    }


    public static InputStream invokeRequest(URL u, String pd, String ref, String userAgent) throws IOException {

        HttpURLConnection uc = HttpURLConnection.class.cast(u.openConnection());
        uc.setReadTimeout(15000); // 15s timeout
        uc.setConnectTimeout(15000);

        if (userAgent != null)
            uc.setRequestProperty("User-Agent", userAgent);

        // uc.setUseCaches(false);
        if (pd != null) {
            uc.setRequestMethod("POST");
            uc.setDoOutput(true);
        }
        uc.setAllowUserInteraction(false);
        if (ref != null)
            uc.addRequestProperty("referer", ref);
        uc.connect();
        if (uc instanceof HttpsURLConnection) {
            HttpsURLConnection suc = (HttpsURLConnection) uc;
            Principal pr = suc.getPeerPrincipal();
            if (pr != null) {
                android.util.Log.d(Communitainment.class.getName(), "SSL Principal:: " + pr.toString());
            }
        }
        if (pd != null) {
            OutputStream os = uc.getOutputStream();
            OutputStreamWriter osr = new OutputStreamWriter(os);
            osr.write(pd);
            osr.flush();
        }
        return uc.getInputStream();
    }


    public static void notification(Context context) {

        if (!App.self.getBooleanPref("daily_news_update")) {
            return;
        }

        if (!Utils.isAndroid41orAbove) {
            return;
        }

        if (!App.self.robin().isDebugMode() && App.self.robin().isRobinRunning) {
            return;
        }


        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setContentTitle("Listen to your morning update")
                        .setSmallIcon(R.drawable.news_icon)
                        .setContentText("Tap \"Listen\" to play your update")
                        .addAction(android.R.drawable.ic_media_play, "Listen", pendingIntent(SuzieService.ApplicationMonitorAction.RUN_COMMUNITAINMENT_FROM_CONTENT_INTENT))
                        .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Cancel", pendingIntent(SuzieService.ApplicationMonitorAction.CANCEL_COMMUNITAINMENT))
                        .setContentIntent(pendingIntent(SuzieService.ApplicationMonitorAction.RUN_COMMUNITAINMENT_FROM_PLAY_BUTTON))
//                        .addAction(android.R.drawable.stat_sys_warning, "Never", pendingIntent(SuzieService.ApplicationMonitorAction.NEVER_ALERT_COMMUNITAINMENT))
                ;

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(COMMUNITAINMENT_NOTIFICATION, builder.getNotification());
    }

    static PendingIntent pendingIntent(SuzieService.ApplicationMonitorAction action) {
        Intent intent = new Intent(action.name(),
                null, App.self, SuzieService.class);
        PendingIntent pendingIntent = PendingIntent.getService(App.self, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    private static class AsyncCommunitainment extends AsyncTask {
        private Context context;
        private String[] news;

        public AsyncCommunitainment(Context context) {
            this.context = context;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            news = getNews();
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
//            Output.sayAndShow(context, news);
            VoiceIO.playTextAlerts(news,
                    App.self.getString(R.string.P_NEWS_INTRO));
            super.onPostExecute(o);
        }
    }
}
