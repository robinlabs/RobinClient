package com.magnifis.parking.model;

import java.io.Serializable;

import com.magnifis.parking.Xml.ML;
import com.magnifis.parking.Xml.ML_alternatives;

public class FbFQLFeedPostUser implements Serializable {
	
    @ML(tag="uid")
	protected String id = null;

	@ML("first_name")
	protected String first_name = null;

	@ML("last_name")
	protected String last_name = null;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFirst_name() {
		return first_name;
	}

	public void setFirst_name(String first_name) {
		this.first_name = first_name;
	}

	public String getLast_name() {
		return last_name;
	}

	public void setLast_name(String last_name) {
		this.last_name = last_name;
	}

}
