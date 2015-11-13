package com.theif519.sakoverlay.FloatingFragments;


import android.app.Fragment;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.ArrayMap;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.theif519.sakoverlay.Activities.MainActivity;
import com.theif519.sakoverlay.R;

import java.util.ArrayList;

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
 * Added to message queue to ensure mContentView is finished inflating. Should be overriden to allow for specific data.
 * -> setup() - After unpacking, any additional steps should be done here. Hence, presumably the state is complete restored
 * and if extra steps are needed, can be handled here. Added to message queue so mContentView has finished inflating.
 * Should be overriden!
 */
public class FloatingFragment extends Fragment {

    /*
        mIsDead: ??? (Seriously, wtf?)

        mMinimizeHint: Helps determine whether or not, when the move event finishes, to minimize this fragment.

        mFinishedMultiTouch: Helps prevent the automatic snapping of views away from the finger released.
     */
    private boolean mIsDead = false, mMinimizeHint = false, mFinishedMultiTouch = false, mIsTransparent = false;

    /*
        Required to keep track of the position and size of the view between each onTouchEvent.
     */
    private int width, height, x, y, tmpWidth, tmpHeight, tmpX, tmpY, sidebarDimen;

    /*
        Shortcut to retrieve the float to scale all views and view elements by.
     */
    private float scaleX = MainActivity.getScaleX(), scaleY = MainActivity.getScaleY();

    /*
        Keeps track of the tag to be
     */
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

    /*
        These constants are for menu options, to maintain consistency.
     */
    protected static final String TRANSPARENCY_TOGGLE_OPTION = "Transparency Toggle", BRING_TO_FRONT_OPTION = "Bring to Front";

    /*
        Convenient tag.
     */
    private static final String TAG = FloatingFragment.class.getName();

    /*
        The list of options to be shown in the list view when the options menu is to be displayed
        If left null, it will create a new one, and have the default options available, else the default
        options are appended to the list view.
     */
    protected ArrayList<String> mOptions;

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
        setupGlobals();
        setupListeners();
        if (mContext != null && Boolean.valueOf(mContext.get(MINIMIZED_KEY))) {
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
     * Where we initialize, retrieve and setup our global variables.
     */
    private void setupGlobals(){
        sidebarDimen = (int) getResources().getDimension(R.dimen.activity_main_sidebar_width);
        if(mOptions == null){
            mOptions = new ArrayList<String>();
        }
        mOptions.add(TRANSPARENCY_TOGGLE_OPTION);
        mOptions.add(BRING_TO_FRONT_OPTION);
    }

    /**
     * Where we initialize all of our listeners.
     */
    private void setupListeners(){
        mContentView.findViewById(R.id.title_bar_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getFragmentManager().beginTransaction().remove(FloatingFragment.this).commit();
                mIsDead = true;
            }
        });
        /*
            This listener handles moving and resizing the floating fragment. How it is handled depends
            on the current action of the touch event, and how many fingers are used.

            Currently, one finger means move, two fingers means resizing.
         */
        mContentView.findViewById(R.id.title_bar_move).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                /*
                    Due to the issue of flinging when letting go of one pointer, I disable any touch events
                    after one of the pointers is let up. This ensures that moving is very smooth and consistent.
                 */
                if (mFinishedMultiTouch) {
                    return mFinishedMultiTouch = (event.getAction() != MotionEvent.ACTION_UP && event.getAction() != MotionEvent.ACTION_POINTER_UP);
                }
                if (event.getPointerCount() == 1) {
                    return handleMove(event);
                } else {
                    return mFinishedMultiTouch = handleResize(event);
                }
            }
        });
        /*
            This adds an observer to the layout all floating fragments belong to, to ensure that on orientation change
            that all floating fragments are within the boundaries of this app, making changes if need be.
         */
        getActivity().findViewById(R.id.main_layout).getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (mContentView.getX() + scaleToInt(mContentView.getWidth(), scaleX) > MainActivity.getMaxX()) {
                    mContentView.setX(MainActivity.getMaxX() - mContentView.getWidth());
                }
                if (mContentView.getY() + scaleToInt(mContentView.getHeight(), scaleY) > MainActivity.getMaxY()) {
                    mContentView.setY(MainActivity.getMaxY() - mContentView.getHeight());
                }
                if (scaleToInt(mContentView.getWidth(), scaleX) > MainActivity.getMaxX()) {
                    mContentView.setLayoutParams(new LinearLayout.LayoutParams(MainActivity.getMaxX(), mContentView.getHeight()));
                }
                if (scaleToInt(mContentView.getHeight(), scaleY) > MainActivity.getMaxY()) {
                    mContentView.setLayoutParams(new LinearLayout.LayoutParams(mContentView.getWidth(), MainActivity.getMaxY()));
                }
            }
        });
        mContentView.findViewById(R.id.title_bar_options).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final PopupWindow window = new PopupWindow(null, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);;
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, mOptions);
                ListView listView = new ListView(getActivity());
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        onItemSelected((String) parent.getItemAtPosition(position));
                        window.dismiss();
                    }
                });
                listView.setScaleX(scaleX);
                listView.setScaleY(scaleY);
                Point p = getScaledCoordinates();
                window.setContentView(listView);
                window.setWidth(measureContentWidth(adapter));
                window.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.transparent_fragment)));
                window.showAtLocation(getActivity().findViewById(R.id.main_layout), Gravity.NO_GRAVITY,
                        p.x + scaleToInt(mContentView.findViewById(R.id.title_bar_options).getWidth(), scaleX),
                        p.y + scaleToInt(mContentView.findViewById(R.id.title_bar_options).getHeight(), scaleY));
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
                if (mContentView.getX() > MainActivity.getMaxX()) {
                    mContentView.setX(MainActivity.getMaxX());
                }
                if (mContentView.getY() > MainActivity.getMaxY()) {
                    mContentView.setY(MainActivity.getMaxY());
                }
                if (mContentView.getVisibility() != View.INVISIBLE) {
                    height = mContentView.getHeight();
                    width = mContentView.getWidth();
                    x = (int) mContentView.getX();
                    y = (int) mContentView.getY();
                }
            }
        });
    }

    /**
     * Function called to unpack any serialized data that was originally in JSON format. This function
     * should be overriden if there is a need to unpack any extra serialized data, and the very first call
     * MUST be the super.unpack(map), as this ensures that the base data gets unpacked first.
     * <p/>
     * It is safe to call getContentView() and should be used to update the view associated with this fragment.
     */
    protected void unpack() {
        x = Integer.parseInt(mContext.get(X_KEY));
        y = Integer.parseInt(mContext.get(Y_KEY));
        width = Integer.parseInt(mContext.get(WIDTH_KEY));
        height = Integer.parseInt(mContext.get(HEIGHT_KEY));
        mContentView.setX(x);
        mContentView.setY(y);
        mContentView.setLayoutParams(new LinearLayout.LayoutParams(width, height));
        // If this is override, the subclass's unpack would be done after X,Y,Width, and Height are set.
    }

    private int measureContentWidth(ListAdapter listAdapter) {
        ViewGroup mMeasureParent = null;
        int maxWidth = 0;
        View itemView = null;
        int itemType = 0;

        final ListAdapter adapter = listAdapter;
        final int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int count = adapter.getCount();
        for (int i = 0; i < count; i++) {
            final int positionType = adapter.getItemViewType(i);
            if (positionType != itemType) {
                itemType = positionType;
                itemView = null;
            }

            if (mMeasureParent == null) {
                mMeasureParent = new FrameLayout(getActivity());
            }

            itemView = adapter.getView(i, itemView, mMeasureParent);
            itemView.measure(widthMeasureSpec, heightMeasureSpec);

            final int itemWidth = itemView.getMeasuredWidth();

            if (itemWidth > maxWidth) {
                maxWidth = itemWidth;
            }
        }

        return maxWidth;
    }

    private int initialX, initialY;

    private boolean handleMove(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialX = (int) event.getRawX() - (int) mContentView.getX();
                initialY = (int) event.getRawY() - (int) mContentView.getY();
                //Log.d(TAG, "Tapped... (" + x + ", " + y + ") : < " + width + "x" + height + " >");
                return false;
            case MotionEvent.ACTION_MOVE:
                if ((int) event.getRawX() >= tmpX && (int) event.getRawX() >= MainActivity.getMaxX())
                    mMinimizeHint = true;
                else mMinimizeHint = false;
                tmpX = (int) event.getRawX();
                tmpY = (int) event.getRawY();
                width = mContentView.getWidth();
                height = mContentView.getHeight();
                int scaleDiffX = (width - (int) (width * scaleX)) / 2;
                int scaleDiffY = (height - (int) (height * scaleY)) / 2;
                int moveX = Math.min(Math.max(tmpX - initialX, -scaleDiffX), MainActivity.getMaxX() - width + scaleDiffX);
                int moveY = Math.min(Math.max(tmpY - initialY, -scaleDiffY), MainActivity.getMaxY() - height + scaleDiffY);
                /*Log.d(TAG, "Moving... (" + moveX + ", " + moveY + ")\nCoordinates: (" + tmpX + ", " + tmpY + ")\nScaled Coordinates: (" + tmpX * scaleX + ", " + tmpY * scaleY + ")\n" +
                        "Size: <" + width + ", " + height + ">\nScale Size: <" + (int)(width * scaleX) + ", " + (int)(height * scaleY) + ")\nScale Difference: (" + scaleDiffX + ", " + scaleDiffY + ")" );
                */
                mContentView.setX(moveX);
                mContentView.setY(moveY);
                return false;
            case MotionEvent.ACTION_UP:
                if (tmpX >= MainActivity.getMaxX() && mMinimizeHint) minimize();
                //Log.d(TAG, "Released... (" + x + ", " + y + ")");
                return true;
        }
        return false;
    }

    private int tmpX2, tmpY2;

    private boolean handleResize(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_POINTER_DOWN:
                tmpX = (int) event.getX(0);
                tmpY = (int) event.getY(0);
                tmpWidth = (int) (width * scaleX);
                tmpHeight = (int) (height * scaleY);
                tmpX2 = (int) event.getX(1);
                tmpY2 = (int) event.getY(1);
                return false;
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() != 2) return true;
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
                int resizeX = Math.min(Math.max(tmpWidth + xPointerDist, 0), MainActivity.getMaxX());
                int resizeY = Math.min(Math.max(tmpHeight + yPointerDist, 0), MainActivity.getMaxY());
                mContentView.setLayoutParams(new LinearLayout.LayoutParams(resizeX, resizeY));
                //Log.d(TAG, "Resizing... (" + resizeX + "x" + resizeY + ")");
                return false;
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
                //Log.d(TAG, "Released... <" + width + "x" + height + ">");
                return true;
        }
        return false;
    }

    private float scaleDiff(float num, float ratio) {
        return num - (num * ratio);
    }

    private float scale(float num, float ratio) {
        return num * ratio;
    }

    private int scaleDiffToInt(float num, float ratio) {
        return (int) scaleDiff(num, ratio);
    }

    private int scaleToInt(float num, float ratio) {
        return (int) scale(num, ratio);
    }

    private Point getScaledCoordinates(){
        return new Point((int) mContentView.getX() + scaleDiffToInt(mContentView.getWidth(), scaleX)/2,
                (int) mContentView.getY() + scaleDiffToInt(mContentView.getHeight(), scaleY)/2);
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
     * For subclasses to override to setup their own additional needed information. Not abstract as it is not
     * necessary to setup.
     */
    protected void setup() {

    }

    public String getLayoutTag() {
        return LAYOUT_TAG;
    }

    protected View getContentView() {
        return mContentView;
    }

    public boolean isDead() {
        return mIsDead;
    }

    public void onItemSelected(String string){
        switch(string){
            case TRANSPARENCY_TOGGLE_OPTION:
                if(mIsTransparent = !mIsTransparent){
                    mContentView.findViewById(R.id.title_bar_root).setBackgroundColor(getResources().getColor(android.R.color.transparent));
                } else {
                    mContentView.findViewById(R.id.title_bar_root).setBackgroundColor(getResources().getColor(R.color.black));
                }
                break;
            case BRING_TO_FRONT_OPTION:
                mContentView.bringToFront();
                break;
        }
    }

}
