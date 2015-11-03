package com.theif519.utils;

import android.view.View;
import android.view.ViewGroup;

/**
 * Created by theif519 on 10/12/2015.
 * <p/>
 * Author: Louis Jenkins
 * <p/>
 * This interface acts as a sort of C++-like namespace for generic callbacks which I most likely will
 * use in the future. They can be accessed
 */
public final class Callbacks {
    public interface NewInstanceCallback<T> {
        T newInstance(Object... params);
    }

    public interface GenericVarArgsWorkCallback<T> {
        void doWork(T... params);
    }

    public interface GenericWorkCallback<T> {
        void doWork(T param);
    }

    public interface NoArgsCallback {
        void doWork();
    }

    public static abstract class CallbackOnRootChildren<T> {

        // TODO: Have to figure out a way to force the interpreter to acknowledge that it's a type safe conversion.
        @SuppressWarnings("unchecked")
        public void callOnChildren(Class<T> clazz, ViewGroup root) {
            for (int i = 0; i < root.getChildCount(); i++) {
                View v = root.getChildAt(i);
                if (v instanceof ViewGroup) {
                    callOnChildren(clazz, (ViewGroup) v);
                }
                if (clazz.isAssignableFrom(v.getClass())) {
                    // The check to see if it is assignable ensures it's type safe.
                    onChild((T) v);
                }
            }
        }

        public abstract void onChild(T child);
    }
}
