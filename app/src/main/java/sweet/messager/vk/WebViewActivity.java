package sweet.messager.vk;


import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class WebViewActivity extends ActionBarActivity {

    WebView webview;
    ProgressBar progressBar;
    TextView progressText;
    LinearLayout progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressText = (TextView) findViewById(R.id.progressText);
        progress = (LinearLayout) findViewById(R.id.progress);
        webview = (WebView) findViewById(R.id.web);
        try {
            android.webkit.CookieManager.getInstance().removeAllCookie();
        } catch (IllegalStateException e) {

        }
        webview.getSettings().setJavaScriptEnabled(true);
        webview.setVerticalScrollBarEnabled(false);
        webview.setHorizontalScrollBarEnabled(false);
        webview.clearCache(true);
        webview.getSettings().setSaveFormData(false);
        webview.setWebViewClient(new VkWebViewClient());
        webview.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int progress) {
                progressText.setText(progress + "%");
                super.onProgressChanged(view, progress);
            }
        });
        Animation animation = AnimationUtils.loadAnimation(ApplicationName.getAppContext(), R.anim.rotate);
        progressBar.setAnimation(animation);
        String url = "http://oauth.vk.com/authorize?client_id=4757672&scope=2080255&redirect_uri="+ URLEncoder.encode("https://oauth.vk.com/blank.html") + "&response_type=token";
        if (getIntent().hasExtra("url")) {
            url = getIntent().getStringExtra("url");
        }
        setStatusBar(true);
        webview.loadUrl(url);
    }


    public static String[] parseRedirectUrl(String url) throws Exception {
        String access_token = extractPattern(url, "access_token=(.*?)&");
        String user_id = extractPattern(url, "user_id=(\\d*)");
        if( user_id == null || user_id.length() == 0 || access_token == null || access_token.length() == 0 ) {
            throw new Exception("Failed to parse redirect url " + url);
        }
        return new String[]{access_token, user_id};
    }


    class VkWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            parseUrl(url);

        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            try {
                String path = new URL(url).getPath();
                Log.e("loadURLWebView", path);
                if (path.equals("/authorize") || path.equals("/oauth/authorize") || path.equals("/login")) {
                    progress.setVisibility(View.GONE);
                    webview.setVisibility(View.VISIBLE);
                    setStatusBar(false);
                } else {
                    progress.setVisibility(View.VISIBLE);
                    webview.setVisibility(View.GONE);
                    setStatusBar(true);
                }
            } catch (MalformedURLException e) {
                if (url.startsWith("http://oauth.vk.com/authorize") ||
                url.startsWith("http://oauth.vk.com/oauth/authorize") ||
                url.startsWith("https://oauth.vk.com/authorize") ||
                url.startsWith("https://oauth.vk.com/oauth/authorize") ||
                url.startsWith("https://m.vk.com/login") ||
                url.startsWith("http://m.vk.com/login")) {
                    progressText.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                    webview.setVisibility(View.VISIBLE);
                    setStatusBar(false);
                } else {
                    progressText.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.VISIBLE);
                    webview.setVisibility(View.GONE);
                    setStatusBar(true);
                }
            }

        }
    }

    private void parseUrl(String url) {
        try {
            Log.e("urlRedirect", url);
            if( url == null ) {
                return;
            }
            if( url.startsWith("https://oauth.vk.com/blank.html") || url.startsWith("http://oauth.vk.com/blank.html")) {
                if( !url.contains("error") ) {
                    String[] auth = parseRedirectUrl(url);
                    Intent intent = new Intent();
                    intent.putExtra("token", auth[0]);
                    intent.putExtra("uid", Integer.valueOf(auth[1]));
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    setResult(RESULT_CANCELED);
                    finish();
                }
            } else if( url.contains("error?err") ) {
                setResult(RESULT_CANCELED);
                finish();
            }
        } catch( Exception e ) {
            e.printStackTrace();
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    public static String extractPattern(String string, String pattern){
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(string);
        if (!m.find())
            return null;
        return m.toMatchResult().group(1);
    }

    public void setStatusBar(boolean black) {
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.setStatusBarColor(getResources().getColor(black ? android.R.color.black : R.color.mainColor));
        }
    }
}
