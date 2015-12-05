package com.theif519.sakoverlay.POD;

import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by theif519 on 12/4/2015.
 */
public class ViewProperties {
    private int x, y, width, height;
    private View v;

    public ViewProperties(View v) {
        this.v = v;
    }

    public ViewProperties update() {
        v.setX(x);
        v.setY(y);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) v.getLayoutParams();
        params.width = width;
        params.height = height;
        v.setLayoutParams(params);
        v.bringToFront();
        return this;
    }

    public int getX() {
        return x;
    }

    public ViewProperties setX(int x) {
        this.x = x;
        v.setX(x);
        return this;
    }

    public int getY() {
        return y;
    }

    public ViewProperties setY(int y) {
        if (this.y == y) return this;
        this.y = y;
        v.setY(y);
        return this;
    }

    public int getWidth() {
        return width;
    }

    public ViewProperties setWidth(int width) {
        if (this.width == width) return this;
        this.width = width;
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) v.getLayoutParams();
        params.width = width;
        v.setLayoutParams(params);
        return this;
    }

    public int getHeight() {
        return height;
    }

    public ViewProperties setCoordinates(int x, int y){
        v.setX(x);
        v.setY(y);
        return this;
    }

    public ViewProperties setSize(int width, int height){
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) v.getLayoutParams();
        params.width = width;
        params.height = height;
        v.setLayoutParams(params);
        return this;
    }

    public ViewProperties setHeight(int height) {
        if (this.height == height) return this;
        this.height = height;
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) v.getLayoutParams();
        params.height = height;
        v.setLayoutParams(params);
        return this;
    }
}
