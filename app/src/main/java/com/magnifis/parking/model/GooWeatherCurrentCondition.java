package com.magnifis.parking.model;

import com.magnifis.parking.Xml.ML;

public class GooWeatherCurrentCondition {
/***
 * <condition data="Mostly Cloudy"/>
 * <temp_f data="46"/>
 * <temp_c data="8"/>
 * <humidity data="Humidity: 71%"/>
 * <icon data="/ig/images/weather/mostly_cloudy.gif"/>
 * <wind_condition data="Wind: W at 24 mph"/>
 */
  @ML(tag="condition",attr="data")
  protected String condition=null;
  @ML(tag="temp_f",attr="data")
  protected Integer tempF=null;
  @ML(tag="temp_c",attr="data")
  protected Integer tempC=null;
  @ML(tag="humidity",attr="data") 
  protected String humidity=null;
  @ML(tag="icon",attr="data") 
  protected String icon=null;
  @ML(tag="wind_condition",attr="data")
  protected String wind_condition=null;
  /**
   * @return the condition
   */
  public String getCondition() {
	  return condition;
  }
  /**
   * @param condition the condition to set
   */
  public void setCondition(String condition) {
	  this.condition = condition;
  }
  /**
   * @return the tempF
   */
  public Integer getTempF() {
	  return tempF;
  }
  /**
   * @param tempF the tempF to set
   */
  public void setTempF(Integer tempF) {
	  this.tempF = tempF;
  }
  /**
   * @return the tempC
   */
  public Integer getTempC() {
	  return tempC;
  }
  /**
   * @param tempC the tempC to set
   */
  public void setTempC(Integer tempC) {
	  this.tempC = tempC;
  }
  /**
   * @return the humidity
   */
  public String getHumidity() {
	  return humidity;
  }
  /**
   * @param humidity the humidity to set
   */
  public void setHumidity(String humidity) {
	  this.humidity = humidity;
  }
  /**
   * @return the icon
   */
  public String getIcon() {
	  return icon;
  }
  /**
   * @param icon the icon to set
   */
  public void setIcon(String icon) {
	  this.icon = icon;
  }
  /**
   * @return the wind_condition
   */
  public String getWind_condition() {
	  return wind_condition;
  }
  /**
   * @param wind_condition the wind_condition to set
   */
  public void setWind_condition(String wind_condition) {
	  this.wind_condition = wind_condition;
  }
}
