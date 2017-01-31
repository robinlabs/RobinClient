package com.magnifis.parking;

import static com.magnifis.parking.tts.MyTTS.speakText;
import static com.magnifis.parking.utils.Utils.isEmpty;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.util.Log;

import com.magnifis.parking.model.LearnAttribute;
import com.magnifis.parking.model.Understanding;
import com.magnifis.parking.VoiceIO.*;

public class FacebookStatusCommandHandler {
	private static String TAG = FacebookStatusCommandHandler.class.getSimpleName();
	private static Understanding fbStatusCommandUnderstanding = null;
	private static Understanding currentCommandUnderstanding = null;
    private static int dictationCounter=0;
	private static int state = -1;
	public final static int DICTATE_MSG = 1;
	public final static int YES_NO = 2;
	private static StopSendingCommandTask stopSendingTask;
	private static final int secondsWaitToKillSendCommand=180000;
	private static int dictationMaxCount = 2;

	/**
	 * Get state string for understanding server request
	 * @return
	 */
    public static String getState() {
    	String result = null;
    	switch (state) {
		case DICTATE_MSG:
			result = "dictate_message";
			break;
		case YES_NO:
			result = "yes_no";
			break;
		default:
			break;
		}
    	return result;
    }
    
    /**
     * Set current command state
     * @param newState
     */
    private static void setState(int newState) {
    	switch (newState) {
		case DICTATE_MSG:
			state = DICTATE_MSG;
			incDictationCounter();
			break;
		case YES_NO:
			state = YES_NO;
			break;
		default:
			break;
		}
		if (getDictationCounter() > dictationMaxCount)
			emptySendingComand();		    	
    }
    
    /**
     * Handle understanding reply (for not UI use)
     * @param understanding
     * @return
     */
    public static boolean handleReply(Understanding understanding) {
		boolean result = false;
		if (understanding.getCommandCode() == Understanding.CMD_FACEBOOK_STATUS) {
			if (isActiveCommand()) {
				emptySendingComand();
			}
			runStopSendingTask();
			fbStatusCommandUnderstanding = understanding;
			result = true;
		} else {
			currentCommandUnderstanding = understanding;
			result = handleCurrentCommandReply();			
		}		
		return result;
	}
	
    /**
     * Handle secondaries understandings (for not UI use)
     * @return
     */
	private static boolean handleCurrentCommandReply() {
		boolean result = false;
		if (currentCommandUnderstanding.getCommandCode() == Understanding.CMD_DO_IT || currentCommandUnderstanding.getCommandCode() == Understanding.CMD_YES) {
			result = true;
		} else if (currentCommandUnderstanding.getCommandCode() == Understanding.CMD_DICTATE) {
			result = true;
		} else if (currentCommandUnderstanding.getCommandCode() == Understanding.CMD_NO) {
			result = true;
		} else {
			clearState();
		}
		return result;
	}
	
	/**
	 * Handle UI operations for understanding
	 * @return
	 */
	public static boolean handleUI() {
		boolean result = false;		
		result = handleCurrentCommandUi();
		if (!result) {
			result = handleFBStatusCommandUi();
		}
		return result;
	}
	
	/**
	 * Saving birthday from FB in props
	 */
	private static void getBirthDayFromFBSilently() {
		if (MainActivity.get().fbHelper.facebook.isSessionValid()) {
			MainActivity.get().fbHelper.getUserBirthday(new SuccessFailure<Date>() {
				@Override
				public void onSuccess(final Date d) {
					Log.d(TAG, "hd=" + d);
					LearnAttribute la = new LearnAttribute(R.string.PfBirthday);
					la.learnDate(d);
				}

				@Override
				public void onCancel() {
				}

				@Override
				public void onFailure() {
				}

			});
		}
	}				
	
	/**
	 * Handle CMD_FACEBOOK_STATUS understanding (for UI using)
	 * @return
	 */
	private static boolean handleFBStatusCommandUi() {
		boolean result = false;
		Output.sayAndShow(MainActivity.get(), fbStatusCommandUnderstanding.getQueryInterpretation().getToShow(), fbStatusCommandUnderstanding.getQueryInterpretation().getToSay(), fbStatusCommandUnderstanding.getQuery(), false);
		if (!fbStatusCommandUnderstanding.isConfirmationRequired() && !isEmpty(fbStatusCommandUnderstanding.getMessage())) {
			result = true;
			setFBStatus();
		} else if (isEmpty(fbStatusCommandUnderstanding.getMessage())) {
			result = true;
			askForMessage(false);
		} else if (fbStatusCommandUnderstanding.isConfirmationRequired() && !isEmpty(fbStatusCommandUnderstanding.getMessage())) {
			result = true;
			askForConfirmation();
		}
		return result;
	}

	private static void askForConfirmation() {
		setState(YES_NO);
		speakText(R.string.P_YOU_SAID);
		speakText(fbStatusCommandUnderstanding.getMessage());
		speakText(R.string.P_is_that_right);
		VoiceIO.listenAfterTheSpeech();
	}
	
	private static void askForMessage(boolean secondTime) {
		setState(DICTATE_MSG);
		if (!secondTime) {
			speakText(R.string.P_SAY_YOUR_MSG);
		} else {
			speakText(R.string.P_SAY_YOUR_MSG_AGAIN);
		}
		VoiceIO.listenAfterTheSpeech();
	}	
	
	private static void setFBStatus() {
		MainActivity.get().fbHelper.setStatus(fbStatusCommandUnderstanding.getMessage());
		getBirthDayFromFBSilently();
		emptySendingComand();
	}
	
	/**
	 * Handle secondary understandings (for UI using)
	 * @return
	 */
	private static boolean handleCurrentCommandUi() {
		boolean result = false;
		if (currentCommandUnderstanding != null) {
			if (currentCommandUnderstanding.getCommandCode() == Understanding.CMD_NO) {				
				stopSendingStatusAndStartEditing();
				result = true;
			} else if (currentCommandUnderstanding.getCommandCode() == Understanding.CMD_YES || currentCommandUnderstanding.getCommandCode() == Understanding.CMD_DO_IT) {
				fbStatusCommandUnderstanding.setConfirmationRequired(false);
				setFBStatus();
				result = true;
			} else if (currentCommandUnderstanding.getCommandCode() == Understanding.CMD_DICTATE) {
				fbStatusCommandUnderstanding.setMessage(currentCommandUnderstanding.getMessage());
				askForConfirmation();
				result = true;
			}
		}
		currentCommandUnderstanding = null;
		return result;
	}
	
	/**
	 * Is command active now
	 * @return
	 */
	public static boolean isActiveCommand() {
		boolean result = false;
		if (fbStatusCommandUnderstanding != null) {
			result = true;
		}
		return result;
	}

	private static void stopSendingStatusAndStartEditing() {
		
		//speakText(currentCommandUnderstanding.getQueryInterpretation().getToSay());
		if (getDictationCounter() < dictationMaxCount) {
			fbStatusCommandUnderstanding.setMessage(null);
			askForMessage(true);
		} else {
			speakText(currentCommandUnderstanding.getQueryInterpretation().getToSay());
			
			MainActivity.get().fbHelper.postToWall();
			emptySendingComand();
		}
	}
	
    private static void incDictationCounter(){
    	dictationCounter++;
    }
    
    private static int getDictationCounter(){
    	return dictationCounter;
    }        
    
    private static void emptyDictationCounter(){
    	dictationCounter = 0;
    }    
    
    private static void emptySendingComand(){
    	clearState();
    	VoiceIO.fireOpes();
    }    
    
    private static void clearState() {
    	fbStatusCommandUnderstanding= null;
   		emptyDictationCounter();
   		state = -1;
    } 
	
    private static class StopSendingCommandTask extends TimerTask {
    	  @Override
		  public void run() {
				 MainActivity ma=MainActivity.get();
				 if (ma != null) ma.runOnUiThread(
				   new Runnable() {
					@Override
					public void run() {
					  if (state!=-1) {
						 emptySendingComand();
					     cancel();
					  }
					}   
				   }
				 );
		  }
	}
    
    private static void runStopSendingTask() {
    	stopSendingTask = new StopSendingCommandTask();
		Timer timer = new Timer();
		timer.schedule(stopSendingTask, secondsWaitToKillSendCommand, 3000);
	}
	
    private static void cancelStopSendingTask() {
    	if (stopSendingTask != null) {
        	stopSendingTask.cancel();
		}
	}    	
}
