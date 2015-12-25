package com.theif519.sakoverlay.Sessions;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.Looper;
import android.util.Log;

import com.theif519.sakoverlay.Sessions.Widgets.WidgetSessionData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by theif519 on 12/10/2015.
 */
public class SessionDatabase extends SQLiteOpenHelper {

    public static final String TABLE_NAME = "Widget_Session_Data", WIDGET_ID = "Id", WIDGET_DATA = "Data", WIDGET_TAG = "Tag";

    SQLiteDatabase mDatabase;

    private SQLiteStatement mUpdate;

    private SQLiteStatement mInsert;

    private SQLiteStatement mDelete;

    public SessionDatabase(Context context) {
        super(context, "SessionData.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + TABLE_NAME + "(Id INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + WIDGET_TAG + " TEXT," + WIDGET_DATA + " BLOB)"
        );
        Log.i(getClass().getName(), "Created the database!");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
        Log.i(getClass().getName(), "Upgraded database!");
    }

    private WidgetSessionData parse(Cursor c) {
        return new WidgetSessionData(
                c.getLong(c.getColumnIndex(WIDGET_ID)),
                c.getString(c.getColumnIndex(WIDGET_TAG)),
                c.getBlob(c.getColumnIndex(WIDGET_DATA))
        );
    }

    private void setupIfNecessary() {
        if(Looper.getMainLooper() == Looper.myLooper()) throw new RuntimeException("Sorry, wrong thread buddy!");
        if (mDatabase == null) mDatabase = getWritableDatabase();
        if (mUpdate == null) mUpdate = mDatabase.compileStatement("UPDATE " + TABLE_NAME + " SET "
                + WIDGET_DATA + "=? WHERE " + WIDGET_ID + "=?");
        if (mInsert == null)
            mInsert = mDatabase.compileStatement("INSERT INTO " + TABLE_NAME + " VALUES (NULL, ?,?)");
        if(mDelete == null){
            mDelete = mDatabase.compileStatement("DELETE FROM " + TABLE_NAME + " WHERE " + WIDGET_ID + "=?");
        }
    }


    public WidgetSessionData read(int id) {
        setupIfNecessary();
        Cursor cursor = mDatabase.query(TABLE_NAME, new String[]{
                WIDGET_ID, WIDGET_TAG, WIDGET_DATA
        }, WIDGET_ID + "=" + id, null, null, null, null);
        if (cursor == null) {
            Log.w(getClass().getName(), "Sorry, the table for id: " + id + " does not exist!");
            return null;
        }
        cursor.moveToFirst();
        WidgetSessionData data = parse(cursor);
        cursor.close();
        Log.i(getClass().getName(), "Read: " + data);
        return data;
    }

    /**
     * Will read all session data from the table if possible. It returns a
     * ArrayMap which maps each WidgetSessionData to it's Tag, making it very easy
     * to instantiate it.
     *
     * @return Map of WidgetSessionData mapped to their tag.
     */
    public List<WidgetSessionData> readAll() {
        setupIfNecessary();
        Cursor cursor = mDatabase.query(TABLE_NAME, new String[]{
                WIDGET_ID, WIDGET_TAG, WIDGET_DATA
        }, null, null, null, null, null);
        if (cursor == null || cursor.getCount() == 0) {
            Log.w(getClass().getName(), "Sorry, the table is empty!");
            return null;
        }
        List<WidgetSessionData> list = new ArrayList<>();
        cursor.moveToFirst();
        do {
            WidgetSessionData data = parse(cursor);
            list.add(data);
            Log.i(getClass().getName(), "Read: " + data);
        } while (cursor.moveToNext());
        Log.i(getClass().getName(), "Read all session data!");
        cursor.close();
        return list;
    }

    /**
     * Determines whether a row exists in the main table.
     *
     * @param id Id
     * @return If exists
     */
    private boolean exists(int id) {
        Cursor cursor = mDatabase.rawQuery("Select * from " + TABLE_NAME + " where " + WIDGET_ID + " = " + id, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public long insert(WidgetSessionData session){
        setupIfNecessary();
        mInsert.bindString(1, session.getTag());
        mInsert.bindBlob(2, session.getData());
        long id = mInsert.executeInsert();
        Log.i(getClass().getName(), "Inserted #" + id + " with Tag: " + session.getTag());
        return id;
    }

    /**
     * Updates a row in the table with the passed information, and if not exists it will create a new one.
     * If it does exist, it will only update the passed data.
     *
     * @param session The data to be updated to disk.
     */
    public void update(WidgetSessionData session) {
        setupIfNecessary();
        mUpdate.bindBlob(1, session.getData());
        mUpdate.bindLong(2, session.getId());
        Log.i(getClass().getName(), "Updated: " + session);
        mUpdate.executeUpdateDelete();
    }

    public void delete(long id){
        setupIfNecessary();
        mDelete.bindLong(1, id);
        Log.i(getClass().getName(), "Deleted: #" + id);
        mDelete.executeUpdateDelete();
    }
}
