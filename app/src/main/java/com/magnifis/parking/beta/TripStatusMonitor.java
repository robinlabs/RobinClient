package com.magnifis.parking.beta;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.util.Log;

import com.magnifis.parking.model.PkFacility;

public class TripStatusMonitor {
	
	static final String TAG = "VacancyMonitor"; 
	
	Timer timer = new Timer();
	static final int MOCK_PARKING_STATUS_CHANGE_TIMEOUT_MILLIS = 20000;
	static final int MOCK_TIMEOUT_TO_SPEED_TRAP_MILLIS = 10000; 
	static final int MOCK_TIMEOUT_TO_SEE_SPEED_TRAP_MILLIS = 10000; 

	PkFacility observable = null; 
	Notifiable alertListener = null; 
	
	static private TripStatusMonitor instance = null; 
	
	
	protected TripStatusMonitor(Notifiable alertListener) {
		this.alertListener = alertListener; 
	}
	
	public static TripStatusMonitor getInstance(Notifiable alertListener) {
		synchronized (TripStatusMonitor.class) {
			if (null == instance) {
				instance = new TripStatusMonitor(alertListener); 
			}
		}
		return instance;
	}
	
	public boolean setObservable(PkFacility facility) {
		
		if (facility.getOccupancy_pct() == null)
			return false; // nothing to observe
		
		this.observable = facility;
		
		// TODO: for demo only
		// set a timer task to alert about low occupancy
		timer.schedule(new TimerTask() {
			
			public void run() {
				Log.i(TAG, "Low vacancy alert activated!!"); 
				observable.setOccupancy_pct(92); // fake it
				alertListener.alertStatusChange(new ParkingStatusChangeAlert(observable)); 
			}
			
		}, MOCK_PARKING_STATUS_CHANGE_TIMEOUT_MILLIS);
		
		
		return true; 
	}

	public void setUpcomingSpeedTrap() {
		        // TODO: for demo only
				// set a timer task to alert about a speed trap
				timer.schedule(new TimerTask() {
					
					public void run() {
						Log.i(TAG, "Speed trap alert activated!!"); 
						
						alertListener.alertStatusChange(new SpeedTrapAlert()); 
					}
					
				}, MOCK_TIMEOUT_TO_SPEED_TRAP_MILLIS);
		
	}

	public void setUpcomingSpeedTrapPrompt() {
		
		        // TODO: for demo only
				// set a timer task to ask feedback about a speed trap
				timer.schedule(new TimerTask() {
					
					public void run() {
						Log.i(TAG, "Speed trap prompt alert activated!!"); 
						
						alertListener.alertStatusChange(new SpeedTrapPromptAlert()); 
					}
					
				}, MOCK_TIMEOUT_TO_SEE_SPEED_TRAP_MILLIS);
		
	} 

}
