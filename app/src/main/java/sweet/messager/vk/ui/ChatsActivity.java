package sweet.messager.vk.ui;

import android.app.Activity;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import sweet.messager.vk.ApplicationName;
import sweet.messager.vk.R;
import sweet.messager.vk.adapters.Chats;
import sweet.messager.vk.adapters.Drawer;
import sweet.messager.vk.db.DialogsLoader;
import sweet.messager.vk.db.Method;
import sweet.messager.vk.interfaces.FragmentListener;
import sweet.messager.vk.interfaces.VKListener;
import sweet.messager.vk.model.ChatModel;
import sweet.messager.vk.vk.Dialogs;


public class ChatsActivity extends Fragment implements Chats.OnItemClickListener {

    private FragmentListener mListener;
    private Chats adapter = null;

    private RecyclerView chats;
    private ProgressBar progressBar;
    private TextView text;
    private Toolbar toolbar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean loading = false, cache = true, update = false;
    private List<ChatModel> dialogs = new ArrayList<>();

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
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
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        super.onCreateView(layoutInflater, viewGroup, bundle);
        return layoutInflater.inflate(R.layout.chats_activity, null);
    }


    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        dialogs = Method.getDialogs();
        boolean vk = true;
        View view = getView();
        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        progressBar = (ProgressBar) view.findViewById(R.id.loading);
        text = (TextView) view.findViewById(R.id.text);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        toolbar.setTitle(getActivity().getString(R.string.connect));
        toolbar.setNavigationIcon(R.mipmap.ic_show_menu);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onFragment(100);
            }
        });
        progressBar.setVisibility(View.VISIBLE);


        if (ApplicationName.colors != null) {
            toolbar.setBackgroundColor(ApplicationName.colors.toolBarColor);
            swipeRefreshLayout.setColorSchemeColors(ApplicationName.colors.toolBarColor);
        } else {
            swipeRefreshLayout.setColorSchemeResources(R.color.mainColor);
        }

        chats = (RecyclerView) view.findViewById(R.id.chats_items);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        chats.setHasFixedSize(true);
        layoutManager.setStackFromEnd(false);
        layoutManager = new LinearLayoutManager(getActivity());
        chats.setLayoutManager(layoutManager);
        // getActivity().getSupportLoaderManager().initLoader(0, null, this);
        if (dialogs.size() != 0) {
            adapter = new Chats(
                    dialogs,
                    ChatsActivity.this
            );
            progressBar.setVisibility(View.GONE);
            text.setVisibility(View.GONE);
            chats.setVisibility(View.VISIBLE);
            chats.setAdapter(adapter);
        }
        if (vk) {
            updateDialogs(0);
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                update = true;
                updateDialogs(0);
            }
        });
        chats.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItems = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPosition();
                if (visibleItems>0 && visibleItems> adapter.items.size()-5 && !loading && !cache) {
                    int size = adapter.items.size();
                    if (ApplicationName.dialogsCount > size) {
                        updateDialogs(adapter.items.size());
                    }
                }
            }

        });
    }

    public void updateDialogs(int offset) {
        // loading = true;
        if (loading) return;
        swipeRefreshLayout.setRefreshing(true);
        loading = true;
        if (cache || update) {
            toolbar.setTitle(getActivity().getString(R.string.connect));
        }
        new Dialogs(new Dialogs.OnChatsListener() {
            @Override
            public void onChats(List<ChatModel> items) {
                loading = false;
                swipeRefreshLayout.setRefreshing(false);
                toolbar.setTitle(ApplicationName.getAppContext().getString(R.string.dialogs));
                if (items.size() == 0 && dialogs.size() != 0) {
                    return;
                }
                if (!cache && !update) {
                    adapter.addChats(items);
                } else {
                    cache = false;
                    toolbar.setTitle(ApplicationName.getAppContext().getString(R.string.dialogs));
                    adapter = new Chats(
                            items,
                            ChatsActivity.this
                    );
                    progressBar.setVisibility(View.GONE);
                    text.setVisibility(View.GONE);
                    chats.setVisibility(View.VISIBLE);
                    chats.setAdapter(adapter);
                }
                update = false;
            }
        }).execute(offset, !update ? 20 : adapter.items.size());
    }

    @Override
    public void onClick(ChatModel item) {
       // mListener.onChat(item);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        /*
        try {
            JSONArray jsonArray = adapter.getItems();
            outState.putString("json", jsonArray.toString());
            Method.putItems(adapter.getItems());
            Log.e("onSaveInstanceState", "ok");
        } catch (NullPointerException e) {
            Log.e("onSaveInstanceState", String.valueOf(e));
        }
         */
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // LayoutInflater inflater = LayoutInflater.from(getActivity());
        // populateViewForOrientation(inflater, (ViewGroup) getView());
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            adapter.onDestroy();
        } catch (NullPointerException e) {

        } catch (IllegalArgumentException e) {

        } catch (Exception e) {

        }
    }


    @Override
    public void onResume() {
        /*
        try {
            if (adapter != null) {
                adapter.onResume();
            }
        } catch (NullPointerException e) {

        } catch (IllegalArgumentException e) {

        } catch (Exception e) {

        }
         */
        super.onResume();
    }

    @Override
    public void onPause() {
        /*
        try {
            if (adapter != null) {
                adapter.onPause();
            }
        } catch (NullPointerException e) {

        } catch (IllegalArgumentException e) {

        } catch (Exception e) {

        }
         */
        super.onPause();
    }

}

