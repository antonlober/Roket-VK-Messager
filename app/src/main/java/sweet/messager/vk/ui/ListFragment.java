package sweet.messager.vk.ui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.Voice;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import sweet.messager.vk.ApplicationName;
import sweet.messager.vk.R;
import sweet.messager.vk.adapters.Chats;
import sweet.messager.vk.adapters.FriendsAdapter;
import sweet.messager.vk.db.Method;
import sweet.messager.vk.interfaces.VKListener;
import sweet.messager.vk.model.ChatModel;
import sweet.messager.vk.model.User;
import sweet.messager.vk.vk.Friends;
import sweet.messager.vk.vk.VKApi;


public class ListFragment extends Fragment implements VKListener {

    static final String ARGUMENT_PAGE_NUMBER = "arg_page_number";
    int pageNumber;
    private FriendsAdapter friendsAdapter = null;
    private ProgressBar progressBar;
    private TextView no_data;
    private RecyclerView recyclerView;
    private boolean online = false, loading = false;
    private SwipeRefreshLayout swipeRefreshLayout;

    static ListFragment newInstance(int page) {
        ListFragment pageFragment = new ListFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(ARGUMENT_PAGE_NUMBER, page);
        pageFragment.setArguments(arguments);
        return pageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageNumber = getArguments().getInt(ARGUMENT_PAGE_NUMBER);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /* Main
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        RelativeLayout main = new RelativeLayout(container.getContext());
        main.setLayoutParams(params);

         Swipe
        SwipeRefreshLayout refreshLayout = new SwipeRefreshLayout(container.getContext());
        refreshLayout.setLayoutParams(params);
        if (ApplicationName.colors != null) {
            refreshLayout.setColorSchemeColors(ApplicationName.colors.toolBarColor);
        } else {
            refreshLayout.setColorSchemeResources(R.color.mainColor);
        }

         List
        RecyclerView recyclerView = new RecyclerView(container.getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        main.addView(refreshLayout, params);

         ProgressBar
        params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        ProgressBar progressBar = new ProgressBar(container.getContext());
        progressBar.setVisibility(View.GONE);
        main.addView(progressBar, params);

         No Data
        TextView noData = new TextView(container.getContext());
        noData.setVisibility(View.GONE);
        noData.setText("Нет данных");
        main.addView(noData, params); */
        return inflater.inflate(R.layout.lists_page, container, false);
    }


    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        online = pageNumber == 1;
        View view = getView();
        recyclerView = (RecyclerView) view.findViewById(R.id.friends_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        no_data = (TextView) view.findViewById(R.id.no_data);

        if (ApplicationName.colors != null) {
            swipeRefreshLayout.setColorSchemeColors(ApplicationName.colors.toolBarColor);
        } else {
            swipeRefreshLayout.setColorSchemeResources(R.color.mainColor);
        }

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (pageNumber != 2) {
                    int visibleItems = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPosition();
                    if (visibleItems>0 && visibleItems> friendsAdapter.users.size()-5 && !loading) {
                        int size = friendsAdapter.users.size();
                        if (ApplicationName.friendsCount > size) {
                            updateFriends(size);
                        }
                    }
                }
            }

        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!loading && pageNumber != 2) {
                    new Friends(ListFragment.this, online).execute();
                } else {
                    new executeAllChats().execute();
                }
            }
        });

        if (pageNumber == 2) {
            List<ChatModel> chats = Method.getAllChats();
            if (chats.size() == 0) {
                progressBar.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                recyclerView.setAdapter(new Chats(chats, new Chats.OnItemClickListener() {
                    @Override
                    public void onClick(ChatModel item) {

                    }
                }));
            }
            new executeAllChats().execute();
            loading = true;
        } else {
            List<User> friends = Method.getFriends(null);
            if (friends.size() == 0) {
                progressBar.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                friendsAdapter = new FriendsAdapter(friends, online);
                recyclerView.setAdapter(friendsAdapter);
            }
            new Friends(this, online).execute();
            loading = true;
        }
    }

    private void updateFriends(int offset) {

    }

    @Override
    public void result(Object res) {
        swipeRefreshLayout.setRefreshing(false);
        progressBar.setVisibility(View.GONE);
        try {
            List<User> friends;
            if (!online) {
                friends = Method.getFriends(null);
                if (friends.size() != 0) {
                    recyclerView.setVisibility(View.VISIBLE);
                    friendsAdapter = new FriendsAdapter(friends, online);
                    recyclerView.setAdapter(friendsAdapter);
                }
            } else {
                friends = Method.getFriends(TextUtils.join(",", ApplicationName.onlineUsers));
                if (friends.size() != 0) {
                    recyclerView.setVisibility(View.VISIBLE);
                    friendsAdapter = new FriendsAdapter(friends, online);
                    recyclerView.setAdapter(friendsAdapter);
                }
            }
            if (friends.size() == 0) {
                recyclerView.setVisibility(View.GONE);
                no_data.setVisibility(View.VISIBLE);
            }
        } catch (NullPointerException e) {
            recyclerView.setVisibility(View.GONE);
            no_data.setVisibility(View.VISIBLE);
        }
        loading = false;
    }


    class executeAllChats extends AsyncTask<Void, Void, List<ChatModel>> {

        @Override
        protected List<ChatModel> doInBackground(Void... params) {
            return VKApi.getAllDialog();
        }

        @Override
        protected void onPostExecute(List<ChatModel> items) {
            super.onPostExecute(items);
            swipeRefreshLayout.setRefreshing(false);
            progressBar.setVisibility(View.GONE);
            if (items.size() == 0) {
                recyclerView.setVisibility(View.GONE);
                no_data.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                recyclerView.setAdapter(new Chats(items, new Chats.OnItemClickListener() {
                    @Override
                    public void onClick(ChatModel item) {

                    }
                }));
            }
        }
    }
}
