package com.magnifis.parking.model;

import com.magnifis.parking.Xml.ML;

public class Script {
	
	@ML(attr="jsFuncName")
	protected String jsFuncName;

	public String getJsFuncName() {
		return jsFuncName;
	}

	public void setJsFuncName(String jsFuncName) {
		this.jsFuncName = jsFuncName;
	} 

}
