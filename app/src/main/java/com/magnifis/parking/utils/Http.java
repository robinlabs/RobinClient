package com.magnifis.parking.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import com.magnifis.parking.Log;

public class Http {
	
	static final String TAG="Http";
	
	public static HttpURLConnection httpRq(URL u, String pd, String ref) throws IOException {

		Log.d(TAG," invokeRequest.rq: "+pd);

		HttpURLConnection uc = HttpURLConnection.class.cast(u
				.openConnection());
		// uc.setUseCaches(false);
		if (pd != null) {
			uc.setRequestMethod("POST");
			uc.setDoOutput(true);
		}
		uc.setAllowUserInteraction(false);
		if (ref != null)
			uc.addRequestProperty("referer", ref);
		
		uc.setAllowUserInteraction(false);
		uc.setUseCaches(false);
		
		uc.connect();
		if (pd != null) {
			OutputStream os = uc.getOutputStream();
			OutputStreamWriter osr = new OutputStreamWriter(os);
			osr.write(pd);
			osr.flush();
		}
		return uc;
	}
}
