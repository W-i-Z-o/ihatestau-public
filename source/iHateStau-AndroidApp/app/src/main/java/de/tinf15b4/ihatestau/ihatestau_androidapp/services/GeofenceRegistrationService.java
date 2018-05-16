package de.tinf15b4.ihatestau.ihatestau_androidapp.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import de.tinf15b4.ihatestau.ihatestau_androidapp.rest.RetrofitRestClient;

public class GeofenceRegistrationService extends IntentService {

    private static final String TAG = GeofenceRegistrationService.class.getSimpleName();

    public static final String ACTION_NEW_CAMERA = "de.tinf15b4.ihatestau.action.ACTION_NEW_CAMERA";
    public static final String ACTION_NEW_NOTIFICATION = "de.tinf15b4.ihatestau.action.ACTION_NEW_NOTIFICATION";
    public static final String ACTION_NEW_EXIT = "de.tinf15b4.ihatestau.action.ACTION_NEW_EXIT";

    public static final String CAMERA_PREFIX = "camera_";
    public static final String CAMERA_ID = "cameraId";
    public static final String EXIT_PREFIX = "exit_";
    public static final String EXIT_ID = "exitId";
    public static final String NOTIFICATION_PREFIX = "notification_";
    public static final String NOTIFICATION_ID = "notificationId";
    public static final String JAM_PROBABILITY_ID = "jamProbabilityId";

    public GeofenceRegistrationService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Log.d(TAG, "GeofencingEvent error " + geofencingEvent.getErrorCode());
        } else {
            int transaction = geofencingEvent.getGeofenceTransition();
            List<Geofence> geofences = geofencingEvent.getTriggeringGeofences();

            Collections.sort(geofences, (o1, o2) -> {
                if (o1.getRequestId().contains(EXIT_PREFIX)) {
                    return -1;
                } else if (o2.getRequestId().contains(EXIT_PREFIX)) {
                    return 1;
                } else {
                    return 0;
                }
            });

            for (Geofence geofence : geofences) {
                switch (intent.getAction()) {
                    case GeofenceService.ACTION_TRAFFIC_EVENT:
                        handleTrafficEvent(transaction, geofence);
                        break;
                    case GeofenceService.ACTION_LEARNING_EVENT:
                        handleLearningEvent(transaction, geofence);
                }
            }
        }
    }

    private void handleTrafficEvent(int transaction, Geofence geofence) {
        if (transaction == Geofence.GEOFENCE_TRANSITION_ENTER) {
            String requestId = geofence.getRequestId();
            Log.d(TAG, "Entering: " + requestId);
            if (geofence.getRequestId().contains(NOTIFICATION_PREFIX)) {
                String notificationId = geofence.getRequestId().replace(NOTIFICATION_PREFIX, "");
                Float jamProbability;
                try {
                    jamProbability = RetrofitRestClient.getStateForSimpleCamera(notificationId);
                } catch (IOException e) {
                    jamProbability = null;
                }

                Intent intent = new Intent(ACTION_NEW_NOTIFICATION);
                intent.putExtra(NOTIFICATION_ID, notificationId);
                intent.putExtra(JAM_PROBABILITY_ID, jamProbability);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            } else if (requestId.contains(CAMERA_PREFIX)) {
                Intent intent = new Intent(ACTION_NEW_CAMERA);
                String cameraId = geofence.getRequestId().replace(CAMERA_PREFIX, "");
                intent.putExtra(CAMERA_ID, cameraId);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            } else if (requestId.contains(EXIT_PREFIX)) {
                Intent intent = new Intent(ACTION_NEW_EXIT);
                String exitId = geofence.getRequestId().replace(EXIT_PREFIX,"");
                intent.putExtra(EXIT_ID, exitId);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }
        }
    }

    private void handleLearningEvent(int transaction, Geofence geofence) {
        if (transaction == Geofence.GEOFENCE_TRANSITION_ENTER) {
            Log.d(TAG, "Entering: " + geofence.getRequestId());

            if (geofence.getRequestId().contains(CAMERA_PREFIX)) {
                Intent intent = new Intent(ACTION_NEW_CAMERA);
                String cameraId = geofence.getRequestId().replace(CAMERA_PREFIX, "");
                intent.putExtra(CAMERA_ID, cameraId);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            } else if (geofence.getRequestId().contains(EXIT_PREFIX)) {
                Intent intent = new Intent(ACTION_NEW_EXIT);
                String exitId = geofence.getRequestId().replace(EXIT_PREFIX, "");
                intent.putExtra(EXIT_ID, exitId);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }
        }
    }
}
