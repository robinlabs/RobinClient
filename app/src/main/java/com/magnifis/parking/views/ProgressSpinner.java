package com.magnifis.parking.views;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.widget.ProgressBar;

public class ProgressSpinner extends Dialog {

	public ProgressSpinner(Context context) {
		this(context,0);
	}

	public ProgressSpinner(Context context, int theme) {
		this(context, false, null);
	}

	public ProgressSpinner(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		
		ProgressBar pb=new ProgressBar(context);
		pb.setIndeterminate(true);
		setContentView(pb);
	
	}

}
