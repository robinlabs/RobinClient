package com.magnifis.parking.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;

import com.magnifis.parking.MapItemIterator;
import static com.magnifis.parking.utils.Utils.*;

import android.content.Context;

public abstract class GeoSpannable<T extends GeoObject> implements Serializable, SortableByDistance {
	
    public void calculate(Context ctx, Understanding rp) {
		calculate(rp.getOriginLocation(), rp.getOrderBy());
    }
    
    protected T []orgSet=null;
    protected DoublePoint orgLoc=null;
    
    public void recalculate() {
    	if (this instanceof FacilitiesSetter) {
    	  ((FacilitiesSetter<T>)this).setFacilities(orgSet);
    	  calculate(orgLoc, orderBy);
    	}
    }
    
    public void calculate(DoublePoint orgLoc, String orderBy) {
    	if (orgSet==null) orgSet=getFacilities();
    	this.orgLoc=orgLoc;
    	if (this instanceof Filterable) ((Filterable)this).filterOutFacilites();
		fixMissedDistances(orgLoc);
		calculateTheSpan(orgLoc);
		if (this instanceof Sortable) ((Sortable)this).orderThem(orderBy);
    }
    
    
    public T getNearestTo(DoublePoint dp) {
      T[] fass=getFacilities();
      if (isEmpty(fass)) return null;
      T best=null; Double best_distable=Double.MAX_VALUE;
      for (T fas:fass) {
    	Double d=fas.getDistanceInMiles();
    	if (d<best_distable) { best_distable=d; best=fas; }
      }
      return best;
    }
    
	public int countAvailable() {
	  return getFacilities()==null?0:getFacilities().length;
	}

    
	protected String orderBy=null;
	
	public String getOrderBy() {
	  return orderBy;
	}
	
	@Override
	public boolean orderByDistance() {
		  if (Understanding.ORDER_DISTANCE.equals(orderBy)) return false; // do nothing
		  orderBy=Understanding.ORDER_DISTANCE;
		  T facilities[]=getFacilities();
		  if (facilities!=null) Arrays.sort(
			  facilities, 
			  new Comparator<T>() {

				public int compare(T o1, T o2) {
					return o1.getDistance().compareTo(o2.getDistance());
				}
				  
			  }
		  );
		  return true;
		}	
 
	private DoublePoint [] boundBox=null;
	
	public DoublePoint [] getBoundBox() {
	   return boundBox;
	}
	
	abstract public T[] getFacilities();
	
	public MapItemIterator<T> facilities() {
		return new MapItemIterator<T>(getFacilities());
	}

	// returns mix. max and center
	protected void calculateTheSpan(DoublePoint dp) {
	   boundBox=dp.calculateSpanOf(getFacilities());
	}
	
	protected void fixMissedDistances(DoublePoint originLocation) {
		   if (getFacilities()!=null) for (T p:getFacilities()) {
				if (p.getDistance()==null) p.setDistance(
						originLocation.distanceInMeters(p.getPoint())
						);
		   }
		}
   
}
