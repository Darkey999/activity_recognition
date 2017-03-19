package com.example.gosu.activityrecognition;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private TextView detectedActivities;
    private Button requestActivityUpdatesButton;
    private Button removeActivityUpdatesButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        detectedActivities = (TextView) findViewById(R.id.detectedActivities);
        requestActivityUpdatesButton = (Button) findViewById(R.id.requestActivityUpdatesButton);
        removeActivityUpdatesButton = (Button) findViewById(R.id.removeActivityUpdatesButton);
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

    // Connect with the API
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Create new Intent to DetectedActivitiesIntentService
        Intent intent = new Intent(this, DetectedActivitiesIntentService.class);

        //Create new PendingIntent
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Request update every 3 seconds
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mGoogleApiClient,
                3000, pendingIntent);

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
    }

    // Click Request Activity Update Button
    public void requestActivityUpdatesButtonHandler(View view) {
    }

    static class ActivityDetectionBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }
}
