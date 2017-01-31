//package com.magnifis.parking.utils.js;
//
//import java.net.MalformedURLException;
//import java.net.URL;
//
//import android.content.Context;
//import android.webkit.WebView;
//
//import com.magnifis.parking.feed.RssFeedController;
//
//public class FeedsJsBridge extends JsBridge {
//	
//	 public FeedsJsBridge(Context c, WebView hostView) {
//		super(c, hostView);
//
//	}
//
//	public void playRss(String rssURL, boolean showTitle) {
//	    	try {
//				RssFeedController.getInstance().playFeed(new URL(rssURL), showTitle);
//			} catch (MalformedURLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} 
//	    }
//
//}
