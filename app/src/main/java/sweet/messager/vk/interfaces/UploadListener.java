package sweet.messager.vk.interfaces;

import org.json.JSONObject;


public interface UploadListener {
    void onStart();
    void onProgress(int progress);
    void onFinish(JSONObject id);
}
