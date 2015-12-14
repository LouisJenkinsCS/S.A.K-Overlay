package com.theif519.sakoverlay.Fragments.Floating;


import android.app.Fragment;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.theif519.sakoverlay.POJO.ViewState;
import com.theif519.sakoverlay.Misc.Globals;
import com.theif519.sakoverlay.Misc.MeasureTools;
import com.theif519.sakoverlay.R;
import com.theif519.sakoverlay.Rx.RxBus;
import com.theif519.sakoverlay.Sessions.SessionManager;
import com.theif519.sakoverlay.Views.TouchInterceptorLayout;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by theif519 on 10/29/2015.
 */
public class FloatingFragment extends Fragment {
    protected ViewState mVC;
    protected String LAYOUT_TAG;
    protected int mLayoutId, mIconId, mMinWidth = MeasureTools.scaleInverse(250), mMinHeight = MeasureTools.scaleInverse(250);
    protected long id = -1;
    private TouchInterceptorLayout mContentView;
    private ImageButton mTaskBarButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = (TouchInterceptorLayout) inflater.inflate(mLayoutId, container, false);
        setupTaskItem();
        // Ensures that the following methods are called after the view is fully drawn and setup.
        mContentView.post(() -> {
            setupListeners();
            setup();
        });
        return mContentView;
    }

    /**
     * This is where we setup the bottom task bar's button for this FloatingFragment. Very bare-bones right now.
     */
    private void setupTaskItem() {
        mTaskBarButton = new ImageButton(getActivity());
        mTaskBarButton.setImageResource(mIconId);
        mTaskBarButton.setOnClickListener(v -> {
            if (mContentView.getVisibility() == View.INVISIBLE) {
                mContentView.setVisibility(View.VISIBLE);
                mContentView.bringToFront();
            } else {
                mContentView.setVisibility(View.INVISIBLE);
            }
        });
        ((LinearLayout) getActivity().findViewById(R.id.main_task_bar)).addView(mTaskBarButton);
    }

    /**
     * Where we initialize all of our listeners.
     */
    private void setupListeners() {
        mContentView.findViewById(R.id.title_bar_close).setOnClickListener(v -> {
            SessionManager.getInstance().deleteSession(FloatingFragment.this);
            getActivity().getFragmentManager().beginTransaction().remove(FloatingFragment.this).commit();
        });
        mContentView.findViewById(R.id.title_bar_minimize).setOnClickListener(v -> minimize());
        mContentView.findViewById(R.id.title_bar_maximize).setOnClickListener(v -> {
            if (mVC.isMaximized()) {
                mVC.setMaximized(false);
                if(mVC.isSnapped()){
                    snap();
                } else restoreState();
            } else {
                maximize();
            }
            SessionManager.getInstance().updateSession(this);
        });
        mContentView.findViewById(R.id.title_bar_move).setOnTouchListener(new View.OnTouchListener() {
            int prevX, prevY, touchXOffset, touchYOffset;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mContentView.bringToFront();
                        if (mVC.isMaximized() || mVC.isSnapped()) {
                            mVC.resetState();
                            restoreState();
                            setCoordinates(
                                    (int) event.getRawX() - MeasureTools.scaleDeltaWidth(mContentView),
                                    (int) event.getRawY() - MeasureTools.scaleDeltaHeight(mContentView)
                            );
                            Point p = MeasureTools.getScaledCoordinates(mContentView);
                            prevX = touchXOffset = p.x - mVC.getX();
                            prevY = touchYOffset = p.y - mVC.getY();
                        } else {
                            prevX = touchXOffset = (int) event.getRawX() - mVC.getX();
                            prevY = touchYOffset = (int) event.getRawY() - mVC.getY();
                        }
                        return false;
                    case MotionEvent.ACTION_MOVE:
                        int tmpX, tmpY;
                        updateSnapMask(prevX, prevY, (tmpX = (int) event.getRawX()), (tmpY = (int) event.getRawY()));
                        prevX = tmpX;
                        prevY = tmpY;
                        setCoordinates(tmpX - touchXOffset, tmpY - touchYOffset);
                        return false;
                    case MotionEvent.ACTION_UP:
                        boundsCheck();
                        snap();
                        SessionManager.getInstance().updateSession(FloatingFragment.this);
                        return true;
                    default:
                        return false;
                }
            }
        });
        mContentView.findViewById(R.id.resize_button).setOnTouchListener(new View.OnTouchListener() {
            int tmpX, tmpY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mContentView.bringToFront();
                        Point p = MeasureTools.getScaledCoordinates(mContentView);
                        tmpX = p.x;
                        tmpY = p.y;
                        return false;
                    case MotionEvent.ACTION_MOVE:
                        mVC.resetState();
                        setSize(
                                Math.abs(MeasureTools.scaleInverse((int) event.getRawX() - tmpX)),
                                Math.abs(MeasureTools.scaleInverse((int) event.getRawY() - tmpY))
                        );
                        return false;
                    case MotionEvent.ACTION_UP:
                        boundsCheck();
                        SessionManager.getInstance().updateSession(FloatingFragment.this);
                        return true;
                    default:
                        return false;
                }
            }
        });
        RxBus.subscribe(Configuration.class)
                .subscribe(configuration -> {
                    boundsCheck();
                    snap();
                    if (mVC.isStateSet(ViewState.MAXIMIZED)) {
                        maximize();
                    }
                });
    }

    private void setSize(int width, int height) {
        mVC
                .setWidth(width)
                .setHeight(height);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mContentView.getLayoutParams();
        params.width = width;
        params.height = height;
        mContentView.setLayoutParams(params);
    }

    private void setCoordinates(int x, int y){
        mVC
                .setX(x)
                .setY(y);
        mContentView.setX(x);
        mContentView.setY(y);
    }

    private void setX(int x){
        mVC
                .setX(x);
        mContentView.setX(x);
    }

    private void setY(int y){
        mVC
                .setY(y);
        mContentView.setY(y);
    }

    private void setWidth(int width){
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mContentView.getLayoutParams();
        params.width = width;
        mContentView.setLayoutParams(params);
    }

    private void setHeight(int height){
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mContentView.getLayoutParams();
        params.height = height;
        mContentView.setLayoutParams(params);
    }

    private void restoreState(){
        mContentView.setX(mVC.getX());
        mContentView.setY(mVC.getY());
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mContentView.getLayoutParams();
        params.width = mVC.getWidth();
        params.height = mVC.getHeight();
        mContentView.setLayoutParams(params);
    }

    /**
     * Used to snap views to a side of the window if the bitmask is set.
     * <p/>
     * By utilizing a bitmask, it allows me to dynamically snap to not just sides, but also corners as well. It uses bitwise AND'ing
     * to retrieve set bits/attributes. Unlike other operations, such as move() and resize(), changes to the mContentView's
     * size and coordinates are not saved, to easily allow the view to go back to it's original size easily.
     */
    private void snap() {
        if(!mVC.isSnapped()) return;
        int maxWidth = Globals.MAX_X.get();
        int maxHeight = Globals.MAX_Y.get();
        int width = 0, height = 0, x = 0, y = 0;
        if (mVC.isStateSet(ViewState.RIGHT )) {
            width = maxWidth / 2;
            height = maxHeight;
            x = maxWidth / 2;
        }
        if (mVC.isStateSet(ViewState.LEFT)) {
            width = maxWidth / 2;
            height = maxHeight;
        }
        if (mVC.isStateSet(ViewState.UPPER)) {
            if (width == 0) {
                width = maxWidth;
            }
            height = maxHeight / 2;
        }
        if (mVC.isStateSet(ViewState.BOTTOM)) {
            if (width == 0) {
                width = maxWidth;
            }
            height = maxHeight / 2;
            y = maxHeight / 2;
        }
        width = MeasureTools.scaleInverse(width);
        height = MeasureTools.scaleInverse(height);
        x -= MeasureTools.scaleDelta(width);
        y -= MeasureTools.scaleDelta(height);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mContentView.getLayoutParams();
        params.width = width;
        params.height = height;
        mContentView.setX(x);
        mContentView.setY(y);
        mContentView.setLayoutParams(params);
    }

    /**
     * Updates the bitmask used to maintain the current snap direction of the current view.
     *
     * @param oldX The old x-coordinate. This is used to determine the horizontal direction the user is going.
     * @param oldY The old y-coordinate. This is used to determine the vertical direction the user is going.
     * @param newX The new x-coordinate. By finding the difference between new and old we can determine which horizontal direction the user is going.
     * @param newY The new y-coordinate. By finding the difference between new and old, we can determine which vertical direction the user is going.
     */
    private void updateSnapMask(int oldX, int oldY, int newX, int newY) {
        mVC.resetSnap();
        int transitionX = newX - oldX;
        int transitionY = newY - oldY;
        int snapOffsetX = Globals.MAX_X.get() / 10;
        int snapOffsetY = Globals.MAX_Y.get() / 10;
        if (transitionX > 0 && newX + snapOffsetX >= Globals.MAX_X.get()) {
            mVC.addState(ViewState.RIGHT);
        }
        if (transitionX < 0 && newX <= snapOffsetX) {
            mVC.addState(ViewState.LEFT);
        }
        if (transitionY < 0 && newY <= snapOffsetY) {
            mVC.addState(ViewState.UPPER);
        }
        if (transitionY > 0 && newY + snapOffsetY >= Globals.MAX_Y.get()) {
            mVC.addState(ViewState.BOTTOM);
        }
    }

    /**
     * Used to check the bounds of the view. Sometimes I don't do a good enough job of checking for
     * whether or not my views remain in bounds, and other times it is impossible to maintain otherwise,
     * like if the user changes orientation. I consider these my FloatingFragment's training wheels,
     * as this gets called whenever a view's state or visibility changes (Meaning on every move, unfortunately).
     * <p/>
     * A lot of this code is honestly brute forced. I go with what I feel would be right, then modify it like
     * 100x until it is working.
     */
    private void boundsCheck() {
        Point p = MeasureTools.getScaledCoordinates(mContentView);
        if (p.x < 0) {
            setX(-MeasureTools.scaleDeltaWidth(mContentView));
        }
        if (p.y < 0) {
            setY(-MeasureTools.scaleDeltaHeight(mContentView));
        }
        if (p.x + MeasureTools.scaleWidth(mContentView) > Globals.MAX_X.get()) {
            setX(Globals.MAX_X.get() - MeasureTools.scaleDeltaWidth(mContentView) - MeasureTools.scaleWidth(mContentView));
        }
        if (p.y + MeasureTools.scaleHeight(mContentView) > Globals.MAX_Y.get()) {
            setY(Globals.MAX_Y.get() - MeasureTools.scaleDeltaHeight(mContentView) - MeasureTools.scaleHeight(mContentView));
        }
        if (MeasureTools.scaleWidth(mContentView) > Globals.MAX_X.get()) {
            setWidth(MeasureTools.scaleInverse(Globals.MAX_X.get()));
        }
        if (MeasureTools.scaleHeight(mContentView) > Globals.MAX_Y.get()) {
            setHeight(MeasureTools.scaleInverse(Globals.MAX_Y.get()));
        }
        if(MeasureTools.scaleWidth(mContentView) < mMinWidth){
            setWidth(mMinWidth);
        }
        if(MeasureTools.scaleHeight(mContentView) < mMinHeight){
            setHeight(mMinHeight);
        }
    }

    /**
     * For subclasses to override to setup their own additional needed information. Not abstract as it is not
     * necessary to setup.
     */
    protected void setup() {
        if(mVC == null){
            mVC = new ViewState();
        }
        restoreState();
        if (mVC.getWidth() == 0 || mVC.getHeight() == 0) {
            setSize(mMinWidth, mMinHeight);
        }
        snap();
        if (mVC.isMaximized()) {
            maximize();
        }
        if(mVC.isMinimized()){
            minimize();
        }
        SessionManager.getInstance().finishedSetup();
    }

    protected JSONObject pack() {
        try {
            return mVC.deserialize();
        } catch (JSONException e){
            Log.w(getClass().getName(), e.getMessage());
            return null;
        }
    }

    protected void unpack(JSONObject obj){
        try {
            mVC = new ViewState(obj);
        } catch(JSONException e){
            Log.w(getClass().getName(), e.getMessage());
        }
    }

    public void deserialize(byte[] data){
        try {
            unpack(new JSONObject(new String(data)));
        } catch(JSONException e){
            Log.w(getClass().getName(), e.getMessage());
        }
    }

    public byte[] serialize(){
        return pack().toString().getBytes();
    }

    /**
     * For any subclasses that need to clean up extra resources, they may do so here.
     */
    protected void cleanUp() {
        ((LinearLayout) getActivity().findViewById(R.id.main_task_bar)).removeView(mTaskBarButton);
    }

    /**
     * Maximizes the view. Note that it does not alter the view's X and Y coordinate through the ViewState
     * instance, as we do not really want it to be saved. This allows for the view to go back to it's
     * previous size and coordinates easily, as the previous are remembered. In fact,  what's really cool about
     * it is that because it is not set, but the state of whether or not it is maximized is, it will allow the user
     * to go back to their previous size EVEN if they already exited the program fully.
     */
    private void maximize() {
        int maxX = MeasureTools.scaleInverse(Globals.MAX_X.get()), maxY = MeasureTools.scaleInverse(Globals.MAX_Y.get());
        mContentView.setX(-MeasureTools.scaleDelta(maxX));
        mContentView.setY(-MeasureTools.scaleDelta(maxY));
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mContentView.getLayoutParams();
        params.width = maxX;
        params.height = maxY;
        mContentView.setLayoutParams(params);
        mContentView.bringToFront();
        mVC.setMaximized(true);
    }

    /**
     * Used to minimize the view. It doesn't do much right now, but it works.
     */
    private void minimize() {
        mContentView.setVisibility(View.INVISIBLE);
        mVC.setMinimized(true);
        SessionManager.getInstance().updateSession(this);
    }

    /**
     * Kind of redundant to have CleanUp() here, but I'll refactor it out later.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        cleanUp();
    }

    /**
     * A way for the subclasses to retrieve the Content View. In the future, I most likely will make the
     * mContentView protected and remove this, or make this public for other classes to use, I.E MainActivity.
     *
     * @return The content view.
     */
    protected View getContentView() {
        return mContentView;
    }

    public ViewState getViewProperties(){
        return mVC;
    }

    public String getLayoutTag() {
        return LAYOUT_TAG;
    }

    public long getUniqueId(){
        return id;
    }

    public void setUniqueId(long id){
        this.id = id;
    }

}
