package com.magnifis.parking.model;

import com.magnifis.parking.db.SqliteDB.DB;

@DB(table="callee_association", primaryKey="match,desiredPhoneType,countryCode,nationalNumber,emergencyNumber")
public class CalleeAssociation extends CalleeAssociationCR {

	@DB(notnull=true)
	protected String match=null;
	@DB(notnull=true,defaultValue="0")
	protected int actualPhoneTypeMask=0;
	
	public int getActualPhoneTypeMask() {
		return actualPhoneTypeMask;
	}

	public void setActualPhoneTypeMask(int actualPhoneType) {
		this.actualPhoneTypeMask = actualPhoneType;
	}

	public String getMatch() {
		return match;
	}

	public void setMatch(String match) {
		this.match = match;
	}

	@DB(notnull=true,defaultValue="0")
	protected long counter=0;

	public long getCounter() {
		return counter;
	}

	public void setCounter(long counter) {
		this.counter = counter;
	}

}
