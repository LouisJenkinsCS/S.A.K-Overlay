package com.theif519.utils.Misc;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by theif519 on 12/20/2015.
 *
 * From: http://jasonmcreynolds.com/?p=388
 */
public class ShakeDetector implements SensorEventListener{

    public interface OnShakeListener {
        void onShake(int count);
    }

    private static final float SHAKE_THRESHOLD_GRAVITY = 1.5f;
    private static final int SHAKE_SLOP_TIME_MS = 166;
    private static final int SHAKE_COUNT_RESET_TIME_MS = 500;

    private OnShakeListener mListener;
    private long mShakeTimestamp;
    private int mShakeCount;

    public void setOnShakeListener(OnShakeListener listener){
        mListener = listener;
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if(mListener != null){
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            float gX = x / SensorManager.GRAVITY_EARTH;
            float gY = y / SensorManager.GRAVITY_EARTH;
            float gZ = z / SensorManager.GRAVITY_EARTH;
            // Will be close to 1 when there is no movement.
            float gForce = (float) Math.sqrt(gX * gX + gY * gY + gZ * gZ);
            if(gForce > SHAKE_THRESHOLD_GRAVITY){
                final long now = System.currentTimeMillis();
                // Only handle this event once every half second.
                if(mShakeTimestamp + SHAKE_SLOP_TIME_MS > now){
                    return;
                }
                // reset the shake count after 3 seconds of no shakes
                if(mShakeTimestamp + SHAKE_COUNT_RESET_TIME_MS < now){
                    mShakeCount = 0;
                }
                mShakeTimestamp = now;
                mShakeCount++;
                mListener.onShake(mShakeCount);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
