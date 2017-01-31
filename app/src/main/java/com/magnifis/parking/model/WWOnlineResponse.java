package com.magnifis.parking.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.magnifis.parking.App;
import com.magnifis.parking.R;
import com.magnifis.parking.Xml.ML;
import static com.magnifis.parking.utils.Utils.*;

public class WWOnlineResponse {
  public static class Forecast {
	  @ML(tag="date", format="yyyy-MM-dd")
	  protected Date date=null;
	  @ML("tempMaxC")
	  protected Integer tempMaxC=null;
	  @ML("tempMaxF")
	  protected Integer tempMaxF=null;
	  @ML("tempMinC")
	  protected Integer tempMinC=null;
	  @ML("tempMinF")
	  protected Integer tempMinF=null;
	  @ML("windspeedMiles")
	  protected Integer windspeedMiles=null;
	  @ML("windspeedKmph")
	  protected Integer windspeedKmph=null;	  
	  @ML("winddirection")
	  protected String winddirection=null;
	  @ML("winddir16Point")
	  protected String winddir16Point=null;
	  @ML("winddirDegree")
	  protected Integer winddirDegree=null;
	  @ML("weatherCode")
	  protected Integer weatherCode=null;
	  @ML("weatherIconUrl")
	  protected String weatherIconUrl=null;
	  @ML("weatherDesc")
	  protected String weatherDesc=null;
	  @ML("precipMM")
	  protected Double precipMM=null;
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public Integer getTempMaxC() {
		return tempMaxC;
	}
	public void setTempMaxC(Integer tempMaxC) {
		this.tempMaxC = tempMaxC;
	}
	public Integer getTempMaxF() {
		return tempMaxF;
	}
	public void setTempMaxF(Integer tempMaxF) {
		this.tempMaxF = tempMaxF;
	}
	public Integer getTempMinC() {
		return tempMinC;
	}
	public void setTempMinC(Integer tempMinC) {
		this.tempMinC = tempMinC;
	}
	public Integer getTempMinF() {
		return tempMinF;
	}
	public void setTempMinF(Integer tempMinF) {
		this.tempMinF = tempMinF;
	}
	public Integer getWindspeedMiles() {
		return windspeedMiles;
	}
	public void setWindspeedMiles(Integer windspeedMiles) {
		this.windspeedMiles = windspeedMiles;
	}
	public Integer getWindspeedKmph() {
		return windspeedKmph;
	}
	public void setWindspeedKmph(Integer windspeedKmph) {
		this.windspeedKmph = windspeedKmph;
	}
	public String getWinddirection() {
		return winddirection;
	}
	public void setWinddirection(String winddirection) {
		this.winddirection = winddirection;
	}
	public String getWinddir16Point() {
		return winddir16Point;
	}
	public void setWinddir16Point(String winddir16Point) {
		this.winddir16Point = winddir16Point;
	}
	public Integer getWinddirDegree() {
		return winddirDegree;
	}
	public void setWinddirDegree(Integer winddirDegree) {
		this.winddirDegree = winddirDegree;
	}
	public Integer getWeatherCode() {
		return weatherCode;
	}
	public void setWeatherCode(Integer weatherCode) {
		this.weatherCode = weatherCode;
	}
	public String getWeatherIconUrl() {
		return weatherIconUrl;
	}
	public void setWeatherIconUrl(String weatherIconUrl) {
		this.weatherIconUrl = weatherIconUrl;
	}
	public String getWeatherDesc() {
		return weatherDesc;
	}
	public void setWeatherDesc(String weatherDesc) {
		this.weatherDesc = weatherDesc;
	}
	public Double getPrecipMM() {
		return precipMM;
	}
	public void setPrecipMM(Double precipMM) {
		this.precipMM = precipMM;
	}

/*
<date>2012-08-26</date>
<tempMaxC>34</tempMaxC>
<tempMaxF>93</tempMaxF>
<tempMinC>24</tempMinC>
<tempMinF>75</tempMinF>
<windspeedMiles>12</windspeedMiles>
<windspeedKmph>19</windspeedKmph>
<winddirection>NW</winddirection>
<winddir16Point>NW</winddir16Point>
<winddirDegree>305</winddirDegree>
<weatherCode>113</weatherCode>
<weatherIconUrl>http://www.worldweatheronline.com/images/wsymbols01_png_64/wsymbol_0001_sunny.png</weatherIconUrl>
<weatherDesc>Sunny</weatherDesc>
<precipMM>0.0</precipMM> 
 */	  
  }
  public static class CurrentCondition {
	  /*
	  @ML(tag="observation_time", format="h:mm a")
	  protected Date observationTime=null;
	  */
	  @ML("temp_C")
	  protected Integer tempC=null;
	  @ML("temp_F")
	  protected Integer tempF=null;
	  @ML("weatherCode")
	  protected Integer weatherCode=null;
	  @ML("weatherIconUrl")
	  protected String weatherIconUrl=null;
	  @ML("weatherDesc")
	  protected String weatherDesc=null;
	  @ML("windspeedMiles")
	  protected Integer windspeedMiles=null;
	  @ML("windspeedKmph")
	  protected Integer windspeedKmph=null;	  
	  @ML("winddir16Point")
	  protected String winddir16Point=null;
	  @ML("winddirDegree")
	  protected Integer winddirDegree=null;
	  @ML("precipMM")
	  protected Double precipMM=null;
	  @ML("humidity")
	  protected Integer humidity=null;
	  @ML("visibility")
	  protected Integer visibility=null;
	  @ML("pressure")
	  protected Integer pressure=null;
	  @ML("cloudcover")
	  protected Integer cloudcover=null;
	  
	  /*
	public Date getObservationTime() {
		return observationTime;
	}
	public void setObservationTime(Date observationTime) {
		this.observationTime = observationTime;
	}
	*/
	public Integer getTempC() {
		return tempC;
	}
	public void setTempC(Integer tempC) {
		this.tempC = tempC;
	}
	public Integer getTempF() {
		return tempF;
	}
	public void setTempF(Integer tempF) {
		this.tempF = tempF;
	}
	public Integer getWeatherCode() {
		return weatherCode;
	}
	public void setWeatherCode(Integer weatherCode) {
		this.weatherCode = weatherCode;
	}
	public String getWeatherIconUrl() {
		return weatherIconUrl;
	}
	public void setWeatherIconUrl(String weatherIconUrl) {
		this.weatherIconUrl = weatherIconUrl;
	}
	public String getWeatherDesc() {
		return weatherDesc;
	}
	public void setWeatherDesc(String weatherDesc) {
		this.weatherDesc = weatherDesc;
	}
	public Integer getWindspeedMiles() {
		return windspeedMiles;
	}
	public void setWindspeedMiles(Integer windspeedMiles) {
		this.windspeedMiles = windspeedMiles;
	}
	public Integer getWindspeedKmph() {
		return windspeedKmph;
	}
	public void setWindspeedKmph(Integer windspeedKmph) {
		this.windspeedKmph = windspeedKmph;
	}

	public String getWinddir16Point() {
		return winddir16Point;
	}
	public void setWinddir16Point(String winddir16Point) {
		this.winddir16Point = winddir16Point;
	}
	public Integer getWinddirDegree() {
		return winddirDegree;
	}
	public void setWinddirDegree(Integer winddirDegree) {
		this.winddirDegree = winddirDegree;
	}
	public Double getPrecipMM() {
		return precipMM;
	}
	public void setPrecipMM(Double precipMM) {
		this.precipMM = precipMM;
	}
	public Integer getHumidity() {
		return humidity;
	}
	public void setHumidity(Integer humidity) {
		this.humidity = humidity;
	}
	public Integer getVisibility() {
		return visibility;
	}
	public void setVisibility(Integer visibility) {
		this.visibility = visibility;
	}
	public Integer getPressure() {
		return pressure;
	}
	public void setPressure(Integer pressure) {
		this.pressure = pressure;
	}
	public Integer getCloudcover() {
		return cloudcover;
	}
	public void setCloudcover(Integer cloudcover) {
		this.cloudcover = cloudcover;
	}
	  
/**
<observation_time>08:05 AM</observation_time>
<temp_C>33</temp_C>
<temp_F>91</temp_F>
<weatherCode>113</weatherCode>
<weatherIconUrl>http://www.worldweatheronline.com/images/wsymbols01_png_64/wsymbol_0001_sunny.png</weatherIconUrl>
<weatherDesc>Sunny</weatherDesc>
<windspeedMiles>4</windspeedMiles>
<windspeedKmph>6</windspeedKmph>
<winddirDegree>280</winddirDegree>
<winddir16Point>W</winddir16Point>
<precipMM>0.0</precipMM>
<humidity>47</humidity>
<visibility>10</visibility>
<pressure>1008</pressure>
<cloudcover>0</cloudcover>  
 * */	  
  }
  public static class Request {
	 @ML("type")
	 protected String type=null;
	 @ML("query")
	 protected String query=null;
	 
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}

  }
  @ML("request")
  protected Request request=null;
  @ML("current_condition")
  protected CurrentCondition currentCondition=null;
  @ML("weather")
  protected Forecast forecasts[]=null;
  public Request getRequest() {
	  return request;
  }
  public void setRequest(Request request) {
	  this.request = request;
  }
  public CurrentCondition getCurrentCondition() {
	  return currentCondition;
  }
  public void setCurrentCondition(CurrentCondition currentCondition) {
	  this.currentCondition = currentCondition;
  }
  public Forecast[] getForecasts() {
	  return forecasts;
  }
  public void setForecasts(Forecast[] forecasts) {
	  this.forecasts = forecasts;
  }
  
  public GooWeather toGooWeather() {
/**** http://www.google.com/ig/api?weather=San+Francisco+California
08-26 12:52:51.617: D/Fetcher<T>(13052): <xml_api_reply version="1">
08-26 12:52:51.617: D/Fetcher<T>(13052): <weather module_id="0" tab_id="0" mobile_row="0" mobile_zipped="1" row="0" section="0">
08-26 12:52:51.617: D/Fetcher<T>(13052): <forecast_information>
08-26 12:52:51.617: D/Fetcher<T>(13052): <city data="San Francisco, CA"/>
08-26 12:52:51.617: D/Fetcher<T>(13052): <postal_code data="San Francisco California"/>
08-26 12:52:51.617: D/Fetcher<T>(13052): <latitude_e6 data=""/>
08-26 12:52:51.617: D/Fetcher<T>(13052): <longitude_e6 data=""/>
08-26 12:52:51.617: D/Fetcher<T>(13052): <forecast_date data="2012-08-26"/>
08-26 12:52:51.617: D/Fetcher<T>(13052): <current_date_time data="2012-08-26 08:56:00 +0000"/>
08-26 12:52:51.617: D/Fetcher<T>(13052): <unit_system data="US"/></forecast_information>
08-26 12:52:51.617: D/Fetcher<T>(13052): <current_conditions>
08-26 12:52:51.617: D/Fetcher<T>(13052): <condition data="Overcast"/>
08-26 12:52:51.617: D/Fetcher<T>(13052): <temp_f data="57"/>
08-26 12:52:51.617: D/Fetcher<T>(13052): <temp_c data="14"/>
08-26 12:52:51.617: D/Fetcher<T>(13052): <humidity data="Humidity: 77%"/>
08-26 12:52:51.617: D/Fetcher<T>(13052): <icon data="/ig/images/weather/cloudy.gif"/>
08-26 12:52:51.617: D/Fetcher<T>(13052): <wind_condition data="Wind: W at 15 mph"/></current_conditions>
08-26 12:52:51.617: D/Fetcher<T>(13052): <forecast_conditions>
08-26 12:52:51.617: D/Fetcher<T>(13052): <day_of_week data="Sun"/>
08-26 12:52:51.617: D/Fetcher<T>(13052): <low data="54"/>
08-26 12:52:51.617: D/Fetcher<T>(13052): <high data="63"/>
08-26 12:52:51.617: D/Fetcher<T>(13052): <icon data="/ig/images/weather/mostly_sunny.gif"/>
08-26 12:52:51.617: D/Fetcher<T>(13052): <condition data="Mostly Sunny"/></forecast_conditions>
08-26 12:52:51.617: D/Fetcher<T>(13052): <forecast_conditions>
08-26 12:52:51.617: D/Fetcher<T>(13052): <day_of_week data="Mon"/>
08-26 12:52:51.617: D/Fetcher<T>(13052): <low data="54"/>
08-26 12:52:51.617: D/Fetcher<T>(13052): <high data="68"/>
08-26 12:52:51.617: D/Fetcher<T>(13052): <icon data="/ig/images/weather/mostly_sunny.gif"/>
08-26 12:52:51.617: D/Fetcher<T>(13052): <condition data="Mostly Sunny"/></forecast_conditions>
08-26 12:52:51.617: D/Fetcher<T>(13052): <forecast_conditions>
08-26 12:52:51.617: D/Fetcher<T>(13052): <day_of_week data="Tue"/>
08-26 12:52:51.617: D/Fetcher<T>(13052): <low data="52"/>
08-26 12:52:51.617: D/Fetcher<T>(13052): <high data="73"/>
08-26 12:52:51.617: D/Fetcher<T>(13052): <icon data="/ig/images/weather/mostly_sunny.gif"/>
08-26 12:52:51.617: D/Fetcher<T>(13052): <condition data="Mostly Sunny"/></forecast_conditions>
08-26 12:52:51.617: D/Fetcher<T>(13052): <forecast_conditions>
08-26 12:52:51.617: D/Fetcher<T>(13052): <day_of_week data="Wed"/>
08-26 12:52:51.617: D/Fetcher<T>(13052): <low data="52"/>
08-26 12:52:51.617: D/Fetcher<T>(13052): <high data="70"/>
08-26 12:52:51.617: D/Fetcher<T>(13052): <icon data="/ig/images/weather/fog.gif"/>
08-26 12:52:51.617: D/Fetcher<T>(13052): <condition data="Fog"/></forecast_conditions></weather></xml_api_reply>

 */
	  
	  if (currentCondition==null || isEmpty(forecasts)) 
		  return null;
	  
	  GooWeather gw=new GooWeather();
	  GooWeatherCurrentCondition gc=new GooWeatherCurrentCondition();
	  //if (currentCondition!=null) {
		  CurrentCondition cc=currentCondition;
		  gc.wind_condition=App.self.getString(R.string.P_wind) + " " +cc.winddir16Point+" "+cc.windspeedMiles+" mph";
		  gc.condition=cc.weatherDesc;   
		  
		  // change weather description text to Russian
		  if (App.self.isInRussianMode()) {
			  gc.condition = gc.condition
				 .replaceAll("Partly Cloudy", "Переменная Облачность")
		         .replaceAll("Cloudy", "Облачно")
			     .replaceAll("Sunny", "Солнечно")
			     .replaceAll("Mostly", "В основном")
			     .replaceAll("Clear", "Ясно")
			     .replaceAll("Overcast", "Облачно")
			     .replaceAll("Mist", "Туман")
			     .replaceAll("Patchy light rain", "Местами дождь")
			     .replaceAll("Patchy rain nearby", "Местами дождь")
			     .replaceAll("Light rain shower", "Небольшой ливневый дождь")
			     .replaceAll("Light snow showers", "Небольшой снегопад")
			     .replaceAll("Light snow", "Небольшой снег")
			     .replaceAll("Moderate snow", "Умеренный снег")
			     .replaceAll("Thundery outbreaks in nearby", "Гроза")
			     .replaceAll("Blowing snow", "Метель")
			     .replaceAll("Freezing fog", "Морозный туман")
			     .replaceAll("Fog", "Туман")
			     .replaceAll("Blizzard", "Метель")
			     .replaceAll("Patchy snow nearby", "Местами снег")
			     .replaceAll("Moderate or heavy snow showers", "Снегопад");
		  }
		  
		  gc.tempC=cc.tempC;
		  gc.tempF=cc.tempF;
		  gc.icon=cc.weatherIconUrl;
		  gc.humidity="Humidity: "+cc.humidity.toString()+"%";
		  gw.currentConditions=gc;
	  //}
	  //if (!isEmpty(forecasts)) {
	    SimpleDateFormat sdf=new SimpleDateFormat("EEE");
		gw.forecast=new GooWeatherForecastCondition[forecasts.length];
		for (int i=0;i<forecasts.length;i++) {
		  GooWeatherForecastCondition gf=gw.forecast[i]=new GooWeatherForecastCondition();
          Forecast fc=forecasts[i];
          gf.Condition=fc.weatherDesc;
          gf.high=fc.tempMaxF;
          gf.low=fc.tempMinF;
          gf.icon=fc.weatherIconUrl;
          gf.dayOfWeek=sdf.format(fc.date);
		//}
	  }
	  return gw;
  }

}
