package de.tinf15b4.ihatestau.ihatestau_androidapp.domain;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import de.tinf15b4.ihatestau.persistence.CameraSpotConfig;

public class CameraGeofenceEntity implements Serializable{

    private CameraSpotConfig camera;
    // mark it transient so defaultReadObject()/defaultWriteObject() ignore it
    private transient LatLng latLng;

    public CameraGeofenceEntity(CameraSpotConfig camera, LatLng latLng) {
        this.camera = camera;
        this.latLng = latLng;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public CameraSpotConfig getCamera() {
        return camera;
    }

    // needed to make LatLng Serializable
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeDouble(latLng.latitude);
        out.writeDouble(latLng.longitude);
    }

    // needed to make LatLng Serializable
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        latLng = new LatLng(in.readDouble(), in.readDouble());
    }
}
