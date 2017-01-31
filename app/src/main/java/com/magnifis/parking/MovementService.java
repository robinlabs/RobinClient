package com.magnifis.parking;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

public class MovementService extends IntentService {
    public static final int DETECTION_INTERVAL_MILLISECONDS = 30000;


    public MovementService() {
        super("MovementService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (ActivityRecognitionResult.hasResult(intent)) {

            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            DetectedActivity mostProbableActivity = result.getMostProbableActivity();
            int confidence = mostProbableActivity.getConfidence();
            int activityType = mostProbableActivity.getType();

            handleNewUserActivity(activityType, confidence);

            String activityName = getNameFromType(activityType);
            Log.d("activityRecog", activityName);

        } else {

        }
    }

    private void handleNewUserActivity(int activityType, int confidence) {

        Robin robin = new Robin();
        if (activityType != DetectedActivity.IN_VEHICLE
                || confidence < 85) {

            if (!robin.isDebugMode()) {
                return;
            }
        }
        robin.showNotification();
    }

    private String getNameFromType(int activityType) {
        switch (activityType) {
            case DetectedActivity.IN_VEHICLE:
                return "in_vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "on_bicycle";
            case DetectedActivity.ON_FOOT:
                return "on_foot";
            case DetectedActivity.STILL:
                return "still";
            case DetectedActivity.UNKNOWN:
                return "unknown";
            case DetectedActivity.TILTING:
                return "tilting";
        }
        return "unknown";
    }

}
