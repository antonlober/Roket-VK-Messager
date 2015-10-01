package sweet.messager.vk.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import sweet.messager.vk.ApplicationName;
import sweet.messager.vk.R;
import sweet.messager.vk.api.VK;
import sweet.messager.vk.api.VKException;
import sweet.messager.vk.model.Album;
import sweet.messager.vk.model.ChatModel;
import sweet.messager.vk.model.User;
import sweet.messager.vk.vk.VKApi;


public class Method {



    public static HashMap<String, String> getObject(String id, String type) {
        Context context = ApplicationName.getAppContext();
        HashMap<String, String> _return = new HashMap<String, String>();
        try {
            SQLiteDatabase db = new Sql(context).getWritableDatabase();
            Cursor user = db.query(type, null, "id = ?", new String[] { id }, null, null, null);
            if (user.moveToFirst()) {
                int photoColIndex = user.getColumnIndex("photo");
                int nameColIndex = user.getColumnIndex("name");
                int idColIndex = user.getColumnIndex("id");
                do {
                    _return.put("photo", user.getString(photoColIndex));
                    _return.put("name", user.getString(nameColIndex));
                    _return.put("id", String.valueOf(user.getInt(idColIndex)));
                } while (user.moveToNext());
            }
            db.close();
            return _return.containsKey("id") ? _return : null;
        } catch (Exception e) {
        }
        return null;
    }



    public static ArrayList<HashMap<String, Object>> getItems() {
        Context context = ApplicationName.getAppContext();
        ArrayList<HashMap<String, Object>> _return = new ArrayList<>();
        try {
            SQLiteDatabase db = new Sql(context).getWritableDatabase();
            Cursor items = db.query("items", null, null, null, null, null, "time DESC LIMIT 24");
            if (items.moveToFirst()) {
                int timeColIndex = items.getColumnIndex("time");
                int idColIndex = items.getColumnIndex("id");
                int msg_idColIndex = items.getColumnIndex("msg_id");
                int countColIndex = items.getColumnIndex("count");
                int userColIndex = items.getColumnIndex("user");
                int textColIndex = items.getColumnIndex("text");
                int readColIndex = items.getColumnIndex("read");
                int myColIndex = items.getColumnIndex("my");
                int chatColIndex = items.getColumnIndex("chat");
                HashMap<String, Object> _item;
                do {
                    _item = new HashMap<>();
                    boolean chat = items.getInt(chatColIndex) == 1;
                    int id = items.getInt(idColIndex);
                    HashMap<String, String> object = getObject(
                            String.valueOf(id),
                            chat ? "chats" : (0 > id ? "emails" : "users")
                    );
                    if (object == null) {
                        object = new HashMap<>();
                        object.put("name", context.getString(R.string.loading));
                        object.put("photo", "http://vk.com/images/camera_b.gif");
                    }
                    _item.put("name", object.get("name"));
                    _item.put("photo", object.get("photo"));
                    _item.put("text", items.getString(textColIndex));
                    _item.put("chat", chat);
                    _item.put("my", items.getInt(myColIndex) == 1);
                    _item.put("read", items.getInt(readColIndex) == 1);
                    _item.put("count", items.getInt(countColIndex));
                    _item.put("msg_id", items.getInt(msg_idColIndex));
                    _item.put("id", id);
                    _item.put("time", items.getInt(timeColIndex));
                    _item.put("user", items.getInt(userColIndex));
                    _return.add(_item);
                } while (items.moveToNext());
            }
            db.close();
            // Collections.reverse(_return);
            return  _return;
        } catch (Exception ignored) {
        }
        return _return;
    }


    /* new Method */

    public static List<JSONObject> updateHistory(String key, JSONArray items) {
        List<JSONObject> _return = new ArrayList<>();
        if (items.length() == 0) return _return;
        try {
            JSONObject msg;
            ApplicationName.getDb().execSQL("DELETE FROM msg WHERE key = '" + key + "'");
            ApplicationName.getDb().beginTransaction();
            for (int i = items.length() - 1; i >= 0; i--) {
                msg = items.getJSONObject(i);
                ContentValues contentValues = new ContentValues();
                contentValues.put("json", msg.toString());
                contentValues.put("key", key);
                long position = ApplicationName.getDb().insert("msg", null, contentValues);
                if (position != -1) {
                    _return.add(msg);
                }
            }
            try {
                ApplicationName.getDb().setTransactionSuccessful();
            } finally {
                ApplicationName.getDb().endTransaction();
            }
        } catch (SQLiteException e) {

        } catch (Exception e) {

        }
        return _return;
    }


    public static List<ChatModel> updateDialogs(JSONArray items) {
        List<ChatModel> _return = new ArrayList<>();
        if (items.length() == 0) return _return;
        try {
            ApplicationName.getDb().execSQL("DELETE FROM dialogs");
            ApplicationName.getDb().beginTransaction();
            ChatModel chatModel;
            JSONObject dialog;
            List<Integer> usersIds = new ArrayList<>();
            try {
                for (int i = 0; i < items.length(); i++) {
                    dialog = items.getJSONObject(i);
                    Log.e("updateDialogs", "JSON: " + String.valueOf(dialog));
                    chatModel = ChatModel.parse(dialog);
                    Log.e("updateDialogs", "MODEL: " + String.valueOf(chatModel));
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("json", chatModel.toString());
                    if (ApplicationName.getDb().insert("dialogs", null, contentValues) != -1) {
                        _return.add(chatModel);
                        int userId;
                        if (dialog.has("admin_id")) {
                            userId = dialog.getInt("admin_id");
                            if (usersIds.indexOf(userId) == -1 && !ApplicationName.isUser(userId)) {
                                usersIds.add(
                                        dialog.getInt("admin_id")
                                );
                            }
                        }
                        if (dialog.has("chat_active")) {
                            JSONArray chat_active = dialog.getJSONArray("chat_active");
                            for (int n = 0; n < chat_active.length(); n++) {
                                userId = chat_active.getInt(n);
                                if (usersIds.indexOf(userId) == -1 && !ApplicationName.isUser(userId)) {
                                    usersIds.add(
                                            chat_active.getInt(n)
                                    );
                                }
                            }
                        }
                    }
                }
                if (usersIds.size() != 0) {
                    JSONArray usersVK = VKApi.getUsers(TextUtils.join(",", usersIds));
                    if (usersVK != null && usersVK.length() != 0) {
                        User user;
                        for (int i = 0; i < usersVK.length(); i++) {
                            user = User.parse(usersVK.getJSONObject(i));
                            int userId = Math.abs(user.id);
                            ApplicationName.addUser(userId, user);
                            ContentValues contentValues = new ContentValues();
                            contentValues.put("json", user.toString());
                            contentValues.put("id", userId);
                            if (ApplicationName.getDb().insert("object", null, contentValues) == -1) {
                                ApplicationName.getDb().update("object", contentValues, "id = ?", new String[]{
                                        String.valueOf(userId)
                                });
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                Log.e("updateDialogs", "JSON: " + String.valueOf(e));
            }
            try {
                ApplicationName.getDb().setTransactionSuccessful();
            } finally {
                ApplicationName.getDb().endTransaction();
            }
        } catch (SQLiteException e) {
        } catch (Exception e) {
        }
        return _return;
    }

    public static void addUser(User user) {
        try {
            int userId = Math.abs(user.id);
            ApplicationName.addUser(userId, user);
            ContentValues contentValues = new ContentValues();
            contentValues.put("json", user.toString());
            contentValues.put("id", userId);
            long addID = ApplicationName.getDb().replace("object", null, contentValues);
            Log.e("getVKUser", "add: " + addID);
        } catch (SQLiteException e) {
            Log.e("getVKUser", "SQLiteException: " + e);
        } catch (Exception e) {
            Log.e("getVKUser", "Exception: " + e);
        }
    }

    public static void updateChat(JSONObject chat) {
        ApplicationName.getDb().beginTransaction();
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("id", chat.getInt("id"));
            contentValues.put("json", chat.toString());
            ApplicationName.getDb().replace("chats", null, contentValues);
            if (chat.has("users")) {
                JSONArray users = chat.getJSONArray("users");
                for (int i = 0; i < users.length(); i++) {
                    JSONObject user = users.getJSONObject(i);
                    contentValues = new ContentValues();
                    contentValues.put("json", User.parse(user).toString());
                    contentValues.put("id", user.getInt("id"));
                    ApplicationName.getDb().replace("object", null, contentValues);
                }
            }
        } catch (JSONException e) {

        }
        try {
            ApplicationName.getDb().setTransactionSuccessful();
        } finally {
            ApplicationName.getDb().endTransaction();
        }
    }

    public static void addUsers(JSONArray users) {
        ApplicationName.getDb().beginTransaction();
        User user;
        try {
            for (int i = 0; i < users.length(); i++) {
                user = User.parse(users.getJSONObject(i));
                int userId = Math.abs(user.id);
                ApplicationName.addUser(userId, user);
                ContentValues contentValues = new ContentValues();
                contentValues.put("json", user.toString());
                contentValues.put("id", userId);
                long addID = ApplicationName.getDb().replace("object", null, contentValues);
            }
        } catch (JSONException e) {

        } catch (SQLiteException e) {

        }
        try {
            ApplicationName.getDb().setTransactionSuccessful();
        } finally {
            ApplicationName.getDb().endTransaction();
        }
    }

    public static List<ChatModel> getDialogs() {
        List<ChatModel> items = new ArrayList<>();
        try {
            Cursor c = ApplicationName.getDb().query("dialogs", null, null, null, null, null, "position ASC LIMIT 20");
            if (c != null && c.moveToFirst()) {
                int jsonColIndex = c.getColumnIndex("json");
                do {
                    ChatModel chatModel = ChatModel.parse(c.getString(jsonColIndex));
                    Log.e("getDialogs", String.valueOf(chatModel));
                    items.add(chatModel);
                } while (c.moveToNext());
            }
            if (c != null) c.close();
        } catch (SQLiteException e) {

        } catch (Exception e) {
        }
        return items;
    }


    public static HashMap<Integer, User> getObjects() {
        HashMap<Integer, User> _return = new HashMap<>();
        try {
            Cursor c = ApplicationName.getDb().query("object", null, null, null, null, null, "id ASC LIMIT 10000");
            if (c != null && c.moveToFirst()) {
                int jsonColIndex = c.getColumnIndex("json");
                int idColIndex = c.getColumnIndex("id");
                do {
                    _return.put(
                            c.getInt(idColIndex),
                            User.parse(c.getString(jsonColIndex))
                    );
                } while (c.moveToNext());
            }
            if (c != null) c.close();
        } catch (SQLiteException e) {

        } catch (Exception e) {

        }
        return _return;
    }

    public static List<JSONObject> putMsg(String key, List<JSONObject> items) {
        List<JSONObject> _return = new ArrayList<>();
        if (items.size() == 0) return _return;
        try {
            Collections.reverse(items);
            JSONObject msg;
            ApplicationName.getDb().execSQL("DELETE FROM msg WHERE key = '" + key + "'");
            ApplicationName.getDb().beginTransaction();
            for (int i = 0; i < items.size(); i++) {
                msg = items.get(i);
                ContentValues contentValues = new ContentValues();
                contentValues.put("json", msg.toString());
                contentValues.put("key", key);
                long position = ApplicationName.getDb().insert("msg", null, contentValues);
                if (position != -1) {
                    _return.add(msg);
                }
            }
            try {
                ApplicationName.getDb().setTransactionSuccessful();
            } finally {
                ApplicationName.getDb().endTransaction();
            }
        } catch (SQLiteException e) {

        } catch (Exception e) {

        }
        return _return;
    }

    public static void saveMsg(String key, JSONObject msg) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("json", msg.toString());
            contentValues.put("key", key);
            long position = ApplicationName.getDb().insert("msg", null, contentValues);
        } catch (SQLiteException e) {
            Log.e("LongPollJSON", "sql: " + String.valueOf(e));
        } catch (Exception e) {
            Log.e("LongPollJSON", "sql: " + String.valueOf(e));
        }
    }

    public static List<JSONObject> getMsg(String key) {
        List<JSONObject> items = new ArrayList<>();
        try {
            Cursor c = ApplicationName.getDb().query("msg", null, "key = ?", new String[]{key}, null, null, "id DESC LIMIT 500");
            if (c != null && c.moveToFirst()) {
                int jsonColIndex = c.getColumnIndex("json");
                try {
                    do {
                        items.add(new JSONObject(c.getString(jsonColIndex)));
                    } while (c.moveToNext());
                } catch (JSONException ignored) { }
            }
            if (c != null) c.close();
        } catch (SQLiteException e) {

        } catch (Exception e) {

        }
        Collections.reverse(items);
        return items;
    }


    public static List<JSONObject> addObject(String key, JSONArray items) {
        List<JSONObject> _return = new ArrayList<>();
        if (items.length() == 0) return _return;
        try {
            JSONObject item;
            ApplicationName.getDb().execSQL("DELETE FROM " + key);
            ApplicationName.getDb().beginTransaction();
            for (int i = 0; i < items.length(); i++) {
                try {
                    item = items.getJSONObject(i);
                    if (item.has("url")) {
                        item.remove("url");
                    }
                    if (item.has("files")) {
                        item.remove("files");
                    }
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("json", item.toString());
                    long position = ApplicationName.getDb().insert(key, null, contentValues);
                    if (position != -1) {
                        _return.add(item);
                    }
                } catch (JSONException e) { }
            }
            try {
                ApplicationName.getDb().setTransactionSuccessful();
            } finally {
                ApplicationName.getDb().endTransaction();
            }
        } catch (SQLiteException e) {

        } catch (Exception e) {

        }
        return _return;
    }

    public static List<JSONObject> getObject(String key) {
        List<JSONObject> items = new ArrayList<>();
        try {
            Cursor c = ApplicationName.getDb().query(key, null, null, null, null, null, "id ASC LIMIT 500");
            if (c != null && c.moveToFirst()) {
                int jsonColIndex = c.getColumnIndex("json");
                try {
                    do {
                        items.add(new JSONObject(c.getString(jsonColIndex)));
                    } while (c.moveToNext());
                } catch (JSONException ignored) { }
            }
            if (c != null) c.close();
        } catch (SQLiteException e) {

        } catch (Exception e) {

        }
        return items;
    }


    public static List<JSONObject> addFriends(JSONArray friends) {
        try {
            List<JSONObject> _return = new ArrayList<>();
            ApplicationName.getDb().execSQL("DELETE FROM friends_objects");
            ApplicationName.getDb().beginTransaction();
            try {
                for (int i = 0; i < friends.length(); i++) {
                    JSONObject friend = friends.getJSONObject(i);
                    friend.put("name", friend.getString("first_name") + " " + friend.getString("last_name"));
                    friend.remove("first_name");
                    friend.remove("last_name");
                    int id = friend.getInt("id");
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("user_id", id);
                    contentValues.put("json", friend.toString());
                    ApplicationName.getDb().insert("friends_objects", null, contentValues);

                    /* Add User */
                    // ApplicationName.addUser(id, friend);
                    contentValues = new ContentValues();
                    contentValues.put("json", friend.toString());
                    contentValues.put("id", id);
                    long p = ApplicationName.getDb().replace("object", null, contentValues);
                }
            } catch (JSONException e) {

            }
            try {
                ApplicationName.getDb().setTransactionSuccessful();
            } finally {
                ApplicationName.getDb().endTransaction();
            }
            return _return;
        } catch (SQLiteException e) {
            ApplicationName.getDb().execSQL("CREATE TABLE friends_objects ("
                    + "position INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "user_id INTEGER UNIQUE,"
                    + "json BLOB);");
             return addFriends(friends);
        }
    }

    public static List<User> getFriends(String ids) {
        try {
            List<User> _return = new ArrayList<>();
            String in = null;
            if (ids != null) {
                in = "user_id IN (" + ids + ")";
            }
            Cursor c = ApplicationName.getDb().query("friends_objects", null, in, null, null, null, "position ASC LIMIT " + (in == null ? 30 : 500));
            if (c != null && c.moveToFirst()) {
                int jsonColIndex = c.getColumnIndex("json");
                do {
                    _return.add(User.parse(c.getString(jsonColIndex)));
                } while (c.moveToNext());
            }
            if (c != null) c.close();
            return _return;
        } catch (SQLiteException e) {
            ApplicationName.getDb().execSQL("CREATE TABLE friends_objects ("
                    + "position INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "user_id INTEGER UNIQUE,"
                    + "json BLOB);");
            return getFriends(ids);
        }
    }


    public static void addAlbums(List<Album> albums, boolean vk) {
        try {
            String base = (vk ? "album_vk" : "album_sd");
            ApplicationName.getDb().execSQL("DELETE FROM " + base);
            ApplicationName.getDb().beginTransaction();
            for (int i = 0; i < albums.size(); i++) {
                Album album = albums.get(i);
                ContentValues contentValues = new ContentValues();
                contentValues.put("json", album.toString());
                ApplicationName.getDb().insert(base, null, contentValues);
            }
            try {
                ApplicationName.getDb().setTransactionSuccessful();
            } finally {
                ApplicationName.getDb().endTransaction();
            }
        } catch (SQLiteException e) {

        } catch (Exception e) {

        }
    }


    public static List<Album> getAlbums(boolean vk) {
        List<Album> albums = new ArrayList<>();
        try {
            Album album;
            String base = (vk ? "album_vk" : "album_sd");
            Cursor c = ApplicationName.getDb().query(base, null, null, null, null, null, "id ASC LIMIT 30");
            if (c != null && c.moveToFirst()) {
                int jsonColIndex = c.getColumnIndex("json");
                try {
                    do {
                        JSONObject json = new JSONObject(c.getString(jsonColIndex));
                        album = new Album();
                        album.title = json.getString("title");
                        album.uri = json.getString("thumb_src");
                        album.count = json.getInt("size");
                        if (!vk) {
                            album.bucket = json.getString("bucket");
                        }
                        albums.add(album);
                    } while (c.moveToNext());
                } catch (JSONException e) {

                }
            }
            if (c != null) c.close();
        } catch (SQLiteException e) {

        } catch (Exception e) {

        }
        return albums;
    }

    public static List<JSONObject> addPhoto(final boolean vk, final String album_id, final JSONArray photos) {
        final String base = (vk ? "all_photo_vk" : "all_photo_sd");
        final List<JSONObject> _return = new ArrayList<>();
        try {
            ApplicationName.getDb().execSQL("DELETE FROM " + base + " WHERE album_id = '" + album_id + "'");
            ApplicationName.getDb().beginTransaction();
            try {
                for (int i = 0; i < photos.length(); i++) {
                    JSONObject photo = photos.getJSONObject(i);
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("json", photo.toString());
                    contentValues.put("album_id", album_id);
                    if (ApplicationName.getDb().insert(base, null, contentValues) != -1) {
                        _return.add(photo);
                    }
                }
            } catch (JSONException e) { }
            try {
                ApplicationName.getDb().setTransactionSuccessful();
            } finally {
                ApplicationName.getDb().endTransaction();
            }
        } catch (SQLiteException e) {

        } catch (Exception e) {

        }
        return _return;
    }

    public static List<JSONObject> getPhotos(final boolean vk, final String album_id) {
        List<JSONObject> photos = new ArrayList<>();
        String base = (vk ? "all_photo_vk" : "all_photo_sd");
        try {
            Cursor c = ApplicationName.getDb().query(base, null, "album_id = ?", new String[] {
                    album_id
            }, null, null, "id ASC LIMIT 50");
            if (c != null && c.moveToFirst()) {
                int jsonColIndex = c.getColumnIndex("json");
                try {
                    do {
                        JSONObject json = new JSONObject(c.getString(jsonColIndex));
                        photos.add(json);
                    } while (c.moveToNext());
                } catch (JSONException e) {

                }
            }
            if (c != null) c.close();
        } catch (SQLiteException e) {
            ApplicationName.getDb().execSQL("CREATE TABLE " + base + " ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "album_id TEXT NOT NULL,"
                    + "json BLOB);");
            return getPhotos(vk, album_id);
        } catch (Exception e) {

        }
        return photos;
    }

    public static void addChat(JSONObject chat) {
        ApplicationName.getDb().beginTransaction();
        try {
            ContentValues contentValues;
            JSONArray newUsers = new JSONArray();
            if (chat.has("users")) {
                JSONArray users = chat.getJSONArray("users");
                JSONObject userVK;
                User user;
                for (int i = 0; i < users.length(); i++) {
                    userVK = users.getJSONObject(i);
                    user = User.parse(userVK);
                    int userId = Math.abs(user.id);
                    ApplicationName.addUser(userId, user);
                    contentValues = new ContentValues();
                    contentValues.put("json", user.toString());
                    contentValues.put("id", userId);
                    ApplicationName.getDb().replace("object", null, contentValues);
                    JSONObject newJSON = new JSONObject();
                    newJSON.put("id", userId);
                    newJSON.put("invited_by", userVK.getInt("invited_by"));
                    if (chat.getInt("admin_id") == userId) {
                        newJSON.put("admin", 1);
                    }
                    newUsers.put(newJSON);
                }
            }
            chat.remove("users");
            chat.put("users", newUsers);
            contentValues = new ContentValues();
            contentValues.put("json", chat.toString());
            contentValues.put("id", chat.getInt("id"));
            ApplicationName.getDb().replace("chats", null, contentValues);
        } catch (JSONException e) {

        } catch (SQLiteException e) {

        } catch (NullPointerException e) {

        }
        try {
            ApplicationName.getDb().setTransactionSuccessful();
        } finally {
            ApplicationName.getDb().endTransaction();
        }
    }

    public static JSONObject getChat(int id) {
        JSONObject _return = new JSONObject();
        if (id > 2000000000) {
            id = Math.abs(id - 2000000000);
        }
        try {
            Cursor c = ApplicationName.getDb().query("chats", null, "id = ?", new String[] {
                    String.valueOf(id)
            }, null, null, "id ASC LIMIT 1");
            if (c != null && c.moveToFirst()) {
                int jsonColIndex = c.getColumnIndex("json");
                try {
                    do {
                        _return = new JSONObject(c.getString(jsonColIndex));
                    } while (c.moveToNext());
                } catch (JSONException e) {

                }
            }
            if (c != null) c.close();
        } catch (SQLiteException e) {

        } catch (NullPointerException e) {

        }
        return _return;
    }

    public static void addMultimedia(JSONArray photos, JSONArray videos, JSONArray audios, JSONArray docs, String key) {
        ApplicationName.getDb().beginTransaction();
        String photoKey = key + "_photo";
        String videoKey = key + "_video";
        String docKey = key + "_doc";
        String audioKey = key + "_audio";
        if (photos.length() != 0) {
            Log.e("getMultimedia:PHOTO", key);
            ApplicationName.getDb().execSQL("DELETE FROM media_msg WHERE type = '" + photoKey + "'");
            ContentValues contentValues = new ContentValues();
            contentValues.put("json", photos.toString());
            contentValues.put("type", photoKey);
            ApplicationName.getDb().insert("media_msg", null, contentValues);
        }
        if (videos.length() != 0) {
            Log.e("getMultimedia:VIDEO", key);
            ApplicationName.getDb().execSQL("DELETE FROM media_msg WHERE type = '" + videoKey + "'");
            ContentValues contentValues = new ContentValues();
            contentValues.put("json", videos.toString());
            contentValues.put("type", videoKey);
            ApplicationName.getDb().insert("media_msg", null, contentValues);
        }
        if (audios.length() != 0) {
            Log.e("getMultimedia:AUDIOS", key);
            ApplicationName.getDb().execSQL("DELETE FROM media_msg WHERE type = '" + audioKey + "'");
            ContentValues contentValues = new ContentValues();
            contentValues.put("json", audios.toString());
            contentValues.put("type", audioKey);
            ApplicationName.getDb().insert("media_msg", null, contentValues);
        }
        if (docs.length() != 0) {
            Log.e("getMultimedia:DOCS", key);
            ApplicationName.getDb().execSQL("DELETE FROM media_msg WHERE type = '" + docKey + "'");
            ContentValues contentValues = new ContentValues();
            contentValues.put("json", docs.toString());
            contentValues.put("type", docKey);
            ApplicationName.getDb().insert("media_msg", null, contentValues);
        }
        try {
            ApplicationName.getDb().setTransactionSuccessful();
        } finally {
            ApplicationName.getDb().endTransaction();
        }
    }


    public static JSONArray getMultimedia(String key) {
        Log.e("getMultimedia:key", key);
        JSONArray _return = new JSONArray();
        try {
            Cursor c = ApplicationName.getDb().query("media_msg", null, "type = ?", new String[] {
                    key
            }, null, null, null);
            if (c != null && c.moveToFirst()) {
                int jsonColIndex = c.getColumnIndex("json");
                try {
                    do {
                        _return = new JSONArray(c.getString(jsonColIndex));
                    } while (c.moveToNext());
                } catch (JSONException e) {

                }
            }
            if (c != null) c.close();
        } catch (SQLiteException e) {
            Log.e("getMultimedia", String.valueOf(e));
        } catch (NullPointerException e) {
            Log.e("getMultimedia", String.valueOf(e));
        }
        Log.e("getMultimedia:return", String.valueOf(_return));
        return _return;
    }

    public static List<ChatModel> addAllChats(JSONArray dialogs) {
        Log.e("allChats", String.valueOf(dialogs));
        List<ChatModel> list = new ArrayList<>();
        if (dialogs.length() != 0) {
            ChatModel chatModel;
            ApplicationName.getDb().beginTransaction();
            try {
                ApplicationName.getDb().execSQL("DELETE FROM allChats");
                for (int i = 0; i < dialogs.length(); i++) {
                    JSONObject dialog = dialogs.getJSONObject(i);
                    if (dialog.getJSONObject("message").has("chat_id")) {
                        chatModel = ChatModel.parse(dialog);
                        ContentValues contentValues = new ContentValues();
                        contentValues.put("json", chatModel.toString());
                        if (ApplicationName.getDb().insert("allChats", null, contentValues) != -1) {
                            list.add(chatModel);
                        }
                        Log.e("allChats", "Add: " + chatModel);
                    }
                }
            } catch (JSONException e) {
                Log.e("allChats", "JSONException: " + String.valueOf(e));
            } catch (SQLiteException e) {
                Log.e("allChats", "SQLiteException: " + String.valueOf(e));
                ApplicationName.getDb().execSQL("CREATE TABLE allChats ("
                        + "position INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + "json BLOB);");
            } catch (NullPointerException e) {
                Log.e("allChats", "NullPointerException: " + String.valueOf(e));
            }
            try {
                ApplicationName.getDb().setTransactionSuccessful();
            } finally {
                ApplicationName.getDb().endTransaction();
            }
        }
        Log.e("allChats", String.valueOf(list));
        return list;
    }

    public static List<ChatModel> getAllChats() {
        List<ChatModel> list = new ArrayList<>();
        try {
            Cursor c = ApplicationName.getDb().query("allChats", null, null, null, null, null, null);
            if (c != null && c.moveToFirst()) {
                int jsonColIndex = c.getColumnIndex("json");
                do {
                    list.add(ChatModel.parse(c.getString(jsonColIndex)));
                } while (c.moveToNext());
            }
            if (c != null) c.close();
        } catch (SQLiteException e) {
            ApplicationName.getDb().execSQL("CREATE TABLE allChats ("
                    + "position INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "json BLOB);");
        } catch (NullPointerException e) {
        }
        return list;
    }
}
