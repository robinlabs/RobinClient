package com.magnifis.parking.model;

import java.util.Date;

import com.magnifis.parking.Xml.ML;

public class GooForecastInformation {
/*
 * <city data="Berkeley, CA"/>
 * <postal_code data="berkeley"/>
 * <latitude_e6 data=""/>
 * <longitude_e6 data=""/>
 * <forecast_date data="2012-03-18"/>
 * <current_date_time data="2012-03-18 09:53:00 +0000"/>
 * <unit_system data="US"/
 * */
	@ML(tag="unit_system",attr="data")
	protected String unitSystem=null;
	public String getUnitSystem() {
		return unitSystem;
	}
	public void setUnitSystem(String unitSystem) {
		this.unitSystem = unitSystem;
	}
	public String getPostalCode() {
		return postalCode;
	}
	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}
	@ML(tag="postalCode",attr="data")
	protected String postalCode=null;
	@ML(tag="city",attr="data")
	protected String city=null;
	@ML(tag="forecast_date",attr="data")
	protected Date forecastDate=null; 
	@ML(tag="current_date_time",attr="data")
	protected Date currentDateTime=null;
	/**
	 * @return the city
	 */
	public String getCity() {
		return city;
	}
	/**
	 * @param city the city to set
	 */
	public void setCity(String city) {
		this.city = city;
	}
	/**
	 * @return the forecastDate
	 */
	public Date getForecastDate() {
		return forecastDate;
	}
	/**
	 * @param forecastDate the forecastDate to set
	 */
	public void setForecastDate(Date forecastDate) {
		this.forecastDate = forecastDate;
	}
	/**
	 * @return the currentDateTime
	 */
	public Date getCurrentDateTime() {
		return currentDateTime;
	}
	/**
	 * @param currentDateTime the currentDateTime to set
	 */
	public void setCurrentDateTime(Date currentDateTime) {
		this.currentDateTime = currentDateTime;
	} 	
}
