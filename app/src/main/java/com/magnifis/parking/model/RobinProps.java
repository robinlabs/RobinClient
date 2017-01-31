package com.magnifis.parking.model;

import java.io.Serializable;
import java.util.Date;

import com.magnifis.parking.Xml.ML;
import com.magnifis.parking.db.SqliteDB.DB;

@DB(table="robin_props", primaryKey="key")
public class RobinProps implements Serializable {
	
	@DB(value="key", isPrimaryKey=true)
	protected String key=null;
	
    @DB("value")
    protected String value=null;

	public String getKey() {
		return key;
	}

	public RobinProps setKey(String key) {
		this.key = key;
		return this;
	}

	public String getValue() {
		return value;
	}

	public RobinProps setValue(String value) {
		this.value = value;
		return this;
	}
    
    
	
	
}
