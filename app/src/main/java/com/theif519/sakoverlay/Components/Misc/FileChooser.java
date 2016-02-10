package com.theif519.sakoverlay.Components.Misc;

import android.content.Context;
import android.widget.ListView;

import com.theif519.sakoverlay.Core.Rx.Transformers;

import java.io.File;
import java.lang.ref.WeakReference;

import rx.Observable;

/**
 * Created by theif519 on 1/20/2016.
 */
public class FileChooser {
    private WeakReference<Context> mContext;
    private ListView mListView;

    public FileChooser(Context context) {
        mContext = new WeakReference<>(context);
        mListView = new ListView(context);
    }

    private Observable<FileWrapper> getAllFilesInDir(File dir, String regExp){
        return Observable.from(dir.listFiles())
                .map(FileWrapperFactory::create)
                .filter(wrapper -> wrapper != null && wrapper.matches(regExp))
                .compose(Transformers.sorted())
                .compose(Transformers.asyncResult());
    }

    public Context getContext(){
        Context context = mContext.get();
        if(context == null){
            throw new RuntimeException("Memory Leak Avoided! Context returned null in FileChooser!");
        }
        return context;
    }
}
