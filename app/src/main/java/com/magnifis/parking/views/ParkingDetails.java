package com.magnifis.parking.views;


import com.magnifis.parking.App;
import com.magnifis.parking.Log;
import com.magnifis.parking.R;
import com.magnifis.parking.model.PkFacility;

import android.content.Context;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import static com.magnifis.parking.utils.Utils.*;

public class ParkingDetails extends Details<PkFacility> {
	
/******
 * 
 *  Undeground: 43px
Weekdays: 23px
Rate hours: 25px BOLD, RGB: #3d4665
Rate cost: 25px, RGB: #3d4665
Address: 20px
Tel: 34px
Red frame: 34px
 * 
 */
	
	TextView 
	    parkingType, parkingHrs,
	    parkingLoadText;
	
	ViewGroup parkingRates;
	
	View parkingLoadEnv;
	
	@Override
	public void setData(PkFacility fas) {
		setOrHide(parkingType,fas.getType());
		if (fas.getHrs()!=null) {
		  StringBuilder sb=new StringBuilder();
		  boolean first=true;
		  for (String s:fas.getHrs()) if (!isEmpty(s))  {
			  if (first) first=false; else sb.append('\n');
			  sb.append(s);
		  }
		  parkingHrs.setText(sb);
		  parkingHrs.setVisibility(VISIBLE);
		} else
		  parkingHrs.setVisibility(GONE);
		removeSubviews(parkingRates,WParkingRate.class);
		
		
		if (fas.getRates()!=null) for (String s:fas.getRates())
		  if (!isEmpty(s)) parkingRates.addView(new WParkingRate(getContext(),s));
		
		/***
		 * 
		 * "rates": [
             "1 Hour: $8",
             "Daily: $14",
             "Early Bird (In By 7:30am): $10"
          ],

		 * 
		 */
        super.setData(fas);
	    
	    int load=fas.getLoad();
	    if (load==PkFacility.LOAD_UNKNOWN) parkingLoadEnv.setVisibility(GONE); else {
	    	switch (load) {
	    	case PkFacility.LOAD_HIGH: 
	    		parkingLoadEnv.setBackgroundResource(R.drawable.red_details);
	    		break;
	    	case PkFacility.LOAD_LOW:
	    		parkingLoadEnv.setBackgroundResource(R.drawable.green_details);
	    		break;
	    	case PkFacility.LOAD_MEDIUM:;
    		    parkingLoadEnv.setBackgroundResource(R.drawable.orange_details);
	    	}
	    	parkingLoadText.setText(fas.getOccupancy_pct()+"%");
	    	parkingLoadEnv.setVisibility(VISIBLE);
	    }
	}
	
	
	public ParkingDetails(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle, R.layout.parking_details, R.layout.parking_details_landscape);
	}
	
	@Override
	protected void afterRotation() {
		super.afterRotation();
        parkingLoadEnv=(RelativeLayout)inflater.inflate(
        	App.self.isInLanscapeMode()
        	  ?R.layout.parking_load_landscape
        	  :R.layout.parking_load
            ,
        	content, false
        );
        
        
        content.addView(parkingLoadEnv);
               
        parkingType=(TextView) findViewById(R.id.ParkingType) ;
        parkingHrs=(TextView) findViewById(R.id.ParkingHrs) ;
        parkingRates= (ViewGroup) findViewById(R.id.Rates) ;
        parkingLoadText= (TextView) findViewById(R.id.ParkingLoadText);
		
	}

	public ParkingDetails(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}

	public ParkingDetails(Context context) {
		this(context,null);
	}

}
