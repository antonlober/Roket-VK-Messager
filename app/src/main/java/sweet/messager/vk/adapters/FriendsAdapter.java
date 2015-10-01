package sweet.messager.vk.adapters;




import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import sweet.messager.vk.ApplicationName;
import sweet.messager.vk.R;
import sweet.messager.vk.model.Title;
import sweet.messager.vk.model.User;
import sweet.messager.vk.utils.AndroidUtils;
import sweet.messager.vk.utils.CircleTransform;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {

    public List<Object> users = new ArrayList<>();

    public FriendsAdapter(List<User> items, boolean online) {
        Log.e("allFriendsAdapter", String.valueOf(items));
        Title title = new Title();
        title.title = "ВАЖНЫЕ";
        if (!online) {
            users.add(title);
        }
        for (int i = 0; i < ((items.size() > 5) ? 5 : items.size()); i++) {
            users.add(items.get(i));
        }
        if (items.size() > 5) {
            items.subList(0, 5).clear();
            if (items.size() != 0) {
                if (!online) {
                    title.title = "ОСТАЛЬНЫЕ";
                    users.add(title);
                }
                for (int i = 0; i < items.size(); i++) {
                    users.add(items.get(i));
                }
            }
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = viewType == 1 ? R.layout.title_line : R.layout.friend;
        return new ViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(layout, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        try {
            Object item = users.get(position);
            if (item instanceof Title) {
                holder.text.setText(
                        ((Title) item).title
                );
            } else if (item instanceof User) {
                User user = (User) item;
                holder.text.setText(user.name);
                holder.time.setText(Html.fromHtml(
                        user.text
                ));
                Picasso.with(ApplicationName.getAppContext()).load(user.photo).transform(new CircleTransform()).into(holder.image);
            }
            if (position == 5) {
                holder.block.setBackgroundResource(0);
            }
            if (position == Math.abs(users.size() - 1)) {
                holder.block.setBackgroundResource(0);
            }
        } catch (NullPointerException e) {

        }
    }

    @Override
    public int getItemCount() {
        return Math.min(50, users.size());
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView image;
        TextView text, time;
        LinearLayout block;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            image = (ImageView) itemView.findViewById(R.id.photo);
            text = (TextView) itemView.findViewById(R.id.name);
            time = (TextView) itemView.findViewById(R.id.time);
            block = (LinearLayout) itemView.findViewById(R.id.block);
        }

        @Override
        public void onClick(View v) {
            int position = getPosition();
            Object type = users.get(getPosition());
            if (type instanceof User) {
                AndroidUtils.goUser((User) type);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return (users.get(position) instanceof Title) ? 1 : 0;
    }
}