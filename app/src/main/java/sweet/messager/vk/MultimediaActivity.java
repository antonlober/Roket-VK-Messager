package sweet.messager.vk;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import sweet.messager.vk.adapters.Audio;
import sweet.messager.vk.adapters.AudioAdapter;
import sweet.messager.vk.adapters.DocAdapter;
import sweet.messager.vk.adapters.FriendsAdapter;
import sweet.messager.vk.adapters.PhotoAdapter;
import sweet.messager.vk.adapters.VideoAdapter;
import sweet.messager.vk.db.Method;
import sweet.messager.vk.model.User;
import sweet.messager.vk.ui.ListFragment;
import sweet.messager.vk.ui.MultimediaFragment;
import sweet.messager.vk.utils.TextViewSpan;
import sweet.messager.vk.view.SlidingTabLayout;
import sweet.messager.vk.vk.Friends;
import sweet.messager.vk.vk.Multimedia;

public class MultimediaActivity extends AppCompatActivity {

    int chat, id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friends_activity);
        id = getIntent().getIntExtra("id", 0);
        chat = getIntent().getIntExtra("chat", 0);

        SlidingTabLayout slidingTabLayout = (SlidingTabLayout) findViewById(R.id.tabs);
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));
        slidingTabLayout.setDistributeEvenly(false);
        slidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return Color.parseColor("#99ffffff");
            }
        });
        slidingTabLayout.setViewPager(viewPager);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Материалы беседы");
        toolbar.setNavigationIcon(R.mipmap.ic_toolbar_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        if (ApplicationName.colors != null) {
            toolbar.setBackgroundColor(ApplicationName.colors.toolBarColor);
            slidingTabLayout.setBackgroundColor(ApplicationName.colors.toolBarColor);
            if (Build.VERSION.SDK_INT >= 21) {
                getWindow().setStatusBarColor(ApplicationName.colors.statusBarColor);
            }
        }
    }

    public class ViewPagerAdapter extends FragmentStatePagerAdapter {

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return MultimediaFragment.newInstance(position, id, chat);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            CharSequence title;
            switch (position) {
                default:
                case 0:
                    title = "ФОТО";
                    break;
                case 1:
                    title = "ВИДЕО";
                    break;
                case 2:
                    title = "АУДИО";
                    break;
                case 3:
                    title = "ДОКУМЕНТЫ";
                    break;
            }
            return title;
        }

        @Override
        public int getCount() {
            return 4;
        }
    }
}
