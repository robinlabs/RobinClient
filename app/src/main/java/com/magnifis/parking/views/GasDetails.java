package com.magnifis.parking.views;


import java.net.MalformedURLException;

import com.magnifis.parking.Log;
import com.magnifis.parking.R;
import com.magnifis.parking.RatingStars;
import com.magnifis.parking.model.GasStation;
import static com.magnifis.parking.model.GasStation.*;

import com.magnifis.parking.model.GasPrice;
import com.magnifis.parking.model.PkFacility;
import com.magnifis.parking.model.Poi;
import com.magnifis.parking.utils.ImageFetcher;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
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
import android.widget.ImageView;
import android.widget.TextView;
import static com.magnifis.parking.utils.Utils.*;

public class GasDetails extends Details<GasStation> {
	
	TextView nameView, tvRegular, tvPlus, tvPremium, tvDiesel;
	ImageView iv;
	
	void setPrice(TextView tv, GasPrice price) {
        if (!isEmpty(price)) {
        	
        	if (price.isMin()) tv.setTextColor(Color.GREEN);
        	  
            if (price.isMin()) 
               tv.setText("(best price)  $"+price.getPrice());
            else
        	   tv.setText("$"+price.getPrice());
        }
	}
	
	@Override
	public void setData(GasStation fas) {
        super.setData(fas);
        String name=fas.getName(), 
        	  iurl=fas.getImageUrl(); 
        
        if (!isEmpty(name)) {
        	SpannableString ul=underline(name);
        	nameView.setText(ul);
        }

        if (!isEmpty(iurl))
			try {
				ImageFetcher.setImageTo(iurl, iv, true);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
        
        setPrice(tvRegular, fas.getRegularPrice());
        setPrice(tvPlus, fas.getPlusPrice());
        setPrice(tvPremium, fas.getPremiumPrice());
        setPrice(tvDiesel, fas.getDieselPrice());
	}
	
	
	public GasDetails(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle,R.layout.gas_details);
		afterRotation();
	}
	
	@Override
	protected void afterRotation() {
		super.afterRotation();
        nameView=(TextView)findViewById(R.id.Name);
        
        tvRegular=(TextView)findViewById(R.id.PriceRegular);
        tvPlus=(TextView)findViewById(R.id.PricePlus);
        tvPremium=(TextView)findViewById(R.id.PricePremium);
        tvDiesel=(TextView)findViewById(R.id.PriceDiesel);
        
        iv=(ImageView)findViewById(R.id.Image);
	}

	public GasDetails(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}

	public GasDetails(Context context) {
		this(context,null);
	}

}
