package de.tinf15b4.ihatestau.ihatestau_androidapp.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

import de.tinf15b4.ihatestau.ihatestau_androidapp.R;
import de.tinf15b4.ihatestau.ihatestau_androidapp.domain.CameraGeofenceEntity;
import de.tinf15b4.ihatestau.ihatestau_androidapp.util.FileManager;
import de.tinf15b4.ihatestau.ihatestau_androidapp.util.SettingsManager;
import de.tinf15b4.ihatestau.ihatestau_androidapp.util.Talker;
import de.tinf15b4.ihatestau.persistence.CameraSpotConfig;

public class TrafficHandler {

    public static final String TAG = TrafficHandler.class.getSimpleName();

    private Context context;
    private BroadcastReceiver broadcastReceiver;

    private Set<String> visitedExits = new HashSet<>();
    private Set<String> visitedCameras = new HashSet<>();

    public TrafficHandler(Context context) {
        this.context = context;
        setUpBroadcastReceiver();
        registerBroadcastReceiver();
    }

    public void endTrafficHandling() {
        unregisterBroadcastReceiver();
    }

    private void registerBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter(GeofenceRegistrationService.ACTION_NEW_CAMERA);
        intentFilter.addAction(GeofenceRegistrationService.ACTION_NEW_NOTIFICATION);
        intentFilter.addAction(GeofenceRegistrationService.ACTION_NEW_EXIT);
        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver, intentFilter);
    }

    private void unregisterBroadcastReceiver() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver);
    }

    private void setUpBroadcastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case GeofenceRegistrationService.ACTION_NEW_CAMERA:
                        String cameraId = intent.getStringExtra(GeofenceRegistrationService.CAMERA_ID);
                        visitedCameras.add(cameraId);
                        break;
                    case GeofenceRegistrationService.ACTION_NEW_EXIT:
                        String exitId = intent.getStringExtra(GeofenceRegistrationService.EXIT_ID);
                        visitedExits.add(exitId);
                        break;
                    case GeofenceRegistrationService.ACTION_NEW_NOTIFICATION:
                        String notificationId = intent.getStringExtra(GeofenceRegistrationService.NOTIFICATION_ID);
                        Float jamProbability = intent.getFloatExtra(GeofenceRegistrationService.JAM_PROBABILITY_ID, -1);
                        handleNotification(notificationId, jamProbability);
                }
            }
        };
    }

    private void handleNotification(String notificationId, Float jamProbability) {
        CameraGeofenceEntity cameraGeofenceEntity = FileManager.getSelectedCameraGeofenceEntity(notificationId);
        if (cameraGeofenceEntity != null) {
            CameraSpotConfig camera = cameraGeofenceEntity.getCamera();

            if (visitedCameras.contains(notificationId) && !notficationInsideCameraGeofence(cameraGeofenceEntity)) {
                return;
            }

            CameraSpotConfig sisterCamera = FileManager.getCamera(camera.getSisterId());

            for (String alternative : sisterCamera.getLastAlternatives()) {
                if (visitedExits.contains(alternative)) {
                    return;
                }
            }

            String message = context.getString(R.string.jam_probability_notification_text_begin)
                    + camera.getName();
            if (jamProbability != -1) {
                int intPercentage = ((Float) (jamProbability * 100)).intValue();
                message += context.getString(R.string.jam_probability_notification_text_end)
                        + intPercentage
                        + context.getString(R.string.jam_probability_notification_percent);
            } else {
                message += " " + context.getString(R.string.jam_probability_not_available);
            }

            Talker.speak(message);
        }
    }

    private boolean notficationInsideCameraGeofence(CameraGeofenceEntity cameraGeofenceEntity) {
        float[] results = new float[1];
        Location.distanceBetween(
                cameraGeofenceEntity.getCamera().getCameraLat(),
                cameraGeofenceEntity.getCamera().getCameraLon(),
                cameraGeofenceEntity.getLatLng().latitude,
                cameraGeofenceEntity.getLatLng().longitude,
                results);
        Log.d(TAG, "Distance between camera " + cameraGeofenceEntity.getCamera().getName()
                + " and notification: " + results[0]);

        return (SettingsManager.getGeofenceRadiusInMeters() - results[0]) > 0;
    }
}
