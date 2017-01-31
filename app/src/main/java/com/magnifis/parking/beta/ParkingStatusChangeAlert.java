package com.magnifis.parking.beta;


import com.magnifis.parking.model.PkFacility;

public class ParkingStatusChangeAlert implements DriverAlert {
	
	PkFacility facility; 
	
	public ParkingStatusChangeAlert(PkFacility facility) 
	{
		this.facility = facility; 
	}
	
	
	public PkFacility getFacility() {
		return facility; 
	}
	
	public AlertType getType() {
		return AlertType.AlertParkingStatus; 
	}

}
