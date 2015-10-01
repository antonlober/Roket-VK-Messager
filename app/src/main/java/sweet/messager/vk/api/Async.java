package sweet.messager.vk.api;

import android.os.AsyncTask;

import org.json.JSONObject;

/**
 * Created by antonpolstyanka on 05.04.15.
 */
public abstract class Async extends AsyncTask<Void, VKException, Object> {

    protected abstract Object background() throws VKException;
    protected abstract void error(VKException error);
    protected abstract void finish(Object json);
    protected abstract void start();

    @Override
    protected void onPreExecute() {
        start();
    }

    @Override
    protected void onProgressUpdate(VKException... vkExceptions) {
        error(vkExceptions[0]);
    }

    @Override
    protected Object doInBackground(Void... params) {
        try {
            return background();
        } catch (VKException e) {
            publishProgress(e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object json) {
        if (json != null) {
            finish(json);
        }
    }

}
