package com.theif519.sakoverlay.Components.Misc;

import android.util.Log;

import com.theif519.sakoverlay.Core.Rx.Transformers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import rx.Observable;

/**
 * Created by theif519 on 1/5/2016.
 */
public class MethodWrapper<T> {
    private T mInstance;
    private Method mMethod;
    private String mDescription;

    public MethodWrapper(T instance, Method method) {
        mMethod = method;
        mInstance = instance;
        Observable.<String>create(subscriber -> {
            StringBuilder methodStr = new StringBuilder();
            methodStr.append(mMethod.getReturnType().getSimpleName());
            methodStr.append(" ");
            methodStr.append(mMethod.getName());
            methodStr.append("(");
            boolean preceded = false;
            for (Class<?> params : mMethod.getParameterTypes()) {
                if (preceded) methodStr.append(", ");
                methodStr.append(params.getSimpleName());
                preceded = true;
            }
            methodStr.append(")");
            subscriber.onNext(methodStr.toString());
            subscriber.onCompleted();
        }).compose(Transformers.backgroundIO()).subscribe(msg -> {
            mDescription = msg;
            Log.i(getClass().getName(), "Parsed method: " + mDescription);
        });
    }

    public Object invoke(Object... params) {
        try {
            return mMethod.invoke(mInstance, params);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Error while attempting to invoke method: \"" + e.getMessage() + "\"");
        }
    }

    public Class<?>[] getParameterTypes() {
        return mMethod.getParameterTypes();
    }

    public Class<?> getReturnType() {
        return mMethod.getReturnType();
    }

    public String getDescription() {
        return mDescription;
    }

    public String getMethodName() {
        return mMethod.getName();
    }
}