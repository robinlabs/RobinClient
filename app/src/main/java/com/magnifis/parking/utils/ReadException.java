package com.magnifis.parking.utils;

import android.annotation.SuppressLint;
import java.io.IOException;

 public class ReadException extends IOException {

	public ReadException(Throwable t) {
		super(t.getMessage());
	    initCause(t);
	}
	
}
