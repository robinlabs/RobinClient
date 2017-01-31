package com.magnifis.parking.model;

import com.magnifis.parking.Xml.ML;

public class GooWeatherForecastCondition {
	/***
	 * 
	 * <day_of_week data="Sun"/> <low data="41"/> <high data="55"/> <icon
	 * data="/ig/images/weather/chance_of_rain.gif"/> <condition
	 * data="Chance of Showers"/>
	 */

	@ML(tag = "day_of_week", attr = "data")
	protected String dayOfWeek = null;
	@ML(tag = "low", attr = "data")
	protected Integer low = null;
	@ML(tag = "high", attr = "data")
	protected Integer high = null;
	@ML(tag = "icon", attr = "data")
	protected String icon = null;
	@ML(tag = "condition", attr = "data")
	protected String Condition = null;
	/**
	 * @return the dayOfWeek
	 */
	public String getDayOfWeek() {
		return dayOfWeek;
	}
	/**
	 * @param dayOfWeek the dayOfWeek to set
	 */
	public void setDayOfWeek(String dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}
	/**
	 * @return the low
	 */
	public Integer getLow() {
		return low;
	}
	/**
	 * @param low the low to set
	 */
	public void setLow(Integer low) {
		this.low = low;
	}
	/**
	 * @return the high
	 */
	public Integer getHigh() {
		return high;
	}
	/**
	 * @param high the high to set
	 */
	public void setHigh(Integer high) {
		this.high = high;
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
	 * @return the condition
	 */
	public String getCondition() {
		return Condition;
	}
	/**
	 * @param condition the condition to set
	 */
	public void setCondition(String condition) {
		Condition = condition;
	}
}
