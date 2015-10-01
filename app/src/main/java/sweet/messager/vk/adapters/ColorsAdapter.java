package sweet.messager.vk.adapters;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;

import sweet.messager.vk.ApplicationName;
import sweet.messager.vk.R;
import sweet.messager.vk.interfaces.OnRecyclerItemListener;
import sweet.messager.vk.model.ColorModel;


public class ColorsAdapter extends RecyclerView.Adapter<ColorsAdapter.ViewHolder> {

    ArrayList<ColorModel> colors = new ArrayList<>();
    OnRecyclerItemListener onRecyclerItemListener;
    int activePosition = 2;

    public ColorsAdapter(OnRecyclerItemListener _onRecyclerItemListener) {
        ColorModel color;
        for (int i = 0; i < 12; i++) {
            color = new ColorModel();
            switch (i) {
                case 0:
                    color.statusBarColor = Color.parseColor("#212121");
                    color.toolBarColor = Color.parseColor("#424242");
                    color.textColor = Color.parseColor("#030303");
                    color.chatBg = Color.parseColor("#CFCFCF");
                    break;
                case 1:
                    color.statusBarColor = Color.parseColor("#3E2723");
                    color.toolBarColor = Color.parseColor("#4E342E");
                    color.textColor = Color.parseColor("#A47165");
                    color.chatBg = Color.parseColor("#D7C1BC");
                    break;
                case 2: // Main Color
                    color.statusBarColor = Color.parseColor("#435e7e");
                    color.toolBarColor = Color.parseColor("#ff54759e");
                    color.textColor = Color.parseColor("#3E90CF");
                    color.chatBg = Color.parseColor("#D1DCE2");
                    break;
                case 3:
                    color.statusBarColor = Color.parseColor("#BF360C");
                    color.toolBarColor = Color.parseColor("#D84315");
                    color.textColor = Color.parseColor("#ED7550");
                    color.chatBg = Color.parseColor("#FBCBBC");
                    break;
                case 4:
                    color.statusBarColor = Color.parseColor("#263238");
                    color.toolBarColor = Color.parseColor("#37474F");
                    color.textColor = Color.parseColor("#668493");
                    color.chatBg = Color.parseColor("#C3CFD5");
                    break;
                case 5:
                    color.statusBarColor = Color.parseColor("#1B5E20");
                    color.toolBarColor = Color.parseColor("#2E7D32");
                    color.textColor = Color.parseColor("#59B15D");
                    color.chatBg = Color.parseColor("#C3EAC5");
                    break;
                case 6:
                    color.statusBarColor = Color.parseColor("#01579B");
                    color.toolBarColor = Color.parseColor("#0091EA");
                    color.textColor = Color.parseColor("#4DBBFE");
                    color.chatBg = Color.parseColor("#B3E2FF");
                    break;
                case 7:
                    color.statusBarColor = Color.parseColor("#004D40");
                    color.toolBarColor = Color.parseColor("#00695C");
                    color.textColor = Color.parseColor("#01A793");
                    color.chatBg = Color.parseColor("#CCFAF4");
                    break;
                case 8:
                    color.statusBarColor = Color.parseColor("#D50000");
                    color.toolBarColor = Color.parseColor("#F44336");
                    color.textColor = Color.parseColor("#F86D63");
                    color.chatBg = Color.parseColor("#FBCECB");
                    break;
                case 9:
                    color.statusBarColor = Color.parseColor("#1A237E");
                    color.toolBarColor = Color.parseColor("#3F51B5");
                    color.textColor = Color.parseColor("#6070C7");
                    color.chatBg = Color.parseColor("#E0E3F5");
                    break;
                case 10:
                    color.statusBarColor = Color.parseColor("#006064");
                    color.toolBarColor = Color.parseColor("#00BCD4");
                    color.textColor = Color.parseColor("#00D5F0");
                    color.chatBg = Color.parseColor("#BDF7FF");
                    break;
                case 11:
                    color.statusBarColor = Color.parseColor("#4A148C");
                    color.toolBarColor = Color.parseColor("#9C27B0");
                    color.textColor = Color.parseColor("#BE39D5");
                    color.chatBg = Color.parseColor("#F2D9F7");
                    break;
                case 12:
                    color.statusBarColor = Color.parseColor("#880E4F");
                    color.toolBarColor = Color.parseColor("#E91E63");
                    color.textColor = Color.parseColor("#ED4A81");
                    color.chatBg = Color.parseColor("#FBD0DF");
                    break;
            }
            if (ApplicationName.colors != null && ApplicationName.colors.toolBarColor == color.toolBarColor) {
                activePosition = i;
            }
            colors.add(color);
        }
        onRecyclerItemListener = _onRecyclerItemListener;
    }

    @Override
    public ColorsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.color, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(ColorsAdapter.ViewHolder holder, int position) {
        ColorModel colorModel = colors.get(position);
        holder.main.setBackgroundColor(colorModel.toolBarColor);
        if (activePosition == position) {
            holder.icon.setVisibility(View.VISIBLE);
        } else {
            holder.icon.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return colors.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        LinearLayout main;
        ImageView icon;
        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            main = (LinearLayout) itemView.findViewById(R.id.main);
            icon = (ImageView) itemView.findViewById(R.id.icon);
        }

        @Override
        public void onClick(View v) {
            int position = getPosition();
            ColorModel colorModel = colors.get(position);
            onRecyclerItemListener.onClick(position, colorModel);
            int prevPosition = activePosition;
            activePosition = position;
            notifyItemChanged(position);
            notifyItemChanged(prevPosition);
        }
    }
}
