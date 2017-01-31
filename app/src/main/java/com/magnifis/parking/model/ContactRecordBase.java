package com.magnifis.parking.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;

import com.magnifis.parking.Consts;
import com.magnifis.parking.Log;
import com.magnifis.parking.utils.Langutils;
import com.magnifis.parking.utils.Setized;
import com.magnifis.parking.utils.Translit;
import com.magnifis.parking.utils.Utils;
import com.robinlabs.utils.BaseUtils;

public abstract class ContactRecordBase {
	
	final static String TAG=ContactRecordBase.class.getSimpleName();
	
	

	public ContactRecordBase() {
	}
	
	public boolean isNamePhoneNumber() {
		return false;
	}


	// shallow copy constructor 
	public ContactRecordBase(ContactRecordBase cr) {
		this.id = cr.id;
		this.name = cr.name; 
		this.phone = cr.phone; 
		this.photoId = cr.photoId;
		this.names = cr.names;
		this.lastContactTime = cr.lastContactTime; 
		this.favorite = cr.favorite; 
		this.rawContactId = cr.rawContactId; 
		this.contactId = cr.contactId; 
		this.timesContacted = cr.timesContacted; 
		this.types = Utils.clone(cr.types); 
		this.typeLabel = cr.typeLabel; 
		this.encodedNames = cr.encodedNames;
	}

	
	public boolean isSameContact(ContactRecordBase c) {
		return c!=null&&c.contactId==contactId;
	}
	
	protected long contactId=-1;	

	
	public long getContactId() {
		return contactId;
	}


	public void setContactId(long contactId) {
		this.contactId = contactId;
	}

	protected long rawContactId=-1;

	
	public long getRawContactId() {
		return rawContactId;
	}

	public void setRawContactId(long personId) {
		this.rawContactId = personId;
	}

	protected long id=-1;

	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public int getNamesCount() {
		if (names!=null) return  names.size();
		if (!BaseUtils.isEmpty(name)) return 1;
		return 9;
	}

	protected Set<String> names=null;
	protected Setized encodedNames=null;
	
	protected static Translit translit=Translit.getHebRus();
	
	public void calculateEncodedNames() {
			 encodedNames=
				  new Setized(
					Langutils.normalize_phonetics(
					  BaseUtils.toString(
					   translit.process(
						  Langutils.deAccent(
						    BaseUtils.remove(
							   BaseUtils.toLowerCase(
									 BaseUtils.trim(
									    getName()
									 )
							   ),
							   '\''
						   )
					     )
					   )
					 )
				  )
				);
	}
	
	public Setized  getEncodedNames() {
		return encodedNames;
	}
	
	public Collection<String> getNames() {
	  if (BaseUtils.isEmpty(names)) {
		  List<String> lst=new ArrayList<String>(1);
		  lst.add(getName());
		  return lst;
	  }
	  return names;
	}
	
	public boolean hasName(String nm) {
	   return onlyOtherName(nm)==null;
	}
	
	/***
	 * returns a name in conninical form only and only if this contact does not have "nm" name
	**/
	public String onlyOtherName(String nm) {
		if (!BaseUtils.isEmpty(nm)){
			nm=Langutils.statementCanonicalForm(nm,true);
			if (!BaseUtils.isEmpty(nm)) {
				if (!BaseUtils.isEmpty(names)) { 
					return names.contains(nm)?null:nm;
				}
				return nm.equals(Langutils.statementCanonicalForm(name,true))?null:nm;
			}
		}
		return null;
	}

	public void setLastContactTime(Long lastContactTime) {
		this.lastContactTime = lastContactTime;
	}

	public  void updateWith(ContactRecordBase r) {
	  if (r!=null) {
		 if ((photoId==null||photoId<=0)&&r.photoId!=null&&r.photoId>0) { 
			 Log.d(TAG,"updateWith:photo "+getName()+" => "+r.getName()+" ("+r.photoId+")");
			 photoId=r.photoId;
		 }
		 
		 if (r.types!=null) for (int x:r.types) addType(x);
		 
		 favorite|=r.favorite;
		 if (r.lastContactTime!=null&&
			 (lastContactTime==null||lastContactTime<r.lastContactTime))
				 lastContactTime=r.lastContactTime;
		
		 synchronized(this) {
			if (r.isNamePhoneNumber()) return; // nothing to do
			if (isNamePhoneNumber()) {
				name=r.getName();
			} else {
			// !e0 && !e1 , concatenate the names
			  String rn=r.getName(), orn=onlyOtherName(rn);
		      if (!(BaseUtils.isEmpty(orn))) {
		    	if (names==null) {
		    	  names=new HashSet<String>();
		    	  if (name!=null) names.add(Langutils.statementCanonicalForm(name,true));
		    	}
		    	names.add(orn);
		    	///////////////////////////
		    	setName(Langutils.combineTwoNames(name,rn));
		    	///////////////////////////
		    	if (!BaseUtils.isEmpty(r.names)) names.addAll(r.names);
		    	
		    	calculateEncodedNames();
		      }
			}
		 }
	  }
	}

	protected int timesContacted=0;	
	

	public int getTimesContacted() {
		return timesContacted;
	}

	public void setTimesContacted(int timesContacted) {
		this.timesContacted = timesContacted;
	}

	protected Long lastContactTime=null;
	
	public Long getLastContactTime() {
		return lastContactTime;
	}
	
	public boolean isVip() {
		return isContactedInLastNDays(Consts.FAVORITE_IS_VIP_IF_CALLED_IN_DAYS)&&(isFavorite()||(isContactedInLastNDays(3)&&getTimesContacted()>=3));
	}
	
	public double getImportance() {
	  double ip=isFavorite()?1.:0;
	  if (getTimesContacted()>=3&&isContactedInLastNDays(14)) {
		  if (isContactedInLastNDays(3)) ip+=1; else if (isContactedInLastNDays(10)) ip+=0.7;
		  ip+=(1.-1./(double)getTimesContacted());
	  } 
	  return ip;
	}
	
	public boolean isContactedInLastNDays(int n) {
	  if (n==0) return true; // special hack
	  if (lastContactTime!=null) {
		  if ((System.currentTimeMillis()-lastContactTime)/(1000L*60*60L*24L)<n)return true;
	  }
	  return false;
	}

	public void setLastContactTime(long lastContactTime) {
		this.lastContactTime = lastContactTime;
	}

	protected boolean favorite=false;
	
	public boolean isFavorite() {
		return favorite;
	}

	public void setFavorite(boolean favorite) {
		this.favorite = favorite;
	}

	public void release() {
		this.typeLabel=null;
	}
	

	protected String name = null, phone = null, typeLabel=null;
	protected Long photoId = null;
	protected int types[]=null;
	
	
	
	public synchronized void addType(int type) {
		if (Utils.indexOf(types, type)<0)
	       types=Utils.cons(type, types);
	}
	
	public int[] getTypes() {
		return types;
	}

	
	public boolean iSsameTypeAs(ContactRecordBase crb) {
		if (types==crb.types) return true;
	    return BaseUtils.anyIntersection(types, crb.types);
	}
	
	public boolean iSsameNameAs(ContactRecordBase crb) {
		if (name==crb.name) return true;
		if (!(BaseUtils.isEmpty(name)||BaseUtils.isEmpty(crb.name))&&
		  Langutils.setize(name).equals(Langutils.setize(crb.name))) return true;
		
		return false;
	}

	public Long getPhotoId() {
		return photoId;
	}

	public void setPhotoId(Long photoId) {
		this.photoId = photoId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}
	
	public String getFormattedPhoneType() {
	   return "";
	}
	
	
	public abstract boolean isSamePhone(ContactRecordBase cj); 
	
	@Override
	public String toString() {
		String tp="";
		if (!BaseUtils.isEmpty(typeLabel)) {
		 tp=" ("+typeLabel+")";
		} else
		 tp=getFormattedPhoneType();
		return getName()+"  "+phone+tp;
	}

}