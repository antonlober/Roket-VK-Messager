package sweet.messager.vk.view;


import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import sweet.messager.vk.ApplicationName;
import sweet.messager.vk.R;
import sweet.messager.vk.utils.AndroidUtils;

public class InfoBlock {

    public static int defaultIcon = R.drawable.transparent;

    public static View create(Context context, int icon, String title, String text) {
        int padding = AndroidUtils.dp(16);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(AndroidUtils.dp(64), AndroidUtils.dp(40));
        LinearLayout main = new LinearLayout(context);
        main.setPadding(padding, padding, padding, padding);
        main.setOrientation(LinearLayout.HORIZONTAL);
        ImageView imageView = new ImageView(context);
        imageView.setImageResource(icon);
        padding = AndroidUtils.dp(10);
        imageView.setPadding(padding, padding, AndroidUtils.dp(22), padding);
        main.addView(imageView, params);

        LinearLayout info = new LinearLayout(context);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setPadding(AndroidUtils.dp(6), 0, 0, 0);

        TextView textView = new TextView(context);
        textView.setSingleLine();
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        textView.setText(title);
        textView.setTextColor(0xff000000);
        info.addView(textView);


        textView = new TextView(context);
        textView.setSingleLine();
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        textView.setText(text);
        textView.setPadding(0, AndroidUtils.dp(4), 0, 0);
        textView.setTextColor(Color.parseColor("#8A8A8A"));
        info.addView(textView);

        main.addView(info);
        return main;
    }

    public static View create(int icon, String title, String text) {
        return create(ApplicationName.getAppContext(), icon, title, text);
    }


    public static View item(Context context, int icon, String title) {
        int padding = AndroidUtils.dp(16);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, AndroidUtils.dp(52));
        LinearLayout main = new LinearLayout(context);
        main.setPadding(0, padding, padding, padding);
        main.setOrientation(LinearLayout.HORIZONTAL);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            TypedValue outValue = new TypedValue();
            ApplicationName.getAppContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            main.setBackgroundResource(outValue.resourceId);
        }
        main.setLayoutParams(params);


        params = new LinearLayout.LayoutParams(AndroidUtils.dp(24), AndroidUtils.dp(20));
        params.setMargins(AndroidUtils.dp(30), 0, 0, 0);
        ImageView imageView = new ImageView(context);
        imageView.setImageResource(icon);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        main.addView(imageView, params);

        TextView textView = new TextView(context);
        textView.setSingleLine();
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        textView.setText(title);
        textView.setPadding(AndroidUtils.dp(6), 0, 0, 0);
        textView.setTextColor(0xff000000);

        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(AndroidUtils.dp(24), 0, 0, 0);
        textView.setLayoutParams(params);

        main.addView(textView);

        return main;
    }

    public static View item(int icon, String title) {
        return item(ApplicationName.getAppContext(), icon, title);
    }
}
