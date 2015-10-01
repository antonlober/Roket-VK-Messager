package sweet.messager.vk.utils;


import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AlignmentSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import sweet.messager.vk.ApplicationName;
import sweet.messager.vk.Cells.VideoCell;
import sweet.messager.vk.Constants;
import sweet.messager.vk.R;
import sweet.messager.vk.interfaces.OnCreateView;
import sweet.messager.vk.model.User;
import sweet.messager.vk.services.VideoPlayer;

public class Body {

    public static String toBody(JSONObject message, String from) {
        String html = getHtml();
        String you = "";
        try {
            you = (message.getInt("out") == 1) ? html.replace("*", "Вы: ") : from == null ? "" : html.replace("*", from + ": ");
            Boolean isHtml = false;
            String text = message.getString("body");
            if (text.equals("")) {
                isHtml = true;
                if (message.has("action")) {
                    text = "Событие в чате";
                } else if (message.has("fwd_messages")) {
                    text = "Пересланные сообщения";
                } else if (message.has("geo")) {
                    text = "Карта";
                } else if (message.has("attachments")) {
                    String type = message.getJSONArray("attachments").getJSONObject(0).getString("type");
                    if (type.equals("photo")) {
                        text = "Фото";
                    } else if (type.equals("video")) {
                        text = "Видео";
                    } else if (type.equals("audio")) {
                        text = "Аудио";
                    } else if (type.equals("doc")) {
                        text = "Документ";
                    } else if (type.equals("wall")) {
                        text = "Запись";
                    } else if (type.equals("wall_reply")) {
                        text = "Комментарий";
                    } else if (type.equals("sticker")) {
                        text = "Стикер";
                    } else if (type.equals("gift")) {
                        text = "Подарок";
                    }
                }
            }
            return you + (isHtml ? html.replace("*", text) : text);
        } catch (JSONException e) {
            return you + html.replace("*", "Другое");
        }
    }

    public static String getHtml() {
        String color = "#6B9CC2";
        if (ApplicationName.colors != null) {
            color = String.valueOf(ApplicationName.colors.textColor);
        }
        return "<font color=\"" + color + "\">*</font>";
    }

    public static ArrayList<HashMap<String, Object>> splitMsg(JSONObject msg) {
        ArrayList<HashMap<String, Object>> _return = new ArrayList<>();
        try {
            HashMap<String, Object> post;
            String body = msg.getString("body");
            int msg_id = msg.getInt("id");


            if (msg.has("action")) {
                String type = msg.getString("action");
                String text = null;
                if (type.equals("chat_title_update")) {
                    text = "Название изменено";
                } else if (type.equals("chat_create")) {
                    text = "Создан чат";
                } else if (type.equals("chat_photo_update")) {
                    text = "Новая фотография";
                } else if (type.equals("chat_invite_user")) {
                    text = "Приглашение";
                } else if (type.equals("chat_kick_user")) {
                    text = "Вышел";
                }
                if (text != null) {
                    JSONObject bodyJSON = new JSONObject("{\"type\":\"event\",\"event\":\"" + text + "\"}");
                    post = new HashMap<>();
                    post.put("my", msg.getInt("out") == 1);
                    post.put("body", bodyJSON);
                    post.put("read", msg.getInt("read_state") == 1);
                    post.put("time", msg.getInt("date"));
                    post.put("attach", true);
                    post.put("msg_id", msg_id);
                    post.put("user_id", msg.getInt("user_id"));
                    _return.add(post);
                    return _return;
                }
            }

            if (msg.has("geo")) {
                JSONObject geo = msg.getJSONObject("geo");
                String coordinates = geo.getString("coordinates");
                String[] llSplit = coordinates.split(" ");
                String ll = llSplit[1] + "," + llSplit[0];
                String titleMap = "Место";
                String nameMap = "Без названия";
                if (geo.has("place")) {
                    JSONObject place = geo.getJSONObject("place");
                    titleMap = place.has("title") ? place.getString("title") : "Место";
                    nameMap =
                            place.has("country") ?
                                    place.getString("country") + (
                                            place.has("city") ? ", " + place.getString("city") : ""
                                    )
                                    : "Без названия";
                }
                JSONObject bodyJSON = new JSONObject("{\"type\":\"map\",\"map\":{\"ll\":\"" + ll + "\",\"title\":\"" + titleMap + "\",\"name\":\"" + nameMap + "\"}}");
                post = new HashMap<>();
                post.put("my", msg.getInt("out") == 1);
                post.put("body", bodyJSON);
                post.put("read", msg.getInt("read_state") == 1);
                post.put("time", msg.getInt("date"));
                post.put("attach", true);
                post.put("msg_id", msg_id);
                post.put("user_id", msg.getInt("user_id"));
                _return.add(post);
            }


            if (msg.has("fwd_messages")) {
                    JSONArray fwd_messages = msg.getJSONArray("fwd_messages");
                    for (int n = 0; n < fwd_messages.length(); n++) {
                        JSONObject fwd_message = fwd_messages.getJSONObject(n);
                        post = new HashMap<>();
                        post.put("body", fwd_message.getString("body"));
                        post.put("my", msg.getInt("out") == 1);
                        post.put("read", msg.getInt("read_state") == 1);
                        post.put("time", msg.getInt("date"));
                        post.put("attach", true);
                        post.put("msg_id", msg_id);
                        post.put("user_id", msg.getInt("user_id"));
                        _return.add(post);
                    }
            }
            if (msg.has("attachments")) {
                JSONArray attachments = msg.getJSONArray("attachments");
                for (int n = Math.abs(attachments.length() - 1); n >= 0; n--) {
                    JSONObject attachment = attachments.getJSONObject(n);
                    post = new HashMap<>();
                    post.put("my", msg.getInt("out") == 1);
                    post.put("body", attachment);
                    post.put("read", msg.getInt("read_state") == 1);
                    post.put("time", msg.getInt("date"));
                    post.put("attach", true);
                    post.put("msg_id", msg_id);
                    post.put("user_id", msg.getInt("user_id"));
                    _return.add(post);
                }
            }

            if (!body.equals("")) {
                post = new HashMap<>();
                post.put("my", msg.getInt("out") == 1);
                post.put("body", msg.getString("body"));
                post.put("read", msg.getInt("read_state") == 1);
                post.put("time", msg.getInt("date"));
                post.put("attach", false);
                post.put("user_id", msg.getInt("user_id"));
                post.put("msg_id", msg_id);
                _return.add(post);
            }
        } catch (JSONException e) {

        }
        return _return;
    }

    public static void createMsg(JSONObject msg, OnCreateView createView, int fwd) {
        try {
            Context context = ApplicationName.getAppContext();
            boolean hiddenDate = false;
            int my = msg.has("out") ? msg.getInt("out") : 2;
            int maxWidth = AndroidUtils.dp(200);
            int[] pixels;

            if (msg.has("attachments")) {
                hiddenDate = true;
                JSONArray attachments = msg.getJSONArray("attachments");

                ImageView imageView;
                LinearLayout linearLayout;
                LinearLayout.LayoutParams params;
                RelativeLayout relativeLayout;
                TextView textView;

                for (int i = 0; i < attachments.length(); i++) {
                    boolean end = Math.abs(attachments.length() - 1) == i;
                    boolean top = (i == 0);
                    int imgPadding = AndroidUtils.dp(2);
                    JSONObject attachment = attachments.getJSONObject(i);
                    String type = attachment.getString("type");
                    final JSONObject object;
                    if (type.equals("photo")) {
                        object = attachment.getJSONObject("photo");

                        /* View Photo */
                        int width = object.getInt("width");
                        int height = object.getInt("height");
                        int new_height = 220 * height / width;
                        imageView = new ImageView(context);
                        imageView.setBackgroundColor(218103808);
                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        String photo;
                        if (object.has("photo_256")) {
                            photo = object.getString("photo_256");
                        } else if (object.has("photo_604")) {
                            photo = object.getString("photo_604");
                        } else {
                            photo = object.getString(object.has("130") ? "130" : "photo_75");
                        }
                        pixels = new int[] {
                                AndroidUtils.dp(220),
                                AndroidUtils.dp(new_height)
                        };
                        params = new LinearLayout.LayoutParams(pixels[0], pixels[1]);
                        if (ApplicationName.getSetting(Constants.IMAGE_LOAD)) {
                            Picasso.with(context).load(photo).resize(pixels[0], pixels[1]).into(imageView);
                        }
                        params.setMargins(0, 0, 0, end ? 0 : imgPadding);
                        imageView.setLayoutParams(params);
                        String photo_small = object.getString("photo_75");
                        if (object.has("photo_2560")) {
                            photo = object.getString("photo_2560");
                            photo_small = object.getString("photo_604");
                        } else if (object.has("photo_1280")) {
                            photo = object.getString("photo_1280");
                            photo_small = object.getString("photo_604");
                        } else if (object.has("photo_807")) {
                            photo = object.getString("photo_807");
                            photo_small = object.getString("photo_604");
                        } else if (object.has("photo_604")) {
                            photo = object.getString("photo_604");
                            photo_small = object.getString("photo_130");
                        } else if (object.has("photo_130")) {
                            photo = object.getString("photo_130");
                            photo_small = object.getString("photo_75");
                        }
                        final String finalPhoto = photo;
                        final String finalSmall = photo_small;
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ApplicationName.showPhoto(finalPhoto, finalSmall);
                            }
                        });
                        createView.onView(imageView);
                    } else if (type.equals("video")) {
                        object = attachment.getJSONObject("video");
                        relativeLayout = new RelativeLayout(context);
                        relativeLayout.setBackgroundColor(218103808);
                        pixels = new int[]{
                                AndroidUtils.dp(220),
                                AndroidUtils.dp(160)
                        };
                        params = new LinearLayout.LayoutParams(pixels[0], pixels[1]);
                        params.setMargins(0, 0, 0, end ? 0 : imgPadding);
                        relativeLayout.setLayoutParams(params);

                        int defaultPadding = AndroidUtils.dp(2);
                        textView = new TextView(context);
                        textView.setTextColor(0xffffffff);
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                        textView.setBackgroundResource(R.drawable.doc_cub);
                        textView.setPadding(defaultPadding, defaultPadding, defaultPadding, defaultPadding);
                        textView.setText(new SimpleDateFormat("mm:ss").format(new Date(object.getInt("duration") * 1000)));

                        imageView = new ImageView(context);
                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        final String poster = object.has("photo_320") ? object.getString("photo_320") : object.getString("photo_130");
                        if (ApplicationName.getSetting(Constants.IMAGE_LOAD)) {
                            Picasso.with(context).load(poster).resize(pixels[0], pixels[1]).into(imageView);
                        }
                        relativeLayout.addView(imageView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

                        int playPadding = AndroidUtils.dp(2);
                        imageView = new ImageView(context);
                        imageView.setBackgroundResource(R.drawable.doc_cub);
                        imageView.setImageResource(R.mipmap.ic_attach_play);
                        imageView.setPadding(playPadding, playPadding, playPadding, playPadding);
                        RelativeLayout.LayoutParams paramsPlay = new RelativeLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);
                        paramsPlay.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

                        relativeLayout.addView(imageView, paramsPlay);
                        LinearLayout.LayoutParams durationParams = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);
                        int marginDuration = AndroidUtils.dp(4);
                        durationParams.setMargins(marginDuration, marginDuration, marginDuration, marginDuration);
                        relativeLayout.addView(textView, durationParams);

                        String videoID = object.getInt("owner_id") + "_" + object.getInt("id");
                        if (object.has("access_key")) {
                            videoID += "_" + object.getString("access_key");
                        }
                        final String VideoIdFianal = videoID;
                        relativeLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ApplicationName.startVideo(poster, VideoIdFianal);
                            }
                        });
                        createView.onView(relativeLayout);
                    } else if (type.equals("gift")) {
                        object = attachment.getJSONObject("gift");

                        /* View Photo */
                        int width = 256;
                        int height = 256;
                        int new_height = 220 * height / width;
                        imageView = new ImageView(context);
                        imageView.setBackgroundColor(218103808);
                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        String photo = object.getString("thumb_256");
                        pixels = new int[] {
                                AndroidUtils.dp(220),
                                AndroidUtils.dp(new_height)
                        };
                        params = new LinearLayout.LayoutParams(pixels[0], pixels[1]);
                        if (ApplicationName.getSetting(Constants.IMAGE_LOAD)) {
                            Picasso.with(context).load(photo).resize(pixels[0], pixels[1]).into(imageView);
                        }
                        params.setMargins(0, 0, 0, end ? 0 : imgPadding);
                        imageView.setLayoutParams(params);
                        createView.onView(imageView);
                    } else if (type.equals("doc")) {
                        object = attachment.getJSONObject("doc");
                        String ext = object.getString("ext");
                        if (ext.equals("gif")) {
                            relativeLayout = new RelativeLayout(context);
                            relativeLayout.setBackgroundColor(218103808);
                            pixels = new int[] {
                                    AndroidUtils.dp(220),
                                    AndroidUtils.dp(120)
                            };
                            params = new LinearLayout.LayoutParams(
                                    pixels[0],
                                    pixels[1]);

                            params.setMargins(0, 0, 0, end ? 0 : imgPadding);
                            relativeLayout.setLayoutParams(params);

                            imageView = new ImageView(context);
                            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            if (ApplicationName.getSetting(Constants.IMAGE_LOAD)) {
                                Picasso.with(context).load(object.getString(object.has("photo_100") ? "photo_100" : "photo_130")).resize(pixels[0], pixels[1]).into(imageView);
                            }

                            relativeLayout.addView(imageView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));


                            int defaultPadding = AndroidUtils.dp(8);
                            textView = new TextView(context);
                            textView.setTextColor(0xffffffff);
                            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                            textView.setBackgroundResource(R.drawable.doc_cub);
                            textView.setPadding(defaultPadding, defaultPadding, defaultPadding, defaultPadding);
                            textView.setText("GIF");
                            RelativeLayout.LayoutParams paramsPlay = new RelativeLayout.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT);
                            paramsPlay.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

                            relativeLayout.addView(textView, paramsPlay);
                            createView.onView(relativeLayout);
                        } else {
                            linearLayout = new LinearLayout(context);
                            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                            linearLayout.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    try {
                                        AndroidUtils.goBrowse(object.getString("url"));
                                    } catch (JSONException e) { }
                                }
                            });
                            int playButtonSize = AndroidUtils.dp(42);
                            params = new LinearLayout.LayoutParams(playButtonSize, playButtonSize);

                            imageView = new ImageView(context);
                            imageView.setImageResource(R.mipmap.ic_attach_doc);
                            imageView.setBackgroundColor(218103808);
                            imageView.setLayoutParams(params);
                            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            int p = AndroidUtils.dp(72);
                            params = new LinearLayout.LayoutParams(
                                    p,
                                    p);
                            imageView.setLayoutParams(params);
                            if (object.has("photo_100") && ApplicationName.getSetting(Constants.IMAGE_LOAD)) {
                                Picasso.with(context).load(object.getString(
                                        object.has("photo_100") ? "photo_100" : "photo_130"
                                )).resize(p, p).into(imageView);
                            }
                            linearLayout.addView(imageView);

                            LinearLayout doc = new LinearLayout(context);
                            doc.setOrientation(LinearLayout.VERTICAL);
                            doc.setPadding(AndroidUtils.dp(6), 0, 0, 0);

                            maxWidth = AndroidUtils.dp(110);
                            textView = new TextView(context);
                            textView.setSingleLine();
                            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                            textView.setText(object.getString("title"));
                            textView.setTextColor(0xff000000);
                            textView.setMaxWidth(maxWidth);
                            doc.addView(textView);

                            textView = new TextView(context);
                            textView.setSingleLine();
                            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                            textView.setText(ext);
                            textView.setMaxWidth(maxWidth);
                            textView.setPadding(0, AndroidUtils.dp(2), 0, 0);
                            textView.setTextColor(0xff000000);
                            doc.addView(textView);
                            linearLayout.addView(doc);
                            if (!end) {
                                linearLayout.setPadding(0, 0, 0, AndroidUtils.dp(2));
                            }
                            createView.onView(linearLayout);
                        }
                    } else if (type.equals("sticker")) {
                        object = attachment.getJSONObject("sticker");
                        imageView = new ImageView(context);
                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        int width = object.getInt("width");
                        int height = object.getInt("height");
                        int new_height = 120 * height / width;
                        pixels = new int[] {
                                AndroidUtils.dp(120),
                                AndroidUtils.dp(new_height)
                        };
                        params = new LinearLayout.LayoutParams(pixels[0], pixels[1]);
                        imageView.setLayoutParams(params);
                        if (ApplicationName.getSetting(Constants.IMAGE_LOAD)) {
                            Picasso.with(context).load(object.getString("photo_128")).resize(pixels[0], pixels[1]).into(imageView);
                        }
                        createView.onView(imageView);
                    } else if (type.equals("link")) {
                        object = attachment.getJSONObject("link");
                        linearLayout = new LinearLayout(context);
                        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                        linearLayout.setBackgroundResource(R.drawable.left_border);

                        linearLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    AndroidUtils.goBrowse(object.getString("url"));
                                } catch (JSONException e) {

                                }
                            }
                        });

                        LinearLayout doc = new LinearLayout(context);
                        doc.setOrientation(LinearLayout.VERTICAL);
                        doc.setPadding(AndroidUtils.dp(6), 0, 0, 0);

                        maxWidth = AndroidUtils.dp(110);
                        textView = new TextView(context);
                        textView.setSingleLine();
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                        textView.setText(object.getString("title"));
                        textView.setTextColor(0xff000000);
                        textView.setMaxWidth(maxWidth);
                        doc.addView(textView);

                        textView = new TextView(context);
                        textView.setSingleLine();
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                        textView.setText(object.getString("description"));
                        textView.setMaxWidth(maxWidth);
                        textView.setPadding(0, AndroidUtils.dp(2), 0, 0);
                        textView.setTextColor(0xff000000);
                        doc.addView(textView);
                        linearLayout.addView(doc);
                        if (!end) {
                            linearLayout.setPadding(0, 0, 0, AndroidUtils.dp(2));
                        }
                        createView.onView(linearLayout);
                    } else if (type.equals("wall")) {
                        object = attachment.getJSONObject("wall");
                        linearLayout = new LinearLayout(context);
                        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                        linearLayout.setBackgroundResource(R.drawable.left_border);
                        linearLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    AndroidUtils.goBrowse("https://m.vk.com/wall" + object.getInt("from_id") + "_" + object.getInt("id"));
                                } catch (JSONException e) {

                                }
                            }
                        });
                        LinearLayout doc = new LinearLayout(context);
                        doc.setOrientation(LinearLayout.VERTICAL);
                        doc.setPadding(AndroidUtils.dp(6), 0, 0, 0);

                        maxWidth = AndroidUtils.dp(110);
                        textView = new TextView(context);
                        textView.setSingleLine();
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                        textView.setText("Запись на стене");
                        textView.setTextColor(0xff000000);
                        textView.setMaxWidth(maxWidth);
                        doc.addView(textView);

                        boolean html = false;
                        String postMsg = object.getString("text");
                        if (postMsg.equals("")) {
                            html = true;
                            postMsg = Body.getHtml().replace("*", "Вложения");
                        }
                        textView = new TextView(context);
                        textView.setSingleLine();
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                        textView.setText(html ? Html.fromHtml(postMsg) : postMsg);
                        textView.setMaxWidth(maxWidth);
                        textView.setPadding(0, AndroidUtils.dp(2), 0, 0);
                        textView.setTextColor(0xff000000);
                        doc.addView(textView);
                        linearLayout.addView(doc);
                        if (!end) {
                            linearLayout.setPadding(0, 0, 0, AndroidUtils.dp(2));
                        }
                        createView.onView(linearLayout);
                    } else if (type.equals("wall_reply")) {
                        object = attachment.getJSONObject("wall_reply");
                        linearLayout = new LinearLayout(context);
                        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                        linearLayout.setBackgroundResource(R.drawable.left_border);
                        linearLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    AndroidUtils.goBrowse("https://m.vk.com/wall" + object.getInt("owner_id") + "_" + object.getInt("post_id") + "?reply=" + object.getInt("id") + "#reply" + object.getInt("id"));
                                } catch (JSONException e) {

                                }
                            }
                        });
                        LinearLayout doc = new LinearLayout(context);
                        doc.setOrientation(LinearLayout.VERTICAL);
                        doc.setPadding(AndroidUtils.dp(6), 0, 0, 0);

                        maxWidth = AndroidUtils.dp(110);
                        textView = new TextView(context);
                        textView.setSingleLine();
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                        textView.setText("Комментарий");
                        textView.setTextColor(0xff000000);
                        textView.setMaxWidth(maxWidth);
                        doc.addView(textView);

                        String postMsg = object.getString("text");
                        boolean html = false;
                        if (postMsg.equals("")) {
                            html = true;
                            postMsg = Body.getHtml().replace("*", "Вложения");
                        }
                        textView = new TextView(context);
                        textView.setSingleLine();
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                        textView.setText(html ? Html.fromHtml(postMsg) : postMsg);
                        textView.setMaxWidth(maxWidth);
                        textView.setPadding(0, AndroidUtils.dp(2), 0, 0);
                        textView.setTextColor(0xff000000);
                        doc.addView(textView);
                        linearLayout.addView(doc);
                        if (!end) {
                            linearLayout.setPadding(0, 0, 0, AndroidUtils.dp(2));
                        }
                        createView.onView(linearLayout);
                    } else if (type.equals("audio")) {
                        object = attachment.getJSONObject("audio");
                        linearLayout = new LinearLayout(context);
                        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                        int playButtonSize = AndroidUtils.dp(42);
                        params = new LinearLayout.LayoutParams(playButtonSize, playButtonSize);

                        imageView = new ImageView(context);
                        imageView.setImageResource(R.mipmap.ic_attach_play);
                        imageView.setBackgroundResource(R.drawable.doc_cub);
                        imageView.setLayoutParams(params);
                        linearLayout.addView(imageView);

                        LinearLayout audio = new LinearLayout(context);
                        audio.setOrientation(LinearLayout.VERTICAL);
                        audio.setPadding(AndroidUtils.dp(6), 0, 0, 0);

                        maxWidth = AndroidUtils.dp(220);
                        textView = new TextView(context);
                        textView.setSingleLine();
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                        textView.setText(object.getString("title"));
                        textView.setTextColor(0xff000000);
                        textView.setMaxWidth(maxWidth);
                        audio.addView(textView);

                        textView = new TextView(context);
                        textView.setSingleLine();
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                        textView.setText(object.getString("artist"));
                        textView.setPadding(0, AndroidUtils.dp(2), 0, 0);
                        textView.setTextColor(0xff000000);
                        textView.setMaxWidth(maxWidth);
                        audio.addView(textView);
                        linearLayout.addView(audio);
                        if (!end) {
                            linearLayout.setPadding(0, 0, 0, AndroidUtils.dp(2));
                        }
                        createView.onView(linearLayout);
                    }
                }
            }

            if (msg.has("geo")) {
                ImageView imageView = new ImageView(context);
                imageView.setBackgroundColor(218103808);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);


                JSONObject geo = msg.getJSONObject("geo");
                String coordinates = geo.getString("coordinates");
                String[] llSplit = coordinates.split(" ");
                String ll = llSplit[1] + "," + llSplit[0];

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(AndroidUtils.dp(220), AndroidUtils.dp(80));

                imageView.setLayoutParams(params);
                if (ApplicationName.getSetting(Constants.IMAGE_LOAD)) {
                    Picasso.with(context).load("https://static-maps.yandex.ru/1.x/?ll=" + ll + "&size=80,80&z=12&lang=ru-RU&l=map&key=AKPeBEwBAAAA0qePSQIA03AwA4O4ze6XTqIecsNp7REB6VYAAAAAAAAAAADNzChqedeUxsCAyYkFUHiD7MPITA==").into(imageView);
                }
                createView.onView(imageView);
            }

            if (msg.has("fwd_messages")) {
                hiddenDate = true;
                JSONArray fwd_messages = msg.getJSONArray("fwd_messages");
                int size = fwd_messages.length();
                for (int i = 0; i < size; i++) {
                    View fwdView = LayoutInflater.from(context).inflate(R.layout.fwd_view, null, false);
                    JSONObject fwd_message = fwd_messages.getJSONObject(i);
                    final User user = ApplicationName.getUsers(fwd_message.getInt("user_id"));
                    fwdView.findViewById(R.id.click).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AndroidUtils.goUser(user);
                        }
                    });
                    Picasso.with(context).load(user.photo).transform(new CircleTransform()).into((ImageView) fwdView.findViewById(R.id.avatar));
                    ((TextView) fwdView.findViewById(R.id.author_name)).setText(user.name);
                    String time = ApplicationName.simpleDateFormat.format(new Date(msg.getInt("date") * 1000L));
                    ((TextView) fwdView.findViewById(R.id.msg_time)).setText(time);
                    final LinearLayout main = (LinearLayout) fwdView.findViewById(R.id.main);
                    fwd++;
                    Body.createMsg(fwd_message, new OnCreateView() {
                        @Override
                        public void onView(View v) {
                            main.addView(v);
                        }

                        @Override
                        public void onView(View v, int index) {
                            main.addView(v, index);
                        }
                    }, fwd);
                    createView.onView(fwdView);
                }
            }

            String body = msg.getString("body");
            if (!body.equals("") && fwd > 0) {
                int defaultPadding = AndroidUtils.dp(3);
                TextView viewBody = new TextView(context);
                viewBody.setText(Html.fromHtml(body, imgGetter, null));
                viewBody.measure(0, 0);
                viewBody.setPadding(defaultPadding, 0, defaultPadding, 0);
                viewBody.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                viewBody.setMaxWidth(AndroidUtils.dp(220));
                viewBody.setTextColor(0xff000000);
                createView.onView(viewBody, 0);
            }
        } catch (JSONException e) {

        }
    }


    public static String titleAttach(String type) {
        String attach;
        if (type.equals("wall")) {
            attach = ApplicationName.getStr(R.string.wall);
        } else if (type.equals("wall_reply")) {
            attach = ApplicationName.getStr(R.string.wall_reply);
        } else if (type.equals("sticker")) {
            attach = ApplicationName.getStr(R.string.sticker);
        } else if (type.equals("gift")) {
            attach = ApplicationName.getStr(R.string.gift);
        } else if (type.equals("photo")) {
            attach = ApplicationName.getStr(R.string.photo);
        } else if (type.equals("video")) {
            attach = ApplicationName.getStr(R.string.video);
        } else if (type.equals("audio")) {
            attach = ApplicationName.getStr(R.string.audio);
        } else if (type.equals("doc")) {
            attach = ApplicationName.getStr(R.string.doc);
        } else if (type.equals("geo")) {
            attach = ApplicationName.getStr(R.string.geo);
        } else if (type.equals("link")) {
            attach = ApplicationName.getStr(R.string.link);
        } else if (type.equals("fwd_messages")) {
            attach = ApplicationName.getStr(R.string.fwd_messages);
        } else {
            attach = type;
        }
        return attach;
    }

    public static String chatEventString(String source_act, String source_mid, boolean out, User aUser) {
        int mid = 0;
        User mUser = null;
        if (source_mid != null) {
            try {
                mid = Integer.parseInt(source_mid);
            } catch (NumberFormatException e) { }
            if (mid != 0) {
                mUser = ApplicationName.getUsers(mid);
            }
        }
        String attach;
        switch (source_act) {
            case "chat_title_update":
                attach = AndroidUtils.chatEvent(out, aUser.sex, "изменил") + " название чата";
                break;
            case "chat_photo_update":
                attach = AndroidUtils.chatEvent(out, aUser.sex, "обновил") + " фото чата";
                break;
            case "chat_photo_remove":
                attach = AndroidUtils.chatEvent(out, aUser.sex, "удалил") + " фото чата";
                break;
            case "chat_create":
                attach = AndroidUtils.chatEvent(out, aUser.sex, "создал") + " чат";
                break;
            case "chat_invite_user":
                assert mUser != null;
                if (aUser.id == mid) {
                    attach = AndroidUtils.chatEvent(out, aUser.sex, new String[] {
                            "вернулись",
                            "вернулся",
                            "вернулась"
                    }) + " в чат";
                } else {
                    attach = AndroidUtils.chatEvent(out, aUser.sex, "пригласил") + " " + mUser.name + " в чат";
                }
                break;
            case "chat_kick_user":
                assert mUser != null;
                if (aUser.id == mid) {
                    attach = AndroidUtils.chatEvent(out, aUser.sex, new String[] {
                            "вышли",
                            "вышел",
                            "вышла"
                    }) + " из чата";
                } else {
                    attach = AndroidUtils.chatEvent(out, aUser.sex, "удалил") + " " + mUser.name + " из чата";
                }
                break;
            default:
                attach = source_act;
                break;
        }
        return attach;
    }


    public static Html.ImageGetter imgGetter = new Html.ImageGetter() {

        public Drawable getDrawable(String source) {
            Drawable drawable = null;
            if (source.equals("done.png")) {
                drawable = ApplicationName.getAppContext().getResources().getDrawable(R.mipmap.ic_done);
            }
            // drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            int size = AndroidUtils.dp(16);
            drawable.setBounds(0, 0, size, size);
            return drawable;
        }
    };
}
