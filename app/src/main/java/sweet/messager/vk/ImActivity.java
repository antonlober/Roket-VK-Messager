package sweet.messager.vk;

import android.content.Intent;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import java.util.HashMap;

import sweet.messager.vk.interfaces.FragmentListener;
import sweet.messager.vk.model.ChatModel;
import sweet.messager.vk.ui.ChatActivity;
import sweet.messager.vk.ui.ChatsActivity;


public class ImActivity extends ActionBarActivity implements FragmentListener  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ApplicationName.baseActivity = this;

        if (ApplicationName.colors != null && Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.setStatusBarColor(ApplicationName.colors.statusBarColor);
        }

        Intent intent = getIntent();
        int id = intent.getIntExtra("id", 0);
        ChatActivity chatActivity;
        if (intent.hasExtra("chat")) {
            chatActivity = ChatActivity.newInstance(
                    id,
                    intent.hasExtra("photo") ? intent.getStringExtra("photo") : null,
                    intent.getStringExtra("title"),
                    intent.getStringExtra("text"));
        } else {
            chatActivity = ChatActivity.newInstance(id);
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, chatActivity).commit();
    }



    @Override
    public void onFragment(int event) {

    }

    @Override
    public void onChat(HashMap<String, Object> item) {

    }
}
