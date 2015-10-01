package sweet.messager.vk;


import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

import sweet.messager.vk.interfaces.FragmentListener;
import sweet.messager.vk.ui.AlbumsActivity;
import sweet.messager.vk.ui.PhotosActivity;


public class AttachActivity extends AppCompatActivity implements FragmentListener {

    boolean photos = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.setStatusBarColor(Color.parseColor("#000000"));
        }
        albums();
    }

    @Override
    public void onFragment(int event) {

    }

    private void albums() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment, AlbumsActivity.newInstance())
                .commit();
        photos = false;
    }

    @Override
    public void onChat(HashMap<String, Object> item) {
        try {
            if (item.containsKey("type_photo")) {
                Intent resultIntent = new Intent();
                JSONArray jsonArray = new JSONArray();
                jsonArray.put((JSONObject) item.get("type_photo"));
                resultIntent.putExtra("jsonArray", jsonArray.toString());
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                String id = (String) item.get("id");
                String title = (String) item.get("title");
                boolean vk = (Boolean) item.get("vk");
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment, PhotosActivity.newInstance(id, title, vk))
                        .commit();
                photos = true;
            }
        } catch (NullPointerException e) {

        } catch (Exception e) {

        }
    }

    @Override
    public void onBackPressed() {
        if (photos) {
            albums();
        } else {
            finish();
        }
    }
}
