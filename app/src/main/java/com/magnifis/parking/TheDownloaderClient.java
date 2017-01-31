package com.magnifis.parking;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Intent;
import android.os.Messenger;
import android.util.Log;

import com.google.android.vending.expansion.downloader.DownloadProgressInfo;
import com.google.android.vending.expansion.downloader.DownloaderClientMarshaller;
import com.google.android.vending.expansion.downloader.DownloaderServiceMarshaller;
import com.google.android.vending.expansion.downloader.IDownloaderClient;
import com.google.android.vending.expansion.downloader.IDownloaderService;
import com.google.android.vending.expansion.downloader.IStub;
import com.magnifis.parking.utils.Utils;

import static com.magnifis.parking.utils.Utils.*;

public class TheDownloaderClient implements IDownloaderClient {
	
	final static String TAG=TheDownloaderClient.class.getSimpleName();

	private IStub mDownloaderClientStub = null;
	private IDownloaderService mRemoteService = null;
	
	private void stopService() {
	  Intent it=new Intent();
	  it.setClass(App.self, TheDownloader.class);
	  try {
	     App.self.stopService(it);
	  } catch(Throwable t) {}
	}
	
	public void abortDownloadAndReleaseTheService() {
        if (null != mDownloaderClientStub) {
        	Log.d(TAG,"dnl:release");
        	try {
        	  mRemoteService.requestAbortDownload();
        	} catch(Throwable t) {}
            mDownloaderClientStub.disconnect(App.self);
            mDownloaderClientStub=null;
            setTimeout(
               new Runnable() {
				@Override
				public void run() {
					stopService();
					
				}   
               },
               800l
            );
        }
	}
	
	public void releaseOnly() {
        if (null != mDownloaderClientStub) {
        	Log.d(TAG,"dnl:release0");
            mDownloaderClientStub.disconnect(App.self);
            mDownloaderClientStub=null;
            mRemoteService=null;
        }
	}
	
	public TheDownloaderClient() {
	    mDownloaderClientStub = DownloaderClientMarshaller.CreateStub(this, TheDownloader.class);
	    if (mDownloaderClientStub!=null)mDownloaderClientStub.connect(App.self);		
	}


	@Override
	public void onServiceConnected(Messenger m) {
		Log.d(TAG,"dnl:onServiceConnected");
        mRemoteService = DownloaderServiceMarshaller.CreateProxy(m);
        mRemoteService.onClientUpdated(mDownloaderClientStub.getMessenger());
	}

	private Intent createIt() {
	   Intent it=new Intent(MainActivity.EXPANSION_DOWNLOADER_NOTIFICATION);
	   it.setClass(App.self, MainActivity.class);
	   return it;
	}
	
	private void send(Intent it) {
		try {
			PendingIntent.getActivity(App.self, 0, it, PendingIntent.FLAG_UPDATE_CURRENT).send();
		} catch (CanceledException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private boolean success=false, failure=false;

	public boolean isSuccess() {
		return success;
	}

	public boolean isFailure() {
		return failure;
	}
	
	public boolean isWorking() {
		return (mRemoteService!=null&&!(success||failure));
	}
	
	private void doFailure() {
    	if (!failure) {
      	  failure=true;
      	  Intent it=createIt();
      	  it.putExtra(MainActivity.EXPANSION_DOWNLOADER_FAILURE, true);
      	  send(it);
      	}
	}

	private int lastState=-1;
	
	@Override
	public void onDownloadStateChanged(int newState) {
		Log.d(TAG,"dnl:onDownloadStateChanged "+newState);	
		lastState=newState;
		switch (newState) {
		case IDownloaderClient.STATE_FETCHING_URL:
			Utils.setTimeout(
			  new Runnable() {
				@Override
				public void run() {
				  if (lastState==IDownloaderClient.STATE_FETCHING_URL)
					  doFailure();
				}
				  
			  }, 
			  10000L
			);
			break;
		// failure:
		case IDownloaderClient.STATE_PAUSED_NETWORK_UNAVAILABLE:
			if (false/*debug*/) {
				this.abortDownloadAndReleaseTheService();
			} else
				break;
		
        case IDownloaderClient.STATE_FAILED_CANCELED:
        case IDownloaderClient.STATE_FAILED:
        case IDownloaderClient.STATE_FAILED_FETCHING_URL:
        case IDownloaderClient.STATE_FAILED_UNLICENSED:
        	Log.d(TAG,"dnl:failure");
        	doFailure();
        	break;
        // success:
        case IDownloaderClient.STATE_COMPLETED:
        	Log.d(TAG,"dnl:success");
        	if (!success) {
        	  success=true;
        	  send(createIt());
        	}
		}
	}


	@Override
	public void onDownloadProgress(DownloadProgressInfo progress) {
		
	}

}
