package de.tinf15b4.ihatestau.ihatestau_androidapp.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import de.tinf15b4.ihatestau.ihatestau_androidapp.R;
import de.tinf15b4.ihatestau.ihatestau_androidapp.activities.OverviewActivity;
import de.tinf15b4.ihatestau.ihatestau_androidapp.domain.CameraGeofenceEntity;
import de.tinf15b4.ihatestau.ihatestau_androidapp.util.FileManager;
import de.tinf15b4.ihatestau.ihatestau_androidapp.util.SettingsManager;
import de.tinf15b4.ihatestau.persistence.CameraSpotConfig;
import de.tinf15b4.ihatestau.persistence.ExitSpotConfig;

public class GeofenceService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final String ACTION_UPDATE_MAP = "de.tinf15b4.ihatestau.action.ACTION_UPDATE_MAP";
    public static final String ACTION_STOP_SERVICE = "de.tinf15b4.ihatestau.action.ACTION_STOP_SERVICE";
    public static final String ACTION_SERVICE_STARTED = "de.tinf15b4.ihatestau.action.ACTION_SERVICE_STARTED";

    public static final String ACTION_START_TRAFFIC_SERVICE = "de.tinf15b4.ihatestau.action.ACTION_START_TRAFFIC_SERVICE";
    public static final String ACTION_TRAFFIC_EVENT = "de.tinf15b4.ihatestau.action.ACTION_TRAFFIC_EVENT";

    public static final String ACTION_START_LEARNING_SERVICE = "de.tinf15b4.ihatestau.action.ACTION_START_LEARNING_SERVICE";
    public static final String ACTION_LEARNING_EVENT = "de.tinf15b4.ihatestau.action.ACTION_LEARNING_EVENT";

    public static final String DELETE_EXISTING_CAMERAS_ID = "deleteExistingCamerasId";

    private final static String TAG = GeofenceService.class.getSimpleName();

    private final static int ONGOING_NOTIFICATION_ID = 11;

    private static boolean isRunning = false;
    private static boolean isLearning = false;

    private GeofencingClient geofencingClient;

    private GoogleApiClient googleApiClient;
    private PendingIntent pendingIntent;
    private GeofencingRequest geofencingRequest;
    private LearningHandler learningHandler;
    private TrafficHandler trafficHandler;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent.getAction().equals(ACTION_STOP_SERVICE)) {
            stopSelf();
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            return START_NOT_STICKY;
        }

        if (intent.getAction().equals(ACTION_START_LEARNING_SERVICE)) {
            isLearning = true;
            startForeground(getString(R.string.learning_service_activated));
            learningHandler = new LearningHandler(getApplicationContext(), intent.getBooleanExtra(DELETE_EXISTING_CAMERAS_ID, false));
        } else if (intent.getAction().equals(ACTION_START_TRAFFIC_SERVICE)) {
            isRunning = true;
            startForeground(getString(R.string.geofencing_service_activated));
            trafficHandler = new TrafficHandler(getApplicationContext());
        }

        startGoogleApiClient();

        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(ACTION_SERVICE_STARTED));

        return START_STICKY;
    }

    private void startGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();

        googleApiClient.connect();
    }

    @Override
    public void onDestroy() {
        geofencingClient.removeGeofences(pendingIntent);
        googleApiClient.disconnect();

        if (learningHandler != null) {
            learningHandler.saveLearnedRoute();
            learningHandler = null;
        }

        if (trafficHandler != null) {
            trafficHandler.endTrafficHandling();
            trafficHandler = null;
        }

        isRunning = false;
        isLearning = false;
        Log.v(TAG, "Google Api Client Disconnected");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startForeground(String serviceDescription) {
        Intent callbackIntent = new Intent(this, OverviewActivity.class);
        callbackIntent.setAction(Intent.ACTION_MAIN);
        callbackIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, callbackIntent, 0);

        Intent stopIntent = new Intent(this, GeofenceService.class);
        stopIntent.setAction(ACTION_STOP_SERVICE);
        PendingIntent pendingStopIntent =
                PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Action actionStop = new NotificationCompat.Action.Builder(
                0,
                getString(R.string.geofencing_service_stop),
                pendingStopIntent)
                .build();

        String channelId;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = createNotificationChannel();
        } else {
            channelId = TAG;
        }

        //TODO:setIcon
        Notification notification =
                new NotificationCompat.Builder(this, channelId)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(serviceDescription)
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setContentIntent(pendingIntent)
                        .addAction(actionStop)
                        .build();

        startForeground(ONGOING_NOTIFICATION_ID, notification);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel() {
        String channelId = TAG;
        String channelName = "My Example Service";
        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.createNotificationChannel(channel);

        return channelId;
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "Google Api Client Connected");
        startGeofencing();
        startLocationMonitor();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Google Connection Suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "Connection Failed:" + connectionResult.getErrorMessage());
    }

    private void startGeofencing() {
        Log.d(TAG, "Start geofencing monitoring call");
        pendingIntent = getGeofencePendingIntent();
        geofencingRequest = new GeofencingRequest.Builder()
                .setInitialTrigger(Geofence.GEOFENCE_TRANSITION_ENTER)
                .addGeofences(getGeofences())
                .build();

        Log.v(TAG, "Registered Geofences:");

        if (!googleApiClient.isConnected()) {
            Log.d(TAG, "Google API client not connected");
        } else {
            try {

                geofencingClient = LocationServices.getGeofencingClient(getApplicationContext());
                geofencingClient.addGeofences(geofencingRequest, getGeofencePendingIntent())
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Successfully Geofencing Connected");
                        })
                        .addOnFailureListener(aVoid ->{
                            Log.d(TAG, "Failed to add Geofencing");
                        });
                
            } catch (SecurityException e) {
                Log.d(TAG, e.getMessage());
            }
        }
    }

    private PendingIntent getGeofencePendingIntent() {
        if (pendingIntent != null) {
            return pendingIntent;
        }
        Intent intent = new Intent(this, GeofenceRegistrationService.class);
        if (isRunning) {
            intent.setAction(ACTION_TRAFFIC_EVENT);
        } else if (isLearning) {
            intent.setAction(ACTION_LEARNING_EVENT);
        }
        return PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
    }

    @NonNull
    private List<Geofence> getGeofences() {
        List<Geofence> geofences = new ArrayList<>();

        if (isRunning) {
            for (CameraGeofenceEntity cge : FileManager.getSelectedCameraGeofenceEntites().values()) {
                CameraSpotConfig camera = cge.getCamera();
                LatLng latLng = cge.getLatLng();
                geofences.add(new Geofence.Builder()
                        .setRequestId(GeofenceRegistrationService.NOTIFICATION_PREFIX + camera.getId())
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        .setCircularRegion(latLng.latitude, latLng.longitude, SettingsManager.getGeofenceRadiusInMeters())
                        .setNotificationResponsiveness(1000)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                        .build());

                geofences.add(new Geofence.Builder()
                        .setRequestId(GeofenceRegistrationService.CAMERA_PREFIX + camera.getId())
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        .setCircularRegion(camera.getCameraLat(), camera.getCameraLon(), SettingsManager.getGeofenceRadiusInMeters())
                        .setNotificationResponsiveness(1000)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                        .build());
            }
        } else if (isLearning) {
            for (CameraSpotConfig camera : FileManager.getAllCameras().values()) {
                geofences.add(new Geofence.Builder()
                        .setRequestId(GeofenceRegistrationService.CAMERA_PREFIX + camera.getId())
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        .setCircularRegion(camera.getCameraLat(), camera.getCameraLon(), SettingsManager.getGeofenceRadiusInMeters())
                        .setNotificationResponsiveness(1000)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                        .build());
            }
        }

        for (ExitSpotConfig exit : FileManager.getAllExits().values()) {
            geofences.add(new Geofence.Builder()
                    .setRequestId(GeofenceRegistrationService.EXIT_PREFIX + exit.getId())
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setCircularRegion(exit.getExitLat(), exit.getExitLon(), SettingsManager.getGeofenceRadiusInMeters())
                    .setNotificationResponsiveness(1000)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .build());
        }

        return geofences;
    }

    private void startLocationMonitor() {
        Log.d(TAG, "start location monitor");
        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(SettingsManager.getGeofenceIntervallInMilliseconds())
                .setFastestInterval(SettingsManager.getGeofenceIntervallInMilliseconds() / 2)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    notifyMap(location);
                    Log.d(TAG, "Location Change Lat Lng " + location.getLatitude() + " " + location.getLongitude());
                }
            });
        } catch (SecurityException e) {
            Log.d(TAG, e.getMessage());
        }

    }

    private void notifyMap(Location location) {
        Log.v(TAG, "notify Map");
        Intent intent = new Intent(ACTION_UPDATE_MAP);
        intent.putExtra("location", location);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    public static boolean isRunning() {
        return isRunning;
    }

    public static boolean isLearning() {
        return isLearning;
    }

}
