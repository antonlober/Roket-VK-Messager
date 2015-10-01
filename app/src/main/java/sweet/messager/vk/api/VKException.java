package sweet.messager.vk.api;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

/**
 * Created by antonpolstyanka on 05.04.15.
 */
public class VKException extends Exception {

    int errorCode = 0;
    JSONObject error;
    Context context;

    public VKException(Context _context) {
        context = _context;
        Log.e("startVK", "error!!!!!");
    }

    public VKException setCode(int code) {
        errorCode = code;
        Log.e("VKError", "code: " + errorCode);
        return this;
    }

    public VKException setJSON(JSONObject _error) {
        error = _error;
        Log.e("VKError", "code: " + error);
        return this;
    }

    public int getErrorCode() {
        return errorCode;
    }
    public JSONObject getError() { return error; }
}
