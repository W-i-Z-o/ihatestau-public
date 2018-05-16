package de.tinf15b4.ihatestau.ihatestau_androidapp.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.gson.internal.LinkedTreeMap;

import java.util.Map;

import de.tinf15b4.ihatestau.ihatestau_androidapp.R;
import de.tinf15b4.ihatestau.ihatestau_androidapp.tasks.TrafficInformationLoader;
import de.tinf15b4.ihatestau.ihatestau_androidapp.util.FileManager;
import de.tinf15b4.ihatestau.ihatestau_androidapp.services.GeofenceService;
import de.tinf15b4.ihatestau.ihatestau_androidapp.services.LearningHandler;
import de.tinf15b4.ihatestau.ihatestau_androidapp.layout.OverviewListAdapter;

public class OverviewActivityFragment extends Fragment {

    private static final int REQUEST_LOCATION_PERMISSION_CODE = 101;

    private OverviewListAdapter overviewListAdapter;

    private ToggleButton toggleButtonGeofence;
    private ToggleButton toggleButtonLearning;
    private View rootView;
    private BroadcastReceiver broadcastReceiver;
    private SwipeRefreshLayout swipeRefreshLayout;

    public OverviewActivityFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_overview, container, false);

        setUpCameraListView();
        setUpToggleButtons();
        setUpBroadcastReceiver();
        setUpAddCameraButton();
        setUpGeofenceButton();
        setUpSwipeRefreshLayout();

        return rootView;
    }

    private void setUpSwipeRefreshLayout() {
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_layout_overview);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            updateSelectedCamerasList();
            updateTrafficStates();
        });
    }

    private void setUpCameraListView() {
        overviewListAdapter = new OverviewListAdapter(
                getActivity(),
                R.layout.list_item_camera_overview,
                FileManager.getListOfSelectedCameras());


        ListView cameraListView = (ListView) rootView.findViewById(R.id.listview_overview);
        cameraListView.setAdapter(overviewListAdapter);
    }

    private void setUpGeofenceButton() {
        Button buttonGeofence = (Button) rootView.findViewById(R.id.button_overview_start_geofence);
        buttonGeofence.setOnClickListener(view -> {

            if (checkLocationMode()) {
                Intent intent = new Intent(getContext(), MapActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                getContext().startActivity(intent);
            } else {
                Toast.makeText(getContext(), getString(R.string.wrong_location_mode), Toast.LENGTH_LONG).show();
            }

        });
    }

    private void setUpAddCameraButton() {
        Button buttonAddCamera = (Button) rootView.findViewById(R.id.button_overview_add_camera);
        buttonAddCamera.setOnClickListener(view -> {
            startActivity(new Intent(getActivity(), CameraListActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
        });
    }

    private void setUpToggleButtons() {
        toggleButtonGeofence = (ToggleButton) rootView.findViewById(R.id.toggle_overview_geofence);
        toggleButtonLearning = (ToggleButton) rootView.findViewById(R.id.toggle_overview_learning);

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
                if (checkLocationMode()) {
                    getActivity().startService(intent);
                    toggleButtonLearning.setEnabled(false);
                } else {
                    toggleButtonGeofence.setChecked(false);
                    Toast.makeText(getContext(), getString(R.string.wrong_location_mode), Toast.LENGTH_LONG).show();
                }
            } else if (!isChecked && GeofenceService.isRunning()) {
                getActivity().stopService(intent);
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
                if (checkLocationMode()) {
                    showWarningDialog(intent);
                } else {
                    toggleButtonLearning.setChecked(false);
                    Toast.makeText(getContext(), getString(R.string.wrong_location_mode), Toast.LENGTH_LONG).show();
                }
            } else if (!isChecked && GeofenceService.isLearning()) {
                getActivity().stopService(intent);
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

    private boolean checkLocationMode() {
        int locationMode = -1;

        try {
            locationMode = Settings.Secure.getInt(getContext().getContentResolver(), Settings.Secure.LOCATION_MODE);

        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        return (locationMode != Settings.Secure.LOCATION_MODE_OFF && locationMode == Settings.Secure.LOCATION_MODE_HIGH_ACCURACY); //check location mode
    }

    private void setUpBroadcastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case GeofenceService.ACTION_STOP_SERVICE:
                        toggleButtonGeofence.setChecked(false);
                        toggleButtonGeofence.setEnabled(true);
                        toggleButtonLearning.setChecked(false);
                        toggleButtonLearning.setEnabled(true);
                        break;
                    case TrafficInformationLoader.ACTION_SYNCHRONIZATION_SUCCESSFULL:
                        if (intent.hasExtra(TrafficInformationLoader.INTENT_PARAM_TRAFFIC_STATES)) {
                            Map<String, Float> trafficStates = (LinkedTreeMap) intent.getSerializableExtra(TrafficInformationLoader.INTENT_PARAM_TRAFFIC_STATES);
                            Map<String, byte[]> cameraImages = (LinkedTreeMap) intent.getSerializableExtra(TrafficInformationLoader.INTENT_PARAM_CAMERA_IMAGES);
                            overviewListAdapter.updateTrafficStates(trafficStates, cameraImages);
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        break;
                    case LearningHandler.ACTION_LEARNING_FINISHED:
                        updateSelectedCamerasList();
                        updateTrafficStates();
                        showLearningSummaryDialog(
                                intent.getStringExtra(LearningHandler.INFO_TITLE_ID),
                                intent.getStringExtra(LearningHandler.INFO_TEXT_ID));
                        break;
                    case TrafficInformationLoader.ACTION_SYNCHRONIZATION_FAILED:
                        swipeRefreshLayout.setRefreshing(false);
                }
            }
        };
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

    @Override
    public void onResume() {
        super.onResume();

        // Check permission and request if needed
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSION_CODE);
        }

        updateSelectedCamerasList();
        updateTrafficStates();
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
        IntentFilter filter = new IntentFilter(GeofenceService.ACTION_STOP_SERVICE);
        filter.addAction(TrafficInformationLoader.ACTION_SYNCHRONIZATION_SUCCESSFULL);
        filter.addAction(TrafficInformationLoader.ACTION_SYNCHRONIZATION_FAILED);
        filter.addAction(LearningHandler.ACTION_LEARNING_FINISHED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, filter);
    }


    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(broadcastReceiver);
    }


    private void updateSelectedCamerasList() {
        overviewListAdapter.updateCameraList(FileManager.getListOfSelectedCameras());
    }

    private void updateTrafficStates() {
        TrafficInformationLoader trafficInformationLoader = new TrafficInformationLoader(getActivity());
        trafficInformationLoader.execute();
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