package com.theif519.sakoverlay.Rx;

import rx.Observable;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * Created by theif519 on 11/25/2015.
 */
public class RxBus {
    private static final Subject<Object, Object> INSTANCE = new SerializedSubject<>(PublishSubject.create());

    public static void post(Object event){
        INSTANCE.onNext(event);
    }

    public static <T> Observable<T> await(final Class<T> eventType){
        return INSTANCE.filter(new Func1<Object, Boolean>() {
            @Override
            public Boolean call(Object o) {
                return eventType.isInstance(o);
            }
        }).cast(eventType);
    }
}
