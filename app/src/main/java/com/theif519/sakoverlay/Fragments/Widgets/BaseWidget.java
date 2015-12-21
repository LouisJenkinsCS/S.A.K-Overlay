package com.theif519.sakoverlay.Fragments.Widgets;


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
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.theif519.sakoverlay.Animations.ResizingAnimation;
import com.theif519.sakoverlay.Builders.MenuBuilder;
import com.theif519.sakoverlay.Misc.Globals;
import com.theif519.sakoverlay.Misc.MeasureTools;
import com.theif519.sakoverlay.POJO.MenuOptions;
import com.theif519.sakoverlay.POJO.ViewState;
import com.theif519.sakoverlay.R;
import com.theif519.sakoverlay.Rx.RxBus;
import com.theif519.sakoverlay.Sessions.SessionManager;
import com.theif519.sakoverlay.Views.TouchInterceptorLayout;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by theif519 on 10/29/2015.
 */
public class BaseWidget extends Fragment {

    public static final BaseWidget INVALID_WIDGET = new BaseWidget();

    protected ViewState mViewState;
    protected String LAYOUT_TAG;
    protected int mLayoutId, mIconId, mMinWidth = MeasureTools.scaleInverse(250), mMinHeight = MeasureTools.scaleInverse(250);
    protected long id = -1;
    private TouchInterceptorLayout mContentView;
    private ImageButton mTaskBarButton;
    private View mSnapShadow;
    private MenuOptions mOptionsMenu;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = (TouchInterceptorLayout) inflater.inflate(mLayoutId, container, false);
        mContentView.setVisibility(View.INVISIBLE);
        setupTaskItem();
        mSnapShadow = new ImageView(getActivity());
        mSnapShadow.setScaleX(Globals.SCALE.get());
        mSnapShadow.setScaleY(Globals.SCALE.get());
        mSnapShadow.setBackground(getResources().getDrawable(R.drawable.snap_shadow));
        mSnapShadow.setVisibility(View.INVISIBLE);
        ((ViewGroup) getActivity().findViewById(R.id.main_layout)).addView(mSnapShadow);
        createOptions();
        // Ensures that the following methods are called after the view is fully drawn and setup.
        mContentView.post(() -> {
            setupListeners();
            setup();
            RxBus.publish(mOptionsMenu);
        });
        return mContentView;
    }

    /**
     * This is where we setup the bottom task bar's button for this BaseWidget. Very bare-bones right now.
     */
    private void setupTaskItem() {
        mTaskBarButton = new ImageButton(getActivity());
        mTaskBarButton.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        mTaskBarButton.setImageResource(mIconId);
        mTaskBarButton.setOnClickListener(v -> toggleMinimize());
        ((LinearLayout) getActivity().findViewById(R.id.main_task_bar)).addView(mTaskBarButton);
    }

    /**
     * Where we initialize all of our listeners.
     */
    private void setupListeners() {
        mContentView.findViewById(R.id.title_bar_close).setOnClickListener(v -> close());
        mContentView.findViewById(R.id.title_bar_minimize).setOnClickListener(v -> minimize());
        mContentView.findViewById(R.id.title_bar_maximize).setOnClickListener(v -> toggleMaximize());
        RxBus
                .observe(Integer.class)
                .filter(count -> count != null && count % 5 == 0 && !mOptionsMenu.isShowing())
                .subscribe(count -> toggleMinimize());
        mContentView.setCallback(() -> RxBus.publish(mOptionsMenu));
        mContentView.findViewById(R.id.title_bar_move)
                .setOnTouchListener(new View.OnTouchListener() {
                    int prevX, prevY, touchXOffset, touchYOffset;

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                if (mViewState.isMaximized() || mViewState.isSnapped()) {
                                    mViewState.resetState();
                                    restoreState();
                                    setCoordinates(
                                            (int) event.getRawX() - MeasureTools.scaleDeltaWidth(mContentView),
                                            (int) event.getRawY() - MeasureTools.scaleDeltaHeight(mContentView)
                                    );
                                    Point p = MeasureTools.getScaledCoordinates(mContentView);
                                    prevX = touchXOffset = p.x - mViewState.getX();
                                    prevY = touchYOffset = p.y - mViewState.getY();
                                } else {
                                    prevX = touchXOffset = (int) event.getRawX() - mViewState.getX();
                                    prevY = touchYOffset = (int) event.getRawY() - mViewState.getY();
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
                                snap(mContentView);
                                SessionManager
                                        .getInstance()
                                        .updateSession(BaseWidget.this);
                                return true;
                            default:
                                return false;
                        }
                    }
                });
        mContentView.findViewById(R.id.resize_button)
                .setOnTouchListener(new View.OnTouchListener() {
                    int tmpX, tmpY;

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                Point p = MeasureTools.getScaledCoordinates(mContentView);
                                tmpX = p.x;
                                tmpY = p.y;
                                return false;
                            case MotionEvent.ACTION_MOVE:
                                mViewState.resetState();
                                setSize(
                                        Math.abs(MeasureTools.scaleInverse((int) event.getRawX() - tmpX)),
                                        Math.abs(MeasureTools.scaleInverse((int) event.getRawY() - tmpY))
                                );
                                return false;
                            case MotionEvent.ACTION_UP:
                                boundsCheck();
                                SessionManager
                                        .getInstance()
                                        .updateSession(BaseWidget.this);
                                return true;
                            default:
                                return false;
                        }
                    }
                });
        RxBus
                .observe(Configuration.class)
                .subscribe(configuration -> {
                    boundsCheck();
                    snap(mContentView);
                    if (mViewState.isMaximized()) {
                        maximize();
                    }
                });
    }

    private void close() {
        cleanUp();
        SessionManager
                .getInstance()
                .deleteSession(BaseWidget.this);
        getActivity()
                .getFragmentManager()
                .beginTransaction()
                .remove(BaseWidget.this)
                .commit();
        if (mOptionsMenu != null) {
            mOptionsMenu.notifyObservers();
        }
    }

    private void setSize(int width, int height) {
        mViewState
                .setWidth(width)
                .setHeight(height);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mContentView.getLayoutParams();
        params.width = width;
        params.height = height;
        mContentView.setLayoutParams(params);
    }

    private void setCoordinates(int x, int y) {
        mViewState
                .setX(x)
                .setY(y);
        mContentView.setX(x);
        mContentView.setY(y);
    }

    private void setX(int x) {
        mViewState.setX(x);
        mContentView.setX(x);
    }

    private void setY(int y) {
        mViewState.setY(y);
        mContentView.setY(y);
    }

    private void setWidth(int width) {
        mViewState.setWidth(width);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mContentView.getLayoutParams();
        params.width = width;
        mContentView.setLayoutParams(params);
    }

    private void setHeight(int height) {
        mViewState.setHeight(height);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mContentView.getLayoutParams();
        params.height = height;
        mContentView.setLayoutParams(params);
    }

    private void restoreState() {
        mContentView.setX(mViewState.getX());
        mContentView.setY(mViewState.getY());
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mContentView.getLayoutParams();
        params.width = mViewState.getWidth();
        params.height = mViewState.getHeight();
        mContentView.setLayoutParams(params);
    }

    /**
     * Used to snap views to a side of the window if the bitmask is set.
     * <p>
     * By utilizing a bitmask, it allows me to dynamically snap to not just sides, but also corners as well. It uses bitwise AND'ing
     * to retrieve set bits/attributes. Unlike other operations, such as move() and resize(), changes to the mContentView's
     * size and coordinates are not saved, to easily allow the view to go back to it's original size easily.
     */
    private void snap(View v) {
        if (!mViewState.isSnapped()) return;
        int maxWidth = Globals.MAX_X.get();
        int maxHeight = Globals.MAX_Y.get();
        int width = 0, height = 0, x = 0, y = 0;
        if (mViewState.isStateSet(ViewState.RIGHT)) {
            width = maxWidth / 2;
            height = maxHeight;
            x = maxWidth / 2;
        }
        if (mViewState.isStateSet(ViewState.LEFT)) {
            width = maxWidth / 2;
            height = maxHeight;
        }
        if (mViewState.isStateSet(ViewState.UPPER)) {
            if (width == 0) {
                width = maxWidth;
            }
            height = maxHeight / 2;
        }
        if (mViewState.isStateSet(ViewState.BOTTOM)) {
            if (width == 0) {
                width = maxWidth;
            }
            height = maxHeight / 2;
            y = maxHeight / 2;
        }
        width = MeasureTools.scaleInverse(width);
        height = MeasureTools.scaleInverse(height);
        if (v == mContentView) {
            mSnapShadow.setVisibility(View.INVISIBLE);
            ResizingAnimation anim = new ResizingAnimation(v, width, height);
            anim.setDuration(500);
            v.startAnimation(anim);
        } else {
            v.getLayoutParams().height = height;
            v.getLayoutParams().width = width;
            v.requestLayout();
        }
        x -= MeasureTools.scaleDelta(width);
        y -= MeasureTools.scaleDelta(height);
        v.setX(x);
        v.setY(y);
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
        mViewState.resetSnap();
        int transitionX = newX - oldX;
        int transitionY = newY - oldY;
        int snapOffsetX = Globals.MAX_X.get() / 10;
        int snapOffsetY = Globals.MAX_Y.get() / 10;
        if (transitionX > 0 && newX + snapOffsetX >= Globals.MAX_X.get()) {
            mViewState.addState(ViewState.RIGHT);
        }
        if (transitionX < 0 && newX <= snapOffsetX) {
            mViewState.addState(ViewState.LEFT);
        }
        if (transitionY < 0 && newY <= snapOffsetY) {
            mViewState.addState(ViewState.UPPER);
        }
        if (transitionY > 0 && newY + snapOffsetY >= Globals.MAX_Y.get()) {
            mViewState.addState(ViewState.BOTTOM);
        }
        if (mViewState.isSnapped()) {
            mSnapShadow.setVisibility(View.VISIBLE);
            snap(mSnapShadow);
        } else mSnapShadow.setVisibility(View.INVISIBLE);
    }

    /**
     * Used to check the bounds of the view. Sometimes I don't do a good enough job of checking for
     * whether or not my views remain in bounds, and other times it is impossible to maintain otherwise,
     * like if the user changes orientation. I consider these my BaseWidget's training wheels,
     * as this gets called whenever a view's state or visibility changes (Meaning on every move, unfortunately).
     * <p>
     * A lot of this code is honestly brute forced. I go with what I feel would be right, then modify it like
     * 100x until it is working.
     */
    private void boundsCheck() {
        // Checks if the scaled width is less than minimum width
        if (MeasureTools.scaleWidth(mContentView) < mMinWidth) {
            setWidth(mMinWidth);
        }
        // Checks if the scaled height is less than minimum height
        if (MeasureTools.scaleHeight(mContentView) < mMinHeight) {
            setHeight(mMinHeight);
        }
        // Checks if the overall width is greater than maximum available width
        if (MeasureTools.scaleWidth(mContentView) > Globals.MAX_X.get()) {
            setWidth(MeasureTools.scaleInverse(Globals.MAX_X.get()));
        }
        // Checks if the overall height is greater than the maximum available height
        if (MeasureTools.scaleHeight(mContentView) > Globals.MAX_Y.get()) {
            setHeight(MeasureTools.scaleInverse(Globals.MAX_Y.get()));
        }
        Point p = MeasureTools.getScaledCoordinates(mContentView);
        // Checks right side of screen relative to X coordinate
        if (p.x + MeasureTools.scaleWidth(mContentView) > Globals.MAX_X.get()) {
            setX(Globals.MAX_X.get() - MeasureTools.scaleDeltaWidth(mContentView) - MeasureTools.scaleWidth(mContentView));
        }
        // Checks top side of screen relative to Y coordinate
        if (p.y + MeasureTools.scaleHeight(mContentView) > Globals.MAX_Y.get()) {
            setY(Globals.MAX_Y.get() - MeasureTools.scaleDeltaHeight(mContentView) - MeasureTools.scaleHeight(mContentView));
        }
        p = MeasureTools.getScaledCoordinates(mContentView);
        // Checks left side of screen
        if (p.x < 0) {
            setX(-MeasureTools.scaleDeltaWidth(mContentView));
        }
        // Checks top side of screen
        if (p.y < 0) {
            setY(-MeasureTools.scaleDeltaHeight(mContentView));
        }
    }

    /**
     * For subclasses to override to setup their own additional needed information. Not abstract as it is not
     * necessary to setup.
     */
    protected void setup() {
        if (mViewState == null) {
            mViewState = new ViewState();
        }
        restoreState();
        if (mViewState.getWidth() == 0 || mViewState.getHeight() == 0) {
            setSize(mMinWidth, mMinHeight);
        }
        snap(mContentView);
        if (mViewState.isMaximized()) {
            maximize();
        }
        if (mViewState.isMinimized()) {
            minimize();
        } else mContentView.setVisibility(View.VISIBLE);
    }

    protected JSONObject pack() {
        try {
            return mViewState.deserialize();
        } catch (JSONException e) {
            Log.w(getClass().getName(), e.getMessage());
            return null;
        }
    }

    protected void unpack(JSONObject obj) {
        try {
            mViewState = new ViewState(obj);
        } catch (JSONException e) {
            Log.w(getClass().getName(), e.getMessage());
        }
    }

    public void deserialize(byte[] data) {
        try {
            unpack(new JSONObject(new String(data)));
        } catch (JSONException e) {
            Log.w(getClass().getName(), e.getMessage());
        }
    }

    public byte[] serialize() {
        return pack()
                .toString()
                .getBytes();
    }

    /**
     * For any subclasses that need to clean up extra resources, they may do so here.
     */
    protected void cleanUp() {
        ((LinearLayout) getActivity()
                .findViewById(R.id.main_task_bar))
                .removeView(mTaskBarButton);
    }

    protected MenuBuilder buildOptions() {
        return new MenuBuilder()
                .addSeparator(LAYOUT_TAG)
                .addOption("Check Bounds", null, this::boundsCheck);
    }

    private void createOptions() {
        MenuBuilder builder = buildOptions();
        mOptionsMenu = new MenuOptions(builder.create(getActivity()), mIconId, LAYOUT_TAG);
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
        mContentView.bringToFront();
        mContentView.setX(-MeasureTools.scaleDelta(maxX));
        mContentView.setY(-MeasureTools.scaleDelta(maxY));
        ResizingAnimation anim = new ResizingAnimation(mContentView, maxX, maxY);
        anim.setDuration(500);
        mContentView.startAnimation(anim);
        mViewState.setMaximized(true);
        SessionManager.getInstance().updateSession(this);
    }

    private void restoreMaximize() {
        if (mViewState.isSnapped()) {
            snap(mContentView);
            mViewState.setMaximized(false);
            SessionManager.getInstance().updateSession(this);
            return;
        }
        mContentView.bringToFront();
        mContentView.setX(mViewState.getX());
        mContentView.setY(mViewState.getY());
        ResizingAnimation anim = new ResizingAnimation(mContentView, mViewState.getWidth(), mViewState.getHeight());
        anim.setDuration(500);
        mContentView.startAnimation(anim);
        mViewState.setMaximized(false);
        SessionManager.getInstance().updateSession(this);
    }

    private void toggleMaximize() {
        if (mViewState.isMaximized()) {
            restoreMaximize();
        } else {
            maximize();
        }
    }

    /**
     * Used to minimize the view. It doesn't do much right now, but it works.
     */
    private void toggleMinimize() {
        if (mContentView.getVisibility() == View.INVISIBLE) {
            restoreMinimize();
        } else {
            minimize();
        }
        SessionManager
                .getInstance()
                .updateSession(this);
    }

    private int oldX, oldY;

    private void minimize() {
        oldX = (int) mContentView.getX();
        oldY = (int) mContentView.getY();
        mContentView
                .animate()
                .translationX(mTaskBarButton.getX())
                .translationY(Globals.MAX_Y.get())
                .scaleX(0)
                .scaleY(0)
                .setDuration(500)
                .withEndAction(() -> {
                    mContentView.setVisibility(View.INVISIBLE);
                    Log.i(getClass().getName(), "(" + mContentView.getX() + ", " + mContentView.getY() + ")\n<" + mContentView.getWidth() + "x" + mContentView.getHeight() + ">");
                })
                .start();
        mViewState.setMinimized(true);
        SessionManager
                .getInstance()
                .updateSession(this);
    }

    private void restoreMinimize() {
        mContentView
                .animate()
                .withStartAction(() -> {
                    mContentView.setVisibility(View.VISIBLE);
                    mContentView.bringToFront();
                })
                .translationX(oldX)
                .translationY(oldY)
                .scaleX(Globals.SCALE.get())
                .scaleY(Globals.SCALE.get())
                .setDuration(500)
                .withEndAction(() -> {
                    boundsCheck();
                    if (mViewState.isMaximized()) {
                        maximize();
                    } else if (mViewState.isSnapped()) {
                        snap(mContentView);
                    }
                    SessionManager
                            .getInstance()
                            .updateSession(this);
                })
                .start();
        mViewState.setMinimized(false);
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

    public String getLayoutTag() {
        return LAYOUT_TAG;
    }

    public long getUniqueId() {
        return id;
    }

    public void setUniqueId(long id) {
        this.id = id;
    }

}
