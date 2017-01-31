/**
 * 
 */
package com.magnifis.parking.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
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

import com.magnifis.parking.Consts;
import com.magnifis.parking.Log;
import com.magnifis.parking.MultiAsyncTask;
import com.magnifis.parking.MultipleEventHandler;
import com.magnifis.parking.MultipleEventHandler.EventSource;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.widget.ImageView;

public class ImageFetcher extends MultiAsyncTask<Object,Object,Bitmap> {
	final static String TAG="ImageFetcher";
	
	private static HashMap<URL,ArrayList<ImageFetcher>> que=new HashMap<URL,ArrayList<ImageFetcher>>();
	private static DiskCache bitmapCache=null;
	
	
	
	boolean useCache=false;
	static boolean fAbort=false;
	
	public static void setAbort() {
	  fAbort=true;
	}
	
	public ImageFetcher(String url) throws MalformedURLException {
	    this(url,false,false);
	}
	
	public static boolean isInCache(String url) {
		String key=Utils.md5(url);
		synchronized(ImageFetcher.class) {
			if (bitmapCache==null) bitmapCache=DiskCacheClient.getDataCache();
			return bitmapCache.containsKey(key)!=0;
		}		
	}
	
	public static Uri getUriFromCache(String url) {
		String key=Utils.md5(url);
		synchronized(ImageFetcher.class) {
			if (bitmapCache==null) bitmapCache=DiskCacheClient.getDataCache();
			return bitmapCache.getUri(key);
		}		
	}
	
	public static Bitmap getFromCache(String url) {
		String key=Utils.md5(url);
		synchronized(ImageFetcher.class) {
			if (bitmapCache==null) bitmapCache=DiskCacheClient.getDataCache();
			return BitmapFactory.decodeStream(bitmapCache.getStream(key));
		}		
	}
	
	protected boolean workSynchronous=false;
	
	public boolean isWorkSynchronous() {
		return workSynchronous;
	}

	public void setWorkSynchronous(boolean workSynchronous) {
		this.workSynchronous = workSynchronous;
	}
	
	@SuppressLint("NewApi")
	private void exec(Object o) {
	  if (workSynchronous) {
		 if (o instanceof Bitmap) 
		   onPostExecute((Bitmap)o);
		 else
		   onPostExecute(doInBackground((URL)o));
	  } else {
		   multiExecute(o);
	  }
	}

	public ImageFetcher(String url, boolean _useCache, boolean _workSynchronous) 
	  throws MalformedURLException
   {
		workSynchronous=_workSynchronous;
		useCache=_useCache; fAbort=false;
		URL u=new URL(url);
		if (useCache) {
			boolean inCache=false;
			String key=Utils.md5(url);
			synchronized(ImageFetcher.class) {
				if (bitmapCache==null) bitmapCache=DiskCacheClient.getDataCache();
				inCache=bitmapCache.containsKey(key)!=0;
			}
			if (inCache) try {
				Bitmap b=BitmapFactory.decodeStream(bitmapCache.getStream(key));
				if (b!=null&&b.getHeight()>0) {
					bitmapCache.touch(key);
					onPostExecute(b);
					Log.d(TAG,"cache OK "+u);
					return;
				}
			} catch (Throwable t) {}
			// the image not in cache or the cached one is corrupt.
			synchronized(ImageFetcher.class) {
				ArrayList<ImageFetcher> ifs=que.get(u);
				if (ifs!=null) {
					ifs.add(this); return;
				}
				que.put(u,new ArrayList<ImageFetcher>());
			}
			exec(u);
			
		} else
			exec(u);
	}

	public static void setImageTo(String u,ImageView iv) throws MalformedURLException  {
		setImageTo(u,iv,null,false);
	}
	
	public static void setImageTo(String u,ImageView iv,boolean useCache) throws MalformedURLException  {
		setImageTo(u,iv,null,useCache);
	}

	public static void setImageTo(String u,ImageView iv,MultipleEventHandler<Bitmap>.EventSource es) throws MalformedURLException  {
		setImageTo(u,iv,es,false);
	}
	
	
	public static void syncCacheImage(String u)  throws MalformedURLException  {
		if (!isInCache(u)) new ImageFetcher(u,true, true);
	}

	
	public static void setImageTo(
	     String u,final ImageView iv,final MultipleEventHandler<Bitmap>.EventSource es,
	     boolean useCache
	) throws MalformedURLException  {
	  new ImageFetcher(u,useCache, false) {
		   @Override
		   protected void onPostExecute(Bitmap bmp) {
			   if (bmp!=null) {
				   iv.setImageBitmap(bmp);
				   Log.d(TAG, ""+bmp.getWidth());
				  // if (es!=null) es.fireEvent(bmp);
			   }
			   if (es!=null) es.fireEvent(bmp);
		   }	  
	  };
	}

	@Override
	protected  Bitmap doInBackground(Object... params) {
		if (params[0] instanceof Bitmap) return (Bitmap)params[0];
		
		URL u=(URL)params[0];
		try {

		   InputStream is=u.openStream();
		   byte buf[]=new byte[1024];
		   ByteArrayOutputStream baos=new ByteArrayOutputStream();
		   for (int natt=0;;) {
			 if (fAbort) {
				 if (useCache) {
					 synchronized(ImageFetcher.class) { 
						 que.remove(u);
					 }
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
			   baos.write(buf,0, sz);
			 }
		   }
		   
		   ByteArrayInputStream bais=new ByteArrayInputStream(baos.toByteArray());
		   
		   Bitmap b=BitmapFactory.decodeStream(bais);
		   
		   if (useCache) {
			   ArrayList<ImageFetcher> ifs=null;
			   synchronized(ImageFetcher.class) { 
				   //cashedBitmaps.put(Utils.md5(u.toString()),baos.toByteArray());
				   bitmapCache.put(Utils.md5(u.toString()),baos.toByteArray());
				   ifs=que.get(u);
				   if (ifs!=null) que.remove(u);
			   }
			   if (ifs!=null) for (ImageFetcher i:ifs) i.exec(b);
		   }
		   
           return b;
		} catch (Throwable t) {
		    if (useCache) {
				synchronized (ImageFetcher.class) {
					que.remove(u);
				}
			}		
		} 
		return null;
	}
}