package sweet.messager.vk.model;




import org.json.JSONException;
import org.json.JSONObject;

import sweet.messager.vk.utils.AndroidUtils;

public class User {
    public String name, photo, photo_big, photo_main, text = "vk", status, domain, bdate, prevName = "VK";
    public int id, last_seen, sex, platform = 0;
    public boolean online = false, blacklisted = false, no_photo = false;


    @Override
    public String toString() {
        /*
        StringBuilder stringBuilder = new StringBuilder("{"); // Start

        stringBuilder.append("\"db\":1,\"id\":");

        stringBuilder.append(id);
        stringBuilder.append(",");

        stringBuilder.append("\"name\":\"");
        stringBuilder.append(name);
        stringBuilder.append("\",");

        stringBuilder.append("\"text\":\"");
        stringBuilder.append(text);
        stringBuilder.append("\",");

        stringBuilder.append("\"photo\":\"");
        stringBuilder.append(photo);
        stringBuilder.append("\",");

        stringBuilder.append("\"photo_big\":\"");
        stringBuilder.append(photo_big);
        stringBuilder.append("\",");

        stringBuilder.append("\"status\":\"");
        stringBuilder.append(status);
        stringBuilder.append("\",");

        stringBuilder.append("\"domain\":\"");
        stringBuilder.append(domain);
        stringBuilder.append("\",");

        stringBuilder.append("\"bdate\":\"");
        stringBuilder.append(bdate);
        stringBuilder.append("\",");

        stringBuilder.append("\"sex\":");
        stringBuilder.append(sex);
        stringBuilder.append(",");

        stringBuilder.append("\"platform\":");
        stringBuilder.append(platform);
        stringBuilder.append(",");

        stringBuilder.append("\"online\":");
        stringBuilder.append(online ? 1 : 0);
        stringBuilder.append(",");

        stringBuilder.append("\"last_seen\":");
        stringBuilder.append(last_seen);
        stringBuilder.append(",");

        stringBuilder.append("\"blacklisted\":");
        stringBuilder.append(blacklisted ? 1 : 0);

        stringBuilder.append("}"); // End
        return stringBuilder.toString();
        */
        return "{\"db\":1,\"id\":" + id + ",\"photo_main\":\"" + photo_main + "\",\"prevName\":\"" + prevName + "\",\"name\":\"" + name + "\",\"text\":\"" + text + "\"," + "\"photo\":\"" + photo + "\"," + "\"photo_big\":\"" + photo_big + "\"," + "\"status\":\"" + status + "\"," + "\"domain\":\"" + domain + "\"," + "\"bdate\":\"" + bdate + "\"," + "\"sex\":" + sex + "," + "\"platform\":" + platform + ",\"no_photo\":" + (no_photo ? 1 : 0) + ",\"online\":" + (online ? 1 : 0) + "," + "\"last_seen\":" + last_seen + "," + "\"blacklisted\":" + (blacklisted ? 1 : 0) + "}";
    }


    public static User parse(JSONObject user) {
        return User.parse(user.toString());
    }

    public static User parse(String info) {
        User _return = new User();
        try {
            JSONObject user = new JSONObject(info);
            int id = user.getInt("id");
            _return.id = id;
            StringBuilder stringBuilder = null;
            if (user.has("db")) {
                try {
                    _return.name = user.getString("name");
                    _return.photo = user.getString("photo");
                    _return.photo_big = user.getString("photo_big");
                    _return.photo_main = user.getString(user.has("photo_main") ? "photo_main" : "photo_big");
                    _return.domain = user.getString("domain");
                    _return.status = user.getString("status");
                    _return.last_seen = user.getInt("last_seen");
                    _return.platform = user.getInt("platform");
                    _return.bdate = user.getString("bdate");
                    _return.no_photo = user.has("no_photo") ? (user.getInt("no_photo") == 1) : false;
                    _return.prevName = user.has("prevName") ? user.getString("prevName") : "VK";
                    _return.online = user.getInt("online") == 1;
                    _return.blacklisted = user.getInt("blacklisted") == 1;
                    _return.text = user.getString("text");
                    _return.sex = user.has("sex") ? user.getInt("sex") : 0;
                } catch (JSONException e) {
                    _return = AndroidUtils.defaultUser(id);
                }
            } else {
                if (user.has("first_name") && user.has("last_name")) {
                    stringBuilder = new StringBuilder(user.getString("first_name"));
                    stringBuilder.append(" ");
                    stringBuilder.append(user.getString("last_name"));
                    _return.name = stringBuilder.toString();
                } else if (user.has("name")) {
                    _return.name = user.getString("name");
                }
                _return.prevName = AndroidUtils.trim(_return.name, 2);
                if (user.has("photo_50") && user.has("photo_100") && user.has("photo_200")) {
                    _return.photo = user.getString("photo_50");
                    _return.photo_big = user.getString("photo_100");
                    _return.photo_main = user.getString("photo_200");
                    _return.no_photo = false;
                } else {
                    _return.photo = "http://vk.com/images/camera_c.gif";
                    _return.photo_big = "http://vk.com/images/camera_b.gif";
                    _return.photo_main = "http://vk.com/images/camera_a.gif";
                    _return.no_photo = true;
                }
                if (user.has("domain")) {
                    _return.domain = user.getString("domain");
                } else {
                    stringBuilder = new StringBuilder("id");
                    stringBuilder.append(id);
                    _return.domain = stringBuilder.toString();
                }
                if (user.has("status") && !user.getString("status").equals("")) {
                    _return.status = user.getString("status");
                } else {
                    if (stringBuilder == null) {
                        stringBuilder = new StringBuilder("id");
                        stringBuilder.append(id);
                    }
                    _return.status = stringBuilder.toString();
                }
                if (user.has("last_seen")) {
                    JSONObject last_seen = user.getJSONObject("last_seen");
                    _return.last_seen = last_seen.getInt("time");
                    _return.platform = last_seen.getInt("platform");
                } else {
                    _return.last_seen = 0;
                    _return.platform = 1;
                }
                _return.bdate = user.has("bdate") ? user.getString("bdate") : "";
                _return.online = user.has("online") && (user.getInt("online") == 1);
                _return.blacklisted = user.has("blacklisted") && user.getInt("blacklisted") == 1;
                _return.sex = user.has("sex") ? user.getInt("sex") : 0;
                if (_return.online) {
                    _return.text = "Online";
                } else if (_return.last_seen != 0) {
                    _return.text = AndroidUtils.last_date(_return.sex, _return.last_seen);
                } else {
                    if (stringBuilder == null) {
                        stringBuilder = new StringBuilder("id");
                        stringBuilder.append(id);
                    }
                    _return.text = stringBuilder.toString();
                }
            }
        } catch (JSONException e) {
            _return = AndroidUtils.defaultUser(0);
        }
        return _return;
    }
}
