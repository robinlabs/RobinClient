package com.magnifis.parking.views;

import com.magnifis.parking.Log;
import com.magnifis.parking.MainActivity;
import com.magnifis.parking.ProximityWakeUp;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

public class MainView extends ScalableShort {
	final static String TAG=MainView.class.getSimpleName();


	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			 Log.d(TAG,"ACTION_DOWN!!!");
            ProximityWakeUp.reset();
			 //ProximityWakeUp.stop(getContext());
			 break;
		case MotionEvent.ACTION_UP:
            ProximityWakeUp.reset();
			 Log.d(TAG,"ACTION_UP!!!");
             //MainActivity.get().scs[0]=ProximityWakeUp.start(MainActivity.get());
			 break;
	     default:
	    	 //Log.d(TAG,"ACTION_!!!"); 
	    	 
		 }	
		return super.onInterceptTouchEvent(ev);
	}

	public MainView(Context context) {
		this(context, null);
	}

	public MainView(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}

	public MainView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

}
