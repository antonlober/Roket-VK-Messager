package sweet.messager.vk;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sweet.messager.vk.adapters.Audio;
import sweet.messager.vk.adapters.Doc;
import sweet.messager.vk.adapters.Video;
import sweet.messager.vk.api.Async;
import sweet.messager.vk.api.VK;
import sweet.messager.vk.api.VKException;
import sweet.messager.vk.db.Method;
import sweet.messager.vk.interfaces.OnRecyclerItemListener;


public class ListActivity extends ActionBarActivity implements OnRecyclerItemListener {

    int type, toolBarColor, statusBarColor;
    RecyclerView list;
    List<JSONObject> items = new ArrayList<>();
    String ids, title;
    Toolbar toolbar;
    ProgressBar progressBar;
    TextView textView;
    Window window;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        window = getWindow();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        list = (RecyclerView) findViewById(R.id.list_view_items);
        progressBar = (ProgressBar) findViewById(R.id.progressBar2);
        textView = (TextView) findViewById(R.id.textView4);
        Intent intent = getIntent();
        type = intent.getIntExtra("type", 0);

        if (ApplicationName.colors != null) {
            toolBarColor = ApplicationName.colors.toolBarColor;
            statusBarColor = ApplicationName.colors.statusBarColor;
            if (Build.VERSION.SDK_INT >= 21) {
                window.setStatusBarColor(statusBarColor);
            }
            toolbar.setBackgroundColor(toolBarColor);
        } else {
            toolBarColor = Color.parseColor("#ff54759e");
            statusBarColor = Color.parseColor("#435e7e");
        }

        ids = intent.hasExtra("ids") ? intent.getStringExtra("ids") : "";
        toolbar.setNavigationIcon(R.mipmap.ic_toolbar_back);
        final String[] titles = new String[] {
                "Аудиозаписи",
                "Видеозаписи",
                "Документы"
        };
        String[] baseName = new String[] {
                "audio",
                "video",
                "doc"
        };
        final String sqlKey = baseName[type];
        title = titles[type];
        toolbar.setTitle(title);
        toolbar.inflateMenu(R.menu.menu_list);
        toolbar.getMenu().findItem(R.id.action_attach_items).setVisible(false);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_attach_items:
                        int size = items.size();
                        if (size != 0) {
                            JSONArray jsonArray = new JSONArray();
                            JSONObject jsonObject;
                            JSONObject object;
                            for (int i = 0; i < size; i++) {
                                try {
                                    jsonObject = new JSONObject();
                                    object = items.get(i);
                                    jsonObject.put("type", sqlKey);
                                    switch (type) {
                                        case Constants.AUDIO:
                                            jsonObject.put("audio", object);
                                            break;
                                        case Constants.VIDEO:
                                            jsonObject.put("video", object);
                                            break;
                                        case Constants.DOC:
                                            jsonObject.put("doc", object);
                                            break;
                                    }
                                    jsonArray.put(jsonObject);
                                } catch (JSONException e) {

                                }
                            }
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("jsonArray", jsonArray.toString());
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        }
                        break;
                }
                return true;
            }
        });
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        list.setLayoutManager(new LinearLayoutManager(this));


        List<JSONObject> _items = Method.getObject(sqlKey);
        if (_items.size() != 0) {
            result(_items);
        }
        new Async() {

            @Override
            protected Object background() throws VKException {
                HashMap<String, Object> post = new HashMap<>();
                post.put("count", 25);
                String method = "audio.get";
                switch (type) {
                    case Constants.VIDEO:
                        method = "video.get";
                        break;
                    case Constants.DOC:
                        method = "docs.get";
                        break;
                }
                return new VK(ListActivity.this).method(method).params(post).getObject();
            }

            @Override
            protected void error(VKException error) {

            }

            @Override
            protected void finish(Object json) {
                JSONObject response = (JSONObject) json;
                try {
                    JSONArray items = response.getJSONArray("items");
                    result(Method.addObject(sqlKey, items));
                } catch (JSONException e) {

                }
            }

            @Override
            protected void start() {

            }
        }.execute();
    }

    @Override
    public void onClick(int position, Object object) {
        int size = items.size();
        JSONObject item = (JSONObject) object;
        if (items.indexOf(item) == -1) {
            if (size > 9) return;
            items.add(item);
            size++;
        } else {
            items.remove(item);
            size--;
        }
        if (size == 0) {
            toolbar.setTitle(title);
            toolbar.setBackgroundColor(toolBarColor);
            toolbar.getMenu().findItem(R.id.action_attach_items).setVisible(false);
            if (Build.VERSION.SDK_INT >= 21) {
                window.setStatusBarColor(statusBarColor);
            }
        } else if (2 > size) {
            toolbar.setTitle("Выбрано: " + size);
            toolbar.setBackgroundColor(Color.parseColor("#212121"));
            toolbar.getMenu().findItem(R.id.action_attach_items).setVisible(true);
            if (Build.VERSION.SDK_INT >= 21) {
                window.setStatusBarColor(getResources().getColor(android.R.color.black));
            }
        } else {
            toolbar.setTitle("Выбрано: " + size);
        }
    }

    public void result(List<JSONObject> items) {
        progressBar.setVisibility(View.GONE);
        if (items.size() == 0) {
            textView.setVisibility(View.VISIBLE);
        } else {
            list.setVisibility(View.VISIBLE);
        }
        RecyclerView.Adapter adapter;
        switch (type) {
            case Constants.VIDEO:
                adapter = new Video(this, items, ids);
                break;
            case Constants.DOC:
                adapter = new Doc(this, items, ids);
                break;
            default:
                adapter = new Audio(this, items, ids);
                break;
        }
        list.setAdapter(adapter);
    }
}
