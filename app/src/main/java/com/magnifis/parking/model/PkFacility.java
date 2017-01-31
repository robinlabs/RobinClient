package com.magnifis.parking.model;

import java.io.Serializable;

import android.graphics.Rect;

import com.magnifis.parking.Phrases;
import com.magnifis.parking.Xml.ML;

public class PkFacility extends GeoObject implements Serializable {
	@ML("point")
	protected Double point[] = null; // lon, lat
	@ML("cost")
	protected Integer cost = null;
	@ML("address")
	protected String address = null;
	@ML("phone")
	protected String phone = null;
	@ML("pmt_type")
	protected String pmt_type = null;
	@ML("is_open")
	protected Boolean open = null;

	@ML("f_id")
	protected Integer f_id = null;
	@ML("name")
	protected String name = null;
	@ML("type")
	protected String type = null;
	@ML("t_link")
	protected String t_link = null;
	@ML("t_link_type")
	protected String t_link_type = null;
	@ML("hrs")
	protected String hrs[] = null;
	@ML("rates")
	protected String rates[] = null;
	@ML("operator")
	protected String operator=null;
	@ML("calculated_rates")
	protected PkCalculatedRate calculated_rates[]=null;
	@ML("distance")
	protected Long distance=null;
	@ML("occupancy_pct")
	protected Integer occupancy_pct=null;
	/**
	 * @return the t_link_type
	 */
	public String getT_link_type() {
		return t_link_type;
	}

	/**
	 * @param t_link_type the t_link_type to set
	 */
	public void setT_link_type(String t_link_type) {
		this.t_link_type = t_link_type;
	}

	/**
	 * @return the occupancy_pct
	 */
	public Integer getOccupancy_pct() {
		return occupancy_pct;
	}

	/**
	 * @param occupancy_pct the occupancy_pct to set
	 */
	public void setOccupancy_pct(Integer occupancy_pct) {
		this.occupancy_pct = occupancy_pct;
	}

	/**
	 * @return the distance
	 */
	@Override
	public Long getDistance() {
		return distance;
	}
	
	@Override
	public Double getDistanceInMiles() {
		return DoublePoint.metersToMiles(distance);
	}
	
	@Override
	public void setDistanceInMiles(Double miles) {
	  setDistance(DoublePoint.milesToMeters(miles));
	}

	/**
	 * @param distance the distance to set
	 */
	@Override
	public void setDistance(Long distance) {
		this.distance = distance;
	}

	/**
	 * @return the ts
	 */
	public Long getTs() {
		return ts;
	}

	/**
	 * @param ts the ts to set
	 */
	public void setTs(Long ts) {
		this.ts = ts;
	}

	@ML("ts")
	protected Long ts=null;

	/**
	 * @return the operator
	 */
	public String getOperator() {
		return operator;
	}

	/**
	 * @param operator the operator to set
	 */
	public void setOperator(String operator) {
		this.operator = operator;
	}

	/**
	 * @return the calculated_rates
	 */
	public PkCalculatedRate[] getCalculated_rates() {
		return calculated_rates;
	}

	/**
	 * @param calculated_rates the calculated_rates to set
	 */
	public void setCalculated_rates(PkCalculatedRate[] calculated_rates) {
		this.calculated_rates = calculated_rates;
	}

	/**
	 * @return the f_id
	 */
	public Integer getF_id() {
		return f_id;
	}

	/**
	 * @param f_id
	 *            the f_id to set
	 */
	public void setF_id(Integer f_id) {
		this.f_id = f_id;
	}

	/**
	 * @return the name
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the t_link
	 */
	public String getT_link() {
		return t_link;
	}

	/**
	 * @param t_link
	 *            the t_link to set
	 */
	public void setT_link(String t_link) {
		this.t_link = t_link;
	}

	/**
	 * @return the hrs
	 */
	public String[] getHrs() {
		return hrs;
	}

	/**
	 * @param hrs
	 *            the hrs to set
	 */
	public void setHrs(String[] hrs) {
		this.hrs = hrs;
	}

	/**
	 * @return the rates
	 */
	public String[] getRates() {
		return rates;
	}

	/**
	 * @param rates
	 *            the rates to set
	 */
	public void setRates(String[] rates) {
		this.rates = rates;
	}

	/**
	 * @return the point
	 */
	@Override
	public DoublePoint getPoint() {
		return point==null?null:new DoublePoint(point);
	}

	/**
	 * @param point
	 *            the point to set
	 */
	public void setPoint(Double[] point) {
		this.point = point;
	}

	/**
	 * @return the cost
	 */
	public Integer getCost() {
		return cost;
	}

	/**
	 * @param cost
	 *            the cost to set
	 */
	public void setCost(Integer cost) {
		this.cost = cost;
	}

	/**
	 * @return the address
	 */
	@Override
	public String getAddress() {
		return address;
	}

	/**
	 * @param address
	 *            the address to set
	 */
	public void setAddress(String address) {
		this.address = address;
	}
	
	/**
	 * @return the phone
	 */
	@Override
	public String getPhone() {
		return phone;
	}

	/**
	 * @param phone
	 *            the phone to set
	 */
	public void setPhone(String phone) {
		this.phone = phone;
	}

	/**
	 * @return the pmt_type
	 */
	public String getPmt_type() {
		return pmt_type;
	}

	/**
	 * @param pmt_type
	 *            the pmt_type to set
	 */
	public void setPmt_type(String pmt_type) {
		this.pmt_type = pmt_type;
	}

	/**
	 * @return the open
	 */
	public boolean isOpen() {
		return (open==null)||open;
	}

	/**
	 * @param open
	 *            the open to set
	 */
	public void setOpen(Boolean open) {
		this.open = open;
	}
	
	public boolean isAvailable () {
	  return isOpen()&&((occupancy_pct==null)||(occupancy_pct<100))
			  &&getCalculated_rates()!=null
				 &&getCalculated_rates().length>0
			  
			  ;
	}
	
	public Double getPerHourRate() {
		if (calculated_rates!=null) {
		  for (PkCalculatedRate c:calculated_rates)
			/*if ("1:00:00".equals(c.getQuoted_duration()))*/ return c.getRate_cost();
		  for (PkCalculatedRate c:calculated_rates)
			if (c.getQuoted_duration()==null) return c.getRate_cost();		  
		}
		return null;
	}
	
	final public static int  LOAD_LOW=0 , LOAD_MEDIUM=1, LOAD_HIGH=2, LOAD_UNKNOWN=-1;

	public int getLoad() {
		if (getOccupancy_pct()==null) return LOAD_UNKNOWN;
		int occ=getOccupancy_pct();
		if (occ<=30) return LOAD_LOW;
		if (occ<=60) return LOAD_MEDIUM;
		return LOAD_HIGH;
	} 
	
	
	public boolean anyCostInfo() {
		return (getPerHourRate()!=null)||((getRates()!=null)&&(getRates().length>0));
	}

	@Override
	public String getUrl() {
		return getT_link();
	}

	@Override
	public String getCategory() {
		return "parking";
	}
	
	@Override
	public String getFormattedPrice() {
		Double price=getPerHourRate();
		String pr=null;
		if (price!=null) {
			pr=price.toString();
			if (pr.endsWith(".0")) pr=pr.substring(0, pr.length()-2);
			pr="$"+pr;
		}
	    return pr;
	}
	
	@Override
	public String getPriceInfoToSpeak() {
	  StringBuilder sb=new StringBuilder();
	  Phrases.formCostText(sb, this, false);
	  return sb.toString();
	}
}
