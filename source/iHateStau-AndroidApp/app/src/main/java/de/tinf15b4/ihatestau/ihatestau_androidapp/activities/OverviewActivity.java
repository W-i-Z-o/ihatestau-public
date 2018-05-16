package de.tinf15b4.ihatestau.ihatestau_androidapp.activities;

import android.os.Bundle;

import de.tinf15b4.ihatestau.ihatestau_androidapp.R;

public class OverviewActivity extends ModelActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.activity_title_overview);
        setContentView(R.layout.activity_overview);
    }
    
}
