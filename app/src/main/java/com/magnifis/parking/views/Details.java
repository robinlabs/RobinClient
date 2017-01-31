package com.magnifis.parking.views;


import com.magnifis.parking.App;
import com.magnifis.parking.Log;
import com.magnifis.parking.MainActivity;
import com.magnifis.parking.R;
import com.magnifis.parking.model.GeoObject;
import com.magnifis.parking.model.PkFacility;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import static com.magnifis.parking.utils.Utils.*;

public class Details<T extends GeoObject> extends ScalableShort {

	
	protected TextView 
	    parkingAddress,
	    parkingPhone;
	
	
	protected View callBtn, btnCallEnv , btnGoThere;
	
	private T lastDataSet=null;
	
	public void setData(T fas) {
		lastDataSet=fas;
		String addr=fas.getAddress();
		if (addr==null) addr=fas.getName();
		setOrHide(parkingAddress,addr);
		
	    if (!isEmpty(fas.getPhone())) {
		   setOrHide(parkingPhone,underline(fas.getPhone()));
		   btnCallEnv.setVisibility(VISIBLE);
	    } else {
	       parkingPhone.setVisibility(View.GONE);
	       btnCallEnv.setVisibility(GONE);
	    }
	}
	
	
	
	protected RelativeLayout content = null;
	protected LayoutInflater inflater;
	private  int loResourceV, loResourceH;

	public Details(Context context, AttributeSet attrs, int defStyle, int loResource) {
	   this(context, attrs, defStyle, loResource,loResource );
	}
	
	public Details(Context context, AttributeSet attrs, int defStyle, int loResource, int loResourceH) {
		super(context, attrs, defStyle);
		this.loResourceV=loResource;
		this.loResourceH=loResourceH;
		
		inLandscape=App.self.isInLanscapeMode();
        inflater = LayoutInflater.from(context);
        
        afterRotation();
        
        setTag(R.string.remove_on_hiding, "true");
	}
	
	protected boolean inLandscape;
	
	protected void afterRotation() {
        removeAllViews();
		
        content = (RelativeLayout)inflater.inflate(inLandscape?R.layout.details_landscape:R.layout.details, this, false);
        addView(content);
        
        RelativeLayout specificDetailsHolder=(RelativeLayout)findViewById(R.id.SpecificDetailsHolder);
        
        View parkingDetails=inflater.inflate(
        	inLandscape?loResourceH:loResourceV, specificDetailsHolder, false
        );
        
        specificDetailsHolder.addView(parkingDetails, 0);       
        
        parkingAddress=(TextView) findViewById(R.id.ParkingAddress) ;
        parkingPhone= (TextView) findViewById(R.id.ParkingPhone) ;
        callBtn= findViewById(R.id.BtnCall);
        btnCallEnv= findViewById(R.id.BtnCallEnv);
        btnGoThere= findViewById(R.id. BtnGoThere);
	}
	
	@Override
	protected void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		boolean f=App.self.isInLanscapeMode();
		if (f!=inLandscape) {
			inLandscape=f;
			afterRotation();
			if (lastDataSet!=null) setData(lastDataSet);
		}
	}

}
