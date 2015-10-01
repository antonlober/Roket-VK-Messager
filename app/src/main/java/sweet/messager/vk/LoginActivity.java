package sweet.messager.vk;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import sweet.messager.vk.api.Async;
import sweet.messager.vk.api.VK;
import sweet.messager.vk.api.VKException;
import sweet.messager.vk.db.Method;
import sweet.messager.vk.model.User;


public class LoginActivity extends ActionBarActivity {

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ApplicationName.baseActivity = this;
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.setStatusBarColor(Color.parseColor("#000000"));
        }
        intent = new Intent(ApplicationName.getAppContext(), WebViewActivity.class);
        if (getIntent().hasExtra("url")) {
            intent.putExtra("url", getIntent().getStringExtra("url"));
        }
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start();
            }
        });
    }

    private void start() {
        startActivityForResult(intent, 1);
    }

    public void showMsg(String text) {
        Toast.makeText(this, text.equals("") ? getString(R.string.errorVK1) : text, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onActivityResult(int result, int response, Intent intent) {
        if (intent != null && intent.hasExtra("token") && intent.hasExtra("uid")) {
            final int userId = intent.getIntExtra("uid", 0);
            if (userId == 0) return;
            final Context context = ApplicationName.getAppContext();
            ApplicationName.setUserInfo(intent.getStringExtra("token"), userId);
            context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean("isLogin", true)
                    .putString("access_token", intent.getStringExtra("token"))
                    .putInt("user_id", userId)
                    .apply();
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("Загрузка данных");
            progressDialog.setCancelable(false);
            progressDialog.show();
            new Async(){

                @Override
                protected Object background() throws VKException {
                    HashMap<String, Object> post = new HashMap<>();
                    post.put("fields", "photo_50,photo_100,photo_200,sex,status,domain");
                    return new VK(ApplicationName.getAppContext()).method("execute.login").params(post).getArray();
                }

                @Override
                protected void error(VKException error) {
                    try {
                        if (error.getError().has("redirect_uri")) {
                            Log.e("redirect_uri", error.getError().getString("redirect_uri"));
                        } else {
                            try {
                                showMsg(error.getMessage());
                            } catch (Exception e) {
                                showMsg("");
                            }
                        }
                    } catch (Exception e) {
                        showMsg("");
                    }
                }

                @Override
                protected void finish(Object json) {
                    progressDialog.dismiss();
                    try {
                        JSONObject user = ((JSONArray) json).getJSONObject(0);
                        Method.addUser(User.parse(user));
                        ApplicationName.setUserId(userId);
                        startActivity(new Intent(
                                LoginActivity.this,
                                BaseActivity.class
                        ));
                    } catch (JSONException e) {
                        showMsg(context.getString(R.string.errorVK2));
                    }
                }

                @Override
                protected void start() {

                }
            }.execute();
        }
    }

    @Override
    public void onBackPressed() {
        showMsg("Выполните пожалуйста вход ВКонтакте");
    }
}
