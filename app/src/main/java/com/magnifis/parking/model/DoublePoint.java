package com.magnifis.parking.model;

import java.io.IOException;
import java.io.Serializable;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.magnifis.parking.Xml.ML;
import com.magnifis.parking.Xml.ML_alternatives;
import com.magnifis.parking.geo.GoogleGeocoder;
import com.magnifis.parking.utils.Utils;

public class DoublePoint implements Serializable, Cloneable {
	static final String TAG=DoublePoint.class.getName();
	
	public static DoublePoint center(DoublePoint orgLoc, DoublePoint dstLoc) {
		return  new DoublePoint(
                (orgLoc.getLat()+dstLoc.getLat())/2,
                (orgLoc.getLon()+dstLoc.getLon())/2
			  );		
	}
	
	public static DoublePoint span(DoublePoint orgLoc, DoublePoint dstLoc) {
		return new DoublePoint(
			Math.abs(dstLoc.getLat()-orgLoc.getLat()),
			Math.abs(dstLoc.getLon()-orgLoc.getLon())
		);
	}
	
	public void set(DoublePoint dp) {
	  if (dp==null) { 
		lat=null; lon=null;
	  } else {
		lat=dp.getLat(); lon=dp.getLon();
	  }
	}
	
	@ML("lat")
	protected Double lat=null;
	@ML_alternatives({
	  @ML("lng"),
	  @ML("lon")
	})
	protected Double lon=null;
	
	public boolean isEmpty() {
		return lat==null&& lon==null;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o!=null&&o instanceof DoublePoint) {
		  DoublePoint dp=(DoublePoint)o;
		  return (lat==dp.lat)&&(lon==dp.lon);
		}
		return super.equals(o);
	}

	public DoublePoint incLat(double inc) {
		lat+=inc;
		return this;
	}
	
	public DoublePoint incLon(double inc) {
		lon+=inc;
		return this;
	}	
	
	public DoublePoint mulLat(double inc) {
		lat*=inc;
		return this;
	}
	
	public DoublePoint mulLon(double inc) {
		lon*=inc;
		return this;
	}	
	
	@Override
	public DoublePoint clone() {
		try {
			DoublePoint pt=(DoublePoint) super.clone();
			pt.lat=lat==null?null:lat.doubleValue();
			pt.lon=lon==null?null:lon.doubleValue();
			return pt;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
	  return new DoublePoint(lat,lon);
	}
	
	public DoublePoint() {}
	
	public DoublePoint(String s) {
      fromString(s);
	}
	
	public DoublePoint(GeoPoint pt) {
	  lat=pt.getLatitudeE6()/1e6;
	  lon=pt.getLongitudeE6()/1e6;
	}

	public DoublePoint(Double pt[]) { // lon, lat
		this(pt[1], pt[0]);
	}
	
	public static DoublePoint from(Location loc) {
		return loc==null?null:new DoublePoint(loc);
	}
	
	public static DoublePoint from(GeoPoint loc) {
		return loc==null?null:new DoublePoint(loc);
	}

    public static DoublePoint from(String s) {
        return Utils.isEmpty(s)?null:new DoublePoint(s);
    }

    public DoublePoint(Location loc) {
		if (loc!=null) {
		  lat = loc.getLatitude();
		  lon = loc.getLongitude();
		}
	}

	public DoublePoint(double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
	}
	/**
	 * @return the lat
	 */
	public double getLat() {
		return lat;
	}

	/**
	 * @param lat
	 *            the lat to set
	 */
	public void setLat(double lat) {
		this.lat = lat;
	}

	/**
	 * @return the lon
	 */
	public double getLon() {
		return lon;
	}

	/**
	 * @param lon
	 *            the lon to set
	 */
	public void setLon(double lon) {
		this.lon = lon;
	}

	public GeoPoint toGeoPoint() {
		return new GeoPoint((int) (lat * 1e6), (int) (lon * 1e6));
	}

	public String toString() {
		return lat + "," + lon;
	}

    public void fromString(String s) {
        lat = 0.0;
        lon = 0.0;
        if (Utils.isEmpty(s))
            return;

        String[] x = s.split(",");
        if (x.length < 2)
            return;

        try {
            lat = Double.parseDouble(x[0]);
            lon = Double.parseDouble(x[1]);
        } catch (Exception e) {}
    }

    public DoublePoint distantByMeters(double dx,double dy) {
		double delta_lon = dx/(111320.*Math.cos(Math.toRadians(lat))); // dx, dy in meters
		double delta_lat = dy/110540.;  //result in degrees long/lat
		
		return new DoublePoint(lat+delta_lat, lon+delta_lon);
	}
	
	public DoublePoint []boxWithSize(double dxMeters,double dyMeters) {
		dxMeters/=2;
		dyMeters/=2;
		return new DoublePoint[] {
		   distantByMeters(-dxMeters,-dyMeters),
		   distantByMeters(dxMeters,dyMeters)
		};
	}

	/*
	 * ////////////////////////// //
	 * http://stackoverflow.com/questions/5845357/distancebetween-two-locations
	 * 
	 * And just to explain how it works -- the trig stuff computes
	 * the angle between point one, the center of the earth, and point two,
	 * which is then converted from radians to degrees. It next uses the
	 * original definition of nautical mile which is that there are 60nm along a
	 * great circle route that subtends one degree. Then you multiply by the
	 * 1.1515 to convert nm to statute miles. Finally, if you want the answer in
	 * km you multiply by 1.609344 to convert statute miles to km. And if you
	 * want the answer in nm, you multiply statute miles by 0.8684 to undo the
	 * conversion from nm to statute miles. – QuantumMechanic Apr 30 '11 at
	 * 23:49
	 * 
	 * k is for the answer in Kilometers, N is for Nautical Miles. What do you
	 * mean by "the difference always in the long?" – Jason Cragun Apr 30 '11 at
	 * 23:57
	 */
	public static double distance(double lat1, double lon1, double lat2, double lon2,
			char unit) {
		
		lat1=Math.toRadians(lat1);
		lat2=Math.toRadians(lat2);
		lon1=Math.toRadians(lon1);
		lon2=Math.toRadians(lon2);
		
		double theta = lon1 - lon2;
		double dist = Math.sin(lat1) * Math.sin(lat2)
				+ Math.cos(lat1) * Math.cos(lat2)
				* Math.cos(theta);
		
		dist = Math.acos(dist);
		dist = Math.toDegrees(dist);
		dist = dist * 60;
		switch (unit) {
		  case 'N': return dist; // nautical miles
		  case 'M': return dist*1852.; // meters
		  case 'K': return (dist*1852.)/1000.;
		  default:
			  return dist*1.1515; // statute miles;
		}
	}

	public double distance(DoublePoint other,char unit) {
		return distance(this.lat,this.lon,other.lat,other.lon,unit);
	}
	
	public long distanceInMeters(DoublePoint other) {
		  float rv[]=new float[1];
		  Location.distanceBetween(
				  getLat(),
				  getLon(), 
				  other.getLat(),
				  other.getLon(),					
				  
				 rv);
		  
		return Math.round(rv[0]);
		
		//return Math.round(distance(other,'M'));
	}
	
	public double distanceInNauticalMiles(DoublePoint other) {
		return distance(other,'N');
	}	
	
	public double distanceInStatuteMiles(DoublePoint other) {
		return distance(other,' ');
	}
	
	public static void updateGabarites(DoublePoint dp[], DoublePoint ...pts) {
		for (DoublePoint pt:pts) {
    		dp[0].setLat(Math.min(dp[0].getLat(), pt.getLat()));
    		dp[0].setLon(Math.min(dp[0].getLon(), pt.getLon()));
    		dp[1].setLat(Math.max(dp[1].getLat(), pt.getLat()));
    		dp[1].setLon(Math.max(dp[1].getLon(), pt.getLon()));
		}		
		dp[2].setLat((dp[1].getLat()+dp[0].getLat())/2);
		dp[2].setLon((dp[1].getLon()+dp[0].getLon())/2);
	}
	
	public static DoublePoint [] getUpdatedGabarites(DoublePoint bounds[]/*3*/, DoublePoint ...pts) {
		DoublePoint dp[]={pts[0].clone(),pts[0].clone(),pts[0].clone()};
		for (int i=1;i<pts.length;i++) {
			DoublePoint pt=pts[i];
    		dp[0].setLat(Math.min(dp[0].getLat(), pt.getLat()));
    		dp[0].setLon(Math.min(dp[0].getLon(), pt.getLon()));
    		dp[1].setLat(Math.max(dp[1].getLat(), pt.getLat()));
    		dp[1].setLon(Math.max(dp[1].getLon(), pt.getLon()));
		}
		if (bounds==null) {
			dp[2].setLat((dp[1].getLat()+dp[0].getLat())/2);
			dp[2].setLon((dp[1].getLon()+dp[0].getLon())/2);			
		} else {
			updateGabarites(dp,bounds[0],bounds[1]);
		}
		return dp;
	}
	
	public DoublePoint [] calculateSpanOf(GeoObject gobs[]) {
		DoublePoint dp[]={clone(),clone(),clone()};
		if (gobs!=null) for (GeoObject f:gobs) {
           DoublePoint pt=f.getPoint();
           if (pt!=null) {
        		dp[0].setLat(Math.min(dp[0].getLat(), pt.getLat()));
        		dp[0].setLon(Math.min(dp[0].getLon(), pt.getLon()));
        		dp[1].setLat(Math.max(dp[1].getLat(), pt.getLat()));
        		dp[1].setLon(Math.max(dp[1].getLon(), pt.getLon()));
           }
		}
		dp[2].setLat((dp[1].getLat()+dp[0].getLat())/2);
		dp[2].setLon((dp[1].getLon()+dp[0].getLon())/2);
		return dp;
	}
	
    public static final double METERS_IN_MILE=1609.347218694;
    
    public static Long milesToMeters(Double miles) {
      return miles==null?null:Math.round(miles*METERS_IN_MILE);
    }
    
    public static Double metersToMiles(Long meters) {
        return meters==null?null:((double)meters)/METERS_IN_MILE;
    }
    
    public boolean isInBox(DoublePoint dp0, DoublePoint dp1) {
      double mnlon=Math.min(dp0.getLon(), dp1.getLon());
      if (getLon()<mnlon) return false;
      double mnlat=Math.min(dp0.getLat(), dp1.getLat());
      if (getLat()<mnlat) return false;
      
      double mxlon=Math.max(dp0.getLon(), dp1.getLon());
      if (getLon()>mxlon) return false;
      double mxlat=Math.max(dp0.getLat(), dp1.getLat());
      if (getLat()>mxlat) return false;
      
      return true;
    }
}
