package com.magnifis.parking;

import com.google.android.vending.expansion.downloader.DownloaderClientMarshaller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;

public class DnlAlarmReceiver extends BroadcastReceiver {
	static final String TAG=DnlAlarmReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
        	Log.d(TAG,intent.toString());
            DownloaderClientMarshaller.startDownloadServiceIfRequired(context, intent, TheDownloader.class);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }       
    }

}
