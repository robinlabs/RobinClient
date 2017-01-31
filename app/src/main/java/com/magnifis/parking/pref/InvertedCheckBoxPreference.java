/**
 * 
 */
package com.magnifis.parking.pref;

import com.magnifis.parking.utils.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.Checkable;

/**
 * @author zeev
 *
 */
public class InvertedCheckBoxPreference extends CheckBoxPreference {
	
	
	/*
	@Override
	protected Parcelable onSaveInstanceState() {
		Bundle b=new Bundle();
		b.putBoolean("defaultValue", defaultValue);
		b.putParcelable("superState", super.onSaveInstanceState());
		return b;
	}


	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if (state!=null&&state instanceof Bundle) {
		   Bundle b=(Bundle)state;
		   defaultValue=b.getBoolean("defaultValue");
		   super.onRestoreInstanceState(b.getParcelable("superState"));
		}
	}*/

	protected boolean defaultValue;
	
	
	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		Object o=super.onGetDefaultValue(a, index);
		if (o instanceof Boolean) defaultValue=(Boolean)o;
		return o;
	}
	

	protected CheckBox checkable = null;
	
    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        
        int com_android_internal_R_id_checkbox=Utils.getInternalAndroidResId("id", "checkbox");

        View checkboxView = view.findViewById(com_android_internal_R_id_checkbox);
        if (checkboxView != null && checkboxView instanceof Checkable) {
        	SharedPreferences sp=getSharedPreferences();
        	checkable=((CheckBox) checkboxView);
        	checkable.setChecked(!sp.getBoolean(getKey(), defaultValue));
        }
  
    }


	@Override
	protected void onClick() {
		if (checkable!=null) {
			SharedPreferences sp=getSharedPreferences();
			Editor ed=sp.edit();
			boolean ch=checkable.isChecked();
			checkable.setChecked(!ch);
			ed.putBoolean(getKey(),ch);
			ed.commit();
		}
	}




	/**
	 * @param packageName
	 */
	public InvertedCheckBoxPreference(Context context) {
		super(context);
	}

	/**
	 * @param packageName
	 * @param attrs
	 */
	public InvertedCheckBoxPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * @param packageName
	 * @param attrs
	 * @param defStyle
	 */
	public InvertedCheckBoxPreference(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

}
