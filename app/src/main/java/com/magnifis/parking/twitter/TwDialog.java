/*
 * Copyright 2010 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.magnifis.parking.twitter;

import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.content.DialogInterface.OnDismissListener;

import com.magnifis.parking.twitter.TwitterWrapper.DialogListener;
import com.magnifis.parking.utils.Utils;
import com.magnifis.parking.views.ProgressSpinner;

public class TwDialog extends Dialog implements OnDismissListener {
	public static final String TAG = "twitter";

	static final int TW_BLUE = 0xFFC0DEED;
	static final float[] DIMENSIONS_LANDSCAPE = { 460, 260 };
	static final float[] DIMENSIONS_PORTRAIT = { 280, 420 };
	static final FrameLayout.LayoutParams FILL = new FrameLayout.LayoutParams(
			ViewGroup.LayoutParams.FILL_PARENT,
			ViewGroup.LayoutParams.FILL_PARENT);
	static final int MARGIN = 4;
	static final int PADDING = 2;

	private Object mIcon;
	private String mUrl;
	private DialogListener mListener;
	private Dialog mSpinner;
	private WebView mWebView;
	private LinearLayout mContent;
	private TextView mTitle;
	private Handler mHandler;

	private twitter4j.Twitter mTwitter;

	private RequestToken mRequestToken;
	

	public TwDialog(Context context, twitter4j.Twitter twitter,
			DialogListener listener, Object icon) {
		super(context);
		mTwitter = twitter;
		mListener = listener;
		mIcon = icon;
		mHandler = new Handler();
	}
	
	public TwDialog(Context context, twitter4j.Twitter twitter,
			DialogListener listener) {
		this(context,twitter,listener,null);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setOnDismissListener(this);
		
		mSpinner = new ProgressSpinner(getContext());
						
		mContent = new LinearLayout(getContext());
		mContent.setOrientation(LinearLayout.VERTICAL);
		
		setUpTitle();
		setUpWebView();

		Display display = getWindow().getWindowManager().getDefaultDisplay();
		final float scale = getContext().getResources().getDisplayMetrics().density;
		float[] dimensions = display.getWidth() < display.getHeight() ? DIMENSIONS_PORTRAIT
				: DIMENSIONS_LANDSCAPE;
		addContentView(mContent, new FrameLayout.LayoutParams(
				(int) (dimensions[0] * scale + 0.5f), (int) (dimensions[1]
						* scale + 0.5f)));

		retrieveRequestToken();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	public void show() {
		super.show();
		mSpinner.show();
	}

	private void setUpTitle() {
		//setTitle("Twitter");
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		Drawable icon=null;
		
		if (mIcon!=null) 
		  if (mIcon instanceof Integer) icon = getContext().getResources().getDrawable(((Integer) mIcon).intValue());
		
		mTitle = new TextView(getContext());
		mTitle.setText("Twitter");
		mTitle.setTextColor(Color.WHITE);
		mTitle.setTypeface(Typeface.DEFAULT_BOLD);
		//mTitle.setBackgroundColor(TW_BLUE);
		mTitle.setPadding(MARGIN + PADDING, MARGIN, MARGIN, MARGIN);
		mTitle.setCompoundDrawablePadding(MARGIN + PADDING);
		mTitle.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
		mContent.addView(mTitle);
		
	}

	private void retrieveRequestToken() {
		mSpinner.show();
		new Thread() {
			@Override
			public void run() {
				try {
					mRequestToken = mTwitter.getOAuthRequestToken(TwitterWrapper.CALLBACK_URI);
					mUrl = mRequestToken.getAuthorizationURL();
					Log.d(TAG,"mUrl="+mUrl);
					Throwable x=Utils.runInGuiAndWait(
					   mHandler, 
						new Runnable() {

							@Override
							public void run() {
								mWebView.loadUrl(mUrl);
								
							}
							
						}	
					);
					if (x!=null) throw x;
					/*
					TwDialog.this.mHandler.post(
						new Runnable() {

							@Override
							public void run() {
								mWebView.loadUrl(mUrl);
								
							}
							
						}	
					);
					*/
				} catch (Throwable e) {
					mListener.onError(new DialogError(e.getMessage(), -1,
							TwitterWrapper.OAUTH_REQUEST_TOKEN));
				}
			}
		}.start();
	}

	private void retrieveAccessToken(final String url) {
		mSpinner.show();
		new Thread() {
			@Override
			public void run() {
				final Bundle values = new Bundle();
				try {
					Uri uri=Uri.parse(url);
					String oav=uri.getQueryParameter("oauth_verifier");
					
					AccessToken at=null;
					
					if (oav!=null) {
						Log.d(TAG,"oauth_verifier="+oav);
						values.putString("oauth_verifier",oav);
						at = mTwitter.getOAuthAccessToken(mRequestToken,oav);
					} else
					    at = mTwitter.getOAuthAccessToken(mRequestToken);
					
					values.putString(TwitterWrapper.ACCESS_TOKEN, at.getToken());
					values.putString(TwitterWrapper.SECRET_TOKEN, at.getTokenSecret());
					
					mListener.onComplete(values);
				} catch (TwitterException e) {
					mListener.onTwitterError(new TwitterError(e.getMessage()));
				}
				mHandler.post(new Runnable() {
					public void run() {
						mSpinner.dismiss();
						TwDialog.this.dismiss();
					}
				});
			}
		}.start();
	}

	private void setUpWebView() {
		mWebView = new WebView(getContext());
		mWebView.setVerticalScrollBarEnabled(false);
		mWebView.setHorizontalScrollBarEnabled(false);
		mWebView.setWebViewClient(new TwDialog.TwWebViewClient());
		mWebView.getSettings().setJavaScriptEnabled(true);
//		mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
		// mWebView.loadUrl(mUrl);
		mWebView.setLayoutParams(FILL);
		mContent.addView(mWebView);
	}

	private class TwWebViewClient extends WebViewClient {

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Log.d(TAG, "Redirect URL: " + url);
			if (url.startsWith(TwitterWrapper.CALLBACK_URI)) {
				retrieveAccessToken(url);
				return true;
			} else if (url.startsWith(TwitterWrapper.CANCEL_URI)) {
				mListener.onCancel();
				TwDialog.this.dismiss();
				return true;
			}
			// launch non-dialog URLs in a full browser
			/*
			getContext().startActivity(
					new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
			return true;*/
			return false;
		}

		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
			super.onReceivedError(view, errorCode, description, failingUrl);
			mListener.onError(new DialogError(description, errorCode,
					failingUrl));
			TwDialog.this.dismiss();
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			Log.d(TAG, "WebView loading URL: " + url);
			super.onPageStarted(view, url, favicon);
			if (mSpinner.isShowing()) {
				mSpinner.dismiss();
			}
			mSpinner.show();
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			String title = mWebView.getTitle();
			if (title != null && title.length() > 0) {
				mTitle.setText(title);
			}
			mSpinner.dismiss();
		}

	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		if (mListener!=null) mListener.onDismiss();
	}
}
