package com.magnifis.parking.views;

import java.net.URL;

import org.w3c.dom.Document;

import com.magnifis.parking.App;
import com.magnifis.parking.MainActivity;

import android.content.Context;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class Scalable extends RelativeLayout {
	static final String TAG="Scalable";
	
	public Scalable(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public Scalable(Context context, AttributeSet attrs) {
	   this(context,attrs,0);
	}

	public Scalable(Context context) {
	  this(context,null,0);
	}
	
	@Override
	public void addView(View child, int index, android.view.ViewGroup.LayoutParams params) {
		if (!isInEditMode()) App.self.scaler.scaleItXY(child,params);
		
		super.addView(child, index, params);
	}
	
}
