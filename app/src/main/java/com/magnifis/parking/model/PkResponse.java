package com.magnifis.parking.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.Iterator;

import com.magnifis.parking.BitSetPlus;
import com.magnifis.parking.Log;
import com.magnifis.parking.MapItemIterator;
import com.magnifis.parking.Xml.ML;

public class PkResponse extends GeoSpannable<PkFacility> 
       implements Serializable, Filterable, Sortable, SortableByDistance, SortableByPrice
{
	final static String TAG="PkResponse";
	
	
	@ML("RateQuery")
	protected PkRateQuery rateQuery = null;

	/**
	 * @return the rateQuery
	 */
	public PkRateQuery getRateQuery() {
		return rateQuery;
	}

	/**
	 * @param rateQuery
	 *            the rateQuery to set
	 */
	public void setRateQuery(PkRateQuery rateQuery) {
		this.rateQuery = rateQuery;
	}

	/**
	 * @return the notices
	 */
	public String[] getNotices() {
		return notices;
	}

	/**
	 * @param notices
	 *            the notices to set
	 */
	public void setNotices(String[] notices) {
		this.notices = notices;
	}
	
	@ML("Notifications")
	protected PkNotification notifications[]=null;

	/**
	 * @return the notifications
	 */
	public PkNotification[] getNotifications() {
		return notifications;
	}

	/**
	 * @param notifications the notifications to set
	 */
	public void setNotifications(PkNotification[] notifications) {
		this.notifications = notifications;
	}

	@ML("Notices")
	protected String notices[] = null;
	
	@ML("Facilities")
	protected PkFacility facilities[] = null;

	/**
	 * @return the facilities
	 */
	@Override
	public PkFacility[] getFacilities() {
		return facilities;
	}
	
	
	public PkFacility  getFacility(int ix) {
	  return (facilities==null)?null:facilities[ix];
	}
	
	/**
	 * @param facilities the facilities to set
	 */
	public void setFacilities(PkFacility[] facilities) {
		this.facilities = facilities;
	}
	
	private int _countAvailable() {
		int counter=0;
		if (facilities!=null) for (PkFacility fas:facilities)
			if (fas.isAvailable()) ++counter;
		return counter;
	}
		
	
	public int filterOutFacilites() {
	   int cnt=_countAvailable();
	   if (cnt==0) facilities=null; else  {
		   PkFacility ar[]=new PkFacility[cnt];
		   int i=0;
		   for (PkFacility fas:facilities) 
			 if (fas.isAvailable()) ar[i++]=fas;
		   setFacilities(ar);
	   }
	   return cnt;
	}
	
	public void orderThem(String orderBy) {
		if (Understanding.orderByDistance(orderBy))
			  orderByDistance();
			else if (Understanding.orderByPrice(orderBy))
			  orderByPrice();
			else if (Understanding.orderByVacancy(orderBy))
			  orderParkingsByVacancy();		
	}
	
	/*
	private Integer [] createIndex() {
	  Integer rv[]=new Integer[facilities.length];
	  for (int i=0;i<facilities.length;i++) rv[i]=i;
	  return rv;
	}*/
	
	@Override
	public boolean orderByPrice() {
	  if (Understanding.orderByPrice(orderBy)) return false;
	  orderBy=Understanding.ORDER_PRICE;
      if (facilities!=null) {
			Arrays.sort(
			  facilities, 
			  new Comparator<PkFacility>() {
				public int compare(PkFacility f0, PkFacility f1) {
				    Double r0=f0.getPerHourRate(), r1=f1.getPerHourRate();
				    if (r0==null) r0=Double.MAX_VALUE;
				    if (r1==null) r1=Double.MAX_VALUE;
					return r0.compareTo(r1);
				}
			  }
		    );
	   }
       return true;
	}
	
	public boolean orderParkingsByVacancy() {
		  if (Understanding.orderByVacancy(orderBy)) return false;
		  orderBy=Understanding.ORDER_VACANCY;
	      if (facilities!=null) {
				Arrays.sort(
				  facilities, 
				  new Comparator<PkFacility>() {
					public int compare(PkFacility f0, PkFacility f1) {
					    Integer r0=f0.getOccupancy_pct(), r1=f1.getOccupancy_pct();
					    if (r0==null) r0=Integer.MAX_VALUE;
					    if (r1==null) r1=Integer.MAX_VALUE;
						return r0.compareTo(r1);
					}
				  }
			    );
		   }
	       return true;
	}

}
