package sweet.messager.vk.utils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.TypefaceSpan;
import android.util.DisplayMetrics;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

import sweet.messager.vk.ApplicationName;
import sweet.messager.vk.ImActivity;
import sweet.messager.vk.MultimediaActivity;
import sweet.messager.vk.R;
import sweet.messager.vk.model.ChatModel;
import sweet.messager.vk.model.User;
import sweet.messager.vk.vk.VKApi;

/**
 * Created by antonpolstyanka on 08.05.15.
 */
public class AndroidUtils {


    public static int[] arrColorsButtons = {
            Color.parseColor("#D32F2F"),
            Color.parseColor("#C2185B"),
            Color.parseColor("#7B1FA2"),
            Color.parseColor("#512DA8"),
            Color.parseColor("#303F9F"),
            Color.parseColor("#1976D2"),
            Color.parseColor("#0288D1"),
            Color.parseColor("#0097A7"),
            Color.parseColor("#00796B"),
            Color.parseColor("#388E3C"),
            Color.parseColor("#689F38"),
            Color.parseColor("#EF6C00"),
            Color.parseColor("#E64A19"),
            Color.parseColor("#455A64")
    };


    private static final Hashtable<String, Typeface> typefaceCache = new Hashtable<>();
    public static float density = 1;
    public static float scaledDensity = 1;
    private static Boolean isTablet = null;

    static {
        density = ApplicationName.getAppContext().getResources().getDisplayMetrics().density;
        scaledDensity = ApplicationName.getAppContext().getResources().getDisplayMetrics().scaledDensity;
    }

    public static int random(int min, int max) {
        return new Random().nextInt(max - min + 1) + min;
    }

    public static int dp(float value) {
        return (int)Math.ceil(density * value);
    }
    public static int sp(float value) {
        return (int) Math.ceil(scaledDensity * value);
    }

    public static float dpFromPx(final Context context, final float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    public static int pxFromDp(final Context context, final float dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    public static int dpToPx(final Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    public static boolean isTablet() {
        if (isTablet == null) {
            isTablet = ApplicationName.getAppContext().getResources().getBoolean(R.bool.isTablet);
        }
        return isTablet;
    }

    public static void runOnUIThread(Runnable runnable) {
        runOnUIThread(runnable, 0);
    }

    public static void runOnUIThread(Runnable runnable, long delay) {
        if (delay == 0) {
            ApplicationName.applicationHandler.post(runnable);
        } else {
            ApplicationName.applicationHandler.postDelayed(runnable, delay);
        }
    }

    public static Spannable replaceTags(String str) {
        try {
            int start = -1;
            int startColor = -1;
            int end = -1;
            StringBuilder stringBuilder = new StringBuilder(str);
            while ((start = stringBuilder.indexOf("<br>")) != -1) {
                stringBuilder.replace(start, start + 4, "\n");
            }
            while ((start = stringBuilder.indexOf("<br/>")) != -1) {
                stringBuilder.replace(start, start + 5, "\n");
            }
            ArrayList<Integer> bolds = new ArrayList<>();
            ArrayList<Integer> colors = new ArrayList<>();
            while ((start = stringBuilder.indexOf("<b>")) != -1 || (startColor = stringBuilder.indexOf("<c")) != -1) {
                if (start != -1) {
                    stringBuilder.replace(start, start + 3, "");
                    end = stringBuilder.indexOf("</b>");
                    stringBuilder.replace(end, end + 4, "");
                    bolds.add(start);
                    bolds.add(end);
                } else if (startColor != -1) {
                    stringBuilder.replace(startColor, startColor + 2, "");
                    end = stringBuilder.indexOf(">", startColor);
                    int color = Color.parseColor(stringBuilder.substring(startColor, end));
                    stringBuilder.replace(startColor, end + 1, "");
                    end = stringBuilder.indexOf("</c>");
                    stringBuilder.replace(end, end + 4, "");
                    colors.add(startColor);
                    colors.add(end);
                    colors.add(color);
                }
            }
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(stringBuilder);
            for (int a = 0; a < bolds.size() / 2; a++) {
                spannableStringBuilder.setSpan(new TypefaceSpan(String.valueOf(AndroidUtils.getTypeface("fonts/rmedium.ttf"))), bolds.get(a * 2), bolds.get(a * 2 + 1), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            for (int a = 0; a < colors.size() / 3; a++) {
                spannableStringBuilder.setSpan(new ForegroundColorSpan(colors.get(a * 3 + 2)), colors.get(a * 3), colors.get(a * 3 + 1), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            return spannableStringBuilder;
        } catch (Exception e) {
            Log.e("tmessages", String.valueOf(e));
        }
        return new SpannableStringBuilder(str);
    }

    public static Typeface getTypeface(String assetPath) {
        synchronized (typefaceCache) {
            if (!typefaceCache.containsKey(assetPath)) {
                try {
                    Typeface t = Typeface.createFromAsset(ApplicationName.getAppContext().getAssets(), assetPath);
                    typefaceCache.put(assetPath, t);
                } catch (Exception e) {
                    Log.e("Typefaces", "Could not get typeface '" + assetPath + "' because " + e.getMessage());
                    return null;
                }
            }
            return typefaceCache.get(assetPath);
        }
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static String declOfNum(int number, String[] titles) {
        int[] cases = new int[]{2, 0, 1, 1, 1, 2};
        return number + " " + titles[(number % 100 > 4 && number % 100 < 20) ? 2 : cases[(number % 10 < 5) ? number % 10 : 5]];
    }

    public static String convertStreamToString(InputStream is) throws IOException {
        InputStreamReader r = new InputStreamReader(is);
        StringWriter sw = new StringWriter();
        char[] buffer = new char[1024];
        try {
            for (int n; (n = r.read(buffer)) != -1;)
                sw.write(buffer, 0, n);
        }
        finally{
            try {
                is.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        return sw.toString();
    }

    public static int rand(int min, int max) {
        return new Random().nextInt(max - min + 1) + min;
    }


    public static List<JSONObject> getThumb(String mId, boolean phone) {
        Uri uri;
        List<JSONObject> listOfAllImages = new ArrayList<>();
        boolean getPhonePhoto = true;
        try {
            String state = Environment.getExternalStorageState();
            if (phone || !Environment.MEDIA_MOUNTED.equals(state)) {
                uri = android.provider.MediaStore.Images.Thumbnails.INTERNAL_CONTENT_URI;
                getPhonePhoto = false;
            } else {
                uri = android.provider.MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI;
            }
        } catch (Exception e) {
            uri = android.provider.MediaStore.Images.Thumbnails.INTERNAL_CONTENT_URI;
        }
        String[] projection = {
                MediaStore.Images.Thumbnails.DATA,
                MediaStore.Images.Thumbnails.IMAGE_ID
        };
        Cursor cursor = ApplicationName.getAppContext().getContentResolver().query(uri, projection, MediaStore.Images.Thumbnails.IMAGE_ID + " = ?", new String[] {
                mId
        }, MediaStore.Images.Thumbnails.IMAGE_ID + " DESC LIMIT 1");
        if (cursor == null) return listOfAllImages;
        int column_index_data = cursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.DATA);
        int column_index_id = cursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.IMAGE_ID);
        JSONObject item;

        while (cursor.moveToNext()) {
            try {
                item = new JSONObject();
                item.put("thumb", cursor.getString(column_index_data));
                item.put("id", cursor.getString(column_index_id));
                listOfAllImages.add(item);
                return listOfAllImages;
            } catch (JSONException e) { }
        }
        cursor.close();
        if (getPhonePhoto) {
            listOfAllImages.addAll(getThumb(mId, true));
        }
        List<Integer> ids = new ArrayList<>();
        HashMap<Integer, JSONObject> photos = new HashMap<>();
        try {
            for (int i = 0; i < listOfAllImages.size(); i++) {
                JSONObject photo = listOfAllImages.get(i);
                int id = photo.getInt("id");
                ids.add(id);
                photos.put(id, photo);
            }
        } catch (JSONException e) { }
        Collections.sort(ids);
        Collections.reverse(ids);
        listOfAllImages = new ArrayList<>();
        try {
            for (int i = 0; i < ids.size(); i++) {
                listOfAllImages.add(photos.get(ids.get(i)));
            }
        } catch (NullPointerException e) { }
        return listOfAllImages;
    }

    public static String getThumbPath(String id) {
        List<JSONObject> items = AndroidUtils.getThumb(id, false);
        if (items.size() == 0) {
            return null;
        } else {
            try {
                return items.get(0).getString("thumb");
            } catch (JSONException e) {
                return null;
            }
        }
    }

    public static String last_date(int sex, int last) {
        String _return = sex == 1 ? "заходила" : "заходил";
        if (sex == 0) {
            _return = "заходил (а)";
        }
        Date date = new Date(last * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM");
        sdf.setTimeZone(TimeZone.getDefault());
        String time = sdf.format(date);
        sdf = new SimpleDateFormat("dd.MM");
        String _this = sdf.format(new Date());
        if (_this.equals(time)) {
            sdf = new SimpleDateFormat("hh:mm");
            sdf.setTimeZone(TimeZone.getDefault());
            _return += " сегодня в " + sdf.format(date);
        } else {
            sdf = new SimpleDateFormat("dd.MM в hh:mm");
            sdf.setTimeZone(TimeZone.getDefault());
            _return += " " + sdf.format(date);
        }
        return _return;
    }

    public static void goBrowse(String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ApplicationName.getAppContext().startActivity(i);
    }


    public static void goChat(ChatModel chatModel) {
        Intent intent = new Intent(
                ApplicationName.getAppContext(),
                ImActivity.class
        );
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("title", chatModel.chat_title);
        intent.putExtra("text", chatModel.text);
        intent.putExtra("id", chatModel.id);
        intent.putExtra("chat", true);
        if (!chatModel.no_photo) {
            intent.putExtra("photo", chatModel.photo_big);
        }
        ApplicationName.getAppContext().startActivity(intent);
    }

    public static void goMultimedia(boolean chat, int id) {
        Intent intent = new Intent(
                ApplicationName.getAppContext(),
                MultimediaActivity.class
        );
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("id", id);
        intent.putExtra("chat", chat ? 1 : 0);
        ApplicationName.getAppContext().startActivity(intent);
    }

    public static void goUser(User user) {
        ApplicationName.getAppContext().startActivity(
                new Intent(
                        ApplicationName.getAppContext(),
                        ImActivity.class
                ).putExtra("id", user.id).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        );
    }

    public static void goUser(int id) {
        goUser(ApplicationName.getUsers(id));
    }

    public static User defaultUser(int id) {
        if (id != 0) {
            VKApi.addUser(id);
        }
        User user = new User();
        user.photo = "http://vk.com/images/camera_c.gif";
        user.photo_big = "http://vk.com/images/camera_b.gif";
        user.id = id;
        user.text = "Online";
        user.name = "Загрузка..";
        user.online = true;
        user.domain = "id" + id;
        user.status = "id" + id;
        user.last_seen = 0;
        user.platform = 1;
        user.bdate = "";
        user.blacklisted = false;
        user.sex = 0;
        return user;
    }

    public static ChatModel defaultChat() {
        ChatModel chatModel = new ChatModel();
        chatModel.msg_id = 0;
        chatModel.read = true;
        chatModel.out = false;
        chatModel.user_id = ApplicationName.getUserId();
        chatModel.date = "";
        chatModel.id = ApplicationName.getUserId();
        chatModel.body = "Загрузка..";
        return chatModel;
    }

    public static String chatEvent(boolean my, int sex, String text) {
        if (!my) {
            switch (sex) {
                case 0:
                    text += text + " (a)";
                    break;
                case 1:
                    text += text + "a";
                    break;
            }
        } else {
            text += text + "и";
        }
        return text;
    }

    public static String chatEvent(boolean my, int sex, String[] _text) {
        String text = _text[0];
        if (!my) {
            switch (sex) {
                case 1:
                    text = _text[2];
                    break;
                case 2:
                    text = _text[1];
                    break;
                default:
                    text = _text[1] + " (a)";
                    break;
            }
        }
        return text;
    }

    public static String trim(String text, int size) {
        try {
            if (text.length() > size) {
                return text.substring(0, size);
            } else {
                return text;
            }
        } catch (NullPointerException e) {
            return "Загрузка..";
        }
    }

    public static String thisDate() {
        return ApplicationName.simpleDateFormat
                .format(
                        new Date(

                        )
                );
    }
}
