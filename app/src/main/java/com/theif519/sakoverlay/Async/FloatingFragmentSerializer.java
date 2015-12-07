package com.theif519.sakoverlay.Async;

import android.os.AsyncTask;
import android.util.ArrayMap;
import android.util.JsonWriter;
import android.util.Log;

import com.theif519.sakoverlay.Rx.RxBus;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

/**
 * Created by theif519 on 10/31/2015.
 * <p/>
 * Author: Louis Jenkins
 * <p/>
 * A simple, yet very powerful utility which accepts any number of mapped data, then serializes them
 * by key-value pairs in JSON format. It uses the entrySet() feature of the map to accurately
 * receive any key-value pairs recorded, and key-value pairs are limited to Strings to make it
 * easier to serialize.
 * <p/>
 * The file to write to must be created in onPreExecute(), and there currently is no need for a
 * onPostExecute(), hence everything besides setting up the File is done on the background thread, meaning
 * it is completely safe for this to run while the activity is paused and/or stopped, as it maintains
 * a strong reference to the data passed, preventing unwanted garbage collection.
 */
public abstract class FloatingFragmentSerializer extends AsyncTask<ArrayMap<String, String>, Void, Void> {

    protected File file;

    /**
     * Should be used to setup the correct file to write to. Only way to get around the limitations
     * of having only one type of vararg parameters without creating an object specifically for it.
     */
    @Override
    abstract protected void onPreExecute();

    /**
     * Directly writes each ArrayMap entry as a JSON entry.
     *
     * @param params List of mapped data to serialize to disk.
     * @return Nothing
     */
    @Override
    protected Void doInBackground(ArrayMap<String, String>... params) {
        try {
            JsonWriter writer = new JsonWriter(new FileWriter(file));
            writer.setIndent(" ");
            writer.beginArray();
            for (ArrayMap<String, String> map : params) {
                writer.beginObject();
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    writer.name(entry.getKey()).value(entry.getValue());
                }
                writer.endObject();
            }
            writer.endArray();
            writer.flush();
            writer.close();
        } catch (IOException e) {
            RxBus.publish("Error while writing to JSON file:\n\"" + e.getMessage() + "\"");
            Log.e(getClass().getSimpleName(), e.getMessage());
            return null;
        }
        return null;
    }
}
