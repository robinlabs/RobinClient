package com.magnifis.parking;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;

public class RatingStars {
	 final static int stars[]={
		R.drawable.stars_0,
		R.drawable.stars_0,
		R.drawable.stars_1,	
		R.drawable.stars_1_half,
		R.drawable.stars_2,	
		R.drawable.stars_2_half,
		R.drawable.stars_3,	
		R.drawable.stars_3_half,
		R.drawable.stars_4,
		R.drawable.stars_4_half,
		R.drawable.stars_5,	
	 };
	 
	 public static Drawable get(double r) {
         int ix=(int) Math.round(r*2.);
         return  App.self.getResources().getDrawable(stars[(ix<=10)?ix:10]);
	 }
}
