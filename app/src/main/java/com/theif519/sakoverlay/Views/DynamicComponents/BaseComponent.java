package com.theif519.sakoverlay.Views.DynamicComponents;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.theif519.sakoverlay.Misc.Globals;
import com.theif519.sakoverlay.R;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by theif519 on 12/27/2015.
 */
public abstract class BaseComponent extends FrameLayout {

    private final GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
        @Override
        public void onLongPress(MotionEvent e) {
            BaseComponent.this.onLongPress();
        }
    });

    private Button mResizeButton, mMoveButton;
    private ViewGroup mContainer;

    public BaseComponent(Context context) {
        this(context, null);
    }

    public BaseComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.dynamic_component, this);
        mResizeButton = (Button) findViewById(R.id.resize_button);
        mResizeButton.setOnTouchListener(this::resize);
        mMoveButton = (Button) findViewById(R.id.move_button);
        mMoveButton.setOnTouchListener(this::move);
        mContainer = (ViewGroup) findViewById(R.id.component_container);
        addView(context, mContainer);
        setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private float tmpX, tmpY;

    private boolean resize(View v, MotionEvent event) {
        ViewGroup parent = (ViewGroup) v.getParent();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                tmpX = parent.getX();
                tmpY = parent.getY();
                return false;
            case MotionEvent.ACTION_MOVE:
                parent.getLayoutParams().width = (int) Math.abs(event.getRawX() - tmpX);
                parent.getLayoutParams().height = (int) Math.abs(event.getRawY() - tmpY);
                parent.requestLayout();
                return false;
            case MotionEvent.ACTION_UP:
                return true;
            default:
                return false;
        }
    }

    private float touchXOffset, touchYOffset;

    private boolean move(View v, MotionEvent event) {
        if (gestureDetector.onTouchEvent(event)) return true;
        ViewGroup parent = (ViewGroup) v.getParent().getParent();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchXOffset = (int) event.getRawX() - parent.getX();
                touchYOffset = (int) event.getRawY() - parent.getY();
                return false;
            case MotionEvent.ACTION_MOVE:
                parent.setX(event.getRawX() - touchXOffset);
                parent.setY(event.getRawY() - touchYOffset);
                return false;
            case MotionEvent.ACTION_UP:
                return true;
            default:
                return false;
        }
    }

    abstract protected void addView(Context context, ViewGroup container);

    protected void onLongPress() {

    }

    public JSONObject serialize() {
        try {
            return new JSONObject()
                    .put(Globals.Keys.X, mContainer.getX())
                    .put(Globals.Keys.Y, mContainer.getY())
                    .put(Globals.Keys.WIDTH, mContainer.getWidth())
                    .put(Globals.Keys.HEIGHT, mContainer.getHeight());
        } catch (JSONException e) {
            throw new RuntimeException("Error serializing BaseComponent: Threw a JSONException with message \"" + e.getMessage() + "\"");
        }
    }

    public void deserialize(JSONObject obj) {
        try {
            mContainer.setX((float) obj.getDouble(Globals.Keys.X));
            mContainer.setY((float) obj.getDouble(Globals.Keys.Y));
            mContainer.getLayoutParams().width = obj.getInt(Globals.Keys.WIDTH);
            mContainer.getLayoutParams().height = obj.getInt(Globals.Keys.HEIGHT);
            mContainer.requestLayout();
        } catch (JSONException e){
            throw new RuntimeException("Error deserializing BaseComponent: Threw a JSONException with message \"" + e.getMessage() + "\"");
        }
    }
}
