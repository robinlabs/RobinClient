package com.magnifis.parking;

import android.app.PendingIntent.CanceledException;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;

import com.google.android.vending.expansion.downloader.IDownloaderClient;
import com.google.android.vending.expansion.downloader.impl.DownloadNotification;
import com.google.android.vending.expansion.downloader.impl.DownloaderService;
import com.magnifis.parking.R;

public class TheDownloader extends DownloaderService {
	
	final static String TAG=TheDownloader.class.getSimpleName();
	
    public static final byte[] SALT = new byte[] {
        1, 43, -12, -1, 54, 98,
        -100, -12, 43, 2, -8, -4, 9, 5, -106, -108, -33, 45, -1, 84
    };

	@Override
	public String getPublicKey() {
		return App.self.getString(R.string.publisher_public_key_base64);
	}

	@Override
	public byte[] getSALT() {
		// TODO Auto-generated method stub
		return SALT;
	}

	@Override
	public String getAlarmReceiverClassName() {
		return DnlAlarmReceiver.class.getName();
	}
	
	int failed_fetching_ulr_counter=0;

	@Override
	public void onCreate() {
		super.onCreate();
       /*
		ApplicationInfo ai = getApplicationInfo();
		CharSequence applicationLabel = getPackageManager().getApplicationLabel(ai);
		mNotification = new DownloadNotification(this, applicationLabel) {

			@Override
			public void onDownloadStateChanged(int newState) {
				boolean show=true;
				switch (newState) {
				    case IDownloaderClient.STATE_FAILED_FETCHING_URL:  
				    	if ((failed_fetching_ulr_counter++&1)!=0) break;
				    	show=false;
					case IDownloaderClient.STATE_COMPLETED:
				}
				if (show) super.onDownloadStateChanged(newState);
			}
			
		};
*/
        
		setDownloadFlags(DownloaderService.FLAGS_DOWNLOAD_OVER_CELLULAR);
	}

	@Override
	public void onDestroy() {
		if (mNotification!=null) {
		  Log.d(TAG, "mNotification.cancel()");	
		  mNotification.cancel();
		  mNotification=null;
		}
		super.onDestroy();
	}

}
