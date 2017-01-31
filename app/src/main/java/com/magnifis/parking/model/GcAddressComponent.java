package com.magnifis.parking.model;

import java.io.Serializable;

import com.magnifis.parking.Xml.ML;
import static com.magnifis.parking.utils.Utils.*;


public class GcAddressComponent implements Serializable {
  @ML("long_name")
  protected String longName=null;
  @ML("short_name")
  protected String shortName=null;
  @ML("type")
  protected String types[]=null;
  /**
   * @return the longName
   */
  public String getLongName() {
	  return longName;
  }
  /**
   * @param longName the longName to set
   */
  public void setLongName(String longName) {
	  this.longName = longName;
  }
  /**
   * @return the shortName
   */
  public String getShortName() {
	  return shortName;
  }
  /**
   * @param shortName the shortName to set
   */
  public void setShortName(String shortName) {
	  this.shortName = shortName;
  }
  /**
   * @return the types
   */
  public String[] getTypes() {
	  return types;
  }
  /**
   * @param types the types to set
   */
  public void setTypes(String[] types) {
	  this.types = types;
  }
  
  public boolean hasType(String t) {
	  if (!isEmpty(types)) for (String type:types) if (t.equalsIgnoreCase(type))  return true;
	  return false;
  }
}
