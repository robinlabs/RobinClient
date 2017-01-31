package com.magnifis.parking.model;

import com.magnifis.parking.Xml.ML;

public class GooDirectionsScraperParams {
   
	@ML("base_url")
	protected String baseURL = null;

    @ML("sparam")
   	protected String sourceParamName = null;

    @ML("dparam")
   	protected String destParamName = null;

    @ML("cur_eta_xpath") // not really xpath, but kind of
   	protected String curEtaXpath = null;
    
    @ML("normal_eta_xpath") // not really xpath, but kind of
   	protected String normalEtaXpath = null;

    @ML("route_length_xpath") // not really xpath, but kind of
   	protected String routeLengthXpath = null;
    
    @ML("route_name_xpath") // not really xpath, but kind of
	protected String routeNameXpath = null;
   
    
    public String getBaseURL() {
		return baseURL;
	}

	public void setBaseURL(String baseURL) {
		this.baseURL = baseURL;
	}

	public String getSourceParamName() {
		return sourceParamName;
	}

	public void setSourceParamName(String sourceParamName) {
		this.sourceParamName = sourceParamName;
	}

	public String getDestParamName() {
		return destParamName;
	}

	public void setDestParamName(String destParamName) {
		this.destParamName = destParamName;
	}

	public String getCurEtaXpath() {
		return curEtaXpath;
	}

	public void setCurEtaXpath(String curEtaXpath) {
		this.curEtaXpath = curEtaXpath;
	}

	public String getNormalEtaXpath() {
		return normalEtaXpath;
	}

	public void setNormalEtaXpath(String normalEtaXpath) {
		this.normalEtaXpath = normalEtaXpath;
	}

	public String getRouteLengthXpath() {
		return routeLengthXpath;
	}

	public void setRouteLengthXpath(String routeLengthXpath) {
		this.routeLengthXpath = routeLengthXpath;
	}

	public String getRouteNameXpath() {
		return routeNameXpath;
	}

	public void setRouteNameXpath(String routeNameXpath) {
		this.routeNameXpath = routeNameXpath;
	}
    
}