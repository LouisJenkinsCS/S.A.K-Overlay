package com.theif519.sakoverlay.Widgets;

import android.content.Context;
import android.support.v7.view.menu.MenuBuilder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.theif519.sakoverlay.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;

import rx.Observable;
import rx.subjects.PublishSubject;

import static com.theif519.sakoverlay.Core.Misc.Globals.Keys.HEIGHT;
import static com.theif519.sakoverlay.Core.Misc.Globals.Keys.STATE;
import static com.theif519.sakoverlay.Core.Misc.Globals.Keys.WIDTH;
import static com.theif519.sakoverlay.Core.Misc.Globals.Keys.X;
import static com.theif519.sakoverlay.Core.Misc.Globals.Keys.Y;

/**
 * Created by theif519 on 2/5/2016.
 */
public abstract class AbstractWidget {

    private static final int UNINITIALIZED = -1;

    private float mX, mY;
    private int mWidth, mHeight, mStateMask = UNINITIALIZED;
    private long mId = UNINITIALIZED;
    private View mView;
    private Menu mMenu;

    PublishSubject<Menu> mMenuRequest = PublishSubject.create();

    public AbstractWidget(Context context){
        mView = createView(context);
        mView.post(() -> setup(context));
    }

    public AbstractWidget(Context context, byte[] data){
        try {
            JSONObject obj = new JSONObject(new String(data));
            unpack(obj);
        } catch (JSONException e) {
            Log.e(getClass().getSimpleName(), "Failed while attempting to parse passed data" +
                    " in constructor, with error messages: \"" + e.getMessage() + "\"!");
        }
        mView = createView(context);
        mView.post(() -> setup(context));
    }

    abstract protected View createView(Context context);

    public View getView(){
        return mView;
    }

    public long getId(){
        return mId;
    }

    public void setId(long id){
        mId = id;
    }

    abstract protected int getIconId();

    protected Context getContext(){
        return mView.getContext();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Below are defined the serialization and deserialization operations. They are used to maintain
    // persistence across user sessions. The subclasses may and almost always override and extend
    // these methods. The is a set of externally accessible methods, deserialize and serialize,
    // which a caller may use for their specified purposes. The set of internal accessible methods,
    // pack and unpack, are called by subclasses and are extended to add their own persistence
    // lifecycle.
    ////////////////////////////////////////////////////////////////////////////////////////////////

    protected JSONObject pack() {
        JSONObject data = new JSONObject();
        try {
            data
                    .put(X, mX)
                    .put(Y, mY)
                    .put(WIDTH, mWidth)
                    .put(HEIGHT, mHeight)
                    .put(STATE, mStateMask);
        } catch (JSONException e) {
            Log.w(getClass().getName(), e.getMessage());
        }
        return data;
    }

    protected void unpack(JSONObject data) {
        try {
            mX = BigDecimal.valueOf(data.getDouble(X)).floatValue();
            mY = BigDecimal.valueOf(data.getDouble(Y)).floatValue();
            mWidth = data.getInt(WIDTH);
            mHeight = data.getInt(HEIGHT);
            mStateMask = data.getInt(STATE);
        } catch (JSONException e) {
            Log.w(getClass().getName(), e.getMessage());
        }
    }

    public byte[] serialize() {
        return pack()
                .toString()
                .getBytes();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Below are the lifecycle methods for this class and all subclasses. They are used to help
    // maintain the lifecycle of a Widget, allowing subclasses to override and extend these methods
    // to allow them to add their own lifecycle code. Generally, the base class handles majority of
    // the code needed to work properly, but some more complex subclasses may prefer to, have to
    // do their own.
    //
    // Generally, setup is called after unpack if data is passed in and after mView has finished
    // being created. Hence, inside setup, it is 100% safe to use mView to manipulate the UI.
    //
    // cleanUp is called when this view is about to be destroyed. Any and all last-minute operations
    // should be done here and clean up fast.
    ////////////////////////////////////////////////////////////////////////////////////////////////


    protected void setup(Context context){
        if(mStateMask == UNINITIALIZED){
            mX = mView.getX();
            mY = mView.getY();
            mWidth = mView.getWidth();
            mHeight = mView.getHeight();
            mStateMask = 0;
        } else {
            mView.setX(mX);
            mView.setY(mY);
            mView.getLayoutParams().width = mWidth;
            mView.getLayoutParams().height = mHeight;
            mView.requestLayout();
            // TODO: Let View know it's state.
        }
    }

    protected void cleanUp(){

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Methods to handle creating and managing the contextual menu. These menus get created once
    // upon creation of this Widget, and after initialization and when it gains focus, it emits
    // to an Observable to request that it gets put at the top. The selection of items are handled
    // as well by allowing subclasses to handle their own menu items.
    //
    // A Menu is destroyed when the owning Widgets gets destroyed as well, of which it will notify
    // the Manager that it has done so.
    ////////////////////////////////////////////////////////////////////////////////////////////////

    protected MenuBuilder buildMenu(){
        MenuBuilder builder = new MenuBuilder(getContext());
        builder
                .add("Close")
                .setIcon(R.drawable.close);
        return builder;
    }

    public Observable<Menu> observeMenuRequest(){
        return mMenuRequest.asObservable();
    }

    protected void menuItemSelected(MenuItem item){

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Methods to handle creating the TaskBarItem, which is the bar located at the bottom of the
    // activity. It handles presses, which normally minimizes mView, however can be overridden and
    // extended for subclasses to extend functionality of the TaskBarItem.
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private View createTaskBarItem(){
        // TODO: Create TaskBarItem, and create the class as well.
        return null;
    }

    protected void onTaskBarItemClick(){
        // TODO: Call mView's toggle for minimization.
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Methods to allow detaching Widgets from their container, as well as reattaching them.
    // To detach, means to remove it from Activity it is confined in, and add it to Android's
    // WindowManager to allow it to exist outside of the screen. Hence, while detached, certain
    // features will stop working but other features become available. At the current time, it is
    // not known what will and will not be working.
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void detach(ViewGroup oldParent){
        oldParent.removeView(mView);
        WindowManager newParent = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        newParent.addView(mView, mView.getLayoutParams());
    }

    public void reattach(ViewGroup newParent){
        WindowManager oldParent = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        oldParent.removeView(mView);
        newParent.addView(mView);
    }
}
