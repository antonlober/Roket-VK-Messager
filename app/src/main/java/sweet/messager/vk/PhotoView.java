package sweet.messager.vk;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import sweet.messager.vk.utils.AndroidUtils;
import sweet.messager.vk.view.TouchImageView;


public class PhotoView extends ActionBarActivity {

    ProgressBar progressBar;
    TouchImageView img;
    int countFalse = 0;
    String photo;
    boolean isShow = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_view);
        photo = getIntent().getStringExtra("photo");
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        img = (TouchImageView) findViewById(R.id.img);
        img.setMaxZoom(5f);
        img.setBackgroundColor(Color.parseColor("#000000"));
        img.setVisibility(View.GONE);
        /*
        RelativeLayout.LayoutParams paramsImg = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        paramsImg.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

        RelativeLayout mainV = (RelativeLayout) findViewById(R.id.mainV);
        mainV.addView(img, paramsImg);
         */
        final LinearLayout topBar = (LinearLayout) findViewById(R.id.topBar);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, getStatusBarHeight(), 0, 0);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        topBar.setLayoutParams(params);
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isShow) {
                    topBar.setVisibility(View.GONE);
                    if (Build.VERSION.SDK_INT >= 19) {
                        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                        // getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                    }
                } else {
                    topBar.setVisibility(View.VISIBLE);
                    if (Build.VERSION.SDK_INT >= 19) {
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                        // getWindow().getDecorView().rem
                    }
                }
                isShow = !isShow;
            }
        });

        loadPhoto();
    }


    public void loadPhoto() {
        Picasso.with(this).load(photo).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                img.setImageBitmap(bitmap);
                img.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                if (countFalse > 5) {
                    Toast.makeText(PhotoView.this, "Не удалось загрузить фото", Toast.LENGTH_LONG).show();
                } else if (countFalse > 3) {
                    photo = getIntent().getStringExtra("photo_small");
                } else {
                    countFalse++;
                    loadPhoto();
                }
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });
    }


    public int getStatusBarHeight() {
        int result = 0;
        if (Build.VERSION.SDK_INT >= 19) {
            try {
                int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
                if (resourceId > 0) {
                    result = getResources().getDimensionPixelSize(resourceId);
                }
            } catch (NullPointerException e) {
                result = AndroidUtils.dp(25);
            } catch (Exception e) {
                result = AndroidUtils.dp(25);
            }
        }
        return result;
    }
}
