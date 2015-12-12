package com.theif519.sakoverlay.Fragments.Floating;


import android.app.Fragment;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.theif519.sakoverlay.Controllers.ViewController;
import com.theif519.sakoverlay.Misc.Globals;
import com.theif519.sakoverlay.Misc.MeasureTools;
import com.theif519.sakoverlay.R;
import com.theif519.sakoverlay.Rx.RxBus;
import com.theif519.sakoverlay.Sessions.SessionManager;
import com.theif519.sakoverlay.Views.TouchInterceptorLayout;

/**
 * Created by theif519 on 10/29/2015.
 * <p/>
 * This is the base and core class for FloatingFragments. A FloatingFragment is what it sounds like, it is
 * a Fragment which acts as if it is attached to a container, I.E Floating. It is bounded within the walls
 * of the main_layout container (beginning of action bar, edges of screen, and the task bar at the bottom
 * however it can be moved at will by the user.
 * <p/>
 * FloatingFragments contain their own custom life cycle methods, handle serializing and deserializing their views,
 * as well as unpacking and setting themselves up. Although the presentation I gave already described how
 * everything works, I will summarize it once again here...
 * <p/>
 * The custom life-cycles reflect the MainActivity's life cycles and are also triggered by them.
 * <p/><pre>
 * MainActivity methods preceded with a (-)
 * FloatingFragments methods preceded with a (+)
 * FloatingFragments custom lifecycle methods preceded with a (~)
 * - OnCreate()
 *  - DeserializeFloatingFragments()
 *      + OnCreateView()
 *          ~ Unpack()
 *              // Will be called if mappedContext passed, as in, if instantiated from the deserializer factory.
 *  ~ Setup()
 *      + OnDestroy()
 *          ~ CleanUp()
 * - OnPause()
 *  - SerializeFloatingFragments()
 *      ~ Serialize()
 *          // Where each FloatingFragment serializes their data.
 * </pre><p/>
 * <p/>
 * Relatively simplistic flow. Some life cycle methods may be changed or outright removed later (I.E CleanUp)
 * however as of now they remain.
 * <p/>
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
    protected ViewController mVC;
    /*
        Tag used to serialize and deserialize/reconstruct with the factory. This must be changed by sub classes.
     */
    protected transient String LAYOUT_TAG = "DefaultFragment";

    /*
        These protected variables MUST be changed by subclasses, and is used during the onCreateView() to initialize
        the root view. default_fragment is akin to a 404 message.
     */
    protected transient int LAYOUT_ID = R.layout.default_fragment, ICON_ID = R.drawable.settings;

    protected long id = -1;

    /*
        We keep track of the root view out of convenience.
     */
    private transient TouchInterceptorLayout mContentView;

    /*
        As of now, the task bar at the bottom of the activity isn't very well developed. We only have an
        image button, which the base classes inflates and adds to it in setup(). Very bare-bones, as I said.
     */
    private transient ImageButton mTaskBarButton;
    private transient int touchXOffset, touchYOffset, prevX, prevY, tmpX, tmpY;

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = (TouchInterceptorLayout) inflater.inflate(LAYOUT_ID, container, false);
        setupTaskItem();
        // Ensures that the following methods are called after the view is fully drawn and setup.
        mContentView.post(() -> {
            setupListeners();
            setup();
            RxBus.publish(FloatingFragment.this);
        });
        return mContentView;
    }

    /**
     * This is where we setup the bottom task bar's button for this FloatingFragment. Very bare-bones right now.
     */
    private void setupTaskItem() {
        mTaskBarButton = new ImageButton(getActivity());
        mTaskBarButton.setImageResource(ICON_ID);
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
            getActivity().getFragmentManager().beginTransaction().remove(FloatingFragment.this).commit();
            mVC.addState(ViewController.DEAD);
        });
        mContentView.findViewById(R.id.title_bar_minimize).setOnClickListener(v -> minimize());
        mContentView.findViewById(R.id.title_bar_maximize).setOnClickListener(v -> {
            if (mVC.isStateSet(ViewController.MAXIMIZED)) {
                restoreOriginal();
            } else {
                maximize();
            }
        });
        mContentView.findViewById(R.id.title_bar_move).setOnTouchListener(this::move);
        mContentView.findViewById(R.id.resize_button).setOnTouchListener(this::resize);
        // Whenever the screen configurations has changed, we get alerted through the event bus.
        RxBus.subscribe(Configuration.class)
                .subscribe(configuration -> {
                    boundsCheck();
                    snap();
                    if (mVC.isStateSet(ViewController.MAXIMIZED)) {
                        maximize();
                    }
                });
    }

    /**
     * Handles the MotionEvent for moving the view. Like Resize(), it is rather complication, however
     * it's readability should be made easier with commenting and MeasureTools syntax.
     *
     * @param event MotionEvent.
     * @param v Ignored view, there for Method Reference.
     * @return If event has been completed.
     */
    private boolean move(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // We bring the current view to the front, so the user gets to clearly see which view is being moved.
                mContentView.bringToFront();
                // If the view is currently snapped or maximized, restore it's original size.
                if (mVC.isMaximized() || mVC.isSnapped()) {
                    restoreOriginal();
                    mVC.setCoordinates(
                            (int) event.getRawX() - MeasureTools.scaleDeltaWidth(mContentView),
                            (int) event.getRawY() - MeasureTools.scaleDeltaHeight(mContentView)
                    );
                    Point p = MeasureTools.getScaledCoordinates(mContentView);
                    prevX = touchXOffset = p.x - mVC.getX();
                    prevY = touchYOffset = p.y - mVC.getY();
                } else {
                    // Get the offsets of the user's original touch so the view moves with it.
                    prevX = touchXOffset = (int) event.getRawX() - mVC.getX();
                    prevY = touchYOffset = (int) event.getRawY() - mVC.getY();
                }
                //mVC.setCoordinates(prevX = (int) event.getRawX() - touchXOffset, prevY = (int) event.getRawY() - touchYOffset);
                return false;
            case MotionEvent.ACTION_MOVE:
                int tmpX, tmpY;
                // On each move, we update the snapmask.
                updateSnapMask(prevX, prevY, (tmpX = (int) event.getRawX()), (tmpY = (int) event.getRawY()));
                prevX = tmpX;
                prevY = tmpY;
                mVC.setX(tmpX - touchXOffset).setY(tmpY - touchYOffset);
                return false;
            case MotionEvent.ACTION_UP:
                boundsCheck();
                // We only snap on ACTION_UP to prevent it from annoyingly snapping on everything.
                snap();
                SessionManager.getInstance().updateSession(this);
                return true;
            default:
                return false;
        }
    }

    /**
     * Handles resizing of the view. It used to be extremely complicated, however with MeasureTools, while the complexity
     * has remained the same, the readability hsa increased dramatically.
     *
     * @param event MotionEvent
     * @param v Ignored view, there for Method Reference.
     * @return If event has been handled.
     */
    private boolean resize(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // We bring the current view to the front, so the user gets to clearly see which view is being resized.
                mContentView.bringToFront();
                // This is a convenient way to retrieve the current x,y coordinates
                Point p = MeasureTools.getScaledCoordinates(mContentView);
                tmpX = p.x;
                tmpY = p.y;
                return false;
            case MotionEvent.ACTION_MOVE:
                // Whenever we resize the view, we must reset the snap mask and maximized state.
                mVC.resetState();
                /*
                    Size = (newX, newY) - (origX, origY)
                    This pretty much allows us to directly get the difference of the current touch as the
                    new size.
                */
                int diffX = (int) event.getRawX() - tmpX;
                int diffY = (int) event.getRawY() - tmpY;
                /*
                   TODO: Find a simple way to explain this... and why does not make sense mathematically...
                 */
                int scaleDiffX = MeasureTools.scaleDifferenceWidth(mContentView);
                int scaleDiffY = MeasureTools.scaleDifferenceHeight(mContentView);
                int width = Math.min(Math.max(MeasureTools.scaleInverse(diffX), 250), Globals.MAX_X.get() + scaleDiffX);
                int height = Math.min(Math.max(MeasureTools.scaleInverse(diffY), 250), Globals.MAX_Y.get() + scaleDiffY);
                mVC.setWidth(width).setHeight(height);
                return false;
            case MotionEvent.ACTION_UP:
                boundsCheck();
                SessionManager.getInstance().updateSession(this);
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
    private void snap() {
        if(!mVC.isSnapped()) return;
        int maxWidth = Globals.MAX_X.get();
        int maxHeight = Globals.MAX_Y.get();
        int width = 0, height = 0, x = 0, y = 0;
        if (mVC.isStateSet(ViewController.RIGHT )) {
            width = maxWidth / 2;
            height = maxHeight;
            x = maxWidth / 2;
        }
        if (mVC.isStateSet(ViewController.LEFT)) {
            width = maxWidth / 2;
            height = maxHeight;
        }
        if (mVC.isStateSet(ViewController.UPPER)) {
            if (width == 0) {
                width = maxWidth;
            }
            height = maxHeight / 2;
        }
        if (mVC.isStateSet(ViewController.BOTTOM)) {
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
    private void updateSnapMask(int oldX, int oldY, int newX, int newY) {
        mVC.resetSnap();
        int transitionX = newX - oldX;
        int transitionY = newY - oldY;
        int snapOffsetX = Globals.MAX_X.get() / 10;
        int snapOffsetY = Globals.MAX_Y.get() / 10;
        if (transitionX > 0 && newX + snapOffsetX >= Globals.MAX_X.get()) {
            mVC.addState(ViewController.RIGHT);
        }
        if (transitionX < 0 && newX <= snapOffsetX) {
            mVC.addState(ViewController.LEFT);
        }
        if (transitionY < 0 && newY <= snapOffsetY) {
            mVC.addState(ViewController.UPPER);
        }
        if (transitionY > 0 && newY + snapOffsetY >= Globals.MAX_Y.get()) {
            mVC.addState(ViewController.BOTTOM);
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
            mVC.setX(-MeasureTools.scaleDeltaWidth(mContentView));
        }
        if (p.y < 0) {
            mVC.setY(-MeasureTools.scaleDeltaHeight(mContentView));
        }
        if (p.x + MeasureTools.scaleWidth(mContentView) > Globals.MAX_X.get()) {
            mVC.setX(Globals.MAX_X.get() - MeasureTools.scaleDeltaWidth(mContentView) - MeasureTools.scaleWidth(mContentView));
        }
        if (p.y + MeasureTools.scaleHeight(mContentView) > Globals.MAX_Y.get()) {
            mVC.setY(Globals.MAX_Y.get() - MeasureTools.scaleDeltaHeight(mContentView) - MeasureTools.scaleHeight(mContentView));
        }
        if (MeasureTools.scaleWidth(mContentView) > Globals.MAX_X.get()) {
            mVC.setWidth(MeasureTools.scaleInverse(Globals.MAX_X.get()));
        }
        if (MeasureTools.scaleHeight(mContentView) > Globals.MAX_Y.get()) {
            mVC.setHeight(MeasureTools.scaleInverse(Globals.MAX_Y.get()));
        }
    }

    /**
     * For subclasses to override to setup their own additional needed information. Not abstract as it is not
     * necessary to setup.
     */
    protected void setup() {
        if(mVC == null){
            mVC = new ViewController(mContentView, LAYOUT_TAG, id);
        }
        if (mVC.getWidth() == 0 || mVC.getHeight() == 0) {
            mVC
                    .setWidth(mContentView.getWidth())
                    .setHeight(mContentView.getHeight());
        }
        if (mVC.isSnapped()) {
            snap();
        }
        if (mVC.isMaximized()) {
            maximize();
        }
        if(mVC.isMinimized()){
            minimize();
        }
    }

    /**
     * For any subclasses that need to clean up extra resources, they may do so here.
     */
    protected void cleanUp() {
        ((LinearLayout) getActivity().findViewById(R.id.main_task_bar)).removeView(mTaskBarButton);
    }

    /**
     * Maximizes the view. Note that it does not alter the view's X and Y coordinate through the ViewController
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
        mVC.addState(ViewController.MAXIMIZED);
    }

    /**
     * Used to restore the original view's size and coordinates. It also resets maximization and snap mask
     * to their original states.
     */
    private void restoreOriginal() {
        mVC.update();
        mVC.resetState();
    }

    /**
     * Used to minimize the view. It doesn't do much right now, but it works.
     */
    private void minimize() {
        mContentView.setVisibility(View.INVISIBLE);
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

    /**
     * Whether or not this FloatingFragment is dead. Majority of times it will be garbage collected, but you never know.
     *
     * @return If is dead.
     */
    public boolean isDead() {
        return mVC.isStateSet(ViewController.DEAD);
    }

    public ViewController getViewProperties(){
        return mVC;
    }

    public String getLayoutTag() {
        return LAYOUT_TAG;
    }

    public long getUniqueId(){
        return id;
    }

    public void setUniqueId(long id){

    }

}
