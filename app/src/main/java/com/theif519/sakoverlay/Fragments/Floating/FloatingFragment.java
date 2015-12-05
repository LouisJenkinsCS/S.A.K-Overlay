package com.theif519.sakoverlay.Fragments.Floating;


import android.app.Fragment;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.theif519.sakoverlay.Misc.Globals;
import com.theif519.sakoverlay.POD.ViewProperties;
import com.theif519.sakoverlay.R;
import com.theif519.utils.Misc.MeasureTools;

/**
 * Created by theif519 on 10/29/2015.
 * <p/>
 * This is the base and core class for FloatingFragments. A FloatingFragment is what it sounds like, it is
 * a Fragment which acts as if it is attached to a container, I.E Floating. It is bounded within the walls
 * of the main_layout container (beginning of action bar, edges of screen, and the task bar at the bottom
 * however it can be moved at will by the user.
 *
 * FloatingFragments contain their own custom life cycle methods, handle serializing and deserializing their views,
 * as well as unpacking and setting themselves up. Although the presentation I gave already described how
 * everything works, I will summarize it once again here...
 *
 * The custom life-cycles reflect the MainActivity's life cycles and are also triggered by them.
 *
 * MainActivity methods preceded with a (-)
 * FloatingFragments methods preceded with a (+)
 * FloatingFragments custom lifecycle methods preceded with a (~)
 * - OnCreate()
 *  - DeserializeFloatingFragments()
 *  + OnCreateView()
 *    ~ Unpack()
 *        // Will be called if mappedContext passed, as in, if instantiated from the deserializer factory.
 *    ~ Setup()
 *  + OnDestroy()
 *    ~ CleanUp()
 *  - OnPause()
 *    - SerializeFloatingFragments()
 *      ~ Serialize()
 *          // Where each FloatingFragment serializes their data.
 *
 *
 * Relatively simplistic flow. Some life cycle methods may be changed or outright removed later (I.E CleanUp)
 * however as of now they remain.
 *
 * It should be noted that I removed the Rx and multithreading from the onTouch events, as while IMO they
 * sounded like a great and cool idea, and they did actually work at first, it was only because of a really
 * big mistake I made by instantiating a new LayoutParam each time I called resize() or snap(), which is surprisingly
 * often (Seems that ACTION_UP gets called more than once a lot of the time). I found this out by checking out
 * Allocation Tracker and seeing that after 5 minutes of use, it had allocated it (which is 64 bytes of memory
 * in size) over a 1,000 times. Even though the VM cleaned it up, it still would have caused the GC to trigger a LOT.
 */
public class FloatingFragment extends Fragment {

    /*
        I decided to change from having separate instance member variables (x, y, width, height)
        to not only encapsulating them inside of another object, BUT ALSO handle resizing the view
        inside of that object as well. This also makes it possible for other classes to easily manipulate
        this fragment's view (if I make it public or make a getter method of course).

        For example, if I wanted to, say, implement gestured or maybe resizing other views when I resize my own
        while snapped, I would need some elegant of doing so.
     */
    protected ViewProperties mViewProperties;

    /*
        mIsDead - Determines whether or not this instance is dead but not reaped by the garbage collector.
     */
    private boolean mIsDead = false, mIsMaximized = false;

    /*
        Tag used to serialize and deserialize/reconstruct with the factory. This must be changed by sub classes.
     */
    protected String LAYOUT_TAG = "DefaultFragment";

    /*
        These protected variables MUST be changed by subclasses, and is used during the onCreateView() to initialize
        the root view. default_fragment is akin to a 404 message.
     */
    protected int LAYOUT_ID = R.layout.default_fragment, ICON_ID = R.drawable.settings;

    /*
        We keep track of the root view out of convenience.
     */
    private View mContentView;

    /*
        This map should be created when serialize() is called, and also set when created from the factory.
        Unpack relies on this to determine whether or not there is anything to unpack or not.
     */
    protected ArrayMap<String, String> mMappedContext;

    /*
        The current snap state of this view. By bitwise OR'ing different snap states, we can get
        combinations like BOTTOM RIGHT and UPPER LEFT.
     */
    protected int mSnapMask;

    /*
        Snap states.
     */
    protected static final int RIGHT = 1, LEFT = 1 << 1, UPPER = 1 << 2, BOTTOM = 1 << 3;

    /*
        As of now, the task bar at the bottom of the activity isn't very well developed. We only have an
        image button, which the base classes inflates and adds to it in setup(). Very bare-bones, as I said.
     */
    private ImageButton mTaskBarButton;

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(LAYOUT_ID, container, false);
        setupListeners();
        setupTaskItem();
        if (mMappedContext != null && Boolean.valueOf(mMappedContext.get(Globals.Keys.MINIMIZED))) {
            minimize();
        }
        // Ensures that the following methods are called after the view is fully drawn and setup.
        mContentView.post(new Runnable() {
            @Override
            public void run() {
                mViewProperties = new ViewProperties(mContentView);
                if (mMappedContext != null) unpack();
                setup();
            }
        });
        return mContentView;
    }

    /**
     * This is where we setup the bottom task bar's button for this FloatingFragment. Very bare-bones right now.
     */
    private void setupTaskItem() {
        mTaskBarButton = new ImageButton(getActivity());
        mTaskBarButton.setImageResource(ICON_ID);
        mTaskBarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mContentView.getVisibility() == View.INVISIBLE) {
                    mContentView.setVisibility(View.VISIBLE);
                    mContentView.bringToFront();
                } else {
                    mContentView.setVisibility(View.INVISIBLE);
                }
            }
        });
        ((LinearLayout) getActivity().findViewById(R.id.main_task_bar)).addView(mTaskBarButton);
    }

    /**
     * Where we initialize all of our listeners.
     */
    private void setupListeners() {
        mContentView.findViewById(R.id.title_bar_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getFragmentManager().beginTransaction().remove(FloatingFragment.this).commit();
                mIsDead = true;
            }
        });
        mContentView.findViewById(R.id.title_bar_minimize).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                minimize();
            }
        });
        mContentView.findViewById(R.id.title_bar_maximize).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsMaximized) {
                    restoreOriginal();
                } else {
                    maximize();
                }
            }
        });
        mContentView.findViewById(R.id.title_bar_move).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return move(event);
            }
        });
        mContentView.findViewById(R.id.resize_button).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return resize(event);
            }
        });
        getActivity().findViewById(R.id.main_root).getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                boundsCheck();
            }
        });
    }

    private int touchXOffset, touchYOffset, prevX, prevY;

    public boolean move(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mContentView.bringToFront();
                if (mSnapMask != 0 || mIsMaximized) {
                    restoreOriginal();
                }
                touchXOffset = (prevX = (int) event.getRawX()) - (int) mContentView.getX();
                touchYOffset = (prevY = (int) event.getRawY()) - (int) mContentView.getY();
                return false;
            case MotionEvent.ACTION_MOVE:
                int tmpX, tmpY;
                updateSnapMask(prevX, prevY, (tmpX = (int) event.getRawX()), (tmpY = (int) event.getRawY()));
                prevX = tmpX;
                prevY = tmpY;
                int width = mViewProperties.getWidth(), height = mViewProperties.getHeight();
                int scaleDiffX = MeasureTools.scaleDiffToInt(width, Globals.SCALE_X.get()) / 2;
                int scaleDiffY = MeasureTools.scaleDiffToInt(height, Globals.SCALE_Y.get()) / 2;
                int moveX = Math.min(Math.max(tmpX - touchXOffset, -scaleDiffX), Globals.MAX_X.get() - width + scaleDiffX);
                int moveY = Math.min(Math.max(tmpY - touchYOffset, -scaleDiffY), Globals.MAX_Y.get() - height + scaleDiffY);
                mViewProperties.setX(moveX).setY(moveY);
                return false;
            case MotionEvent.ACTION_UP:
                snap();
                return true;
            default:
                return false;
        }
    }

    private int tmpX, tmpY;

    public boolean resize(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mContentView.bringToFront();
                Point p = MeasureTools.getScaledCoordinates(mContentView);
                tmpX = p.x;
                tmpY = p.y;
                return false;
            case MotionEvent.ACTION_MOVE:
                mSnapMask = 0;
                int diffX = (int) event.getRawX() - tmpX;
                int diffY = (int) event.getRawY() - tmpY;
                int scaleDiffX = MeasureTools.scaleDiffToInt(mContentView.getWidth(), Globals.SCALE_X.get());
                int scaleDiffY = MeasureTools.scaleDiffToInt(mContentView.getHeight(), Globals.SCALE_Y.get());
                int width = Math.min(Math.max((int) (diffX / Globals.SCALE_X.get()), 250), Globals.MAX_X.get() + scaleDiffX);
                int height = Math.min(Math.max((int) (diffY / Globals.SCALE_Y.get()), 250), Globals.MAX_Y.get() + scaleDiffY);
                mViewProperties.setWidth(width).setHeight(height);
                return false;
            case MotionEvent.ACTION_UP:
                return true;
            default:
                return false;
        }
    }

    /**
     * Used to snap views to a side of the window if the bitmask is set.
     * <p/>
     * By utilizing a bitmask, it allows me to dynamically snap to not just sides, but also corners as well. It uses bitwise AND'ing
     * to retrieve set bits/attributes. Unlike other operations, such as move() and resize(), changes to the mContentView's
     * size and coordinates are not saved, to easily allow the view to go back to it's original size easily.
     */
    public void snap() {
        if (mSnapMask == 0) return;
        int maxWidth = Globals.MAX_X.get();
        int maxHeight = Globals.MAX_Y.get();
        int width = 0, height = 0, x = 0, y = 0;
        if ((mSnapMask & RIGHT) != 0) {
            width = maxWidth / 2;
            height = maxHeight;
            x = maxWidth / 2;
        }
        if ((mSnapMask & LEFT) != 0) {
            width = maxWidth / 2;
            height = maxHeight;
        }
        if ((mSnapMask & UPPER) != 0) {
            if (width == 0) {
                width = maxWidth;
            }
            height = maxHeight / 2;
        }
        if ((mSnapMask & BOTTOM) != 0) {
            if (width == 0) {
                width = maxWidth;
            }
            height = maxHeight / 2;
            y = maxHeight / 2;
        }
        width = (int) (width / Globals.SCALE_X.get());
        height = (int) (height / Globals.SCALE_Y.get());
        x -= MeasureTools.scaleDiffToInt(width, Globals.SCALE_X.get()) / 2;
        y -= MeasureTools.scaleDiffToInt(height, Globals.SCALE_Y.get()) / 2;
        /*
            Interesting note, but I used to just create a new LayoutParams each time instead of obtaining
            the old one, updating it, and then giving it back (recycling), and I could see the app making a new
            one hundreds of times. Didn't even realize you could recycle it, tbh. This greatly reduces the
            heap fragmentation.
         */
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
    public void updateSnapMask(int oldX, int oldY, int newX, int newY) {
        mSnapMask = 0;
        int transitionX = newX - oldX;
        int transitionY = newY - oldY;
        int snapOffsetX = Globals.MAX_X.get() / 10;
        int snapOffsetY = Globals.MAX_Y.get() / 10;
        if (transitionX > 0 && newX + snapOffsetX >= Globals.MAX_X.get()) {
            mSnapMask |= RIGHT;
        }
        if (transitionX < 0 && newX <= snapOffsetX) {
            mSnapMask |= LEFT;
        }
        if (transitionY < 0 && newY <= snapOffsetY) {
            mSnapMask |= UPPER;
        }
        if (transitionY > 0 && newY + snapOffsetY >= Globals.MAX_Y.get()) {
            mSnapMask |= BOTTOM;
        }
    }

    public ArrayMap<String, String> serialize() {
        ArrayMap<String, String> map = new ArrayMap<>();
        map.put(Globals.Keys.LAYOUT_TAG, LAYOUT_TAG);
        map.put(Globals.Keys.X_COORDINATE, Integer.toString(mViewProperties.getX()));
        map.put(Globals.Keys.Y_COORDINATE, Integer.toString(mViewProperties.getY()));
        map.put(Globals.Keys.Z_COORDINATE, Integer.toString((int) mContentView.getZ()));
        map.put(Globals.Keys.WIDTH, Integer.toString(mViewProperties.getWidth()));
        map.put(Globals.Keys.HEIGHT, Integer.toString(mViewProperties.getHeight()));
        map.put(Globals.Keys.MINIMIZED, Boolean.toString(mContentView.getVisibility() == View.INVISIBLE));
        map.put(Globals.Keys.MAXIMIZED, Boolean.toString(mIsMaximized));
        map.put(Globals.Keys.SNAP_MASK, Integer.toString(mSnapMask));
        return map;
    }

    /**
     * Function called to unpack any serialized data that was originally in JSON format. This function
     * should be overriden if there is a need to unpack any extra serialized data, and the very first call
     * MUST be the super.unpack(), as this ensures that the base data gets unpacked first.
     * <p/>
     * It is safe to call getContentView() and should be used to update the view associated with this fragment.
     */
    protected void unpack() {
        mViewProperties
                .setX(Integer.parseInt(mMappedContext.get(Globals.Keys.X_COORDINATE)))
                .setY(Integer.parseInt(mMappedContext.get(Globals.Keys.Y_COORDINATE)))
                .setWidth(Integer.parseInt(mMappedContext.get(Globals.Keys.WIDTH)))
                .setHeight(Integer.parseInt(mMappedContext.get(Globals.Keys.HEIGHT)));
        mSnapMask = Integer.parseInt(mMappedContext.get(Globals.Keys.SNAP_MASK));
        mIsMaximized = Boolean.parseBoolean(mMappedContext.get(Globals.Keys.MAXIMIZED));
        // If this is override, the subclass's unpack would be done after X,Y,Width, and Height are set.
    }

    private void boundsCheck() {
        if (mContentView.getX() - MeasureTools.scaleDiffToInt(mContentView.getWidth(), Globals.SCALE_X.get()) / 2 < 0) {
            mContentView.setX(-MeasureTools.scaleDiffToInt(mContentView.getWidth(), Globals.SCALE_X.get()) / 2);
        }
        if (mContentView.getY() - MeasureTools.scaleDiffToInt(mContentView.getHeight(), Globals.SCALE_Y.get()) / 2 < 0) {
            mContentView.setY(-MeasureTools.scaleDiffToInt(mContentView.getHeight(), Globals.SCALE_Y.get()) / 2);
        }
        if (mContentView.getX() + MeasureTools.scaleToInt(mContentView.getWidth(), Globals.SCALE_X.get()) > Globals.MAX_X.get()) {
            mContentView.setX(Globals.MAX_X.get() - mContentView.getWidth());
        }
        if (mContentView.getY() + MeasureTools.scaleToInt(mContentView.getHeight(), Globals.SCALE_Y.get()) > Globals.MAX_Y.get()) {
            mContentView.setY(Globals.MAX_Y.get() - mContentView.getHeight());
        }
        if (MeasureTools.scaleToInt(mContentView.getWidth(), Globals.SCALE_X.get()) > Globals.MAX_X.get()) {
            mContentView.setLayoutParams(new FrameLayout.LayoutParams(Globals.MAX_X.get(), mContentView.getHeight()));
        }
        if (MeasureTools.scaleToInt(mContentView.getHeight(), Globals.SCALE_Y.get()) > Globals.MAX_Y.get()) {
            mContentView.setLayoutParams(new FrameLayout.LayoutParams(mContentView.getWidth(), Globals.MAX_Y.get()));
        }
    }

    /**
     * For subclasses to override to setup their own additional needed information. Not abstract as it is not
     * necessary to setup.
     */
    protected void setup() {
        // Release as we no longer need, to prevent memory leak.
        mMappedContext = null;
        if (mViewProperties.getWidth() == 0 || mViewProperties.getHeight() == 0) {
            mViewProperties
                    .setWidth(mContentView.getWidth())
                    .setHeight(mContentView.getHeight());
        }
        if (mSnapMask != 0) {
            snap();
        }
        if (mIsMaximized) {
            maximize();
        }
    }

    /**
     * For any subclasses that need to clean up extra resources, they may do so here.
     */
    protected void cleanUp() {
        ((LinearLayout) getActivity().findViewById(R.id.main_task_bar)).removeView(mTaskBarButton);
    }

    private void maximize() {
        mContentView.setX(-MeasureTools.scaleDiffToInt(mViewProperties.getWidth(), Globals.SCALE_X.get()) / 2);
        mContentView.setY(-MeasureTools.scaleDiffToInt(mViewProperties.getHeight(), Globals.SCALE_Y.get()) / 2);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mContentView.getLayoutParams();
        params.width = (int) (Globals.MAX_X.get() / Globals.SCALE_X.get());
        params.height = (int) (Globals.MAX_Y.get() / Globals.SCALE_Y.get());
        mContentView.setLayoutParams(params);
        mContentView.bringToFront();
        mIsMaximized = true;
    }

    private void restoreOriginal() {
        mViewProperties.update();
        mIsMaximized = false;
        mSnapMask = 0;
    }

    private void minimize() {
        mContentView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cleanUp();
    }

    protected View getContentView() {
        return mContentView;
    }

    public boolean isDead() {
        return mIsDead;
    }

}
