package sweet.messager.vk.vk;


import android.os.AsyncTask;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import sweet.messager.vk.model.HistoryResult;

public class History extends AsyncTask<Integer, Void, HistoryResult> {

    public interface OnHistoryListener {
        void onHistory(HistoryResult messages);
    }

    OnHistoryListener onHistoryListener;

    public History(OnHistoryListener _onHistoryListener) {
        onHistoryListener = _onHistoryListener;
    }


    @Override
    protected HistoryResult doInBackground(Integer... params) {
        return VKApi.getHistory(params[0], params[1], params[2]);
    }

    @Override
    protected void onPostExecute(HistoryResult messages) {
        super.onPostExecute(messages);
        onHistoryListener.onHistory(messages);
    }
}
