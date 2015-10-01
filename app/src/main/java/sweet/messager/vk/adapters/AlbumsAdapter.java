package sweet.messager.vk.adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

import sweet.messager.vk.ApplicationName;
import sweet.messager.vk.R;
import sweet.messager.vk.model.Album;


public class AlbumsAdapter extends BaseAdapter {

    List<Album> items;
    public AlbumsAdapter(List<Album> _items) {
        items = _items;
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
        return Integer.valueOf(items.get(position).bucket);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_view, parent, false);
        }
        Album item = items.get(position);
        TextView title = (TextView) convertView.findViewById(R.id.title);
        TextView count = (TextView) convertView.findViewById(R.id.count);
        ImageView cover = (ImageView) convertView.findViewById(R.id.cover);
        title.setText(item.title);
        count.setText(String.valueOf(
                item.count
        ));
        Picasso picasso = Picasso.with(ApplicationName.getAppContext());
        if (item.vk) {
            picasso.load(item.uri).into(cover);
        } else {
            picasso.load(new File(item.uri)).into(cover);
        }
        return convertView;
    }

}