package com.magnifis.parking.model;

import java.io.Serializable;

import com.magnifis.parking.Xml.ML;

public class GasPrice implements Serializable {
	
/*
	public String getRoundedPrice() {
		if (price==null) return null;
		return String.format("%1$.2f", Math.round(price*100.)/100.);
	}
*/
	
	public String getFormattedPrice() {
		if (price==null) return "n/a";
		return String.format("%1$.2f", price.doubleValue());
	}	
	
	public void roundThePrice() {
		if (price!=null) price=Math.round(price*100.)/100.;
	}
	
	public static Double min(Double p1, GasPrice p2) {
		  if ((p1==null)&&GasStation.isEmpty(p2)) return null;
		  if (p1==null) return p2.getPrice();
		  if (GasStation.isEmpty(p2)) return p1;
		  return Math.min(p1, p2.getPrice());
    }
	
	public static Double max(Double p1, GasPrice p2) {
		  if ((p1==null)&&GasStation.isEmpty(p2)) return null;
		  if (p1==null) return p2.getPrice();
		  if (GasStation.isEmpty(p2)) return p1;
		  return Math.max(p1, p2.getPrice());
    }
	
	public static Double min(GasPrice p1, GasPrice p2) {
	  if (GasStation.isEmpty(p1)&&GasStation.isEmpty(p2)) return null;
	  if (GasStation.isEmpty(p1)) return p2.getPrice();
	  return Math.min(p1.getPrice(), p2.getPrice());
	}

	public static Double max(GasPrice p1, GasPrice p2) {
		if (GasStation.isEmpty(p1)&&GasStation.isEmpty(p2)) return null;
		if (GasStation.isEmpty(p1)) return p2.getPrice();
		return Math.max(p1.getPrice(), p2.getPrice());
	}	
	
	public void markMinMax(Double min, Double max) {
		if (price==null||(min==null&&max==null)) return;
		if (min!=null&&max!=null&&(min.doubleValue()==max.doubleValue())) return;
		if ((min!=null)&&(price.doubleValue()==min.doubleValue())) setMin(true); 
		if ((max!=null)&&(price.doubleValue()==max.doubleValue())) setMax(true);
	}
	
	boolean min=false, max=false;
	
	public boolean isMin() {
		return min;
	}
	public void setMin(boolean min) {
		this.min = min;
	}
	public boolean isMax() {
		return max;
	}
	public void setMax(boolean max) {
		this.max = max;
	}
	
	@ML
	protected Double price=null;
	@ML(attr="lowest_price")
	protected Boolean lowestPrice=null;
	
	public Double getPrice() {
		return price;
	}
	public void setPrice(Double price) {
		this.price = price;
	}
	public boolean isLowestPrice() {
		return lowestPrice==null?false:lowestPrice;
	}
	public void setLowestPrice(Boolean lowestPrice) {
		this.lowestPrice = lowestPrice;
	}
}
