package de.tinf15b4.ihatestau.ihatestau_androidapp.layout;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import de.tinf15b4.ihatestau.ihatestau_androidapp.R;
import de.tinf15b4.ihatestau.ihatestau_androidapp.activities.AddCameraActivityFragment;
import de.tinf15b4.ihatestau.persistence.ExitSpotConfig;

public class AddCameraButtonListAdapter extends ArrayAdapter<ExitSpotConfig> {

    private static final int REQUEST_PLACE_PICKER = 1;

    private int layout;
    private AddCameraActivityFragment fragment;

    public AddCameraButtonListAdapter(@NonNull AddCameraActivityFragment fragment, int resource, @NonNull List<ExitSpotConfig> objects) {
        super(fragment.getActivity(), resource, objects);
        layout = resource;
        this.fragment = (AddCameraActivityFragment) fragment;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        AddCameraButtonItemViewHolder viewHolder;

        ExitSpotConfig exit = getItem(position);

        //TODO: Hack, cause else-case will never reached.
        // Without this hack fist Items will repeated after Scrolling.
        // Should be fixed, when there is time...
        convertView = null;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(layout, parent, false);
            viewHolder = new AddCameraButtonItemViewHolder();
            viewHolder.button = (Button) convertView.findViewById(R.id.list_item_add_camera_button);
            viewHolder.button.setOnClickListener(v -> {

                fragment.startPlacePicker(new LatLng(exit.getExitLat(),exit.getExitLon()));

            });
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (AddCameraButtonItemViewHolder) convertView.getTag();
        }

        viewHolder.button.setText(exit.getName());
        return convertView;
    }
}