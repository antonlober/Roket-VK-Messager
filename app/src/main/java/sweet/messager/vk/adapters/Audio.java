package sweet.messager.vk.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import sweet.messager.vk.ListActivity;
import sweet.messager.vk.R;
import sweet.messager.vk.interfaces.OnRecyclerItemListener;

public class Audio extends RecyclerView.Adapter<Audio.ViewHolder> {

    Context context;
    ArrayList<JSONObject> items = new ArrayList<>();
    OnRecyclerItemListener onRecyclerItemListener;
    List<String> ids = new ArrayList<>();

    public Audio(ListActivity _listActivity, List<JSONObject> _items, String _ids) {
        context = _listActivity;
        onRecyclerItemListener = _listActivity;
        if (_ids != null && !_ids.equals("")) {
            ids = Arrays.asList(_ids.split("\\,"));
        }
        for (int i = 0; i < _items.size(); i++) {
            JSONObject item = _items.get(i);
            try {
                if (ids.indexOf(item.getString("id")) != -1) {
                    item.put("active", 1);
                    onRecyclerItemListener.onClick(i, item);
                }
                items.add(item);
            } catch (JSONException e) { }
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(context)
                .inflate(R.layout.audio_attach, null)
        );
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        try {
            JSONObject item = items.get(position);
            holder.title.setText(item.getString("title"));
            holder.artist.setText(item.getString("artist"));
            if (item.has("active")) {
                holder.plus.setImageResource(R.mipmap.ic_done_one_opacity);
                holder.plus.setBackgroundResource(R.drawable.count);
            } else {
                holder.plus.setImageResource(R.mipmap.ic_attachment);
                holder.plus.setBackgroundResource(android.R.color.transparent);
            }
        } catch (JSONException e) { }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView title, artist;
        ImageView plus;

        public ViewHolder(View _itemView) {
            super(_itemView);
            _itemView.setOnClickListener(this);
            title = (TextView) itemView.findViewById(R.id.title);
            artist = (TextView) itemView.findViewById(R.id.artist);
            plus = (ImageView) itemView.findViewById(R.id.plus);
        }

        @Override
        public void onClick(View v) {
            try {
                int position = getPosition();
                JSONObject item = items.get(position);
                if (item.has("active")) {
                    item.remove("active");
                } else {
                    item.put("active", 1);
                }
                notifyItemChanged(position);
                onRecyclerItemListener.onClick(position, item);
            } catch (JSONException e) { }
        }
    }
}
