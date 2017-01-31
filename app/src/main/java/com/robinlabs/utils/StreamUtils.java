package com.robinlabs.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;

import com.magnifis.parking.Log;

public class StreamUtils {
	public static void copy(InputStream is, File to) throws IOException {
		copy(is, to, true);
	}
	
	public static void copy(InputStream is, OutputStream os) throws IOException {
		byte buf[]=new byte[4096];
		for (;;) {
			//int avl=is.available();
			//if (avl<=0) break;
			int sz=is.read(buf);
			if (sz<=0) break;
			os.write(buf, 0, sz);
		}
	}
	
	public static void copy(InputStream is, File to, boolean renew) throws IOException {
		if (renew&&to.exists()) to.delete();
		copy(is,new FileOutputStream(to));
		FileOutputStream fos=new FileOutputStream(to);
		try {
			copy(is, fos);
		} finally {
			fos.close();
		}
	}
	
	public static void copy(URL from, File to, boolean renew) throws IOException {
		copy(from.openStream(), to ,renew);
	}
	
	public static StringBuilder getTextFromStream(InputStream is) {
		StringBuilder content = null;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(is)); 
			String line;
			while ((line = br.readLine()) != null) {
				if (content==null) 
				  content=new StringBuilder(line);
				else {
				  content.append(line);
				}
				content.append('\n');
			}
		} catch (Exception e) {
		//	Log.e(TAG, e.getMessage());
        }
        return content;
    }
}
