package com.theif519.sakoverlay.POD;

import android.view.View;
import android.widget.FrameLayout;

import com.theif519.sakoverlay.Misc.Globals;
import com.theif519.sakoverlay.Misc.MeasureTools;

/**
 * Created by theif519 on 12/4/2015.
 *
 * Encapsulates attributes and properties of a view. This class is used to keep track of not only the
 * view properties/state, but also set them directly. That, in conjunction with it's fluent interface/method chaining,
 * it allows for easy instantiation, easy updating, and easy serialization/deserialization.
 *
 * In the future, if I wish to have a background thread be able to also manipulate a view, I can make the changes
 * here without interrupting/needing to refactor other classes which uses this class.
 *
 * That could be accomplished by doing something along the lines of...
 *
 * private Handler mUiHandler = new Handler(); // Automatically gets looper of the current thread, in this case main looper.
 *
 * public ViewProperties update(){
 *     if(Looper.myLooper() != Looper.getMainLooper()){ // Note also we do not need to worry about thread safety
 *         mUiHandler.post(() -> update()); // Would result in the UI thread calling this method
 *         return; // Then we return as we do not want this thread to touch anything else.
 *     }
 *     // Otherwise if it is the main thread, do it here.
 * }
 *
 * The reasons for needing such checks, of course, is that I plan on making ViewProperties accessible outside
 * of this class. Meaning, it is a way for other classes and even threads to post updates to through this class.
 */
public class ViewProperties {
    private int x, y, width, height;
    private View v;

    public ViewProperties(View v) {
        this.v = v;
    }

    /**
     * This class is generally used to restore the view to a previous state. For instance, when snap() and
     * maximize() is called, these attributes are never updated, hence it is easiest to just call this method
     * to restore the previous view's state. It should also be noted that there are checks put in place,
     * which right now are very simplistic and minimal, to offer a means of bounds checking, in the case
     * that the user changes orientation.
     * @return This.
     */
    public ViewProperties update() {
        int scaleDiffX = MeasureTools.scaleDelta(width);
        int scaleDiffY = MeasureTools.scaleDelta(height);
        int minX = -scaleDiffX;
        int minY = -scaleDiffY;
        int maxX = MeasureTools.scaleInverse(Globals.MAX_X.get());
        int maxY = MeasureTools.scaleInverse(Globals.MAX_Y.get());
        x = x > maxX ? maxX : x < minX ? minX : x;
        y = y > maxY ? maxY : y < minY ? minY : y;
        width = width > maxX ? maxX : width < 250 ? 250 : width;
        height = height > maxY ? maxY : height < 250 ? 250 : height;
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
