package com.magnifis.parking.beta;

import com.magnifis.parking.beta.DriverAlert.AlertType;

public class SpeedTrapAlert  implements DriverAlert {
	
	SpeedTrapAlert() {}
	
	public AlertType getType() {
		return AlertType.AlertSpeedTrap; 
	}

}
