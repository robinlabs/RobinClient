package com.magnifis.parking.beta;

public interface DriverAlert {
	
	public enum AlertType { 	AlertParkingStatus, 
								AlertSpeedTrap, 
								AlertSpeedTrapPrompt, 
								AlertReachedDestination, 
								AlertTrafficConjestion, 
								AlertTrafficConjestionPrompt, 
								AlertFriendCheckin, 
								AlertAux, 
								AlertNone}; 
	
	AlertType getType(); 

}
