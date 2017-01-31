package com.magnifis.parking.model;

import java.io.Serializable;
import java.util.List;

import com.magnifis.parking.Launchers;
import com.magnifis.parking.MAStatus;
import com.magnifis.parking.MapItemIterator;
import com.magnifis.parking.model.audioburst.ABFeed;
import com.magnifis.parking.utils.Utils;

public class MagReply implements Serializable, Cloneable {
	
	protected boolean processedByHandlerInBg=false, processedByHandlerInFg=false;
	
	public boolean isProcessedByHandlerInBg() {
		return processedByHandlerInBg;
	}

	public void setProcessedByHandlerInBg(boolean processedByHandlerInBg) {
		this.processedByHandlerInBg = processedByHandlerInBg;
	}

	public boolean isProcessedByHandlerInFg() {
		return processedByHandlerInFg;
	}

	public void setProcessedByHandlerInFg(boolean processedByHandlerInFg) {
		this.processedByHandlerInFg = processedByHandlerInFg;
	}
	
	
	protected List<Launchers.AppCacheEntry> launchCandidates=null;


	public List<Launchers.AppCacheEntry> getLaunchCandidates() {
		return launchCandidates;
	}

	public void setLaunchCandidates(List<Launchers.AppCacheEntry> launchCandidates) {
		this.launchCandidates = launchCandidates;
	}

	@Override
	public MagReply clone() {
		return Utils.cloneSerializable(this);
	}
	
	protected volatile List<ContactRecord> contacts=null;
	
	public List<ContactRecord> getContacts() {
		return contacts;
	}

	public void setContacts(List<ContactRecord> phones) {
		this.contacts = phones;
	}


	protected int mode=MAStatus.MODE_OTHER;
	
	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}
	
	
	protected GasReply gasStations=null;

	public GasReply getGasStations() {
		return gasStations;
	}

	public void setGasStations(GasReply gasStations) {
		this.gasStations = gasStations;
	}


	protected GeoSpannable<Poi> pois=null;
	
	public GeoSpannable<Poi> getPois() {
		return pois;
	}
	
	public MapItemIterator<Poi> pois() {
	  return pois==null?null:pois.facilities();
	}

	public void setPois(GeoSpannable<Poi> pois) {
		this.pois = pois;
	}

	public boolean noUnderstanding() {
		return understanding==null||understanding.isError();	
	}
	
	public int getCommandCode() {
		return understanding.getCommandCode();
	}
	
	protected Understanding understanding = null;
	
	protected PkResponse parking = null;

	public Understanding getUnderstanding() {
		return understanding;
	}

	public void setUnderstanding(Understanding understanding) {
		this.understanding = understanding;
	}

	public PkResponse getParking() {
		return parking;
	}

	public void setParking(PkResponse parking) {
		this.parking = parking;
	}

	protected String trafficReports[] = null;

	public String[] getTrafficReports() {
		return trafficReports;
	}

	public void setTrafficReports(String[] trafficReports) {
		this.trafficReports = trafficReports;
	}
	
	
	String pluginScriptCode = null; // can come with the response 
	
	public String getScriptCode() {
		return pluginScriptCode; 
	}

	public void setScriptCode(String jsFuncCode) {
		pluginScriptCode = jsFuncCode; 
	}
	
	protected ABFeed abFeed=null;

	public ABFeed getAbFeed() {
		return abFeed;
	}

	public void setAbFeed(ABFeed abFeed) {
		this.abFeed = abFeed;
	}
	
}
