package com.magnifis.parking.model;

import java.util.Calendar;
import java.util.Date;

import com.magnifis.parking.Log;
import com.magnifis.parking.Xml.ML;
import com.magnifis.parking.utils.Utils;

public class GooWeather {
    static final String TAG="GooWeather";
	
	public GooWeatherForecastCondition getForecastFor(Date d) {
		if (d==null) // today
			return forecast[0];
		
	
		long offset=Utils.daysBetween(new Date(), d);
		if (offset<forecast.length) return forecast[(int) offset];
	
		return null;
	}
	
	
	@ML(tag="problem_cause", attr="data")
	protected String problemCause=null;
	
	/**
	 * @return the problemCause
	 */
	public String getProblemCause() {
		return problemCause;
	}

	/**
	 * @param problemCause the problemCause to set
	 */
	public void setProblemCause(String problemCause) {
		this.problemCause = problemCause;
	}

	@ML("forecast_information")
	protected GooForecastInformation forecastInformation = null;
	@ML("current_conditions")
	protected GooWeatherCurrentCondition currentConditions = null;
	@ML("forecast_conditions")
	protected GooWeatherForecastCondition forecast[] = null;

	/**
	 * @return the forecastInformation
	 */
	public GooForecastInformation getForecastInformation() {
		return forecastInformation;
	}

	/**
	 * @param forecastInformation
	 *            the forecastInformation to set
	 */
	public void setForecastInformation(
			GooForecastInformation forecastInformation) {
		this.forecastInformation = forecastInformation;
	}

	/**
	 * @return the currentConditions
	 */
	public GooWeatherCurrentCondition getCurrentConditions() {
		return currentConditions;
	}

	/**
	 * @param currentConditions
	 *            the currentConditions to set
	 */
	public void setCurrentConditions(
			GooWeatherCurrentCondition currentConditions) {
		this.currentConditions = currentConditions;
	}

	/**
	 * @return the forecast
	 */
	public GooWeatherForecastCondition[] getForecast() {
		return forecast;
	}

	/**
	 * @param forecast
	 *            the forecast to set
	 */
	public void setForecast(GooWeatherForecastCondition[] forecast) {
		this.forecast = forecast;
	}
}
