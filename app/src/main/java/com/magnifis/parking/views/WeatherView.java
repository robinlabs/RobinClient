package com.magnifis.parking.views;

import java.net.MalformedURLException;
import java.util.TimeZone;

import com.magnifis.parking.App;
import com.magnifis.parking.R;
import com.magnifis.parking.model.GooWeather;
import com.magnifis.parking.model.GooWeatherCurrentCondition;
import com.magnifis.parking.model.GooWeatherForecastCondition;
import com.magnifis.parking.utils.ImageFetcher;
import com.magnifis.parking.utils.MeasurementSystem;
import com.magnifis.parking.utils.Utils;

import static com.magnifis.parking.utils.Utils.*;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class WeatherView extends Scalable {
	
	final static String TAG=WeatherView.class.getSimpleName();
	
	final static String GOO="http://www.google.com";
	final static String DEGREE="\u00B0";
	
	ImageView currentWeatherIc;
	TextView currentWeatherCond, currentWeatherTempr, currentWeatherWind;
	
	ForecastView fvs[]=new ForecastView[4];
	
	
	//MultipleEventHandler meh;	
	
	public void loadImage(String ru,final ImageView iv) {
		try {
			if (ru.indexOf("://")<0) ru=GOO+ru;
			ImageFetcher.setImageTo(ru,iv,null,true);
			iv.setVisibility(VISIBLE);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}		
	}
	

	
	private final static String MEAS_SYSTEM="temperatureSystem", CELSIUS="celsius";
	
	public static boolean tempInCelsius() {
      SharedPreferences prefs=App.self.getPrefs();
      if ((prefs.getString(MEAS_SYSTEM, null)==null) && Utils.isBooleanPrefSet(CELSIUS)) {
    	// upgrade from a previous version  
    	  Editor ed=prefs.edit();
    	  boolean c=prefs.getBoolean(CELSIUS, false);
    	  ed.remove(CELSIUS);
    	  ed.putString(MEAS_SYSTEM, c?"c":"f");
    	  ed.commit();
    	  return c;
      }
      String temperatureSystem=App.self.getStringPref(MEAS_SYSTEM);
      if ("a".equals(temperatureSystem)) return MeasurementSystem.detectIfTempInCelsius();
      return "c".equals(temperatureSystem)?true:false;	
	}
	
	boolean celsius=false;
	
	public void setData(GooWeather weather) {	
		GooWeatherCurrentCondition cc=weather.getCurrentConditions();
		loadImage(cc.getIcon(),currentWeatherIc);
		GooWeatherForecastCondition fc[]=weather.getForecast();
		
		celsius=tempInCelsius();
		
		for (int i=0;i<fc.length&&i<fvs.length;i++) {
			if (i==0) 
				fc[0].setDayOfWeek(App.self.getString(R.string.P_today));
			
			fvs[i].setData(fc[i],this);
		}
		currentWeatherCond.setText(cc.getCondition());
		
		String t=celsius
		  ?(f2c(cc.getTempF())+DEGREE+" C")
		  :(cc.getTempF()+DEGREE+" F");
		currentWeatherTempr.setText(t);
		currentWeatherWind.setText((cc.getWind_condition()==null)?"":cc.getWind_condition());
		setVisibility(View.VISIBLE);
	}
		
	Scalable content = null;
	
	public WeatherView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
        LayoutInflater inflater = LayoutInflater.from(context);
        content = (Scalable)inflater.inflate(R.layout.weather, this, false);
        addView(content);
        
        currentWeatherIc=(ImageView)findViewById(R.id.CurrentWeatherIc);
        currentWeatherCond=(TextView)findViewById(R.id.CurrentWeatherCond);
        currentWeatherTempr=(TextView)findViewById(R.id.CurrentWeatherTempr);
        currentWeatherWind=(TextView)findViewById(R.id.CurrentWeatherWind);
        
        fvs[0]=(ForecastView)findViewById(R.id.ForecastView0);
        fvs[1]=(ForecastView)findViewById(R.id.ForecastView1);
        fvs[2]=(ForecastView)findViewById(R.id.ForecastView2);
        fvs[3]=(ForecastView)findViewById(R.id.ForecastView3);
 
    }

	public WeatherView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public WeatherView(Context context) {
		this(context,null);
	}

}
