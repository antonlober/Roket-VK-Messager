package sweet.messager.vk.adapters;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import sweet.messager.vk.ApplicationName;
import sweet.messager.vk.R;

public class PhotoAdapter extends BaseAdapter {

    JSONArray items;

    public PhotoAdapter(JSONArray _items) {
        items = _items;
    }

    @Override
    public int getCount() {
        return Math.min(200, items.length());
    }

    @Override
    public Object getItem(int position) {
        try {
            return items.getJSONArray(position);
        } catch (JSONException e) {
            return new JSONObject();
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_select, parent, false);
        }
        ImageView imageView = (ImageView) convertView.findViewById(R.id.image);
        try {
            JSONObject item = items.getJSONObject(position);
            if (item.has("photo_130")) {
                String img = item.getString("photo_130");
                Picasso.with(ApplicationName.getAppContext()).load(img).placeholder(R.mipmap.ic_placeholder)
                        .error(R.mipmap.ic_placeholder)
                        .into(imageView);
            }
        } catch (JSONException e) {
            Log.e("getMultimedia", String.valueOf(e));
        }
        return convertView;
    }

}

