package sweet.messager.vk.ui;


import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import sweet.messager.vk.ApplicationName;
import sweet.messager.vk.R;
import sweet.messager.vk.adapters.ImageAdapter;
import sweet.messager.vk.api.Async;
import sweet.messager.vk.api.VK;
import sweet.messager.vk.api.VKException;
import sweet.messager.vk.db.Method;
import sweet.messager.vk.interfaces.FragmentListener;
import sweet.messager.vk.utils.AndroidUtils;

public class PhotosActivity extends Fragment implements AdapterView.OnItemClickListener {

    private FragmentListener mListener;
    String id;
    boolean vk;
    String title;
    GridView gridView;
    ProgressBar progressBar;

    public static PhotosActivity newInstance(String id, String title, boolean vk) {
        PhotosActivity photosActivity = new PhotosActivity();
        Bundle arguments = new Bundle();
        arguments.putString("id", id);
        arguments.putString("title", title);
        arguments.putBoolean("vk", vk);
        photosActivity.setArguments(arguments);
        return photosActivity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        id = bundle.getString("id");
        title = bundle.getString("title");
        vk = bundle.getBoolean("vk");
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        super.onCreateView(layoutInflater, viewGroup, bundle);
        return layoutInflater.inflate(R.layout.activity_attach, null);
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
        gridView = (GridView) view.findViewById(R.id.gridview);
        gridView.setOnItemClickListener(this);
        // gridView.setAdapter(new ImageAdapter(null, vk));
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar3);
        TextView topTitle = (TextView) view.findViewById(R.id.topTitle);
        view.findViewById(R.id.imageView4).setVisibility(View.GONE);
        view.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        topTitle.setText(title);
        gridView.setColumnWidth(AndroidUtils.dp(108));
        List<JSONObject> photos = Method.getPhotos(vk, id);
        if (photos.size() != 0) {
            gridView.setVisibility(View.VISIBLE);
            gridView.setAdapter(new ImageAdapter(photos, vk));
        }
        updatePhotos();

    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        JSONObject photo = (JSONObject) parent.getItemAtPosition(position);
        HashMap<String, Object> put = new HashMap<>();
        put.put("type_photo", photo);
        mListener.onChat(put);
    }


    public void updatePhotos() {
        new GetPhotos().execute();
    }

    class GetPhotos extends Async {

        @Override
        protected Object background() throws VKException {
            if (vk) {
                HashMap<String, Object> post = new HashMap<>();
                post.put("album_id", id);
                try {
                    JSONArray photos = new VK(ApplicationName.getAppContext()).method("photos.get").params(post).getObject().getJSONArray("items");
                    return Method.addPhoto(true, id, photos);
                } catch (JSONException e) {
                    return null;
                }
            } else {
                JSONArray photos = getPhotos(id);
                List<JSONObject> photosSql = Method.addPhoto(false, id, photos);
                // Collections.reverse(photosSql);
                return photosSql;
            }
        }

        @Override
        protected void error(VKException error) {

        }

        @Override
        protected void finish(Object json) {
            if (json != null) {
                List<JSONObject> photos = (List<JSONObject>) json;
                if (photos.size() != 0) {
                    gridView.setVisibility(View.VISIBLE);
                    gridView.setAdapter(new ImageAdapter(photos, vk));
                } else {

                }
            }
        }

        @Override
        protected void start() {

        }
    }



    public JSONArray getPhotos(String id) {
        JSONArray result = new JSONArray();
        final String[] projection = {
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media._ID
        };
        final String selection = MediaStore.Images.Media.BUCKET_ID + " = ?";
        final String[] selectionArgs = { id };
        try {
            final Cursor cursor = getActivity().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    MediaStore.Images.Media._ID + " DESC LIMIT 50");

            if (cursor == null) return result;
            if (cursor.moveToFirst()) {
                JSONObject item;
                final int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                final int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                try {
                    do {
                        item = new JSONObject();
                        String mId = cursor.getString(idColumn);
                        String thumb = AndroidUtils.getThumbPath(mId);
                        String big = cursor.getString(dataColumn);
                        item.put("big", big);
                        item.put("thumb", thumb == null ? big : thumb);
                        item.put("id", cursor.getString(idColumn));
                        result.put(item);
                    } while (cursor.moveToNext());
                } catch (JSONException e) { }
            }
            cursor.close();
        } catch (NullPointerException e) {

        }
        return result;
    }
}
