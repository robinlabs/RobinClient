/**
 * 
 */
package com.magnifis.parking.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;
import java.util.WeakHashMap;

import org.w3c.dom.Element;

import com.magnifis.parking.Log;
import com.magnifis.parking.MultiAsyncTask;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.ImageView;

public class CachingFileFetcher extends MultiAsyncTask<Object,Object,File> {
	final static String TAG="DiskCacheClient";
	
	private static HashMap<URL,ArrayList<CachingFileFetcher>> que=new HashMap<URL,ArrayList<CachingFileFetcher>>();
	private static DiskCache cache=null;
	
	static boolean fAbort=false;
	
	public static void setAbort() {
	  fAbort=true;
	}
	
	public CachingFileFetcher(String url) throws MalformedURLException {
	    this(url,false);
	}
	
	public static boolean isInCache(String url) {
		String key=Utils.md5(url);
		synchronized(CachingFileFetcher.class) {
			if (cache==null) cache=DiskCacheClient.getDataCache();
			return cache.containsKey(key)!=0;
		}		
	}
	
	public static Uri getUriFromCache(String url) {
		String key=Utils.md5(url);
		synchronized(CachingFileFetcher.class) {
			if (cache==null) cache=DiskCacheClient.getDataCache();
			return cache.getUri(key);
		}		
	}
	
	protected boolean workSynchronous=false;
	
	public boolean isWorkSynchronous() {
		return workSynchronous;
	}

	public void setWorkSynchronous(boolean workSynchronous) {
		this.workSynchronous = workSynchronous;
	}
	
	private void exec(Object o) {
	  if (workSynchronous) {
		 if (o instanceof File) 
		   onPostExecute((File)o);
		 else
		   onPostExecute(doInBackground((URL)o));
	  } else
		 multiExecute(o);
	}

	public CachingFileFetcher(String url, boolean _workSynchronous) 
	  throws MalformedURLException
   {
		workSynchronous=_workSynchronous;
		fAbort=false;
		URL u=new URL(url);

		boolean inCache=false;
		String key=Utils.md5(url);
		synchronized(CachingFileFetcher.class) {
			if (cache==null) cache=DiskCacheClient.getDataCache();
			inCache=cache.containsKey(key)!=0;
		}
		if (inCache) try {
			File b=cache.getFile(key);
			if (b!=null&&b.exists()&&b.length()>0) {
				cache.touch(key);
				onPostExecute(b);
				Log.d(TAG,"cache OK "+u);
				return;
			}
		} catch (Throwable t) {}
		// the file is not in cache or the cached one is corrupt.
		synchronized(CachingFileFetcher.class) {
			ArrayList<CachingFileFetcher> ifs=que.get(u);
			if (ifs!=null) {
				ifs.add(this); return;
			}
			que.put(u,new ArrayList<CachingFileFetcher>());
		}
		exec(u);	
	}
	
	protected String userAgent=null;

	@Override
	protected  File doInBackground(Object... params) {
		if (params[0] instanceof File) return (File)params[0];
		
		URL u=(URL)params[0];
		try {
			HttpURLConnection uc = HttpURLConnection.class.cast(u.openConnection());

			uc.setReadTimeout(30000); // 30s timeout
			uc.setConnectTimeout(30000);
			
			if (userAgent!=null)
			   uc.setRequestProperty("User-Agent",  userAgent);
			
		   uc.connect();
		   InputStream is=uc.getInputStream();
			
		   byte buf[]=new byte[1024];
		   
		   File fl=cache.getFile(Utils.md5(u.toString()));
		   fl.delete();
		   
		   FileOutputStream fos=new FileOutputStream(fl);
		   
		   try {
			   for (int natt=0;;) {
				   if (fAbort) {
					  
						   synchronized(CachingFileFetcher.class) { 
							   que.remove(u);
						   }
					  
					   try {
						   is.close(); 
					   } catch(Throwable t) {}
					   cancel(true); 
					   return null;
				   }
				   int sz=is.read(buf);
				   if (sz<0) break;
				   if (sz==0) {
					   if (++natt>10) break;
					   Thread.sleep(50);
				   } else {
					   natt=0;
					   fos.write(buf,0, sz);
				   }
			   }
		   } finally {
		     fos.close();
		     fos=null;
		   }
		   
		   ArrayList<CachingFileFetcher> ifs=null;
		   synchronized(CachingFileFetcher.class) { 
			   ifs=que.get(u);
			   if (ifs!=null) que.remove(u);
		   }
		   if (ifs!=null) for (CachingFileFetcher i:ifs) i.exec(fl);
		   
		   
		   return fl;
           
		} catch (Throwable t) {
				synchronized (CachingFileFetcher.class) {
					que.remove(u);
				}
		} 
		return null;
	}
}