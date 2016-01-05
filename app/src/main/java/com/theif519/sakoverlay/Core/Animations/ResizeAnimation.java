package com.theif519.sakoverlay.Core.Animations;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by theif519 on 12/21/2015.
 */
public class ResizeAnimation extends Animation {

    private int mWidth, mHeight;
    private int mStartWidth, mStartHeight;
    private View mView;

    public ResizeAnimation(View view, int width, int height) {
        mView = view;
        mWidth = width;
        mHeight = height;
        mStartWidth = view.getWidth();
        mStartHeight = view.getHeight();
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        int newWidth = mStartWidth + (int) ((mWidth - mStartWidth) * interpolatedTime);
        int newHeight = mStartHeight + (int) ((mHeight - mStartHeight) * interpolatedTime);
        mView.getLayoutParams().width = newWidth;
        mView.getLayoutParams().height = newHeight;
        mView.requestLayout();
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
    }

    @Override
    public boolean willChangeBounds() {
        return true;
    }
}
