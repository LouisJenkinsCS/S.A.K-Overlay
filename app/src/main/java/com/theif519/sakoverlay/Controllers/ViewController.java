package com.theif519.sakoverlay.Controllers;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.FrameLayout;

import com.theif519.sakoverlay.Misc.Globals;
import com.theif519.sakoverlay.Misc.MeasureTools;

/**
 * Created by theif519 on 12/4/2015.
 * <p/>
 * Encapsulates attributes and properties of a view. This class is used to keep track of not only the
 * view properties/state, but also set them directly. That, in conjunction with it's fluent interface/method chaining,
 * it allows for easy instantiation, easy updating, and easy serialization/deserialization.
 * <p/>
 * In the future, if I wish to have a background thread be able to also manipulate a view, I can make the changes
 * here without interrupting/needing to refactor other classes which uses this class.
 * <p/>
 * That could be accomplished by doing something along the lines of...
 * <p/>
 * <code> <pre>
 * private Handler mUiHandler = new Handler(); // Automatically gets looper of the current thread, in this case main looper.
 *
 * public ViewController update(){
 *      if(Looper.myLooper() != Looper.getMainLooper()){ // Note also we do not need to worry about thread safety
 *          mUiHandler.post(() -> update()); // Would result in the UI thread calling this method
 *          return; // Then we return as we do not want this thread to touch anything else.
 *      }
 *      // Otherwise if it is the main thread, do it here.
 * }
 * </pre> </code>
 * <p/>
 * The reasons for needing such checks, of course, is that I plan on making ViewController accessible outside
 * of this class. Meaning, it is a way for other classes and even threads to post updates to through this class.
 */
public class ViewController {

    private String tag;

    private long id;

    private int stateMask = 0;

    public static final int RIGHT = 1, LEFT = 1 << 1, UPPER = 1 << 2, BOTTOM = 1 << 3;

    public static final int MINIMIZED = 1 << 4, MAXIMIZED = 1 << 5, DEAD = 1 << 6;

    private int x, y, width, height;
    private transient View v;
    private transient Handler mHandler;

    public ViewController(){
        mHandler = new Handler(Looper.getMainLooper());
    }

    public ViewController(@NonNull View v, String tag, long id) {
        mHandler = new Handler(Looper.getMainLooper());
        this.tag = tag;
        this.id = id;
        this.v = v;
    }

    /**
     * This class is generally used to restore the view to a previous state. For instance, when snap() and
     * maximize() is called, these attributes are never updated, hence it is easiest to just call this method
     * to restore the previous view's state. It should also be noted that there are checks put in place,
     * which right now are very simplistic and minimal, to offer a means of bounds checking, in the case
     * that the user changes orientation.
     *
     * @return This.
     */
    public ViewController update() {
        if(!isUIThread()){
            mHandler.post(this::update);
            return this;
        }
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

    public ViewController setX(int x) {
        if(!isUIThread()){
            mHandler.post(() -> setX(x));
            return this;
        }
        this.x = x;
        v.setX(x);
        return this;
    }

    public int getY() {
        return y;
    }

    public ViewController setY(int y) {
        if(!isUIThread()){
            mHandler.post(() -> setY(y));
            return this;
        }
        if (this.y == y) return this;
        this.y = y;
        v.setY(y);
        return this;
    }

    public int getWidth() {
        return width;
    }

    public ViewController setWidth(int width) {
        if(!isUIThread()){
            mHandler.post(() -> setWidth(width));
            return this;
        }
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

    public ViewController setHeight(int height) {
        if(!isUIThread()){
            mHandler.post(() -> setHeight(height));
            return this;
        }
        if (this.height == height) return this;
        this.height = height;
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) v.getLayoutParams();
        params.height = height;
        v.setLayoutParams(params);
        return this;
    }

    public ViewController addState(int mask){
        stateMask |= mask;
        return this;
    }

    public ViewController removeState(int mask){
        stateMask &= ~mask;
        return this;
    }

    public ViewController toggleState(int mask){
        stateMask ^= mask;
        return this;
    }

    public int getState(){
        return stateMask;
    }

    public boolean isStateSet(int mask){
        return (stateMask & mask) != 0;
    }

    public boolean hasState(){
        return stateMask != 0;
    }

    public ViewController resetState(){
        stateMask = 0;
        return this;
    }

    public boolean isMaximized(){
        return isStateSet(MAXIMIZED);
    }

    public boolean isMinimized(){
        return isStateSet(MINIMIZED);
    }

    public boolean isSnapped(){
        return isStateSet(RIGHT | LEFT | BOTTOM | UPPER);
    }

    public ViewController resetSnap(){
        return removeState(RIGHT | LEFT | BOTTOM | UPPER);
    }

    public ViewController setCoordinates(int x, int y) {
        if(!isUIThread()){
            mHandler.post(() -> setCoordinates(x, y));
            return this;
        }
        v.setX(x);
        v.setY(y);
        return this;
    }

    public ViewController setSize(int width, int height) {
        if(!isUIThread()){
            mHandler.post(() -> setSize(width, height));
            return this;
        }
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) v.getLayoutParams();
        params.width = width;
        params.height = height;
        v.setLayoutParams(params);
        return this;
    }

    public String getTag() {
        return tag;
    }

    public long getId() {
        return id;
    }

    public ViewController setId(long id){
        this.id = id;
        return this;
    }

    public ViewController setTag(String tag){
        this.tag = tag;
        return this;
    }

    private boolean isUIThread(){
        return Looper.myLooper() == Looper.getMainLooper();
    }
}
