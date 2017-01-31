package com.magnifis.parking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by oded on 4/3/14.
 */
public class PlugInControlReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action.equals(Intent.ACTION_POWER_CONNECTED)) {
            // Do something when power connected
        } else if (action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
            // Do something when power disconnected
            Robin.shutdown();
        }
    }

}
