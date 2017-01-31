package com.magnifis.parking.model;

import java.io.Serializable;

import com.magnifis.parking.Xml.ML;

public class PkCalculatedRate implements Serializable {
	/*
    "quoted_duration": "1:00:00",
    "rate_cost": 11,
    "rate_type": "B"
    */
	@ML("quoted_duration")
	protected String quoted_duration=null;
	@ML("rate_cost")
	protected Double rate_cost=null;
	@ML("rate_type")
	protected String rate_type=null;
	/**
	 * @return the quoted_duration
	 */
	public String getQuoted_duration() {
		return quoted_duration;
	}
	/**
	 * @param quoted_duration the quoted_duration to set
	 */
	public void setQuoted_duration(String quoted_duration) {
		this.quoted_duration = quoted_duration;
	}
	/**
	 * @return the rate_cost
	 */
	public Double getRate_cost() {
		return rate_cost;
	}
	/**
	 * @param rate_cost the rate_cost to set
	 */
	public void setRate_cost(Double rate_cost) {
		this.rate_cost = rate_cost;
	}
	/**
	 * @return the rate_type
	 */
	public String getRate_type() {
		return rate_type;
	}
	/**
	 * @param rate_type the rate_type to set
	 */
	public void setRate_type(String rate_type) {
		this.rate_type = rate_type;
	}
}
