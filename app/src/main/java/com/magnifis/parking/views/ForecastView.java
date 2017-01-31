package com.magnifis.parking.views;

import com.magnifis.parking.R;
import com.magnifis.parking.model.GooWeatherForecastCondition;
import static com.magnifis.parking.utils.Utils.*;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

public class ForecastView extends Scalable {
	
	TextView dayView, temprView;
	ImageView forecastWeatherIc;
	
	Scalable content = null;
	
	public void setData(GooWeatherForecastCondition fc,WeatherView wv) {
		forecastWeatherIc.setVisibility(GONE);
		
		int tH=wv.celsius
				  ?f2c(fc.getHigh())
				  :fc.getHigh();
		
	    int tL=wv.celsius
				  ?f2c(fc.getLow())
				  :fc.getLow();
				  
		temprView.setText(tL+WeatherView.DEGREE+"|"+tH+WeatherView.DEGREE);
		dayView.setText(fc.getDayOfWeek());

		wv.loadImage(fc.getIcon(), forecastWeatherIc);
	}

	public ForecastView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
        LayoutInflater inflater = LayoutInflater.from(context);
        content = (Scalable)inflater.inflate(R.layout.forecast, this, false);
        addView(content);
        
        forecastWeatherIc=(ImageView)findViewById(R.id.ForecastWeatherIc);
        temprView=(TextView)findViewById(R.id.ForecastWeatherTempr);
        dayView=(TextView)findViewById(R.id.ForecastWeatherDay);
	}

	public ForecastView(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}

	public ForecastView(Context context) {
		this(context,null);
	}

}
