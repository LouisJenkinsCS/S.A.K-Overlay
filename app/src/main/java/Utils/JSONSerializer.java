package Utils;

import android.os.AsyncTask;
import android.util.ArrayMap;
import android.util.JsonWriter;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

/**
 * Created by theif519 on 10/31/2015.
 *
 * Author: Louis Jenkins
 *
 * A simple, yet very powerful utility which accepts any number of mapped data, then serializes them
 * by key-value pairs in JSON format. It uses the entrySet() feature of the map to accurately
 * receive any key-value pairs recorded, and key-value pairs are limited to Strings to make it
 * easier to serialize.
 *
 * The file to write to must be created in onPreExecute(), and there currently is no need for a
 * onPostExecute(), hence everything besides setting up the File is done on the background thread, meaning
 * it is completely safe for this to run while the activity is paused and/or stopped, as it maintains
 * a strong reference to the data passed, preventing unwanted garbage collection.
 */
public abstract class JSONSerializer extends AsyncTask<ArrayMap<String, String>, Void, Void> {

    protected File file;

    /**
     * Should be used to setup the correct file to write to. Only way to get around the limitations
     * of having only one type of vararg parameters without creating an object specifically for it.
     */
    @Override
    abstract protected void onPreExecute();

    /**
     * Utilizes the entrySet() feature to serialize any key-value pairs. Writes any mapped
     * data to disk. On error, it will stop immediately.
     * @param params List of mapped data to serialize to disk.
     * @return
     */
    @Override
    protected Void doInBackground(ArrayMap<String, String>... params) {
        try {
            JsonWriter writer = new JsonWriter(new FileWriter(file));
            writer.setIndent(" ");
            writer.beginArray();
            for(ArrayMap<String, String> map : params){
                writer.beginObject();
                for(Map.Entry<String, String> entry: map.entrySet()){
                    writer.name(entry.getKey()).value(entry.getValue());
                }
                writer.endObject();
            }
            writer.endArray();
            writer.flush();
            writer.close();
        } catch (IOException e) {
            Log.e(getClass().getSimpleName(), e.getMessage());
            return null;
        }
        return null;
    }
}
