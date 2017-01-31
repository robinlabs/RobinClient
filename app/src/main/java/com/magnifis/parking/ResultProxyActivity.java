package com.magnifis.parking;

import com.magnifis.parking.utils.Utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;

/***
 * 
 * @author zeev
 * This is a special activity intended to support "startActivityForResult" call from a service
 *  
 */
public class ResultProxyActivity extends Activity {
	public static String TAG=ResultProxyActivity.class.getSimpleName(),
		ACTION_START_ACTIVITY_FOR_RESULT="com.magnifis.parking.START_ACTIVITY_FOR_RESULT",
		START_ACTIVITY_FOR_RESULT_INTENT="it",
		START_ACTIVITY_FOR_RESULT_REQUEST_CODE="rc",
		START_ACTIVITY_FOR_RESULT_RECEIVER="rcvr",
		ACTION_ACTIVITY_RESULT="com.magnifis.parking.ACTIVITY_RESULT",
	    ACTIVITY_RESULT_INTENT="it",
	    ACTIVITY_RESULT_REQUEST_CODE="rc",
	    ACTIVITY_RESULT_RESULT_CODE="resCode",
	    ACTIVITY_RESULT_EXCEPTION="ex"
	;
	
	/****
	 * Expected to be called from a service
	 * 
	 * @param wanted
	 * @param requestCode
	 * @param receiver
	 */
	public static void startActivityFromServiceForResult(Intent wanted, int requestCode, ComponentName receiver) {
		Intent it=new Intent(ACTION_START_ACTIVITY_FOR_RESULT);
		it.setClass(App.self, ResultProxyActivity.class);
		it.putExtra(START_ACTIVITY_FOR_RESULT_INTENT, (Parcelable)wanted);
		it.putExtra(START_ACTIVITY_FOR_RESULT_REQUEST_CODE, requestCode);
		it.putExtra(START_ACTIVITY_FOR_RESULT_RECEIVER, (Parcelable)receiver);
		it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Utils.startActivityFromNowhere(it);
	}
	
	public static void startActivityFromServiceForResult(Intent wanted, int requestCode, Class receiver) {
		startActivityFromServiceForResult(wanted, requestCode, new ComponentName(App.self,receiver));
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Intent it=getIntent();
		int rqc=it.getIntExtra(START_ACTIVITY_FOR_RESULT_REQUEST_CODE, 0);
		if (requestCode==rqc) {
		   Intent r=new Intent(ACTION_ACTIVITY_RESULT);
		   r.setComponent((ComponentName)it.getParcelableExtra(START_ACTIVITY_FOR_RESULT_RECEIVER));
		   r.putExtra(ACTIVITY_RESULT_REQUEST_CODE, rqc);
		   r.putExtra(ACTIVITY_RESULT_RESULT_CODE,resultCode);
		   r.putExtra(ACTIVITY_RESULT_INTENT, data);
		   startService(r);
		   finish();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent it=this.getIntent();
		if (ACTION_START_ACTIVITY_FOR_RESULT.equals(it.getAction())) try {
			//////
		   Launchers.startNestedActivityWithCode(
			  this, 
		      (Intent)it.getParcelableExtra(START_ACTIVITY_FOR_RESULT_INTENT), it.getIntExtra(START_ACTIVITY_FOR_RESULT_REQUEST_CODE, 0), null
		   );
		   /*
		   startActivityForResult( 
		     (Intent)it.getParcelableExtra(START_ACTIVITY_FOR_RESULT_INTENT), 
		     it.getIntExtra(START_ACTIVITY_FOR_RESULT_REQUEST_CODE, 0)
		   );
		   */
		   ///////
		   return;
		} catch (android.content.ActivityNotFoundException x) {
		   Intent r=new Intent(ACTION_ACTIVITY_RESULT);
		   r.setComponent((ComponentName)it.getParcelableExtra(START_ACTIVITY_FOR_RESULT_RECEIVER));
		   r.putExtra(ACTIVITY_RESULT_REQUEST_CODE, it.getIntExtra(START_ACTIVITY_FOR_RESULT_REQUEST_CODE, 0));
		   r.putExtra(ACTIVITY_RESULT_EXCEPTION, x.getMessage());
		   startService(r);
		}
		finish();
	}

	@Override
	protected void onResume() {
		super.onResume();

		Log.d(TAG,"onResume");
		App.self.setActiveActivity(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();

		Log.d(TAG,"onPause");
		App.self.removeActiveActivity(this);
	}
	
	@Override
	protected void onStop() {
		super.onStop();

		Log.d(TAG,"onStop");
		App.self.notifyStopActivity(this);
	}

}
