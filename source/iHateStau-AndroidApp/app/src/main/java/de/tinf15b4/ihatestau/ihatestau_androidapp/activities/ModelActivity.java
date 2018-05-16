package de.tinf15b4.ihatestau.ihatestau_androidapp.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import de.tinf15b4.ihatestau.ihatestau_androidapp.R;

public class ModelActivity extends AppCompatActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_overview:
                this.startActivity(new Intent(this, OverviewActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                break;
            case R.id.menu_camera_list:
                this.startActivity(new Intent(this, CameraListActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                break;
            case R.id.menu_settings:
                this.startActivity(new Intent(this, SettingsActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                break;
            case R.id.menu_map:
                this.startActivity(new Intent(this, MapActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
