package com.magnifis.parking;

import com.magnifis.parking.suzie.SuzieService;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DLReceiver extends BroadcastReceiver {
	static String TAG=DLReceiver.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent it) {
		Log.d(TAG,"onReceive");
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(it.getAction())) {
        	Intent i=(Intent)it.clone();
        	i.setClass(App.self, SuzieService.class);
            App.self.startService(i);
        }
	}

}
