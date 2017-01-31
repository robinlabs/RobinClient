package com.magnifis.parking.views;

import java.net.URL;

import org.w3c.dom.Document;

import com.magnifis.parking.App;
import com.magnifis.parking.MainActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.LevelListDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class ScalableLLShort extends LinearLayout {
	static final String TAG=ScalableLLShort.class.getSimpleName();
	


	public ScalableLLShort(Context context, AttributeSet attrs) {
	   super(context,attrs);
	}

	public ScalableLLShort(Context context) {
	  this(context,null);
	}
	
	@Override
	public void addView(View child, int index, android.view.ViewGroup.LayoutParams params) {
		if (!isInEditMode()) App.self.scaler.scaleItShort(child,params);
		
	//	inLandscape=App.self.isInLanscapeMode();
		
		super.addView(child, index, params);
	}
	/*
	protected boolean inLandscape;
	
	@SuppressLint("NewApi")
	@Override
	protected void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		boolean f=App.self.isInLanscapeMode();
		if (f!=inLandscape) {
			inLandscape=f;
			Log.d(TAG, "rescale for "+App.self.getDspResolutionString());
			for (int i=0;i<getChildCount();i++)  {
			   View child=getChildAt(i);
		   	   App.self.scaler.scaleItShort(child,child.getLayoutParams());
			}
		}
	}
*/
}
