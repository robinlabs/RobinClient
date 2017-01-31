package com.magnifis.parking;


import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.magnifis.parking.utils.Utils;

public class ProximityWakeUp implements SensorEventListener {
	
	static final String TAG=ProximityWakeUp.class.getSimpleName();

	final static String 
	        CAPTURE_TASK="CAPTURE_TASK",
	        RESET_ICON="RESET_ICON", 
			DISABLE_HANDWAVING="DISABLE_HANDWAVING",
			ENABLE_HANDWAVING="ENABLE_HANDWAVING",
			TIMEOUT="TIMEOUT";
	
	private static final float EPS = 0.0000000001f;
	
	static private PowerStatusReceiver powerStatusReceiver = null;
    static private  SensorManager mSensorManager=null;
    static private  Sensor mSensor=null;
    static private boolean handwavingDisabled = false;
    static private ProximityWakeUp self = null;

    static public void start() {
        Log.d(TAG,"start");

        if (self != null)
            return;

        reset();

        mSensorManager = (SensorManager)App.self.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        //mSensorManager.getSensorList(SensorManager.SENSOR_ALL);

        self = new ProximityWakeUp();
        if (mSensor!=null) mSensorManager.registerListener(self, mSensor, SensorManager.SENSOR_DELAY_NORMAL);

        // observe power source state
        powerStatusReceiver = new PowerStatusReceiver();
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        App.self.registerReceiver(powerStatusReceiver, filter);
	};
	
	static public void stop() {
        if (self == null)
            return;

        Log.d(TAG,"stop");
        mSensorManager.unregisterListener(self);
        if (powerStatusReceiver != null)
            App.self.unregisterReceiver(powerStatusReceiver);

        self = null;
	};
	
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}
	
    static float sensorSave[] = new float[3];
    static long sensorTime[] = new long[3];

	public void onSensorChanged(SensorEvent event) {
		Log.d(TAG, "sensor: "+Utils.dump(event.values)+" history: "+Utils.dump(sensorSave));

		if (event.sensor.getType() != Sensor.TYPE_PROXIMITY)
            return;
        if (event.values.length < 1)
            return;

        sensorSave[2] = sensorSave[1];
        sensorSave[1] = sensorSave[0];
        sensorSave[0] = event.values[0];

        sensorTime[2] = sensorTime[1];
        sensorTime[1] = sensorTime[0];
        sensorTime[0] = System.nanoTime();

        if (sensorSave[2] > sensorSave[1] || sensorSave[1] < sensorSave[0]) {
            Log.d(TAG, "Event ignored (waiting CLOSE-FAR-CLOSE)");
            return;
        }

        long diff = Math.round((sensorTime[0] - sensorTime[2])/1000000);
        Log.d(TAG, "time elapsed between events " + Math.round(diff));
        if (diff < 250 || diff > 700) {
            Log.d(TAG, "Event ignored by time");
            return;
        }

        if (handwavingDisabled || !App.self.shouldUseProximitySensor())
            return;

        App.activateApp();
	}
	
	public static void disableHandWaving() {
        handwavingDisabled = true;
	}
	
	public static void enableHandWaving() {
        handwavingDisabled = false;
	}
	
    public static void reset() {
        sensorSave[2] = 0;
        sensorSave[1] = 0;
        sensorSave[0] = 0;

        sensorTime[2] = 0;
        sensorTime[1] = 0;
        sensorTime[0] = 0;
    }
}
