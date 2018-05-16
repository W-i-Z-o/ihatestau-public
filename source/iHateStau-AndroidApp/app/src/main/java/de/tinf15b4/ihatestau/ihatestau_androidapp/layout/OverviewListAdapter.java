package de.tinf15b4.ihatestau.ihatestau_androidapp.layout;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.internal.LinkedTreeMap;

import java.util.List;
import java.util.Map;

import de.tinf15b4.ihatestau.ihatestau_androidapp.R;
import de.tinf15b4.ihatestau.ihatestau_androidapp.activities.AddCameraActivity;
import de.tinf15b4.ihatestau.ihatestau_androidapp.activities.AddCameraActivityFragment;
import de.tinf15b4.ihatestau.ihatestau_androidapp.activities.OverviewActivity;
import de.tinf15b4.ihatestau.ihatestau_androidapp.util.FileManager;
import de.tinf15b4.ihatestau.persistence.CameraSpotConfig;

public class OverviewListAdapter extends ArrayAdapter<CameraSpotConfig> {

    private static final String TAG = OverviewListAdapter.class.getSimpleName();
    private int layout;

    Map<String, Float> trafficStates = new LinkedTreeMap<>();
    Map<String, byte[]> cameraImages = new LinkedTreeMap<>();

    public OverviewListAdapter(@NonNull Context context, int resource, @NonNull List<CameraSpotConfig> objects) {
        super(context, resource, objects);
        layout = resource;
        updateCameraList(objects);
    }

    public void updateCameraList(List<CameraSpotConfig> cameras) {
        clear();
        addAll(cameras);
        notifyDataSetChanged();
    }

    public void updateTrafficStates(Map<String, Float> trafficStates, Map<String, byte[]> cameraImages){
        this.trafficStates = trafficStates;
        this.cameraImages = cameraImages;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        OverviewListItemViewHolder viewHolder;

        CameraSpotConfig camera = getItem(position);

        //TODO: Hack, cause else-case will never reached.
        // Without this hack fist Items will repeated after Scrolling.
        // Should be fixed, when there is time...
        convertView = null;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(layout, parent, false);
            viewHolder = new OverviewListItemViewHolder();
            viewHolder.textViewName = (TextView) convertView.findViewById(R.id.list_item_overview_name);
            viewHolder.textViewTraffic = (TextView) convertView.findViewById(R.id.list_item_overview_traffic);
            viewHolder.button = (Button) convertView.findViewById(R.id.list_item_overview_button);
            viewHolder.imgLayout = (LinearLayout) convertView.findViewById(R.id.list_item_overview_img_layout);
            viewHolder.img = (ImageView) convertView.findViewById(R.id.list_item_overview_img);
            viewHolder.button.setOnClickListener(v -> {

                Button button = viewHolder.button;

                if (FileManager.containsSelectedCamera(camera)) {
                    FileManager.removeSelectedCamera(camera);
                    if (getContext() instanceof OverviewActivity) {
                        remove(camera);
                    }
                } else {
                    Intent addCameraIntent = new Intent(getContext(), AddCameraActivity.class);
                    addCameraIntent.setAction(AddCameraActivityFragment.ACTION_ADD);
                    addCameraIntent.putExtra("Camera", camera);
                    getContext().startActivity(addCameraIntent);
                }

                notifyDataSetChanged();

            });
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (OverviewListItemViewHolder) convertView.getTag();
        }

        if (FileManager.containsSelectedCamera(camera)) {
            viewHolder.button.setText(getContext().getString(R.string.list_item_camera_list_button_caption_remove));
            convertView.setOnClickListener(view -> {
                if(viewHolder.imgLayout.getVisibility() == View.GONE){
                    viewHolder.imgLayout.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.imgLayout.setVisibility(View.GONE);
                }
            });
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

        viewHolder.textViewName.setText(camera.getName());

        String trafficString = "Stau: ";
        Float jamProbability = trafficStates.get(camera.getId());
        if (jamProbability != null) {
            int intPercentage = ((Float) (jamProbability * 100)).intValue();
            trafficString += intPercentage + "%";
        } else {
            trafficString += "---";
        }

        byte[] image = cameraImages.get(camera.getId());
        if(image != null){
            Bitmap bitmap= BitmapFactory.decodeByteArray(image,0,image.length);
            viewHolder.img.setImageBitmap(bitmap);
        }

        viewHolder.textViewTraffic.setText(trafficString);
        return convertView;
    }
}