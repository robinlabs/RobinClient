package com.magnifis.parking.model;

import com.magnifis.parking.Xml.ML;

public class Horoscope {
  @ML("description")
	protected String description = null;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
