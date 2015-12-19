package com.theif519.sakoverlay.Sessions;

import android.content.Context;
import android.util.Log;

import com.annimon.stream.Stream;
import com.theif519.sakoverlay.Fragments.Widgets.BaseWidget;
import com.theif519.sakoverlay.Fragments.Widgets.WidgetFactory;
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
    private static final PublishSubject<BaseWidget> mPublishUpdate = PublishSubject.create();
    private static final PublishSubject<BaseWidget> mPublishDelete = PublishSubject.create();

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    private SessionManager() {

    }

    /**
     * Appends the widget to the database, which asynchronously returns the ID associated with it
     * in an observable.
     *
     * @param widget Widget to append
     * @return Observable which emits the unique id for it.
     */
    public Observable<Long> appendSession(BaseWidget widget) {
        Log.i(getClass().getName(), "Appending a new widget to session data!");
        return Observable
                .create(new Observable.OnSubscribe<Long>() {
                    @Override
                    public void call(Subscriber<? super Long> subscriber) {
                        subscriber.onNext(mDatabase.insert(new WidgetSessionData(-1, widget.getLayoutTag(), JSONObject.quote("{}").getBytes())));
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
                .map(BaseWidget::getUniqueId)
                .subscribe(mDatabase::delete);
        Log.i(getClass().getName(), "Created Database and setup publish subscription!");
    }

    public Observable<BaseWidget> restoreSession(Context context) {
        if (mDatabase == null) {
            setup(context);
        }
        return Observable
                .create(new Observable.OnSubscribe<BaseWidget>() {
                    @Override
                    public void call(Subscriber<? super BaseWidget> subscriber) {
                        List<WidgetSessionData> dataList = mDatabase.readAll();
                        if (dataList != null) {
                            Stream
                                    .of(dataList)
                                    .map(WidgetFactory::getWidget)
                                    .filter(widget -> widget != null)
                                    .forEach(subscriber::onNext);
                        }
                        subscriber.onCompleted();
                    }
                })
                .compose(Transformers.asyncResult());
    }

    public void updateSession(BaseWidget widget) {
        Log.i(getClass().getName(), "Updating widget!");
        mPublishUpdate.onNext(widget);
    }

    public void deleteSession(BaseWidget widget) {
        Log.i(getClass().getName(), "Deleting widget!");
        mPublishDelete.onNext(widget);
    }

}
