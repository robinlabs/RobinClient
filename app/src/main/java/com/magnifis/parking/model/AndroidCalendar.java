package com.magnifis.parking.model;

import com.magnifis.parking.Consts;
import com.magnifis.parking.db.SqliteDB.DB;

public class AndroidCalendar {
	@DB("_id")
	protected Integer id=null;
	@DB("name")
	protected String name=null; 
	@DB("calendar_displayName")
	protected String displayName=null; 
	@DB("account_name")
	protected String accountName=null;
	@DB("account_type")
	protected String accountType=null;
	
	@DB
	protected boolean visible=true;
	@DB
	protected boolean deleted=false;
	
	@DB("_sync_account_type")
	protected String syncAccountType=null;
	
	public String getSyncAccountType() {
		return syncAccountType;
	}

	public void setSyncAccountType(String syncAccountType) {
		this.syncAccountType = syncAccountType;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public boolean isGoogle() {
		return Consts.AT_GOOGLE.equals(accountType)||Consts.AT_GOOGLE.equals(this.syncAccountType);
	}
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer _id) {
		this.id = _id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getAccountName() {
		return accountName;
	}
	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}
	public String getAccountType() {
		return accountType;
	}
	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}
	

}
