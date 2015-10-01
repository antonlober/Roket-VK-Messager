package sweet.messager.vk.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sweet.messager.vk.ListActivity;
import sweet.messager.vk.R;
import sweet.messager.vk.interfaces.OnRecyclerItemListener;


public class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.ViewHolder> {

    JSONArray items;

    public AudioAdapter(JSONArray _items) {
        items = _items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.audio_attach, null)
        );
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        try {
            JSONObject item = items.getJSONObject(position);
            holder.title.setText(item.getString("title"));
            holder.artist.setText(item.getString("artist"));
            holder.plus.setVisibility(View.GONE);
        } catch (JSONException e) { }
    }

    @Override
    public int getItemCount() {
        return Math.min(200, items.length());
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

        }
    }
}
