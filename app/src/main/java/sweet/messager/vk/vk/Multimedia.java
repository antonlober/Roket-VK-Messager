package sweet.messager.vk.vk;


import android.os.AsyncTask;

import org.json.JSONArray;

public class Multimedia extends AsyncTask<Integer, Void, JSONArray> {

    public interface OnUpdateListener {
        void onUpdate();
    }
    OnUpdateListener onUpdateListener;

    public Multimedia(OnUpdateListener _onUpdateListener) {
        onUpdateListener = _onUpdateListener;
    }

    @Override
    protected JSONArray doInBackground(Integer... params) {
        return VKApi.getMultimedia(params[0], params[1]);
    }

    @Override
    protected void onPostExecute(JSONArray jsonArray) {
        onUpdateListener.onUpdate();
    }
}
