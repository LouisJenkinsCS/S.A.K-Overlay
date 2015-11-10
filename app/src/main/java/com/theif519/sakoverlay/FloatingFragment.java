package com.theif519.sakoverlay;


import android.app.Fragment;
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
 * FloatingFragment is the base class for all floating views, which implements the dynamic movement,
 * resizability, and other on(Touch|Click)Listeners. It must be overriden, as the LAYOUT_ID and
 * LAYOUT_TAG must be set, as well as ICON optionally. It maintains it's state, which is used to serialize
 * to disk, and can readily be deserialized and unpacked to restore it's state from mContext if not null.
 * <p/>
 * Serialization works like this...
 * <p/>
 * -> OnPause(): Signifies that the user may never leave, hence we should serialize all data.
 * -> Serialize(): Maps information used to restore the state of the fragment at a later date.
 * -> JSONSerializer: Flushes the data to disk in JSON format.
 * <p/>
 * Deserialization works like this...
 * <p/>
 * -> OnCreate() - Activity first created, if there is any serialized fragments, recreate them.
 * -> JSONDeserializer: Read the JSON data into mapped context, passing it to the factory.
 * -> FloatingFragmentFactory: Reconstructs the fragment based on LAYOUT_TAG (hence important), passing context to be recreated.
 * -> unpack() - If mContext is not null, unpacks the mContext object to restore overall state.
 *      Added to message queue to ensure mContentView is finished inflating. Should be overriden to allow for specific data.
 * -> setup() - After unpacking, any additional steps should be done here. Hence, presumably the state is complete restored
 *      and if extra steps are needed, can be handled here. Added to message queue so mContentView has finished inflating.
 *      Should be overriden!
 */
public class FloatingFragment extends Fragment {

    /*
        mIsDead: ??? (Seriously, wtf?)

        mMinimizeHint: Helps determine whether or not, when the move event finishes, to minimize this fragment.

        mFinishedMultiTouch: Helps prevent the automatic snapping of views away from the finger released.
     */
    private boolean mIsDead = false, mMinimizeHint = false, mFinishedMultiTouch = false;

    private int width, height, x, y, tmpWidth, tmpHeight, tmpX, tmpY, sidebarDimen;

    private float scaleX = MainActivity.SCALE_X.value, scaleY = MainActivity.SCALE_Y.value;

    protected String LAYOUT_TAG = "DefaultFragment";

    /*
        These protected variables MUST be overriden, and is used during the onCreateView() to initialize
        the root view. default_fragment is akin to a 404 message.
     */
    protected int LAYOUT_ID = R.layout.default_fragment, ICON_ID = R.drawable.settings;

    /*
        These keys are used to maintain consistency with serialization and deserialization of data.
     */
    protected static final String X_KEY = "X Coordinate", Y_KEY = "Y Coordinate", MINIMIZED_KEY = "Minimized",
            WIDTH_KEY = "Width", HEIGHT_KEY = "Height", LAYOUT_TAG_KEY = "Layout Tag";

    private static final String TAG = FloatingFragment.class.getName();

    /*
        We keep track of the root view out of convenience.
     */
    private View mContentView;

    /*
        The state of the fragment is deserialized from this map, and is used to restore the context
        of the fragment after the application and host activity is destroyed.

        This is left protected so that subclasses can override and set the context for their own purposes.
     */
    protected ArrayMap<String, String> mContext;

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
        mContentView.setVisibility(View.INVISIBLE);
        sidebarDimen = (int)getResources().getDimension(R.dimen.activity_main_sidebar_width);
        if(sidebarDimen == 0) sidebarDimen = 55;
        mContentView.findViewById(R.id.title_bar_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getFragmentManager().beginTransaction().remove(FloatingFragment.this).commit();
                mIsDead = true;
            }
        });
        mContentView.findViewById(R.id.title_bar_move).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(mFinishedMultiTouch){
                    return mFinishedMultiTouch = (event.getAction() != MotionEvent.ACTION_UP && event.getAction() != MotionEvent.ACTION_POINTER_UP);
                }
                if(event.getPointerCount() == 1){
                    return handleMove(event);
                } else {
                    return mFinishedMultiTouch = handleResize(event);
                }
            }
        });
        getActivity().findViewById(R.id.main_layout).getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (mContentView.getX() + (mContentView.getWidth() * scaleX) > MainActivity.MAX_X.value) {
                    mContentView.setX(MainActivity.MAX_X.value - mContentView.getWidth());
                }
                if (mContentView.getY() + mContentView.getHeight() * scaleY > MainActivity.MAX_Y.value) {
                    mContentView.setY(MainActivity.MAX_Y.value - mContentView.getHeight());
                }
            }
        });
       mContentView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (mContentView.getX() * scaleX < 0) {
                    mContentView.setX(0);
                }
                if (mContentView.getY() * MainActivity.SCALE_Y.value < 0) {
                    mContentView.setY(0);
                }
                if (mContentView.getX() * MainActivity.SCALE_X.value > MainActivity.MAX_X.value) {
                    mContentView.setX(MainActivity.MAX_X.value);
                }
                if (mContentView.getY() * MainActivity.SCALE_Y.value > MainActivity.MAX_Y.value) {
                    mContentView.setY(MainActivity.MAX_Y.value);
                }
                if (mContentView.getVisibility() != View.INVISIBLE) {
                    height = mContentView.getHeight();
                    width = mContentView.getWidth();
                    x = (int) mContentView.getX();
                    y = (int) mContentView.getY();
                }
            }
        });
        if(mContext != null && Boolean.valueOf(mContext.get(MINIMIZED_KEY))){
            minimize();
        } else mContentView.setVisibility(View.VISIBLE);
        mContentView.post(new Runnable() {
            @Override
            public void run() {
                if (mContext != null) unpack();
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
        x = Integer.parseInt(mContext.get(X_KEY));
        y = Integer.parseInt(mContext.get(Y_KEY));
        width = Integer.parseInt(mContext.get(WIDTH_KEY));
        height = Integer.parseInt(mContext.get(HEIGHT_KEY));
        mContentView.setX(x);
        mContentView.setY(y);
        mContentView.setLayoutParams(new LinearLayout.LayoutParams(width, height));
        // If this is override, the subclass's unpack would be done after X,Y,Width, and Height are set.
    }

    private boolean handleMove(MotionEvent event){
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                tmpX = (int) event.getRawX();
                tmpY = (int) event.getRawY();
                //Log.d(TAG, "Tapped... (" + x + ", " + y + ") : < " + width + "x" + height + " >");
                return false;
            case MotionEvent.ACTION_MOVE:
                if((int)event.getRawX() >= tmpX && (int)event.getRawX() >= MainActivity.MAX_X.value) mMinimizeHint = true;
                else mMinimizeHint = false;
                tmpX = (int) event.getRawX();
                tmpY = (int) event.getRawY();
                width = mContentView.getWidth();
                height = mContentView.getHeight();
                int scaleDiffX = width - (int)(width * scaleX);
                int scaleDiffY = height - (int)(height * scaleY);
                int moveX = Math.min(Math.max(tmpX - (int) (width * scaleX) / 2, -scaleDiffX + sidebarDimen), MainActivity.MAX_X.value - (int)(width * scaleX)-sidebarDimen);
                int moveY = Math.min(Math.max(tmpY - (int) (height * scaleY) / 2, -scaleDiffY), MainActivity.MAX_Y.value - (int)(height * scaleY));
                Log.d(TAG, "Moving... (" + moveX + ", " + moveY + ")\nCoordinates: (" + tmpX + ", " + tmpY + ")\nScaled Coordinates: (" + tmpX * scaleX + ", " + tmpY * scaleY + ")\n" +
                        "Size: <" + width + ", " + height + ">\nScale Size: <" + (int)(width * scaleX) + ", " + (int)(height * scaleY) + ")\nScale Difference: (" + scaleDiffX + ", " + scaleDiffY + ")" );
                mContentView.setX(moveX);
                mContentView.setY(moveY);
                return false;
            case MotionEvent.ACTION_UP:
                if(tmpX >= MainActivity.MAX_X.value && mMinimizeHint) minimize();
                //Log.d(TAG, "Released... (" + x + ", " + y + ")");
                return true;
        }
        return false;
    }

    private int tmpX2, tmpY2;

    private boolean handleResize(MotionEvent event){
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_POINTER_DOWN:
                tmpX = (int) event.getX(0);
                tmpY = (int) event.getY(0);
                tmpWidth = (int)(width * scaleX);
                tmpHeight = (int)(height * scaleY);
                tmpX2 = (int) event.getX(1);
                tmpY2 = (int) event.getY(1);
                return false;
            case MotionEvent.ACTION_MOVE:
                if(event.getPointerCount() != 2) return true;
                // TODO: Make resizing it at negative coordinates rebound
                /*
                    We capture the difference between this and the originally captured coordinates. The reason
                    is that, for example, if the user is pinching, then the pointer difference is negative, meaning
                    that the overall size is shrinking. If it is positive, then the width and height should grow.
                 */
                int firstPointerDiffX = tmpX - (int) event.getX(0);
                int firstPointerDiffY = tmpY - (int) event.getY(0);
                int secondPointerDiffX = tmpX2 - (int) event.getX(1);
                int secondPointerDiffY = tmpY2 - (int) event.getY(1);
                /*
                    Now we take the distance between both X pointers and Y pointers. This will result in how much we should
                    grow or shrink. I.E, if pinching, then both will be negative, so, width + (-X) + (-X2) = new width.
                 */
                int xPointerDist = firstPointerDiffX - secondPointerDiffX;
                int yPointerDist = firstPointerDiffY - secondPointerDiffY;
                int resizeX = Math.min(Math.max((int)(tmpWidth * scaleX) + xPointerDist, 0), MainActivity.MAX_X.value);
                int resizeY = Math.min(Math.max((int)(tmpHeight * scaleY) + yPointerDist, 0), MainActivity.MAX_Y.value);
                mContentView.setLayoutParams(new LinearLayout.LayoutParams(resizeX, resizeY));
                //Log.d(TAG, "Resizing... (" + resizeX + "x" + resizeY + ")");
                return false;
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
                /*float scaleX = (float)mContentView.getWidth() / (float)MainActivity.MAX_X.value;
                float scaleY = (float)mContentView.getHeight() / (float)MainActivity.MAX_Y.value;
                mContentView.setScaleX(scaleX);
                mContentView.setScaleY(scaleY);
                Log.d(TAG, "Scale: (" + scaleX + ", " + scaleY + ")");
                */
                //Log.d(TAG, "Released... <" + width + "x" + height + ">");
                return true;
        }
        return false;
    }

    private void minimize() {
        mContentView.setVisibility(View.INVISIBLE);
        final LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.minimized_fragments);
        final ImageView view = new ImageView(getActivity());
        view.setImageResource(ICON_ID);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMarginEnd(3);
        layoutParams.setMargins(0, 0, 0, 10);
        layout.addView(view, layoutParams);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContentView.setVisibility(View.VISIBLE);
                layout.removeView(view);
            }
        });
    }

    public ArrayMap<String, String> serialize() {
        ArrayMap<String, String> map = new ArrayMap<>();
        map.put(LAYOUT_TAG_KEY, LAYOUT_TAG);
        map.put(X_KEY, Integer.toString(x));
        map.put(Y_KEY, Integer.toString(y));
        map.put(WIDTH_KEY, Integer.toString(width));
        map.put(HEIGHT_KEY, Integer.toString(height));
        map.put(MINIMIZED_KEY, Boolean.toString(mContentView.getVisibility() == View.INVISIBLE));
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
