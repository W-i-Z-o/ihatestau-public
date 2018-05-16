package de.tinf15b4.ihatestau.ihatestau_androidapp.util;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.tinf15b4.ihatestau.ihatestau_androidapp.domain.CameraGeofenceEntity;
import de.tinf15b4.ihatestau.persistence.CameraSpotConfig;
import de.tinf15b4.ihatestau.persistence.ExitSpotConfig;

//TODO: this class have to be refactored - We all know this is REALLY UGLY
public class FileManager {

    private final static String TAG = FileManager.class.getSimpleName();

    private final static String FILE_NAME_SELECTED_CAMERAS = "selectedCameras.data";
    private final static String FILE_NAME_ALL_CAMERAS = "allCameras.data";
    private final static String FILE_NAME_ALL_EXITS = "allExits.data";

    private static File directory;

    private static Map<String, CameraGeofenceEntity> selectedCameras = new LinkedHashMap<>();
    private static Map<String, CameraSpotConfig> allCameras = new HashMap<>();
    private static Map<String, ExitSpotConfig> allExits = new HashMap<>();

    public static void saveAllExits() {
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(directory + FILE_NAME_ALL_EXITS));
            out.writeObject(allExits);
            Log.v(TAG, "Exits: " + allExits.keySet());
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadAllExits() {

        allExits.clear();

        try {
            ObjectInput in = new ObjectInputStream(new FileInputStream(directory + FILE_NAME_ALL_EXITS));
            allExits.putAll((Map<String, ExitSpotConfig>) in.readObject());
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveAllCameras() {
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(directory + FILE_NAME_ALL_CAMERAS));
            out.writeObject(allCameras);
            Log.v(TAG, "allCameras: " + allCameras.keySet());
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadAllCameras() {

        allCameras.clear();

        try {
            ObjectInput in = new ObjectInputStream(new FileInputStream(directory + FILE_NAME_ALL_CAMERAS));
            allCameras.putAll((Map<String, CameraSpotConfig>) in.readObject());
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void saveSelectedCameras() {
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(directory + FILE_NAME_SELECTED_CAMERAS));
            out.writeObject(selectedCameras);
            Log.v(TAG, "selectedCameras: " + selectedCameras.keySet());
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static CameraSpotConfig getCamera(String id){
        return allCameras.get(id);
    }

    public static void loadSelectedCameras() {

        selectedCameras.clear();

        try {
            ObjectInput in = new ObjectInputStream(new FileInputStream(directory + FILE_NAME_SELECTED_CAMERAS));
            selectedCameras.putAll((Map<String, CameraGeofenceEntity>) in.readObject());
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void initFiles(Context context) {
        directory = new File(context.getFilesDir() + "/");

        String[] fileNames = new String[]{FILE_NAME_ALL_EXITS, FILE_NAME_ALL_CAMERAS, FILE_NAME_SELECTED_CAMERAS};

        for(String fileName : fileNames){
            File file = new File(directory + fileName);
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        loadSelectedCameras();
        loadAllCameras();
        loadAllExits();
    }

    public static Map<String, CameraGeofenceEntity> getSelectedCameraGeofenceEntites() {
        return selectedCameras;
    }

    public static List<CameraSpotConfig> getListOfSelectedCameras(){
        List<CameraSpotConfig> cameras = new ArrayList<>();
        for(CameraGeofenceEntity cge : selectedCameras.values()){
            cameras.add(cge.getCamera());
        }
        return  cameras;
    }

    public static CameraSpotConfig getSelectedCamera(String cameraId){
        return getSelectedCameraGeofenceEntity(cameraId).getCamera();
    }

    public static CameraGeofenceEntity getSelectedCameraGeofenceEntity(String cameraId) {
        return selectedCameras.get(cameraId);
    }

    public static void addSelectedCamera(CameraSpotConfig camera, LatLng latLng){
        selectedCameras.put(camera.getId(), new CameraGeofenceEntity(camera,latLng));
        saveSelectedCameras();
    }

    public static void removeSelectedCamera(CameraSpotConfig camera){
        removeSelectedCamera(camera.getId());
    }

    public static void removeSelectedCamera(String cameraId){
        selectedCameras.remove(cameraId);
        saveSelectedCameras();
    }

    public static boolean containsSelectedCamera(CameraSpotConfig camera){
        return containsSelectedCamera(camera.getId());
    }

    public static boolean containsSelectedCamera(String cameraId){
        return selectedCameras.get(cameraId) != null;
    }

    public static void reloadAllCameras(List<CameraSpotConfig> cameras) {
        allCameras.clear();
        for(CameraSpotConfig camera : cameras){
            allCameras.put(camera.getId(), camera);
        }
        saveAllCameras();
    }

    public static List<CameraSpotConfig> getListOfAllCameras(){
        return new ArrayList<>(allCameras.values());
    }

    public static Map<String, CameraSpotConfig> getAllCameras() {
        return allCameras;
    }

    public static ExitSpotConfig getExit(String id){
        return allExits.get(id);
    }

    public static Map<String, ExitSpotConfig> getAllExits() {
        return allExits;
    }

    public static List<ExitSpotConfig> getListOfAllExits(){
        return new ArrayList<>(allExits.values());
    }

    public static List<ExitSpotConfig> getAllExitsForCamera(CameraSpotConfig camera){
        List<ExitSpotConfig> exits = new ArrayList<>();
        for(String id : camera.getLastAlternatives()){
            exits.add(getExit(id));
        }
        return  exits;
    }

    public static void reloadAllExits(List<ExitSpotConfig> exits) {
        allExits.clear();
        for(ExitSpotConfig exit : exits){
            allExits.put(exit.getId(), exit);
        }
        saveAllExits();
    }

    public static void reloadSelectedCameras(List<CameraGeofenceEntity> cameraGeofenceEntities) {
        selectedCameras.clear();
        addSelectedCameras(cameraGeofenceEntities);
    }

    public static void addSelectedCameras(List<CameraGeofenceEntity> cameraGeofenceEntities){
        for(CameraGeofenceEntity cameraGeofenceEntity : cameraGeofenceEntities){
            selectedCameras.put(cameraGeofenceEntity.getCamera().getId(), cameraGeofenceEntity);
        }
        saveSelectedCameras();
    }
}
