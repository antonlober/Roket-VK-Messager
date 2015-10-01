package sweet.messager.vk.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import sweet.messager.vk.ApplicationName;
import sweet.messager.vk.ListActivity;
import sweet.messager.vk.R;
import sweet.messager.vk.interfaces.OnRecyclerItemListener;
import sweet.messager.vk.utils.AndroidUtils;


public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder> {


    JSONArray items;

    public VideoAdapter(JSONArray _items) {
        items = _items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.video_attach, null)
        );
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        try {
            JSONObject item = items.getJSONObject(position);
            holder.title.setText(item.getString("title"));
            holder.duration.setText(new SimpleDateFormat("mm:ss").format(
                    new Date(Integer.valueOf(item.getInt("duration")) * 1000)
            ));
            holder.desc.setText(AndroidUtils.declOfNum(Integer.valueOf(item.getInt("views")), new String[]{
                    "просмотр",
                    "просмотра",
                    "просмотров"
            }));
            Picasso.with(ApplicationName.getAppContext()).load(item.getString(item.has("photo_320") ? "photo_320" : "photo_130")).into(holder.photo);
            holder.plus.setVisibility(View.GONE);
        } catch (JSONException e) { }
    }

    @Override
    public int getItemCount() {
        return Math.min(200, items.length());
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView plus, photo;
        TextView title, desc, duration;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            photo = (ImageView) itemView.findViewById(R.id.photo);
            title = (TextView) itemView.findViewById(R.id.title);
            desc = (TextView) itemView.findViewById(R.id.desc);
            duration = (TextView) itemView.findViewById(R.id.duration);
            plus = (ImageView) itemView.findViewById(R.id.plus);
        }

        @Override
        public void onClick(View v) {

        }
    }
}

