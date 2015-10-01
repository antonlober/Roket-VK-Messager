package sweet.messager.vk.adapters;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import org.apache.http.entity.mime.MultipartEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import sweet.messager.vk.ApplicationName;
import sweet.messager.vk.R;
import sweet.messager.vk.interfaces.UploadListener;
import sweet.messager.vk.utils.AndroidUtils;
import sweet.messager.vk.vk.UploadPhoto;


public class Attachments extends RecyclerView.Adapter<Attachments.ViewHolder> implements UploadListener {

    public List<JSONObject> items = new ArrayList<>();
    public boolean isUploading = false;
    private List<JSONObject> photosUploads = new ArrayList<>();
    private ProgressBar mProgressBar = null;
    private HashMap<String, Integer> photoUploadPosition = new HashMap<>();
    private int mPosition = 0;
    private int mProgress = 0;

    public Attachments() {
        super();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RelativeLayout relativeLayout = new RelativeLayout(parent.getContext());
        int margin = AndroidUtils.dp(4);
        int size = AndroidUtils.dp(82);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        params.setMargins(margin, margin, 0, margin);
        relativeLayout.setLayoutParams(params);
        relativeLayout.setBackgroundColor(1711276032);
        return new ViewHolder(relativeLayout);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        try {
            holder.itemView.removeAllViews();
            JSONObject item = items.get(position);
            JSONObject object;
            final String type = item.getString("type");
            TextView textView;
            ImageView imageView;
            RelativeLayout.LayoutParams paramsView;
            int intValue;
            if (type.equals("audio")) {
                object = item.getJSONObject("audio");
                LinearLayout linearLayout = new LinearLayout(ApplicationName.getAppContext());
                intValue = AndroidUtils.dp(8);
                linearLayout.setPadding(intValue, intValue, intValue, intValue);
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                textView = new TextView(ApplicationName.getAppContext());
                textView.setText("Музыка");
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                textView.setTextColor(Color.WHITE);
                textView.setSingleLine();
                textView.setGravity(Gravity.CENTER);
                linearLayout.addView(textView);

                textView = new TextView(ApplicationName.getAppContext());
                textView.setText(object.getString("title"));
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                textView.setTextColor(Color.WHITE);
                textView.setGravity(Gravity.CENTER);
                textView.setSingleLine();
                intValue = AndroidUtils.dp(2);
                textView.setPadding(0, intValue, 0, intValue);
                linearLayout.addView(textView);

                textView = new TextView(ApplicationName.getAppContext());
                textView.setText(object.getString("artist"));
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                textView.setTextColor(Color.WHITE);
                textView.setGravity(Gravity.CENTER);
                textView.setSingleLine();
                linearLayout.addView(textView);


                paramsView = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                paramsView.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);


                holder.itemView.addView(linearLayout, paramsView);
            } else if (type.equals("video")) {
                object = item.getJSONObject("video");
                String photo = object.getString(object.has("photo_320") ? "photo_320" : "photo_130");
                imageView = new ImageView(ApplicationName.getAppContext());
                imageView.setScaleType(ImageView.ScaleType.CENTER);
                Picasso.with(ApplicationName.getAppContext()).load(photo).into(imageView);
                holder.itemView.addView(imageView);

                intValue = AndroidUtils.dp(2);
                textView = new TextView(ApplicationName.getAppContext());
                textView.setTextColor(Color.WHITE);
                textView.setPadding(intValue, intValue, intValue, intValue);
                textView.setSingleLine();
                textView.setGravity(Gravity.CENTER);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                textView.setText(object.getString("title"));
                textView.setBackgroundColor(Color.parseColor("#80000000"));

                paramsView = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                paramsView.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                holder.itemView.addView(textView, paramsView);

                intValue = AndroidUtils.dp(30);
                imageView = new ImageView(ApplicationName.getAppContext());
                imageView.setBackgroundResource(R.drawable.doc_cub);
                imageView.setImageResource(R.mipmap.ic_attach_play);
                RelativeLayout.LayoutParams paramsPlay = new RelativeLayout.LayoutParams(intValue, intValue);
                paramsPlay.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

                holder.itemView.addView(imageView, paramsPlay);
            } else if (type.equals("doc")) {
                object = item.getJSONObject("doc");
                if (object.has("photo_100")) {
                    String photo = object.getString(object.has("photo_130") ? "photo_130" : "photo_100");
                    imageView = new ImageView(ApplicationName.getAppContext());
                    imageView.setScaleType(ImageView.ScaleType.CENTER);
                    Picasso.with(ApplicationName.getAppContext()).load(photo).into(imageView);
                    holder.itemView.addView(imageView);

                    if (object.getString("ext").equals("gif")) {
                        intValue = AndroidUtils.dp(8);
                        textView = new TextView(ApplicationName.getAppContext());
                        textView.setTextColor(0xffffffff);
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                        textView.setBackgroundResource(R.drawable.doc_cub);
                        textView.setPadding(intValue, intValue, intValue, intValue);
                        textView.setText("GIF");
                        paramsView = new RelativeLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);
                        paramsView.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                        holder.itemView.addView(textView, paramsView);
                    } else {
                        intValue = AndroidUtils.dp(2);
                        textView = new TextView(ApplicationName.getAppContext());
                        textView.setTextColor(Color.WHITE);
                        textView.setPadding(intValue, intValue, intValue, intValue);
                        textView.setSingleLine();
                        textView.setGravity(Gravity.CENTER);
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                        textView.setText(object.getString("title"));
                        textView.setBackgroundColor(Color.parseColor("#80000000"));

                        paramsView = new RelativeLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);
                        paramsView.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                        holder.itemView.addView(textView, paramsView);
                    }

                } else {
                    LinearLayout linearLayout = new LinearLayout(ApplicationName.getAppContext());
                    intValue = AndroidUtils.dp(8);
                    linearLayout.setPadding(intValue, intValue, intValue, intValue);
                    linearLayout.setOrientation(LinearLayout.VERTICAL);
                    textView = new TextView(ApplicationName.getAppContext());
                    textView.setText("Документ");
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                    textView.setTextColor(Color.WHITE);
                    textView.setSingleLine();
                    textView.setGravity(Gravity.CENTER);
                    linearLayout.addView(textView);

                    textView = new TextView(ApplicationName.getAppContext());
                    textView.setText(object.getString("title"));
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                    textView.setTextColor(Color.WHITE);
                    textView.setGravity(Gravity.CENTER);
                    textView.setSingleLine();
                    intValue = AndroidUtils.dp(2);
                    textView.setPadding(0, intValue, 0, intValue);
                    linearLayout.addView(textView);

                    textView = new TextView(ApplicationName.getAppContext());
                    textView.setText(object.getString("ext"));
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                    textView.setTextColor(Color.WHITE);
                    textView.setGravity(Gravity.CENTER);
                    textView.setSingleLine();
                    linearLayout.addView(textView);


                    paramsView = new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    paramsView.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                    holder.itemView.addView(linearLayout, paramsView);
                }
            } else if (type.equals("photo")) {
                object = item.getJSONObject("photo");
                imageView = new ImageView(ApplicationName.getAppContext());
                imageView.setScaleType(ImageView.ScaleType.CENTER);
                Picasso picasso = Picasso.with(ApplicationName.getAppContext());
                if (object.has("thumb")) {
                    picasso.load(new File(object.getString("thumb")))
                            .placeholder(R.mipmap.ic_placeholder)
                            .error(R.mipmap.ic_placeholder)
                            .into(imageView);
                } else if (object.has("mobile_img")) {
                    picasso.load(new File(object.getString("mobile_img")))
                            .placeholder(R.mipmap.ic_placeholder)
                            .error(R.mipmap.ic_placeholder)
                            .into(imageView);
                } else {
                    String photoUrl = object.getString(object.has("photo_130") ? "photo_130" : "photo_75");
                    picasso.load(photoUrl)
                            .placeholder(R.mipmap.ic_placeholder)
                            .error(R.mipmap.ic_placeholder)
                            .into(imageView);
                }
                holder.itemView.addView(imageView);
                if (object.has("thumb")) {
                    RelativeLayout relativeLayout = new RelativeLayout(ApplicationName.getAppContext());
                    relativeLayout.setBackgroundResource(R.drawable.doc_cub);

                    ProgressBar progressBar = new ProgressBar(ApplicationName.getAppContext(), null, android.R.attr.progressBarStyleHorizontal);
                    progressBar.setMax(115);
                    progressBar.setIndeterminate(false);
                    progressBar.setProgressDrawable(
                            ApplicationName.getAppContext()
                                    .getResources()
                                    .getDrawable(R.drawable.progress));
                    if (mPosition == position) {
                        mProgressBar = progressBar;
                        Animation animation = AnimationUtils.loadAnimation(ApplicationName.getAppContext(), R.anim.rotate);
                        mProgressBar.startAnimation(animation);
                        mProgressBar.setProgress(mProgress);
                    }
                    intValue = AndroidUtils.dp(52);
                    paramsView = new RelativeLayout.LayoutParams(intValue, intValue);
                    paramsView.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                    relativeLayout.addView(progressBar, paramsView);

                    intValue = AndroidUtils.dp(82);
                    paramsView = new RelativeLayout.LayoutParams(intValue, intValue);
                    holder.itemView.addView(relativeLayout, paramsView);
                } else {

                }
            }
        } catch (JSONException e) {
            Log.e("errorJSONParse", String.valueOf(e));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onStart() {
        isUploading = true;
        mProgress = 0;
    }

    @Override
    public void onProgress(int progress) {
        if (mProgressBar != null) {
            mProgress = progress;
            mProgressBar.setProgress(progress);
        }
    }

    @Override
    public void onFinish(JSONObject photo) {
        mProgress = 0;
        if (photo == null) {
            Toast.makeText(ApplicationName.getAppContext(), "Загрузка не удалась.", Toast.LENGTH_LONG).show();
        } else {
            try {
                int position = photoUploadPosition.get(photo.getString("phone_id"));
                JSONObject item = new JSONObject();
                if (items.size() > position) {
                    item = items.get(position);
                }
                items.remove(item);
                photo.put("mobile_img", item.getJSONObject("photo").getString("thumb"));
                JSONObject newPhoto = new JSONObject();
                newPhoto.put("type", "photo");
                newPhoto.put("photo", photo);
                items.add(position, newPhoto);
                notifyItemChanged(position);
            } catch (IndexOutOfBoundsException e) {
            } catch (JSONException e) {
                Log.e("UploadPhotoClass", String.valueOf(e));
            }
        }
        isUploading = false;
        if (photosUploads.size() != 0) {
            goUpload(
                    photosUploads.get(0)
            );
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout itemView;
        public ViewHolder(View _itemView) {
            super(_itemView);
            itemView = (RelativeLayout) _itemView;
        }
    }

    public void addAttach(JSONObject item) {
        int size = items.size();
        items.add(item);
        notifyItemInserted(size);
    }

    public void addUploadPhoto(List<JSONObject> photos) {
        try {
            for (int i = 0; i < photos.size(); i++) {
                JSONObject put = new JSONObject();
                JSONObject photo = photos.get(i);
                put.put("type", "photo");
                put.put("photo", photo);
                photoUploadPosition.put(photo.getString("id"), items.size());
                addAttach(put);
                goUpload(photo);
            }
        } catch (JSONException e) { }


    }

    public void addAttachs(JSONArray _items) {
        try {
            for (int i = 0; i < _items.length(); i++) {
                addAttach(_items.getJSONObject(i));
            }
        } catch (JSONException e) { }
    }

    public int getSize() {
        return items.size();
    }

    public void goUpload(JSONObject photo) {
        if (isUploading) {
            photosUploads.add(photo);
        } else {
            photosUploads.remove(photo);
            try {
                String imgId = photo.getString("id");
                String big = photo.has("big") ? photo.getString("big") : null;
                mPosition = photoUploadPosition.get(imgId);
                notifyItemChanged(mPosition);
                new UploadPhoto(this).execute(imgId, big);
                isUploading = true;
            } catch (JSONException e) { }
        }
    }

    public JSONArray getItems() {
        List<JSONObject> audios = new ArrayList<>();
        JSONArray _return = new JSONArray();
        try {
            for (int i = 0; i < items.size(); i++) {
                JSONObject item = items.get(i);
                if (item.getString("type").equals("audio")) {
                    audios.add(item);
                } else {
                    _return.put(item);
                }
            }
            if (audios.size() != 0) {
                for (int i = 0; i < audios.size(); i++) {
                    _return.put(audios.get(i));
                }
            }
        } catch (JSONException e) { }
        return _return;
    }

    public String getIds() {
        ArrayList<String> audios = new ArrayList<>();
        ArrayList<String> _return = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            try {
                JSONObject item = items.get(i);
                JSONObject object = new JSONObject();
                final String type = item.getString("type");
                StringBuilder stringBuilder = new StringBuilder(type);
                if (type.equals("audio")) {
                    object = item.getJSONObject("audio");
                } else if (type.equals("video")) {
                    object = item.getJSONObject("video");
                } else if (type.equals("doc")) {
                    object = item.getJSONObject("doc");
                } else if (type.equals("photo")) {
                    object = item.getJSONObject("photo");
                }
                stringBuilder.append(object.getInt("owner_id"));
                stringBuilder.append("_");
                stringBuilder.append(object.getInt("id"));
                if (type.equals("audio")) {
                    audios.add(stringBuilder.toString());
                } else {
                    _return.add(stringBuilder.toString());
                }
            } catch (NullPointerException e) {
                Log.e("errorGetIds", String.valueOf(e));
            } catch (JSONException e) {
                Log.e("errorGetIds", String.valueOf(e));
            }
        }
        _return.addAll(audios);
        if (_return.size() == 0) {
            return null;
        }
        return TextUtils.join(",", _return);
    }

    public void clearAll() {
        items.clear();
        notifyDataSetChanged();
    }

    class testClass extends MultipartEntity {

    }
}
