package sweet.messager.vk.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import sweet.messager.vk.ApplicationName;
import sweet.messager.vk.AttachActivity;
import sweet.messager.vk.ColorSelectActivity;
import sweet.messager.vk.Constants;
import sweet.messager.vk.R;
import sweet.messager.vk.interfaces.FragmentListener;


public class SettingsActivity extends Fragment implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private FragmentListener mListener;
    SwitchCompat notification, read, music, image, load, videoPlayer;
    ImageView wallPaper_image;
    View color_view;
    Toolbar toolbar;
    TextView account_category, design_category, message_category;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        super.onCreateView(layoutInflater, viewGroup, bundle);
        return layoutInflater.inflate(R.layout.settings_activity, null);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof FragmentListener) {
            mListener = (FragmentListener) activity;
        } else {
            throw new ClassCastException(activity.toString() + " must implement MyListFragment.OnItemSelectedListener");
        }
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        View view = getView();
        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setTitle("Настройки");
        toolbar.setNavigationIcon(R.mipmap.ic_show_menu);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onFragment(100);
            }
        });

        account_category = (TextView) view.findViewById(R.id.account_category);
        design_category = (TextView) view.findViewById(R.id.design_category);
        message_category = (TextView) view.findViewById(R.id.message_category);

        notification = (SwitchCompat) view.findViewById(R.id.notification_switch);
        read = (SwitchCompat) view.findViewById(R.id.read_switch);
        music = (SwitchCompat) view.findViewById(R.id.music_switch);
        image = (SwitchCompat) view.findViewById(R.id.image_switch);
        load = (SwitchCompat) view.findViewById(R.id.load_switch);
        videoPlayer = (SwitchCompat) view.findViewById(R.id.videoPlayer_switch);

        notification.setChecked(ApplicationName.getSetting(Constants.NOTIFICATION));
        read.setChecked(ApplicationName.getSetting(Constants.READ_MSG));
        music.setChecked(ApplicationName.getSetting(Constants.MELODY));
        image.setChecked(ApplicationName.getSetting(Constants.IMAGE_LOAD));
        load.setChecked(ApplicationName.getSetting(Constants.UPDATE_MSG));
        videoPlayer.setChecked(ApplicationName.getSetting(Constants.VIDEO_PLAYER));

        notification.setOnCheckedChangeListener(this);
        read.setOnCheckedChangeListener(this);
        music.setOnCheckedChangeListener(this);
        image.setOnCheckedChangeListener(this);
        load.setOnCheckedChangeListener(this);
        videoPlayer.setOnCheckedChangeListener(this);

        wallPaper_image = (ImageView) view.findViewById(R.id.wallPaper_image);
        color_view = view.findViewById(R.id.color_view);

        if (ApplicationName.bgMsg != null) {
            Picasso picasso = Picasso.with(getActivity());
            RequestCreator requestCreator;
            if (ApplicationName.vkBgMsg) {
                requestCreator = picasso.load(ApplicationName.bgMsg);
            } else {
                requestCreator = picasso.load(new File(ApplicationName.bgMsg));
            }
            requestCreator.into(wallPaper_image);
        }

        view.findViewById(R.id.load_click).setOnClickListener(this);
        view.findViewById(R.id.image_click).setOnClickListener(this);
        view.findViewById(R.id.music_click).setOnClickListener(this);
        view.findViewById(R.id.read_click).setOnClickListener(this);
        view.findViewById(R.id.notification_click).setOnClickListener(this);
        view.findViewById(R.id.videoPlayer_click).setOnClickListener(this);
        view.findViewById(R.id.exit_click).setOnClickListener(this);
        view.findViewById(R.id.wallPaper_click).setOnClickListener(this);
        view.findViewById(R.id.color_click).setOnClickListener(this);
        setColor();
    }

    public void setColor() {
        if (ApplicationName.colors != null) {
            color_view.setBackgroundColor(ApplicationName.colors.toolBarColor);
            toolbar.setBackgroundColor(ApplicationName.colors.toolBarColor);
            notification.setDrawingCacheBackgroundColor(ApplicationName.colors.toolBarColor);
            if (Build.VERSION.SDK_INT >= 21) {
                Window window = getActivity().getWindow();
                window.setStatusBarColor(ApplicationName.colors.statusBarColor);
            }
            account_category.setTextColor(ApplicationName.colors.textColor);
            design_category.setTextColor(ApplicationName.colors.textColor);
            message_category.setTextColor(ApplicationName.colors.textColor);
            mListener.onFragment(200);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        String key = null;
        switch (buttonView.getId()) {
            case R.id.notification_switch:
                key = Constants.NOTIFICATION;
                break;
            case R.id.read_switch:
                key = Constants.READ_MSG;
                break;
            case R.id.music_switch:
                key = Constants.MELODY;
                break;
            case R.id.image_switch:
                key = Constants.IMAGE_LOAD;
                break;
            case R.id.load_switch:
                key = Constants.UPDATE_MSG;
                break;
            case R.id.videoPlayer_switch:
                key = Constants.VIDEO_PLAYER;
                break;
        }
        if (key != null) {
            ApplicationName.setSettings(key, isChecked);
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.notification_click:
                notification.setChecked(!notification.isChecked());
                break;
            case R.id.read_click:
                read.setChecked(!read.isChecked());
                break;
            case R.id.music_click:
                music.setChecked(!music.isChecked());
                break;
            case R.id.image_click:
                image.setChecked(!image.isChecked());
                break;
            case R.id.load_click:
                load.setChecked(!load.isChecked());
                break;
            case R.id.videoPlayer_click:
                videoPlayer.setChecked(!videoPlayer.isChecked());
                break;
            case R.id.exit_click:
                ApplicationName.logout();
                break;
            case R.id.wallPaper_click:
                intent = new Intent(ApplicationName.getAppContext(), AttachActivity.class);
                startActivityForResult(intent, 3);
                break;
            case R.id.color_click:
                intent = new Intent(ApplicationName.getAppContext(), ColorSelectActivity.class);
                startActivityForResult(intent, 2);
                break;
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 2:
                setColor();
                break;
            case 3:
                if (data == null) {
                    return;
                }
                try {
                    JSONArray jsonArray = new JSONArray(data.getStringExtra("jsonArray"));
                    JSONObject photo = jsonArray.getJSONObject(0);
                    if (photo.has("big")) {
                        String urlPhoto = photo.getString("big");
                        ApplicationName.setBgMsg(urlPhoto, false);
                        File imgFile = new File(urlPhoto);
                        if (imgFile.exists()) {
                            wallPaper_image.setImageURI(Uri.fromFile(imgFile));
                        }
                    } else if (photo.has("photo_1280")) {
                        String urlPhoto = photo.getString("photo_1280");
                        ApplicationName.setBgMsg(urlPhoto, true);
                        Picasso.with(ApplicationName.getAppContext()).load(urlPhoto).into(wallPaper_image);
                    } else if (photo.has("photo_807")) {
                        String urlPhoto = photo.getString("photo_807");
                        ApplicationName.setBgMsg(urlPhoto, true);
                        Picasso.with(ApplicationName.getAppContext()).load(urlPhoto).into(wallPaper_image);
                    } else if (photo.has("photo_604")) {
                        String urlPhoto = photo.getString("photo_604");
                        ApplicationName.setBgMsg(urlPhoto, true);
                        Picasso.with(ApplicationName.getAppContext()).load(urlPhoto).into(wallPaper_image);
                    }
                    mListener.onFragment(200);
                } catch (JSONException e) {
                    Log.e("errorJSON", String.valueOf(e));
                } catch (OutOfMemoryError e) {
                    Toast.makeText(getActivity(), "Фон установлен", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
}
