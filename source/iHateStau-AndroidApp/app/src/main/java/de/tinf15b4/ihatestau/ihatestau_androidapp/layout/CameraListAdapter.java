package de.tinf15b4.ihatestau.ihatestau_androidapp.layout;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import de.tinf15b4.ihatestau.ihatestau_androidapp.R;
import de.tinf15b4.ihatestau.ihatestau_androidapp.activities.AddCameraActivity;
import de.tinf15b4.ihatestau.ihatestau_androidapp.activities.AddCameraActivityFragment;
import de.tinf15b4.ihatestau.ihatestau_androidapp.activities.OverviewActivity;
import de.tinf15b4.ihatestau.ihatestau_androidapp.util.FileManager;
import de.tinf15b4.ihatestau.persistence.CameraSpotConfig;

public class CameraListAdapter extends ArrayAdapter<CameraSpotConfig> {
    private int layout;

    public CameraListAdapter(@NonNull Context context, int resource, @NonNull List<CameraSpotConfig> objects) {
        super(context, resource, objects);
        layout = resource;
        updateCameraList(objects);
    }

    public void updateCameraList(List<CameraSpotConfig> cameras){
        clear();
        addAll(cameras);
        sort((c1,c2) -> c1.getName().compareTo(c2.getName()));
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        CameraListItemViewHolder viewHolder;

        CameraSpotConfig camera = getItem(position);

        //TODO: Hack, cause else-case will never reached.
        // Without this hack fist Items will repeated after Scrolling.
        // Should be fixed, when there is time...
        convertView = null;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(layout, parent, false);
            viewHolder = new CameraListItemViewHolder();
            viewHolder.textView = (TextView) convertView.findViewById(R.id.list_item_camera_list_textview);
            viewHolder.button = (Button) convertView.findViewById(R.id.list_item_camera_list_button);
            viewHolder.button.setOnClickListener(v -> {

                Button button = viewHolder.button;

                if (FileManager.containsSelectedCamera(camera)) {
                    FileManager.removeSelectedCamera(camera);
                    if(getContext() instanceof OverviewActivity){
                        remove(camera);
                    }
                } else {
                    Intent addCameraIntent = new Intent(getContext(), AddCameraActivity.class);
                    addCameraIntent.setAction(AddCameraActivityFragment.ACTION_ADD);
                    addCameraIntent.putExtra("Camera", camera);
                    getContext().startActivity(addCameraIntent);
                }
//                FileManager.saveSelectedCameras();

                notifyDataSetChanged();

                for (CameraSpotConfig c : FileManager.getListOfSelectedCameras()) {
                    Log.v("SIMON", c.getName());
                }

            });
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (CameraListItemViewHolder) convertView.getTag();
        }

        if (FileManager.containsSelectedCamera(camera)) {
            viewHolder.button.setText(getContext().getString(R.string.list_item_camera_list_button_caption_remove));
            convertView.setOnLongClickListener(view -> {
                Intent intent = new Intent(getContext(), AddCameraActivity.class);
                intent.setAction(AddCameraActivityFragment.ACTION_EDIT);
                intent.putExtra("Camera", camera);
                getContext().startActivity(intent);
                return true;
            });
        } else {
            viewHolder.button.setText(getContext().getString(R.string.list_item_camera_list_button_caption_add));
        }
        viewHolder.textView.setText(camera.getName());
        return convertView;
    }
}