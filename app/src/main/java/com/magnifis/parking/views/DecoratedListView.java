package com.magnifis.parking.views;


import com.magnifis.parking.App;
import com.magnifis.parking.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DecoratedListView extends RelativeLayout {
	
	protected ListView listView;
	protected TextView footer;
	
	
	public ListView getListView() {
		return listView;
	}

	public TextView getFooter() {
		return footer;
	}

	public void setFooterText(CharSequence text) {
		SpannableString content = new SpannableString(text);
		content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
		footer.setText(content);
		footer.setVisibility(VISIBLE);
	}

	public DecoratedListView(Context context) {
		this(context,null);
	}

	public DecoratedListView(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}
	
	private boolean inLandscape;
	
	private int ID_FOOTER=101010,ID_LIST=101011;

	public DecoratedListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		inLandscape=App.self.isInLanscapeMode();
	
		footer=new TextView(context);
		footer.setGravity(Gravity.CENTER_HORIZONTAL);
		footer.setTextAppearance(context, android.R.style.TextAppearance_DeviceDefault_Large);
		footer.setTextColor(Color.BLACK);  
		footer.setVisibility(GONE);
		footer.setClickable(true);
		footer.setBackgroundResource(android.R.drawable.list_selector_background);
		footer.setId(ID_FOOTER);
		int p=App.self.toPxSize(8/*dp*/);
		footer.setPadding(0, p, 0, p);
	
		afterRotation();
		addView(footer);

		
		listView=new ListView(context);
		listView.setBackgroundColor(Color.WHITE);
		LayoutParams  lp=new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
		lp.addRule(ALIGN_PARENT_TOP);
		lp.addRule(this.ABOVE, ID_FOOTER);
		listView.setLayoutParams(lp);
		listView.setId(ID_LIST);
		addView(listView);
	}
	
	protected void afterRotation() {
		LayoutParams lp=new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT/*280*/);
		lp.addRule(ALIGN_PARENT_BOTTOM);
		
		lp.leftMargin=lp.rightMargin= App.self.toPxSize(30/*dp*/);
		lp.bottomMargin= inLandscape?0:App.self.toPxSize(3);
		
		footer.setLayoutParams(lp);		
	}
	
	@SuppressLint("NewApi") @Override
	protected void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		boolean f=App.self.isInLanscapeMode();
		if (f!=inLandscape) {
			inLandscape=f;
			afterRotation();
		}
	}

}
