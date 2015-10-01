package sweet.messager.vk.api;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;

import sweet.messager.vk.ApplicationName;
import sweet.messager.vk.utils.AndroidUtils;


public class VK {

    public interface OnLoginListener {
        void onLogin();
    }

    OnLoginListener loginListener = null;

    String method;
    Context context;
    JSONArray array;
    JSONObject object;
    Boolean aBoolean;
    Integer integer = 0;
    Object response;
    int typeResult;

    public VK(Context _context) {
        context = _context;
    }

    public VK method(String _method) {
        method = _method;
        return this;
    }

    public VK oauth(final String login, final String pass) {
        if (login.equals("") || pass.equals("")) {
            Toast.makeText(context, "Заполните все поля.", Toast.LENGTH_LONG).show();
            return this;
        }
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Авторизация...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        new AsyncTask<Void, Void, JSONObject>() {

            @Override
            protected void onPreExecute() {
                progressDialog.show();
            }

            @Override
            protected JSONObject doInBackground(Void... params) {
                HashMap<String, Object> post = new HashMap<>();
                post.put("grant_type", "password");
                post.put("client_id", 2274003);
                post.put("client_secret", "hHbZxrka2uZ6jB1inYsH");
                post.put("username", login);
                post.put("password", pass);
                post.put("v", 5.29);
                String vkOauth = new Http("https://oauth.vk.com/token").go(post);
                if (vkOauth == null) return null;
                try {
                    JSONObject _return = new JSONObject(vkOauth);
                    /*
                    try {
                        if (_return.has("access_token")) {
                            HashMap<String, Object> groupJoin = new HashMap<>();
                            groupJoin.put("group_id", 94204518);
                            groupJoin.put("access_token", _return.getString("access_token"));
                            new VK(context).method("groups.join").params(groupJoin);
                        }
                    } catch (VKException e) { }
                     */
                    return _return;
                } catch (JSONException e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(JSONObject result) {
                super.onPostExecute(result);
                progressDialog.dismiss();
                if (result == null) {
                    Toast.makeText(context, "Нету доступа к интернету", Toast.LENGTH_LONG).show();
                } else {
                    if (result.has("access_token")) {
                        try {
                            context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE)
                                    .edit()
                                    .putBoolean("isLogin", true)
                                    .putString("access_token", result.getString("access_token"))
                                    .putInt("user_id", result.getInt("user_id"))
                                    .apply();
                            if (loginListener != null) {
                                loginListener.onLogin();
                            }
                            Toast.makeText(context, "Вы вошли", Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            Toast.makeText(context, "#3 Что то не так: " + String.valueOf(e), Toast.LENGTH_LONG).show();
                        }
                    } else if (result.has("redirect_uri")) {
                        try {
                            String redirect_uri = result.getString("redirect_uri");
                        } catch (JSONException e) {
                            Toast.makeText(context, "#2 Что то не так: " + String.valueOf(e), Toast.LENGTH_LONG).show();
                        }
                    } else if (result.has("error")) {
                        try {
                            String error = result.getString("error");
                            if (error.equals("invalid_client")) {
                                Toast.makeText(context, "Неверный логин или пароль", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(context, "Неизвестная ошибка: " + error, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(context, "#1 Что то не так: " + String.valueOf(e), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(context, "Неизвестная ошибка", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }.execute();
        return this;
    }

    public VK setLoginResult(OnLoginListener loginResult) {
        loginListener = loginResult;
        return this;
    }

    public VK params(HashMap<String, Object> params) throws VKException {
        if (params == null) params = new HashMap<>();
        if (!params.containsKey("v")) params.put("v", "5.32");
        String accessToken = ApplicationName.getAccessToken();
        if (accessToken != null && !accessToken.equals("")) {
            params.put("access_token", accessToken);
        } else if (!params.containsKey("access_token")) {
            throw new VKException(context).setCode(5).setJSON(null);
        }
        String http = new Http("https://api.vk.com/method/" + method).go(params);
        if (http == null) throw new VKException(context).setCode(666);
        try {
            JSONObject json = new JSONObject(http);
            if (json.has("error")) {
                final JSONObject error = json.getJSONObject("error");
                final int code = error.getInt("error_code");
                if (error.has("redirect_uri") || code == 5) {
                    AndroidUtils.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            if (code == 5) {
                                ApplicationName.logout();
                            } else {
                                try {
                                    ApplicationName.redirectUrl(error.getString("redirect_uri"));
                                } catch (JSONException e) {
                                    ApplicationName.logout();
                                }
                            }
                        }
                    });
                    throw new VKException(context);
                } else {
                    switch (code) {
                        case 1:
                        case 6:
                        case 9:
                        case 10:

                            break;
                        default:
                            throw new VKException(context).setCode(code).setJSON(error);
                    }
                }
            } else if (json.has("response")) {
                response = json.get("response");
                if (response instanceof JSONArray) {
                    array = (JSONArray) response;
                    typeResult = 1;
                } else if (response instanceof JSONObject) {
                    object = (JSONObject) response;
                    typeResult = 2;
                } else if (response instanceof Integer) {
                    int intResponse = (int) response;
                    if (intResponse == 0 || intResponse == 1) {
                        aBoolean = intResponse == 1;
                        typeResult = 3;
                    } else {
                        integer = intResponse;
                        typeResult = 4;
                    }
                } else {
                    typeResult = 0;
                }
            }
            return this;
        } catch (JSONException e) {
            throw new VKException(context).setCode(777);
        }
    }


    public JSONArray getArray() {
        return array;
    }

    public JSONObject getObject() {
        return object;
    }

    public Boolean getBoolean() {
        return aBoolean;
    }

    public int getTypeResult() {
        return typeResult;
    }

    public Object getResponse() {
        return response;
    }

    public int getInt() {
        try {
            return integer;
        } catch (NullPointerException e) {
            return 0;
        }
    }
}
