package de.tinf15b4.ihatestau.ihatestau_androidapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;

import java.util.List;

import de.tinf15b4.ihatestau.ihatestau_androidapp.R;
import de.tinf15b4.ihatestau.ihatestau_androidapp.layout.AddCameraButtonListAdapter;
import de.tinf15b4.ihatestau.ihatestau_androidapp.domain.CameraGeofenceEntity;
import de.tinf15b4.ihatestau.ihatestau_androidapp.util.FileManager;
import de.tinf15b4.ihatestau.persistence.CameraSpotConfig;
import de.tinf15b4.ihatestau.persistence.ExitSpotConfig;

public class AddCameraActivityFragment extends Fragment {

    private static final String TAG = AddCameraActivityFragment.class.getSimpleName();

    public static final String ACTION_ADD = "de.tinf15b4.ihatestau.action.ACTION_ADD";
    public static final String ACTION_EDIT = "de.tinf15b4.ihatestau.action.ACTION_EDIT";

    private static final int REQUEST_PLACE_PICKER = 1;

    private CameraSpotConfig camera;
    private LatLng latLng;

    private TextView textPickedLocation;
    private Button buttonAddCamera;
    private Button buttonPreselectedSpot;

    public AddCameraActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_add_camera, container, false);

        textPickedLocation = (TextView) rootView.findViewById(R.id.textview_add_camera_picked_location);
        buttonAddCamera = (Button) rootView.findViewById(R.id.button_add_camera_add);
        buttonAddCamera.setOnClickListener(view -> {
            FileManager.addSelectedCamera(camera, latLng);
            NavUtils.navigateUpFromSameTask(getActivity());
        });

        buttonPreselectedSpot = (Button) rootView.findViewById(R.id.button_add_camera_preselected_spot);
        buttonPreselectedSpot.setOnClickListener(view -> startPlacePicker(latLng));

        Intent incomingIntent = getActivity().getIntent();
        if (incomingIntent != null && incomingIntent.getAction() != null) {
            switch (incomingIntent.getAction()) {
                case ACTION_ADD:
                    camera = (CameraSpotConfig) incomingIntent.getSerializableExtra("Camera");

                    break;
                case ACTION_EDIT:
                    camera = (CameraSpotConfig) incomingIntent.getSerializableExtra("Camera");
                    CameraGeofenceEntity cameraGeofenceEntity = FileManager.getSelectedCameraGeofenceEntites().get(camera.getId());
                    setLatLng(cameraGeofenceEntity.getLatLng(),
                            String.format("%s, %s",cameraGeofenceEntity.getLatLng().latitude,cameraGeofenceEntity.getLatLng().longitude));
                    buttonPreselectedSpot.setVisibility(View.VISIBLE);
                    buttonAddCamera.setText(R.string.add_camera_button_edit);
                    getActivity().setTitle(R.string.activity_title_edit_camera);
//                    TextView textHeadline = (TextView) rootView.findViewById(R.id.textview_add_camera_headline);
//                    textHeadline.setText(R.string.add_camera_button_edit);
            }
        }

        TextView textView = (TextView) rootView.findViewById(R.id.textview_add_camera_selected);
        textView.setText(camera.getName());

        List<ExitSpotConfig> exits = FileManager.getAllExitsForCamera(camera);

        AddCameraButtonListAdapter listAdapter = new AddCameraButtonListAdapter(
                this, // Die aktuelle Umgebung (diese Activity)
                R.layout.list_item_add_camera_buttons, // ID der XML-Layout Datei
                FileManager.getAllExitsForCamera(camera)); // Beispieldaten in einer ArrayList

        ListView listView = (ListView) rootView.findViewById(R.id.listview_add_camera);
        listView.setAdapter(listAdapter);

        Button focusCamera = (Button) rootView.findViewById(R.id.button_add_camera_focus_camera);
        focusCamera.setOnClickListener(view -> startPlacePicker(new LatLng(camera.getCameraLat(), camera.getCameraLon())));

        return rootView;
    }

    public void startPlacePicker(LatLng location) {
        LatLngBounds.Builder latLngBounds = new LatLngBounds.Builder().include(location);

        double offsetMeters = 500;
        LatLng offsetPoint = SphericalUtil.computeOffset(location, offsetMeters, 0);
        latLngBounds.include(offsetPoint);
        offsetPoint = SphericalUtil.computeOffset(location, offsetMeters, 90);
        latLngBounds.include(offsetPoint);
        offsetPoint = SphericalUtil.computeOffset(location, offsetMeters, 180);
        latLngBounds.include(offsetPoint);
        offsetPoint = SphericalUtil.computeOffset(location, offsetMeters, 270);
        latLngBounds.include(offsetPoint);

        PlacePicker.IntentBuilder placePicker = new PlacePicker.IntentBuilder().setLatLngBounds(latLngBounds.build());
        try {
            startActivityForResult(placePicker.build(getActivity()), REQUEST_PLACE_PICKER);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PLACE_PICKER) {
            if (resultCode == Activity.RESULT_OK) {
                Place place = PlacePicker.getPlace(data, getActivity());
                setLatLng(place.getLatLng(),String.format("%s\n%s", place.getName(), place.getAddress()));
            }
        }
    }

    private void setLatLng(LatLng latLng, String text){
        this.latLng = latLng;
        textPickedLocation.setText(text);
        buttonAddCamera.setEnabled(true);
        buttonPreselectedSpot.setVisibility(View.VISIBLE);
    }
}