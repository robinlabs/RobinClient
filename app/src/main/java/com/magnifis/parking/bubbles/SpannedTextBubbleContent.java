package com.magnifis.parking.bubbles;

import android.text.SpannableString;
import android.text.SpannedString;
import android.view.View;

public class SpannedTextBubbleContent extends SpannableString implements IBubbleContent {
	public SpannedTextBubbleContent(CharSequence source) {
		super(source);
	}

	@Override
	public boolean isMenuRestricted() {
		return false;
	}
	
}
