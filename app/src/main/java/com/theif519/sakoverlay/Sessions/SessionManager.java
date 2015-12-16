package com.theif519.sakoverlay.Sessions;

import android.content.Context;
import android.util.Log;

import com.annimon.stream.Stream;
import com.theif519.sakoverlay.Fragments.Floating.FloatingFragment;
import com.theif519.sakoverlay.Fragments.Floating.FloatingFragmentFactory;
import com.theif519.sakoverlay.Rx.RxBus;
import com.theif519.sakoverlay.Rx.Transformers;

import org.json.JSONObject;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.subjects.PublishSubject;

/**
 * Created by theif519 on 12/10/2015.
 */
public class SessionManager {

    private static final SessionManager INSTANCE = new SessionManager();

    private SessionDatabase mDatabase;
    private static final PublishSubject<FloatingFragment> mPublishUpdate = PublishSubject.create();
    private static final PublishSubject<FloatingFragment> mPublishDelete = PublishSubject.create();

    private static int mWidgetSetupNum = 0;

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    private SessionManager() {

    }

    /**
     * Appends the widget to the database, which asynchronously returns the ID associated with it
     * in an observable.
     *
     * @param fragment Widget to append
     * @return Observable which emits the unique id for it.
     */
    public Observable<Long> appendSession(FloatingFragment fragment) {
        Log.i(getClass().getName(), "Appending a new fragment to session data!");
        return Observable
                .create(new Observable.OnSubscribe<Long>() {
                    @Override
                    public void call(Subscriber<? super Long> subscriber) {
                        subscriber.onNext(mDatabase.insert(new WidgetSessionData(-1, fragment.getLayoutTag(), JSONObject.quote("{}").getBytes())));
                        subscriber.onCompleted();
                    }
                })
                .compose(Transformers.asyncResult());
    }

    private void setup(Context context) {
        mDatabase = new SessionDatabase(context);
        mPublishUpdate
                .asObservable()
                .compose(Transformers.backgroundIO())
                .map(WidgetSessionData::new)
                .subscribe(mDatabase::update);
        mPublishDelete
                .asObservable()
                .compose(Transformers.backgroundIO())
                .map(FloatingFragment::getUniqueId)
                .subscribe(mDatabase::delete);
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
                            mWidgetSetupNum = dataList.size();
                            Stream
                                    .of(dataList)
                                    .map(FloatingFragmentFactory::getFragment)
                                    .filter(f -> f != null)
                                    .forEach(subscriber::onNext);
                            RxBus.publish("Restoring session...");
                        } else RxBus.publish("Created new session!");
                        subscriber.onCompleted();
                    }
                })
                .compose(Transformers.asyncResult());
    }

    public void updateSession(FloatingFragment fragment) {
        Log.i(getClass().getName(), "Updating fragment!");
        mPublishUpdate.onNext(fragment);
    }

    public void deleteSession(FloatingFragment fragment) {
        Log.i(getClass().getName(), "Deleting fragment!");
        mPublishDelete.onNext(fragment);
    }

    public void finishedSetup(){
        if(--mWidgetSetupNum == 0){
            RxBus.publish("Restored Session!");
        }
    }

}
