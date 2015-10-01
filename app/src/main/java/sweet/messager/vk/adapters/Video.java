package sweet.messager.vk.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import sweet.messager.vk.ListActivity;
import sweet.messager.vk.R;
import sweet.messager.vk.interfaces.OnRecyclerItemListener;
import sweet.messager.vk.utils.AndroidUtils;

public class Video extends RecyclerView.Adapter<Video.ViewHolder> {


    Context context;
    List<JSONObject> items = new ArrayList<>();
    OnRecyclerItemListener onRecyclerItemListener;
    List<String> ids = new ArrayList<>();

    public Video(ListActivity _listActivity, List<JSONObject> _items, String _ids) {
        context = _listActivity;
        onRecyclerItemListener = _listActivity;
        if (_ids != null && !_ids.equals("")) {
            ids = Arrays.asList(_ids.split("\\,"));
        }
        for (int i = 0; i < _items.size(); i++) {
            try {
                JSONObject item = _items.get(i);
                if (ids.indexOf(item.get("id")) != -1) {
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
                        .inflate(R.layout.video_attach, null)
        );
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        try {
            JSONObject item = items.get(position);
            holder.title.setText(item.getString("title"));
            holder.duration.setText(new SimpleDateFormat("mm:ss").format(
                    new Date(Integer.valueOf(item.getInt("duration")) * 1000)
            ));
            holder.desc.setText(AndroidUtils.declOfNum(Integer.valueOf(item.getInt("views")), new String[]{
                    "просмотр",
                    "просмотра",
                    "просмотров"
            }));
            Picasso.with(context).load(item.getString(item.has("photo_320") ? "photo_320" : "photo_130")).into(holder.photo);
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
