package com.theif519.sakoverlay.Animations;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by theif519 on 12/21/2015.
 */
public class MoveAndResizeAnimation extends Animation {
    private int mWidth, mHeight;
    private int mStartWidth, mStartHeight;
    private float mX, mY;
    private float mStartX, mStartY;
    private View mView;

    public MoveAndResizeAnimation(View view, float x, float y, int width, int height) {
        mView = view;
        mX = x;
        mY = y;
        mWidth = width;
        mHeight = height;
        mStartX = view.getX();
        mStartY = view.getY();
        mStartWidth = view.getWidth();
        mStartHeight = view.getHeight();
    }

    public MoveAndResizeAnimation(View view, int[] properties){
        mView = view;
        mX = properties[0];
        mY = properties[1];
        mWidth = properties[2];
        mHeight = properties[3];
        mStartX = view.getX();
        mStartY = view.getY();
        mStartWidth = view.getWidth();
        mStartHeight = view.getHeight();
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        int newWidth = mStartWidth + (int) ((mWidth - mStartWidth) * interpolatedTime);
        int newHeight = mStartHeight + (int) ((mHeight - mStartHeight) * interpolatedTime);
        float newX = mStartX + (int) ((mX - mStartX) * interpolatedTime);
        float newY = mStartY + (int) ((mY - mStartY) * interpolatedTime);
        mView.setX(newX);
        mView.setY(newY);
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
