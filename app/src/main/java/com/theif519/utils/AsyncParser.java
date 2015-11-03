package com.theif519.utils;

import android.os.AsyncTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by theif519 on 10/12/2015.
 * <p/>
 * Author: Louis Jenkins
 * <p/>
 * AsyncParser in essence is what it's name suggests, an asynchronous parser. It is not a JSON parser,
 * it is meant for CSV/Comma-Delimited files. By using generics it allows it to ensure type-safety
 * while also by utilizing callback, is general-use enough to be utilized on any and all factories
 * that implement the Parsable interface.
 * <p/>
 * It does not use reflection, hence it is fast and is completely type-safe. It is also presumably thread-safe,
 * although any accesses to variables should be synchronized to be sure.
 */
public class AsyncParser<T> extends AsyncTask<Parsable<T>, T, List<T>> {

    public interface Callback<T> {
        void onDone(List<T> parsedObjects);

        void onUpdate(T newObjects);
    }

    private Callback<T> mCallback;
    private Scanner mParser;
    private String mDelimiter;

    public AsyncParser(File file, String delimiter, Callback<T> callback) throws FileNotFoundException {
        mCallback = callback;
        mParser = new Scanner(file);
        mDelimiter = delimiter;
    }

    // TODO: Have to find a way to work around the issue with varargs and generics
    @SuppressWarnings("unchecked")
    @Override
    protected List<T> doInBackground(Parsable<T>[] parsables) {
        List<T> list = new ArrayList<>();
        Parsable<T> parsable = parsables[0];
        while (true) {
            if (!mParser.hasNext()) break;
            String line = mParser.nextLine();
            T obj = parsable.parseObject(line, mDelimiter);
            if (obj == null) continue;
            list.add(obj);
            publishProgress(obj);
        }
        mParser.close();
        return list;
    }

    @Override
    protected void onPostExecute(List<T> ts) {
        // Works fine.
        mCallback.onDone(ts);
    }

    @Override
    protected void onProgressUpdate(T[] values) {
        // Possible heap corruption, according to warning. Why?
        mCallback.onUpdate(values[0]);
    }
}
