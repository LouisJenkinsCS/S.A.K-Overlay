package com.theif519.sakoverlay.Widgets.Misc.Sessions;

import android.content.Context;
import android.util.Log;

import com.annimon.stream.Optional;
import com.theif519.sakoverlay.Core.Database.SessionDatabase;
import com.theif519.sakoverlay.Core.Rx.Transformers;
import com.theif519.sakoverlay.Widgets.BaseWidget;
import com.theif519.sakoverlay.Widgets.POJO.WidgetSessionData;
import com.theif519.sakoverlay.Widgets.WidgetFactory;

import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by theif519 on 12/10/2015.
 */
public class WidgetSessionManager {

    private static final WidgetSessionManager INSTANCE = new WidgetSessionManager();

    private SessionDatabase mDatabase;
    private static final PublishSubject<BaseWidget> mPublishUpdate = PublishSubject.create();
    private static final PublishSubject<BaseWidget> mPublishDelete = PublishSubject.create();

    public static WidgetSessionManager getInstance() {
        return INSTANCE;
    }

    private WidgetSessionManager() {

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
        return mDatabase.insert(widget.getLayoutTag())
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
        return mDatabase.readAll()
                .map(WidgetFactory::getWidget)
                .filter(Optional::isPresent)
                .map(Optional::get)
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
