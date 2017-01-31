package com.magnifis.parking.model;

public class CallSmsAttempt {
   protected String contactNames[]=null;
   protected int phoneType=CalleeAssociationCR.DESIRED_PT_UNKNOWN;
   protected boolean sms;
   protected long when=System.currentTimeMillis();
   
   public long getWhen() {
	 return when;
   }

   public void setWhen(long when) {
	 this.when = when;
   }

   public CallSmsAttempt(String contactNames[],Integer phoneType, boolean sms) {
	 this.contactNames=contactNames;
	 this.phoneType=phoneType;
	 this.sms=sms;
   }
   
   public CallSmsAttempt() {}
   
   public String[] getContactNames() {
	   return contactNames;
   }
   public void setContactNames(String[] contactNames) {
	   this.contactNames = contactNames;
   }
   public int getPhoneType() {
	   return phoneType;
   }
   public void setPhoneType(int phoneType) {
	   this.phoneType = phoneType;
   }
   public boolean isSms() {
	   return sms;
   }
   public void setSms(boolean sms) {
	   this.sms = sms;
   }
   

}