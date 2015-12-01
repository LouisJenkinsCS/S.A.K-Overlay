package com.theif519.sakoverlay.POD;

/**
 * Created by theif519 on 11/25/2015.
 */
public class TouchEventInfo {
    private int mX, mY, mMask;

    public static final int RIGHT = 1;

    public static final int LEFT = 1 << 1;

    public static final int UPPER = 1 << 2;

    public static final int BOTTOM = 1 << 3;

    public TouchEventInfo(int x, int y, int snapMask) {
        this.mX = x;
        this.mY = y;
        this.mMask = snapMask;
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

    public int getMask() {
        return mMask;
    }

    public void setMask(int mask) {
        mMask = mask;
    }
}
