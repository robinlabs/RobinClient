package com.robinlabs.utils;

import android.os.Bundle;

public class AndroidUtils {
    public static boolean isEmpty(Bundle bl) {
    	return bl==null||bl.isEmpty();
    }
}
