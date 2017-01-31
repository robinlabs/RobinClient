package com.magnifis.parking;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.magnifis.parking.model.HelpTopic;
import com.magnifis.parking.suzie.RequiresSuzie;
import com.magnifis.parking.views.TheWebView;

import java.net.URI;

/**
 * Created by user on 08/12/13.
 */
public class Web extends Activity implements RequiresSuzie {

    public static final String OPEN_WEB = "com.magnifis.parking.OPEN_WEB";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_activity);
        Intent it=getIntent();
        if (it.hasExtra("URL")) {
            LinearLayout v = (LinearLayout)findViewById(R.id.mainWeb);
            WebView w = new WebView(this);
            w.setLayoutParams(new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
            w.getSettings().setJavaScriptEnabled(true);
            w.getSettings().setSupportZoom(true);
            w.getSettings().setBuiltInZoomControls(true);
            //w.getSettings().setDisplayZoomControls(false);
            w.getSettings().setUseWideViewPort(true);
            w.setWebViewClient(
                    new WebViewClient() {
                        @Override
                        public void onPageFinished(WebView view, String url) {
                            super.onPageFinished(view, url);
                        }

                        @Override
                        public void onPageStarted(WebView view, String url, Bitmap favicon) {
                            super.onPageStarted(view, url, favicon);
                        }

                        @Override
                        public void onReceivedError(WebView view, int errorCode,
                                                    String description, String failingUrl) {
                            super.onReceivedError(view, errorCode, description, failingUrl);
                        }

                        @Override
                        public boolean shouldOverrideUrlLoading(WebView view, String url) {
                            return false;
                        }
                    }
            );
            v.addView(w);
            String u = it.getStringExtra("URL");
            w.loadUrl(u);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        App.self.setActiveActivity(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        App.self.removeActiveActivity(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        App.self.notifyStopActivity(this);
    }

    @Override
    public boolean isRequiringSuzie() {
        return true;
    }
}