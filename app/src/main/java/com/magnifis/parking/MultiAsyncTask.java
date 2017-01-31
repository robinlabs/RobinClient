package com.magnifis.parking;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;

public abstract class MultiAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
	
    @SuppressLint("NewApi")
	public  MultiAsyncTask<Params, Progress, Result> multiExecute(Params... params) {
		  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
			   executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
			else 
			   execute(params);
		  return this;
    }
    
    public MultiAsyncTask() {}
}
