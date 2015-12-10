package com.theif519.sakoverlay.Sessions;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by theif519 on 12/10/2015.
 */
public class SessionDatabase extends SQLiteOpenHelper {

    private SQLiteDatabase mDatabase;

    public static final String TABLE_NAME = "Widget Session Data", WIDGET_ID = "Identifier", WIDGET_DATA = "Data", WIDGET_TAG = "Tag";

    public SessionDatabase(Context context){
        super(context, "SessionData.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + TABLE_NAME + "(" + WIDGET_ID + " INTEGER PRIMARY KEY,"
                        + WIDGET_TAG + " TEXT," + WIDGET_DATA + " BLOB)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    private WidgetSessionData parse(Cursor c){
        int id = -1;
        String tag = null;
        byte data[] = null;
        for(int i = 0; i < c.getColumnCount(); i++){
            switch(c.getColumnName(i)){
                case WIDGET_TAG:
                    tag = c.getString(i);
                    break;
                case WIDGET_DATA:
                    data = c.getBlob(i);
                    break;
                case WIDGET_ID:
                    id = c.getInt(i);
                    break;
            }
        }
        return new WidgetSessionData(id, tag, data);
    }


    public WidgetSessionData read(int id){
        if(mDatabase == null) mDatabase = this.getWritableDatabase();
        Cursor cursor = mDatabase.query(TABLE_NAME, new String[]{
                WIDGET_ID, WIDGET_TAG, WIDGET_DATA
        }, WIDGET_ID + "=" + id, null, null, null, null);
        if(cursor == null){
            Log.w(getClass().getName(), "Sorry, the table for id: " + id + " does not exist!");
            return null;
        }
        cursor.moveToFirst();
        WidgetSessionData data = parse(cursor);
        cursor.close();
        return data;
    }

    /**
     * Will read all session data from the table if possible. It returns a
     * ArrayMap which maps each WidgetSessionData to it's Tag, making it very easy
     * to instantiate it.
     * @return Map of WidgetSessionData mapped to their tag.
     */
    public List<WidgetSessionData> readAll(){
        if(mDatabase == null) mDatabase = this.getWritableDatabase();
        Cursor cursor = mDatabase.query(TABLE_NAME, new String[] {
                WIDGET_ID, WIDGET_TAG, WIDGET_DATA
        }, null, null, null, null, null);
        if(cursor == null){
            Log.w(getClass().getName(), "Sorry, the table is empty!");
            return null;
        }
        List<WidgetSessionData> list = new ArrayList<>();
        cursor.moveToFirst();
        do {
            list.add(parse(cursor));
        } while(cursor.moveToNext());
        cursor.close();
        return list;
    }

    /**
     * Determines whether a row exists in the main table.
     * @param id Id
     * @return If exists
     */
    private boolean exists(int id){
        Cursor cursor = mDatabase.rawQuery("Select * from " + TABLE_NAME + " where " + WIDGET_ID + " = " + id, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    /**
     * Updates a row in the table with the passed information, and if not exists it will create a new one.
     * If it does exist, it will only update the passed data.
     * @param session The data to be updated to disk.
     */
    public void update(WidgetSessionData session){
        if(mDatabase == null) mDatabase = this.getWritableDatabase();
        try {
            mDatabase.beginTransaction();
            ContentValues values = new ContentValues();
            values.put(WIDGET_DATA, session.getData());
            if(exists(session.getId())){
                mDatabase.update(TABLE_NAME, values, WIDGET_ID + "=" + session.getId(), null);
            } else {
                values.put(WIDGET_ID, session.getId());
                values.put(WIDGET_TAG, session.getTag());
                mDatabase.insert(TABLE_NAME, null, values);
            }
            mDatabase.setTransactionSuccessful();
        } finally {
            mDatabase.endTransaction();
        }
    }
}
