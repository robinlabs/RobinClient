package com.magnifis.parking.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class TransparentImageView extends ImageView {

	public TransparentImageView(Context context) {
		this(context,null);
		// TODO Auto-generated constructor stub
	}

	public TransparentImageView(Context context, AttributeSet attrs) {
		this(context, attrs,0);

	}

	public TransparentImageView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		this.setAlpha(0xc0);
	}

}
