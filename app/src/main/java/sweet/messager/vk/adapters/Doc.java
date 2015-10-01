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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import sweet.messager.vk.ListActivity;
import sweet.messager.vk.R;
import sweet.messager.vk.interfaces.OnRecyclerItemListener;
import sweet.messager.vk.utils.AndroidUtils;

public class Doc extends RecyclerView.Adapter<Doc.ViewHolder> {

    Context context;
    List<JSONObject> items = new ArrayList<>();
    OnRecyclerItemListener onRecyclerItemListener;
    List<String> ids = new ArrayList<>();

    public Doc(ListActivity _listActivity, List<JSONObject> _items, String _ids) {
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
                .inflate(R.layout.doc_attach, null)
        );
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        try {
            JSONObject item = items.get(position);

            if (!item.has("photo_100")) {
                holder.image.setVisibility(View.GONE);
                holder.ext.setVisibility(View.VISIBLE);
                holder.ext.setText(item.getString("ext"));
            } else {
                String image = item.getString(item.has("photo_130") ? "photo_130" : "photo_100");
                holder.image.setVisibility(View.VISIBLE);
                holder.ext.setVisibility(View.GONE);
                Picasso.with(context).load(image).into(holder.image);
            }
            holder.title.setText(item.getString("title"));
            holder.text.setText(item.getString("ext") + ", " + AndroidUtils.humanReadableByteCount(Integer.valueOf(
                    item.getInt("size")
            ), true));

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

        ImageView plus, image;
        TextView text, ext, title;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            image = (ImageView) itemView.findViewById(R.id.image);
            text = (TextView) itemView.findViewById(R.id.text);
            ext = (TextView) itemView.findViewById(R.id.ext);
            title = (TextView) itemView.findViewById(R.id.title);
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