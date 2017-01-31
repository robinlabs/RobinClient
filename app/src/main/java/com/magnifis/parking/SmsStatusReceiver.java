package com.magnifis.parking;

import com.magnifis.parking.feed.SmsFeedController;
import com.magnifis.parking.utils.Utils;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class SmsStatusReceiver extends BroadcastReceiver {
	final static String TAG=SmsStatusReceiver.class.getSimpleName();
	

	@Override
	public void onReceive(Context context, Intent it) {
		Log.d(TAG, "onReceive ...");
		// forward the intent to packageName 
	  it.putExtra(SmsFeedController.STATUS, getResultCode());
	  
	  ComponentName cn=it.getParcelableExtra(SmsFeedController.COMPONENT);
	  if (cn==null) {
		  SmsFeedController.getInstance().
		     handleSendingStatusIntent(App.self,
					App.self.voiceIO.getOperationTracker(), it);
	  } else {
		// forward the intent to packageName 
		 it.putExtra(SmsFeedController.STATUS, getResultCode());
		 it.setComponent(cn);
		 it.removeExtra(SmsFeedController.COMPONENT);
		 
		 
		  if (Utils.isService(cn)) try {
			App.self.startService(it);  
		  } catch (Throwable t) {} else {
			Utils.startActivityFromNowhere(it);
		  }
	  }
	   
	}

}
