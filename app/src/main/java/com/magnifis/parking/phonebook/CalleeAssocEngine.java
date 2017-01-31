package com.magnifis.parking.phonebook;

import static com.robinlabs.utils.BaseUtils.isEmpty;
import static com.robinlabs.utils.BaseUtils.toLowerCase;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import android.database.DatabaseUtils;
import android.telephony.PhoneNumberUtils;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.magnifis.parking.App;
import com.magnifis.parking.db.RobinDB;
import com.magnifis.parking.model.BrokenPhoneNumber;
import com.magnifis.parking.model.CalleeAssociation;
import com.magnifis.parking.model.CalleeAssociationCR;
import com.magnifis.parking.model.ContactRecordBase;
import com.robinlabs.utils.BaseUtils;

public class CalleeAssocEngine {
	
	final int     initialPhoneType;
	final private PhoneNumberUtil phoneNumberUtils=PhoneNumberUtil.getInstance();
	final private RobinDB rdb;

	public int getInitialPhoneType() {
		return initialPhoneType;
	}

	public PhoneNumberUtil getPhoneNumberUtils() {
		return phoneNumberUtils;
	}

	public CalleeAssocEngine(int initialPhoneType) {
	  this.initialPhoneType=initialPhoneType;
	  rdb = RobinDB.getInstance(App.self);
	}
	
	public String [] getCalleeAssociations(String contactNames[], Long tillMilliseconds) {
		StringBuilder cond=new StringBuilder(" counter>=1 and (desiredPhoneType=");
		cond.append(initialPhoneType);
		cond.append(" or (actualPhoneTypeMask&");
		cond.append(CalleeAssociation.calculateDesiredPhoneTypeMask(initialPhoneType));
		cond.append("<>0)");
		if (tillMilliseconds!=null) {
			cond.append(") and ((last_used is null) or (last_used<=");
			cond.append(tillMilliseconds);
		}
		cond.append(")) and match in (");
		for (int i=0;i<contactNames.length;i++) {
			if (i>0) cond.append(',');
			cond.append('?');
		}
		cond.append(
		 ") group by countryCode,nationalNumber,emergencyNumber having count(match)>=2"+
		 " or max(counter)>=2 " //" or max(counter)>=3 " // remember from second time
		);
		List<CalleeAssociationCR> lst=rdb.getWhere(
				cond.toString(),
				toLowerCase(contactNames),
				CalleeAssociationCR.class,
				"count(match) desc"
		);
		if (!isEmpty(lst)) {
			String rv[]=new String[lst.size()];
			for (int i=0;i<rv.length;i++) rv[i]=lst.get(i).getPhone().toString();
			return rv;
		}
		return null;
	}
	
	public static class Association implements Serializable {
		protected String  phone=null;
		protected String cnames[]=null;
		protected int initialPhoneType;
		
		
		
		public int getInitialPhoneType() {
			return initialPhoneType;
		}

		public void setInitialPhoneType(int initialPhoneType) {
			this.initialPhoneType = initialPhoneType;
		}

		public String getPhone() {
			return phone;
		}
		
		public void setPhone(String phone) {
			this.phone = phone;
		}
		
		public String[] getCnames() {
			return cnames;
		}
		
		public void setCnames(String[] cnames) {
			this.cnames = cnames;
		}
		
		public Association(int ipt,   ContactRecordBase rec, String cnames[]) {
			this(ipt,rec.getPhone(),cnames);
		}
		
		public Association(int ipt, String  phone, String cnames[]) {
		  this.phone=phone;
		  this.cnames=cnames;
		  this.initialPhoneType=ipt;
		}
		
        public CalleeAssocEngine getEngine() {
           return new CalleeAssocEngine(this.initialPhoneType);
        }		
        
        
        public void clear(String outgoigPhoneNumber) {
           if (PhoneNumberUtils.compare(outgoigPhoneNumber,phone)) { 
        	   CalleeAssocEngine eng=getEngine();
        	   eng.clearCalleeAssociations(phone, cnames);
           }
        }

	};
	
	public void clearCalleeAssociations(ContactRecordBase rec, String cnames[]) {
		clearCalleeAssociations(rec.getPhone(),cnames);
	}
	
	public void clearCalleeAssociations(String phone, String cnames[]) {
		if (!isEmpty(cnames)) {	
		  BrokenPhoneNumber bp=new BrokenPhoneNumber(phoneNumberUtils,phone);
			
		  StringBuilder sb=new StringBuilder(
			"counter=counter-1 where counter>0 and desiredPhoneType="
		  );
		  sb.append(initialPhoneType);
		  sb.append(" and countryCode in (-1");
		  if (bp.countryCode!=-1) {
			sb.append(',');
			sb.append(bp.countryCode);
		  }
		  sb.append(") and emergencyNumber=");
		  sb.append(bp.emergencyNumber?'1':'0');
		  sb.append(" and nationalNumber=");
		  sb.append(DatabaseUtils.sqlEscapeString(bp.nationalNumber));		  
		  
		  sb.append(" and match in(");
		  for (int i=0;i<cnames.length;i++) {
			if (i>0) sb.append(',');
			sb.append('?');
		  } 
		  sb.append(')');
		  rdb.update(CalleeAssociation.class, sb.toString(), BaseUtils.toLowerCase(cnames));
		}
	}
	
	public void touch(String phone, String cnames[]) {
		if (!isEmpty(cnames)) {	
			  BrokenPhoneNumber bp=new BrokenPhoneNumber(phoneNumberUtils,phone);
				
			  StringBuilder sb=new StringBuilder(
				"last_used="
			  );
			  sb.append(System.currentTimeMillis());
			  sb.append(" where counter>0 and desiredPhoneType=");
			  sb.append(initialPhoneType);
			  sb.append(" and countryCode in (-1");
			  if (bp.countryCode!=-1) {
				sb.append(',');
				sb.append(bp.countryCode);
			  }
			  sb.append(") and emergencyNumber=");
			  sb.append(bp.emergencyNumber?'1':'0');
			  sb.append(" and nationalNumber=");
			  sb.append(DatabaseUtils.sqlEscapeString(bp.nationalNumber));		  
			  
			  sb.append(" and match in(");
			  for (int i=0;i<cnames.length;i++) {
				if (i>0) sb.append(',');
				sb.append('?');
			  } 
			  sb.append(')');
			  rdb.update(CalleeAssociation.class, sb.toString(), BaseUtils.toLowerCase(cnames));
			}		
	}
	
	
	public void saveCalleeAssociations(ContactRecordBase rec, String cnames[]) {
		String cnss[]=toLowerCase(cnames);
		
		if (!isEmpty(cnss)) {
			
            BrokenPhoneNumber bp=new BrokenPhoneNumber(phoneNumberUtils,rec.getPhone());
			
            CalleeAssociation sels[]=new CalleeAssociation[cnss.length];
			for (int i=0;i<cnss.length;i++) {
				CalleeAssociation ccs = new CalleeAssociation();
				ccs.set(bp);
				ccs.setMatch(cnss[i]);
				ccs.setDesiredPhoneType(initialPhoneType);
				ccs.setActualPhoneTypeMask(
					CalleeAssociation.calculateActualPhoneTypeMask(rec.getTypes()));
				ccs.setLastUsed(new Date());
				sels[i]=ccs;
			}
			rdb.insertOrIgnore(sels);

			StringBuilder sb=new StringBuilder("counter=counter+1, last_used=");
			sb.append(System.currentTimeMillis());
			sb.append(" where countryCode=");
			sb.append(bp.countryCode);
			sb.append(" and desiredPhoneType=");
			sb.append(initialPhoneType);
			sb.append(" and emergencyNumber=");
			sb.append(bp.emergencyNumber?'1':'0');
			sb.append(" and nationalNumber=");
			sb.append(DatabaseUtils.sqlEscapeString(bp.nationalNumber));
			
			sb.append(" and match in(");
			for (int i=0;i<cnss.length;i++) {
				if (i>0) sb.append(',');
				sb.append('?');
			}
			sb.append(')');
			rdb.update(CalleeAssociation.class,sb.toString(),cnss);
		}
	}	

}
