package de.tinf15b4.ihatestau.ihatestau_androidapp.util;

import android.content.Context;
import android.content.SharedPreferences;

import de.tinf15b4.ihatestau.ihatestau_androidapp.R;

public class SettingsManager {

    private static Context context;
    private static SharedPreferences sharedPreferences;

    public static void initSettings(Context context) {
        SettingsManager.context = context;
        sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static float getGeofenceRadiusInMeters(){
        String geofenceRadius = sharedPreferences.getString(context.getString(R.string.preference_geofence_radius_in_meters_key),context.getString(R.string.preference_geofence_radius_in_meters_default));
        return Float.parseFloat(geofenceRadius);
    }

    public static long getGeofenceIntervallInMilliseconds(){
        String geofenceIntervall = sharedPreferences.getString(context.getString(R.string.preference_geofence_intervall_in_milliseconds_key),context.getString(R.string.preference_geofence_intervall_in_milliseconds_default));
        return Long.parseLong(geofenceIntervall);
    }

    public static boolean getFancyColorMode() {
        return sharedPreferences.getBoolean(context.getString(R.string.preference_fancy_color_mode_key),
                Boolean.getBoolean(context.getString(R.string.preference_fancy_color_mode_default)));
    }
}
