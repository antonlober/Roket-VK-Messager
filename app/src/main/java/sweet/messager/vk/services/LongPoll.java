package sweet.messager.vk.services;


import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import sweet.messager.vk.ApplicationName;
import sweet.messager.vk.Constants;
import sweet.messager.vk.ImActivity;
import sweet.messager.vk.R;
import sweet.messager.vk.api.Async;
import sweet.messager.vk.api.Http;
import sweet.messager.vk.api.VK;
import sweet.messager.vk.api.VKException;
import sweet.messager.vk.db.Method;
import sweet.messager.vk.interfaces.LongPollListener;
import sweet.messager.vk.model.ChatModel;
import sweet.messager.vk.model.LongPollModel;
import sweet.messager.vk.model.User;
import sweet.messager.vk.utils.AndroidUtils;
import sweet.messager.vk.utils.Body;
import sweet.messager.vk.vk.GetMsg;
import sweet.messager.vk.vk.VKApi;

public class LongPoll extends Service {

    public interface OnDialogUpdate {
        void onNewMsg(ChatModel chat);
        void onReadMsg(String id, int msg_id);
        void onTypingMsg(String id);
    }


    int muser_id;
    public String mId = null;
    private LongPollModel longPollModel = new LongPollModel();

    public OnDialogUpdate longPollListener = null;
    public LongPollListener longPollListenerChat = null;
    private final IBinder mBinder = new LocalBinder();
    private NotificationManager notificationManager;
    private String NOTIFICATION_CLICK;
    private LayoutInflater inflater;
    private View msgView;
    private Dialog dialog;
    private List<Integer> notifications = new ArrayList<>();
    private SharedPreferences share;
    @Override
    public void onCreate() {
        super.onCreate();
        share = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        /* Set Long Poll */
        /*
        longPollModel = new LongPollModel();
        longPollModel.ts = share.getInt("longPoll_TS", 0);
        longPollModel.key = share.getString("longPoll_KEY", null);
        longPollModel.server = share.getString("longPoll_SERVER", null);
         */


        muser_id = share.getInt("user_id", 0);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (muser_id != 0) {
            t.start();
        }
        inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        msgView = inflater.inflate(R.layout.window_box, null, false);


        dialog = new Dialog(LongPoll.this, R.style.MyReplyTheme);
        dialog.setContentView(msgView);
        Window window = dialog.getWindow();
        window.setGravity(Gravity.CENTER);
        window.setLayout(AndroidUtils.dp(260), ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);


        NOTIFICATION_CLICK = getPackageName() + ".NOTIFICATION_CLICK";
        IntentFilter intentFilter = new IntentFilter(NOTIFICATION_CLICK);
        registerReceiver(new NotificationClick(), intentFilter);
    }

    public class LocalBinder extends Binder {
        public LongPoll getService() {
            return LongPoll.this;
        }
    }

    public void registerListener(OnDialogUpdate _longPollListener) {
        longPollListener = _longPollListener;
    }

    public void registerChat(LongPollListener _longPollListener, String _id) {
        longPollListenerChat = _longPollListener;
        mId = _id;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private void parseUpdate(JSONArray update) {
        try {
            boolean chat;
            int msg_id;
            int type = update.getInt(0);
            switch (type) {
                case 6:
                case 7:
                    /* Прочитка сообщений */
                    final int owner_id = update.getInt(type == 3 ? 3 : 1);
                    msg_id = update.getInt(2);
                    chat = owner_id > 2000000000;
                    if (longPollListener != null || longPollListenerChat != null) {
                        String strId;
                        if (chat) {
                            strId = "chat" + Math.abs(owner_id - 2000000000);
                        } else {
                            strId = "user" + owner_id;
                        }
                        if (longPollListener != null) {
                            longPollListener.onReadMsg(strId, msg_id);
                        }
                        if (longPollListenerChat != null && type == 7 && mId != null && mId.equals(strId)) {
                            longPollListenerChat.onReadMsg(strId, msg_id);
                        }
                    }
                    if (!chat && notifications.indexOf(owner_id) != -1 && type == 6) {
                        notificationManager.cancel(owner_id);
                        notifications.remove((Object) owner_id);
                    }
                    break;
                case 8:
                case 9:
                    try {
                        final int userId = Math.abs(update.getInt(1));
                        if (type == 8) {
                            ApplicationName.onlineUsers.add(userId);
                        } else {
                            ApplicationName.onlineUsers.remove((Object) userId);
                        }
                    } catch (NullPointerException e) {
                        ApplicationName.onlineUsers = new ArrayList<>();
                        final int userId = Math.abs(update.getInt(1));
                        if (type == 8) {
                            ApplicationName.onlineUsers.add(userId);
                        } else {
                            ApplicationName.onlineUsers.remove((Object) userId);
                        }
                    }
                    break;
                case 61:
                    /* Печатание */
                    final int user_id = update.getInt(1);
                    if (longPollListener != null) {
                        longPollListener.onTypingMsg("user" + user_id);
                    }
                    if (longPollListenerChat != null) {
                        longPollListenerChat.onTypingMsg("user" + user_id);
                    }
                    if (notifications.indexOf(user_id) != -1) {
                        notificationManager.cancel(user_id);
                        notifications.remove((Object) user_id);
                    }
                    break;
                case 4:
                    /* Новые сообщения */
                    int owner = update.getInt(3);
                    chat = owner > 2000000000;
                    String _id = (chat ? "chat" : 0 > owner ? "email" : "user") + owner;
                    msg_id = update.getInt(1);
                    String text = update.getString(6);
                    Integer author = owner;


                    int flag = update.getInt(2);
                    int out = 0;

                    /* flags */
                    final int UNREAD = 1;
                    final int OUTBOX = 2;
                    final int REPLIED = 4;
                    final int IMPORTANT = 8;
                    final int CHAT = 16;
                    final int FRIENDS = 32;
                    final int SPAM = 64;
                    final int DELЕTЕD = 128;
                    final int FIXED = 256;
                    final int MEDIA = 512;


                    if (owner > 2000000000) {
                        try {
                            JSONObject attach = update.getJSONObject(7);
                            int from = attach.has("from") ? attach.getInt("from") : 0;
                            int chat_id = Math.abs(owner - 2000000000);
                            if (attach.has("geo") || attach.has("fwd") || attach.has("attach1_type") || attach.has("source_act")) {
                                String source_act = null, source_mid = null, attach_type;
                                if (attach.has("geo")) {
                                    attach_type = "geo";
                                } else if (attach.has("attach1_type") && !attach.has("source_act")) {
                                    attach_type = attach.getString("attach1_type");
                                } else if (attach.has("fwd")) {
                                    attach_type = "fwd_messages";
                                } else if (attach.has("source_act")) {
                                    attach_type = "source_act";
                                    source_act = attach.getString("source_act");
                                    if (attach.has("source_mid")) {
                                        source_mid = attach.getString("source_mid");
                                    }
                                } else {
                                    attach_type = "Other";
                                }
                                if (longPollListener != null || longPollListenerChat != null) {
                                    try {
                                        JSONObject msg = new JSONObject();
                                        msg.put("id", msg_id);
                                        msg.put("body", text);
                                        msg.put("user_id", from);
                                        msg.put("from_id", from);
                                        msg.put("title", update.getString(5));
                                        msg.put("chat_id", chat_id);
                                        msg.put("out", from == muser_id ? 1 : 0);
                                        msg.put("read_state", 0);
                                        if (text.equals("")) {
                                            msg.put("attach", source_act == null ? Body.titleAttach(attach_type) : Body.chatEventString(source_act, source_mid, from == muser_id, ApplicationName.getUsers(from)));
                                        }
                                        msg.put("date", ((int) (System.currentTimeMillis() / 1000L)));
                                        if (longPollListener != null) {
                                            ChatModel chatModel = new ChatModel();
                                            chatModel.id = (chat_id + 2000000000);
                                            chatModel.chat = true;
                                            chatModel.msg_id = msg_id;
                                            chatModel.read = false;
                                            chatModel.chat_title = update.getString(5);
                                            chatModel.out = from == muser_id;
                                            chatModel.user_id = from;
                                            chatModel.date = AndroidUtils.thisDate();
                                            if (text.equals("")) {
                                                chatModel.isBody = false;
                                                chatModel.attach = source_act == null ? Body.titleAttach(attach_type) : Body.chatEventString(source_act, source_mid, chatModel.out, ApplicationName.getUsers(from));
                                            } else {
                                                chatModel.isBody = true;
                                                chatModel.body = text;
                                            }
                                            longPollListener.onNewMsg(chatModel);
                                        }
                                    } catch (JSONException e) { }
                                }
                                final String strId2 = "chat" + chat_id;
                                final int msgId2 = msg_id;
                                new GetMsg(new GetMsg.OnLoadMsg() {
                                    @Override
                                    public void onMsg(JSONObject msg) {
                                        Method.saveMsg(strId2, msg);
                                        if (longPollListenerChat != null && mId != null && mId.equals(strId2)) {
                                            longPollListenerChat.onNewMsg(msg);
                                        }
                                    }
                                }).execute(msgId2);
                            } else {
                                if (longPollListener != null || longPollListenerChat != null) {
                                    try {
                                        JSONObject msg = new JSONObject();
                                        msg.put("id", msg_id);
                                        msg.put("body", text);
                                        msg.put("user_id", from);
                                        msg.put("from_id", from);
                                        msg.put("chat_id", chat_id);
                                        msg.put("title", update.getString(5));
                                        msg.put("out", from == muser_id ? 1 : 0);
                                        msg.put("read_state", 0);
                                        msg.put("date", ((int) (System.currentTimeMillis() / 1000L)));
                                        if (longPollListener != null) {
                                            ChatModel chatModel = new ChatModel();
                                            chatModel.id = (chat_id + 2000000000);
                                            chatModel.chat = true;
                                            chatModel.msg_id = msg_id;
                                            chatModel.read = false;
                                            chatModel.chat_title = update.getString(5);
                                            chatModel.out = from == muser_id;
                                            chatModel.user_id = from;
                                            chatModel.date = AndroidUtils.thisDate();
                                            chatModel.isBody = true;
                                            chatModel.body = text;
                                            longPollListener.onNewMsg(chatModel);
                                        }
                                        Method.saveMsg("chat" + chat_id, msg);
                                        if (longPollListenerChat != null && mId != null && mId.equals("chat" + chat_id)) {
                                            longPollListenerChat.onNewMsg(msg);
                                        }
                                    } catch (JSONException e) {
                                    }
                                }
                            }
                        } catch (JSONException e) {

                        }
                    } else {
                        switch (flag) {
                            /* Отправил */
                            case UNREAD + OUTBOX:
                            case (UNREAD + CHAT + OUTBOX):
                            case (UNREAD + FRIENDS + OUTBOX):
                            case (UNREAD + FRIENDS + CHAT + OUTBOX):
                                out = 1;
                            /* Получил */
                            case UNREAD:
                            case (UNREAD + CHAT):
                            case (UNREAD + FRIENDS):
                            case (UNREAD + FRIENDS + CHAT):
                                if (ApplicationName.getSetting(Constants.NOTIFICATION) && mId == null && out == 0 && longPollListener == null && longPollListenerChat == null) {
                                    createNotification(text, owner, null, true);
                                }

                                try {
                                    JSONObject msg = new JSONObject();
                                    msg.put("id", msg_id);
                                    msg.put("body", text);
                                    msg.put("user_id", owner);
                                    msg.put("from_id", owner);
                                    msg.put("out", out);
                                    msg.put("read_state", 0);
                                    msg.put("date", ((int) (System.currentTimeMillis() / 1000L)));
                                    if (longPollListener != null) {
                                        ChatModel chatModel = new ChatModel();
                                        chatModel.id = owner;
                                        chatModel.msg_id = msg_id;
                                        chatModel.read = false;
                                        chatModel.out = out == 1;
                                        chatModel.user_id = owner;
                                        chatModel.date = AndroidUtils.thisDate();
                                        chatModel.isBody = true;
                                        chatModel.body = text;
                                        longPollListener.onNewMsg(chatModel);
                                    }
                                    if (longPollListenerChat != null && mId != null && mId.equals("user" + owner)) {
                                        longPollListenerChat.onNewMsg(msg);
                                    }
                                    Method.saveMsg("user" + owner, msg);
                                } catch (JSONException e) {
                                }
                                break;
                            /* Отправил */
                            case UNREAD + MEDIA + OUTBOX:
                            case (UNREAD + CHAT + MEDIA + OUTBOX):
                            case (UNREAD + FRIENDS + MEDIA + OUTBOX):
                            case (UNREAD + FRIENDS + MEDIA + CHAT + OUTBOX):
                                out = 1;
                            /* Получил */
                            case UNREAD + MEDIA:
                            case (UNREAD + CHAT + MEDIA):
                            case (UNREAD + FRIENDS + MEDIA):
                            case (UNREAD + FRIENDS + MEDIA + CHAT):
                            default:
                                JSONObject attach = update.getJSONObject(7);
                                String attach_type;
                                if (attach.has("geo")) {
                                    attach_type = "geo";
                                } else if (attach.has("attach1_type")) {
                                    attach_type = attach.getString("attach1_type");
                                } else if (attach.has("fwd")) {
                                    attach_type = "fwd_messages";
                                } else {
                                    attach_type = "Other";
                                }
                                if (longPollListener != null) {
                                    ChatModel chatModel = new ChatModel();
                                    chatModel.id = owner;
                                    chatModel.msg_id = msg_id;
                                    chatModel.read = false;
                                    chatModel.out = out == 1;
                                    chatModel.user_id = owner;
                                    chatModel.date = AndroidUtils.thisDate();
                                    if (text.equals("")) {
                                        chatModel.isBody = false;
                                        chatModel.attach = Body.titleAttach(attach_type);
                                    } else {
                                        chatModel.isBody = true;
                                        chatModel.body = text;
                                    }
                                    longPollListener.onNewMsg(chatModel);
                                }

                                if (ApplicationName.getSetting(Constants.NOTIFICATION)
                                        && !attach_type.equals("photo")
                                        && mId == null
                                        && out == 0
                                        && longPollListener == null
                                        && longPollListenerChat == null) {
                                    createNotification((text.equals("") ? "[" + Body.titleAttach(attach_type) + "]" : text), owner, null, false);
                                }

                                if (longPollListenerChat != null) {
                                    JSONObject msg = new JSONObject();
                                    msg.put("id", msg_id);
                                    msg.put("body", text);
                                    msg.put("user_id", owner);
                                    msg.put("from_id", owner);
                                    msg.put("out", out);
                                    msg.put("read_state", 0);
                                    msg.put("date", ((int) (System.currentTimeMillis() / 1000L)));
                                    if (text.equals("")) {
                                        msg.put("attach", Body.titleAttach(attach_type));
                                    }
                                    final int msgId = msg_id;
                                    final String strId = "user" + owner;
                                    new GetMsg(new GetMsg.OnLoadMsg() {
                                        @Override
                                        public void onMsg(JSONObject msg) {
                                            Method.saveMsg(strId, msg);
                                            if (longPollListenerChat != null && mId != null && mId.equals(strId)) {
                                                longPollListenerChat.onNewMsg(msg);
                                            }
                                            try {
                                                String bodyMsg = msg.getString("body");
                                                boolean isNotification = false;
                                                String bigPhoto = null;
                                                if (msg.has("attachments")) {
                                                    JSONObject attachment = msg.getJSONArray("attachments").getJSONObject(0);
                                                    if (attachment.getString("type").equals("photo")) {
                                                        isNotification = true;
                                                        JSONObject object = attachment.getJSONObject("photo");
                                                        if (object.has("photo_256")) {
                                                            bigPhoto = object.getString("photo_256");
                                                        } else if (object.has("photo_604")) {
                                                            bigPhoto = object.getString("photo_604");
                                                        } else {
                                                            bigPhoto = object.getString(object.has("130") ? "130" : "photo_64");
                                                        }
                                                    }
                                                }
                                                if (ApplicationName.getSetting(Constants.NOTIFICATION) && isNotification && mId == null && msg.getInt("out") == 0 && longPollListener == null && longPollListenerChat == null) {
                                                    createNotification((bodyMsg.equals("") ? "" : bodyMsg), msg.getInt("user_id"), bigPhoto, false);
                                                }
                                            } catch (JSONException e) {
                                            }
                                        }
                                    }).execute(msgId);
                                }
                                break;
                        }
                    }
                    break;
            }
        } catch (JSONException e) {

        }


    }

    /* New LonPoll Class */

    Thread t = new Thread(new Runnable() {
        @Override
        public void run() {
            while (true) {
                if (longPollModel.ts == 0) {
                    if (setLongPollParams()) {
                        continue; // Переход в начало цикла
                    } else {
                        break; // Параметры не установленны. Остановка цикла
                    }
                }
                JSONObject result = connectLongPoll(); // Подключение к LongPoll
                if (result.has("failed")) { // Если есть ошибка, запросим новые данные для подключения
                    if (setLongPollParams()) {
                        continue; // Переход в начало цикла
                    } else {
                        break; // Параметры не установленны. Остановка цикла
                    }
                } else if (result.has("ts") && result.has("updates")) { // Если все ок, парсим данные
                    try {
                        longPollModel.ts = result.getInt("ts");
                        final JSONArray updates = result.getJSONArray("updates");
                        if (updates.length() == 0) {
                            continue; // Событий нету, переход в начало цикла
                        } else {
                            int length = updates.length();
                            int startId = 0;
                            for (int i = 0; i < length; i++) {
                                final JSONArray update = updates.getJSONArray(i);
                                AndroidUtils.runOnUIThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        parseUpdate(update);
                                    }
                                });
                                /*
                                if (update.getInt(0) == 3) {
                                    if (length == Math.abs(i - 1)) {
                                        AndroidUtils.runOnUIThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                update.remove(1);
                                                update.put(1, startId);
                                                parseUpdate(update);
                                            }
                                        });
                                    } else {
                                        startId = update.getInt(1);
                                    }
                                } else {
                                    AndroidUtils.runOnUIThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            parseUpdate(update);
                                        }
                                    });
                                }
                                 */
                            }
                        }
                    } catch (JSONException e) {

                    }
                } else {
                    break; // Данных нет. Останавливаем цикл
                }
            }
        }
    });


    private JSONObject connectLongPoll() {
        try {
            StringBuilder params = new StringBuilder("http://");
            params.append(longPollModel.server);
            params.append("?ts=");
            params.append(longPollModel.ts);
            params.append("&key=");
            params.append(longPollModel.key);
            params.append("&act=a_check&wait=25&mode=2");
            String longPollHttp = VKApi.http(params.toString());
            if (longPollHttp == null) {
                if (setLongPollParams()) {
                    return connectLongPoll();
                } else {
                    return new JSONObject();
                }
            } else {
                try {
                    return new JSONObject(longPollHttp);
                } catch (JSONException e) {
                    return new JSONObject();
                }
            }
        } catch (NullPointerException e) {
            return new JSONObject();
        }
    }

    public boolean setLongPollParams() {
        LongPollModel model = VKApi.getLongPoll();
        if (model != null) {
            longPollModel = VKApi.getLongPoll();
            share.edit()
                    .putInt("longPoll_TS", longPollModel.ts)
                    .putString("longPoll_KEY", longPollModel.key)
                    .putString("longPoll_SERVER", longPollModel.server)
                    .apply();
            return true;
        } else {
            return false;
        }
    }

    public void createNotification(final String text, final int owner, final String bigPhoto, final boolean reply) {
        if (notifications.indexOf(owner) == -1) {
            notifications.add(owner);
        }
        final User user = ApplicationName.getUsers(owner);
        Intent notificationIntent = new Intent(LongPoll.this, ImActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        notificationIntent.putExtra("id", user.id);
        final PendingIntent contentIntent = PendingIntent.getActivity(this,
                0, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        Picasso.with(this)
                .load(user.photo)
                .resize(AndroidUtils.dp(50), AndroidUtils.dp(50))
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        if (bigPhoto == null) {
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(LongPoll.this);
                            builder.setContentIntent(contentIntent)
                                    .setSmallIcon(R.drawable.ic_nofitication)
                                    .setTicker(user.name + ": " + text)
                                    .setWhen(System.currentTimeMillis())
                                    .setAutoCancel(true)
                                    .setLargeIcon(bitmap)
                                    .setContentTitle(user.name)
                                    .setContentText(text);

                            if (reply) {
                                Intent replyReceive = new Intent(NOTIFICATION_CLICK);
                                replyReceive.putExtra("owner", owner);
                                replyReceive.putExtra("body", text);
                                PendingIntent pendingIntentYes = PendingIntent.getBroadcast(LongPoll.this, 12345, replyReceive, PendingIntent.FLAG_UPDATE_CURRENT);
                                builder.addAction(R.drawable.ic_reply_msg, "Быстрый ответ", pendingIntentYes);
                            }


                            Notification notification = builder.build();
                            notificationManager.notify(owner, notification);
                        } else {
                            photoNotification(bitmap, user.name, bigPhoto, text, owner);
                        }
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });
    }


    private void photoNotification(final Bitmap photo, final String name, String img, final String text, final int ownerId) {
        Picasso.with(this).load(img).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                NotificationCompat.BigPictureStyle notiStyle = new NotificationCompat.BigPictureStyle();
                notiStyle.setBigContentTitle(name);
                notiStyle.setSummaryText(text);
                notiStyle.bigPicture(bitmap);


                User user = ApplicationName.getUsers(ownerId);
                Intent notificationIntent = new Intent(LongPoll.this, ImActivity.class);
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                notificationIntent.putExtra("id", user.id);
                final PendingIntent contentIntent = PendingIntent.getActivity(LongPoll.this,
                        0, notificationIntent,
                        PendingIntent.FLAG_CANCEL_CURRENT);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(LongPoll.this);
                builder.setContentIntent(contentIntent)
                        .setSmallIcon(R.drawable.ic_nofitication)
                        .setTicker(name + ": " + text)
                        .setStyle(notiStyle)
                        .setWhen(System.currentTimeMillis())
                        .setAutoCancel(true)
                        .setLargeIcon(photo);


                Notification notification = builder.build();
                notificationManager.notify(ownerId, notification);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });
    }


    class NotificationClick extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            final int userId = intent.getIntExtra("owner", 0);
            notificationManager.cancel(userId);
            User user = ApplicationName.getUsers(userId);
            TextView body = (TextView) msgView.findViewById(R.id.body);
            body.setText(intent.getStringExtra("body"));
            ((TextView) msgView.findViewById(R.id.title)).setText(user.name);
            final EditText inputMsg = (EditText) msgView.findViewById(R.id.inputMsg);
            msgView.findViewById(R.id.btnSend).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String text = String.valueOf(inputMsg.getText());
                    if (!text.equals("")) {
                        inputMsg.setText("");
                        final HashMap<String, Object> post = new HashMap<String, Object>();
                        post.put("message", text);
                        post.put("user_id", userId);
                        new Async() {

                            @Override
                            protected Object background() throws VKException {
                                return new VK(ApplicationName.getAppContext()).method("messages.send").params(post).getInt();
                            }

                            @Override
                            protected void error(VKException error) {
                            }

                            @Override
                            protected void finish(Object json) {
                            }

                            @Override
                            protected void start() {

                            }
                        }.execute();
                    }
                    dialog.dismiss();
                }
            });

            dialog.show();
        }
    }

}
