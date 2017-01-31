package com.magnifis.parking.views;

import com.magnifis.parking.R;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class WParkingRate extends ScalableShort {

	TextView when,howMach;

	public WParkingRate(Context context,String text) {
		super(context);
		
		LayoutParams lp=new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
		setLayoutParams(lp);
		
		when=new TextView(context);
		lp=new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		lp.addRule(ALIGN_PARENT_LEFT);
		when.setLayoutParams(lp);
		when.setTextAppearance(context,R.style.rateHour);
		addView(when);
		
		howMach=new TextView(context);
		lp=new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		lp.addRule(ALIGN_PARENT_RIGHT);
		howMach.setLayoutParams(lp);
		howMach.setTextAppearance(context,R.style.rateCost);
		addView(howMach);
		
		int i=text.lastIndexOf(':');
		if (i<0) {
			when.setText(text);
		} else {
			when.setText(text.substring(0, i/*+1*/));
			howMach.setText(text.substring(i+1));
		}
	}

}
