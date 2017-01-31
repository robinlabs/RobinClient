package com.magnifis.parking.model;

import java.util.Date;

import android.provider.ContactsContract;

import com.magnifis.parking.db.SqliteDB.DB;
import com.robinlabs.utils.BaseUtils;

@DB(table="callee_association")
public class CalleeAssociationCR {
	
	@DB
    protected int     countryCode=-1;
	@DB
	protected String  nationalNumber=null;
	@DB
	protected boolean emergencyNumber=false;
	@DB
	public int leadingZeros=0;
	@DB(notnull=true,defaultValue="0")
	protected int desiredPhoneType=0;
	
	
	@DB("last_used")
	protected Date lastUsed=null;
	
	public Date getLastUsed() {
		return lastUsed;
	}

	public void setLastUsed(Date lastUsed) {
		this.lastUsed = lastUsed;
	}

	public final static int 
	       DESIRED_PT_UNKNOWN=0,
	       DESIRED_PT_HOME=1,
	       DESIRED_PT_MOBILE=2,
	       DESIRED_PT_WORK=3
		;
	
	public static int calculateDesiredPhoneTypeMask(int desiredPhoneType) {
		return desiredPhoneType==0?0:(1<<desiredPhoneType-1);
	}
	
	public static int calculateActualPhoneTypeMask(int types[]) {
		if (types!=null) {
			int rv=0;
			for (int x:types) rv|=calculateActualPhoneTypeMask(x);
			return rv;
		}
		return 0;
	}
	
	public static int calculateActualPhoneTypeMask(Integer androidPhoneType) {
		if (androidPhoneType!=null) switch (androidPhoneType) {
		case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
			return calculateDesiredPhoneTypeMask(DESIRED_PT_MOBILE);
		case ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE:
			return calculateDesiredPhoneTypeMask(DESIRED_PT_MOBILE)|
				   calculateDesiredPhoneTypeMask(DESIRED_PT_WORK)
					;
		case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
			return calculateDesiredPhoneTypeMask(DESIRED_PT_WORK);
		case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
			return calculateDesiredPhoneTypeMask(DESIRED_PT_HOME);
		}
		return 0;
	}
	
	public static int calculateDesiredPhoneType(String pt) {
		if (!BaseUtils.isEmpty(pt)) {
			if ("mobile".equalsIgnoreCase(pt)) return DESIRED_PT_MOBILE; 
			if ("home".equalsIgnoreCase(pt)) return DESIRED_PT_HOME; 
			if ("work".equalsIgnoreCase(pt)) return DESIRED_PT_WORK;
		}
		return DESIRED_PT_UNKNOWN;
	}
	
	public void setDesiredPhoneType(String pt) {
	   desiredPhoneType=calculateDesiredPhoneType(pt);
	}
	
	
	public int getDesiredPhoneType() {
		return desiredPhoneType;
	}


	public void setDesiredPhoneType(int desiredPhoneType) {
		this.desiredPhoneType = desiredPhoneType;
	}


	public int getLeadingZeros() {
		return leadingZeros;
	}


	public void setLeadingZeros(int leadingZeros) {
		this.leadingZeros = leadingZeros;
	}


	public void set(BrokenPhoneNumber bp) {
		countryCode=bp.countryCode;
		nationalNumber=bp.nationalNumber;
		emergencyNumber=bp.emergencyNumber;
		leadingZeros=bp.leadingZeros;
	}
	
	
	public int getCountryCode() {
		return countryCode;
	}
	public void setCountryCode(int countryCode) {
		this.countryCode = countryCode;
	}
	public String getNationalNumber() {
		return nationalNumber;
	}
	public void setNationalNumber(String nationalNumber) {
		this.nationalNumber = nationalNumber;
	}
	public boolean isEmergencyNumber() {
		return emergencyNumber;
	}
	public void setEmergencyNumber(boolean emergencyNumber) {
		this.emergencyNumber = emergencyNumber;
	}
	
	public CharSequence getPhone() {
		StringBuilder sb=new StringBuilder();
		if (countryCode!=-1) {
		   sb.append('+');
		   sb.append(countryCode);
		} else {
		  for (int i=0;i<leadingZeros;i++) sb.append('0');
		}
		
		sb.append(nationalNumber);
		
		return sb;
	}
}
