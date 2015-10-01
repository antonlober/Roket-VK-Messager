package sweet.messager.vk.vk;

import android.content.Context;
import android.content.Intent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import sweet.messager.vk.ApplicationName;
import sweet.messager.vk.Constants;
import sweet.messager.vk.api.Async;
import sweet.messager.vk.api.VK;
import sweet.messager.vk.api.VKException;
import sweet.messager.vk.utils.AndroidUtils;

public class Send {

    public static JSONObject createMsg(String text, int msg_id, JSONArray attachments) {
        try {
            int owner = ApplicationName.getUserId();
            JSONObject msg = new JSONObject();
            msg.put("id", msg_id);
            msg.put("body", text);
            msg.put("user_id", owner);
            msg.put("from_id", owner);
            msg.put("out", 1);
            msg.put("sending", 1);
            msg.put("read_state", 0);
            msg.put("date", ((int) (System.currentTimeMillis() / 1000L)));
            if (attachments.length() != 0) {
                msg.put("attachments", attachments);
            }
            return msg;
        } catch (JSONException e) {
            return null;
        }
    }
}
