package com.theif519.sakoverlay.Core.Animations;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by theif519 on 12/21/2015.
 */
public class MoveAnimation extends Animation{

    private float mX, mY;
    private float mStartX, mStartY;
    private View mView;

    public MoveAnimation(View view, float x, float y) {
        mView = view;
        mX = x;
        mY = y;
        mStartX = view.getX();
        mStartY = view.getY();
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        float newX = mStartX + (int) ((mX - mStartX) * interpolatedTime);
        float newY = mStartY + (int) ((mY - mStartY) * interpolatedTime);
        mView.setX(newX);
        mView.setY(newY);
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
