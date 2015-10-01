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


import sweet.messager.vk.ApplicationName;
import sweet.messager.vk.R;
import sweet.messager.vk.utils.AndroidUtils;


public class DocAdapter extends RecyclerView.Adapter<DocAdapter.ViewHolder> {

    JSONArray items;

    public DocAdapter(JSONArray _items) {
        items = _items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.doc_attach, null)
        );
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        try {
            JSONObject item = items.getJSONObject(position);

            if (!item.has("photo_100")) {
                holder.image.setVisibility(View.GONE);
                holder.ext.setVisibility(View.VISIBLE);
                holder.ext.setText(item.getString("ext"));
            } else {
                String image = item.getString(item.has("photo_130") ? "photo_130" : "photo_100");
                holder.image.setVisibility(View.VISIBLE);
                holder.ext.setVisibility(View.GONE);
                Picasso.with(ApplicationName.getAppContext()).load(image).into(holder.image);
            }
            holder.title.setText(item.getString("title"));
            holder.text.setText(item.getString("ext") + ", " + AndroidUtils.humanReadableByteCount(Integer.valueOf(
                    item.getInt("size")
            ), true));
            holder.plus.setVisibility(View.GONE);
        } catch (JSONException e) { }
    }

    @Override
    public int getItemCount() {
        return Math.min(200, items.length());
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

        }
    }
}
