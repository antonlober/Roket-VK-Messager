package sweet.messager.vk;


import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import sweet.messager.vk.adapters.ObjectAdapter;
import sweet.messager.vk.db.Method;
import sweet.messager.vk.model.ObjectModel;
import sweet.messager.vk.model.User;
import sweet.messager.vk.utils.AndroidUtils;
import sweet.messager.vk.utils.CircleTransform;
import sweet.messager.vk.utils.TextViewSpan;
import sweet.messager.vk.view.InfoBlock;

public class ObjectActivity extends AppCompatActivity {

    int totalScroll = 0;
    int minHeaderTranslation;
    int toolbarTitleLeftMargin;
    int actionBarHeight;
    float paddingLeft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        final ImageView photo = (ImageView) findViewById(R.id.photo);
        final TextView name = (TextView) findViewById(R.id.name);
        final TextView topTitle = (TextView) findViewById(R.id.topTitle);

        Intent intent = getIntent();
        int id = intent.getIntExtra("id", 0);
        topTitle.setText(intent.getStringExtra("text"));
        List<ObjectModel> adapterItems = new ArrayList<>();

        try {
            if (!intent.getBooleanExtra("isChat", false)) {
                User user = ApplicationName.getUsers(id);
                name.setText(user.name);
                Picasso.with(this).load(user.photo_big).transform(new CircleTransform()).into(photo);


                ObjectModel objectModel = new ObjectModel();
                objectModel.type = 0;
                adapterItems.add(objectModel);

                objectModel = new ObjectModel();
                objectModel.type = 1;
                objectModel.typeIcon = R.mipmap.ic_profile_status;
                objectModel.typeTitle = "Статус";
                objectModel.typeText = user.status;
                adapterItems.add(objectModel);

                objectModel = new ObjectModel();
                objectModel.type = 1;
                objectModel.typeIcon = R.mipmap.ic_profile_bdate;
                objectModel.typeTitle = "Дата рождения";
                objectModel.typeText = user.bdate;
                adapterItems.add(objectModel);

                objectModel = new ObjectModel();
                objectModel.type = 2;
                objectModel.typeId = 1;
                objectModel.typeIcon = R.mipmap.ic_all_media;
                objectModel.typeTitle = "Материалы беседы";
                adapterItems.add(objectModel);


                /*
                objectModel = new ObjectModel();
                objectModel.type = 2;
                objectModel.typeId = 2;
                objectModel.typeIcon = R.mipmap.ic_profile_block;
                objectModel.typeTitle = "Заблокировать пользователя";
                adapterItems.add(objectModel);
                 */



            } else {
                ObjectModel objectModel = new ObjectModel();
                objectModel.type = 0;
                adapterItems.add(objectModel);

                objectModel = new ObjectModel();
                objectModel.type = 2;
                objectModel.typeId = 1;
                objectModel.typeIcon = R.mipmap.ic_all_media;
                objectModel.typeTitle = "Материалы беседы";
                adapterItems.add(objectModel);
                JSONObject chat = Method.getChat(id);
                name.setText(chat.getString("title"));
                Picasso.with(this).load(chat.getString("photo_100")).transform(new CircleTransform()).into(photo);
                if (chat.has("users")) {
                    JSONArray users = chat.getJSONArray("users");
                    if (users.length() != 0) {
                        objectModel = new ObjectModel();
                        objectModel.type = 4;
                        adapterItems.add(objectModel);
                    }
                    for (int i = 0; i < users.length(); i++) {
                        objectModel = new ObjectModel();
                        objectModel.type = 5;
                        objectModel.user = users.getJSONObject(i);
                        adapterItems.add(objectModel);
                    }
                }
            }
        } catch (JSONException e) {

        }

        TypedValue tv = new TypedValue();
        actionBarHeight = AndroidUtils.dp(56);
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }
        toolbarTitleLeftMargin = AndroidUtils.dp(40);
        minHeaderTranslation = -AndroidUtils.dp(148) + actionBarHeight;


        toolbar.setNavigationIcon(R.mipmap.ic_toolbar_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        if (ApplicationName.colors != null) {
            if (Build.VERSION.SDK_INT >= 21) {
                getWindow().setStatusBarColor(ApplicationName.colors.statusBarColor);
            }
            toolbar.setBackgroundColor(ApplicationName.colors.toolBarColor);
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.list_object);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new ObjectAdapter(adapterItems, intent.getBooleanExtra("isChat", false), id));
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalScroll += dy;
                int height = AndroidUtils.dp(50);
                float offset = (1 - Math.max((float) (-minHeaderTranslation - totalScroll) / -minHeaderTranslation, 0f));

                if (height > totalScroll) {
                    int newHeight = Math.abs(height - totalScroll);
                    paddingLeft = toolbarTitleLeftMargin * offset;
                    if (Build.VERSION.SDK_INT >= 11) {
                        photo.setTranslationY(newHeight);
                        name.setTranslationY(newHeight);
                        topTitle.setTranslationY(newHeight);
                        photo.setTranslationX(paddingLeft);
                        name.setTranslationX(paddingLeft);
                        topTitle.setTranslationX(paddingLeft);
                    } else {
                        TranslateAnimation anim = new TranslateAnimation(paddingLeft, paddingLeft, newHeight, newHeight);
                        anim.setFillAfter(true);
                        anim.setDuration(0);
                        photo.startAnimation(anim);
                        name.startAnimation(anim);
                        topTitle.startAnimation(anim);
                    }
                    int photoSize = Math.max(actionBarHeight, Math.abs(AndroidUtils.dp(90) - totalScroll));
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(photoSize, photoSize);
                    params.setMargins(AndroidUtils.dp(30), 0, 0, 0);
                    photo.setLayoutParams(params);
                } else {
                    int leftPadding = AndroidUtils.dp(22);
                    if (Build.VERSION.SDK_INT >= 11) {
                        photo.setTranslationY(0);
                        name.setTranslationY(0);
                        topTitle.setTranslationY(0);
                        photo.setTranslationX(leftPadding);
                        name.setTranslationX(leftPadding);
                        topTitle.setTranslationX(leftPadding);
                    } else {
                        TranslateAnimation anim= new TranslateAnimation(leftPadding, leftPadding, 0, 0);
                        anim.setFillAfter(true);
                        anim.setDuration(0);
                        photo.startAnimation(anim);
                        name.startAnimation(anim);
                        topTitle.startAnimation(anim);
                    }
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(actionBarHeight, actionBarHeight);
                    params.setMargins(AndroidUtils.dp(30), 0, 0, 0);
                    photo.setLayoutParams(params);
                }

                //photo.setTranslationX(dy);
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

            }
        });



    }

}
