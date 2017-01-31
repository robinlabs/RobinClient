package com.magnifis.parking.sportfeeds;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.text.format.DateUtils;
import android.util.Log;

import com.magnifis.parking.JSONFetcher;
import com.magnifis.parking.Json;
import com.magnifis.parking.MainActivity;
import com.magnifis.parking.VoiceIO;
import com.magnifis.parking.Xml;
import com.magnifis.parking.model.ChadwickFeed;
import com.magnifis.parking.model.ChadwickStory;
import com.magnifis.parking.model.GooDirectionsScraperParams;
import com.magnifis.parking.model.PkResponse;
import com.magnifis.parking.traffic.EtaMonitor;
import com.magnifis.parking.utils.Utils;

import compat.org.json.JSONObject;

public class ChadwickFeedFetcher extends JSONFetcher<ChadwickFeed> {
	
	private static final String TAG = ChadwickFeedFetcher.class.getSimpleName(); 
	final String feedUrl = "http://bball-live.com/api/games.php?";
	final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	
	public ChadwickFeedFetcher()  {
		super(); 
		   try {
			    int  numStories = 5; 
		    	StringBuffer urlBuf = new StringBuffer(feedUrl);
		    	Date curDate = new Date(System.currentTimeMillis()); 
		    	if (curDate.getHours() < 17) { // TODO:??
		    		curDate = new Date(System.currentTimeMillis() - 24*3600);// go back one day 
		    	}
		    	urlBuf.append("amount_stories=").append(numStories).append("&date=").append(sdf.format(curDate)); 
				execute(new URL(urlBuf.toString()));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
	}	
	
	
	@Override
	protected  ChadwickFeed doInBackground(Object... params) {
	
		try{ 
			URL u=(URL)params[0];
			InputStream is=invokeRequest(u,null, null, null);
			if (is!=null) try {
				ChadwickFeed feed = consumeInputStream(is);
				return feed;
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
	
	
	protected ChadwickFeed consumeJsonData(JSONObject root) {
		Element el = Json.convertToDom(root); 
		ChadwickFeed feed = Xml.setPropertiesFrom(el, ChadwickFeed.class);
		if (feed != null && feed.getStories() != null)
			Log.w(TAG, "consumeJsonData: feed size is " + feed.getStories().length); 
		
		return feed; 
	}
	
	
	@Override
	protected void onPostExecute(ChadwickFeed feed) {
		// playback
		
		Log.d(TAG, "Chadwick feed: on post execute"); 
		
		if (feed != null) {
			ChadwickStory[] stories = feed.getStories();
			if (!Utils.isEmpty(stories)) {
				VoiceIO.sayAndShowFromGui("Basketball updates from Robin's newsroom, provided by Chadwick: ", true);// switch voices 
				for (ChadwickStory story : stories) {
					Log.i(TAG, "Chadwick feed: " + story.getText()); 
					
					VoiceIO.sayAndShowFromGui(story.getText()); 
				}
			} else {
				VoiceIO.sayAndShowFromGui("I couldn't find any recent updates. Let's try again later.");
			}
		}

	}

}
 