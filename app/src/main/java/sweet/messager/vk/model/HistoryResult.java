package sweet.messager.vk.model;

import org.json.JSONObject;

import java.util.List;

public class HistoryResult {
    public boolean isChat, isPhoto;
    public String photo = "", chat_title, text;
    public List<JSONObject> history;
    public User user;
}
