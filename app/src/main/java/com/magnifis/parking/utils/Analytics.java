package com.magnifis.parking.utils;

import android.content.Context;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

/**
 * Created by oded on 12/3/13.
 */
public class Analytics {

    Context ctx;

    public Analytics(Context ctx){
        this.ctx = ctx;
    }

    private EasyTracker tracker(){
        EasyTracker easyTracker = EasyTracker.getInstance(ctx);
        return easyTracker;
    }

    public void trackButtonPress(String buttonName){

        if(tracker()==null)return;
        // MapBuilder.createEvent().build() returns a Map of event fields and values
        // that are set and sent with the hit.
        tracker().send(MapBuilder
                .createEvent("ui_action",     // Event category (required)
                        "button_press",  // Event action (required)
                        buttonName,   // Event label
                        null)            // Event value
                .build()
        );
    }
    public void trackEvent(String category, String action, String label){

        if(tracker()==null)return;
        tracker().send(MapBuilder
                .createEvent(category,
                        action,
                        label,
                        null)
                .build()
        );
    }

}
