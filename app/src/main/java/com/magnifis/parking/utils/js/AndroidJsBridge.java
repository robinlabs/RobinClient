package com.magnifis.parking.utils.js;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang.StringEscapeUtils;

import com.magnifis.parking.Launchers;
import com.magnifis.parking.Output;


import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;

import android.content.Context;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

public class AndroidJsBridge extends JsBridge {
	
	

    /** Instantiate the interface and set the packageName */
	public AndroidJsBridge(Context c, WebView hostView) {
       super(c, hostView); 
    }

    /** Show a toast from the web page */
    //@JavascriptInterface
    public void showToast(String toast) {
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
    }
    
    public void tts(String text) {
    	Output.sayAndShow(mContext, text); 
    }
    
    public void narrateFromHtml(String htmlFrag) {
    	
    	try {
			String text = ArticleExtractor.getInstance().getText(htmlFrag); // extract readable text
			Output.sayAndShow(mContext, text); 
		}catch (BoilerpipeProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }

    public void startCamera() {
    	Launchers.launchCamera(mContext); 
    }
    
    
}
