package com.theif519.sakoverlay.Views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.theif519.sakoverlay.R;

/**
 * Created by theif519 on 12/27/2015.
 */
public class DynamicComponent extends FrameLayout {

    private Button mTopRightResize, mTopLeftResize, mBottomRightResize, mBottomLeftResize;

    public DynamicComponent(Context context) {
        this(context, null);
    }

    public DynamicComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.dynamic_component, this);
        mTopLeftResize = (Button) findViewById(R.id.top_left_resize_button);
        mTopRightResize = (Button) findViewById(R.id.top_right_resize_button);
        mBottomLeftResize = (Button) findViewById(R.id.bottom_left_resize_button);
        mBottomRightResize = (Button) findViewById(R.id.bottom_right_resize_button);
        mTopRightResize.setRotation(90);
        mBottomLeftResize.setRotation(90);
        mTopRightResize.setOnTouchListener(this::resize);
        mTopLeftResize.setOnTouchListener(this::resize);
        mBottomLeftResize.setOnTouchListener(this::resize);
        mBottomRightResize.setOnTouchListener(this::resize);
    }

    private float tmpX, tmpY;

    private boolean resize(View v, MotionEvent event) {
        ViewGroup parent = (ViewGroup) v.getParent();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                tmpX = parent.getX();
                tmpY = parent.getY();
                return false;
            case MotionEvent.ACTION_MOVE:
                parent.getLayoutParams().width = (int) Math.abs(event.getRawX() - tmpX);
                parent.getLayoutParams().height = (int) Math.abs(event.getRawY() - tmpY);
                parent.requestLayout();
                return false;
            case MotionEvent.ACTION_UP:
                return true;
            default:
                return false;
        }
    }
}
