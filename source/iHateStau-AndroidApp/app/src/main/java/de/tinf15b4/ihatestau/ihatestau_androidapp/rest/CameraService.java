package de.tinf15b4.ihatestau.ihatestau_androidapp.rest;

import java.util.List;

import de.tinf15b4.ihatestau.persistence.CameraSpotConfig;
import retrofit2.Call;
import retrofit2.http.GET;

public interface CameraService {

    String ROUTE_CAMERAS = "spots";

    @GET(ROUTE_CAMERAS)
    Call<List<CameraSpotConfig>> getCameras();
}
