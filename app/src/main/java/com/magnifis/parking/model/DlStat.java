package com.magnifis.parking.model;

import java.io.Serializable;
import java.util.Date;

import com.magnifis.parking.Xml.ML;
import com.magnifis.parking.db.SqliteDB.DB;

@DB(table="dnl_stat", primaryKey="filename")
public class DlStat implements Serializable {
	
	@DB(value="filename", isPrimaryKey=true)
	protected String filename=null;
	
	@DB("description")
	protected String description=null;

	@DB("when_installed")
	protected Date whenInstalled=null;


	public String getFilename() {
		return filename;
	}

	public DlStat setFilename(String filename) {
		this.filename = filename;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public DlStat setDescription(String description) {
		this.description = description;
		return this;
	}

	public Date getWhenInstalled() {
		return whenInstalled;
	}

	public DlStat setWhenInstalled(Date whenInstalled) {
		this.whenInstalled = whenInstalled;
		return this;
	}

	
	
}
