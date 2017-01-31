/**
 * 
 */
package com.magnifis.parking;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import compat.org.json.JSONException;
import compat.org.json.JSONObject;
import compat.org.json.JSONTokener;

import android.os.AsyncTask;

public class JSONFetcher<T> extends Fetcher<T> {
	
	@Override
	protected T consumeInputStream(InputStream is)  throws IOException {
		if (is!=null) {

			try {
				JSONTokener jsto = new JSONTokener(is);
				JSONObject jso=new JSONObject(jsto);
				return consumeJsonData(jso);
			} catch (JSONException e) {
				throw new IOException(e.getMessage());
			}

			
		}
		return null;
	}
	
	protected T consumeJsonData(JSONObject root) {
	   return (T)root;
	}
}