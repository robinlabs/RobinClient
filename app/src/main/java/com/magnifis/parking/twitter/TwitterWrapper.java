package com.magnifis.parking.twitter;

import java.io.IOException;
import java.net.MalformedURLException;

import com.magnifis.parking.App;
import com.magnifis.parking.MainActivity;
import com.magnifis.parking.utils.Utils;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

public class TwitterWrapper {
	public static final String TAG = "twitter";
	
	public static final String CALLBACK_URI = /*"http://magnifisrobin.com";*/ "twitter://callback";
	public static final String CANCEL_URI = "twitter://cancel";
	public static final String ACCESS_TOKEN = "access_token";
	public static final String SECRET_TOKEN = "secret_token";

	public static final String REQUEST = "request";
	public static final String AUTHORIZE = "authorize";
	
	final protected static String  API_BASE="https://api.twitter.com/";

	final protected static String REQUEST_ENDPOINT = "https://api.twitter.com/1";
	
	final protected static String OAUTH_REQUEST_TOKEN = "https://api.twitter.com/oauth/request_token";
	final protected static String OAUTH_ACCESS_TOKEN = "https://api.twitter.com/oauth/access_token";
	final protected static String OAUTH_AUTHORIZE = "https://api.twitter.com/oauth/authorize";
    
	final public static String PERF_ACCESS_TOKEN="twitter:"+ACCESS_TOKEN;
	final public static String PERF_SECRET_TOKEN="twitter:"+SECRET_TOKEN;
	
	public static void resetPreferences(Context ctx) {
	  SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(ctx);
	  if (prefs.contains(PERF_ACCESS_TOKEN)||prefs.contains(PERF_SECRET_TOKEN)) {
		Editor ed=prefs.edit();
		ed.remove(PERF_ACCESS_TOKEN);
		ed.remove(PERF_SECRET_TOKEN);
		ed.commit();
	  }
	}
	
	public void resetPreferences() {
		if (prefs.contains(PERF_ACCESS_TOKEN)||prefs.contains(PERF_SECRET_TOKEN)) {
			Editor ed=prefs.edit();
			ed.remove(PERF_ACCESS_TOKEN);
			ed.remove(PERF_SECRET_TOKEN);
			ed.commit();
		}
	}
	
	private Activity context=null;
	
	private Object mIcon=null;
	private twitter4j.Twitter mTwitter=null;
	private SharedPreferences prefs=null;
	
	public TwitterWrapper(Activity ctx,String consumerKey, String consumerSecret) {
		this(ctx,null, consumerKey, consumerSecret );
	}
	
	public boolean anyAccessToken() {
	  return (prefs.contains(PERF_ACCESS_TOKEN)&&prefs.contains(PERF_SECRET_TOKEN));
	}
	
	public void restoreAccessToken() {
        String at=prefs.getString(PERF_ACCESS_TOKEN, null),
         	   st=prefs.getString(PERF_SECRET_TOKEN, null);
         
        if (at!=null&&st!=null) {
            AccessToken act=new AccessToken(at, st);
            getTwitter().setOAuthAccessToken(act);
        }		
	}
	
	public void saveAccessToken(Bundle values) {
	   Editor ed=prefs.edit();
	   
	   ed.putString(PERF_ACCESS_TOKEN, values.getString(TwitterWrapper.ACCESS_TOKEN));
	   ed.putString(PERF_SECRET_TOKEN, values.getString(TwitterWrapper.SECRET_TOKEN));
	   
	   ed.commit();
	}
	
	private String consumerKey, consumerSecret;
	
	public void resetEngine() {
		if (mTwitter!=null) try {
			mTwitter.shutdown();	
		} catch (Throwable t) {}
		mTwitter = new TwitterFactory().getInstance();
	    mTwitter.setOAuthConsumer(consumerKey, consumerSecret);	
	}
	
	public TwitterWrapper(Activity ctx,Object icon, String consumerKey, String consumerSecret) {
		mIcon = icon;

	    this.consumerKey=consumerKey;
	    this.consumerSecret=consumerSecret;
		
	    resetEngine();
	    
	    context=ctx;
	    prefs=PreferenceManager.getDefaultSharedPreferences(ctx);
	}
	
	public static void logout() {
		CookieSyncManager csm=CookieSyncManager.createInstance(App.self);
		CookieManager cm=CookieManager.getInstance();
		String q=cm.getCookie(API_BASE);
		if (!Utils.isEmpty(q)) {
			 cm.setCookie(API_BASE,"auth_token="/*q.replaceAll("auth_token=.+;", "")*/);
			 csm.sync();
			/*
        cookie:lang=en; 
        original_referer=JbKFAfGwv4RwApvTLqS%2BuUA9SZR5uydG0STMTRBXN6VhNp9NHTwobdwrT4z%2FwHnm7mIJqKSQaLwyiMbR%2Fi2vv5Qt56z4OYFDbWHxrjd1BnqHa7uBaaMXouV2FDje34%2FF;
         __utma=43838368.1210589942.1338314024.1338906849.1338909377.12;
          __utmb=43838368.7.10.1338909377; 
          __utmc=43838368; 
          __utmv=43838368.lang%3A%20en;
           __utmz=43838368.1338314024.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none);
            _twitter_sess=BAh7DjoMY3NyZl9pZCIlMzY4ZTkzZDAxNzE2YWFhMTMzMTYzNWI1ZDc5Mzk0%250AOTM6EHN0YXlfc2VjdXJlVDoJdXNlcmkE%252FE5bAjobc2Vzc2lvbl9wYXNzd29y%250AZF90b2tlbiItNDBiMzdjOGQ0YjgyMDNmZDIwYjk4OWUwOGVmMTFiZTg1MzJl%250AYTMyZToPY3JlYXRlZF9hdGwrCNhZu5k3AToOcmV0dXJuX3RvImNodHRwczov%250AL2FwaS50d2l0dGVyLmNvbS9vYXV0aC9hdXRob3JpemU%252Fb2F1dGhfdG9rZW49%250ARVRRVlVsRXM3amNxcU96VGxLbVhqbGg5Mnl1WGx2VjROeGk5b2NnMTk0Igpm%250AbGFzaElDOidBY3Rpb25Db250cm9sbGVyOjpGbGFzaDo6Rmxhc2hIYXNoewAG%250AOgpAdXNlZHsAOhNwYXNzd29yZF90b2tlbiItNDBiMzdjOGQ0YjgyMDNmZDIw%250AYjk4OWUwOGVmMTFiZTg1MzJlYTMyZToHaWQiJWRjYzQ0YzU3Y2UxZWZjNThl%250ANjFiNTRjNWY2YzkyYjJm--530a40fdadbd0ce1c4f679ba4925060b56a4030d; 
            auth_token=40b37c8d4b8203fd20b989e08ef11be8532ea32e;
             auth_token_session=true; guest_id=v1%3A13383140213352124; k=10.34.124.113.1338314021326509; secure_session=true; twid=u%3D39538428%7CjimepMdl4lTFM0ceh5Kb7PFnD30%3D; twll=l%3D1338314234
			 
			 */
		}
	}
	
	public void authorize(final DialogListener listener) {
	    CookieSyncManager.createInstance(context);
		dialog(context, new DialogListener() {

			public void onComplete(Bundle values) {
			CookieSyncManager.getInstance().sync();
				mTwitter.setOAuthAccessToken(new AccessToken(values.getString(ACCESS_TOKEN), values.getString(SECRET_TOKEN)));
				if (isSessionValid()) {
					Log.d(TAG, "token "+values.getString(ACCESS_TOKEN)+" "+values.getString(SECRET_TOKEN));
					saveAccessToken(values);
					if (listener!=null) listener.onComplete(values);
				} else {
					onTwitterError(new TwitterError("failed to receive oauth token"));
				}
			}

			public void onTwitterError(TwitterError e) {
				Log.d(TAG, "Login failed: "+e);
				if (listener!=null) listener.onTwitterError(e);
			}

			public void onError(DialogError e) {
				Log.d(TAG, "Login failed: "+e);
				if (listener!=null) listener.onError(e);
			}

			public void onCancel() {
				Log.d(TAG, "Login cancelled");
				if (listener!=null) listener.onCancel();
			}

			@Override
			public void onDismiss() {
				Log.d(TAG, "Login dismissed");
				if (listener!=null) listener.onDismiss();
			}
			
		});
	}
	
	private TwDialog dlg=null;
	
	public void abortDialog() {
	  if (dlg!=null) dlg.dismiss();
	}

	private void dialog(Context ctx, DialogListener listener) {
		(dlg=new TwDialog(ctx, mTwitter, listener, mIcon) {

			@Override
			protected void onStop() {
				super.onStop();
				dlg=null;
			}
			
		}
		).show();
	}
	
	public boolean isBusy() {
		return dlg!=null;
	}
	
	public boolean isSessionValid() {
		try {
			mTwitter.getId();
			return true;
		} catch (Throwable e) {
		
		}
		return false;
		 //return mTwitter.getAuthorization().isEnabled();
	}
	
	
	public twitter4j.Twitter getTwitter() {
	    return mTwitter;
	}
	
	public static interface DialogListener {
		public void onComplete(Bundle values);
		public void onDismiss();
		public void onTwitterError(TwitterError e);
		public void onError(DialogError e);
		public void onCancel();
	}
	
	public void consume(final Consumer csm) {
	   final Runnable runner=new Runnable() {
		   @Override
		   public void run() {
			  csm.onReady(getTwitter(), TwitterWrapper.this);
		   }
	   };
	   new Thread() {
		   @Override
		   public void run() {
			  if (isSessionValid()) {
				 runner.run();
				 return;
			  }
			  if (anyAccessToken()) {
				 restoreAccessToken();
				 if (isSessionValid()) {
				   runner.run();
				   return;
				 }				 
			  }
			  resetEngine();
			  context.runOnUiThread(
				 new Runnable() {
					@Override
					public void run() {
						authorize(
						   new DialogListener() {

							@Override
							public void onComplete(Bundle values) {
								runner.run();
							}

							@Override
							public void onTwitterError(TwitterError e) {
						        csm.onFailure();
								
							}

							@Override
							public void onError(DialogError e) {
								csm.onFailure();
							}

							@Override
							public void onCancel() {
								csm.onFailure();
							}

							@Override
							public void onDismiss() {
								csm.onFailure();	
							}
							
						   }
						);
					}
				 }
			  );
		   }
	   }.start();
	}
	
	public static interface Consumer {
	   public void onReady(Twitter tw,TwitterWrapper tww);
	   public void onFailure();
	}
}