package sweet.messager.vk.model;



import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import sweet.messager.vk.ApplicationName;
import sweet.messager.vk.R;
import sweet.messager.vk.utils.AndroidUtils;

public class ChatModel {
    public boolean read, out, isBody = true, typing = false, chat = false, no_photo = false;
    public int unread = 0, id, msg_id, user_id;
    public int[] typings = new int[]{};
    public String body, photo, photo_big, photo_main, date, chat_title, attach, prevName = "VK", text = "Messenger";


    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("{"); // Start

        /* db */
        stringBuilder.append("\"db\":1,\"id\":");

        /* id */
        stringBuilder.append(id);
        stringBuilder.append(",");

        /* unread */
        stringBuilder.append("\"unread\":");
        stringBuilder.append(unread);
        stringBuilder.append(",");

        /* msg_id */
        stringBuilder.append("\"msg_id\":");
        stringBuilder.append(msg_id);
        stringBuilder.append(",");

        /* user_id */
        stringBuilder.append("\"user_id\":");
        stringBuilder.append(user_id);
        stringBuilder.append(",");

        /* out */
        stringBuilder.append("\"out\":");
        stringBuilder.append(out ? 1 : 0);
        stringBuilder.append(",");

        /* read */
        stringBuilder.append("\"read\":");
        stringBuilder.append(read ? 1 : 0);
        stringBuilder.append(",");

        /* isBody */
        stringBuilder.append("\"isBody\":");
        stringBuilder.append(isBody ? 1 : 0);
        stringBuilder.append(",");

        /* no_photo */
        stringBuilder.append("\"no_photo\":");
        stringBuilder.append(no_photo ? 1 : 0);
        stringBuilder.append(",");

        if (!isBody) {
            /* attach */
            stringBuilder.append("\"attach\":\"");
            stringBuilder.append(attach);
            stringBuilder.append("\",");
        } else {
            /* body */
            stringBuilder.append("\"body\":\"");
            stringBuilder.append(body);
            stringBuilder.append("\",");
        }

        if (chat) {
            /* chat */
            stringBuilder.append("\"chat\":");
            stringBuilder.append(chat ? 1 : 0);
            stringBuilder.append(",");

            /* chat_title */
            stringBuilder.append("\"chat_title\":\"");
            stringBuilder.append(chat_title);
            stringBuilder.append("\",");

            /* text */
            stringBuilder.append("\"text\":\"");
            stringBuilder.append(text);
            stringBuilder.append("\",");

            if (!no_photo) {
                /* photo */
                stringBuilder.append("\"photo\":\"");
                stringBuilder.append(photo);
                stringBuilder.append("\",");

                /* photo_big */
                stringBuilder.append("\"photo_big\":\"");
                stringBuilder.append(photo_big);
                stringBuilder.append("\",");

                 /* photo_main */
                stringBuilder.append("\"photo_main\":\"");
                stringBuilder.append(photo_main);
                stringBuilder.append("\",");
            }
        }

        /* date */
        stringBuilder.append("\"prevName\":\"");
        stringBuilder.append(prevName);
        stringBuilder.append("\",");

        /* date */
        stringBuilder.append("\"date\":\"");
        stringBuilder.append(date);
        stringBuilder.append("\"");



        stringBuilder.append("}"); // End
        return stringBuilder.toString();
    }

    public static ChatModel parse(JSONObject mChat) {
        return ChatModel.parse(mChat.toString());
    }

    public static ChatModel parse(String info) {
        try {
            JSONObject msg = new JSONObject(info);
            ChatModel chatModel = new ChatModel();
            chatModel.unread = msg.has("unread") ? msg.getInt("unread") : 0;
            if (msg.has("db")) {
                chatModel.id = msg.getInt("id");
                chatModel.msg_id = msg.getInt("msg_id");
                chatModel.read = msg.getInt("read") == 1;
                chatModel.out = msg.getInt("out") == 1;
                chatModel.user_id = msg.getInt("user_id");
                chatModel.date = msg.getString("date");
                chatModel.no_photo = msg.getInt("no_photo") == 1;
                chatModel.isBody = msg.getInt("isBody") == 1;
                chatModel.prevName = msg.getString("prevName");
                if (chatModel.isBody) {
                    chatModel.body = AndroidUtils.trim(msg.getString("body"), 26);
                } else if (msg.has("attach")) {
                    chatModel.attach = msg.getString("attach");
                } else {
                    chatModel.attach = ApplicationName.getStr(R.string.other);
                }
                if (msg.has("chat")) {
                    chatModel.text = msg.has("text") ? msg.getString("text") : ApplicationName.getStr(R.string.loading);
                    chatModel.chat = true;
                    chatModel.chat_title = (msg.has("chat_title") && !msg.getString("chat_title").equals("")) ? msg.getString("chat_title") : "Чат";
                    if (!chatModel.no_photo) {
                        chatModel.photo = msg.getString("photo");
                        chatModel.photo_big = msg.getString("photo_big");
                        chatModel.photo_main = msg.getString(msg.has("photo_main") ? "photo_main" : "photo_big");
                    }
                }
                /*
                String[] split = photo.split("\\/");
        String[] path = split[Math.abs(split.length - 1)].split("\\.");
        String format = path[Math.abs(path.length - 1)];
                 */
            } else {
                msg = msg.getJSONObject("message");
                chatModel.msg_id = msg.getInt("id");
                chatModel.read = msg.getInt("read_state") == 1;
                chatModel.out = msg.getInt("out") == 1;
                chatModel.user_id = msg.getInt("user_id");
                chatModel.id = msg.getInt("user_id");
                chatModel.date = ApplicationName.simpleDateFormat
                        .format(
                                new Date(
                                        msg.getInt("date") * 1000L
                                )
                        );
                if (msg.has("body") && !msg.getString("body").equals("")) {
                    chatModel.body = AndroidUtils.trim(msg.getString("body"), 26);
                    chatModel.isBody = true;
                } else {
                    chatModel.isBody = false;
                    String attach = null;
                    int mid = msg.has("action_mid") ? Integer.parseInt(
                            msg.getString("action_mid")
                    ) : 0;
                    User mUser = mid == 0 ? null : ApplicationName.getUsers(mid);
                    User aUser = ApplicationName.getUsers(chatModel.user_id);
                    if (msg.has("action") && msg.has("users_count")) {
                        String action = msg.getString("action");
                        switch (action) {
                            case "chat_title_update":
                                attach = AndroidUtils.chatEvent(chatModel.out, aUser.sex, "изменил") + " название чата";
                                break;
                            case "chat_photo_update":
                                attach = AndroidUtils.chatEvent(chatModel.out, aUser.sex, "обновил") + " фото чата";
                                break;
                            case "chat_photo_remove":
                                attach = AndroidUtils.chatEvent(chatModel.out, aUser.sex, "удалил") + " фото чата";
                                break;
                            case "chat_create":
                                attach = AndroidUtils.chatEvent(chatModel.out, aUser.sex, "создал") + " чат";
                                break;
                            case "chat_invite_user":
                                assert mUser != null;
                                if (mid == ApplicationName.getUserId()) {
                                    attach = AndroidUtils.chatEvent(chatModel.out, aUser.sex, "пригласил") + " Вас в чат";
                                } else if (chatModel.user_id == mid) {
                                    attach = AndroidUtils.chatEvent(chatModel.out, aUser.sex, new String[] {
                                            "вернулись",
                                            "вернулся",
                                            "вернулась"
                                    }) + " в чат";
                                } else {
                                    attach = AndroidUtils.chatEvent(chatModel.out, aUser.sex, "пригласил") + " " + mUser.name + " в чат";
                                }
                                break;
                            case "chat_kick_user":
                                assert mUser != null;
                                if (chatModel.user_id == mid) {
                                    attach = AndroidUtils.chatEvent(chatModel.out, aUser.sex, new String[] {
                                            "вышли",
                                            "вышел",
                                            "вышла"
                                    }) + " из чата";
                                } else {
                                    attach = AndroidUtils.chatEvent(chatModel.out, aUser.sex, "удалил") + " " + mUser.name + " из чата";
                                }
                                break;
                            default:
                                attach = action;
                                break;
                        }
                    } else if (msg.has("fwd_messages")) {
                        attach = ApplicationName.getStr(R.string.fwd_messages);
                        msg.remove("fwd_messages");
                    } else if (msg.has("geo")) {
                        attach = ApplicationName.getStr(R.string.geo);
                        msg.remove("geo");
                    } else if (msg.has("attachments")) {
                        JSONObject attachJSON = msg.getJSONArray("attachments").getJSONObject(0);
                        String type = attachJSON.getString("type");
                        msg.remove("attachments");
                        switch (type) {
                            case "link":
                                attach = ApplicationName.getStr(R.string.link);
                                break;
                            case "wall":
                                attach = ApplicationName.getStr(R.string.wall);
                                break;
                            case "wall_reply":
                                attach = ApplicationName.getStr(R.string.wall_reply);
                                break;
                            case "sticker":
                                attach = ApplicationName.getStr(R.string.sticker);
                                break;
                            case "gift":
                                attach = ApplicationName.getStr(R.string.gift);
                                break;
                            case "photo":
                                attach = ApplicationName.getStr(R.string.photo);
                                break;
                            case "video":
                                attach = ApplicationName.getStr(R.string.video);
                                break;
                            case "audio":
                                attach = ApplicationName.getStr(R.string.audio);
                                break;
                            case "doc":
                                try {
                                    String ext = attachJSON.getJSONObject("doc").getString("ext");
                                    if (ext.equals("gif")) {
                                        attach = ApplicationName.getStr(R.string.gif);
                                    } else {
                                        attach = ApplicationName.getStr(R.string.doc);
                                    }
                                } catch (JSONException e) {
                                    attach = ApplicationName.getStr(R.string.doc);
                                }
                                break;
                            default:
                                attach = type;
                                break;
                        }
                    }
                    if (attach == null) {
                        attach = ApplicationName.getStr(R.string.other);
                    }
                    chatModel.attach = attach;
                }
                if (msg.has("chat_id")) {
                    chatModel.chat = true;
                    chatModel.chat_title = msg.has("title") ? msg.getString("title") : "Чат";
                    chatModel.prevName = AndroidUtils.trim(chatModel.chat_title, 2);
                    chatModel.id = msg.getInt("chat_id") + 2000000000;
                    if (msg.has("photo_50") && msg.has("photo_100")) {
                        chatModel.photo = msg.getString("photo_50");
                        chatModel.photo_big = msg.getString("photo_100");
                        chatModel.photo_main = msg.getString("photo_200");
                        chatModel.no_photo = false;
                    } else {
                        chatModel.photo = "http://vk.com/images/camera_c.gif";
                        chatModel.photo_big = "http://vk.com/images/camera_b.gif";
                        chatModel.photo_main = "http://vk.com/images/camera_a.gif";
                        chatModel.no_photo = true;
                    }
                    if (msg.has("chat_active") && msg.has("users_count")) {
                        chatModel.text = AndroidUtils.declOfNum(msg.getInt("users_count"), new String[]{
                                "учасник",
                                "учасника",
                                "учасников"
                        }) + ", " + msg.getJSONArray("chat_active").length() + " online";
                    } else {
                        chatModel.text = "Вас удалили из чата";
                        chatModel.attach = chatModel.text;
                        chatModel.isBody = false;
                        chatModel.out = false;
                        chatModel.user_id = 0;
                    }
                }
            }
            return chatModel;
        } catch (JSONException e) {
            Log.e("ChatModel", String.valueOf(e) + ": " + info);
            return AndroidUtils.defaultChat();
        }
    }
}
