package sweet.messager.vk.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import sweet.messager.vk.ApplicationName;
import sweet.messager.vk.AttachActivity;
import sweet.messager.vk.Constants;
import sweet.messager.vk.ImActivity;
import sweet.messager.vk.ListActivity;
import sweet.messager.vk.ObjectActivity;
import sweet.messager.vk.R;
import sweet.messager.vk.adapters.Attachments;
import sweet.messager.vk.adapters.Images;
import sweet.messager.vk.adapters.MessagesAdapter;
import sweet.messager.vk.api.Async;
import sweet.messager.vk.api.VK;
import sweet.messager.vk.api.VKException;
import sweet.messager.vk.db.Method;
import sweet.messager.vk.interfaces.FragmentListener;
import sweet.messager.vk.interfaces.OnObjectInfo;
import sweet.messager.vk.interfaces.OnRecyclerItemListener;
import sweet.messager.vk.interfaces.VKListener;
import sweet.messager.vk.model.HistoryResult;
import sweet.messager.vk.model.User;
import sweet.messager.vk.utils.AndroidUtils;
import sweet.messager.vk.utils.CircleTransform;
import sweet.messager.vk.utils.CreateMenu;
import sweet.messager.vk.vk.History;
import sweet.messager.vk.vk.Send;


public class ChatActivity extends Fragment {

    private FragmentListener mListener;
    private Bundle mBundle;
    private RecyclerView list_view_attachments;
    private Attachments attachmentsAdapter;
    private Dialog dialog;
    private ImageView sendButton;
    private MessagesAdapter messages = null;
    private String mCurrentPhotoPath;
    private TextView topTitle;
    private String topText = "off";
    private ImageView photo;
    private TextView chatName;

    public static ChatActivity newInstance(int id) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("isChat", false);
        bundle.putInt("id", id);
        ChatActivity chatActivity = new ChatActivity();
        chatActivity.setArguments(bundle);
        return chatActivity;
    }

    public static ChatActivity newInstance(int id, String photo, String title, String text) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("isChat", true);
        if (photo != null) {
            bundle.putString("photo", photo);
            bundle.putBoolean("isPhoto", true);
        } else {
            bundle.putBoolean("isPhoto", false);
        }
        bundle.putString("chat_title", title);
        bundle.putString("text", text);
        bundle.putInt("id", id);
        ChatActivity chatActivity = new ChatActivity();
        chatActivity.setArguments(bundle);
        return chatActivity;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        mBundle = getArguments();
        Log.e("mBundle", String.valueOf(mBundle));
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof FragmentListener) {
            mListener = (FragmentListener) activity;
        } else {
            // throw new ClassCastException(activity.toString() + " must implement MyListFragment.OnItemSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        super.onCreateView(layoutInflater, viewGroup, bundle);
        return layoutInflater.inflate(R.layout.chat_activity, null);
    }

    @Override
    public void onActivityCreated(Bundle _bundle) {
        super.onActivityCreated(_bundle);
        View view = getView();

        final LinearLayout toolbar = (LinearLayout) view.findViewById(R.id.toolbar);
        if (ApplicationName.colors != null) {
            toolbar.setBackgroundColor(ApplicationName.colors.toolBarColor);
            view.findViewById(R.id.main).setBackgroundColor(ApplicationName.colors.chatBg);
        }

        final RecyclerView chats = (RecyclerView) view.findViewById(R.id.list_view_messages);
        final EditText inputMsg = (EditText) view.findViewById(R.id.inputMsg);
        final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        final TextView text = (TextView) view.findViewById(R.id.text);
        photo = (ImageView) view.findViewById(R.id.photo);
        chatName = (TextView) view.findViewById(R.id.name);
        topTitle = (TextView) view.findViewById(R.id.topTitle);
        final View llMsgCompose = view.findViewById(R.id.llMsgCompose);
        final boolean isChat = mBundle.getBoolean("isChat");
        final int id = mBundle.getInt("id");
        sendButton = (ImageView) view.findViewById(R.id.btnSend);
        list_view_attachments = (RecyclerView) view.findViewById(R.id.list_view_attachments);
        topText = mBundle.getString("title");

        if (isChat) {
            setChat(
                    mBundle.getBoolean("isPhoto"),
                    mBundle.getString("photo"),
                    mBundle.getString("chat_title"),
                    mBundle.getString("text")
            );
        } else {
            setUser(ApplicationName.getUsers(id));
        }


        if (ApplicationName.bgMsg != null) {
            Picasso picasso = Picasso.with(getActivity());
            RequestCreator requestCreator;
            if (ApplicationName.vkBgMsg) {
                requestCreator = picasso.load(ApplicationName.bgMsg);
            } else {
                requestCreator = picasso.load(new File(ApplicationName.bgMsg));
            }
            if (ApplicationName.screenSize != null) {
                requestCreator.resize(
                        ApplicationName.screenSize[0],
                        ApplicationName.screenSize[1]
                );
            }
            requestCreator.into((ImageView) view.findViewById(R.id.bg_chat));
        }

        view.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        chats.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(true);
        chats.setLayoutManager(layoutManager);

        List<JSONObject> items;
        if (isChat) {
            items = Method.getMsg("chat" + Math.abs(id - 2000000000));
        } else {
            items = Method.getMsg("user" + Math.abs(id));
        }
        if (items.size() != 0) {
            messages = MessagesAdapter.setItems(items, isChat, id);
            messages.setFragment(ChatActivity.this);
            chats.setAdapter(messages);
            chats.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }
        if (ApplicationName.getSetting(Constants.UPDATE_MSG)) {
            new History(new History.OnHistoryListener() {

                @Override
                public void onHistory(HistoryResult historyResult) {
                    messages = MessagesAdapter.setItems(historyResult.history, isChat, id);
                    messages.setFragment(ChatActivity.this);
                    chats.setAdapter(messages);
                    chats.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    if (!isChat) {
                        setUser(historyResult.user);
                    } else {
                        setChat(
                                historyResult.isPhoto,
                                historyResult.photo,
                                historyResult.chat_title,
                                historyResult.text
                        );
                    }
                }
            }).execute(isChat ? 1 : 0, id, 0);
        }

        view.findViewById(R.id.attachment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final View menu = LayoutInflater.from(getActivity()).inflate(R.layout.menu, null);

                final RecyclerView imageList = (RecyclerView) menu.findViewById(R.id.list_view_photos);
                imageList.setHasFixedSize(true);
                LinearLayoutManager manager = new LinearLayoutManager(getActivity());
                manager.setOrientation(0);
                imageList.setLayoutManager(manager);


                dialog = new Dialog(getActivity(), R.style.MyMenuTheme);
                dialog.setContentView(menu);
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                Window window = dialog.getWindow();
                window.setGravity(Gravity.BOTTOM);
                dialog.show();

                final List<JSONObject> attachPhotos = new ArrayList<>();

                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        attachPhotos.clear();
                    }
                });
                new CreateMenu(getActivity(), menu, R.menu.attachment, new CreateMenu.OnItemClickListener() {
                    @Override
                    public void onItemClick(int id) {
                        Intent intentAttach = new Intent(getActivity(), ListActivity.class);
                        switch (id) {
                            case R.id.action_add_image:
                                if (attachPhotos.size() == 0) {
                                    Intent intent = new Intent(ApplicationName.getAppContext(), AttachActivity.class);
                                    startActivityForResult(intent, 3);
                                } else {
                                    list_view_attachments.setVisibility(View.VISIBLE);
                                    attachmentsAdapter.addUploadPhoto(attachPhotos);
                                }
                                break;
                            case R.id.action_add_audio:
                                intentAttach.putExtra("type", Constants.AUDIO);
                                // intentAttach.putExtra("ids", attachmentsAdapter.getIds("audio"));
                                startActivityForResult(intentAttach, 1);
                                break;
                            case R.id.action_add_video:
                                intentAttach.putExtra("type", Constants.VIDEO);
                                // intentAttach.putExtra("ids", attachmentsAdapter.getIds("video"));
                                startActivityForResult(intentAttach, 1);
                                break;
                            case R.id.action_add_doc:
                                intentAttach.putExtra("type", Constants.DOC);
                                // intentAttach.putExtra("ids", attachmentsAdapter.getIds("doc"));
                                startActivityForResult(intentAttach, 1);
                                break;
                        }
                        dialog.dismiss();
                    }
                });

                final TextView photosName = (TextView) ((LinearLayout) menu.findViewById(R.id.items)).getChildAt(0).findViewById(R.id.name);
                imageList.setAdapter(new Images(
                        new OnRecyclerItemListener() {
                            @Override
                            public void onClick(int position, Object object) {
                                if (position == 0) {
                                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                    if (takePictureIntent.resolveActivity(ApplicationName.getAppContext().getPackageManager()) != null) {
                                        File photoFile = null;
                                        try {
                                            photoFile = createImageFile();
                                        } catch (IOException ex) {

                                        }
                                        if (photoFile != null) {
                                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                                            startActivityForResult(takePictureIntent, 2);
                                        }
                                    }
                                } else {
                                    int size = attachPhotos.size();
                                    JSONObject item = (JSONObject) object;
                                    if (attachPhotos.indexOf(item) == -1) {
                                        if (size > 9) {
                                            return;
                                        }
                                        attachPhotos.add(item);
                                        size = size + 1;
                                    } else {
                                        attachPhotos.remove(item);
                                        size = Math.abs(size - 1);
                                    }
                                    photosName.setText(size == 0 ? "Изображение" : Html.fromHtml(
                                            "Прикрепить <b>" + AndroidUtils.declOfNum(attachPhotos.size(), new String[]{
                                                    "фотографию",
                                                    "фотографии",
                                                    "фотографий"
                                            }) + "</b>"
                                    ));
                                }
                            }
                        }
                ));
            }
        });


        list_view_attachments.setHasFixedSize(true);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setOrientation(0);
        list_view_attachments.setLayoutManager(manager);
        attachmentsAdapter = new Attachments();
        list_view_attachments.setAdapter(attachmentsAdapter);


        /* Отправить сообщение */
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = String.valueOf(inputMsg.getText()).trim();
                // String attachment = attachmentsAdapter.getAttachment();
                if (!message.equals("") || attachmentsAdapter.items.size() != 0) {
                    if (attachmentsAdapter.isUploading) {
                        Toast.makeText(getActivity(), "Дождитесь окончания загрузки", Toast.LENGTH_LONG).show();
                        return;
                    }
                    final int msg_id = 20000000 + AndroidUtils.random(0, 66666);
                    final String msgText = String.valueOf(inputMsg.getText());
                    HashMap<String, Object> post = new HashMap<String, Object>();
                    if (!msgText.equals("")) {
                        post.put("message", msgText);
                    }
                    String attachmentsIds = attachmentsAdapter.getIds();
                    if (attachmentsIds != null) {
                        post.put("attachment", attachmentsIds);
                    }
                    if (isChat) {
                        post.put("chat_id", Math.abs(id - 2000000000));
                    } else {
                        post.put("user_id", id);
                    }
                    post.put("guid", msg_id);
                    send(post, msg_id);
                    inputMsg.setText("");
                    sendButton.setImageResource(R.mipmap.ic_send_gray);
                    list_view_attachments.setVisibility(View.GONE);
                    attachmentsAdapter.clearAll();
                }
            }
        });


        chats.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItems = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                if (visibleItems < 5) {
                    Log.e("offsetMGS", "load");
                }
            }

        });

        view.findViewById(R.id.objectClick).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(
                        ApplicationName.getAppContext(),
                        ObjectActivity.class
                );
                intent.putExtra("isChat", isChat);
                intent.putExtra("text", topText);
                intent.putExtra("id", id);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ApplicationName.getAppContext().startActivity(intent);
            }
        });
    }

    public void onTyping() {
        try {
            topTitle.setText("Печатает..");
            AndroidUtils.runOnUIThread(new Runnable() {
                public void run() {
                    topTitle.setText(topText);
                }
            }, 5000);
        } catch (NullPointerException e) {

        } catch (IllegalArgumentException e) {

        } catch (Exception e) {

        }
    }

    public void onNewMsg() {
        try {
            topTitle.setText(topText);
        } catch (NullPointerException e) {

        } catch (IllegalArgumentException e) {

        } catch (Exception e) {

        }
    }

    private void send(final HashMap<String, Object> post, final int mgsId) {
        messages.onNewMsg(Send.createMsg(post.containsKey("message") ?
                ((String) post.get("message")) : ""
                , mgsId, attachmentsAdapter.getItems()));
        new Async() {

            @Override
            protected Object background() throws VKException {
                return new VK(getActivity()).method("messages.send").params(post).getInt();
            }

            @Override
            protected void error(VKException error) {
                messages.setErrorMsg(mgsId);
            }

            @Override
            protected void finish(Object json) {
                int id = (int) json;
                messages.setOriginalId(mgsId, id);
            }

            @Override
            protected void start() {

            }
        }.execute();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (data == null) {
                    return;
                }
                try {
                    JSONArray jsonArray = new JSONArray(data.getStringExtra("jsonArray"));
                    attachmentsAdapter.addAttachs(jsonArray);
                    list_view_attachments.setVisibility(View.VISIBLE);
                } catch (JSONException e) {

                }
                break;
            case 2:
                if (data != null) return;
                list_view_attachments.setVisibility(View.VISIBLE);
                dialog.dismiss();
                new AsyncTask<Void, Void, List<JSONObject>>() {

                    ProgressDialog progressDialog;

                    @Override
                    public void onPreExecute() {
                        progressDialog = new ProgressDialog(getActivity());
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progressDialog.setMessage("Обработка...");
                        progressDialog.show();
                    }

                    @Override
                    protected List<JSONObject> doInBackground(Void... params) {
                        List<JSONObject> itemsPhoto = new ArrayList<>();
                        try {
                            File file = new File(mCurrentPhotoPath);
                            String dd = MediaStore.Images.Media.insertImage(ApplicationName.getAppContext().getContentResolver(),
                                    file.getAbsolutePath(),
                                    file.getName(),
                                    file.getName());
                            String[] splitString = dd.split("\\/");
                            String mId = splitString[Math.abs(
                                    splitString.length - 1
                            )];
                            itemsPhoto = AndroidUtils.getThumb(mId, false);
                        } catch (NullPointerException e) {
                        } catch (FileNotFoundException e) {
                            Log.e("CameraPhoto", "no file");
                        }
                        return itemsPhoto;
                    }

                    @Override
                    protected void onPostExecute(List<JSONObject> itemsPhoto) {
                        super.onPostExecute(itemsPhoto);
                        progressDialog.dismiss();
                        if (itemsPhoto.size() != 0) {
                            attachmentsAdapter.addUploadPhoto(itemsPhoto);
                        }
                    }
                }.execute();

                break;
            case 3:
                if (data == null) {
                    return;
                }
                try {
                    JSONArray jsonArray = new JSONArray(data.getStringExtra("jsonArray"));
                    List<JSONObject> itemsPhoto = new ArrayList<>();
                    JSONArray vkPhoto = new JSONArray();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject photo = jsonArray.getJSONObject(i);
                        if (photo.has("photo_130")) {
                            JSONObject photoJSON = new JSONObject();
                            photoJSON.put("photo", photo);
                            photoJSON.put("type", "photo");
                            vkPhoto.put(photoJSON);
                        } else {
                            itemsPhoto.add(photo);
                        }
                    }
                    if (itemsPhoto.size() != 0) {
                        attachmentsAdapter.addUploadPhoto(itemsPhoto);
                        list_view_attachments.setVisibility(View.VISIBLE);
                    }
                    if (vkPhoto.length() != 0) {
                        attachmentsAdapter.addAttachs(vkPhoto);
                        list_view_attachments.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    Log.e("errorJSON", String.valueOf(e));
                }
                break;
        }
    }


    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            if (messages != null) {
                messages.onDestroy();
            }
        } catch (NullPointerException e) {

        } catch (IllegalArgumentException e) {

        } catch (Exception e) {

        }
    }

    @Override
    public void onResume() {
        try {
            if (messages != null) {
                messages.onResume();
            }
        } catch (NullPointerException e) {

        } catch (IllegalArgumentException e) {

        } catch (Exception e) {

        }
        super.onResume();
    }

    @Override
    public void onPause() {
        try {
            if (messages != null) {
                messages.onPause();
            }
        } catch (NullPointerException e) {

        } catch (IllegalArgumentException e) {

        } catch (Exception e) {

        }
        super.onPause();
    }

    public void setUser(User user) {
        chatName.setText(user.name);
        topText = user.text;
        Picasso.with(ApplicationName.getAppContext())
                .load(user.photo_big)
                .transform(new CircleTransform())
                .into(photo);
        topTitle.setText(topText);
    }

    public void setChat(boolean isPhoto, String photo_url, String chat_title, String text) {
        if (isPhoto) {
            Picasso.with(ApplicationName.getAppContext())
                    .load(photo_url)
                    .transform(new CircleTransform())
                    .into(photo);
        }
        chatName.setText(chat_title);
        if (text != null) {
            topText = text;
            topTitle.setText(topText);
        }
    }
}
