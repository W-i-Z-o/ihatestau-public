package de.tinf15b4.ihatestau.ihatestau_androidapp.activities;

import android.os.Bundle;

import de.tinf15b4.ihatestau.ihatestau_androidapp.R;

public class MapActivity extends ModelActivity {

    private static final String TAG = MapActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
    }
}
