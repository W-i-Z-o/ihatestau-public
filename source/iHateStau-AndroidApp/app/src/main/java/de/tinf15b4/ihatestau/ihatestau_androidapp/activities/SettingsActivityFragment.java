package de.tinf15b4.ihatestau.ihatestau_androidapp.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import de.tinf15b4.ihatestau.ihatestau_androidapp.R;

public class SettingsActivityFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preference);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        Preference geofenceRadiusPref = findPreference(getString(R.string.preference_geofence_radius_in_meters_key));
        geofenceRadiusPref.setOnPreferenceChangeListener(this);
        // initialize preference with default value
        String defaultGeofenceRadius = sharedPrefs.getString(geofenceRadiusPref.getKey(), "");
        onPreferenceChange(geofenceRadiusPref, defaultGeofenceRadius);

        Preference geofenceIntervallPref = findPreference(getString(R.string.preference_geofence_intervall_in_milliseconds_key));
        geofenceIntervallPref.setOnPreferenceChangeListener(this);
        // initialize preference with default value
        String defaultGeofenceIntervall = sharedPrefs.getString(geofenceIntervallPref.getKey(), "");
        onPreferenceChange(geofenceIntervallPref, defaultGeofenceIntervall);

        Preference fancyColorModePref = findPreference(getString(R.string.preference_fancy_color_mode_key));
        fancyColorModePref.setOnPreferenceChangeListener(this);
        boolean defaulFancyColorMode = sharedPrefs.getBoolean(getString(R.string.preference_fancy_color_mode_key), false);
        onPreferenceChange(fancyColorModePref, defaulFancyColorMode);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        if (preference.getKey().equals(getString(R.string.preference_geofence_intervall_in_milliseconds_key))) {
            int min = Integer.parseInt(getString(R.string.preference_geofence_intervall_in_milliseconds_min));
            Integer edit;
            if (value.toString().isEmpty()) {
                edit = null;
            } else {
                edit = Integer.parseInt(value.toString());
            }

            if (edit == null || edit < min) {
                Toast.makeText(getActivity(), String.format("%s %s", getString(R.string.preference_min_violation), min), Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        preference.setSummary(value.toString());

        return true;
    }
}
