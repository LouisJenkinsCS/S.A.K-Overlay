package com.theif519.sakoverlay.Sessions;

import android.content.Context;
import android.util.Log;

import com.annimon.stream.Stream;
import com.google.gson.GsonBuilder;
import com.theif519.sakoverlay.Fragments.Floating.FloatingFragment;
import com.theif519.sakoverlay.Fragments.Floating.FloatingFragmentFactory;
import com.theif519.sakoverlay.Rx.RxBus;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by theif519 on 12/10/2015.
 */
public class SessionManager {

    private static final SessionManager INSTANCE = new SessionManager();

    private SessionDatabase mDatabase;
    private static final PublishSubject<FloatingFragment> mPublishUpdate = PublishSubject.create();

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    private SessionManager() {

    }

    public Observable<Long> appendSession(FloatingFragment fragment) {
        Log.i(getClass().getName(), "Appending a new fragment to session data!");
        return Observable
                .create(new Observable.OnSubscribe<Long>() {
                    @Override
                    public void call(Subscriber<? super Long> subscriber) {
                        subscriber.onNext(mDatabase.insert(new WidgetSessionData(fragment)));
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private void setup(Context context) {
        mDatabase = new SessionDatabase(context);
        mPublishUpdate
                .asObservable()
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .map(fragment -> new WidgetSessionData(
                                fragment.getId(),
                                fragment.getTag(),
                                new GsonBuilder()
                                        .excludeFieldsWithoutExposeAnnotation()
                                        .create()
                                        .toJson(fragment)
                                        .getBytes()
                        )
                )
                .subscribe(mDatabase::update);
        Log.i(getClass().getName(), "Created Database and setup publish subscription!");
    }

    public Observable<FloatingFragment> restoreSession(Context context) {
        if (mDatabase == null) {
            setup(context);
        }
        return Observable
                .create(new Observable.OnSubscribe<FloatingFragment>() {
                    @Override
                    public void call(Subscriber<? super FloatingFragment> subscriber) {
                        List<WidgetSessionData> dataList = mDatabase.readAll();
                        if (dataList != null) {
                            Stream.of(dataList)
                                    .map(FloatingFragmentFactory::getFragment)
                                    .filter(f -> f != null)
                                    .forEach(subscriber::onNext);
                        } else RxBus.publish("Created new session!");
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public void updateSession(FloatingFragment fragment) {
        Log.i(getClass().getName(), "Updating fragment!");
        mPublishUpdate.onNext(fragment);
    }

}
