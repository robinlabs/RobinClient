package com.magnifis.parking.model;

import java.io.IOException;
import java.io.Serializable;
import java.net.URLDecoder;
import java.util.List;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.magnifis.parking.App;
import com.magnifis.parking.MainActivity;
import com.magnifis.parking.R;
import com.magnifis.parking.UserLocationProvider;
import com.magnifis.parking.Xml.ML;
import com.magnifis.parking.geo.GoogleGeocoder;
import com.magnifis.parking.utils.Utils;

import static com.magnifis.parking.utils.Utils.*;

public class Origin implements Serializable {
	static public final String TAG="Place";
	
	public Origin() {}
	
	public Origin(DoublePoint loc) {
		setLocation(loc);
		setFullAddress(loc.toString());
	}
	
	
    public boolean resoleCustomLocationIfAny(Understanding u, boolean fixQInt) {
    	String cl=getCustomLocation();
    	if (cl!=null) {
    		if (
    		  cl.equalsIgnoreCase("near you")||
    		  cl.equalsIgnoreCase("around here")||
    		  cl.equalsIgnoreCase("here")
    		) {
    			DoublePoint loc=u.getGpsLocation();
    			if (loc==null) loc=UserLocationProvider.readLocationPoint();
    			if (loc!=null) {
    			  setLocation(loc);
    			  setFullAddress(loc.toString());
    			  return true;
    			}
    			if (fixQInt) {
        			u.setQueryInterpretation(
        	    	  App.self.getString(R.string.P_DONT_KNOW_WHERE_YOU_ARE)
        	    	);
    			}
    			return false;
    		}
    		String key="learn:"+cl;
    		SharedPreferences prefs=App.self.getPrefs();
    		String val=prefs.getString(key, null);
    		if (val!=null) {
    		   String a[]=val.split(":");
    		   if (a.length>0) try {
    			   if (a.length>1) setLocation(new DoublePoint(a[1])); 
				   setFullAddress(URLDecoder.decode(a[0],"UTF-8"));
				   return true; // resolved
    		   } catch(Throwable t) {}
    		}
    		if (fixQInt) {
    			u.setQueryInterpretation(
    			  Utils.getString(R.string.P_TEACH_ME_WHERE_IS,"dst", cl)
    			);
    		}
    		return false;
    	}
    	return true; // not a custom location
    }
	
	public boolean isGasStation() {
	  return "gas stations".equalsIgnoreCase(poiCategory);
	}
	
	@ML("poi_category")
	protected String poiCategory=null;
	
	public String getPoiCategory() {
		return poiCategory;
	}

	public void setPoiCategory(String poiCategory) {
		this.poiCategory = poiCategory;
	}

	@ML("poi_name")
	protected String poiName = null;
	@ML("custom_location")
	protected String custom_location = null;
	
	/**
	 * @return the poi_name
	 */
	public String getPoiName() {
		return poiName;
	}

	/**
	 * @param poi_name the poi_name to set
	 */
	public void setPoiName(String poi_name) {
		this.poiName = poi_name;
	}

	/**
	 * @return the custom_location
	 */
	public String getCustomLocation() {
		return custom_location;
	}

	/**
	 * @param custom_location the custom_location to set
	 */
	public void setCustomLocation(String custom_location) {
		this.custom_location = custom_location;
	}

	public boolean anyPoiInfo() {
	   return !(isEmpty(getPoiName())&&isEmpty(getPoiCategory()));
	}
	
	public boolean anyAddressInfo() {
	  return !(isEmpty(getCity())&&isEmpty(getStreet()));
	}
	

	/**
	 * @return the street
	 */
	public String getStreet() {
		return street;
	}

	/**
	 * @param street
	 *            the street to set
	 */
	public void setStreet(String street) {
		this.street = street;
	}

	/**
	 * @return the house
	 */
	public String getHouse() {
		return house;
	}

	/**
	 * @param house
	 *            the house to set
	 */
	public void setHouse(String house) {
		this.house = house;
	}

	@ML("street")
	protected String street = null;

	@ML("house")
	protected String house = null;

	@ML("city")
	protected String city = null;
	
	@ML("state")
	protected String state = null;
	
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	@ML("full_address")
	protected String full_address = null;
	
	@ML(tag="full_address",attr="onGcError")
	protected String onGcError=null;
	
	public String getOnGcError() {
		return onGcError;
	}

	public void setOnGcError(String onGcError) {
		this.onGcError = onGcError;
	}

	final private Pattern latlonP=Pattern.compile("^[-]?\\d+([.]\\d*)?[,][-]?\\d+([.]\\d*)?$");
	
	public boolean isAddressLatlon() {
	   if (full_address!=null) return latlonP.matcher(full_address).matches();
	   return false;
	}

	/**
	 * @return the city
	 */
	public String getCity() {
		return city;
	}

	/**
	 * @param city
	 *            the city to set
	 */
	public void setCity(String city) {
		this.city = city;
	}

	/**
	 * @return the full_address
	 */
	public String getFullAddress() {
		if (anyPoiInfo()) {
			//String city=getCity(), street=getStreet(), state=getState();
			StringBuilder sb=new StringBuilder();
			if (!isEmpty(street)) sb.append(street);
			if (!isEmpty(city)) {	
			  if (sb.length()>0) sb.append(", ");
			  sb.append(city);
			}
			if (!isEmpty(state)) {
			  if (sb.length()>0) sb.append(", ");
			  sb.append(state);
		    }
		    
			return sb.length()>0?sb.toString():null;
		}
		return full_address;
	}

	/**
	 * @param full_address
	 *            the full_address to set
	 */
	public void setFullAddress(String full_address) {
		this.full_address = full_address;
	}
	
	/**
	 * @return the lat
	 */
	public Double getLat() {
		return lat;
	}

	/**
	 * @param lat the lat to set
	 */
	public void setLat(Double lat) {
		this.lat = lat;
	}

	/**
	 * @return the lon
	 */
	public Double getLon() {
		return lon;
	}

	/**
	 * @param lon the lon to set
	 */
	public void setLon(Double lon) {
		this.lon = lon;
	}

	@ML("lat")
	protected Double lat=null;
	@ML("lon")
	protected Double lon=null;
	
	private DoublePoint _loc=null;
	
	public void setLocation(DoublePoint dp) {
	  _loc=(dp==null||dp.isEmpty())?null:dp;
	}
	
	public static DoublePoint getLocation(Origin org) {
		return org==null?null:org.getLocation(org);
	}
	
	public DoublePoint getLocation() {
	  if (_loc!=null) return _loc;
	  if (lat!=null&&lon!=null) return _loc=new DoublePoint(lat,lon);
	  /*
	  Poi poi=getPoi();
	  return poi==null?null:poi.getPoint();
	  */
	  return null;
	}
	
	public boolean calculateCityState(Context ctx) {
		return calculateCityState(ctx,getLocation());
	}
	
	public boolean calculateCityState(Context ctx,DoublePoint pt) {
	   if (!(isEmpty(getCity())||isEmpty(getState()))) return true;
	   if (pt==null) {
		   String city=getCity();
		   if (isEmpty(city)) return false;
		   GeocodingResult gr=GeocodingResult.fromAddress(
				getFullAddress(), UserLocationProvider.readLocationPoint() /*myLoc*/, 
				   city, getStreet()
		   );
		   if (gr!=null&&!gr.isEmpty()) {
			  setState(gr.getState());
			  Log.d(TAG, "/calculateCityStateFromThePoint:/ "+gr.getState());
		   }
		   return true;
	   } else {
		  Geocoder gc=new Geocoder(ctx);
		  try {
			List<Address> adrs=null;
			adrs=gc.getFromLocation(pt.getLat(), pt.getLon(), 1);
			if (!isEmpty(adrs)) {
			  String city=adrs.get(0).getLocality();
//			  if (city!=null&&!city.equalsIgnoreCase(getCity())) continue;
			  setCity(city);
			  String state=adrs.get(0).getAdminArea();
			  setState(state); 
			  return (city!=null)||(state!=null);
			}
		  } catch(Throwable t) {
			t.printStackTrace();
		  }
	   }
	   return false;
	}
	
	
	private boolean locationIsGood=true;
	
	public boolean isLocationGood() {
		return locationIsGood;
	}
	
	public boolean calculateLocation(DoublePoint myLoc) {
		if (getLocation()!=null) return true;
		String adr=getFullAddress();

		if (adr!=null) { 
		  GeocodingResult gr=GeocodingResult.fromAddress(adr, myLoc, city, street);
		  if (locationIsGood=gr.isGood()) setLocation(gr); 
		}
			
		return getLocation()!=null;
	}

}
