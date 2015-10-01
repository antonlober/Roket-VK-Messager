package sweet.messager.vk.utils;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.StaticLayout;
import android.text.TextPaint;

import java.text.SimpleDateFormat;
import java.util.Date;

import sweet.messager.vk.R;

/**
 * Created by antonpolstyanka on 03.05.15.
 */
public class Timer {

    public static String toDate(int value) {
        /*
        int time = value;
        String timeString = null;
        if (time >= 1 && time < 60) {
            timeString = "" + value;
            if (timeString.length() < 2) {
                timeString += "s";
            }
        } else if (time >= 60 && time < 60 * 60) {
            timeString = "" + value / 60;
            if (timeString.length() < 2) {
                timeString += "m";
            }
        } else if (time >= 60 * 60 && time < 60 * 60 * 24) {
            timeString = "" + value / 60 / 60;
            if (timeString.length() < 2) {
                timeString += "h";
            }
        } else if (time >= 60 * 60 * 24 && time < 60 * 60 * 24 * 7) {
            timeString = "" + value / 60 / 60 / 24;
            if (timeString.length() < 2) {
                timeString += "d";
            }
        } else {
            timeString = "" + value / 60 / 60 / 24 / 7;
            if (timeString.length() < 2) {
                timeString += "w";
            } else if (timeString.length() > 2) {
                timeString = "c";
            }
        }
        return timeString;
         */


        try{
            return String.valueOf(value / 60 / 60 / 24 / 7);
        }
        catch(Exception ex){
            return "xx";
        }
    }

}
