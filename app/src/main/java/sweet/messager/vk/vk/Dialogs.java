package sweet.messager.vk.vk;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import sweet.messager.vk.ApplicationName;
import sweet.messager.vk.R;
import sweet.messager.vk.api.Async;
import sweet.messager.vk.api.VK;
import sweet.messager.vk.api.VKException;
import sweet.messager.vk.db.Method;
import sweet.messager.vk.interfaces.VKListener;
import sweet.messager.vk.model.ChatModel;
import sweet.messager.vk.model.User;
import sweet.messager.vk.utils.Body;


public class Dialogs extends AsyncTask<Integer, Void, List<ChatModel>> {

    public interface OnChatsListener {
        void onChats(List<ChatModel> chats);
    }

    Context context;
    OnChatsListener onChatsListener;

    public Dialogs(OnChatsListener _onChatsListener) {
        context = ApplicationName.getAppContext();
        onChatsListener = _onChatsListener;
    }

    private String readTxt(int id)  {
        InputStream raw = context.getResources().openRawResource(id);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int i;
        try {
            i = raw.read();
            while (i != -1) {
                byteArrayOutputStream.write(i);
                i = raw.read();
            }
            raw.close();
        } catch (IOException e) {
        }
        return byteArrayOutputStream.toString();
    }

    @Override
    protected List<ChatModel> doInBackground(Integer... params) {
        return VKApi.getDialogs(params[0], params[1]);
    }

    @Override
    protected void onPostExecute(List<ChatModel> chats) {
        onChatsListener.onChats(chats);
    }
}
