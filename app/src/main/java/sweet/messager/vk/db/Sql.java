package sweet.messager.vk.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import sweet.messager.vk.ApplicationName;


public class Sql extends SQLiteOpenHelper {

    public Sql(Context context) {
        super(context, "myDB", null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE object ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "json BLOB);");
        db.execSQL("CREATE TABLE dialogs ("
                + "position INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "json BLOB);");
        db.execSQL("CREATE TABLE msg ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "key TEXT NOT NULL, "
                + "json BLOB);");
        db.execSQL("CREATE TABLE audio ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "json BLOB);");
        db.execSQL("CREATE TABLE video ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "json BLOB);");
        db.execSQL("CREATE TABLE doc ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "json BLOB);");
        db.execSQL("CREATE TABLE photo_vk (" // Пустая
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "json BLOB);");
        db.execSQL("CREATE TABLE photo_sd (" // Пустая
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "json BLOB);");
        db.execSQL("CREATE TABLE album_vk (" // Пустая
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "json BLOB);");
        db.execSQL("CREATE TABLE album_sd (" // Пустая
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "json BLOB);");
        db.execSQL("CREATE TABLE media_msg (" // Пустая
                + "position INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "json BLOB,"
                + "type TEXT);");
        db.execSQL("CREATE TABLE chats (" // Пустая
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "json BLOB);");
        db.execSQL("CREATE TABLE fave (" // Пустая
                + "position INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "json BLOB);");
        db.execSQL("CREATE TABLE stickers (" // Пустая
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "json BLOB);");
        db.execSQL("CREATE TABLE emoji (" // Пустая
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "path TEXT,"
                + "un TEXT);");
        db.execSQL("CREATE TABLE top (" // Пустая
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "json BLOB);");

        Log.e("sqlCreate", "create All table");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("sqlCreate", " --- onUpgrade database from " + oldVersion
                + " to " + newVersion + " version --- ");
        /*
        if (newVersion == 2 && 1 == oldVersion) {
            db.execSQL("CREATE TABLE friends ("
                    + "position INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "user_id INTEGER,"
                    + "type INTEGER);");
        }
         */
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // onUpgrade(db, oldVersion, newVersion);
    }
}
