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
import java.security.Principal;

import javax.net.ssl.HttpsURLConnection;

import org.w3c.dom.Element;

import android.os.AsyncTask;
import android.util.Log;


public abstract class Fetcher<T> extends OurAsyncTask<Object,Integer,T> {
	final static String TAG="Fetcher<T>";
	
	public void execute(URL url, String postData, String referrer) throws MalformedURLException {
		networkCommunicationError=false;
		super.execute(url,postData,referrer);
	}

	protected abstract T consumeInputStream(InputStream is) throws IOException;
	
	
	protected String exactNcePlace=null;
	protected Exception exactNceException=null;
	protected void exactNcePlace(String s) {
		exactNcePlace(s,null);
	}
	protected void exactNcePlace(String s, Exception x) {
		if (exactNcePlace==null) {
			exactNcePlace=s;
			exactNceException=x;
		}
	}
	
	final static protected int N_CONNECTION_ATTEMPTS=4, N_WRITE_ATTEMPTS=4, N_READ_ATTEMPS=4;
	
	protected InputStream invokeRequest(URL u, String pd, String ref, String userAgent) throws IOException {
		
		HttpURLConnection uc = null;
		IOException xx=null;
		
		for (int i=0;i<N_CONNECTION_ATTEMPTS;i++) try {
			xx=null;
			if (i>0) try {
				Thread.sleep(100);
			} catch(InterruptedException ix) {
				return null;
			}
			uc=HttpURLConnection.class.cast(u.openConnection());
			if (uc==null) continue;
			/*
		    uc.setReadTimeout(15000); // 15s timeout
		    uc.setConnectTimeout(15000);
			 */
			uc.setConnectTimeout(1500);
			uc.setConnectTimeout(15000);


			if (userAgent!=null)
				uc.setRequestProperty("User-Agent",  userAgent);

			// uc.setUseCaches(false);
			uc.setDoInput(true);
			if (pd != null) {
				uc.setRequestMethod("POST");
				uc.setDoOutput(true);
			}
			uc.setAllowUserInteraction(false);
			if (ref != null)
				uc.addRequestProperty("referer", ref);
			uc.connect();
			if (uc instanceof HttpsURLConnection) {
				HttpsURLConnection suc=(HttpsURLConnection)uc;
				Principal pr=suc.getPeerPrincipal();
				if (pr!=null) {
					Log.d(TAG,"SSL Principal:: "+pr.toString());
				}
			}
			break;
		} catch(IOException x) {
			xx=x;
		}
	//	xx=new IOException("simulador");
		if (xx!=null) {
			exactNcePlace("Fetcher#1",xx);
			throw xx;
		}
		if (pd != null) {
			OutputStream os =null;
			try {
		       os =uc.getOutputStream();
			} catch(IOException x) {
				xx=x;
			}
			if (os==null) {
				exactNcePlace("Fetcher#0",xx);
				if (xx!=null) throw xx;
				return null;
			}
			byte bb[]=pd.getBytes("UTF-8");
			for (int j=0;j<bb.length;j++) {
			  for (int i=0; i<N_WRITE_ATTEMPTS; i++) try {
				 xx=null;
				 if (i>0) try {
				   Thread.sleep(100);
			     } catch(InterruptedException ix) {
				   return null;
				 }
			     os.write(bb[j]);
			     break;
			  } catch(IOException x) {
			     xx=x;
		      }
			}
			if (xx!=null) {
				exactNcePlace("Fetcher#1",xx);
				throw xx;
			}
			try {
			   os.flush(); 
			} catch(IOException x) {
				exactNcePlace("Fetcher#1.1",x);
				throw x;
			}
			try {
			   os.close();
			} catch(IOException x) {
			   exactNcePlace("Fetcher#1.2",x);
			   throw x;
			}
		}
		return uc.getInputStream();
	}
	
	protected T consumeData(Object o) {
		return null;
	}
	
	protected void onNetworkCommunicationError() {}
	
	private void _handleNce() {
    	if (!isCancelled()) {
    	   networkCommunicationError=true;
	       onNetworkCommunicationError();
	       Log.d(TAG,"_handleNce");
	    }		
	}

	@Override
	protected T doInBackground(Object... params) {
		if (params.length==1) return consumeData(params[0]);
			
		URL u=URL.class.cast(params[0]);
		try {
			String pd=String.class.cast(params[1]),ref=String.class.cast(params[2]);
		    InputStream is=invokeRequest(u,pd,ref,null);
		    if (is==null) {
				exactNcePlace("Fetcher#3");
		    	_handleNce();
		    	return null;
		    }
			return consumeInputStream(is);
		} catch (IOException e) {
			exactNcePlace("Fetcher#4",e);
			_handleNce();
           Log.e(TAG, e.getMessage(), e);
		}
		return null;
	}
	
	protected volatile boolean networkCommunicationError=false;

	public boolean isNetworkCommunicationError() {
		return networkCommunicationError;
	}
	
	
}