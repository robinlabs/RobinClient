/**
 * 
 */
package com.magnifis.parking;

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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.magnifis.parking.model.GooDirectionsScraperParams;
import com.magnifis.parking.traffic.EtaMonitor;
import com.magnifis.parking.utils.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

/**
 * This class makes sure that we can sync parameters for the Google Directions scraper from the cloud (Dropbox), 
 * in case the HTML format changes 
 * @author IE
 *
 */
public class SettingsFetcher extends XMLFetcher<GooDirectionsScraperParams> {
	final static String TAG="SettingsFetcher";
	// sits in Google Drive
	//static final String SETTINGS_XML_URL = "https://docs.google.com/open?id=0B2IYkWlKuqX0bmw2NzdPbUM5d2s"; 
	static final String SETTINGS_XML_URL = "http://dl.dropbox.com/u/100597905/google_directions_params.xml"; 
	public SettingsFetcher()  {
	    try {
			exec(new URL(SETTINGS_XML_URL));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}	
	
	private void exec(Object o) {
	 	execute(o);
	}



	@Override
	protected  GooDirectionsScraperParams doInBackground(Object... params) {
		//if (params[0] instanceof Bitmap) return (GooDirectionsScraperParams)params[0];
		try{ 
			URL u=(URL)params[0];
			InputStream is=invokeRequest(u,null, null, null);
			if (is!=null) try {
	           Document doc=Xml.loadXmlFile(is);
	           if (doc!=null) {
	        	   GooDirectionsScraperParams scraperParams=Xml.setPropertiesFrom(
	        			 doc.getDocumentElement(), GooDirectionsScraperParams.class
	        	  );
	              if (scraperParams!=null) {
	            	  EtaMonitor.setParams(scraperParams); 
	            	  return scraperParams;
	              }
	           }
			} finally {
			  is.close();
			}
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null; 

	}
}