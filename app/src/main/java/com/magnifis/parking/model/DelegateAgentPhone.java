package com.magnifis.parking.model;

import java.util.List;

import com.magnifis.parking.db.RobinDB;
import com.magnifis.parking.db.SqliteDB.DB;
import static  com.magnifis.parking.utils.Utils.*;

import android.content.Context;
import android.telephony.PhoneNumberUtils;
import android.util.Log;


@DB(table="agent_phone_0")
public class DelegateAgentPhone {
	final static String TAG=DelegateAgentPhone.class.getSimpleName();

	@DB(value="agent")
	protected String  agent=null;
	
	@DB(value="phone_number")
	protected String  phoneNumber=null;
	
	public DelegateAgentPhone() {}
	public DelegateAgentPhone(String agent, String phoneNumber) {
		this.phoneNumber=phoneNumber;
		this.agent=agent;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	public String getAgent() {
		return agent;
	}
	public void setAgent(String agent) {
		this.agent = agent;
	}
	
	public static String [] getPhoneNumbers(String agent, Context context) {
		try {
	    	  RobinDB db=RobinDB.getInstance(context);
	    	  List<DelegateAgentPhone> dps=db.getBy("agent", agent, DelegateAgentPhone.class);
	    	  if (!isEmpty(dps)) {
	    		  String pns[]=new String[dps.size()];
	    		  for (int i=0; i<dps.size(); i++) pns[i]=dps.get(i).getPhoneNumber();
	    		  return pns;
	    	  }		
			} catch(Throwable t) {
				;
			}
	    	return null;		
	}
	
	public static String getAgentByPhone(String phone, Context context) {
		if (!isEmpty(phone)) try {
    	  RobinDB db=RobinDB.getInstance(context);
    	  
    	  phone=phone.replaceAll("\\s+|[-]+", "");
    	  
    	  int    pl=phone.length();
    	  CharSequence last4=phone.subSequence(pl-4, pl);
    	  
    	  List<DelegateAgentPhone> afs=db.getWhere("phone_number like '%"+last4.toString() +"'",  null, DelegateAgentPhone.class);
    	  if (!isEmpty(afs)) for (DelegateAgentPhone daf:afs) if (PhoneNumberUtils.compare(phone, daf.phoneNumber)) {
    		  return daf.agent;
    	  }
		} catch(Throwable t) {
			Log.d(TAG, "--", t);
			
		}
    	return null;
	}
}
