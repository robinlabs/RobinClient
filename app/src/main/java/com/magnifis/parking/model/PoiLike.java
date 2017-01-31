package com.magnifis.parking.model;

import java.io.Serializable;
import static com.magnifis.parking.utils.Utils.*;

import com.magnifis.parking.Xml.ML;

public class PoiLike extends GeoObject implements Serializable {
	

	@Override
	public Double getDistanceInMiles() {
	   return distance;
	}
	
	@Override
	public void setDistanceInMiles(Double miles) {
	   distance=miles;
	}
	
	/**
	 * @return the name
	 */
	@Override
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the address
	 */
	@Override
	public String getAddress() {
		return address;
	}
	/**
	 * @param address the address to set
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * @return the url
	 */
	@Override
	public String getUrl() {
		return url;
	}
	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
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
	
	@Override
	public DoublePoint getPoint() {
	  if (lat==null||lon==null) return null;
	  return new DoublePoint(lat.doubleValue(),lon.doubleValue());
	}

	
	/**
	 * @return the distance
	 */
	@Override
	public Long getDistance() {
		return DoublePoint.milesToMeters(distance);
	}
	/**
	 * @param distance the distance to set
	 */
	@Override
	public void setDistance(Long meters) {
		this.distance = DoublePoint.metersToMiles(meters);
	}
	@ML("name")
	protected String name=null;
	@ML("address")
	protected String address=null;	


	@ML("url")
	protected String url=null;
	
	@ML("lat")
	protected Double lat=null;
	@ML("lon")
	protected Double lon=null;
	@ML("distance")
	protected Double distance=null;	
	
	@ML("image_url")
	protected String imageUrl=null;

	
	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	
	public String getStreetAddress() {
		if (!isEmpty(address))
			for (String part :address.split(",")) if (!isEmpty(part)) return part;
		return address;
	}
	
	@Override
	public int compareByDistance(GeoObject go) {
		return getDistanceInMiles().compareTo(go.getDistanceInMiles());
	}

}
