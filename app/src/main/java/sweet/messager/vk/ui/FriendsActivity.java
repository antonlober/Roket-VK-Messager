package sweet.messager.vk.ui;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import sweet.messager.vk.ApplicationName;
import sweet.messager.vk.R;
import sweet.messager.vk.adapters.FriendsAdapter;
import sweet.messager.vk.api.Async;
import sweet.messager.vk.api.VKException;
import sweet.messager.vk.db.Method;
import sweet.messager.vk.interfaces.FragmentListener;
import sweet.messager.vk.interfaces.VKListener;
import sweet.messager.vk.model.User;
import sweet.messager.vk.view.SlidingTabLayout;
import sweet.messager.vk.vk.Friends;


public class FriendsActivity extends Fragment {

    private FragmentListener mListener;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        super.onCreateView(layoutInflater, viewGroup, bundle);
        return layoutInflater.inflate(R.layout.friends_activity, null);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof FragmentListener) {
            mListener = (FragmentListener) activity;
        } else {
            throw new ClassCastException(activity.toString() + " must implement MyListFragment.OnItemSelectedListener");
        }
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        View view = getView();
        SlidingTabLayout slidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.tabs);
        ViewPager viewPager = (ViewPager) view.findViewById(R.id.pager);
        viewPager.setAdapter(new ViewPagerAdapter(getActivity().getSupportFragmentManager()));
        slidingTabLayout.setDistributeEvenly(true);
        slidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return Color.parseColor("#99ffffff");
            }
        });
        slidingTabLayout.setViewPager(viewPager);
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setTitle("Друзья");
        toolbar.setNavigationIcon(R.mipmap.ic_show_menu);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onFragment(100);
            }
        });
        if (ApplicationName.colors != null) {
            toolbar.setBackgroundColor(ApplicationName.colors.toolBarColor);
            slidingTabLayout.setBackgroundColor(ApplicationName.colors.toolBarColor);
        }
    }


    public class ViewPagerAdapter extends FragmentStatePagerAdapter {

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return ListFragment.newInstance(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            CharSequence title;
            switch (position) {
                default:
                case 0:
                    title = "ВСЕ";
                    break;
                case 1:
                    title = "ОНЛАЙН";
                    break;
                case 2:
                    title = "БЕСЕДЫ";
                    break;
            }
            return title;
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}


