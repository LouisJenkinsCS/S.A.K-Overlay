package com.theif519.sakoverlay.Sessions;

import android.content.Context;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.theif519.sakoverlay.Fragments.Floating.FloatingFragment;
import com.theif519.sakoverlay.Fragments.Floating.FloatingFragmentFactory;

import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by theif519 on 12/10/2015.
 */
public class SessionManager {

    private static final SessionManager INSTANCE = new SessionManager();

    private SessionDatabase mDatabase;
    private static final PublishSubject<FloatingFragment> mPublishUpdate = PublishSubject.create();

    public static SessionManager getInstance(){
        return INSTANCE;
    }

    private SessionManager(){

    }

    private void setup(){
        mPublishUpdate
                .asObservable()
                .observeOn(Schedulers.io())
                .throttleLast(1, TimeUnit.SECONDS)
                .filter(f -> f != null)
                .map(FloatingFragment::serialize)
                .subscribe(mDatabase::update);
    }

    public Observable<List<FloatingFragment>> restoreSession(Context context) {
        if(mDatabase == null) {
            mDatabase = new SessionDatabase(context);
            mPublishUpdate
                    .asObservable()
                    .observeOn(Schedulers.io())
                    .subscribeOn(Schedulers.io())
                    .throttleLast(1, TimeUnit.SECONDS)
                    .filter(f -> f != null)
                    .map(FloatingFragment::serialize)
                    .subscribe(mDatabase::update);
        }
        return Observable.from(new FutureTask<>(
                () -> Stream.of(mDatabase.readAll())
                        .map(FloatingFragmentFactory::getFragment)
                        .filter(f -> f != null)
                        .collect(Collectors.toList())
        ), Schedulers.io());
    }

    public void updateSession(FloatingFragment fragment){
        mPublishUpdate.onNext(fragment);
    }

}
