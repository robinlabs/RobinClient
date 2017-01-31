package com.magnifis.parking.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.magnifis.parking.MainActivity;
import com.magnifis.parking.MultiAsyncTask;
import com.magnifis.parking.Output;
import com.magnifis.parking.utils.Utils;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TheWebView extends WebView {	
	/*
	public void control(Understanding u) {
		
	}*/
	private boolean loading=false;
	
	public boolean isLoading() {
	   return loading;
	}
	
	public boolean hanldeBack() {
		if (isShown()) {
			if (isLoading()) {
				stopLoading();
				return true;
			}
			if (canGoBack()) {
				goBack();
				return !Utils.isEmpty(getUrl()); 
			}
			//setVisibility(GONE);
			return false;
		}
		return false;
	}
	
	@Override
	public void setVisibility(int visibility) {
		if (visibility==GONE) pbar().setVisibility(visibility);
		super.setVisibility(visibility);
	}

	private ProgressBar pbar() {
		return MainActivity.get().getProgressBar();
	}

	public TheWebView(Context context) {
		this(context,null);
	}

	public TheWebView(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}

	public TheWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, android.R.attr.webViewStyle);
		
		WebSettings wbs=getSettings(); 
		wbs.setJavaScriptEnabled(true);
		wbs.setBuiltInZoomControls(false);
		wbs.setAllowFileAccess(false);
		wbs.setLightTouchEnabled(true);
		//wbs.setPluginsEnabled(true);
		wbs.setSupportZoom(true);
		
		final Context theContext = context;  
		setWebViewClient(
		  new WebViewClient() {
				@Override
			public void onPageFinished(WebView view, String url) {
				// TODO Auto-generated method stub
				super.onPageFinished(view, url);
                try {
                    pbar().setVisibility(GONE);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
				loading=false;
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				pbar().setVisibility(VISIBLE);
				loading=true;
			
				if (url.contains("wikipedia.org")) {// TODO: optimize!
					final String theUrl = url; 
					MultiAsyncTask asyncWikiHtmlParser = new MultiAsyncTask() {

						@Override
						protected String doInBackground(Object[] params) {
							Connection conn = Jsoup.connect(theUrl); 
							Document dom = null;
							
							try {	
								Elements elems = null; 
								if (conn != null) 
									dom = conn.get();
								if (dom != null)
									elems = dom.select("div.content div p"); 
								
								if (!Utils.isEmpty(elems)) {
									String text = elems.get(0).text(); 
									String summary = null; 
									
									if (text != null) { 
										Pattern pattern = Pattern.compile("^(.*?\\.)[ ]+[A-Z]");// extract the first sentence 
										Matcher matcher = pattern.matcher(text);
										if (matcher.find()) {
										    text = matcher.group(1);
										    while (!Utils.isEmpty(text)) { // clean text up
										    	summary = text.replaceAll("^(.*?)(\\[.*?\\]|\\(.*?\\))+(.*?)", "$1$3"); // remove all that is in (..) or in [..]
										    	if (summary.equals(text))
										    		break; 
										    	text = summary; 
											} 
										}
										if (!Utils.isEmpty(summary)) {
											Output.sayAndShow(theContext, summary); 
										}
									}
								}
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} 
							return null;
						}
					};
							
					asyncWikiHtmlParser.multiExecute(); 
				}
				
				super.onPageStarted(view, url, favicon);
			}

			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				pbar().setVisibility(GONE);
				loading=false;
				super.onReceivedError(view, errorCode, description, failingUrl);
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				return false;
			}		
		  }
		);

		setOnTouchListener(
		   new OnTouchListener() {
		    @Override
		    public boolean onTouch(View v, MotionEvent event)
		    {
		        switch (event.getAction())
		        {
		            case MotionEvent.ACTION_DOWN:
		            case MotionEvent.ACTION_UP:
		                if (!v.hasFocus())
		                {
		                    v.requestFocus();
		            		v.requestFocus(View.FOCUS_DOWN);
		            		v.requestFocusFromTouch();
		                }
		                break;
		        }
		        return false;
		    }
		  }
	   );

	}
}
