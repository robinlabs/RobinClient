package com.magnifis.parking.ads;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.client.methods.HttpUriRequest;

import com.google.android.vending.expansion.downloader.Constants;
import com.magnifis.parking.App;
import com.magnifis.parking.R;
import com.magnifis.parking.UserLocationProvider;
import com.magnifis.parking.R.string;
import com.magnifis.parking.db.RobinDB;
import com.magnifis.parking.model.PushAd;
import com.magnifis.parking.suzie.SuzieService;
import com.magnifis.parking.utils.Http;
import com.magnifis.parking.utils.Utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

public class AdManager extends BroadcastReceiver {

	private static final String TAG = AdManager.class.getSimpleName();
    static final String TRACKING_URL = App.self.getString(R.string.ad_self_tracking_url);
	public static final String TRACKING_STATUS_IMPRESSION_RECEIVED = "1"; 
	public static final String TRACKING_STATUS_CONVERTED = "2"; 
	public static final String TRACKING_STATUS_STARTED = "3"; 
	public static final String TRACKING_STATUS_STARTED_BY_ROBIN = "4"; 
	public static final String TRACKING_STATUS_ALREADY_INSTALLED = "0"; 
	public static final String TRACKING_STATUS_INVALID_URL = "-1"; 
	

	
	public static String getTrackingUrl(String packageName, String status) {
		String countryCode = UserLocationProvider.getCountry();	
		
		StringBuffer ackUrlBuf = new StringBuffer(TRACKING_URL)
		.append("install=").append(App.self.android_id)
		.append("&package=").append(packageName)
		.append("&status=").append(status);
		
		if (!Utils.isEmpty(countryCode))
			ackUrlBuf.append("&country=").append(countryCode);
		
		return ackUrlBuf.toString();
	}


	public static void sendToServer(String packageName, String status) {
		String url = AdManager.getTrackingUrl(packageName, status);
		
		if (url != null)
			try {
				HttpURLConnection  x = (HttpURLConnection)new URL(url).openConnection();
				x.setAllowUserInteraction(false);
				x.setUseCaches(false);
				x.getInputStream().close();
			    x.disconnect();
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	
	@Override
	public void onReceive(final Context context, Intent it) {
	  if (Intent.ACTION_PACKAGE_ADDED.equals(it.getAction())) {
		  Log.d(TAG,"ACTION_PACKAGE_ADDED:");
		  Intent it1=(Intent)it.clone();
		  it1.setClass(App.self, SuzieService.class);
		  App.self.startService(it1);
	  }
 	}
}
