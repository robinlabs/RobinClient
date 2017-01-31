package com.magnifis.parking.model;

import com.magnifis.parking.Xml.ML;

public class CalEventLocation {
	@ML
	protected String city=null;
	@ML("full_description")
	protected String fullDescription=null;
	
	public String getCity() {
		return city;
	}
	
	public void setCity(String city) {
		this.city = city;
	}
	
	public String getFullDescription() {
		return fullDescription;
	}
	
	public void setFullDescription(String fullDescription) {
		this.fullDescription = fullDescription;
	}
}
