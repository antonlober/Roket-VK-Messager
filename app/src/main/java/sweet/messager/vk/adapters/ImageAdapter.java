package sweet.messager.vk.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import sweet.messager.vk.ApplicationName;
import sweet.messager.vk.R;


public class ImageAdapter extends BaseAdapter {

    List<JSONObject> items = new ArrayList<>();
    boolean vk;
    public ImageAdapter(List<JSONObject> _items, boolean _vk) {
        if (_items == null) {
            JSONObject photo = new JSONObject();
            for (int i = 0; i < 20; i++) {
                items.add(photo);
            }
        } else {
            items = _items;
        }
        vk = _vk;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_select, parent, false);
        }
        ImageView imageView = (ImageView) convertView.findViewById(R.id.image);
        JSONObject item = items.get(position);
        if (item.has("photo_130") || item.has("thumb")) {
            try {
                String img = null;
                if (!vk && item.has("thumb")) {
                    img = item.getString("thumb");
                } else if (vk && item.has("photo_130")) {
                    img = item.getString("photo_130");
                }
                if (img == null) {
                    imageView.setImageResource(R.mipmap.ic_placeholder);
                } else {
                    Picasso picasso = Picasso.with(ApplicationName.getAppContext());
                    RequestCreator requestCreator;
                    if (vk) {
                        requestCreator = picasso.load(img);
                    } else {
                        requestCreator = picasso.load(new File(img));
                    }
                    requestCreator.placeholder(R.mipmap.ic_placeholder)
                            .error(R.mipmap.ic_placeholder)
                            .into(imageView);
                }
            } catch (JSONException e) { }
        }
        return convertView;
    }

}
