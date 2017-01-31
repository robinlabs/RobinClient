package com.magnifis.parking;


import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import static com.magnifis.parking.VoiceIO.*;


public class SamsungNavigationCommandHandler {
	private static String TAG = SamsungNavigationCommandHandler.class.getSimpleName();
	private static String name = null;
	private static stopCommandTask stopTask;
	private static final int secondsWaitToKillSendCommand=60000;

    public static boolean handle(Context ctx, ArrayList<String> matches) {
		boolean result = false;
    	String[] names = new String[4];
    	names[0] = "mark";
    	names[1] = "tyler";
    	names[2] = "andreas";
    	names[3] = "jon";
    	String[] places = new String[2];
    	places[0] = "restroom";
    	places[1] = "bathroom";
    	String[] goPhrases = new String[3];
    	goPhrases[0] = "to him";
    	goPhrases[1] = "to her";
    	goPhrases[2] = "there";
    	String[] rooms = new String[3];
    	rooms[0] = "310";
    	rooms[1] = "321";
    	rooms[2] = "330";
		for (String s : matches) {
			for (String n : names) {
				if (s.toLowerCase().indexOf(n) > -1) {
					//Launchers.launchSamsungNavigator(n, null);
	        		Random randomGenerator = new Random();
	        		int randomInt = randomGenerator.nextInt(rooms.length);
	        		String room = rooms[randomInt];
					runStopTask();
					name = n;
					if (name.equals(names[3])) {
						Output.sayAndShow(MainActivity.get(), name + " works at the Samsung San Jose Lab in cubicle 20");
					} else {
						Output.sayAndShow(MainActivity.get(), name + " is staying at room " + room + " in the Garden Court hotel, he is currently nearby, but not in his room");
					}
					listenAfterTheSpeech();						
					result = true;				
					break;
				}						
			}
			if (name != null && !result) {
				for (String go : goPhrases) {
					if (s.toLowerCase().indexOf(go) > -1) {
						Output.sayAndShow(MainActivity.get(), "Ok launching navigation to " + name + " location");
						Launchers.launchSamsungNavigator(ctx,name, null);
						emptyComand();
						result = true;
						break;
					}						
				}
			}
			if (!result) 
			for (String place : places) {
				if (s.toLowerCase().indexOf(place) > -1) {
					Output.sayAndShow(MainActivity.get(), "Ok launching navigation to the nearest " + place);
					Launchers.launchSamsungNavigator(ctx, null, place);
					result = true;
					break;
				}						
			}				
			if (result) {
				break;
			}
		}		
		return result;
	}
    
    private static void emptyComand(){
    	cancelStopTask();
    	VoiceIO.fireOpes();
    	name = null;
   		
    }    
	
    private static class stopCommandTask extends TimerTask {
		  public void run() {
			  emptyComand();
			  cancel();
		  }
	}
    
    private static void runStopTask() {
    	stopTask = new stopCommandTask();
		Timer timer = new Timer();
		timer.schedule(stopTask, secondsWaitToKillSendCommand, 3000);
	}
	
    private static void cancelStopTask() {
    	if (stopTask != null) {
    		stopTask.cancel();
		}
	}    
}
