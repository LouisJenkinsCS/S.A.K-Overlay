package com.theif519.sakoverlay.ReactiveX.Observers;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.jakewharton.rxbinding.view.RxView;
import com.theif519.sakoverlay.Misc.Globals;
import com.theif519.utils.Misc.MeasureTools;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by theif519 on 11/23/2015.
 */
public class ViewOutOfBounds {

    private View mView;

    public ViewOutOfBounds(final View mView) {
        Log.i(getClass().getName(), "Inside of ViewOutOfBounds constructor");
        this.mView = mView;
        Observable.just(
                RxView.touches(mView)
                        .filter(new Func1<MotionEvent, Boolean>() {
                            @Override
                            public Boolean call(MotionEvent event) {
                                return event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_POINTER_UP;
                            }
                        }),
                RxView.globalLayouts(mView)
        ).subscribe(new Action1<Object>() {
            @Override
            public void call(Object ignore) {
                Log.i(getClass().getName(), "View Boundary: (" + mView.getX() + ", " + mView.getY() + ")");
                boundsCheck();
            }
        });
    }

    private void boundsCheck() {
        if (mView.getX() < 0) {
            mView.setX(0);
        }
        if (mView.getY() < 0) {
            mView.setY(0);
        }
        if (mView.getX() + MeasureTools.scaleToInt(mView.getWidth(), Globals.SCALE_X.get()) > Globals.MAX_X.get()) {
            mView.setX(Globals.MAX_X.get() - mView.getWidth());
        }
        if (mView.getY() + MeasureTools.scaleToInt(mView.getHeight(), Globals.SCALE_Y.get()) > Globals.MAX_Y.get()) {
            mView.setY(Globals.MAX_Y.get() - mView.getHeight());
        }
        if (MeasureTools.scaleToInt(mView.getWidth(), Globals.SCALE_X.get()) > Globals.MAX_X.get()) {
            mView.setLayoutParams(new LinearLayout.LayoutParams(Globals.MAX_X.get(), mView.getHeight()));
        }
        if (MeasureTools.scaleToInt(mView.getHeight(), Globals.SCALE_Y.get()) > Globals.MAX_Y.get()) {
            mView.setLayoutParams(new LinearLayout.LayoutParams(mView.getWidth(), Globals.MAX_Y.get()));
        }
    }
}
