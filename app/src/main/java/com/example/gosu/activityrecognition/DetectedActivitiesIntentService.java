package com.example.gosu.activityrecognition;


import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import java.util.ArrayList;

public class DetectedActivitiesIntentService extends IntentService {

    private static final String TAG = "detection_is";

    public DetectedActivitiesIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Intent localIntent = new Intent(Constants.BROADCAST_ACTION);

        //Check if the intent contains data
        if (ActivityRecognitionResult.hasResult(intent)) {
            // Get results
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            // Get PropableActivities into ArrayList
            ArrayList<DetectedActivity> detectedActivities =
                    (ArrayList<DetectedActivity>) result.getProbableActivities();

            // Put ArrayList into Intent and send Broadcast
            localIntent.putExtra(Constants.ACTIVITY_EXTRA, detectedActivities);
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        }

    }

}
