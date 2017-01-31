package com.magnifis.parking.model;

import java.io.Serializable;

import com.magnifis.parking.Xml.ML;
import static com.magnifis.parking.utils.Utils.*;

public class GcResult implements Serializable {
	@ML("type")
	protected String types[] = null;
	@ML("formatted_address")
	protected String formattedAddress = null;
	@ML("address_component")
	protected GcAddressComponent addressComponents[] = null;
	@ML("geometry")
	protected GcGeometry geometry = null;

	/**
	 * @return the type
	 */
	public String []getTypes() {
		return types;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setTypes(String types[]) {
		this.types = types;
	}

	/**
	 * @return the formattedAddress
	 */
	public String getFormattedAddress(boolean compact) {
		String addr = formattedAddress;
		if (compact && addr != null) { 
			if (addr.toLowerCase().matches("(.+), [a-z]{2} [0-9]{5}, (usa|ca|canada|uk)")) // trim zip code, state and county 
				addr = addr.substring(0, addr.length()-15); 
		}
		
		return addr; 
	}

	/**
	 * @param formattedAddress
	 *            the formattedAddress to set
	 */
	public void setFormattedAddress(String formattedAddress) {
		this.formattedAddress = formattedAddress;
	}

	/**
	 * @return the addressComponents
	 */
	public GcAddressComponent[] getAddressComponents() {
		return addressComponents;
	}

	/**
	 * @param addressComponents
	 *            the addressComponents to set
	 */
	public void setAddressComponents(GcAddressComponent[] addressComponents) {
		this.addressComponents = addressComponents;
	}

	/**
	 * @return the geometry
	 */
	public GcGeometry getGeometry() {
		return geometry;
	}

	/**
	 * @param geometry
	 *            the geometry to set
	 */
	public void setGeometry(GcGeometry geometry) {
		this.geometry = geometry;
	}
	
	public String getState() {
		if (!isEmpty(addressComponents)) {
			for (GcAddressComponent ac : addressComponents) 
				if (ac.hasType("political")&&
						ac.hasType("administrative_area_level_1")
						) return ac.getLongName();
		}
		return null;
	}
	
	private static String filterAc(String ac) {
	  while (!isEmpty(ac)) {
		if (Character.isUnicodeIdentifierPart(ac.charAt(ac.length()-1))) break;
		ac=ac.substring(0, ac.length()-1);
		//if (ac.charAt(ac.length()-1)<'/'
	  }
	  return ac;
	}

	public boolean isGood(String city, String street) {
		boolean ok = true;
		if (!isEmpty(addressComponents)) {
			if (!isEmpty(city)) {
				ok = false;
				for (GcAddressComponent ac : addressComponents) {
					if (ac.hasType("political")&&
						(ac.hasType("locality") ||  ac.hasType("sublocality") || ac.hasType("administrative_area_level_2"))&&
						(city.equalsIgnoreCase(filterAc(ac.getLongName()))||
						 city.equalsIgnoreCase(filterAc(ac.getShortName()))
						)
					) {
						ok = true;
						break;
					}
				}
			}
			if (ok&&!isEmpty(street)) {
				ok = false;
				for (GcAddressComponent ac : addressComponents) {
					if (!ac.hasType("political")) {
					  ok= true; break;
					}
				}
				
			}
		}
		return ok;
	}

}
