package de.tinf15b4.ihatestau.ihatestau_androidapp.rest;

import android.util.Log;

import com.google.gson.internal.LinkedTreeMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.tinf15b4.ihatestau.persistence.CameraSpotConfig;
import de.tinf15b4.ihatestau.persistence.ExitSpotConfig;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitRestClient {

    private static final String TAG = RetrofitRestClient.class.getSimpleName();

    private static final String BASE_URL = "ENTER_UR_SERVER_URL_HERE";

    private static Retrofit CLIENT = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    private static CameraService cameraService = CLIENT.create(CameraService.class);
    private static TrafficService trafficService = CLIENT.create(TrafficService.class);
    private static ExitService exitService = CLIENT.create(ExitService.class);

    public static List<CameraSpotConfig> getCameras() throws IOException {
        return getResource(cameraService.getCameras());
    }

    public static Float getStateForSimpleCamera(String id) throws IOException {
        return getResource(trafficService.getStateForSimpleCamera(id));
    }

    public static LinkedTreeMap<String, Float> getStateBulk(Set<String> cameras) throws IOException {
        LinkedTreeMap<String, Float> trafficStates = getResource(trafficService.getStateBulk(cameras));
        return (trafficStates!=null) ? trafficStates : new LinkedTreeMap<>();
    }

    public static List<ExitSpotConfig> getExits() throws IOException {
        List<ExitSpotConfig> exits = getResource(exitService.getExits());
        return (exits != null) ? exits : new ArrayList<>();
    }

    public static LinkedTreeMap<String, byte[]> getFrontImageBulk(Set<String> cameras) throws IOException {
        LinkedTreeMap<String, byte[]> images = new LinkedTreeMap<>();
        Map<String, String> resource = getResource(trafficService.getFrontImageBulk(cameras));

        if(resource != null) {
            for(Map.Entry<String, String> entry : resource.entrySet()){
                images.put(entry.getKey(), android.util.Base64.decode(entry.getValue(), android.util.Base64.DEFAULT));
            }
        }

        return images;
    }

    private static <T> T getResource(Call<T> call) throws IOException {
            Log.v(TAG, call.request().toString());
            Response<T> response = call.execute();
            if (response.isSuccessful()){
                return response.body();
            } else {
                Log.v(TAG, response.errorBody().string());
                return null;
            }
    }


}
