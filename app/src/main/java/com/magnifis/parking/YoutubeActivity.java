package com.magnifis.parking;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;

import com.magnifis.parking.utils.ParserContext;

public class YoutubeActivity extends Activity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.youtube_activity);

        Intent it = getIntent();
        onNewIntent(it);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        WebView webView = (WebView) findViewById(R.id.youtube);
        if (webView == null)
            return;

        String html = intent.getStringExtra("HTML");
        webView.loadUrl("http://google.com");
        //webView.getSettings().setJavaScriptEnabled(true);
        if (html != null)
            webView.loadData(html, "text/html", "UTF-8");
        else
            webView.loadUrl("http://youtube.com");

        super.onNewIntent(intent);
    }
/*
     private ParserContext parserContext = null;

   @Override
    protected void onStop() {
        super.onStop();
        parserContext = App.self.getRokuContext();
        App.self.setRokuContext(null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        App.self.setRokuContext(parserContext);
    }
    */
}