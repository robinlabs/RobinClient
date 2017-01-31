package com.magnifis.parking.model;

import java.io.Serializable;
import java.util.Map;

import android.util.Pair;

import com.magnifis.parking.Xml.ML;

public class ActionParam  implements Serializable {
	@ML(attr="value")
	protected String value=null;
	@ML(attr="name")
	protected String name = null;
	
	public String getValue() {
		return value; 
	} 
	
	public Pair<String, String> getNameValuePair() {
		return new Pair<String, String>(name, value); 
	} 
	

}
