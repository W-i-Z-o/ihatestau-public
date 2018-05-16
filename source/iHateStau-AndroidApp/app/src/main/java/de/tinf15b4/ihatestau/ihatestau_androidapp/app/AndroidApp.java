package de.tinf15b4.ihatestau.ihatestau_androidapp.app;

import android.app.Application;

import de.tinf15b4.ihatestau.ihatestau_androidapp.tasks.CameraLoader;
import de.tinf15b4.ihatestau.ihatestau_androidapp.util.FileManager;
import de.tinf15b4.ihatestau.ihatestau_androidapp.util.SettingsManager;
import de.tinf15b4.ihatestau.ihatestau_androidapp.util.Talker;

public class AndroidApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FileManager.initFiles(getApplicationContext());
        Talker.init(getApplicationContext());
        SettingsManager.initSettings(getApplicationContext());

        CameraLoader synchronizer = new CameraLoader(getApplicationContext());
        synchronizer.execute();
    }
}
