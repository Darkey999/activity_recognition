package com.example.gosu.activityrecognition;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {

    private ActivityDetectionBroadcastReceiver mBroadcastReceiver;
    private GoogleApiClient mGoogleApiClient;
    private TextView detectedActivitiesTxt;
    private Button requestActivityUpdatesButton;
    private Button removeActivityUpdatesButton;
    private final long UPDATE_TIME = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        detectedActivitiesTxt = (TextView) findViewById(R.id.detectedActivities);
        requestActivityUpdatesButton = (Button) findViewById(R.id.requestActivityUpdatesButton);
        removeActivityUpdatesButton = (Button) findViewById(R.id.removeActivityUpdatesButton);
        mBroadcastReceiver = new ActivityDetectionBroadcastReceiver();

        buildGoogleApiClient();
    }

    // Build GoogleApiClient for ActivityRecognition
    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(ActivityRecognition.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register the receiver when app is resumed
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver,
                new IntentFilter(Constants.BROADCAST_ACTION));
    }

    @Override
    protected void onPause() {
        // Unregister the receiver when app is paused
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        super.onPause();
    }

    // Connect with the API
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i("Connected with client", "");
    }

    // Connection with the API suspended
    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    // Connection with the API failed
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("Connection failed ", connectionResult.getErrorMessage());
    }

    // Click Remove Activity Update Button
    public void removeActivityUpdatesButtonHandler(View view) {
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(MainActivity.this, R.string.client_not_connected, Toast.LENGTH_SHORT).show();
        }

        Toast.makeText(MainActivity.this, R.string.updates_removed, Toast.LENGTH_SHORT).show();
        // Remove activity updates
        ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mGoogleApiClient,
                createPendingIntent()).setResultCallback(this);

        // Handle Buttons behaviour
        requestActivityUpdatesButton.setEnabled(true);
        removeActivityUpdatesButton.setEnabled(false);
    }

    // Click Request Activity Update Button
    public void requestActivityUpdatesButtonHandler(View view) {
        Toast.makeText(MainActivity.this, R.string.updates_requested, Toast.LENGTH_SHORT).show();

        // Request update every 3 seconds
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mGoogleApiClient,
                UPDATE_TIME, createPendingIntent()).setResultCallback(this);

        // Handle Buttons behaviour
        requestActivityUpdatesButton.setEnabled(false);
        removeActivityUpdatesButton.setEnabled(true);
    }

    // Create PendingIntent
    public PendingIntent createPendingIntent() {
        // Create new Intent to DetectedActivitiesIntentService
        Intent intent = new Intent(this, DetectedActivitiesIntentService.class);

        // Return new PendingIntent
        return PendingIntent.getService(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onResult(@NonNull Status status) {
        if (status.isSuccess()) {
            Log.i("Successfully added det.", "");

        } else {
            Log.e("Error adding det.", "");
        }
    }


    // Receive the broadcast
    public class ActivityDetectionBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<DetectedActivity> detectedActivities = intent.getParcelableArrayListExtra
                    (Constants.ACTIVITY_EXTRA);
            String strStatus = "";
            // Get activity type and confidence, then add to StringBuilder
            for (DetectedActivity detectedActivity : detectedActivities) {
                strStatus += getActivityString(detectedActivity.getType()) + " " +
                        detectedActivity.getConfidence() + "%\n";
            }

            // Set text to detectedActivitiesTxt TextView
            detectedActivitiesTxt.setText(strStatus);
        }
    }

    // Get activity name
    public String getActivityString(int detectedActivityType) {
        Resources resources = this.getResources();
        switch (detectedActivityType) {
            case DetectedActivity.IN_VEHICLE:
                return resources.getString(R.string.in_vehicle);
            case DetectedActivity.ON_BICYCLE:
                return resources.getString(R.string.on_bicycle);
            case DetectedActivity.ON_FOOT:
                return resources.getString(R.string.on_foot);
            case DetectedActivity.RUNNING:
                return resources.getString(R.string.running);
            case DetectedActivity.STILL:
                return resources.getString(R.string.still);
            case DetectedActivity.TILTING:
                return resources.getString(R.string.tilting);
            case DetectedActivity.UNKNOWN:
                return resources.getString(R.string.unknown);
            case DetectedActivity.WALKING:
                return resources.getString(R.string.walking);
            default:
                return resources.getString(R.string.unidentifiable_activity, detectedActivityType);
        }
    }
}
