package com.magnifis.parking.model;

import java.io.Serializable;
import java.util.Date;

import com.magnifis.parking.Xml.ML;
import com.magnifis.parking.db.SqliteDB.DB;

@DB(table="push_add", primaryKey="package")
public class PushAd implements Serializable {
	
	@DB
	@ML("url")
	protected String url=null;
	
	protected String packageUrl=null;
	
	public String getPackageUrl() {
		return packageUrl;
	}
	public void setPackageUrl(String packageUrl) {
		this.packageUrl = packageUrl;
	}

	@DB("bg_url")
	@ML("bg_url")
	protected String bgUrl=null;	
	
	@DB("ack_url")
	@ML("ack_url")
	protected String ackUrl=null;	
	
	@DB
	@ML(attr="type")
	protected String type=null;
	@DB
	@ML(attr="phrase")
	protected String phrase=null;
	
	@DB(value="package", isPrimaryKey=true)
	@ML(attr="package")
	protected String pushAdPackage=null;
	
	@ML(attr="condAppNotThere")
	protected Boolean pushIfAppNotThere=null;	
	

	public String getUrl() {
		return url;
	}
	public void setPushAdUrl(String pushAdUrl) {
		this.url = pushAdUrl;
	}
	
	public String getBgUrl() {
		return bgUrl;
	}
	public void setBgUrl(String u) {
		this.bgUrl = u;
	}

	public String getPushAdType() {
		return type;
	}

	public void setPushAdType(String pushAdType) {
		this.type = pushAdType;
	}

	public String getPushAdPhrase() {
		return phrase;
	}

	public void setPushAdPhrase(String pushAdPhrase) {
		this.phrase = pushAdPhrase;
	}

	public String getPushAdPackage() {
		return pushAdPackage;
	}

	public void setPushAdPackage(String pushAdPackage) {
		this.pushAdPackage = pushAdPackage;
	}

	public Boolean getPushAdIfAppNotThere() {
		return pushIfAppNotThere;
	}

	public void setPushAdIfAppNotThere(boolean pushAdIfAppNotThere) {
		this.pushIfAppNotThere = pushAdIfAppNotThere;
	}
	public String getAckUrl() {
		return ackUrl;
	}

	@DB("when_installed")
	protected Date whenInstalled=null;
    @DB("runned")
    protected boolean runned=false;
    
	@DB("when_requested")
	protected Date whenRequested=null;

	public Date getWhenInstalled() {
		return whenInstalled;
	}
	public void setWhenInstalled(Date whenInstalled) {
		this.whenInstalled = whenInstalled;
	}
	public boolean isRunned() {
		return runned;
	}
	public void setRunned(boolean runned) {
		this.runned = runned;
	}
	public Date getWhenRequested() {
		return whenRequested;
	}
	public void setWhenRequested(Date whenRequested) {
		this.whenRequested = whenRequested;
	}
	
	
}
