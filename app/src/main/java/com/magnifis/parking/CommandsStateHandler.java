package com.magnifis.parking;

import static com.magnifis.parking.utils.Utils.isEmpty;

/**
 * 
 * @author chek
 * Return state of current active long command
 */
public class CommandsStateHandler {
	/**
	 * Get state of current active command
	 * @return
	 */
    public static String getState() {
    	if (FacebookStatusCommandHandler.isActiveCommand()) {
    		return FacebookStatusCommandHandler.getState();
    	}
    	return null;
    }
}
