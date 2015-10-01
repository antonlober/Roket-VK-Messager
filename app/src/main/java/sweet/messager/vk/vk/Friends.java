package sweet.messager.vk.vk;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import sweet.messager.vk.ApplicationName;
import sweet.messager.vk.api.Async;
import sweet.messager.vk.api.VK;
import sweet.messager.vk.api.VKException;
import sweet.messager.vk.db.Method;
import sweet.messager.vk.interfaces.VKListener;

public class Friends extends Async {

    VKListener vkListener;
    boolean online;

    public Friends(VKListener _vkListener, boolean _online) {
        vkListener = _vkListener;
        online = _online;
    }

    @Override
    protected Object background() throws VKException {
        if (online) {
            HashMap<String, Object> post = new HashMap<>();
            post.put("order", "hints");
            return new VK(ApplicationName.getAppContext()).method("friends.getOnline").params(post).getArray();
        } else {
            JSONObject friends = new VK(ApplicationName.getAppContext()).method("execute.contacts").params(new HashMap<String, Object>()).getObject();
            try {
                ApplicationName.friendsCount = friends.getJSONObject("all").getInt("count");
            } catch (JSONException e) { }
            try {
                Method.addFriends(friends.getJSONObject("all").getJSONArray("items"));
                return friends.getJSONArray("online");
            } catch (JSONException e) {
                Log.e("ERROR_JSON", String.valueOf(e));
                return null;
            }
        }
    }

    @Override
    protected void error(VKException error) {

    }

    @Override
    protected void finish(Object json) {
        if (json != null) {
            JSONArray online = (JSONArray) json;
            try {
                ApplicationName.onlineUsers = new ArrayList<>();
                for (int i = 0; i < online.length(); i++) {
                    ApplicationName.onlineUsers.add(online.getInt(i));
                }
                // Collections.reverse(ApplicationName.onlineUsers);
            } catch (JSONException e) { }
        }
        if (vkListener != null) {
            vkListener.result(null);
        }
    }

    @Override
    protected void start() {

    }
}
