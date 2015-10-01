package sweet.messager.vk.vk;


import android.os.AsyncTask;

import org.json.JSONObject;

public class GetMsg extends AsyncTask<Integer, Void, JSONObject> {

    public interface OnLoadMsg {
        void onMsg(JSONObject msg);
    }

    OnLoadMsg onLoadMsg;

    public GetMsg(OnLoadMsg _onLoadMsg) {
        onLoadMsg = _onLoadMsg;
    }

    @Override
    protected JSONObject doInBackground(Integer... params) {
        return VKApi.getMsgById(params[0]);
    }

    @Override
    protected void onPostExecute(JSONObject msg) {
        if (msg != null) {
            onLoadMsg.onMsg(msg);
        }
    }
}
