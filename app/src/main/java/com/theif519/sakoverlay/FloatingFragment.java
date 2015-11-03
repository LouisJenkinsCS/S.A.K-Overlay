package com.theif519.sakoverlay;


import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by theif519 on 10/29/2015.
 *
 * The reasoning behind choosing a HashMap for serialization is simply because the HashMap naturally
 * implements the serializable interface, and any attempts I've attempted to create a serializable
 * or parcelable ArrayMap failed horribly (which may be why Android's ArrayMap doesn't support it
 * naturally to begin with). While HashMap may be slow and cumbersome compared to an ArrayMap, it's
 * usage in this case is justified in not only convenience, but also the fact that it saves development time.
 *
 * Serialization works like this...
 *
 * -> OnPause() - Signaling user may not come back, hence serialize data.
 * -> serialize() - Serialize base data, mostly X,Y coordinates and WidthxHeight
 * --> serializeExtras() - Called if not null, lets subclasses implement their own.
 * -> write() - Writes serialized data to disk in JSON format.
 *
 * Deserialization works like this...
 *
 * -> OnCreate() - Activity first created, need to recreate any serialized fragments.
 * -> read() - Reads any serialized data from disk into a map from JSON format.
 * -> deserializeData() - Creates a new fragment, then passes mapped deserialized data as bundle argument.
 * -> unpack() - Recreates the state of the fragment from scratch.
 */
public class FloatingFragment extends Fragment {

    private boolean mIsDead = false;

    private int originalWidth, originalHeight, originalX, originalY;

    private String mTitle, mLayoutTag;

    protected static final String TITLE_KEY = "Title", LAYOUT_ID_KEY = "Layout Id",
            X_KEY = "X Coordinate", Y_KEY = "Y Coordinate",
            WIDTH_KEY = "Width", HEIGHT_KEY = "Height", LAYOUT_TAG_KEY = "Layout Tag";

    private static final String TAG = FloatingFragment.class.getSimpleName();

    private View mContentView;

    protected ArrayMap<String, String> mappedData;

    public static FloatingFragment newInstance(int layoutId, String layoutTag, String title){
        FloatingFragment fragment = new FloatingFragment();
        Bundle args = new Bundle();
        args.putString(TITLE_KEY, title);
        args.putInt(LAYOUT_ID_KEY, layoutId);
        args.putString(LAYOUT_TAG_KEY, layoutTag);
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        if(args == null) return inflater.inflate(R.layout.default_fragment, container, false);
        mLayoutTag = args.getString(LAYOUT_TAG_KEY);
        mTitle = args.getString(TITLE_KEY);
        mContentView = inflater.inflate(args.getInt(LAYOUT_ID_KEY), container, false);
        ((TextView) mContentView.findViewById(R.id.custom_action_title)).setText(mTitle);
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
                        originalWidth = mContentView.getWidth();
                        originalHeight = mContentView.getHeight();
                        originalX = (int) event.getRawX();
                        originalY = (int) event.getRawY();
                        //Log.d(TAG, "Tapped... (" + originalX + ", " + originalY + ") : < " + originalWidth + "x" + originalHeight + " >");
                        return false;
                    case MotionEvent.ACTION_MOVE:
                        int moveX = Math.min(Math.max((int) event.getRawX() - v.getWidth(), 0), MainActivity.maxX.get() - originalWidth);
                        int moveY = Math.min(Math.max((int) event.getRawY() - v.getHeight(), 0), MainActivity.maxY.get() - originalHeight);
                        //Log.d(TAG, "Moving... (" + moveX + ", " + moveY + ")");
                        mContentView.setX(moveX);
                        mContentView.setY(moveY);
                        originalX = moveX;
                        originalY = moveY;
                        return false;
                    case MotionEvent.ACTION_UP:
                        //Log.d(TAG, "Released... (" + originalX + ", " + originalY + ")");
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
                        originalX = (int) event.getRawX();
                        originalY = (int) event.getRawY();
                        originalWidth = mContentView.getWidth();
                        originalHeight = mContentView.getHeight();
                        //Log.d(TAG, "Tapped... (" + originalX + ", " + originalY + ") : < " + originalWidth + "x" + originalHeight + " >");
                        return false;
                    case MotionEvent.ACTION_MOVE:
                        // TODO: Make resizing it at negative coordinates rebound
                        int moveX = Math.min(Math.max(Math.abs(originalWidth + (int) event.getRawX() - originalX), 0), MainActivity.maxX.get());
                        int moveY = Math.min(Math.max(Math.abs(originalHeight + (int) event.getRawY() - originalY), 0), MainActivity.maxY.get());
                        //Log.d(TAG, "Resizing... (" + moveX + "x" + moveY + ")");
                        mContentView.setLayoutParams(new LinearLayout.LayoutParams(moveX, moveY));
                        return false;
                    case MotionEvent.ACTION_UP:
                        //Log.d(TAG, "Released... <" + originalWidth + "x" + originalHeight + ">");
                        return true;
                }
                return false;
            }
        });
        mContentView.findViewById(R.id.custom_action_minimize).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.minimized_fragments);
                Bitmap bitmap = Bitmap.createBitmap(mContentView.getWidth(), mContentView.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                mContentView.draw(canvas);
                final ImageView view = new ImageView(getActivity());
                view.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 200, 200, false));
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
                if(mContentView.getX() < 0){
                    mContentView.setX(0);
                }
                if(mContentView.getY() < 0){
                    mContentView.setY(0);
                }
                if(mContentView.getX() > MainActivity.maxX.get()){
                    mContentView.setX(MainActivity.maxX.get());
                }
                if(mContentView.getY() > MainActivity.maxY.get()){
                    mContentView.setY(MainActivity.maxY.get());
                }
            }
        });
        if(mappedData != null) unpack();
        return mContentView;
    }

    /**
     * Function called to unpack any serialized data that was originally in JSON format. This function
     * should be overriden if there is a need to unpack any extra serialized data, and the very first call
     * MUST be the super.unpack(map), as this ensures that the base data gets unpacked first.
     *
     * It is safe to call getContentView() and should be used to update the view associated with this fragment.
     */
    public void unpack(){
        mContentView.setX(Integer.parseInt(mappedData.get(X_KEY)));
        mContentView.setY(Integer.parseInt(mappedData.get(Y_KEY)));
        mContentView.setLayoutParams(new LinearLayout.LayoutParams(
                Integer.parseInt(mappedData.get(WIDTH_KEY)), Integer.parseInt(mappedData.get(HEIGHT_KEY))
        ));
        // If this is override, the subclass's unpack would be done after X,Y,Width, and Height are set.
    }


    public ArrayMap<String, String> serialize() {
        ArrayMap<String, String> map = new ArrayMap<>();
        map.put(TITLE_KEY, mTitle);
        map.put(LAYOUT_TAG_KEY, mLayoutTag);
        map.put(X_KEY, Integer.toString((int) mContentView.getX()));
        map.put(Y_KEY, Integer.toString((int) mContentView.getY()));
        map.put(WIDTH_KEY, Integer.toString(mContentView.getWidth()));
        map.put(HEIGHT_KEY, Integer.toString(mContentView.getHeight()));
        return map;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getLayoutTag() {
        return mLayoutTag;
    }

    public void setLayoutTag(String mLayoutTag) {
        this.mLayoutTag = mLayoutTag;
    }

    public View getContentView() {
        return mContentView;
    }

    public boolean isDead() {
        return mIsDead;
    }
}
