package com.theif519.sakoverlay.Rx;

import rx.Observable;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * Created by theif519 on 11/25/2015.
 *
 * A simple EventBus implementation.
 */
public class RxBus {
    private static final Subject<Object, Object> INSTANCE = new SerializedSubject<>(PublishSubject.create());

    /**
     * Publish an event to all listening subscribers.
     * @param event Event to publish.
     */
    public static void publish(Object event){
        INSTANCE.onNext(event);
    }

    /**
     * Returns a filtered observable for a specified published event.
     * @param eventType Specified class of event to filter.
     * @param <T> Type of event.
     * @return Observable the caller can manipulate and subscribe to.
     */
    public static <T> Observable<T> subscribe(final Class<T> eventType){
        return INSTANCE.filter(new Func1<Object, Boolean>() {
            @Override
            public Boolean call(Object o) {
                return eventType.isInstance(o);
            }
        }).cast(eventType);
    }
}
