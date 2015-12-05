package com.theif519.sakoverlay.POD;

import android.os.Looper;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by theif519 on 12/4/2015.
 */
public class ViewProperties {
    private int x, y, width, height;
    private View v;
    private boolean shouldUpdate;

    public ViewProperties(View v) {
        this.v = v;
    }

    public ViewProperties update() {
        if (shouldUpdate && Looper.myLooper() == Looper.getMainLooper()) {
            v.setX(x);
            v.setY(y);
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) v.getLayoutParams();
            params.width = width;
            params.height = height;
            v.setLayoutParams(params);
            shouldUpdate = false;
        }
        return this;
    }

    public int getX() {
        return x;
    }

    public ViewProperties setX(int x) {
        if (this.x == x) return this;
        this.x = x;
        if (Looper.myLooper() == Looper.getMainLooper()) {
            v.setX(x);
        } else {
            shouldUpdate = true;
        }
        return this;
    }

    public int getY() {
        return y;
    }

    public ViewProperties setY(int y) {
        if (this.y == y) return this;
        this.y = y;
        if (Looper.myLooper() == Looper.getMainLooper()) {
            v.setY(y);
        } else {
            shouldUpdate = true;
        }
        return this;
    }

    public int getWidth() {
        return width;
    }

    public ViewProperties setWidth(int width) {
        if (this.width == width) return this;
        this.width = width;
        if (Looper.myLooper() == Looper.getMainLooper()) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) v.getLayoutParams();
            params.width = width;
            v.setLayoutParams(params);
        } else {
            shouldUpdate = true;
        }
        return this;
    }

    public int getHeight() {
        return height;
    }

    /**
     * Marks this instance to be updated on the UI thread when possible.
     * @return This.
     */
    public ViewProperties markUpdate(){
        shouldUpdate = true;
        return this;
    }

    public ViewProperties setHeight(int height) {
        if (this.height == height) return this;
        this.height = height;
        if (Looper.myLooper() == Looper.getMainLooper()) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) v.getLayoutParams();
            params.height = height;
            v.setLayoutParams(params);
        } else {
            shouldUpdate = true;
        }
        return this;
    }
}
