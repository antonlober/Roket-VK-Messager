package sweet.messager.vk;

import android.content.Intent;
import android.graphics.Point;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import java.util.HashMap;

import sweet.messager.vk.adapters.Drawer;
import sweet.messager.vk.interfaces.ClickMenu;
import sweet.messager.vk.interfaces.FragmentListener;
import sweet.messager.vk.model.ChatModel;
import sweet.messager.vk.services.LongPoll;
import sweet.messager.vk.ui.ChatsActivity;
import sweet.messager.vk.ui.FriendsActivity;
import sweet.messager.vk.ui.SettingsActivity;
import sweet.messager.vk.utils.AndroidUtils;
import sweet.messager.vk.vk.VKApi;


public class BaseActivity extends FragmentActivity implements FragmentListener {

    DrawerLayout drawer_layout;
    Drawer drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        ApplicationName.baseActivity = this;
        if (getSharedPreferences(getPackageName(), MODE_PRIVATE).getBoolean("isLogin", false)) {
            VKApi.addStats();
            if (ApplicationName.colors != null && Build.VERSION.SDK_INT >= 21) {
                Window window = getWindow();
                window.setStatusBarColor(ApplicationName.colors.statusBarColor);
            }
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            int height, width;
            try {
                display.getRealSize(size);
                height = size.y;
                width = size.x;
            } catch (NoSuchMethodError e) {
                height = display.getHeight();
                width = display.getWidth();
            }
            ApplicationName.screenSize = new int[] {
                    width,
                    height
            };

            drawer_layout = (DrawerLayout) findViewById(R.id.drawer_layout);
            RecyclerView drawerMenu = (RecyclerView) findViewById(R.id.lv_navigation_drawer);
            drawerMenu.setHasFixedSize(true);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            drawerMenu.setLayoutManager(layoutManager);
            drawer = new Drawer(new ClickMenu() {
                @Override
                public void onClick(int type) {
                    if (type == 3) {
                        AndroidUtils.goUser(308205829);
                    }
                    Fragment fragment;
                    switch (type) {
                        case 1:
                            fragment = new FriendsActivity();
                            break;
                        case 2:
                            fragment = new SettingsActivity();
                            break;
                        default:
                            fragment = new ChatsActivity();
                            break;
                    }
                    drawer_layout.closeDrawer(Gravity.START);
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment, fragment)
                            .commit();
                }
            });

            width = Math.abs(ApplicationName.screenSize[0] - AndroidUtils.dp(70));
            if (width > AndroidUtils.dp(290)) {
                width = AndroidUtils.dp(290);
            }
            DrawerLayout.LayoutParams params = (android.support.v4.widget.DrawerLayout.LayoutParams) drawerMenu.getLayoutParams();
            params.width = width;
            drawerMenu.setLayoutParams(params);
            drawerMenu.setAdapter(drawer);
            main();
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }
    }


    public void main() {
        startService(new Intent(BaseActivity.this, LongPoll.class));
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, new ChatsActivity()).commit();
    }

    @Override
    public void onFragment(int event) {
        try {
            switch (event) {
                case 0:
                    main();
                    break;
                case 100:
                    drawer_layout.openDrawer(Gravity.LEFT);
                    break;
                case 200:
                    drawer.notifyItemChanged(0);
                    break;
            }
        } catch (NullPointerException e) { }
    }

    @Override
    public void onChat(HashMap<String, Object> item) {
        /*Intent intent = new Intent(this, ImActivity.class);
        intent.putExtra("photo", (item.no_photo ? null : item.photo));
        startActivity(intent);

        chatActivity = ChatActivity.newInstance(item);
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right)
                .add(R.id.fragment, chatActivity)
                .commit();
         */
    }

    @Override
    public void onBackPressed() {
        /*
        if (chatActivity != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right)
                    .remove(chatActivity)
                    .commit();
            chatActivity = null;
        }
         */
    }

    @Override
    protected void onResume() {
        super.onResume();
        ApplicationName.baseActivity = BaseActivity.this;
    }

}
