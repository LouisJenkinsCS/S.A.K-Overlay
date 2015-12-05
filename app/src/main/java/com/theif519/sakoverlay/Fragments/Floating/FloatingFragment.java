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
import com.theif519.sakoverlay.POD.TouchEventInfo;
import com.theif519.sakoverlay.POD.ViewProperties;
import com.theif519.sakoverlay.R;
import com.theif519.utils.Misc.MeasureTools;

import java.util.ArrayList;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by theif519 on 10/29/2015.
 * <p/>
 * FloatingFragment is the base class for all floating views, which implements the dynamic movement,
 * resizability, and other on(Touch|Click)Listeners. It must be overriden, as the LAYOUT_ID and
 * LAYOUT_TAG must be set, as well as ICON optionally. It maintains it's state, which is used to serialize
 * to disk, and can readily be deserialized and unpacked to restore it's state from mMappedContext if not null.
 * <p/>
 * Serialization works like this...
 * <p/>
 * -> OnPause(): Signifies that the user may never leave, hence we should serialize all data.
 * -> Serialize(): Maps information used to restore the state of the fragment at a later date.
 * -> FloatingFragmentSerializer: Flushes the data to disk in JSON format.
 * <p/>
 * Deserialization works like this...
 * <p/>
 * -> OnCreate() - Activity first created, if there is any serialized fragments, recreate them.
 * -> FloatingFragmentDeserializer: Read the JSON data into mapped context, passing it to the factory.
 * -> FloatingFragmentFactory: Reconstructs the fragment based on LAYOUT_TAG (hence important), passing context to be recreated.
 * -> unpack() - If mMappedContext is not null, unpacks the mMappedContext object to restore overall state.
 * Added to message queue to ensure mContentView is finished inflating. Should be overriden to allow for specific data.
 * -> setup() - After unpacking, any additional steps should be done here. Hence, presumably the state is complete restored
 * and if extra steps are needed, can be handled here. Added to message queue so mContentView has finished inflating.
 * Should be overriden!
 */
public class FloatingFragment extends Fragment {

    protected ViewProperties mViewProperties;

    /*
        Used to signify that this fragment is dead or is about to be, but Garbage Collector hasn't collected us yet.
     */
    private boolean mIsDead = false, mIsMaximized = false;

    /*
        Tag used to serialize and deserialize/reconstruct with the factory. This must be overriden.
     */
    protected String LAYOUT_TAG = "DefaultFragment";

    /*
        These protected variables MUST be overriden, and is used during the onCreateView() to initialize
        the root view. default_fragment is akin to a 404 message.
     */
    protected int LAYOUT_ID = R.layout.default_fragment, ICON_ID = R.drawable.settings;

    /*
        Represents Menu Options.
     */
    protected static final String TRANSPARENCY_TOGGLE = "Transparency Toggle", BRING_TO_FRONT = "Bring to Front";

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
    protected ArrayMap<String, String> mMappedContext;

    protected int mSnapMask;

    private ImageButton mTaskBarButton;

    public static FloatingFragment newInstance(int layoutId, String layoutTag) {
        FloatingFragment fragment = new FloatingFragment();
        fragment.LAYOUT_TAG = layoutTag;
        fragment.LAYOUT_ID = layoutId;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(LAYOUT_ID, container, false);
        setupListeners();
        setupReactive();
        setupTaskItem();
        if (mMappedContext != null && Boolean.valueOf(mMappedContext.get(Globals.Keys.MINIMIZED))) {
            minimize();
        }
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
    }

    /**
     * In this helper, we create implement the wonders of RxJava and affiliated sub-libraries, to
     * make handling touch events, I.E moving and resizing views, more elegant in an approach and more efficient.
     * <p/>
     * Documentation below will provide a walk-through as to what each class does, their purpose, and what
     * they do in the grand scheme of things, and of course I will contrast to the complexity that a normal
     * onTouchListener couldn't even begin to do as elegantly.
     * <p/>
     * Some key explanations to help with understanding the code below...
     * <p/>
     * An Observer, is what it sounds like. It is an object which observes some event. You can think of
     * an onTouchListener as an Observer, as it "Listens" for a touch, and the Motion Event itself the
     * Observable (which I will explain in a bit). An Observer can be anything, so long as it emits an
     * "item", or an event. The Observer also handles the operations, I.E Map, Filter, etc., allowing
     * it asynchronicity and elegance. To expand on the onTouchListener comparison, the onTouchListener feels
     * as asynchronous as the Observer does, but only can the Observer truly be considered asynchronous. The
     * OnTouch event is dispatched by and on the main looper, while an Observer can be dispatched on any thread.
     * <p/>
     * An Observable is also what it sounds like. It is an object which can be observed. Like the above
     * analogy, the onTouch event is the Observable. The main thread polls for new events, on observable
     * events. The Observable "emits" items, or events, like a touch event, while an Observer listens for
     * these items. The observable mostly calls the callbacks onNext(), meaning for the next event,
     * onError(), meaning if an error or an exception is thrown and not handled, or onComplete(), for
     * when the Observable has finished broadcasting its events. An Observer can Subscribe to an
     * Observable, and when the Observable emits these items, the Observer's operators handles transforming
     * it however it wants, and if it decides to handle the items emitted, it can subscribe(), which will
     * call the Observables onSubscribe() if declared. I.E, when a new Observer subscribes to an Observable,
     * say the main thread wants to query from a database, it may onSubscribe() and query for any immediate returns
     * or block until one is ready, upon which the main thread (Observer) subscribes() and does something with
     * said data.
     * <p/>
     * I understand this may be longwinded and overwhelming for a simple Javadoc, but for a more
     * thorough explanation, see RxJava's documentation and tutorials.
     */
    private void setupReactive() {
        observableFromTouch(mContentView.findViewById(R.id.title_bar_move))
                .observeOn(AndroidSchedulers.mainThread()) // The Observer, the UI Thread, waits for processed events containing the information needed to manipulate views.
                .subscribeOn(Schedulers.computation()) // The Observable's events are processed on a computational thread, which is a non I/O-Bound thread. Perfect for this.
                .map(new Func1<MotionEvent, Void>() { // We process each MotionEvent in a background thread, then dispose of it.
                    @Override
                    public Void call(MotionEvent event) {
                        /*
                            I decided, from a design point of view, it would be better to encapsulate all View manipulations from an
                            inner class. Due to this, I do not need to return anything, and the added benefit is that (presumably) we
                            relinquish it's reference to be garbage collected or recycled.
                         */
                        move(event);
                        return null;
                    }
                })
                .subscribe(new Action1<Void>() { // Where the UI Thread gets called.
                    @Override
                    public void call(Void none) {
                        mViewProperties.update();
                    }
                });
        getActivity().findViewById(R.id.main_root).getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                boundsCheck();
            }
        });
        observableFromTouch(mContentView.findViewById(R.id.resize_button))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.computation())
                .map(new Func1<MotionEvent, Void>() {
                    @Override
                    public Void call(MotionEvent event) {
                        resize(event);
                        return null;
                    }
                })
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void none) {
                        mViewProperties.update();
                    }
                });
    }

    /**
     * Used to create an Observable from a onTouchListener event.
     * <p/>
     * A PublishSubject acts as both an Observable and an Observer. Pretty much, it serves as a
     * proxy between Observers and Observables. It abstracts the need to subscribe and create an observable of
     * everything by just calling onNext() to emit an event, as here onNext() is called to directly
     * send the MotionEvent to any listeners, and subscribe() determines how it handles the onTouch event.
     *
     * @param v View
     * @return An Observable that gets published to from an onTouchListener handler.
     */
    private Observable<MotionEvent> observableFromTouch(View v) {
        final PublishSubject<MotionEvent> onTouch = PublishSubject.create();
        v.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Whenever we receive a touch event, re-emit the event. Effectively turns it into an observer.
                if (onTouch.hasObservers()) {
                    onTouch.onNext(event);
                    return true;
                } else {
                    return false;
                }
            }
        });
        return onTouch.asObservable();
    }

    private int touchXOffset, touchYOffset, prevX, prevY;

    public void move(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mContentView.bringToFront();
                if (mSnapMask != 0 || mIsMaximized) {
                    restoreOriginal();
                }
                touchXOffset = (prevX = (int) event.getRawX()) - (int) mContentView.getX();
                touchYOffset = (prevY = (int) event.getRawY()) - (int) mContentView.getY();
                break;
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
                break;
            case MotionEvent.ACTION_UP:
                snap();
                break;
        }
    }

    private int tmpX, tmpY;

    public void resize(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mContentView.bringToFront();
                Point p = MeasureTools.getScaledCoordinates(mContentView);
                tmpX = p.x;
                tmpY = p.y;
                break;
            case MotionEvent.ACTION_MOVE:
                mSnapMask = 0;
                int diffX = (int) event.getRawX() - tmpX;
                int diffY = (int) event.getRawY() - tmpY;
                int scaleDiffX = MeasureTools.scaleDiffToInt(mContentView.getWidth(), Globals.SCALE_X.get());
                int scaleDiffY = MeasureTools.scaleDiffToInt(mContentView.getHeight(), Globals.SCALE_Y.get());
                int width = Math.min(Math.max((int) (diffX / Globals.SCALE_X.get()), 250), Globals.MAX_X.get() + scaleDiffX);
                int height = Math.min(Math.max((int) (diffY / Globals.SCALE_Y.get()), 250), Globals.MAX_Y.get() + scaleDiffY);
                mViewProperties.setWidth(width).setHeight(height);
                break;
        }
    }

    /**
     * Used to snap views to a side of the window if the bitmask is set. It should be noted that RxJava's schedulers are guaranteed
     * to perform tasks sequentially, hence, I have no need to worry about race conditions while performing operations on the
     * bitmask.
     *
     * By utilizing a bitmask, it allows me to dynamically snap to not just sides, but also corners as well. It uses bitwise AND'ing
     * to retrieve set bits/attributes. Unlike other operations, such as move() and resize(), changes to the mContentView's
     * size and coordinates are not saved, to easily allow the view to go back to it's original size easily.
     */
    public void snap() {
        if(mSnapMask == 0) return;
        int maxWidth = Globals.MAX_X.get();
        int maxHeight = Globals.MAX_Y.get();
        int width = 0, height = 0, x = 0, y = 0;
        if ((mSnapMask & TouchEventInfo.RIGHT) != 0) {
            width = maxWidth / 2;
            height = maxHeight;
            x = maxWidth / 2;
        }
        if ((mSnapMask & TouchEventInfo.LEFT) != 0) {
            width = maxWidth / 2;
            height = maxHeight;
        }
        if ((mSnapMask & TouchEventInfo.UPPER) != 0) {
            if (width == 0) {
                width = maxWidth;
            }
            height = maxHeight / 2;
        }
        if ((mSnapMask & TouchEventInfo.BOTTOM) != 0) {
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
     * Updates the bitmask used to maintain the current snap direction of the current view. It should be
     * noted that as RxJava's schedulers perform tasks sequentially, that updateSnapMask() and snap() do not
     * conflict with each other in any way.
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
            mSnapMask |= TouchEventInfo.RIGHT;
        }
        if (transitionX < 0 && newX <= snapOffsetX) {
            mSnapMask |= TouchEventInfo.LEFT;
        }
        if (transitionY < 0 && newY <= snapOffsetY) {
            mSnapMask |= TouchEventInfo.UPPER;
        }
        if (transitionY > 0 && newY + snapOffsetY >= Globals.MAX_Y.get()) {
            mSnapMask |= TouchEventInfo.BOTTOM;
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
        mViewProperties.markUpdate().update();
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
