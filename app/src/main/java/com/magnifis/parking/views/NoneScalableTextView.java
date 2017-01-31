package com.magnifis.parking.views;

import com.magnifis.parking.R;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class NoneScalableTextView extends TextView {

	public NoneScalableTextView(Context context) {
		this(context,null);
	}

	public NoneScalableTextView(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}

	public NoneScalableTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setTag(R.string.don_t_scale_font, true);
	}

}
