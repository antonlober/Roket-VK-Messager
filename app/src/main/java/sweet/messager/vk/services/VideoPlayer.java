package sweet.messager.vk.services;

import android.app.Dialog;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Objects;

import sweet.messager.vk.ApplicationName;
import sweet.messager.vk.AttachActivity;
import sweet.messager.vk.Constants;
import sweet.messager.vk.ListActivity;
import sweet.messager.vk.R;
import sweet.messager.vk.api.Async;
import sweet.messager.vk.api.VK;
import sweet.messager.vk.api.VKException;
import sweet.messager.vk.utils.AndroidUtils;
import sweet.messager.vk.utils.CreateMenu;

public class VideoPlayer extends Service {

    private WindowManager windowManager;
    private View videoView = null, menu;
    private WindowManager.LayoutParams params;
    private ImageView posterView;
    private ProgressBar progressBar;
    private WebView webView;
    private boolean isVideo = false, isMove = false;
    private Dialog dialog;
    private TextView moveName;
    private String urlVideo;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("clickVideoMSG", "create");
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        menu = LayoutInflater.from(ApplicationName.baseActivity).inflate(R.layout.menu, null);
        menu.findViewById(R.id.list_view_photos).setVisibility(View.GONE);
        dialog = new Dialog(ApplicationName.baseActivity, R.style.MyMenuTheme);
        dialog.setContentView(menu);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        Window window = dialog.getWindow();
        window.setGravity(Gravity.BOTTOM);
        new CreateMenu(ApplicationName.baseActivity, menu, R.menu.video, new CreateMenu.OnItemClickListener() {
            @Override
            public void onItemClick(int id) {
                switch (id) {
                    case R.id.action_move_video:
                        if (!isMove) {
                            webView.setOnTouchListener(touchListener);
                            moveName.setText(Html.fromHtml("<b>Перемещение включено</b>"));
                            isMove = true;
                        }
                        break;
                    case R.id.action_open_video:
                        dialog.dismiss();
                        try {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlVideo));
                            browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            ApplicationName.getAppContext().startActivity(browserIntent);
                        } catch (Exception e) {

                        }
                        break;
                    case R.id.action_close_video:
                        dialog.dismiss();
                        VideoPlayer.this.stopSelf();
                        break;
                }
            }
        });
        moveName = (TextView) ((LinearLayout) menu.findViewById(R.id.items)).getChildAt(0).findViewById(R.id.name);


        videoView = LayoutInflater.from(this).inflate(R.layout.video_player, null, false);
        posterView = (ImageView) videoView.findViewById(R.id.poster);
        progressBar = (ProgressBar) videoView.findViewById(R.id.progressBar);
        webView = (WebView) videoView.findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBuiltInZoomControls(false);
        webView.setWebViewClient(new VideoWebViewClient());
        params = new WindowManager.LayoutParams(
                AndroidUtils.dp(220),
                AndroidUtils.dp(156),
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = AndroidUtils.dp(18);
        params.y = AndroidUtils.dp(66);
        webView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                dialog.show();
                return true;
            }
        });
    }

    View.OnTouchListener touchListener = new View.OnTouchListener() {
        private int initialX;
        private int initialY;
        private float initialTouchX;
        private float initialTouchY;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    initialX = params.x;
                    initialY = params.y;
                    initialTouchX = event.getRawX();
                    initialTouchY = event.getRawY();
                    return true;
                case MotionEvent.ACTION_UP:
                    webView.setOnTouchListener(null);
                    moveName.setText("Влючить перемещение");
                    isMove = false;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    params.x = initialX + (int) (event.getRawX() - initialTouchX);
                    params.y = initialY + (int) (event.getRawY() - initialTouchY);
                    windowManager.updateViewLayout(videoView, params);
                    return true;
            }
            return false;
        }

    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.hasExtra("poster") && intent.hasExtra("id")) {
            progressBar.setVisibility(View.VISIBLE);
            webView.setVisibility(View.GONE);

            String poster = intent.getStringExtra("poster");
            Picasso.with(this).load(poster).into(posterView);

            setVideoUrl(intent.getStringExtra("id"));
            if (!isVideo) {
                isVideo = true;
                windowManager.addView(videoView, params);
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (videoView != null) {
            if (Build.VERSION.SDK_INT >= 11) {
                webView.onPause();
            } else {
                try {
                    Class.forName("android.webkit.WebView")
                            .getMethod("onPause", (Class[]) null)
                            .invoke(webView, (Object[]) null);

                } catch(ClassNotFoundException cnfe) {
                } catch(NoSuchMethodException nsme) {
                } catch(InvocationTargetException ite) {
                } catch (IllegalAccessException iae) {
                }
            }
            windowManager.removeView(videoView);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    private void setVideoUrl(final String id) {
        new Async(){

            @Override
            protected Object background() throws VKException {
                HashMap<String, Object> post = new HashMap<>();
                post.put("videos", id);
                try {
                    return new VK(ApplicationName.getAppContext()).method("video.get").params(post).getObject().getJSONArray("items").getJSONObject(0);
                } catch (JSONException e) {
                    return null;
                }
            }

            @Override
            protected void error(VKException error) {

            }

            @Override
            protected void finish(Object json) {
                if (json != null) {
                    JSONObject video = (JSONObject) json;
                    if (video.has("player")) {
                        try {
                            urlVideo = video.getString("player");
                            webView.loadUrl(urlVideo);
                        } catch (JSONException e) {

                        }
                    }
                }
            }

            @Override
            protected void start() {

            }
        }.execute();
    }



    class VideoWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progressBar.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
            videoView.requestFocus();
            // webView.setOnTouchListener(VideoPlayer.this);
            //videoView.setOnTouchListener(null);
        }
    }
}
