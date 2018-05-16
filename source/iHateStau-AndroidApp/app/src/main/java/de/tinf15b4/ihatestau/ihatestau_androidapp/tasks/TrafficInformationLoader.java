package de.tinf15b4.ihatestau.ihatestau_androidapp.tasks;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.google.gson.internal.LinkedTreeMap;

import java.io.IOException;
import java.util.Set;

import de.tinf15b4.ihatestau.ihatestau_androidapp.R;
import de.tinf15b4.ihatestau.ihatestau_androidapp.rest.RetrofitRestClient;
import de.tinf15b4.ihatestau.ihatestau_androidapp.util.FileManager;

public class TrafficInformationLoader extends AsyncTask<Void, Void, Void> {

    private static final String TAG = TrafficInformationLoader.class.getSimpleName();

    public static final String ACTION_SYNCHRONIZATION_SUCCESSFULL
            = "de.tinf15b4.ihatestau.action.ACTION_SYNCHRONIZATION_SUCCESSFULL";
    public static final String ACTION_SYNCHRONIZATION_FAILED
            = "de.tinf15b4.ihatestau.action.ACTION_SYNCHRONIZATION_FAILED";
    public static final String INTENT_PARAM_TRAFFIC_STATES = "TRAFFIC_STATES";
    public static final String INTENT_PARAM_CAMERA_IMAGES = "CAMER_IMAGES";

    private Context context;

    private LinkedTreeMap<String, Float> trafficStates;
    private LinkedTreeMap<String, byte[]> cameraImages;

    private boolean successfull = false;

    public TrafficInformationLoader(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Set<String> camerasIds = FileManager.getSelectedCameraGeofenceEntites().keySet();
        try {
            trafficStates = RetrofitRestClient.getStateBulk(camerasIds);
            cameraImages = RetrofitRestClient.getFrontImageBulk(camerasIds);
            successfull = true;
        } catch (IOException e) {
            e.printStackTrace();
            successfull = false;
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        Intent intent;
        if(successfull){
            intent = new Intent(ACTION_SYNCHRONIZATION_SUCCESSFULL);
            intent.putExtra(INTENT_PARAM_TRAFFIC_STATES, trafficStates);
            intent.putExtra(INTENT_PARAM_CAMERA_IMAGES, cameraImages);
        } else {
            Toast.makeText(context, R.string.rest_call_failed, Toast.LENGTH_SHORT).show();
            intent = new Intent(ACTION_SYNCHRONIZATION_FAILED);
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
