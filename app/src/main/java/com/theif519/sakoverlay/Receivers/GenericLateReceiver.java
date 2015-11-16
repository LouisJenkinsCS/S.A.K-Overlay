package com.theif519.sakoverlay.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by theif519 on 11/15/2015.
 *
 * The name "Late" in GenericLateReceiver doesn't mean that it receives later, but rather the callback
 * can be declared late. While callbacks are being executed, it is within a critical section, hence
 * it is safe to change the lock later.
 */
public class GenericLateReceiver extends BroadcastReceiver {

    private final Object mLock = new Object();

    public interface OnReceiveCallback {
        void onReceive(Context context, Intent intent);
    }
    private OnReceiveCallback mCallback;

    public GenericLateReceiver(OnReceiveCallback callback) {
        mCallback = callback;
    }

    public void registerCallback(OnReceiveCallback callback){
        synchronized (mLock){
            mCallback = callback;
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        synchronized (mLock){
            if(mCallback != null) mCallback.onReceive(context, intent);
        }
    }
}
