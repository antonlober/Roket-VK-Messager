package sweet.messager.vk.adapters;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import sweet.messager.vk.ApplicationName;
import sweet.messager.vk.R;
import sweet.messager.vk.interfaces.OnRecyclerItemListener;

public class Images extends RecyclerView.Adapter<Images.ViewHolder> {

    Context context;
    List<JSONObject> images = new ArrayList<>();
    OnRecyclerItemListener onItemListener;

    public Images(OnRecyclerItemListener _onItemListener) {
        super();
        onItemListener = _onItemListener;
        context = ApplicationName.getAppContext();
        images = getAllShownImagesPath(false);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(context)
                        .inflate(
                                viewType == 5 ? R.layout.camera : R.layout.image
                                , parent, false)
        );
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (position != 0) {
            try {
                JSONObject item = images.get(position);
                holder.image_bg.setVisibility(item.has("active") ? View.VISIBLE : View.GONE);
                String img = item.getString("thumb");
                Picasso.with(ApplicationName.getAppContext())
                        .load(new File(img))
                        .placeholder(R.mipmap.ic_placeholder)
                        .error(R.mipmap.ic_placeholder)
                        .into(holder.imageView);
            } catch (JSONException e) { }
        } else {

        }
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView imageView;
        View image_bg;
        SurfaceView surface;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            imageView = (ImageView) itemView.findViewById(R.id.image);
            image_bg = itemView.findViewById(R.id.image_bg);
            surface = (SurfaceView) itemView.findViewById(R.id.camera_view);
        }

        @Override
        public void onClick(View v) {
            int position = getPosition();
            try {
                JSONObject item = images.get(position);
                images.remove(item);
                if (item.has("active")) {
                    item.remove("active");
                } else {
                    item.put("active", "active");
                }
                images.add(position, item);
                notifyItemChanged(position);
                onItemListener.onClick(position, item);
            } catch (JSONException e) { }
        }
    }


    @Override
    public int getItemViewType(int position) {
        return position == 0 ? 5 : 0;
    }


    public List<JSONObject> getAllShownImagesPath(boolean phone) {
        Uri uri;
        List<JSONObject> listOfAllImages = new ArrayList<>();
        boolean getPhonePhoto = true;
        try {
            String state = Environment.getExternalStorageState();
            if (phone || !Environment.MEDIA_MOUNTED.equals(state)) {
                uri = android.provider.MediaStore.Images.Thumbnails.INTERNAL_CONTENT_URI;
                getPhonePhoto = false;
            } else {
                uri = android.provider.MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI;
            }
        } catch (Exception e) {
            uri = android.provider.MediaStore.Images.Thumbnails.INTERNAL_CONTENT_URI;
        }
        String[] projection = {
                MediaStore.Images.Thumbnails.DATA,
                MediaStore.Images.Thumbnails.IMAGE_ID
        };
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, MediaStore.Images.Thumbnails.IMAGE_ID + " DESC LIMIT 15");
        JSONObject item = new JSONObject();
        listOfAllImages.add(item);
        if (cursor == null) return listOfAllImages;
        int column_index_data = cursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.DATA);
        int column_index_id = cursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.IMAGE_ID);
        while (cursor.moveToNext()) {
            try {
                item = new JSONObject();
                item.put("thumb", cursor.getString(column_index_data));
                item.put("id", cursor.getString(column_index_id));
                listOfAllImages.add(item);
            } catch (JSONException e) { }
        }
        /*if (getPhonePhoto) {
            listOfAllImages.addAll(getAllShownImagesPath(true));
        }

        List<Integer> ids = new ArrayList<>();
        HashMap<Integer, JSONObject> photos = new HashMap<>();
        try {
            for (int i = 0; i < listOfAllImages.size(); i++) {
                JSONObject photo = listOfAllImages.get(i);
                int id = photo.getInt("id");
                ids.add(id);
                photos.put(id, photo);
            }
        } catch (JSONException e) { }
        Collections.sort(ids);
        Collections.reverse(ids);
        listOfAllImages = new ArrayList<>();
        try {
            for (int i = 0; i < ids.size(); i++) {
                listOfAllImages.add(photos.get(ids.get(i)));
            }
        } catch (NullPointerException e) { }
         */
        return listOfAllImages;
    }


}
