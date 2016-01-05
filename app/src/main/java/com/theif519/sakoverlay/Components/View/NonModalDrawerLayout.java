package com.theif519.sakoverlay.Components.View;

import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by theif519 on 1/3/2016.
 */
public class NonModalDrawerLayout extends DrawerLayout {

    private View mModalView;

    public NonModalDrawerLayout(Context context) {
        super(context);
    }

    public NonModalDrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NonModalDrawerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setModalView(View v){
        mModalView = v;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(mModalView == null || !isDrawerOpen(mModalView)) return super.onInterceptTouchEvent(ev);
        return false;
    }
}
