package sweet.messager.vk.ui;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
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
import java.util.HashMap;
import java.util.List;

import sweet.messager.vk.ApplicationName;
import sweet.messager.vk.R;
import sweet.messager.vk.adapters.AlbumsAdapter;
import sweet.messager.vk.adapters.ImageAdapter;
import sweet.messager.vk.api.Async;
import sweet.messager.vk.api.VK;
import sweet.messager.vk.api.VKException;
import sweet.messager.vk.db.Method;
import sweet.messager.vk.interfaces.FragmentListener;
import sweet.messager.vk.model.Album;
import sweet.messager.vk.utils.AndroidUtils;


public class AlbumsActivity extends Fragment implements AdapterView.OnItemClickListener {

    private FragmentListener mListener;
    GridView gridView;
    ProgressBar progressBar;
    boolean vk = false;

    public static AlbumsActivity newInstance() {
        AlbumsActivity albumsActivity = new AlbumsActivity();
        return albumsActivity;
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
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar3);
        final TextView topTitle = (TextView) view.findViewById(R.id.topTitle);
        final PopupMenu popupMenu = new PopupMenu(getActivity(), view.findViewById(R.id.v));
        popupMenu.inflate(R.menu.photo_direcroty);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                topTitle.setText(item.getTitle());
                if (item.getItemId() == R.id.action_vk) {
                    vk = true;
                    new VKAlbums().execute();
                } else {
                    vk = false;
                    new LoadingAlbums().execute();
                }
                return false;
            }
        });
        view.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        topTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.show();
            }
        });
        gridView.setOnItemClickListener(this);
        new LoadingAlbums().execute();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Album albumObj = (Album) parent.getItemAtPosition(position);
        HashMap<String, Object> album = new HashMap<>();
        album.put("id", String.valueOf(id));
        album.put("title", albumObj.title);
        album.put("vk", vk);
        mListener.onChat(album);
    }


    class VKAlbums extends Async {


        @Override
        protected Object background() throws VKException {
            List<Album> _return = new ArrayList<>();
            HashMap<String, Object> post = new HashMap<>();
            post.put("need_system", 1);
            post.put("need_covers", 1);
            try {
                JSONArray albums = new VK(ApplicationName.getAppContext()).method("photos.getAlbums").params(post).getObject().getJSONArray("items");
                Album album;
                for (int i = 0; i < albums.length(); i++) {
                    JSONObject albumVK = albums.getJSONObject(i);
                    album = new Album();
                    album.title = albumVK.getString("title");
                    album.bucket = String.valueOf(albumVK.getInt("id"));
                    album.count = albumVK.getInt("size");
                    album.taken = null;
                    album.modified = null;
                    album.vk = true;
                    album.uri = albumVK.getString("thumb_src");
                    album.id = String.valueOf(albumVK.getInt("id"));
                    _return.add(album);
                }
            } catch (JSONException e) { }
            Method.addAlbums(_return, true);
            return _return;
        }

        @Override
        protected void error(VKException error) {

        }

        @Override
        protected void finish(Object json) {
            progressBar.setVisibility(View.GONE);
            if (json != null) {
                List<Album> items = (List<Album>) json;
                gridView.setAdapter(new AlbumsAdapter(items));
                gridView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected void start() {
            List<Album> albums = Method.getAlbums(true);
            if (albums.size() != 0) {
                gridView.setAdapter(new AlbumsAdapter(albums));
                gridView.setVisibility(View.VISIBLE);
            } else {
                gridView.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
            }
        }
    }

    class LoadingAlbums extends AsyncTask<Void, Void, List<Album>> {

        @Override
        protected void onPreExecute() {
            List<Album> albums = Method.getAlbums(false);
            if (albums.size() != 0) {
                gridView.setAdapter(new AlbumsAdapter(albums));
                gridView.setVisibility(View.VISIBLE);
            } else {
                gridView.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected List<Album> doInBackground(Void... params) {
            List<Album> albums = new ArrayList<>();

            Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            String[] projection = new String[]{
                    MediaStore.Images.Media.BUCKET_ID,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DATE_TAKEN,
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.DATE_MODIFIED
            };

            try {
                String BUCKET_ORDER_BY = MediaStore.Images.Media.DATE_MODIFIED + " DESC";
                String BUCKET_GROUP_BY = "1) GROUP BY 1,(2";
                Cursor cursor = getActivity().getContentResolver().query(images,
                        projection,
                        BUCKET_GROUP_BY,
                        null,
                        BUCKET_ORDER_BY + " LIMIT 9"
                );

                if (cursor != null) {
                    int dataColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                    int bucketColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
                    int idColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                    int bucketIdColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID);
                    int takenIdColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);
                    int modifiedColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED);
                    Album album;
                    while (cursor.moveToNext()) {
                        String mId = cursor.getString(idColumnIndex);
                        String thumb = AndroidUtils.getThumbPath(mId);
                        album = new Album();
                        album.title = cursor.getString(bucketColumnIndex);
                        album.bucket = cursor.getString(bucketIdColumnIndex);
                        album.count = getPhotos(album.bucket).size();
                        album.taken = cursor.getString(takenIdColumnIndex);
                        album.modified = cursor.getString(modifiedColumnIndex);
                        album.uri = thumb == null ? cursor.getString(dataColumnIndex) : thumb;
                        album.id = cursor.getString(idColumnIndex);
                        if (album.title.equals("Camera")) {
                            albums.add(0, album);
                        } else {
                            albums.add(album);
                        }
                    }
                    cursor.close();
                }
                Method.addAlbums(albums, false);
            } catch (NullPointerException e) {

            }
            return albums;
        }

        @Override
        protected void onPostExecute(final List<Album> items) {
            progressBar.setVisibility(View.GONE);
            Log.e("allPhonePhotos", String.valueOf(items));
            gridView.setAdapter(new AlbumsAdapter(items));
            gridView.setVisibility(View.VISIBLE);
        }
    }


    public List<JSONObject> getPhotos(String id) {
        final String[] projection = {
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media._ID
        };
        final String selection = MediaStore.Images.Media.BUCKET_ID + " = ?";
        final String[] selectionArgs = { id };
        final Cursor cursor = getActivity().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null);
        List<JSONObject> result = new ArrayList<>();
        if (cursor == null) return result;
        if (cursor.moveToFirst()) {
            JSONObject item;
            final int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            final int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
            try {
                do {
                    item = new JSONObject();
                    item.put("thumb", cursor.getString(dataColumn));
                    item.put("id", cursor.getString(idColumn));
                    result.add(item);
                } while (cursor.moveToNext());
            } catch (JSONException e) { }
        }
        cursor.close();
        return result;
    }




}
