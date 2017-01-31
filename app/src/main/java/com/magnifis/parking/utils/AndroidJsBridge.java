package com.magnifis.parking.utils;

import com.magnifis.parking.Launchers;
import com.magnifis.parking.Output;

import android.content.Context;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

public class AndroidJsBridge {
	
	Context mContext;
	WebView theWebView; 

    /** Instantiate the interface and set the packageName */
	public AndroidJsBridge(Context c, WebView hostView) {
        mContext = c;
        theWebView = hostView; 
    }

    /** Show a toast from the web page */
    //@JavascriptInterface
    public void showToast(String toast) {
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
    }
    
    public void tts(String text) {
    	Output.sayAndShow(mContext, text); 
    }

    public void startCamera() {
    	Launchers.launchCamera(mContext); 
    }
    
    public void toggleWebView(boolean on) {
    	if (theWebView != null) {
    		if (on)
    			theWebView.setVisibility(View.VISIBLE); 
    		else
    			theWebView.setVisibility(View.GONE); 	
    	}	
    }
    
    public void showWebView() {
    	if (theWebView != null) {
    		theWebView.setVisibility(View.VISIBLE); 
    	}
    }
}
