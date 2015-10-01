package sweet.messager.vk.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import sweet.messager.vk.R;


/**
 * Created by antonpolstyanka on 28.04.15.
 */
public class CreateMenu {

    public interface OnItemClickListener {
        void onItemClick(int id);
    }

    LinearLayout items;
    Context context;
    OnItemClickListener onItemClickListener;

    public CreateMenu(Context _context, View view, int id, OnItemClickListener _onItemClickListener) {
        onItemClickListener = _onItemClickListener;
        context = _context;
        items = (LinearLayout) view.findViewById(R.id.items);
        items.removeAllViews();
        Menu menu = new ActionMenu(context);
        new MenuInflater(context).inflate(id, menu);
        for (int i = 0; i < menu.size(); i++) {
            try {
                addItem(menu.getItem(i));
            } catch (IndexOutOfBoundsException e) {

            }
        }
    }

    public void addItem(final MenuItem item) {
        try {
            View itemView = LayoutInflater.from(context).inflate(R.layout.item_menu, null);
            ((ImageView) itemView.findViewById(R.id.icon))
                    .setImageDrawable(item.getIcon());
            ((TextView) itemView.findViewById(R.id.name))
                    .setText(item.getTitle());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick(item.getItemId());
                }
            });
            items.addView(itemView);
        } catch (IndexOutOfBoundsException e) {

        }
    }


}

