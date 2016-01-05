package com.theif519.sakoverlay.Core.Rx;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by theif519 on 12/15/2015.
 */
public class Transformers {
    public static <T> Observable.Transformer<T, T> asyncResult(){
        return observable -> observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static <T> Observable.Transformer<T, T> backgroundIO(){
        return observable -> observable
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io());
    }
}
