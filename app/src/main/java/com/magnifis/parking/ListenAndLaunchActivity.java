package com.magnifis.parking;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.widget.ProgressBar;

import com.magnifis.parking.cmd.i.ClientStateInformer;
import com.magnifis.parking.model.Understanding;
import com.magnifis.parking.suzie.RequiresSuzie;
import com.magnifis.parking.suzie.SuzieService;
import com.magnifis.parking.utils.Utils;
import com.magnifis.parking.views.ProgressSpinner;

public class ListenAndLaunchActivity extends Activity implements RequiresSuzie {
	
	final static String TAG=ListenAndLaunchActivity.class.getSimpleName();
	
	VR vr=null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.d(TAG,"LL.create");
		   
		Intent it= getIntent();
		
		App.self.setActiveActivity(this);

		if (SuzieService.isSuzieVisible()) {
			Log.d(TAG,"LL.create: suzie");
			vr = VR.get();
			vr.start(true);
			finish();
		}
		else if (!(/*Config.oldmic||*/(MainActivity.get()==null))) {
			Log.d(TAG,"LL.create: main");
			if (Intent.ACTION_VOICE_COMMAND.equals(it.getAction()) && VR.get() != null)
				VR.get().markCallByVoiceCommand();		

			it=new Intent(MainActivity.WAKE_UP);
		   it.setClass(this, MainActivity.class);
		   
		   boolean locked=Config.allow_sms_dialogs_while_phone_is_locked?false:App.self.isPhoneLocked();
		   Log.d(TAG,"locked="+locked);
		   
		   if (!locked&&(Utils.getRobinTaskIndex(this,2)!=null)) 
			   it.putExtra(MainActivity.EXTRA_REQUEST_SEARCH, true);
		  
		   
		   startActivity(it);
		   if (locked)
			  new Thread() {
			   @Override
			   public void run() {
				   try {
					   sleep(1000);
				   } catch (InterruptedException e) {
					   // TODO Auto-generated catch block
					   e.printStackTrace();
				   }
			   }
		     }.start();
			finish();
		   
		} else {
			Log.d(TAG,"LL.create: std");
			vr=VR.create(this,null,true,Intent.ACTION_VOICE_COMMAND.equals(it.getAction()));
			vr.start(true);
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent it) {
		Log.d(TAG, "LL.onActivityResult");
    	switch (requestCode) {
    	case VR.VOICE_RECOGNITION_REQUEST_CODE:
    		Log.d(TAG, "LL.VR results");
    		
    		if (resultCode==Activity.RESULT_FIRST_USER)
    			return;

            if (resultCode==VR.RESULT_RUN_INTENT)
                return;

            if (resultCode==Activity.RESULT_OK) {
    		   it.setAction(MainActivity.VR_RESULTS);
   			   it.setClass(this, MainActivity.class);
    		   startActivity(it);
    		   Log.d(TAG,"LL.Launching main packageName");
    		}
    		finish();
    		return;
    	}
		super.onActivityResult(requestCode, resultCode, it);
	}

	@Override
	protected void onResume() {
		super.onResume();

		Log.d(TAG,"LL.resume");
		App.self.setActiveActivity(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();

		Log.d(TAG,"LL.pause");
		App.self.removeActiveActivity(this);
	}
	
	@Override
	protected void onStop() {
		super.onStop();

		Log.d(TAG,"LL.stop");
		App.self.notifyStopActivity(this);
	}

	@Override
	public boolean isRequiringSuzie() {
		return true;
	}
	
}
