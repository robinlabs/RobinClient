package com.magnifis.parking.views;


import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.magnifis.parking.App;
import com.magnifis.parking.Log;
import com.magnifis.parking.R;
import com.magnifis.parking.RatingStars;
import com.magnifis.parking.model.PkFacility;
import com.magnifis.parking.model.Poi;
import com.magnifis.parking.utils.ImageFetcher;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import static com.magnifis.parking.utils.Utils.*;

public class PoiDetails extends Details<Poi> {
	
	TextView nameView, categoryView, recView, basedOnNReviews;
	ImageView iv, ratingView;
	LinearLayout hoursView0, hoursView1, hoursView2;
	
	@Override
	public void setData(Poi fas) {
        super.setData(fas);
        String name=fas.getName(), category=fas.getCategory(),
        	
        		
        		// TODO: restore?
        		rec=fas.getAttribution(), 
        	  
        		
        		
        		//fas.getRecommendation(),
        	  /*
        	     inLandscape
        	        ?fas.getRecommendationForDetailsView(7)
        	        :fas.getRecommendationForDetailsView(),
        	        */
        	  iurl=fas.getImageUrl();
        
        if (!isEmpty(name)) {
        	SpannableString ul=underline(name);
        	nameView.setText(ul);
        	/*
        	while (nameView.getLineCount()>2) {
        		nameView.setTextSize(nameView.getTextSize()-2);
        		nameView.invalidate();
        	}
        	*/
        }
        if (!isEmpty(category)) categoryView.setText(category);
        if (!(recView==null||isEmpty(rec)))  { 
        	Log.d(TAG, "Attribution is: " + rec); 
        	
        	//recView.setText(underline(StringEscapeUtils.unescapeHtml(text)));
        	recView.setText(Html.fromHtml(rec));
        	recView.setMovementMethod(LinkMovementMethod.getInstance());
        	
        } else if (recView != null) {
        	recView.setHeight(0); 
        }
        
        if (!(hoursView0==null||fas.getHours()==null||isEmpty(fas.getHours().getDays()))) {
        	hoursView0.removeAllViews();
        	hoursView1.removeAllViews();
        	if (hoursView2!=null) hoursView2.removeAllViews();
        	
        	SimpleDateFormat fHHmm=new SimpleDateFormat("HH:mm");
        	
         	int dayc=0;
        	for (Poi.Day day:fas.getHours().getDays()) if (!isEmpty(day.getOpen())) {
        	  LinearLayout tr=new ScalableLLShort(getContext());
        	  if (hoursView2==null)
        	    (dayc>3?hoursView1:hoursView0).addView(tr);
        	  else {
        		 if (dayc<3) hoursView0.addView(tr); else
        	     if (dayc<6) hoursView1.addView(tr); else
        	    	 hoursView2.addView(tr);
        	  }
        	  TextView dayName=new TextView(getContext());
        	  LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(
        		 38, LayoutParams.WRAP_CONTENT
        	  );
        	  lp.gravity=Gravity.LEFT;
        	  dayName.setLayoutParams(lp);
        	  
              dayName.setTextSize(TypedValue.COMPLEX_UNIT_PX, 15);
              dayName.setTextColor(Color.WHITE);      
              dayName.setText(
                 getContext()
                  .getResources()
                  .getStringArray(R.array.daysOfWeekShort)[day.getSeq()]
              );
              tr.addView(dayName);
              LinearLayout cell=new ScalableLLShort(getContext());
              cell.setOrientation(LinearLayout.VERTICAL);
              tr.addView(cell);
              for (Poi.Open op:day.getOpen()) {
            	 TextView ot=new TextView(getContext()); 
            	 
            	 lp = new LinearLayout.LayoutParams(
            			 LayoutParams.WRAP_CONTENT,
            			 LayoutParams.WRAP_CONTENT);
            	 lp.gravity = Gravity.RIGHT;
            	 ot.setLayoutParams(lp);
            	 
            	 ot.setTextSize(TypedValue.COMPLEX_UNIT_PX, 15);
            	 ot.setTextColor(Color.WHITE);
            	 cell.addView(ot);
            	 ot.setText(fHHmm.format(op.getFrom())+" - "+fHHmm.format(op.getTo()));
            	 ++dayc;
              }
        	}
        }
        
        if (!isEmpty(iurl))
			try {
				ImageFetcher.setImageTo(iurl, iv, true);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
        
        Double r=fas.getRating();
        if (r!=null) ratingView.setImageDrawable(RatingStars.get(r));
        
        Integer nr=fas.getReviewCount();
        
        if (nr!=null) basedOnNReviews.setText(underline(
        	"("+  nr + (nr==1?" " + App.self.getString(R.string.poidetails_review):" " + App.self.getString(R.string.poidetails_reviews))+")"
        ));
        
        /*
        
        if (!isEmpty(rurl))
			try {
				ImageFetcher.setImageTo(rurl, ratingView);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		*/
	}
	
	@Override
	protected void afterRotation() {
		super.afterRotation();
        nameView=(TextView)findViewById(R.id.Name);
        categoryView=(TextView)findViewById(R.id.Category);
        recView=(TextView)findViewById(R.id.Recommendation);
        basedOnNReviews=(TextView)findViewById(R.id.BasedOnNReviews);
        hoursView0=(LinearLayout)findViewById(R.id.Hours0);
        hoursView1=(LinearLayout)findViewById(R.id.Hours1);
        hoursView2=(LinearLayout)findViewById(R.id.Hours2);
        
        iv=(ImageView)findViewById(R.id.Image);
        ratingView=(ImageView)findViewById(R.id.RatingImage);		
	}

	public PoiDetails(Context context, int vLayoutId, int hLayoutId) {
		super(context, null, 0, vLayoutId, hLayoutId);
	}

}
