package sweet.messager.vk.vk;


import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import sweet.messager.vk.ApplicationName;
import sweet.messager.vk.db.Method;
import sweet.messager.vk.model.ChatModel;
import sweet.messager.vk.model.HistoryResult;
import sweet.messager.vk.model.LongPollModel;
import sweet.messager.vk.model.User;
import sweet.messager.vk.utils.AndroidUtils;

public class VKApi {


    public static String http(String url) {
        HttpURLConnection connection = null;
        try{
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("GET");
            // connection.getOutputStream().write(url.getBytes("UTF-8"));
            /*
            if(enable_compression){
                connection.setRequestProperty("Accept-Encoding", "gzip");
            }
             */

            int code = connection.getResponseCode();
            if (code == -1) {
                //
            }
            InputStream is = new BufferedInputStream(connection.getInputStream(), 8192);
            String enc=connection.getHeaderField("Content-Encoding");
            if (enc != null && enc.equalsIgnoreCase("gzip")) {
                is = new GZIPInputStream(is);
            }
            String response = AndroidUtils.convertStreamToString(is);
            Log.e("VKApi", String.valueOf(response));
            return response;
        } catch (MalformedURLException e) {
        } catch (ProtocolException e) {
        } catch (IOException e) {
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }

    public static JSONObject get(String method, String params) {
        String accessToken = ApplicationName.getAccessToken();
        StringBuilder stringBuilder = new StringBuilder("https://api.vk.com/method/");
        stringBuilder.append(method);
        stringBuilder.append("?v=5.34");
        if (params != null) {
            stringBuilder.append(params);
        }
        if (accessToken != null) {
            stringBuilder.append("&access_token=" + accessToken + "&");
        }
        String url = stringBuilder.toString();
        Log.e("VKApi", url);
        String http = http(url);
        if (http == null) {
            for (int i = 0; i < 5; i++) {
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException ignored) { }
                http = url;
                if (http != null) {
                    break;
                }
            }
        }
        JSONObject vk = new JSONObject();
        JSONObject error = new JSONObject();
        boolean err = false;
        if (http != null) {
            try {
                vk = new JSONObject(http);
                if (vk.has("error")) {
                    error = vk.getJSONObject("error");
                    if (error.has("redirect_uri")) {
                        final String redirect_uri = error.getString("redirect_uri");
                        AndroidUtils.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                ApplicationName.redirectUrl(redirect_uri);
                            }
                        });
                    } else {
                        int code = error.getInt("error_code");
                        switch (code) {
                            case 1:
                            case 6:
                            case 9:
                            case 10:
                                break;
                            case 5:
                                AndroidUtils.runOnUIThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ApplicationName.logout();
                                    }
                                });
                                break;
                        }
                    }
                } else {
                    return vk;
                }
            } catch (JSONException e) {
                err = true;
            }
        }
        if (err) {
            try {
                error.put("error_code", 666);
                vk.put("error", error);
                return vk;
            } catch (JSONException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    public static void addUser(final int id) {
        new Thread(new Runnable() {
            public void run() {
                StringBuilder params = new StringBuilder("&user_ids=");
                params.append(id);
                params.append("&fields=photo_50,photo_100,photo_200,sex,online,bdate,blacklisted,last_seen");
                try {
                    JSONObject vkUser = get("users.get", params.toString()).getJSONArray("response").getJSONObject(0);
                    Log.e("getVKUser", String.valueOf(vkUser));
                    Method.addUser(
                            User.parse(vkUser)
                    );
                } catch (JSONException e) {
                    Log.e("VKApi", String.valueOf(e));
                } catch (NullPointerException e) { }
            }
        }).start();
    }

    public static void addStats() {
        new Thread(new Runnable() {
            public void run() {
                get("stats.trackVisitor", null);
            }
        }).start();
    }


    public static List<ChatModel> getDialogs(int offset, int count) {
        if (count == 0) {
            count = 20;
        }
        List<ChatModel> _return = new ArrayList<>();
        StringBuilder params = new StringBuilder("&count=");
        params.append(count);
        params.append("&offset=");
        params.append(offset);
        try {
            JSONObject response = get("execute.getDialogs", params.toString()).getJSONObject("response");
            if (response.has("profiles")) {
                Method.addUsers(response.getJSONArray("profiles"));
            }
            if (response.has("dialogs")) {
                JSONObject dialogs = response.getJSONObject("dialogs");
                ApplicationName.dialogsCount = dialogs.getInt("count");
                _return = Method.updateDialogs(dialogs.getJSONArray("items"));
            }
        } catch (JSONException e) {
            Log.e("VKApi", String.valueOf(e));
        } catch (NullPointerException e) {

        }
        return _return;
    }

    public static JSONArray getUsers(String ids) {
        StringBuilder params = new StringBuilder("&user_ids=");
        params.append(ids);
        params.append("&fields=photo_50,photo_100,photo_200,sex,online,bdate,blacklisted,last_seen");
        try {
            return get("users.get", params.toString()).getJSONArray("response");
        } catch (JSONException e) {
            Log.e("VKApi", String.valueOf(e));
            return new JSONArray();
        } catch (NullPointerException e) {
            return new JSONArray();
        }
    }

    public static HistoryResult getHistory(int isChat, int object, int offset) {
        String key = (isChat == 1) ? "chat" + Math.abs(object - 2000000000) : "user" + object;
        HistoryResult historyResult = new HistoryResult();
        List<JSONObject> _return = new ArrayList<>();
        StringBuilder params = new StringBuilder("&chat=");
        params.append(isChat);
        params.append("&id=");
        params.append((isChat == 1) ? Math.abs(object - 2000000000) : object);
        params.append("&offset=");
        params.append(offset);
        try {
            JSONObject response = get("execute.getHistory", params.toString()).getJSONObject("response");
            JSONObject objectVK = response.getJSONObject("object");
            if (isChat == 1) {
                Method.addChat(objectVK);
                historyResult.chat_title = objectVK.getString("title");
                historyResult.isChat = true;
                historyResult.isPhoto = objectVK.has("photo_100");
                if (historyResult.isPhoto) {
                    historyResult.photo = objectVK.getString("photo_100");
                }
                String topTitle = null;
                if (objectVK.has("kicked")) {
                    topTitle = "Вас выгнали из чата";
                } else if (objectVK.has("left")) {
                    topTitle = "Вы покинули чат";
                } else {
                    try {
                        JSONArray users = objectVK.getJSONArray("users");
                        int online = 0;
                        for (int i = 0; i < users.length(); i++) {
                            if (users.getJSONObject(i).getInt("online") == 1) {
                                online++;
                            }
                        }
                        topTitle = AndroidUtils.declOfNum(users.length(), new String[]{
                                "учасник",
                                "учасника",
                                "учасников"
                        }) + ", " + online + " online";
                    } catch (JSONException e) { }
                }
                historyResult.text = topTitle;
            } else {
                Method.addUser(User.parse(objectVK));
                historyResult.user = User.parse(objectVK);
                historyResult.isChat = false;
            }
            JSONObject history = response.getJSONObject("history");
            JSONArray items = history.getJSONArray("items");
            historyResult.history = Method.updateHistory(key, items);
            return historyResult;
        } catch (JSONException e) {
            Log.e("VKApi", String.valueOf(e));

        } catch (NullPointerException e) {
        }
        historyResult.isChat = false;
        historyResult.user = AndroidUtils.defaultUser(ApplicationName.getUserId());
        historyResult.history = _return;
        return historyResult;
    }

    public static LongPollModel getLongPoll() {
        LongPollModel longPollModel = new LongPollModel();
        try {
            JSONObject response = get("messages.getLongPollServer", null).getJSONObject("response");
            longPollModel.key = response.getString("key");
            longPollModel.server = response.getString("server");
            longPollModel.ts = response.getInt("ts");
            return longPollModel;
        } catch (JSONException e) {
            Log.e("VKApi", String.valueOf(e));
            return null;
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static JSONObject getMsgById(int id) {
        try {
            StringBuilder params = new StringBuilder("&message_ids=");
            params.append(id);
            JSONObject response = get("messages.getById", params.toString()).getJSONObject("response").getJSONArray("items").getJSONObject(0);
            return response;
        } catch (JSONException e) {
            Log.e("VKApi", String.valueOf(e));
            return null;
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static JSONArray getMultimedia(int chat, int id) {
        try {
            String key;
            if (chat == 1) {
                id = ((id > 2000000000) ? Math.abs(id - 2000000000) : id);
                key = "chat" + id;
            } else {
                key = "user" + id;
            }
            StringBuilder params = new StringBuilder("&id=");
            params.append(id);
            params.append("&chat=");
            params.append(chat);
            JSONArray response = get("execute.getMedia", params.toString()).getJSONArray("response");
            JSONArray photos = new JSONArray();
            JSONArray videos = new JSONArray();
            JSONArray docs = new JSONArray();
            JSONArray audios = new JSONArray();
            JSONObject msg;
            JSONArray attachments;
            String type;
            JSONObject object;
            for (int i = 0; i < response.length(); i++) {
                msg = response.getJSONObject(i);
                if (msg.has("attachments")) {
                    attachments = msg.getJSONArray("attachments");
                    for (int n = 0; n < attachments.length(); n++) {
                        type = attachments.getJSONObject(n).getString("type");
                        switch (type) {
                            case "photo":
                                object = attachments.getJSONObject(n).getJSONObject("photo");
                                photos.put(object);
                                break;
                            case "video":
                                object = attachments.getJSONObject(n).getJSONObject("video");
                                videos.put(object);
                                break;
                            case "audio":
                                object = attachments.getJSONObject(n).getJSONObject("audio");
                                audios.put(object);
                                break;
                            case "doc":
                                object = attachments.getJSONObject(n).getJSONObject("doc");
                                docs.put(object);
                                break;
                        }
                    }
                }
            }
            Method.addMultimedia(photos, videos, audios, docs, key);
            return response;
        } catch (JSONException e) {
            Log.e("VKApi", String.valueOf(e));
            return null;
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static List<ChatModel> getAllDialog() {
        List<ChatModel> chatModels = new ArrayList<>();
        try {
            JSONArray response = get("execute.allDialog", null).getJSONArray("response");
            return Method.addAllChats(response);
        } catch (JSONException e) {
            Log.e("VKApi", String.valueOf(e));
            return chatModels;
        } catch (NullPointerException e) {
            return chatModels;
        }
    }

}
