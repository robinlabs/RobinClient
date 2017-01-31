package com.magnifis.parking.utils;

import android.text.format.DateUtils;

import java.util.HashMap;

/**
 * Created by oded on 6/26/14.
 */
public class ParserContext {

    ParserContext() {
        timeStamp = System.currentTimeMillis();
    }

    HashMap<String, String> selectionMap;

    private long timeStamp;

    boolean isContextRelevant() {
        if (System.currentTimeMillis() - timeStamp < DateUtils.MINUTE_IN_MILLIS * 5) {
            return true;
        }
        return false;
    }

}
