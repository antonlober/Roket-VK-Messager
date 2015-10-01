package sweet.messager.vk;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import org.json.JSONException;
import org.json.JSONObject;
import android.os.Handler;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import sweet.messager.vk.api.Async;
import sweet.messager.vk.api.VK;
import sweet.messager.vk.api.VKException;
import sweet.messager.vk.db.Method;
import sweet.messager.vk.db.Sql;
import sweet.messager.vk.model.ColorModel;
import sweet.messager.vk.model.User;
import sweet.messager.vk.services.VideoPlayer;
import sweet.messager.vk.utils.AndroidUtils;

public class ApplicationName extends Application {

    private static Context context;
    private static SQLiteDatabase db;
    private static volatile HashMap<Integer, User> users = new HashMap<>();
    private static int internetType = 0;
    private static int userId = 0;
    private static String access_token = null;
    public static volatile Handler applicationHandler;
    public static int dbVersion = 2;
    public static HashMap<String, Boolean> settings = new HashMap<>();
    public static SharedPreferences settingsShare;
    public static List<Integer> onlineUsers = new ArrayList<>();
    public static Context baseActivity;
    public static int dialogsCount = 0;
    public static int friendsCount = 0;
    public static String bgMsg = null;
    public static boolean vkBgMsg = false;
    public static SimpleDateFormat simpleDateFormat;
    public static ColorModel colors = null;
    public static int[] screenSize = null;

    public void onCreate(){
        super.onCreate();
        ApplicationName.context = getApplicationContext();
        applicationHandler = new Handler(context.getMainLooper());
        SharedPreferences share = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        ApplicationName.db = new Sql(getAppContext()).getWritableDatabase();
        ApplicationName.users = Method.getObjects();
        ApplicationName.userId = share.getInt("user_id", 0);
        ApplicationName.access_token = share.getString("access_token", null);
        ApplicationName.baseActivity = getAppContext();

        simpleDateFormat = new SimpleDateFormat("HH:mm");
        simpleDateFormat.setTimeZone(TimeZone.getDefault());
        /*

        Log.d("sqlCreate", ApplicationName.db.getVersion() + " & " + ApplicationName.dbVersion);
        if (ApplicationName.db.getVersion() != ApplicationName.dbVersion) {
            ApplicationName.db.beginTransaction();
            try {
                // new Sql(this).onUpgrade(db, ApplicationName.db.getVersion(), ApplicationName.dbVersion);
                ApplicationName.db.setVersion(ApplicationName.dbVersion);
                ApplicationName.db.setTransactionSuccessful();
            } finally {
                ApplicationName.db.endTransaction();
            }
        }

         */


        /* Settings */
        ApplicationName.settingsShare = getSharedPreferences("setting", MODE_PRIVATE);
        ApplicationName.settings.put(Constants.NOTIFICATION, settingsShare.getBoolean(Constants.NOTIFICATION, true));
        ApplicationName.settings.put(Constants.READ_MSG, settingsShare.getBoolean(Constants.READ_MSG, true));
        ApplicationName.settings.put(Constants.MELODY, settingsShare.getBoolean(Constants.MELODY, true));
        ApplicationName.settings.put(Constants.IMAGE_LOAD, settingsShare.getBoolean(Constants.IMAGE_LOAD, true));
        ApplicationName.settings.put(Constants.UPDATE_MSG, settingsShare.getBoolean(Constants.UPDATE_MSG, true));
        ApplicationName.settings.put(Constants.VIDEO_PLAYER, settingsShare.getBoolean(Constants.VIDEO_PLAYER, true));
        vkBgMsg = ApplicationName.settingsShare.getBoolean("vkBgMsg", false);
        bgMsg = ApplicationName.settingsShare.getString("bgMsg", null);

        int toolBarColor = ApplicationName.settingsShare.getInt("toolBarColor", 0);
        if (toolBarColor != 0) {
            ColorModel colorModel = new ColorModel();
            colorModel.toolBarColor = toolBarColor;
            colorModel.statusBarColor = ApplicationName.settingsShare.getInt("statusBarColor", 0);
            colorModel.textColor = ApplicationName.settingsShare.getInt("textColor", 0);
            colorModel.chatBg = ApplicationName.settingsShare.getInt("chatBgColor", 0);
            if (colorModel.statusBarColor != 0 && colorModel.textColor != 0 && colorModel.chatBg != 0) {
                ApplicationName.colors = colorModel;
            }
        }
    }

    public static Context getAppContext() {
        return ApplicationName.context;
    }
    public static SQLiteDatabase getDb() { return ApplicationName.db; }

    public static User getUsers(int _id) {
        final int id = Math.abs(_id);
        if (id == 308205829) {
            User user = new User();
            user.photo = "https://pp.vk.me/c629318/v629318970/4e5f/FhwWOFPpwog.jpg";
            user.photo_big = "https://pp.vk.me/c629318/v629318970/4e5e/I_57Lek9y1M.jpg";
            user.id = 308205829;
            user.text = "Агент поддержки";
            user.name = "Rocket мессенджер";
            user.online = true;
            user.domain = "id308205829";
            user.status = "id308205829";
            user.last_seen = 0;
            user.platform = 1;
            user.bdate = "";
            user.blacklisted = false;
            user.sex = 0;
            return user;
        }
        if (ApplicationName.users.containsKey(id)) {
            return ApplicationName.users.get(id);
        } else {
            return AndroidUtils.defaultUser(id);
        }
    }
    public static void addUser(int id, User user) {
        try {
            if (ApplicationName.users.containsKey(id)) {
                ApplicationName.users.remove(id);
                ApplicationName.users.remove(ApplicationName.users.get(id));
            }
        } catch (NullPointerException e) {  } catch (Exception e) { }
        ApplicationName.users.put(id, user);
    }

    public static boolean isUser(int id) {
        return ApplicationName.users.containsKey(id);
    }
    public static String getStr(int id) {
        return ApplicationName.getAppContext().getString(id);
    }
    public static void setUserId(int id) {
        ApplicationName.userId = id;
    }

    public static int getUserId() {
        return ApplicationName.userId;
    }

    public static String getAccessToken() {
        return ApplicationName.access_token;
    }
    public static void setUserInfo(String token, int user_id) {
        ApplicationName.userId = user_id;
        ApplicationName.access_token = token;
    }

    public static boolean getSetting(String key) {
        return ApplicationName.settings.containsKey(key) ? ApplicationName.settings.get(key) : true;
    }

    public static void setSettings(String key, boolean checked) {
        ApplicationName.settingsShare.edit().putBoolean(key, checked).apply();
        ApplicationName.settings.put(key, checked);
    }

    public static void setBgMsg(String path, boolean vk) {
        ApplicationName.settingsShare
                .edit()
                .putBoolean("vkBgMs", vk)
                .putString("bgMsg", path)
                .apply();
        ApplicationName.bgMsg = path;
        ApplicationName.vkBgMsg = vk;
    }

    public static void setAppColor(ColorModel colorModel) {
        ApplicationName.settingsShare
                .edit()
                .putInt("toolBarColor", colorModel.toolBarColor)
                .putInt("statusBarColor", colorModel.statusBarColor)
                .putInt("textColor", colorModel.textColor)
                .putInt("chatBgColor", colorModel.chatBg)
                .apply();
        ApplicationName.colors = colorModel;
    }

    public static void showPhoto(String photo, String small) {
        ApplicationName.getAppContext().startActivity(
                new Intent(
                        ApplicationName.getAppContext(),
                        PhotoView.class
                ).putExtra("photo", photo)
                        .putExtra("photo_small", small)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        );
    }

    public static void startVideo(String poster, final String id) {
        if (false) { // getSetting(Constants.VIDEO_PLAYER)) {
            ApplicationName.getAppContext().startService(
                    new Intent(
                            ApplicationName.getAppContext(),
                            VideoPlayer.class
                    ).putExtra("poster", poster).putExtra("id", id)
            );
        } else {
            new Async() {

                ProgressDialog progressDialog;

                @Override
                protected Object background() throws VKException {
                    HashMap<String, Object> post = new HashMap<>();
                    post.put("videos", id);
                    try {
                        return new VK(ApplicationName.getAppContext()).method("video.get").params(post).getObject().getJSONArray("items").getJSONObject(0);
                    } catch (JSONException e) {
                        return null;
                    }
                }

                @Override
                protected void error(VKException error) {
                    progressDialog.dismiss();
                }

                @Override
                protected void finish(Object json) {
                    progressDialog.dismiss();
                    if (json != null) {
                        JSONObject video = (JSONObject) json;
                        if (video.has("player")) {
                            try {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(video.getString("player")));
                                browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                ApplicationName.getAppContext().startActivity(browserIntent);
                            } catch (JSONException e) {

                            } catch (Exception e) {

                            }
                        }
                    }
                }

                @Override
                protected void start() {
                    progressDialog = new ProgressDialog(baseActivity);
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.setMessage("Получаю ссылку..");
                    progressDialog.show();
                    // Toast.makeText(ApplicationName.getAppContext(), "Получаю ссылку..", Toast.LENGTH_LONG).show();
                }
            }.execute();
        }
    }


    public static void logout() {
        SharedPreferences share = ApplicationName.getAppContext().getSharedPreferences(
                ApplicationName.getAppContext().getPackageName(),
                MODE_PRIVATE
        );
        if (share.getBoolean("isLogin", false)) {
            ApplicationName.getAppContext().getSharedPreferences(
                    ApplicationName.getAppContext().getPackageName(),
                    MODE_PRIVATE
            ).edit().putString("access_token", "").putBoolean("isLogin", false).apply();
            ApplicationName.getDb().execSQL("DELETE FROM object");
            ApplicationName.getDb().execSQL("DELETE FROM dialogs");
            ApplicationName.getDb().execSQL("DELETE FROM msg");
            ApplicationName.getDb().execSQL("DELETE FROM audio");
            ApplicationName.getDb().execSQL("DELETE FROM video");
            ApplicationName.getDb().execSQL("DELETE FROM doc");
            ApplicationName.getDb().execSQL("DELETE FROM photo_vk");
            ApplicationName.getDb().execSQL("DELETE FROM album_vk");
            ApplicationName.getDb().execSQL("DELETE FROM friends");
            ApplicationName.getDb().execSQL("DELETE FROM media_msg");
            ApplicationName.getDb().execSQL("DELETE FROM chats");
            ApplicationName.getDb().execSQL("DELETE FROM fave");
            ApplicationName.getDb().execSQL("DELETE FROM stickers");
            ApplicationName.getDb().execSQL("DELETE FROM top");
            Intent intent = new Intent(ApplicationName.getAppContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ApplicationName.getAppContext().startActivity(intent);
        }
    }

    public static void redirectUrl(String url) {
        Intent intent = new Intent(ApplicationName.getAppContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("url", url);
        ApplicationName.getAppContext().startActivity(intent);
    }
}
