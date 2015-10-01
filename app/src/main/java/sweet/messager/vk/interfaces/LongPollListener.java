package sweet.messager.vk.interfaces;

import org.json.JSONObject;

/**
 * Created by antonpolstyanka on 30.04.15.
 */
public interface LongPollListener {
    void onNewMsg(JSONObject msg);
    void onReadMsg(String id, int msg_id);
    void onTypingMsg(String id);
}
