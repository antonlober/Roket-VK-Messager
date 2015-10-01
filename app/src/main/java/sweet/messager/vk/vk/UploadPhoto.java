package sweet.messager.vk.vk;


import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import sweet.messager.vk.ApplicationName;
import sweet.messager.vk.api.VK;
import sweet.messager.vk.api.VKException;
import sweet.messager.vk.interfaces.UploadListener;
import sweet.messager.vk.interfaces.UploadProgressListener;
import sweet.messager.vk.utils.CustomMultiPartEntity;

public class UploadPhoto extends AsyncTask<String, Integer, JSONObject> {

    UploadListener uploadListener;
    long totalSize = 0;

    public UploadPhoto(UploadListener _uploadListener) {
        uploadListener = _uploadListener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        uploadListener.onStart();
    }

    @Override
    public void onProgressUpdate(Integer... progress) {
        uploadListener.onProgress(progress[0]);
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        try {
            String result;
            String image;
            try {
                image = params[1] != null ? params[1] : uriToFullImage(params[0]);
            } catch (NullPointerException e) {
                Log.e("UploadPhotoClass", String.valueOf(e));
                image = uriToFullImage(params[0]);
            }
            if (image == null) {
                return null;
            }
            publishProgress(5);

            String server = new VK(ApplicationName.getAppContext()).method("photos.getMessagesUploadServer").params(new HashMap<String, Object>()).getObject().getString("upload_url");
            Log.e("UploadPhotoClass", "server: " + server);
            publishProgress(15);


            /* New Code Upload */

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(server);
            try {
                CustomMultiPartEntity entity = new CustomMultiPartEntity(new UploadProgressListener() {
                            @Override
                            public void transferred(long num) {
                                publishProgress(
                                        ((int) ((num / (float) totalSize) * 100)) + 15
                                );
                            }
                });
                File sourceFile = new File(image);
                entity.addPart("photo", new FileBody(sourceFile));
                totalSize = entity.getContentLength();
                httppost.setEntity(entity);
                // Making server call
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity r_entity = response.getEntity();

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    result = EntityUtils.toString(r_entity);
                } else {
                    result = "Error occurred! Http Status Code: " + statusCode;
                }
            } catch (ClientProtocolException e) {
                result = e.toString();
            } catch (IOException e) {
                result = e.toString();
            }


            /* End Code Upload */

            if (result == null) {
                return null;
            } else {
                try {
                    JSONObject resultVK = new JSONObject(result);
                    HashMap<String, Object> postVK = new HashMap<>();
                    postVK.put("server", resultVK.getString("server"));
                    postVK.put("photo", String.valueOf(resultVK.getString("photo")));
                    postVK.put("hash", resultVK.getString("hash"));
                    JSONObject savePhoto = new VK(ApplicationName.getAppContext()).method("photos.saveMessagesPhoto").params(postVK).getArray().getJSONObject(0);
                    publishProgress(125);
                    savePhoto.put("phone_id", params[0]);
                    return savePhoto;
                } catch (JSONException e) {
                    Log.e("UploadPhotoClass", "JSONException2: " + e + ", result: " + result);
                }
            }
        } catch (VKException e) {
            Log.e("UploadPhotoClass", "VKException: " + e.getError());
        } catch (JSONException e) {
            Log.e("UploadPhotoClass", "JSONException: " + e);
        } catch (Exception e) {
            Log.e("UploadPhotoClass", "Exception: " + e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONObject photo) {
        super.onPostExecute(photo);
        uploadListener.onFinish(photo);
    }

    private String uriToFullImage(String imageId){
        String[] filePathColumn = { MediaStore.Images.Media.DATA };
        Cursor imagesCursor = ApplicationName.getAppContext().getContentResolver()
                .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        filePathColumn,
                        MediaStore.Images.Media._ID + " = ?",
                        new String[]{
                                imageId
                        },
                        null);
        if (imagesCursor != null && imagesCursor.moveToFirst()) {
            int columnIndex = imagesCursor.getColumnIndex(filePathColumn[0]);
            String filePath = imagesCursor.getString(columnIndex);
            imagesCursor.close();
            return filePath;
        } else {
            return uriToFullImagePhone(imageId);
        }
    }

    private String uriToFullImagePhone(String imageId){
        String[] filePathColumn = { MediaStore.Images.Media.DATA };
        Cursor imagesCursor = ApplicationName.getAppContext().getContentResolver()
                .query(MediaStore.Images.Media.INTERNAL_CONTENT_URI,
                        filePathColumn,
                        MediaStore.Images.Media._ID + " = ?",
                        new String[]{
                                imageId
                        },
                        null);
        if (imagesCursor != null && imagesCursor.moveToFirst()) {
            int columnIndex = imagesCursor.getColumnIndex(filePathColumn[0]);
            String filePath = imagesCursor.getString(columnIndex);
            imagesCursor.close();
            return filePath;
        } else {
            return null;
        }
    }
}
