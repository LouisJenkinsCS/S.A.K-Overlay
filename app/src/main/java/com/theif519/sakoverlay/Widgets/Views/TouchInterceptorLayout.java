package com.theif519.sakoverlay.Widgets.Views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import com.theif519.utils.Misc.AttributeRetriever;

/**
 * Created by theif519 on 12/5/2015.
 * <p/>
 * This is the proof-of-concept for the upcoming Mac OS X style menus. Right now, whenever a ACTION_DOWN
 * event occurs in any of this layout's children, it will broadcast the name of layout, which in this case,
 * is the name of the BaseWidget.
 * <p/>
 * So imagine instead of a string, instead it sends it's own custom Menu hierarchy. This would allow me to
 * just broadcast it (Imagine how impossible this would be with a normal BroadcastReceiver, or how rather how tightly
 * coupled everything would be. I would have to keep a weak reference to the activity and it's call it's method
 * to set the current menu, OR I would have to somehow serialize the entire view (FYI, View
 * does not implement Parcelable, and that's MUCH slower than just sending the object through RxBus)
 * and have it set it that way, etc.) and allow whatever activity/fragment/class to handle removing
 * the old view and adding the new one, etc.
 */
public class TouchInterceptorLayout extends LinearLayout {

    private String mIdentifier;
    private Runnable mCallback;

    public TouchInterceptorLayout(Context context) {
        super(context);
    }

    public TouchInterceptorLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        AttributeRetriever.fillAttributes(getClass(), this, context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (mCallback != null) mCallback.run();
            bringToFront();
        }
        return false;
    }

    public void setCallback(Runnable callback){
        mCallback = callback;
    }

    public String getIdentifier() {
        return mIdentifier;
    }

    @AttributeRetriever.AttributeHelper(source = "tilIdentifier")
    public void setIdentifier(String identifier) {
        mIdentifier = identifier;
    }
}
