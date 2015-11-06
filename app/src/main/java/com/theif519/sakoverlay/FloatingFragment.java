package com.theif519.sakoverlay;


import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Created by theif519 on 10/29/2015.
 * <p/>
 * The reasoning behind choosing a HashMap for serialization is simply because the HashMap naturally
 * implements the serializable interface, and any attempts I've attempted to create a serializable
 * or parcelable ArrayMap failed horribly (which may be why Android's ArrayMap doesn't support it
 * naturally to begin with). While HashMap may be slow and cumbersome compared to an ArrayMap, it's
 * usage in this case is justified in not only convenience, but also the fact that it saves development time.
 * <p/>
 * Serialization works like this...
 * <p/>
 * -> OnPause() - Signaling user may not come back, hence serialize data.
 * -> serialize() - Serialize base data, mostly X,Y coordinates and WidthxHeight
 * --> serializeExtras() - Called if not null, lets subclasses implement their own.
 * -> write() - Writes serialized data to disk in JSON format.
 * <p/>
 * Deserialization works like this...
 * <p/>
 * -> OnCreate() - Activity first created, need to recreate any serialized fragments.
 * -> read() - Reads any serialized data from disk into a map from JSON format.
 * -> deserializeData() - Creates a new fragment, then passes mapped deserialized data as bundle argument.
 * -> unpack() - Recreates the state of the fragment from scratch.
 */
public class FloatingFragment extends Fragment {

    private boolean mIsDead = false;

    private int width, height, x, y, tmpWidth, tmpHeight, tmpX, tmpY;

    protected String LAYOUT_TAG = "DefaultFragment";

    protected int LAYOUT_ID = R.layout.default_fragment;

    protected static final String X_KEY = "X Coordinate", Y_KEY = "Y Coordinate", MINIMIZED_KEY = "Minimized",
            WIDTH_KEY = "Width", HEIGHT_KEY = "Height", LAYOUT_TAG_KEY = "Layout Tag";

    private static final String TAG = FloatingFragment.class.getSimpleName();

    private View mContentView;

    protected ArrayMap<String, String> mappedData;

    public static FloatingFragment newInstance(int layoutId, String layoutTag) {
        FloatingFragment fragment = new FloatingFragment();
        fragment.LAYOUT_TAG = layoutTag;
        fragment.LAYOUT_ID = layoutId;
        return fragment;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(LAYOUT_ID, container, false);
        mContentView.findViewById(R.id.custom_action_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getFragmentManager().beginTransaction().remove(FloatingFragment.this).commit();
                mIsDead = true;
            }
        });
        mContentView.findViewById(R.id.custom_action_move).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        tmpX = (int) event.getRawX();
                        tmpY = (int) event.getRawY();
                        //Log.d(TAG, "Tapped... (" + x + ", " + y + ") : < " + width + "x" + height + " >");
                        return false;
                    case MotionEvent.ACTION_MOVE:
                        int moveX = Math.min(Math.max((int) event.getRawX() - v.getWidth(), 0), MainActivity.maxX.get() - width);
                        int moveY = Math.min(Math.max((int) event.getRawY() - v.getHeight(), 0), MainActivity.maxY.get() - height);
                        //Log.d(TAG, "Moving... (" + moveX + ", " + moveY + ")");
                        mContentView.setX(moveX);
                        mContentView.setY(moveY);
                        return false;
                    case MotionEvent.ACTION_UP:
                        //Log.d(TAG, "Released... (" + x + ", " + y + ")");
                        return true;
                }
                return false;
            }
        });
        mContentView.findViewById(R.id.custom_action_resize).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        tmpX = (int) event.getRawX();
                        tmpY = (int) event.getRawY();
                        tmpWidth = width;
                        tmpHeight = height;
                        //Log.d(TAG, "Tapped... (" + x + ", " + y + ") : < " + width + "x" + height + " >");
                        return false;
                    case MotionEvent.ACTION_MOVE:
                        // TODO: Make resizing it at negative coordinates rebound
                        int moveX = Math.min(Math.max(Math.abs(tmpWidth + (int) event.getRawX() - tmpX), 0), MainActivity.maxX.get());
                        int moveY = Math.min(Math.max(Math.abs(tmpHeight + (int) event.getRawY() - tmpY), 0), MainActivity.maxY.get());
                        //Log.d(TAG, "Resizing... (" + moveX + "x" + moveY + ")");
                        mContentView.setLayoutParams(new LinearLayout.LayoutParams(moveX, moveY));
                        return false;
                    case MotionEvent.ACTION_UP:
                        //Log.d(TAG, "Released... <" + width + "x" + height + ">");
                        return true;
                }
                return false;
            }
        });
        mContentView.findViewById(R.id.custom_action_minimize).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                minimize(mContentView.getWidth(), mContentView.getHeight());
            }
        });
        getActivity().findViewById(R.id.main_layout).getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (mContentView.getX() + mContentView.getWidth() > MainActivity.maxX.get()) {
                    mContentView.setX(MainActivity.maxX.get() - mContentView.getWidth());
                }
                if (mContentView.getY() + mContentView.getHeight() > MainActivity.maxY.get()) {
                    mContentView.setY(MainActivity.maxY.get() - mContentView.getHeight());
                }
            }
        });
        mContentView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (mContentView.getX() < 0) {
                    mContentView.setX(0);
                }
                if (mContentView.getY() < 0) {
                    mContentView.setY(0);
                }
                if (mContentView.getX() > MainActivity.maxX.get()) {
                    mContentView.setX(MainActivity.maxX.get());
                }
                if (mContentView.getY() > MainActivity.maxY.get()) {
                    mContentView.setY(MainActivity.maxY.get());
                }
                if (mContentView.getVisibility() != View.GONE) {
                    height = mContentView.getHeight();
                    width = mContentView.getWidth();
                    x = (int) mContentView.getX();
                    y = (int) mContentView.getY();
                }
            }
        });
        mContentView.post(new Runnable() {
            @Override
            public void run() {
                if (mappedData != null) unpack();
                setup();
            }
        });
        return mContentView;
    }

    /**
     * Function called to unpack any serialized data that was originally in JSON format. This function
     * should be overriden if there is a need to unpack any extra serialized data, and the very first call
     * MUST be the super.unpack(map), as this ensures that the base data gets unpacked first.
     * <p/>
     * It is safe to call getContentView() and should be used to update the view associated with this fragment.
     */
    public void unpack() {
        x = Integer.parseInt(mappedData.get(X_KEY));
        y = Integer.parseInt(mappedData.get(Y_KEY));
        width = Integer.parseInt(mappedData.get(WIDTH_KEY));
        height = Integer.parseInt(mappedData.get(HEIGHT_KEY));
        boolean isMinimized = Boolean.valueOf(mappedData.get(MINIMIZED_KEY));
        if (isMinimized) {
            mContentView.post(new Runnable() {
                @Override
                public void run() {
                    minimize(width, height);
                }
            });
        }
        mContentView.setX(x);
        mContentView.setY(y);
        mContentView.setLayoutParams(new LinearLayout.LayoutParams(width, height));
        Log.d(TAG, "Minimized: " + isMinimized);
        // If this is override, the subclass's unpack would be done after X,Y,Width, and Height are set.
    }

    private void minimize(int width, int height) {
        final LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.minimized_fragments);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        mContentView.draw(canvas);
        final ImageView view = new ImageView(getActivity());
        view.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 192, 192, false));
        layout.addView(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContentView.setVisibility(View.VISIBLE);
                layout.removeView(view);
            }
        });
        mContentView.setVisibility(View.GONE);
    }

    public ArrayMap<String, String> serialize() {
        ArrayMap<String, String> map = new ArrayMap<>();
        map.put(LAYOUT_TAG_KEY, LAYOUT_TAG);
        map.put(X_KEY, Integer.toString(x));
        map.put(Y_KEY, Integer.toString(y));
        map.put(WIDTH_KEY, Integer.toString(width));
        map.put(HEIGHT_KEY, Integer.toString(height));
        map.put(MINIMIZED_KEY, Boolean.toString(mContentView.getVisibility() == View.GONE));
        return map;
    }

    /**
     * For subclasses to override to setup their own additional needed information.
     */
    public void setup(){

    }

    public String getLayoutTag() {
        return LAYOUT_TAG;
    }

    public View getContentView() {
        return mContentView;
    }

    public boolean isDead() {
        return mIsDead;
    }
}
