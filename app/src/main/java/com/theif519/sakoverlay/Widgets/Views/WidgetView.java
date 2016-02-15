package com.theif519.sakoverlay.Widgets.Views;

import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.theif519.sakoverlay.R;

import rx.subjects.BehaviorSubject;

/**
 * Created by theif519 on 2/5/2016.
 *
 * WidgetView is to be the general-use and reusable dynamic moving and resizng view. As these two
 * features are desirable in a WindowManager, it is best to abstract all movement and resizing here.
 * Doing so also allows us to inherit from this class, to allow for more specific actions,
 * while keeping the original functionality intact.
 */
public class WidgetView extends RelativeLayout {

    // TODO: Configure
    private static final float MIN_WIDTH = 0;

    // Temporary variables for onMoveEvent
    private float mMoveXOffset, mMoveYOffset;

    // Actual attributes for this View
    int mX, mY, mWidth, mHeight;

    private ViewGroup mContainer;

    /*
        In particular, this is used to allow the parent Widget to know when a MotionEvent has
        been handled without it knowing, so it can serialize this along with everything else.
     */
    private BehaviorSubject<MotionEvent> mOnMotionEventHandled = BehaviorSubject.create();

    public WidgetView(View v) {
        super(v.getContext());
        LayoutInflater.from(getContext()).inflate(R.layout.widget_view, this);
        mContainer = (ViewGroup) findViewById(R.id.widget_view_container);
        mContainer.addView(v);
    }

    // TODO: Move onMove and onResize here.

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // If the title bar is touched at all, we need to intercept.
        Rect hitRect = new Rect();
        findViewById(R.id.widget_view_title_bar).getHitRect(hitRect);
        for(int i = 0; i < ev.getPointerCount(); i++) {
            if (hitRect.contains((int) ev.getX(0), (int) ev.getY(0))) {
                return true;
            }
        }
        /*
            Here we can implement a way to determine if we should resize based on if the user has
            touched a portion of the TitleBar, and anything else. This way there's no need to worry
            about the extra touches getting through to the child view since we can just return
            true to consume the event.

            TODO: Do the above
         */
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean result = ev.getPointerCount() == 1 ? onMoveEvent(ev) : onResizeEvent(ev);
        if(ev.getAction() == MotionEvent.ACTION_UP){
            mOnMotionEventHandled.onNext(ev);
        }
        return result;
    }

    private boolean onMoveEvent(MotionEvent ev){
        switch(ev.getAction()){
            /*
                When the user first selects the Title Bar, we immediately obtain the touch offset
                so as to be able to move the view with the user, where they originally touched.
             */
            case MotionEvent.ACTION_DOWN:
                mMoveXOffset = (int) ev.getRawX() - mX;
                mMoveYOffset = (int) ev.getRawY() - mY;
                break;
            case MotionEvent.ACTION_MOVE:
                setX(ev.getRawX() - mMoveXOffset);
                setY(ev.getRawY() - mMoveYOffset);
                break;
            case MotionEvent.ACTION_UP:
                checkViewBounds();
                break;
            default:
                return false;
        }
        return true;
    }

    private boolean onResizeEvent(MotionEvent ev){
        return false;
        // TODO: Move new origin to closest pointer, remember that pointer and use it for further calculations
        // TODO: Change size as the distance between both pointers, no less than MIN_X and MIN_Y
    }

    private void checkViewBounds(){

    }
}
