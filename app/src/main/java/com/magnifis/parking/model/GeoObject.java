package com.magnifis.parking.model;

import java.io.Serializable;

public abstract class GeoObject {
  abstract public DoublePoint getPoint();
  abstract public String getName();
  abstract public String getAddress();
  public String getPhone() { return  null; }
  abstract public String getUrl();
  abstract public Long getDistance(); 
  abstract public void setDistance(Long d);
  abstract public Double getDistanceInMiles(); 
  abstract public void setDistanceInMiles(Double miles);  
  
  public int compareByDistance(GeoObject go) {
	 return getDistance().compareTo(go.getDistance());
  }
  
  public String getCategory() { return null; }
  
  public String getCategoryPlural() {
	return (getCategory()==null)?null:(getCategory()+"s");
  }
  
  public String getFormattedPrice() {
	return null;
  }
  
  public String getPriceInfoToSpeak() {
	return getFormattedPrice();
  }
  
}
