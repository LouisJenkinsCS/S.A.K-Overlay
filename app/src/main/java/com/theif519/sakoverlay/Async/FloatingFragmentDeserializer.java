package com.theif519.sakoverlay.Async;

import android.os.AsyncTask;
import android.util.ArrayMap;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import com.theif519.sakoverlay.Rx.RxBus;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by theif519 on 10/31/2015.
 * <p/>
 * A simple, yet very powerful utilities which converts serialized key-value pairs into ArrayMaps.
 * JSONObjects get turned into ArrayMaps, and the root, JSONArray, is the actual mapList.
 */
abstract public class FloatingFragmentDeserializer extends AsyncTask<Void, Void, List<ArrayMap<String, String>>> {

    protected File file;

    /**
     * Should be used initialize the file used to read from.
     */
    @Override
    abstract protected void onPreExecute();

    /**
     * Where the implementer handles the returned list of maps.
     * Note: It can return null on an IOException, and should be handled appropriately.
     *
     * @param mapList Parsed list of maps.
     */
    @Override
    abstract protected void onPostExecute(List<ArrayMap<String, String>> mapList);

    /**
     * In the background thread, it will directly write each JSON entry into an ArrayMap entry, for each
     * object in the JSONArray.
     * @param params Nothing.
     * @return List of parsed array map, or null on error.
     */
    @Override
    protected List<ArrayMap<String, String>> doInBackground(Void... params) {
        List<ArrayMap<String, String>> mapList = new ArrayList<>();
        try {
            JsonReader reader = new JsonReader(new FileReader(file));
            reader.beginArray();
            while (reader.peek() == JsonToken.BEGIN_OBJECT) {
                ArrayMap<String, String> map = new ArrayMap<>();
                reader.beginObject();
                while (reader.hasNext()) {
                    map.put(reader.nextName(), reader.nextString());
                }
                reader.endObject();
                mapList.add(map);
            }
            reader.endArray();
            reader.close();
        } catch (IOException e) {
            RxBus.publish("Error while parsing JSON file:\n\"" + e.getMessage() + "\"");
            Log.e(getClass().getSimpleName(), e.getMessage());
            return null;
        }
        return mapList;
    }
}
