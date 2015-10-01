package sweet.messager.vk.ui;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;

import sweet.messager.vk.ApplicationName;
import sweet.messager.vk.R;
import sweet.messager.vk.adapters.AudioAdapter;
import sweet.messager.vk.adapters.DocAdapter;
import sweet.messager.vk.adapters.PhotoAdapter;
import sweet.messager.vk.adapters.VideoAdapter;
import sweet.messager.vk.db.Method;
import sweet.messager.vk.vk.Multimedia;

public class MultimediaFragment extends Fragment {

    static final String ARGUMENT_PAGE_NUMBER = "arg_page_number";
    String key;
    int id, chat, type = 0;
    RecyclerView recyclerView;
    ProgressBar progressBar;
    TextView no_data;
    GridView gridLayout;
    boolean isUpdate = false;
    SwipeRefreshLayout swipeRefreshLayout;

    public static MultimediaFragment newInstance(int type, int id, int chat) {
        MultimediaFragment pageFragment = new MultimediaFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(ARGUMENT_PAGE_NUMBER, type);
        arguments.putInt("_id", id);
        arguments.putInt("_chat", chat);
        pageFragment.setArguments(arguments);
        return pageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        type = getArguments().getInt(ARGUMENT_PAGE_NUMBER);
        id = getArguments().getInt("_id", 0);
        chat = getArguments().getInt("chat", 0);
        if (chat == 1) {
            id = ((id > 2000000000) ? Math.abs(id - 2000000000) : id);
            key = "chat" + id;
        } else {
            key = "user" + id;
        }
    }


    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        super.onCreateView(layoutInflater, viewGroup, bundle);
        return layoutInflater.inflate(R.layout.lists_page, null);
    }


    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        View view = getView();
        recyclerView = (RecyclerView) view.findViewById(R.id.friends_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(ApplicationName.getAppContext()));
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        no_data = (TextView) view.findViewById(R.id.no_data);
        gridLayout = (GridView) view.findViewById(R.id.gridview);

        if (ApplicationName.colors != null) {
            swipeRefreshLayout.setColorSchemeColors(ApplicationName.colors.toolBarColor);
        } else {
            swipeRefreshLayout.setColorSchemeResources(R.color.mainColor);
        }

        progressBar.setVisibility(View.VISIBLE);
        get();
        updateLibrary();
    }

    public void get() {
        no_data.setVisibility(View.GONE);
        String _key;
        switch (type) {
            case 1:
                _key = key + "_video";
                break;
            case 2:
                _key = key + "_audio";
                break;
            case 3:
                _key = key + "_doc";
                break;
            default:
                _key = key + "_photo";
                break;
        }
        new AsyncTask<String, Void, JSONArray>() {

            @Override
            protected JSONArray doInBackground(String... params) {
                return Method.getMultimedia(params[0]);
            }

            @Override
            protected void onPostExecute(JSONArray items) {
                if (isUpdate || type != 0) {
                    progressBar.setVisibility(View.GONE);
                }
                if (items.length() != 0) {
                    progressBar.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    gridLayout.setVisibility(View.GONE);
                    swipeRefreshLayout.setVisibility(View.VISIBLE);
                    switch (type) {
                        case 2:
                            recyclerView.setAdapter(new AudioAdapter(items));
                            break;
                        case 3:
                            recyclerView.setAdapter(new DocAdapter(items));
                            break;
                        case 1:
                            recyclerView.setAdapter(new VideoAdapter(items));
                            break;
                        default:
                            Log.e("getMultimedia", "visible Photos");
                            recyclerView.setVisibility(View.GONE);
                            gridLayout.setVisibility(View.VISIBLE);
                            swipeRefreshLayout.setVisibility(View.GONE);
                            gridLayout.setAdapter(new PhotoAdapter(items));
                            break;
                    }
                } else if (isUpdate || type != 0) {
                    gridLayout.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.GONE);
                    no_data.setVisibility(View.VISIBLE);
                }
            }
        }.execute(_key);
    }

    public void updateLibrary() {
        if (type == 0) {
            new Multimedia(new Multimedia.OnUpdateListener() {
                @Override
                public void onUpdate() {
                    isUpdate = true;
                    get();
                }
            }).execute(chat, id);
        }
    }
}
