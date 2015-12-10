package com.theif519.sakoverlay.Sessions;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import com.annimon.stream.Stream;
import com.google.gson.Gson;
import com.theif519.sakoverlay.Fragments.Floating.FloatingFragmentFactory;
import com.theif519.sakoverlay.Rx.Events.WidgetDeserializeAllEvent;
import com.theif519.sakoverlay.Rx.Events.WidgetFinishedDeserializing;
import com.theif519.sakoverlay.Rx.Events.WidgetUpdateEvent;
import com.theif519.sakoverlay.Rx.RxBus;

import java.util.List;

/**
 * Created by theif519 on 12/10/2015.
 */
public class SessionManager {

    private HandlerThread mWorker;
    private Handler mUIHandle, mWorkerHandler;
    private SessionDatabase mDatabase;
    // As handlers are sequential we can keep reusing it over and over without thread safety issues.
    private WidgetSessionData mCurrentWidgetData;

    public SessionManager(Context context) {
        mWorker = new HandlerThread("Session Manager", android.os.Process.THREAD_PRIORITY_BACKGROUND);
        mWorker.start();
        mWorkerHandler = new Handler(mWorker.getLooper());
        mUIHandle = new Handler(Looper.getMainLooper());
        mDatabase = new SessionDatabase(context);
        mCurrentWidgetData = new WidgetSessionData(-1, null, null);
    }

    private void setupEventHandling() {
        RxBus.subscribe(WidgetUpdateEvent.class)
                .map(WidgetUpdateEvent::getFragment)
                .subscribe(fragment -> {
                    mWorkerHandler.post(() -> {
                        String json = new Gson().toJson(fragment);
                        Log.i(getClass().getName(), json);
                        mDatabase.update(mCurrentWidgetData
                                        .setId(fragment.getUniqueId())
                                        .setTag(fragment.getLayoutTag())
                                        .setData(json.getBytes())
                        );
                    });
                });
        RxBus.subscribe(WidgetDeserializeAllEvent.class)
                .subscribe(ev -> mWorkerHandler.post(() -> {
                            List<WidgetSessionData> list = mDatabase.readAll();
                            if(list == null) {
                                RxBus.publish("Created new session...");
                                return;
                            }
                            RxBus.publish("Restoring session...");
                            Stream.of(list)
                                    .map(FloatingFragmentFactory::getFragment)
                                    .filter(fragment -> fragment != null)
                                    .map(WidgetFinishedDeserializing::new)
                                    .forEach(RxBus::publish);
                        })
                );
    }

}
