package com.magnifis.parking;

import java.io.Serializable;

import com.magnifis.parking.model.GeoObject;
import com.magnifis.parking.model.PkFacility;

public class MapItemIterator<T extends GeoObject> implements Serializable {
	private BitSetPlus reminder;
	
	public int indexOf(T f) {
		if ((f!=null)&&(facilities!=null))
			for (int i=0;i<facilities.length;i++) if (facilities[i]==f) return i;
		return -1;
	}
	
	private void setBits() {
	   for (int i=0;i<facilities.length;i++) reminder.set(i);
	}
	
	private T facilities[]=null;
	
	public T get(int i) {
	  if (facilities==null) return null;
	  return facilities[i];
	}
	
	public int size() {
		return (facilities==null)?0:facilities.length;
	}
	
	MapItemIterator(){}
	
	public MapItemIterator(T fas[]) {
		facilities=fas.clone();
		reset();
	}
	
	public void reset() {
		reminder=new BitSetPlus(facilities.length);
		setBits();		
	}
	

	public boolean hasNext() {
		return reminder.cardinality()>0;
	}
	

	public boolean hasPrev() {
		return reminder.cardinality()<facilities.length;
	}
	
	public T next() {
		int i=reminder.nextSetBit(0);
		if (i>=0) {
			reminder.clear(i);
			return facilities[i];
		}
		return null;
	}
	
	public T next(T prev) {
		if (prev!=null) reminder.clear(indexOf(prev));
		return next();
	}

	public T prev(T next) {
        int ix=indexOf(next);
        reminder.set(ix);
        int i=reminder.prevClearBit((ix==0)?facilities.length-1:ix);
        if (i>=0) {
        	reminder.set(i);
        	return facilities[i];          	   
        }
        return null;
	}
	
}