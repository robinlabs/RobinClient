package com.magnifis.parking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.net.URI;

public class GenericInstallTrackersReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        (new AsyncRun(intent)).multiExecute();
    }

    class AsyncRun extends MultiAsyncTask {
        private Intent intent;

        public AsyncRun(Intent intent) {
            this.intent = intent;
        }

        @Override
        protected Object doInBackground(Object[] objects) {

            if (intent == null || intent.getExtras() == null) return null;

            String referrerString = intent.getExtras().getString("referrer");

            if (referrerString == null) return null;

            try {
                (new DefaultHttpClient()).execute(new HttpGet(new URI(referrerString)));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            return null;
        }

    }
}
