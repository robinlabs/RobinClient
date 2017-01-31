package com.magnifis.parking.utils;

import android.annotation.SuppressLint;
import java.io.IOException;

 public class WriteException extends IOException {

	public WriteException(Throwable t) {
		super(t.getMessage());
	    initCause(t);
	}
	
}
