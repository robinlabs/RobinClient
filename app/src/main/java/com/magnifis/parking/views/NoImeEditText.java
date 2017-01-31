package com.magnifis.parking.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EditText;

public class NoImeEditText extends EditText {

	public NoImeEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	//public boolean allowKeyboard = false;
	public OnClickListener myOnClickListener = null;
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		/*
		if (event.getAction() == MotionEvent.ACTION_DOWN && myOnClickListener != null) {
			setEnabled(true);
	    	requestFocus();
	    	setCursorVisible(true);
			myOnClickListener.onClick(this);
			setEnabled(false);
			return true;
		}*/
		//setEnabled(false);
		boolean x = super.onTouchEvent(event); 
		//setEnabled(true);
		if (event.getAction() == MotionEvent.ACTION_UP) {
			//setEnabled(false);
			//setEnabled(true);
			//setCursorVisible(true);
		}
		return x;
	}
	
	@Override      
	public boolean onCheckIsTextEditor() {   
		  return true;     
	}        
		  
}
