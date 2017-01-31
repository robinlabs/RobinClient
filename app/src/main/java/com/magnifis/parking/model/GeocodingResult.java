package com.magnifis.parking.model;

import java.io.IOException;

import android.location.Location;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.magnifis.parking.geo.GoogleGeocoder;
import com.magnifis.parking.utils.Utils;

import static com.magnifis.parking.utils.Utils.*;


public class GeocodingResult extends DoublePoint {
	
	protected GcResult originalResult=null;
	
	public GcResult getOriginalResult() {
		return originalResult;
	}
	
	public String getState() {
	  return (originalResult==null)?null:originalResult.getState();
	}

	public void setOriginalResult(GcResult originalResult) {
		this.originalResult = originalResult;
	}

	public static GeocodingResult fromAddress(String adr,DoublePoint myLoc, String city, String street) {
		GeocodingResult result=new GeocodingResult();
		if (adr!=null) {
			try {
				if (myLoc!=null) {

					Log.i(TAG, "Geocoding " + adr + "  from (" + myLoc + ")"); 
					DoublePoint box[]=myLoc.boxWithSize(100000., 100000.); // 100x100 km
					Log.i(TAG, "Bounding box: (" + box[0] + ") -- (" + box[1] + ")"); 		
					GcResult grs[]=GoogleGeocoder.getFromLocationName(adr, box);
					if (!Utils.isEmpty(grs)) {
						double bestDist=Double.MAX_VALUE;
						for (GcResult gr:grs) {
							if (gr.isGood(city, street)) {
								Log.d(TAG+".fromAddress","found adr#1: "+gr.getGeometry().getLocation().toString()); 
								DoublePoint lc=gr.getGeometry().getLocation();
								double dist=lc.distanceInNauticalMiles(myLoc);
								if (dist<bestDist) { 
									result.set(lc); 
									result.setOriginalResult(gr);
									bestDist=dist;
								} 								
							}
						}
						if (!result.isEmpty()) return result;
					}
				}			

				GcResult grs[]=GoogleGeocoder.getFromLocationName(adr);
				boolean noloc=myLoc==null;
				if (!Utils.isEmpty(grs)) {
					double bestDist=Double.MAX_VALUE;
					for (GcResult gr:grs) {
						if (gr.isGood(city, street)) {
							Log.d(TAG+".fromAddress","found adr#1: "+gr.getGeometry().getLocation().toString()); 
							DoublePoint lc=gr.getGeometry().getLocation();
							double dist=noloc?0.:lc.distanceInNauticalMiles(myLoc);
							if (noloc||(dist<bestDist)) { 
								result.set(lc); 
								result.setOriginalResult(gr);
								if (noloc) break;
								bestDist=dist;
							} 								
						}
					}
					if (!result.isEmpty()) return result;				
				}
				result.setGood(false);
				Log.d(TAG+".fromAddress","nothing found"); 
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}				
		}
		return result;
	}


	protected boolean good=true;

	public boolean isGood() {
		return good;
	}

	public void setGood(boolean f) {
		good = f;
	}

	public GeocodingResult() {
		// TODO Auto-generated constructor stub
	}

	public GeocodingResult(String s) {
		super(s);
		// TODO Auto-generated constructor stub
	}

	public GeocodingResult(GeoPoint pt) {
		super(pt);
		// TODO Auto-generated constructor stub
	}

	public GeocodingResult(Double[] pt) {
		super(pt);
		// TODO Auto-generated constructor stub
	}

	public GeocodingResult(Location loc) {
		super(loc);
		// TODO Auto-generated constructor stub
	}

	public GeocodingResult(double lat, double lon) {
		super(lat, lon);
		// TODO Auto-generated constructor stub
	}

}
