package de.tinf15b4.ihatestau.ihatestau_androidapp.rest;

import com.google.gson.internal.LinkedTreeMap;

import java.util.Set;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TrafficService {

    String BASE_ROUTE = "traffic/";
    String SINGLE_CAMERA_ROUTE = BASE_ROUTE + "camera/{id}";
    String BULK_ROUTE = BASE_ROUTE + "bulk";
    String BULK_FRONT_IMAGE_ROUTE = BASE_ROUTE + "bulk-front-image";

    @GET(SINGLE_CAMERA_ROUTE)
    public Call<Float> getStateForSimpleCamera(@Path("id") String id);

    @GET(BULK_ROUTE)
    public Call<LinkedTreeMap<String, Float>> getStateBulk(@Query("camera") Set<String> cameras);

    @GET(BULK_FRONT_IMAGE_ROUTE)
    public Call<LinkedTreeMap<String, String>> getFrontImageBulk(@Query("camera") Set<String> cameras);

}
