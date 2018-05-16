package de.tinf15b4.ihatestau.ihatestau_androidapp.tasks;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

import de.tinf15b4.ihatestau.ihatestau_androidapp.R;
import de.tinf15b4.ihatestau.ihatestau_androidapp.rest.RetrofitRestClient;
import de.tinf15b4.ihatestau.ihatestau_androidapp.util.FileManager;
import de.tinf15b4.ihatestau.persistence.CameraSpotConfig;
import de.tinf15b4.ihatestau.persistence.ExitSpotConfig;

public class CameraLoader extends AsyncTask<Void, Integer, Void> {

    private static final String TAG = CameraLoader.class.getSimpleName();

    public static final String ACTION_SYNCHRONIZATION_SUCCESSFULL
            = "de.tinf15b4.ihatestau.action.ACTION_SYNCHRONIZATION_SUCCESSFULL";
    public static final String ACTION_SYNCHRONIZATION_FAILED
            = "de.tinf15b4.ihatestau.action.ACTION_SYNCHRONIZATION_FAILED";

    private List<CameraSpotConfig> cameras;
    private List<ExitSpotConfig> exits;
    private Context context;

    private boolean successfull = false;

    public CameraLoader(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            cameras = RetrofitRestClient.getCameras();
            exits = RetrofitRestClient.getExits();
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
            FileManager.reloadAllCameras(cameras);
            FileManager.reloadAllExits(exits);
            Toast.makeText(context, context.getString(R.string.camera_list_reload_completed), Toast.LENGTH_SHORT).show();
            intent = new Intent(ACTION_SYNCHRONIZATION_SUCCESSFULL);
        } else {
            Toast.makeText(context, R.string.rest_call_failed, Toast.LENGTH_SHORT).show();
            intent = new Intent(ACTION_SYNCHRONIZATION_FAILED);
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
