package com.magnifis.parking;

import com.magnifis.parking.utils.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

public abstract class RunningInActivity implements Runnable {
	final static String TAG=RunningInActivity.class.getSimpleName();//Launchers.TAG;
	
	
	protected Activity activity=null;
	protected boolean usingProxyActivity=false;
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {}
	
    public Activity getActivity() {
		return activity;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}
	
	public RunningInActivity(Context ctx) {
		this(ctx, RunCallbackActivity.class);
	}
	
	protected Class<? extends Activity> proxyClass=null;
	
	public void killProxy() {
		
	}
	
	final static String OBJECT_KEY="object_key";

	public RunningInActivity(Context ctx, Class<? extends Activity> proxyClass) {
		 this.proxyClass=proxyClass;
    	 if (ctx!=null&&ctx instanceof Activity) {
    		Log.d(TAG,"ctx!=null&&ctx instanceof Activity");
    		activity=(Activity)ctx;
    		run();
    	 } else {
    		 Log.d(TAG,"useProxy");
    		usingProxyActivity=true;
    		App.self.setToRunInActivity(this);
    		Intent it=new Intent();
    		it.setClass(App.self, proxyClass);
    		it.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
    		it.putExtra(OBJECT_KEY, System.identityHashCode(this));
    		Activity ac=App.self.getActiveActivity();
    		if (ac==null)
    		  Utils.startActivityFromNowhere(it);
    		else
    		  ac.startActivity(it);
    	 }
     }
	
	 public boolean onBackPressed() { return false; } 
	
     public abstract void run();

     public boolean isRequiringSuzie() { return false; }
}
