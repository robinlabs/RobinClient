package com.magnifis.parking.model;

import java.lang.ref.WeakReference;

import com.magnifis.parking.MAStatus;

public class UnderstandingStatus {
	
	protected static WeakReference<UnderstandingStatus> selfWr=null;
	
	private UnderstandingStatus() {
		selfWr=new WeakReference<UnderstandingStatus>(this);
	}
	
	public static UnderstandingStatus getInstance() {
	   synchronized(UnderstandingStatus.class) {
		   UnderstandingStatus us=get();
		   if (us==null) us=new UnderstandingStatus();
		   return us;
	   }
	}
	
	public static UnderstandingStatus get() {
		return selfWr==null?null:selfWr.get();
	}

	public MAStatus status = new MAStatus(), prevStatus = new MAStatus();
	public Understanding savedReply = null;
	public boolean waitingForConfirmation = false;

}
