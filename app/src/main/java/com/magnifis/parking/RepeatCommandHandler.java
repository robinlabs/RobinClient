package com.magnifis.parking;


import com.magnifis.parking.model.Understanding;

public class RepeatCommandHandler {
	private static String TAG = RepeatCommandHandler.class.getSimpleName();
	private static Understanding lastCommandUnderstanding = null;
	private static Understanding currentCommandUnderstanding = null;
	
    /**
     * Handle understanding reply (for not UI use)
     * @param understanding
     * @return
     */    
    public static boolean handleReply(Understanding understanding) {
		boolean result = false;
		currentCommandUnderstanding = understanding;
		if (understanding.getCommandCode() != Understanding.CMD_REPEAT) {
			lastCommandUnderstanding = understanding;
		}		
		return result;
	}	
    

	/**
	 * Handle UI operations for understanding
	 * @return
	 */	
	public static boolean handleUI() {
		boolean result = false;
		if (currentCommandUnderstanding!=null&&currentCommandUnderstanding.getCommandCode() == Understanding.CMD_REPEAT) {
			if (lastCommandUnderstanding != null)
			switch (lastCommandUnderstanding.getCommandCode()) {
			case Understanding.CMD_SEARCH:
			case Understanding.CMD_PARKING:
			case Understanding.CMD_GAS:
			case Understanding.CMD_NEAR:
			case Understanding.CMD_NEAREST:
			case Understanding.CMD_CHEAP:
			case Understanding.CMD_CHEAPEST:
			case Understanding.CMD_GO_BACK:
			case Understanding.CMD_OTHER: 
			case Understanding.CMD_PREV: 
			case Understanding.CMD_MORE:
		    case Understanding.CMD_DETAILS:
				result = false;	
				break;
			default:
				lastCommandUnderstanding.getQueryInterpretation().sayAndShow(MainActivity.get());
				result = true;
				break;
			}						
		}		
		return result;
	}
}
