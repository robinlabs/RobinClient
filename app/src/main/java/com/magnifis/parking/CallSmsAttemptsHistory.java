package com.magnifis.parking;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.magnifis.parking.model.CallSmsAttempt;
import com.magnifis.parking.model.CalleeAssociation;
import com.magnifis.parking.model.Understanding;

public class CallSmsAttemptsHistory {
	
	final int MAX_ATTEMPTS_REMEMBERED=3;
	final int MAX_TTL=50000; // 50 seconds
	
    List<CallSmsAttempt> attempts=new ArrayList<CallSmsAttempt>();
    
    protected static SoftReference<CallSmsAttemptsHistory> selfWr=null;
    
    private CallSmsAttemptsHistory() {
       selfWr=new SoftReference<CallSmsAttemptsHistory>(this);
    }
    
    public static CallSmsAttemptsHistory get() {
       return selfWr==null?null:selfWr.get();
    }
    
    public static CallSmsAttemptsHistory getInstance() {
    	synchronized(CallSmsAttemptsHistory.class) {
    		CallSmsAttemptsHistory h=get();
    		if (h==null) h=new CallSmsAttemptsHistory();
    		return h;
    	}
    }
    
       
    synchronized public void remember(Understanding u) {
    	remember(u.getContactNames(),CalleeAssociation.calculateDesiredPhoneType(u.getPhoneType()),u.getCommandCode()==Understanding.CMD_SEND);
    }
    
    synchronized public void remember(String contactNames[], int phoneType, boolean sms) {
    	if (attempts.size()==MAX_ATTEMPTS_REMEMBERED) attempts.remove(0);
    	attempts.add(new CallSmsAttempt(contactNames,phoneType,sms));
    }
    
    synchronized public  List<CallSmsAttempt> getRelevantAttempts(Understanding u) {
    	return getRelevantAttempts(CalleeAssociation.calculateDesiredPhoneType(u.getPhoneType()),u.getCommandCode()==Understanding.CMD_SEND);
    }
    
    synchronized public  List<CallSmsAttempt> getRelevantAttempts(int phoneType, boolean sms) {
    	long t=System.currentTimeMillis();
    	List<CallSmsAttempt> res=new ArrayList<CallSmsAttempt>();
    	for (CallSmsAttempt a:attempts) if ((t-a.getWhen()<=MAX_TTL)&&(a.isSms()==sms)) res.add(0, a);
    	return res;
    }
}
