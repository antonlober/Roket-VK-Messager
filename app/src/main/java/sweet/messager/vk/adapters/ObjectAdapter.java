package sweet.messager.vk.adapters;



import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import sweet.messager.vk.ApplicationName;
import sweet.messager.vk.R;
import sweet.messager.vk.model.ObjectModel;
import sweet.messager.vk.model.User;
import sweet.messager.vk.utils.AndroidUtils;
import sweet.messager.vk.utils.Body;
import sweet.messager.vk.utils.CircleTransform;
import sweet.messager.vk.view.InfoBlock;

public class ObjectAdapter extends RecyclerView.Adapter<ObjectAdapter.ViewHolder> {

    List<ObjectModel> items;
    boolean chat;
    int id;

    public ObjectAdapter(List<ObjectModel> _items, boolean _chat, int _id) {
        items = _items;
        chat = _chat;
        id = _id;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = 0;
        switch (viewType) {
            case 5:
                layout = R.layout.friend;
                break;
            case 0:
                layout = R.layout.header;
                break;
            case 4:
                layout = R.layout.abyss;
                break;
            case 3:
                layout = R.layout.item_menu;
                break;
        }
        if (layout == 0) {
            LinearLayout main = new LinearLayout(parent.getContext());
            main.setOrientation(LinearLayout.HORIZONTAL);
            return new ViewHolder(main);
        } else {
            return new ViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(layout, parent, false)
            );
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ObjectModel item = items.get(position);
        LinearLayout linearLayout;
        switch (item.type) {
            case 0:
                if (ApplicationName.colors != null) {
                    holder.main.setBackgroundColor(ApplicationName.colors.toolBarColor);
                }
                break;
            case 1:
                linearLayout = (LinearLayout) holder.itemView;
                linearLayout.removeAllViews();
                linearLayout.addView(
                        InfoBlock.create(
                                item.typeIcon,
                                (item.typeText.equals("") ? "Не указано" : item.typeText),
                                item.typeTitle
                        ));
                break;
            case 2:
                linearLayout = (LinearLayout) holder.itemView;
                linearLayout.removeAllViews();
                linearLayout.addView(
                        InfoBlock.item(
                                item.typeIcon,
                                item.typeTitle
                        ));
                break;
            case 5:
                try {
                    JSONObject userVK = item.user;
                    User user = ApplicationName.getUsers(userVK.getInt("id"));

                    holder.text.setText(user.name);
                    holder.time.setText(user.text);

                    Picasso.with(ApplicationName.getAppContext()).load(user.photo_big).transform(new CircleTransform()).into(holder.image);
                    holder.block.setBackgroundResource(0);
                    holder.main.setImageResource(userVK.has("admin") ? R.mipmap.ic_group : R.drawable.transparent);
                    holder.main.setVisibility(View.VISIBLE);
                } catch (JSONException e) {

                }
                break;
        }
        /*
        switch (type) {
            case 0:
                try {
                    JSONObject userVK = items.get(position);
                    User user = ApplicationName.getUsers(userVK.getInt("id"));

                    holder.text.setText(user.name);
                    holder.time.setText(user.text);

                    Picasso.with(ApplicationName.getAppContext()).load(user.photo_big).transform(new CircleTransform()).into(holder.image);
                    holder.block.setBackgroundResource(0);
                    holder.main.setImageResource(userVK.has("admin") ? R.mipmap.ic_group : R.drawable.transparent);
                    holder.main.setVisibility(View.VISIBLE);
                } catch (JSONException e) {

                }
                break;
            case 1:
                if (ApplicationName.colors != null) {
                    holder.main.setBackgroundColor(ApplicationName.colors.toolBarColor);
                }
                // Picasso.with(ApplicationName.getAppContext()).load(photo).into(holder.main);
                break;
            case 3:
                try {
                    JSONObject menu = items.get(position);
                    if (menu.has("menu")) {
                        int typeMenu = menu.getInt("menu");
                        switch (typeMenu) {
                            case 1:
                                holder.name.setText("Уведомления");
                                break;
                            case 2:
                                holder.name.setTextColor(Color.BLACK);
                                holder.name.setText("Материалы переписки");
                                holder.icon.setImageResource(R.drawable.transparent);
                                break;
                            case 3:
                                holder.name.setText("Редактировать");
                                break;
                            case 4:
                                holder.name.setText("Добавить учасника");
                                break;
                            case 5:
                                holder.name.setText("Покинуть беседу");
                                break;
                        }
                    } else {
                        int typeMenu = items.get(position).getInt("menu_user");
                        switch (typeMenu) {
                            case 0:
                                holder.name.setTextColor(Color.parseColor("#B71C1C"));
                                holder.name.setText("Заблокировать");
                                holder.icon.setImageResource(R.drawable.transparent);
                                break;
                            case 2:
                                holder.name.setText("Материалы переписки");
                                holder.icon.setImageResource(R.drawable.transparent);
                                break;
                            case 3:
                                holder.name.setText("Редактировать");
                                break;
                            case 4:
                                holder.name.setText("Добавить учасника");
                                break;
                            case 5:
                                holder.name.setText("Покинуть беседу");
                                break;
                        }

                    }
                } catch (JSONException e) {

                }
                break;
            case 4:
                ((LinearLayout) holder.itemView).addView(
                        InfoBlock.create(
                                R.mipmap.ic_attach_doc,
                                "test",
                                "Статус"
                        ));
                break;
        }
         */
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        ObjectModel type = items.get(position);
        return type.type;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView text, time, name;
        ImageView main, image, icon;
        LinearLayout block, mainLayout;
        public ViewHolder(View itemView) {
            super(itemView);
            main = (ImageView) itemView.findViewById(R.id.main);
            image = (ImageView) itemView.findViewById(R.id.photo);
            icon = (ImageView) itemView.findViewById(R.id.icon);
            text = (TextView) itemView.findViewById(R.id.name);
            name = (TextView) itemView.findViewById(R.id.name);
            time = (TextView) itemView.findViewById(R.id.time);
            block = (LinearLayout) itemView.findViewById(R.id.block);
            mainLayout = (LinearLayout) itemView.findViewById(R.id.mainLayout);
            if (getPosition() == 0) {

            } else {
                itemView.setOnClickListener(this);
                // itemView.setBackgroundColor(R.attr.selectableItemBackground);

            }
        }

        @Override
        public void onClick(View v) {
            ObjectModel model = items.get(getPosition());
            if (model.type == 5) {
                try {
                    AndroidUtils.goUser(model.user.getInt("id"));
                } catch (JSONException e) { }
            } else if (model.type == 2) {
                switch (model.typeId) {
                    case 1:
                        AndroidUtils.goMultimedia(chat, id);
                        break;
                    case 2:

                        break;
                }
            }
        }
    }




}
