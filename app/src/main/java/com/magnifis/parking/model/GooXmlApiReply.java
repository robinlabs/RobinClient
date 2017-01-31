package com.magnifis.parking.model;

import com.magnifis.parking.Xml.ML;

public class GooXmlApiReply {
    @ML("weather")
	protected GooWeather weather = null;

	/**
	 * @return the weather
	 */
	public GooWeather getWeather() {
		return weather;
	}

	/**
	 * @param weather
	 *            the weather to set
	 */
	public void setWeather(GooWeather weather) {
		this.weather = weather;
	}
}
