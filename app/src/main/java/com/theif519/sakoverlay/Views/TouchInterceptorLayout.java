package com.theif519.sakoverlay.Views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import com.theif519.sakoverlay.Rx.RxBus;
import com.theif519.utils.Misc.AttributeRetriever;

/**
 * Created by theif519 on 12/5/2015.
 */
public class TouchInterceptorLayout extends LinearLayout {

    private String mIdentifier;

    public TouchInterceptorLayout(Context context) {
        super(context);
    }

    public TouchInterceptorLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        AttributeRetriever.fillAttributes(getClass(), this, context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(ev.getAction() == MotionEvent.ACTION_DOWN){
            if(mIdentifier != null) RxBus.publish(mIdentifier);
        }
        return false;
    }

    @AttributeRetriever.AttributeHelper(source = "tilIdentifier")
    public void setIdentifier(String identifier){
        mIdentifier = identifier;
    }

    public String getIdentifier(){
        return mIdentifier;
    }
}
