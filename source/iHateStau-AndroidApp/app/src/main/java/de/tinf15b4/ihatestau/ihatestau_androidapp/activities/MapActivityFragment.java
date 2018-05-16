package de.tinf15b4.ihatestau.ihatestau_androidapp.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.tinf15b4.ihatestau.ihatestau_androidapp.R;
import de.tinf15b4.ihatestau.ihatestau_androidapp.domain.CameraGeofenceEntity;
import de.tinf15b4.ihatestau.ihatestau_androidapp.services.GeofenceService;
import de.tinf15b4.ihatestau.ihatestau_androidapp.services.LearningHandler;
import de.tinf15b4.ihatestau.ihatestau_androidapp.util.FileManager;
import de.tinf15b4.ihatestau.ihatestau_androidapp.util.SettingsManager;
import de.tinf15b4.ihatestau.persistence.CameraSpotConfig;
import de.tinf15b4.ihatestau.persistence.ExitSpotConfig;

public class MapActivityFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = MapActivityFragment.class.getSimpleName();

    private GoogleMap googleMap;

    private Map<String, CameraGeofenceEntity> selectedCameras;
    private Intent geofenceService;
    private ToggleButton toggleButtonGeofence;
    private ToggleButton toggleButtonLearning;
    private View rootView;

    private BroadcastReceiver broadcastReceiver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        selectedCameras = FileManager.getSelectedCameraGeofenceEntites();

        rootView = inflater.inflate(R.layout.fragment_map, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.geofence_map);

        mapFragment.getMapAsync(this);

        geofenceService = new Intent(getActivity(), GeofenceService.class);
        geofenceService.setAction(GeofenceService.ACTION_START_TRAFFIC_SERVICE);

        setUpToggleButtons();
        setUpBroadcastReceiver();


        return rootView;
    }

    private void setUpBroadcastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case GeofenceService.ACTION_UPDATE_MAP:
                        Location location = (Location) intent.getParcelableExtra("location");
                        Log.v(TAG, "onReceive called " + location);

                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        break;
                    case GeofenceService.ACTION_STOP_SERVICE:
                        getActivity().stopService(geofenceService);
                        googleMap.clear();
                        toggleButtonGeofence.setChecked(false);
                        toggleButtonGeofence.setEnabled(true);
                        toggleButtonLearning.setChecked(false);
                        toggleButtonLearning.setEnabled(true);
                        break;
                    case GeofenceService.ACTION_SERVICE_STARTED:
                        drawGeofences();
                        break;
                    case LearningHandler.ACTION_LEARNING_FINISHED:
                        showLearningSummaryDialog(
                                intent.getStringExtra(LearningHandler.INFO_TITLE_ID),
                                intent.getStringExtra(LearningHandler.INFO_TEXT_ID));
                }
            }
        };
    }

    private void setUpToggleButtons() {
        toggleButtonGeofence = (ToggleButton) rootView.findViewById(R.id.toggle_map_geofence);
        toggleButtonLearning = (ToggleButton) rootView.findViewById(R.id.toggle_map_learning);

        toggleButtonGeofence.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!checkPermission()) {
                return;
            }

            Intent intent = new Intent(getActivity(), GeofenceService.class);
            intent.setAction(GeofenceService.ACTION_START_TRAFFIC_SERVICE);


            if (isChecked && !GeofenceService.isRunning() && !GeofenceService.isLearning()) {

                if (FileManager.getSelectedCameraGeofenceEntites().isEmpty()) {
                    Toast.makeText(getContext(), getString(R.string.no_cameras_to_observe), Toast.LENGTH_LONG).show();
                    toggleButtonGeofence.setChecked(false);
                    return;
                }

                getActivity().startService(intent);
                toggleButtonLearning.setEnabled(false);

            } else if (!isChecked && GeofenceService.isRunning()) {
                getActivity().stopService(intent);
                googleMap.clear();
                toggleButtonLearning.setEnabled(true);
            }
        });

        toggleButtonLearning.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!checkPermission()) {
                return;
            }

            Intent intent = new Intent(getActivity(), GeofenceService.class);
            intent.setAction(GeofenceService.ACTION_START_LEARNING_SERVICE);
            if (isChecked && !GeofenceService.isLearning() && !GeofenceService.isRunning()) {
                if (FileManager.getAllCameras().isEmpty() || FileManager.getAllExits().isEmpty()) {
                    Toast.makeText(getContext(), getString(R.string.no_cameras_to_learn), Toast.LENGTH_LONG).show();
                    toggleButtonLearning.setChecked(false);
                    return;
                }

                showWarningDialog(intent);

            } else if (!isChecked && GeofenceService.isLearning()) {
                getActivity().stopService(intent);
                googleMap.clear();
                toggleButtonGeofence.setEnabled(true);
            }
        });
    }

    private void showWarningDialog(Intent intent) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        toggleButtonGeofence.setEnabled(false);
                        intent.putExtra(GeofenceService.DELETE_EXISTING_CAMERAS_ID, true);
                        getActivity().startService(intent);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        toggleButtonGeofence.setEnabled(false);
                        intent.putExtra(GeofenceService.DELETE_EXISTING_CAMERAS_ID, false);
                        getActivity().startService(intent);
                        break;

                    case DialogInterface.BUTTON_NEUTRAL:
                        toggleButtonLearning.setChecked(false);
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.learning_mode_caution).setMessage(R.string.learning_mode_caution_text).setPositiveButton(R.string.yes, dialogClickListener)
                .setNegativeButton(R.string.no, dialogClickListener).setNeutralButton(R.string.cancel, dialogClickListener).show();
    }

    private boolean checkPermission() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), getString(R.string.no_use_without_permissions), Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    @NonNull
    private List<Geofence> getGeofences() {
        List<Geofence> geofences = new ArrayList<>();
        for (CameraGeofenceEntity cge : selectedCameras.values()) {
            CameraSpotConfig camera = cge.getCamera();
            LatLng latLng = cge.getLatLng();
            geofences.add(new Geofence.Builder()
                    .setRequestId(camera.getId())
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setCircularRegion(latLng.latitude, latLng.longitude, SettingsManager.getGeofenceRadiusInMeters())
                    .setNotificationResponsiveness(1000)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .build());
        }

        return geofences;
    }

    @Override
    public void onResume() {
        super.onResume();
        int response = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getActivity());
        if (response != ConnectionResult.SUCCESS) {
            Log.d(TAG, "Google Play Service Not Available");
            GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), response, 1).show();

        } else {
            Log.d(TAG, "Google play service available");

            IntentFilter intentFilter = new IntentFilter(GeofenceService.ACTION_UPDATE_MAP);
            intentFilter.addAction(GeofenceService.ACTION_STOP_SERVICE);
            intentFilter.addAction(GeofenceService.ACTION_SERVICE_STARTED);
            intentFilter.addAction(LearningHandler.ACTION_LEARNING_FINISHED);
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, intentFilter);
        }

        if (GeofenceService.isRunning()) {
            toggleButtonGeofence.setChecked(true);
            toggleButtonGeofence.setEnabled(true);
            toggleButtonLearning.setChecked(false);
            toggleButtonLearning.setEnabled(false);
        } else if (GeofenceService.isLearning()) {
            toggleButtonGeofence.setChecked(false);
            toggleButtonGeofence.setEnabled(false);
            toggleButtonLearning.setChecked(true);
            toggleButtonLearning.setEnabled(true);
        } else {
            toggleButtonGeofence.setChecked(false);
            toggleButtonGeofence.setEnabled(true);
            toggleButtonLearning.setChecked(false);
            toggleButtonLearning.setEnabled(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onStart() {
        super.onStart();
        setFancyColorLayoutVisibility();
        selectedCameras = FileManager.getSelectedCameraGeofenceEntites();
    }

    private void setFancyColorLayoutVisibility() {
        LinearLayout fancyColorLayout = (LinearLayout) rootView.findViewById(R.id.fancy_color_layout);
        if (SettingsManager.getFancyColorMode()) {
            fancyColorLayout.setVisibility(View.VISIBLE);
        } else {
            fancyColorLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        this.googleMap = googleMap;

        drawGeofences();

        googleMap.setMyLocationEnabled(true);

        LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        Location currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        LatLng currentLatLng;
        if (currentLocation != null) {
            currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        } else {
            currentLatLng = new LatLng(49.02, 8.4130);  // Wildpark-Stadium, Karlsruhe
        }
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 13f));

    }

    private void drawGeofences() {
        googleMap.clear();
        if (GeofenceService.isRunning()) {
            for (CameraGeofenceEntity cge : selectedCameras.values()) {
                CameraSpotConfig camera = cge.getCamera();
                LatLng latLng = cge.getLatLng();

                googleMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.defaultMarker(
                                SettingsManager.getFancyColorMode()
                                        ? BitmapDescriptorFactory.HUE_GREEN
                                        : BitmapDescriptorFactory.HUE_RED))
                        .title(String.format("%s: %s", getString(R.string.notificationpoint_for), camera.getName())));

                googleMap.addCircle(new CircleOptions()
                        .center(latLng)
                        .radius(SettingsManager.getGeofenceRadiusInMeters())
                        .strokeColor(SettingsManager.getFancyColorMode() ? Color.GREEN : Color.RED)
                        .strokeWidth(4f));
            }
        } else if (GeofenceService.isLearning()) {
            for (CameraSpotConfig camera : FileManager.getAllCameras().values()) {
                LatLng latLng = new LatLng(camera.getCameraLat(), camera.getCameraLon());
                googleMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        .title(String.format("%s: %s", getString(R.string.camera), camera.getName())));

                googleMap.addCircle(new CircleOptions()
                        .center(latLng)
                        .radius(SettingsManager.getGeofenceRadiusInMeters())
                        .strokeColor(Color.RED)
                        .strokeWidth(4f));
            }

            for (ExitSpotConfig exit : FileManager.getAllExits().values()) {
                LatLng latLng = new LatLng(exit.getExitLat(), exit.getExitLon());
                googleMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.defaultMarker(
                                SettingsManager.getFancyColorMode()
                                        ? BitmapDescriptorFactory.HUE_BLUE
                                        : BitmapDescriptorFactory.HUE_RED))
                        .title(String.format("%s: %s", getString(R.string.exit), exit.getName())));

                googleMap.addCircle(new CircleOptions()
                        .center(latLng)
                        .radius(SettingsManager.getGeofenceRadiusInMeters())
                        .strokeColor(SettingsManager.getFancyColorMode() ? Color.BLUE : Color.RED)
                        .strokeWidth(4f));
            }
        }

    }

    private void showLearningSummaryDialog(String title, String message) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // no action needed
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title).setMessage(message).setPositiveButton(R.string.ok, dialogClickListener).show();
    }

}
