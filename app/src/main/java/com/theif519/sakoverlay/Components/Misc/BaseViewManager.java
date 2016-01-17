package com.theif519.sakoverlay.Components.Misc;

import android.support.annotation.NonNull;
import android.view.View;

import com.annimon.stream.Optional;

import rx.Observable;

/**
 * Created by theif519 on 1/17/2016.
 */
public abstract class BaseViewManager {

    public static BaseViewManager plain(View v) {
        return new BaseViewManager(v) {
            @Override
            public Optional<String> validate() {
                return Optional.empty();
            }

            @Override
            public void handle() {

            }

            @Override
            public void reset() {

            }

            @NonNull
            @Override
            public Observable<Void> observeStateChanges() {
                return Observable.never();
            }
        };
    }

    private View mView;

    public BaseViewManager(View v) {
    mView = v;
    }

    public View getView(){
        return mView;
    }

    public abstract Optional<String> validate();

    public abstract void handle();

    public abstract void reset();

    @NonNull
    public abstract Observable<Void> observeStateChanges();
}
