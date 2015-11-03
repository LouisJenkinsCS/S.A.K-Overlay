package com.theif519.utils;

import android.os.AsyncTask;
import android.util.ArrayMap;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by theif519 on 10/31/2015.
 *
 * A simple, yet very powerful utilities which converts serialized key-value pairs into ArrayMaps.
 * JSONObjects get turned into ArrayMaps, and the root, JSONArray, is the actual mapList.
 */
abstract public class JSONDeserializer extends AsyncTask<Void, Void, List<ArrayMap<String, String>>> {

    protected File file;

    /**
     * Should be used initialize the file used to read from.
     */
    @Override
    abstract protected void onPreExecute();

    /**
     * Where the implementer handles the returned list of maps.
     * Note: It can return null on an IOException, and should be handled appropriately.
     * @param mapList Parsed list of maps.
     */
    @Override
    abstract protected void onPostExecute(List<ArrayMap<String, String>> mapList);

    @Override
    protected List<ArrayMap<String, String>> doInBackground(Void... params) {
        List<ArrayMap<String, String>> mapList = new ArrayList<>();
        try {
            JsonReader reader = new JsonReader(new FileReader(file));
            reader.beginArray();
            while(reader.peek() == JsonToken.BEGIN_OBJECT){
                ArrayMap<String, String> map = new ArrayMap<>();
                reader.beginObject();
                while(reader.hasNext()){
                    map.put(reader.nextName(), reader.nextString());
                }
                reader.endObject();
                mapList.add(map);
            }
            reader.endArray();
            reader.close();
        } catch (IOException e) {
            Log.e(getClass().getSimpleName(), e.getMessage());
            return null;
        }
        return mapList;
    }
}
