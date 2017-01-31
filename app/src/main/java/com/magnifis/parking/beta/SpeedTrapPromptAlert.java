package com.magnifis.parking.beta;

import com.magnifis.parking.beta.DriverAlert.AlertType;

/**
 * Alert user to report or confirm a speed trap
 * @author Ilya
 */
public class SpeedTrapPromptAlert implements DriverAlert {
	
	public SpeedTrapPromptAlert() {	
	}
	
	public AlertType getType() {
		return AlertType.AlertSpeedTrapPrompt; 
	}
}