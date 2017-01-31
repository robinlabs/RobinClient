package com.magnifis.parking.beta;

import com.magnifis.parking.beta.DriverAlert.AlertType;

public class FriendCheckinAlert implements DriverAlert {

	FriendCheckinAlert() {	
	}
	
	
	public AlertType getType() {
		return AlertType.AlertFriendCheckin; 
	}

}
