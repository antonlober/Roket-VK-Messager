package sweet.messager.vk.adapters;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.IBinder;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

import sweet.messager.vk.ApplicationName;
import sweet.messager.vk.Constants;
import sweet.messager.vk.R;
import sweet.messager.vk.api.Async;
import sweet.messager.vk.api.VK;
import sweet.messager.vk.api.VKException;
import sweet.messager.vk.db.Method;
import sweet.messager.vk.interfaces.LongPollListener;
import sweet.messager.vk.interfaces.OnCreateView;
import sweet.messager.vk.model.User;
import sweet.messager.vk.services.LongPoll;
import sweet.messager.vk.ui.ChatActivity;
import sweet.messager.vk.utils.AndroidUtils;
import sweet.messager.vk.utils.Body;
import sweet.messager.vk.utils.CircleTransform;
import sweet.messager.vk.utils.TextViewSpan;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> implements LongPollListener {


    HashMap<Integer, Integer> msgIds = new HashMap<>();
    List<Integer> unReadMsg = new ArrayList<>();
    List<Integer> myUnReadMsg = new ArrayList<>();
    List<Integer> ignoredIds = new ArrayList<>();
    boolean show = false, forward_posts = false, pause = false;

    SoundPool sp = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
    int sound_in, sound_out;

    @Override
    public void onNewMsg(JSONObject msg) {
        if (msg == null) return;
        try {
            if (show) {
                final int msgId = msg.getInt("id");
                if (msgId > 20000000) {
                    forward_posts = true;
                } else if (msg.getInt("out") == 1 && forward_posts) {
                    forward_posts = false;
                    return;
                }
                if (ignoredIds.indexOf(msgId) == -1) {
                    final int bottomPosition = items.size();
                    msgIds.put(msgId, bottomPosition);
                    items.add(msg);
                    notifyItemInserted(bottomPosition);
                    recyclerView.scrollToPosition(bottomPosition);
                    if (ApplicationName.getSetting(Constants.MELODY)) {
                        sp.play(msg.getInt("out") == 1 ? sound_out : sound_in, 1, 1, 0, 0, 1);
                    }
                } else {
                    ignoredIds.remove((Object) msgId);
                    unReadMsg.add(msgId);
                }
            }
            if (msg.getInt("out") == 0 && chatActivity != null) {
                chatActivity.onNewMsg();
            }
        } catch (JSONException e) {
        } catch (NullPointerException e) {
        } catch (IllegalArgumentException e) {
        } catch (Exception e) {
        }
    }

    public void setOriginalId(int _id, int msg_id) {
        ignoredIds.add(msg_id);
        try {
            int position = msgIds.get(_id);
            msgIds.remove(_id);
            msgIds.put(msg_id, position);
            unReadMsg.add(msg_id);
            if (position != -1) {
                try {
                    JSONObject item = items.get(position);
                    items.remove(item);
                    item.remove("sending");
                    item.put("id", msg_id);
                    items.add(position, item);
                    notifyItemChanged(position);
                } catch (JSONException e) {

                }
            }
        } catch (NullPointerException e) {

        }
    }

    public void setErrorMsg(int _id) {
        try {
            int position = msgIds.get(_id);
            if (position != -1) {
                try {
                    JSONObject item = items.get(position);
                    items.remove(item);
                    item.remove("sending");
                    item.put("error", 1);
                    items.add(position, item);
                    notifyItemChanged(position);
                } catch (JSONException e) {

                }
            }
        } catch (NullPointerException e) {

        }
    }

    @Override
    public void onReadMsg(String id, int msg_id) {
        int size = unReadMsg.size();
        // int startId = 0;
        if (size != 0) {
            for (int i = 0; i < size; i++) {
                try {
                    int msgId = unReadMsg.get(i);
                    int position = msgIds.get(msgId);
                    JSONObject item = items.get(position);
                    item.put("read_state", 1);
                    items.remove(item);
                    items.add(position, item);
                    notifyItemChanged(position);
                } catch (NullPointerException e) {
                } catch (JSONException e) {
                }
            }
            /*
            if (startId != 0) {
                notifyItemRangeChanged(startId, Math.abs(items.size() - 1));
            }
             */
            unReadMsg.clear();
        }
    }

    @Override
    public void onTypingMsg(String id) {
        try {
            if (mId.equals(id)) {
                if (chatActivity != null) {
                    chatActivity.onTyping();
                }
            }
        } catch (NullPointerException e) {

        } catch (IllegalArgumentException e) {

        } catch (Exception e) {

        }
    }

    public List<JSONObject> items;
    Context context;
    RecyclerView recyclerView;
    String mId;
    Boolean chat;
    ChatActivity chatActivity = null;
    LongPoll mService = null;
    boolean mBound;

    ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ((LongPoll.LocalBinder) service).getService();
            mService.registerChat(MessagesAdapter.this, getMid());
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };


    public void setFragment(ChatActivity _chatActivity) {
        try {
            chatActivity = _chatActivity;
        } catch (NullPointerException e) {

        } catch (IllegalArgumentException e) {

        } catch (Exception e) {

        }
    }

    public MessagesAdapter(List<JSONObject> _items, boolean isChat, int _id) {
        super();
        Log.e("sizeMsg", String.valueOf(_items.size()));
        context = ApplicationName.getAppContext();
        sound_in = sp.load(context, R.raw.sound_in, 1);
        sound_out = sp.load(context, R.raw.sound_out, 1);
        // Collections.reverse(_items);
        items = _items;
        chat = isChat;
        if (chat) {
            mId = "chat" + Math.abs(_id - 2000000000);
        } else {
            mId = (0 > _id ? "email" : "user") + _id;
        }
        Log.e("emailText", mId);
        show = true;
        context.bindService(
                new Intent(
                        context,
                        LongPoll.class
                ),
                mServiceConnection,
                Context.BIND_AUTO_CREATE
        );

    }

    public static MessagesAdapter setItems(List<JSONObject> _items, boolean isChat, int _id) {
        return new MessagesAdapter(_items, isChat, _id);
    }

    @Override
    public void onAttachedToRecyclerView (RecyclerView _recyclerView) {
        recyclerView = _recyclerView;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(context).inflate(R.layout.msg_view, parent, false)
        );
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        LinearLayout body, bodyMsg, attach, main;
        View toRight;
        ImageView photo, msgStatus, sticker;
        TextView msgText, msgTime, msgAuthor;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            body = (LinearLayout) itemView.findViewById(R.id.body);
            toRight = itemView.findViewById(R.id.toRight);
            photo = (ImageView) itemView.findViewById(R.id.photo);
            msgStatus = (ImageView) itemView.findViewById(R.id.msgStatus);
            msgText = (TextView) itemView.findViewById(R.id.msgText);
            msgTime = (TextView) itemView.findViewById(R.id.msgTime);
            bodyMsg = (LinearLayout) itemView.findViewById(R.id.bodyMsg);
            attach = (LinearLayout) itemView.findViewById(R.id.attach);
            sticker = (ImageView) itemView.findViewById(R.id.sticker);
            main = (LinearLayout) itemView.findViewById(R.id.main);
            msgAuthor = (TextView) itemView.findViewById(R.id.author_name);
        }

        @Override
        public void onClick(View v) {
            JSONObject msg = items.get(getPosition());
            if (msg.has("error")) {

            }
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.attach.removeAllViews();
        holder.sticker.setVisibility(View.GONE);
        try {
            JSONObject msg = items.get(position);
            final int msg_id = msg.getInt("id");
            int user_id = msg.getInt("user_id");
            boolean my = msg.getInt("out") == 1;
            boolean read = msg.getInt("read_state") == 1;
            String body = msg.getString("body");

            if (!read && my) {
                if (unReadMsg.indexOf(msg_id) == -1) {
                    unReadMsg.add(msg_id);
                    msgIds.put(msg_id, position);
                }
            }

            if (!pause &&
                !msg.has("sending") &&
                !msg.has("error") &&
                !read &&
                !my &&
                ApplicationName.getSetting(Constants.READ_MSG)) {
                items.remove(msg);
                msg.put("read_state", 1);
                items.add(position, msg);
                new Async() {

                    @Override
                    protected Object background() throws VKException {
                        HashMap<String, Object> post = new HashMap<>();
                        post.put("message_ids", msg_id);
                        return new VK(ApplicationName.getAppContext()).method("messages.markAsRead").params(post);
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

            if (chat) {
                holder.photo.setVisibility(my ? View.GONE : View.VISIBLE);
                final User author = ApplicationName.getUsers(user_id);
                holder.msgAuthor.setVisibility(my ? View.GONE : View.VISIBLE);
                if (ApplicationName.colors != null) {
                    holder.msgAuthor.setTextColor(ApplicationName.colors.toolBarColor);
                }
                holder.msgAuthor.setText(
                        author.name
                );
                holder.photo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AndroidUtils.goUser(author);
                    }
                });
                holder.msgAuthor.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AndroidUtils.goUser(author);
                    }
                });
                Picasso.with(context)
                        .load(author.photo)
                        .transform(new CircleTransform())
                        .into(holder.photo);
            }

            holder.toRight.setVisibility(my ? View.VISIBLE : View.GONE);
            holder.body.setBackgroundResource(my ? R.drawable.msg_out : R.drawable.msg_in);
            int defaultPadding = AndroidUtils.dp(8);
            if (my) {
                holder.body.setPadding(defaultPadding, defaultPadding, AndroidUtils.dp(16), defaultPadding);
            } else {
                holder.body.setPadding(AndroidUtils.dp(16), defaultPadding, defaultPadding, defaultPadding);
            }

            boolean toBody = true;
            final HashMap<String, Boolean> settingsMsg = new HashMap<>();
            if (msg.has("attachments")) {
                JSONObject sticker = msg.getJSONArray("attachments").getJSONObject(0);
                if (sticker.has("sticker")) {
                    holder.msgAuthor.setVisibility(View.GONE);
                    holder.sticker.setVisibility(View.VISIBLE);
                    sticker = sticker.getJSONObject("sticker");
                    holder.body.setBackgroundResource(R.drawable.transparent);
                    holder.sticker.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    int width = sticker.getInt("width");
                    int height = sticker.getInt("height");
                    int new_height = 120 * height / width;
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            AndroidUtils.dp(120),
                            AndroidUtils.dp(new_height));
                    holder.sticker.setLayoutParams(params);
                    Picasso.with(context).load(sticker.getString("photo_128")).into(holder.sticker);
                    holder.bodyMsg.setBackgroundResource(R.drawable.doc_cub);
                    holder.msgAuthor.setVisibility(View.GONE);
                    toBody = false;
                } else if (sticker.has("gift")) {
                    holder.msgAuthor.setVisibility(View.VISIBLE);
                    holder.msgAuthor.setText(ApplicationName.getStr(R.string.gift));
                    holder.bodyMsg.setBackgroundResource(R.drawable.transparent);
                } else {
                    holder.msgAuthor.setVisibility(View.GONE);
                    holder.bodyMsg.setBackgroundResource(R.drawable.transparent);
                }
            } else {
                holder.msgAuthor.setVisibility(View.GONE);
                holder.bodyMsg.setBackgroundResource(R.drawable.transparent);
            }
            if (toBody) {
                Body.createMsg(msg, new OnCreateView() {
                    @Override
                    public void onView(View v) {
                        settingsMsg.put("hiddenDate", true);
                        holder.attach.addView(v);
                    }

                    @Override
                    public void onView(View v, int index) {
                        // holder.attach.addView(v, index);
                    }
                }, 0);
            }

            if (!body.equals("")) {
                // body = body.replaceAll("<br>", "(\r\n|\n)");
                holder.msgText.setVisibility(View.VISIBLE);
                holder.msgText.setText(body);
                TextViewSpan.parse(holder.msgText);
            } else {
                holder.msgText.setVisibility(View.GONE);
            }


            String time = ApplicationName.simpleDateFormat.format(new Date(msg.getInt("date") * 1000L));
            holder.msgTime.setText(time);
            holder.msgTime.setTextColor(toBody ? Color.parseColor(
                    my ? "#70b15c" : "#a1aab3"
            ) : Color.parseColor("#ffffff"));
            if (my) {
                holder.msgStatus.setVisibility(View.VISIBLE);
                if (msg.has("sending")) {
                    holder.msgStatus.setImageResource(R.mipmap.ic_query_builder);
                } else if (msg.has("error")) {
                    holder.msgStatus.setImageResource(R.mipmap.ic_error);
                } else {
                    holder.msgStatus.setImageResource(read ? R.mipmap.ic_done : R.mipmap.ic_done_one);
                }
            } else {
                holder.msgStatus.setVisibility(View.GONE);
            }
        } catch (JSONException e) {

        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        JSONObject item = items.get(position);
        int _return = 0;
        if (item.has("audio")) {
            _return = 1;
        } else if (item.has("video")) {
            _return = 2;
        } else if (item.has("doc")) {
            _return = 3;
        } else if (item.has("wall")) {
            _return = 4;
        } else if (item.has("map")) {
            _return = 5;
        } else if (item.has("photo")) {
            _return = 6;
        } else if (item.has("event")) {
            _return = 7;
        }
        return _return;
    }

    public String getmId() {
        return mId;
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        Log.e("eventAdapter", "onDetachedFromRecyclerView");
    }

    public void onDestroy() {
        try {
            if (mService != null) {
                mService.mId = null;
                mService.longPollListenerChat = null;
                mService = null;
            }
            context.unbindService(mServiceConnection);
            Collections.reverse(items);
            Method.putMsg(mId, items);
            items.clear();
            notifyDataSetChanged();
            show = false;
            Log.e("ChatAdapter", "onDestroy");
        } catch (NullPointerException e) {

        } catch (IllegalArgumentException e) {

        } catch (Exception e) {

        }
    }

    public void onPause() {
        pause = true;
        try {
            if (mService != null) {
                mService.mId = null;
                mService.longPollListenerChat = null;
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

    public String getMid() {
        return mId;
    }

}
