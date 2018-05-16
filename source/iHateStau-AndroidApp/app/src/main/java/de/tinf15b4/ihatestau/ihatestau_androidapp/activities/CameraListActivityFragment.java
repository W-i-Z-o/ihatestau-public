package de.tinf15b4.ihatestau.ihatestau_androidapp.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

import de.tinf15b4.ihatestau.ihatestau_androidapp.R;
import de.tinf15b4.ihatestau.ihatestau_androidapp.tasks.CameraLoader;
import de.tinf15b4.ihatestau.ihatestau_androidapp.layout.CameraListAdapter;
import de.tinf15b4.ihatestau.ihatestau_androidapp.util.FileManager;

public class CameraListActivityFragment extends Fragment {

    private CameraListAdapter cameraListAdapter;
    private SwipeRefreshLayout rootView;

    private BroadcastReceiver broadcastReceiver;

    public CameraListActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setupBroadcastReceiver();

        cameraListAdapter = new CameraListAdapter(
                getActivity(),
                R.layout.list_item_camera_list,
                new ArrayList<>());

        rootView = (SwipeRefreshLayout) inflater.inflate(R.layout.fragment_camera_list, container, false);

        ListView cameraListView = (ListView) rootView.findViewById(R.id.listview_camera_list);
        cameraListView.setAdapter(cameraListAdapter);

        rootView.setOnRefreshListener(() -> {
            CameraLoader cameraLoader = new CameraLoader(getContext());
            cameraLoader.execute();
        });

        return rootView;
    }

    private void setupBroadcastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case CameraLoader.ACTION_SYNCHRONIZATION_SUCCESSFULL:
                        updateCameraList();
                        rootView.setRefreshing(false);
                        break;
                    case CameraLoader.ACTION_SYNCHRONIZATION_FAILED:
                        rootView.setRefreshing(false);
                }
            }
        };
    }

    @Override
    public void onResume(){
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(CameraLoader.ACTION_SYNCHRONIZATION_SUCCESSFULL);
        intentFilter.addAction(CameraLoader.ACTION_SYNCHRONIZATION_FAILED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver,intentFilter);
        updateCameraList();
    }

    @Override
    public void onPause(){
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
    }

    private void updateCameraList() {
        cameraListAdapter.updateCameraList(FileManager.getListOfAllCameras());
    }
}