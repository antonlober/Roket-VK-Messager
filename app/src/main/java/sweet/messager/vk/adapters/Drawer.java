package sweet.messager.vk.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import java.io.File;
import sweet.messager.vk.ApplicationName;
import sweet.messager.vk.R;
import sweet.messager.vk.interfaces.ClickMenu;
import sweet.messager.vk.model.User;
import sweet.messager.vk.utils.ActionMenu;
import sweet.messager.vk.utils.CircleTransform;

public class Drawer extends RecyclerView.Adapter<Drawer.ViewHolder> {

    Menu menu;
    ClickMenu clickMenu;

    public Drawer(ClickMenu _clickMenu) {
        clickMenu = _clickMenu;
        Context context = ApplicationName.getAppContext();
        menu = new ActionMenu(context);
        new MenuInflater(context).inflate(R.menu.drawer, menu);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int id;
        switch (viewType) {
            case 1:
                id = R.layout.mini_profile;
                break;
            default:
                id = R.layout.item_menu;
                break;
        }
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(id, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (position == 0) {
            User user = ApplicationName.getUsers(ApplicationName.getUserId());
            holder.name.setText(user.name);
            Picasso.with(ApplicationName.getAppContext()).load(user.photo_big).transform(new CircleTransform()).into(holder.photo);
            holder.text.setText(user.status);
            if (ApplicationName.colors != null) {
                holder.icon.setBackgroundColor(ApplicationName.colors.textColor);
            }
            if (ApplicationName.bgMsg != null) {
                Picasso picasso = Picasso.with(ApplicationName.getAppContext());
                RequestCreator requestCreator;
                if (ApplicationName.vkBgMsg) {
                    requestCreator = picasso.load(ApplicationName.bgMsg);
                } else {
                    requestCreator = picasso.load(new File(ApplicationName.bgMsg));
                }
                requestCreator.into(holder.icon);
            } else {
            }
        } else {
            position--;
            MenuItem item = menu.getItem(position);
            holder.icon.setImageDrawable(item.getIcon());
            holder.name.setText(item.getTitle());
        }
    }

    @Override
    public int getItemCount() {
        return menu.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? 1 : 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView name, text;
        ImageView icon, photo;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            icon = (ImageView) itemView.findViewById(R.id.icon);
            photo = (ImageView) itemView.findViewById(R.id.photo);
            name = (TextView) itemView.findViewById(R.id.name);
            text = (TextView) itemView.findViewById(R.id.text);
        }

        @Override
        public void onClick(View v) {
            clickMenu.onClick(
                    Math.abs(getPosition() - 1)
            );
        }
    }
}
