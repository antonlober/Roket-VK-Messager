package sweet.messager.vk.api;

import android.text.TextUtils;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import sweet.messager.vk.utils.AndroidUtils;

public class Http {

    String url;

    public Http(String _url) {
        url = _url;
    }

    public String go(HashMap<String, Object> post) {
        if (post == null) post = new HashMap<>();
        ArrayList<String> entity = new ArrayList<>();
        for (Map.Entry<String, Object> entry: post.entrySet()) {
            entity.add(entry.getKey() + "=" + entry.getValue());
        }
        String body = TextUtils.join("&", entity);
        String result = connect(url, body);
        if (result != null) return result;
        for (int i = 0; i < 5; i++) {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            result = connect(url, body);
            if (result != null) return result;
        }
        return result;
    }

    private String connect(String _url, String body) {
        HttpURLConnection connection = null;
        try{
            connection = (HttpURLConnection) new URL(_url).openConnection();
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.getOutputStream().write(body.getBytes("UTF-8"));
            /*
            if(enable_compression){
                connection.setRequestProperty("Accept-Encoding", "gzip");
            }
             */

            int code = connection.getResponseCode();
            if (code == -1) {
                //
            }
            InputStream is = new BufferedInputStream(connection.getInputStream(), 8192);
            String enc=connection.getHeaderField("Content-Encoding");
            if (enc != null && enc.equalsIgnoreCase("gzip")) {
                is = new GZIPInputStream(is);
            }
            String response = AndroidUtils.convertStreamToString(is);
            Log.e("responseHttp", response);
            return response;
        } catch (MalformedURLException e) {
            Log.e("responseHttp", String.valueOf(e));
        } catch (ProtocolException e) {
            Log.e("responseHttp", String.valueOf(e));
        } catch (IOException e) {
            Log.e("responseHttp", String.valueOf(e));
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }
}
