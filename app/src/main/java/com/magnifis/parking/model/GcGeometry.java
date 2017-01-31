package com.magnifis.parking.model;

import java.io.Serializable;

import com.magnifis.parking.Xml.ML;

public class GcGeometry implements Serializable {
   @ML("location_type")
   protected String location_type=null;
   @ML("location")
   protected DoublePoint location=null;
   @ML(tag="southwest",indirect=true)
   protected DoublePoint vpSouthwest=null;
   @ML(tag="northeast",indirect=true)
   protected DoublePoint vpNortheast=null;
   /**
    * @return the location_type
    */
   public String getLocation_type() {
	   return location_type;
   }
   /**
    * @param location_type the location_type to set
    */
   public void setLocation_type(String location_type) {
	   this.location_type = location_type;
   }
   /**
    * @return the location
    */
   public DoublePoint getLocation() {
	   return location;
   }
   /**
    * @param location the location to set
    */
   public void setLocation(DoublePoint location) {
	   this.location = location;
   }
   /**
    * @return the vpSouthwest
    */
   public DoublePoint getVpSouthwest() {
	   return vpSouthwest;
   }
   /**
    * @param vpSouthwest the vpSouthwest to set
    */
   public void setVpSouthwest(DoublePoint vpSouthwest) {
	   this.vpSouthwest = vpSouthwest;
   }
   /**
    * @return the vpNortheast
    */
   public DoublePoint getVpNortheast() {
	   return vpNortheast;
   }
   /**
    * @param vpNortheast the vpNortheast to set
    */
   public void setVpNortheast(DoublePoint vpNortheast) {
	   this.vpNortheast = vpNortheast;
   }

   public boolean isApproximateLocation() {
	   return "APPROXIMATE".equals(location_type);
   }
 
}
