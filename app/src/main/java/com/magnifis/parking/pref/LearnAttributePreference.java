package com.magnifis.parking.pref;

import com.magnifis.parking.model.LearnAttribute;

import android.app.Activity;
import android.content.Context;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.util.AttributeSet;

public class LearnAttributePreference extends EditTextPreference {

	@Override
	protected void onClick() {
       LearnAttribute la=new LearnAttribute(getKey());
	   la.learnFromTextInput((Activity)getContext(), LearnAttribute.getDefVisiblePart(getKey()), null);
	}

	public LearnAttributePreference(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public LearnAttributePreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public LearnAttributePreference(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

}
