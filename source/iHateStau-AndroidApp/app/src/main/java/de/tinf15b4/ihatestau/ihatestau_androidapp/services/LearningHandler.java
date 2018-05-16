package de.tinf15b4.ihatestau.ihatestau_androidapp.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.tinf15b4.ihatestau.ihatestau_androidapp.R;
import de.tinf15b4.ihatestau.ihatestau_androidapp.domain.CameraGeofenceEntity;
import de.tinf15b4.ihatestau.ihatestau_androidapp.util.FileManager;
import de.tinf15b4.ihatestau.persistence.CameraSpotConfig;
import de.tinf15b4.ihatestau.persistence.ExitSpotConfig;

public class LearningHandler {

    public static final String TAG = LearningHandler.class.getSimpleName();

    public static final String ACTION_LEARNING_FINISHED = "de.tinf15b4.ihatestau.action.ACTION_LEARNING_FINISHED";

    public static final String INFO_TITLE_ID = "title";
    public static final String INFO_TEXT_ID = "text";

    private Context context;
    private BroadcastReceiver broadcastReceiver;

    private boolean deleteExistingCameras = false;
    private boolean cameraMatchFound = false;
    private Set<String> visitedExits = new HashSet<>();
    private Set<String> unmatchedCameras = new HashSet<>();
    private Map<String, String> relevantCameras = new LinkedHashMap<>();

    public LearningHandler(Context context, boolean deleteExistingCameras) {
        this.context = context;
        this.deleteExistingCameras = deleteExistingCameras;
        setUpBroadcastReceiver();
        registerBroadcastReceiver();
    }

    public LearningHandler() {
    }

    public void saveLearnedRoute() {
        unregisterBroadcastReceiver();

        String message = context.getString(R.string.learning_mode_summary_text_start);
        String title = context.getString(R.string.learning_mode_summary_title);
        List<CameraGeofenceEntity> cameraGeofenceEntities = new ArrayList<>();

        for (Map.Entry<String, String> entry : relevantCameras.entrySet()) {
            if(deleteExistingCameras || !FileManager.containsSelectedCamera(entry.getKey())) {
                CameraSpotConfig camera = FileManager.getCamera(entry.getKey());
                message += camera.getName() + ":\n";
                LatLng notifationPoint;
                if (entry.getValue() != null) {
                    ExitSpotConfig exit = FileManager.getExit(entry.getValue());
                    message += context.getString(R.string.notificationpoint) + ": Ausfahrt " + exit.getName();
                    notifationPoint = new LatLng(exit.getExitLat(), exit.getExitLon());
                } else {
                    message += context.getString(R.string.no_notificationpoint);
                    notifationPoint = new LatLng(camera.getCameraLat(), camera.getCameraLon());
                }
                cameraGeofenceEntities.add(new CameraGeofenceEntity(camera, notifationPoint));

                message += "\n\n";
            }
        }

        if(!cameraGeofenceEntities.isEmpty()){
            if(deleteExistingCameras){
                FileManager.reloadSelectedCameras(cameraGeofenceEntities);
            } else {
                FileManager.addSelectedCameras(cameraGeofenceEntities);
            }

            message += context.getString(R.string.learning_mode_summary_text_end);
        } else {
            message = context.getString(R.string.no_new_cameras_found);
        }

        Intent intent = new Intent(ACTION_LEARNING_FINISHED);
        intent.putExtra(INFO_TITLE_ID, title);
        intent.putExtra(INFO_TEXT_ID, message);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private void registerBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter(GeofenceRegistrationService.ACTION_NEW_CAMERA);
        intentFilter.addAction(GeofenceRegistrationService.ACTION_NEW_EXIT);
        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver, intentFilter);
    }

    private void unregisterBroadcastReceiver() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver);
    }

    public void addCamera(String cameraId) {
        CameraSpotConfig camera = FileManager.getCamera(cameraId);

        for (String lastAlternaltive : camera.getLastAlternatives()) {
            if (visitedExits.contains(lastAlternaltive)) {
                relevantCameras.put(cameraId, lastAlternaltive);

                if(camera.getSisterId() != null){
                    String sisterId = camera.getSisterId();

                    if(relevantCameras.containsKey(sisterId) && relevantCameras.get(sisterId) == null){
                        relevantCameras.remove(sisterId);
                    }
                }

                cameraMatchFound = true;
                return;
            }
        }

        if (!cameraMatchFound) {
            relevantCameras.put(cameraId, null);
            unmatchedCameras.add(cameraId);
        }
    }

    public void addExit(String exitId) {
        for (String cameraId : unmatchedCameras) {

            CameraSpotConfig camera = FileManager.getCamera(cameraId);
            if (camera.getLastAlternatives().contains(exitId)) {
                relevantCameras.remove(cameraId);
            }
        }
        visitedExits.add(exitId);
    }

    private void setUpBroadcastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case GeofenceRegistrationService.ACTION_NEW_CAMERA:
                        String cameraId = intent.getStringExtra(GeofenceRegistrationService.CAMERA_ID);
                        addCamera(cameraId);
                        break;
                    case GeofenceRegistrationService.ACTION_NEW_EXIT:
                        String exitId = intent.getStringExtra(GeofenceRegistrationService.EXIT_ID);
                        addExit(exitId);
                }

                Log.v(TAG, "Cameras: " + relevantCameras.keySet());
                Log.v(TAG, "Exits: " + visitedExits);
            }
        };
    }

    public Map<String, String> getRelevantCameras() {
        return relevantCameras;
    }
}
