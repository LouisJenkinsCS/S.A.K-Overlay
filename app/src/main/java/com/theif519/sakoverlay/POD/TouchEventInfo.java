package com.theif519.sakoverlay.POD;

/**
 * Created by theif519 on 11/25/2015.
 */
public class TouchEventInfo {
    private int mX, mY, mWidth, mHeight;
    private boolean mIsMultiTouch;

    public TouchEventInfo(int x, int y, int width, int height, boolean isMultiTouch) {
        this.mX = x;
        this.mY = y;
        this.mWidth = width;
        this.mHeight = height;
        this.mIsMultiTouch = isMultiTouch;
    }

    public int getX() {
        return mX;
    }

    public void setX(int x) {
        mX = x;
    }

    public int getY() {
        return mY;
    }

    public void setY(int y) {
        mY = y;
    }

    public int getWidth() {
        return mWidth;
    }

    public void setWidth(int width) {
        mWidth = width;
    }

    public int getHeight() {
        return mHeight;
    }

    public void setHeight(int height) {
        mHeight = height;
    }

    public boolean isMultiTouch() {
        return mIsMultiTouch;
    }

    public void setIsMultiTouch(boolean isMultiTouch) {
        mIsMultiTouch = isMultiTouch;
    }
}
