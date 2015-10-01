package sweet.messager.vk.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.CursorLoader;

import java.util.ArrayList;
import java.util.HashMap;

import sweet.messager.vk.ApplicationName;

/**
 * Created by antonpolstyanka on 20.05.15.
 */
public class PutDialogsLoader extends CursorLoader {

    ArrayList<HashMap<String, Object>> items;

    public PutDialogsLoader(Context context, ArrayList<HashMap<String, Object>> _items) {
        super(context);
        items = _items;
    }

    @Override
    public Cursor loadInBackground() {
        SQLiteDatabase db = new Sql(ApplicationName.getAppContext()).getWritableDatabase();
        Cursor items = db.query("items", null, null, null, null, null, "time DESC LIMIT 24");
        // db.close();
        return items;
    }
}
