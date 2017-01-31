package com.magnifis.parking;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

public class PowerStatusReceiver extends BroadcastReceiver {
	final static String TAG=PowerStatusReceiver.class.getSimpleName();
	

	@Override
	public void onReceive(Context context, Intent it) {
		Log.d(TAG, "onReceive ...");
		
		int plugged = it.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
		App.self.powerStatus = plugged; 
        if (plugged == BatteryManager.BATTERY_PLUGGED_AC) {
            // on AC power
        } else if (plugged == BatteryManager.BATTERY_PLUGGED_USB) {
            // on USB power
        } else if (plugged == 0) {
            // on battery power
        } else {
            // intent didn't include extra info
        }  
	}

}
