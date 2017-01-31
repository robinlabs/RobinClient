package com.magnifis.parking.pref;
import java.lang.reflect.Field;

import com.magnifis.parking.utils.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.TypedArray;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;


public class IntListPreference extends ListPreference {
	
	private static String TAG=IntListPreference.class.getSimpleName();
	
	@Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
		if (a.hasValue(index)) {
		  return Integer.toString(a.getInt(index,-1));
		}
		return null;
	}		
    

	@Override
	protected String getPersistedString(String defaultReturnValue) {
		int d=Utils.isEmpty(defaultReturnValue)?-1:Integer.parseInt(defaultReturnValue);
		return Integer.toString( getPersistedInt(d) );
	}


	@Override
	protected boolean persistString(String value) {
		return super.persistInt(Integer.parseInt(value));
	}


	@Override
	public void setEntryValues(int entryValuesResId) {
		Context ctx=getContext();
		int ia[]=ctx.getResources().getIntArray(entryValuesResId);
		if (ia==null) super.setEntryValues(null); else {
			String sa[]=new String[ia.length];
			for (int i=0;i<ia.length;i++) sa[i]=Integer.toString(ia[i]);
			super.setEntryValues(sa);
		}
	}

	public IntListPreference(Context context) {
		this(context, null);
	}

	public IntListPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		int idsListPreference[]=Utils.getInternalAndroidResIds("styleable","ListPreference");
		int idListPreference_entryValues=Utils.getInternalAndroidResId("styleable", "ListPreference_entryValues");
		
        TypedArray a = context.obtainStyledAttributes(attrs, idsListPreference, 0, 0);
        int rId=a.getResourceId(idListPreference_entryValues, 0);
        setEntryValues(rId);
        a.recycle();
	}


}
