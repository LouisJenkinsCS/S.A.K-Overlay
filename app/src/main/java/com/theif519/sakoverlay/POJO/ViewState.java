package com.theif519.sakoverlay.POJO;

import com.theif519.sakoverlay.Misc.Globals;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by theif519 on 12/4/2015.
 * <p>
 * Encapsulates attributes and properties of a view. This class is used to keep track of not only the
 * view properties/state, but also set them directly. That, in conjunction with it's fluent interface/method chaining,
 * it allows for easy instantiation, easy updating, and easy serialization/deserialization.
 * <p>
 * In the future, if I wish to have a background thread be able to also manipulate a view, I can make the changes
 * here without interrupting/needing to refactor other classes which uses this class.
 * <p>
 * That could be accomplished by doing something along the lines of...
 * <p>
 * <code> <pre>
 * private Handler mUiHandler = new Handler(); // Automatically gets looper of the current thread, in this case main looper.
 * <p>
 * public ViewState update(){
 *      if(Looper.myLooper() != Looper.getMainLooper()){ // Note also we do not need to worry about thread safety
 *          mUiHandler.post(() -> update()); // Would result in the UI thread calling this method
 *          return; // Then we return as we do not want this thread to touch anything else.
 *      }
 *      // Otherwise if it is the main thread, do it here.
 * }
 * </pre> </code>
 * <p>
 * The reasons for needing such checks, of course, is that I plan on making ViewState accessible outside
 * of this class. Meaning, it is a way for other classes and even threads to post updates to through this class.
 */
public class ViewState {

    public static final int RIGHT = 1, LEFT = 1 << 1, UPPER = 1 << 2, BOTTOM = 1 << 3,
            MINIMIZED = 1 << 4, MAXIMIZED = 1 << 5;
    private int x, y, width, height, mask;

    public ViewState() {

    }

    public ViewState(JSONObject data) throws JSONException {
        x = data.getInt(Globals.Keys.X);
        y = data.getInt(Globals.Keys.Y);
        width = data.getInt(Globals.Keys.WIDTH);
        height = data.getInt(Globals.Keys.HEIGHT);
        mask = data.getInt(Globals.Keys.STATE);
    }

    public int getX() {
        return x;
    }

    public ViewState setX(int x) {
        this.x = x;
        return this;
    }

    public int getY() {
        return y;
    }

    public ViewState setY(int y) {
        this.y = y;
        return this;
    }

    public int getWidth() {
        return width;
    }

    public ViewState setWidth(int width) {
        this.width = width;
        return this;
    }

    public int getHeight() {
        return height;
    }

    public ViewState setHeight(int height) {
        this.height = height;
        return this;
    }

    public ViewState addState(int mask) {
        this.mask |= mask;
        return this;
    }

    public ViewState removeState(int mask) {
        this.mask &= ~mask;
        return this;
    }

    public boolean isStateSet(int mask) {
        return (this.mask & mask) != 0;
    }

    public ViewState resetState() {
        mask = 0;
        return this;
    }

    public boolean isMaximized() {
        return (mask & MAXIMIZED) != 0;
    }

    public ViewState setMaximized(boolean max) {
        if (max) {
            mask |= MAXIMIZED;
        } else {
            mask &= ~MAXIMIZED;
        }
        return this;
    }

    public boolean isMinimized() {
        return (mask & MINIMIZED) != 0;
    }

    public ViewState setMinimized(boolean min) {
        if (min) {
            mask |= MINIMIZED;
        } else {
            mask &= ~MINIMIZED;
        }
        return this;
    }

    @Override
    public String toString() {
        return "ViewState{" +
                "x=" + x +
                ", y=" + y +
                ", width=" + width +
                ", height=" + height +
                ", mask=" + mask +
                '}';
    }

    public boolean isSnapped() {
        return isStateSet(RIGHT | LEFT | BOTTOM | UPPER);
    }

    public ViewState resetSnap() {
        return removeState(RIGHT | LEFT | BOTTOM | UPPER);
    }

    public JSONObject deserialize() throws JSONException {
        return new JSONObject()
                .put(Globals.Keys.X, x)
                .put(Globals.Keys.Y, y)
                .put(Globals.Keys.WIDTH, width)
                .put(Globals.Keys.HEIGHT, height)
                .put(Globals.Keys.STATE, mask);
    }
}
