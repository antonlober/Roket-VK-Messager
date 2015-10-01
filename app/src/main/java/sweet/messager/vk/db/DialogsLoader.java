package sweet.messager.vk.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.CursorLoader;

import java.util.ArrayList;

import sweet.messager.vk.ApplicationName;
import sweet.messager.vk.utils.AndroidUtils;

/**
 * Created by antonpolstyanka on 20.05.15.
 */
public class DialogsLoader extends CursorLoader {
    public DialogsLoader(Context context) {
        super(context);
    }

    @Override
    public Cursor loadInBackground() {
        SQLiteDatabase db = new Sql(ApplicationName.getAppContext()).getWritableDatabase();
        Cursor items = db.query("items", null, null, null, null, null, "time DESC LIMIT 24");
        // db.close();
        return items;
    }
}
