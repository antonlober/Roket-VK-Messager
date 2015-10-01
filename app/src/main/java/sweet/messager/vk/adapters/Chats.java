package sweet.messager.vk.adapters;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import sweet.messager.vk.ApplicationName;
import sweet.messager.vk.R;
import sweet.messager.vk.model.ChatModel;
import sweet.messager.vk.model.User;
import sweet.messager.vk.services.LongPoll;
import sweet.messager.vk.utils.AndroidUtils;
import sweet.messager.vk.utils.Body;
import sweet.messager.vk.utils.CircleTransform;


public class Chats extends RecyclerView.Adapter<Chats.ViewHolder> implements LongPoll.OnDialogUpdate {

    public List<ChatModel> items = new ArrayList<>();
    public List<Integer> users = new ArrayList<>();
    public List<Integer> chats = new ArrayList<>();
    boolean pause = false;

    @Override
    public void onNewMsg(final ChatModel dialog) {
        try {
            boolean addList = true;
            String id = (dialog.chat ? "chat" : "user") + dialog.id;
            final int position = msgPosition.indexOf(id);
            ChatModel item;
            if (position != -1) {
                item = items.get(position);
                item.typing = false;
                notifyItemMoved(position, 0);
                items.remove(item);
                msgPosition.remove(id);
            } else {
                item = new ChatModel();
            }
            if (dialog.out) {
                dialog.unread = 0;
            } else {
                dialog.unread = (item.unread + 1);
            }
            if (dialog.chat && item.chat) {
                dialog.text = item.text;
                dialog.prevName = item.prevName;
                dialog.photo = item.photo;
                dialog.photo_big = item.photo_big;
            }
            /*
            if (msg.has("chat_id")) {
                if (message.has("chat_active") && message.has("users_count") && message.has("admin_id")) {
                    msg.put("chat_active", message.getString("chat_active"));
                    msg.put("users_count", message.getInt("users_count"));
                    msg.put("admin_id", message.getInt("admin_id"));
                    msg.put("photo_50", message.has("photo_50") ? message.getString("photo_50") : "http://vk.com/images/camera_c.gif");
                } else {
                    addList = false;
                    final int finalUnread = unread;
                    final String finalId = id;
                    new Async() {

                        @Override
                        protected Object background() throws VKException {
                            try {
                                HashMap<String, Object> post = new HashMap<>();
                                post.put("chat_id", msg.getInt("chat_id"));
                                post.put("fields", "sex,photo_50,photo_100,domain,online");
                                JSONObject chat = new VK(ApplicationName.getAppContext()).method("messages.getChat").params(post).getObject();
                                JSONArray users = chat.getJSONArray("users");
                                JSONArray chat_active = new JSONArray();
                                int count = users.length();
                                for (int i = 0; i < count; i++) {
                                    JSONObject user = users.getJSONObject(i);
                                    if (user.has("online")) {
                                        int online = user.getInt("online");
                                        if (online == 1) {
                                            chat_active.put(user.getInt("id"));
                                        }
                                        user.remove("online");
                                    }
                                    if (!ApplicationName.isUser(user.getInt("id"))) {
                                        Method.addUser(User.parse(user));
                                    }
                                }
                                chat.put("users_count", count);
                                chat.put("chat_active", chat_active.toString());
                                return chat;
                            } catch (JSONException e) {
                                Log.e("errorJSON #2", String.valueOf(e));
                            }
                            return null;
                        }

                        @Override
                        protected void error(VKException error) {
                            Log.e("errorJSON #4", "VKError");
                        }

                        @Override
                        protected void finish(Object json) {
                            if (json != null) {
                                try {
                                    JSONObject chat = (JSONObject) json;
                                    msg.put("users_count", chat.getInt("users_count"));
                                    msg.put("admin_id", chat.getInt("admin_id"));
                                    msg.put("photo_50", chat.has("photo_50") ? chat.getString("photo_50") : "http://vk.com/images/camera_c.gif");
                                    msg.put("chat_active", chat.getString("chat_active"));
                                    JSONObject nMsg = new JSONObject().put("message", msg).put("unread", finalUnread);
                                    items.add(0, ChatModel.parse(nMsg));
                                    msgPosition.add(0, finalId);
                                    if (position != -1) {
                                        notifyItemChanged(0);
                                    } else {
                                        notifyItemInserted(0);
                                    }
                                } catch (JSONException e) {
                                    Log.e("errorJSON #3", String.valueOf(e));
                                }
                            }
                        }

                        @Override
                        protected void start() {

                        }
                    }.execute();
                }
            }
             */
            if (addList) {
                items.add(0, dialog);
                msgPosition.add(0, id);
                if (position != -1) {
                    notifyItemChanged(0);
                } else {
                    notifyItemInserted(0);
                }
            }
        } catch (NullPointerException e) {
        }
    }

    @Override
    public void onReadMsg(String id, int msg_id) {
        try {
            final int position = msgPosition.indexOf(id);
            if (position != -1) {
                ChatModel item = items.get(position);
                if (item.msg_id == msg_id) {
                    item.unread = 0;
                    item.read = true;
                    items.remove(item);
                    items.add(position, item);
                    notifyItemChanged(position);
                }
            }
        } catch (NullPointerException e) { }
    }

    @Override
    public void onTypingMsg(String id) {
        try {
            final int position = msgPosition.indexOf(id);
            if (position != -1) {
                ChatModel item = items.get(position);
                item.typing = true;
                items.remove(item);
                items.add(position, item);
                notifyItemChanged(position);
            }
        } catch (NullPointerException e) {

        }
    }

    public interface OnItemClickListener {
        void onClick(ChatModel item);
    }


    LongPoll mService = null;
    boolean mBound;

    ArrayList<String> msgPosition = new ArrayList<String>();

    ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ((LongPoll.LocalBinder) service).getService();
            mService.registerListener(Chats.this);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };

    Context context;

    OnItemClickListener onItemClickListener;

    public Chats(List<ChatModel> _items, OnItemClickListener _onItemClickListener) {
        super();
        context = ApplicationName.getAppContext();
        items = _items;
        int size = items.size();
        /*
        if (size == ApplicationName.dialogsCount) {
            try {
                JSONObject endText = new JSONObject();
                endText.put("end_text", "Показано " + AndroidUtils.declOfNum(size, new String[]{
                        "диалог",
                        "диалога",
                        "диалогов"
                }));
                items.add(endText);
                Log.e("offsetDialogs", "create end");
            } catch (JSONException e) {
                Log.e("offsetDialogs", String.valueOf(e));
            }
        }
         */

        onItemClickListener = _onItemClickListener;
        for (int i = 0; i < _items.size(); i++) {
            ChatModel item = items.get(i);
            try {
                String strId = (item.chat ? "chat" : "user") + item.id;
                if (msgPosition.indexOf(strId) == -1) {
                    msgPosition.add(strId);
                }
            } catch (NullPointerException e) {
                items.add(new ChatModel());
            }
        }

        context.bindService(
                new Intent(
                        context,
                        LongPoll.class
                ),
                mServiceConnection,
                Context.BIND_AUTO_CREATE
        );
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(i == 1 ? R.layout.title_line : R.layout.chat, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {
        boolean html = false;
        final ChatModel item = items.get(i);
        final User user = ApplicationName.getUsers(item.user_id);
        String title = item.chat ? item.chat_title : user.name;
        String photo = null;
        try {
            photo = item.chat ? (item.no_photo ? null : item.photo_big) : (user.no_photo ? null : user.photo_big);
        } catch (NullPointerException ignored) { }

        String body;
        if (item.isBody) {
            body = item.body;
        } else {
            body = Body.getHtml().replace("*", item.attach);
            html = true;
        }
        viewHolder.time.setText(item.date);
        viewHolder.name.setText(title);
        if (item.chat) {
            viewHolder.top.removeView(viewHolder.chat);
            viewHolder.chat.setVisibility(View.VISIBLE);
            viewHolder.chat.setImageResource(R.mipmap.ic_group);
            viewHolder.top.addView(viewHolder.chat, 0);
        } else if (ApplicationName.onlineUsers.indexOf(item.user_id) != -1) {
            viewHolder.top.removeView(viewHolder.chat);
            viewHolder.chat.setVisibility(View.VISIBLE);
            viewHolder.chat.setImageResource(R.mipmap.ic_online);
            viewHolder.top.addView(viewHolder.chat, 1);
        } else {
            viewHolder.chat.setVisibility(View.GONE);
        }

        if (item.out) {
            body = (item.user_id == 0 ? "" : Body.getHtml().replace("*", "Вы:")) + " " + body;
            html = true;
            viewHolder.count.setVisibility(View.GONE);
            viewHolder.status.setVisibility(View.VISIBLE);
            viewHolder.status.setImageResource(item.read ? R.mipmap.ic_done : R.mipmap.ic_done_one);
        } else {
            viewHolder.count.setVisibility(item.read || item.unread == 0 ? View.GONE : View.VISIBLE);
            viewHolder.status.setVisibility(View.GONE);
            viewHolder.count.setText(String.valueOf(
                    item.unread
            ));
            if (item.chat) {
                body = (item.user_id == 0 ? "" : Body.getHtml().replace("*", user.name + ":")) + " " + body;
                html = true;
            }
        }
        if (item.typing) {
            body = Body.getHtml().replace("*", "Печатает...");
            html = true;
        }
        viewHolder.body.setText(html ? Html.fromHtml(body) : body);
        /*
        String[] split = photo.split("\\/");
        String[] path = split[Math.abs(split.length - 1)].split("\\.");
        String format = path[Math.abs(path.length - 1)];
         */

        viewHolder.photoText.setVisibility(View.VISIBLE);
        viewHolder.photoText.setText((item.chat ? item.prevName : user.prevName).toUpperCase());
        viewHolder.photo.setBackgroundResource(R.drawable.count);
        viewHolder.photo.setImageResource(R.drawable.transparent);
        if (photo != null) {
            Picasso.with(context).load(photo).transform(new CircleTransform()).into(viewHolder.photo, new Callback() {
                @Override
                public void onSuccess() {
                    viewHolder.photoText.setVisibility(View.GONE);
                }

                @Override
                public void onError() {
                    viewHolder.photoText.setVisibility(View.VISIBLE);
                    viewHolder.photoText.setText(item.chat ? item.prevName : user.prevName);
                    viewHolder.photo.setBackgroundResource(R.drawable.count);
                    viewHolder.photo.setImageResource(0);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        TextView name, body, time, count, photoText;
        ImageView chat, status, photo;
        LinearLayout top;
        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            name = (TextView) itemView.findViewById(R.id.name);
            body = (TextView) itemView.findViewById(R.id.body);
            time = (TextView) itemView.findViewById(R.id.time);
            count = (TextView) itemView.findViewById(R.id.count);
            photo = (ImageView) itemView.findViewById(R.id.photo);
            chat = (ImageView) itemView.findViewById(R.id.chat);
            status = (ImageView) itemView.findViewById(R.id.status);
            top = (LinearLayout) itemView.findViewById(R.id.top);
            photoText = (TextView) itemView.findViewById(R.id.photo_text);
        }

        @Override
        public void onClick(View v) {
            ChatModel chatModel = items.get(getPosition());
            if (chatModel.chat) {
                AndroidUtils.goChat(chatModel);
            } else {
                AndroidUtils.goUser(chatModel.id);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            /*
            final String[] mCatsName = {"Не читать сообщения", "Создать быстрый чат", "Удалить переписку"};

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Опции"); // заголовок для диалога

            builder.setItems(mCatsName, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item) {

                }
            });
            builder.setCancelable(false);
            builder.create().show();
             */
            return false;
        }
    }

    public List<ChatModel> getItems() {
        return items;
    }


    public void onDestroy() {
        try {
            if (mService != null) {
                mService.longPollListener = null;
                mService = null;
            }
            context.unbindService(mServiceConnection);
        } catch (NullPointerException e) {

        } catch (IllegalArgumentException e) {

        } catch (Exception e) {

        }
    }

    public void onPause() {
        try {
            pause = true;
            if (mService != null) {
                mService.longPollListener = null;
                mService = null;
            }
            context.unbindService(mServiceConnection);
        } catch (NullPointerException e) {

        } catch (IllegalArgumentException e) {

        } catch (Exception e) {

        }
    }

    public void onResume() {
        pause = false;
        try {
            context.bindService(
                    new Intent(
                            context,
                            LongPoll.class
                    ),
                    mServiceConnection,
                    Context.BIND_AUTO_CREATE
            );
        } catch (NullPointerException e) {

        } catch (IllegalArgumentException e) {

        } catch (Exception e) {

        }
    }

    @Override
    public int getItemViewType(int position) {
        return 0; // (items.get(position).has("end_text")) ? 1 : 0;
    }

    public void addChats(List<ChatModel> _items) {
        int size = items.size();
        ChatModel dialog;
        for (int i = 0; i < _items.size(); i++) {
            dialog = _items.get(i);
            String strId = (dialog.chat ? "chat" : "user") + dialog.id;
            if (msgPosition.indexOf(strId) == -1) {
                msgPosition.add(strId);
            } else {
                continue;
            }
            items.add(dialog);
        }
        int newSize = items.size();
        /*
        if (newSize == ApplicationName.dialogsCount) {
            try {
                JSONObject endText = new JSONObject();
                endText.put("end_text", "Показано " + AndroidUtils.declOfNum(newSize, new String[]{
                        "диалог",
                        "диалога",
                        "диалогов"
                }));
                newSize++;
                items.add(endText);
                Log.e("offsetDialogs", "create end");
            } catch (JSONException e) {
                Log.e("offsetDialogs", String.valueOf(e));
            }
        }
         */
        notifyItemRangeInserted(size, newSize);
    }
}
