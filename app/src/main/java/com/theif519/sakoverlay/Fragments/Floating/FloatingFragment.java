package com.theif519.sakoverlay.Fragments.Floating;


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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.jakewharton.rxbinding.view.RxView;
import com.theif519.sakoverlay.Misc.Globals;
import com.theif519.sakoverlay.POD.TouchEventInfo;
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

    /*
        mIsDead: Used to signify that this fragment is dead or is about to be, but Garbage Collector hasn't collected us yet.

        mMinimizeHint: Helps determine whether or not, when the move event finishes, to minimize this fragment.

        mFinishedMultiTouch: Helps prevent the automatic snapping of views away from the finger released.
     */
    private boolean mIsDead = false, mMinimizeHint = false, mFinishedMultiTouch = false, mIsTransparent = false;

    /*
        Required to keep track of the position and size of the view between each onTouchEvent.
     */
    private int width, height, x, y, tmpWidth, tmpHeight, tmpX, tmpY, tmpX2, tmpY2;

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
        //mContentView.setVisibility(View.INVISIBLE);
        setupGlobals();
        setupListeners();
        setupReactive();
        setupTaskItem();
        /*f (mMappedContext != null && Boolean.valueOf(mMappedContext.get(Globals.Keys.MINIMIZED))) {
            minimize();
        } else mContentView.setVisibility(View.VISIBLE);*/
        mContentView.post(new Runnable() {
            @Override
            public void run() {
                if (mMappedContext != null) unpack();
                setup();
            }
        });
        return mContentView;
    }

    private void setupTaskItem() {
        mTaskBarButton = new ImageButton(getActivity());
        mTaskBarButton.setImageResource(ICON_ID);
        ((LinearLayout) getActivity().findViewById(R.id.main_task_bar)).addView(mTaskBarButton);
    }

    /**
     * Where we initialize, retrieve and setup our global variables.
     */
    private void setupGlobals() {
        if (mOptions == null) {
            mOptions = new ArrayList<>();
        }
        mOptions.add(TRANSPARENCY_TOGGLE);
        mOptions.add(BRING_TO_FRONT);
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
        mContentView.findViewById(R.id.title_bar_options).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final PopupWindow window = new PopupWindow(null, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
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
                Point p = MeasureTools.getScaledCoordinates(mContentView);
                window.setContentView(listView);
                window.setWidth(MeasureTools.measureContentWidth(getActivity(), adapter));
                window.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.transparent_fragment)));
                window.showAtLocation(getActivity().findViewById(R.id.main_layout), Gravity.NO_GRAVITY,
                        p.x + MeasureTools.scaleToInt(mContentView.findViewById(R.id.title_bar_options).getWidth(), Globals.SCALE_X.get()),
                        p.y + MeasureTools.scaleToInt(mContentView.findViewById(R.id.title_bar_options).getHeight(), Globals.SCALE_Y.get()));
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
     * <p/>
     * Decided to place all relevant comments describing what a certain thing does that cannot fit within one
     * line.
     * <p/>
     * RxView is a sub-library of RxJava that handles and automates creating observables from Android's
     * vanilla listeners. The reason I do not use this above is because it is extremely paranoid about running on the main thread,
     * even if it does not touch the view other than it's properties (which is legal), and throws a runtime exception.
     * <p/>
     * Since the below is extremely simple, I do it for simplicity. Notice the concatWith() operator, which
     * creates one observable from both. Hence, when either listener goes off, subscribe() will be called
     * for either one. Otherwise I would have to create two different onGlobalLayoutChangeListeners for them.
     */
    private void setupReactive() {
        observableFromTouch(mContentView.findViewById(R.id.title_bar_move))
                .observeOn(AndroidSchedulers.mainThread()) // The Observer, the UI Thread, waits for processed events containing the information needed to manipulate views.
                .subscribeOn(Schedulers.computation()) // The Observable's events are processed on a computational thread, which is a non I/O-Bound thread. Perfect for this.
                .map(new Func1<MotionEvent, TouchEventInfo>() { // Map transforms one item to another item. We process the MotionEvent and create an object that encapsulates straight-forward instructions.
                    @Override
                    public TouchEventInfo call(MotionEvent event) {
                        return move(event);
                    }
                })
                .filter(new Func1<TouchEventInfo, Boolean>() { // Here we "filter" unwanted processed items. If it returns null, it does not have to move at all.
                    @Override
                    public Boolean call(TouchEventInfo info) {
                        return info != null;
                    }
                })
                .subscribe(new Action1<TouchEventInfo>() { // This part is ran on the UI Thread. The MainThread does a lot less work than before, which is good.
                    @Override
                    public void call(TouchEventInfo info) {
                        int x = info.getX(), y = info.getY();
                        if (x != Integer.MAX_VALUE && y != Integer.MAX_VALUE) { // If X and Y are dummy values, we do not set them.
                            mContentView.setX(info.getX());
                            mContentView.setY(info.getY());
                        }
                        mSnapMask = info.getMask();
                        if (mSnapMask != 0)
                            snap(mSnapMask); // Keep in mind, that if it is 0, then no bits are set.
                    }
                });
        RxView.globalLayouts(getActivity().findViewById(R.id.main_layout)).concatWith(RxView.globalLayouts(mContentView))
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        boundsCheck();
                    }
                });
        observableFromTouch(mContentView.findViewById(R.id.resize_button))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.computation())
                .map(new Func1<MotionEvent, Point>() {
                    @Override
                    public Point call(MotionEvent event) {
                        return resize(event);
                    }
                })
                .filter(new Func1<Point, Boolean>() {
                    @Override
                    public Boolean call(Point point) {
                        return point != null;
                    }
                })
                .subscribe(new Action1<Point>() {
                    @Override
                    public void call(Point point) {
                        mContentView.setLayoutParams(new FrameLayout.LayoutParams(point.x, point.y));
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

    public TouchEventInfo move(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mContentView.bringToFront();
                touchXOffset = (prevX = (int) event.getRawX()) - (int) mContentView.getX();
                touchYOffset = (prevY = (int) event.getRawY()) - (int) mContentView.getY();
                return null;
            case MotionEvent.ACTION_MOVE:
                mSnapHint = getSnapMask(prevX, prevY, (tmpX = (int) event.getRawX()), (tmpY = (int) event.getRawY()));
                prevX = tmpX;
                prevY = tmpY;
                width = mContentView.getWidth();
                height = mContentView.getHeight();
                int scaleDiffX = MeasureTools.scaleDiffToInt(width, Globals.SCALE_X.get()) / 2;
                int scaleDiffY = MeasureTools.scaleDiffToInt(height, Globals.SCALE_Y.get()) / 2;
                int moveX = Math.min(Math.max(tmpX - touchXOffset, -scaleDiffX), Globals.MAX_X.get() - width + scaleDiffX);
                int moveY = Math.min(Math.max(tmpY - touchYOffset, -scaleDiffY), Globals.MAX_Y.get() - height + scaleDiffY);
                return new TouchEventInfo(moveX, moveY, 0);
            case MotionEvent.ACTION_UP:
                return new TouchEventInfo(Integer.MAX_VALUE, Integer.MAX_VALUE, mSnapHint);
            default:
                return null;
        }
    }

    public Point resize(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Point p = MeasureTools.getScaledCoordinates(mContentView);
                tmpX2 = p.x;
                tmpY2 = p.y;
                return null;
            case MotionEvent.ACTION_MOVE:
                int diffX = (int) event.getRawX() - tmpX2;
                int diffY = (int) event.getRawY() - tmpY2;
                int scaleDiffX = MeasureTools.scaleDiffToInt(mContentView.getWidth(), Globals.SCALE_X.get());
                int scaleDiffY = MeasureTools.scaleDiffToInt(mContentView.getHeight(), Globals.SCALE_Y.get());
                int width = Math.min(Math.max((int) (diffX / Globals.SCALE_X.get()), 250), Globals.MAX_X.get() + scaleDiffX);
                int height = Math.min(Math.max((int) (diffY / Globals.SCALE_Y.get()), 250), Globals.MAX_Y.get() + scaleDiffY);
                return new Point(width, height);
            default:
                return null;
        }
    }

    private int mSnapHint = 0;

    public void snap(int snapHint) {
        int maxWidth = getActivity().findViewById(R.id.main_layout).getWidth();
        int maxHeight = getActivity().findViewById(R.id.main_layout).getHeight();
        int width = 0, height = 0, x = 0, y = 0;
        if ((snapHint & TouchEventInfo.RIGHT) != 0) {
            width = maxWidth / 2;
            height = maxHeight;
            x = maxWidth / 2;
        }
        if ((snapHint & TouchEventInfo.LEFT) != 0) {
            width = maxWidth / 2;
            height = maxHeight;
        }
        if ((snapHint & TouchEventInfo.UPPER) != 0) {
            if (width == 0) {
                width = maxWidth;
            }
            height = maxHeight / 2;
        }
        if ((snapHint & TouchEventInfo.BOTTOM) != 0) {
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
        mContentView.setX(x);
        mContentView.setY(y);
        mContentView.setLayoutParams(new FrameLayout.LayoutParams(width, height));
    }

    public int getSnapMask(int oldX, int oldY, int newX, int newY) {
        int snapMask = 0;
        int transitionX = newX - oldX;
        int transitionY = newY - oldY;
        int snapOffsetX = MeasureTools.scaleToInt(mContentView.getWidth(), Globals.SCALE_X.get()) / 10;
        int snapOffsetY = MeasureTools.scaleToInt(mContentView.getHeight(), Globals.SCALE_Y.get()) / 10;
        if (transitionX > 0 && newX + snapOffsetX >= Globals.MAX_X.get()) {
            snapMask |= TouchEventInfo.RIGHT;
        }
        if (transitionX < 0 && MeasureTools.getScaledCoordinates(mContentView).x <= snapOffsetX) {
            snapMask |= TouchEventInfo.LEFT;
        }
        if (transitionY < 0 && MeasureTools.getScaledCoordinates(mContentView).y <= snapOffsetY) {
            snapMask |= TouchEventInfo.UPPER;
        }
        if (transitionY > 0 && newY + snapOffsetY >= Globals.MAX_Y.get()) {
            snapMask |= TouchEventInfo.BOTTOM;
        }
        return snapMask;
    }

    public ArrayMap<String, String> serialize() {
        ArrayMap<String, String> map = new ArrayMap<>();
        map.put(Globals.Keys.LAYOUT_TAG, LAYOUT_TAG);
        map.put(Globals.Keys.X_COORDINATE, Integer.toString(x));
        map.put(Globals.Keys.Y_COORDINATE, Integer.toString(y));
        map.put(Globals.Keys.WIDTH, Integer.toString(width));
        map.put(Globals.Keys.HEIGHT, Integer.toString(height));
        map.put(Globals.Keys.MINIMIZED, Boolean.toString(mContentView.getVisibility() == View.INVISIBLE));
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
        x = Integer.parseInt(mMappedContext.get(Globals.Keys.X_COORDINATE));
        y = Integer.parseInt(mMappedContext.get(Globals.Keys.Y_COORDINATE));
        width = Integer.parseInt(mMappedContext.get(Globals.Keys.WIDTH));
        height = Integer.parseInt(mMappedContext.get(Globals.Keys.HEIGHT));
        mContentView.setX(x);
        mContentView.setY(y);
        mContentView.setLayoutParams(new FrameLayout.LayoutParams(width, height));
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

    }

    /**
     * For any subclasses that need to clean up extra resources, they may do so here.
     */
    protected void cleanUp() {
        ((LinearLayout) getActivity().findViewById(R.id.main_task_bar)).removeView(mTaskBarButton);
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

    public void onItemSelected(String string) {
        switch (string) {
            case TRANSPARENCY_TOGGLE:
                if (mIsTransparent = !mIsTransparent) {
                    mContentView.findViewById(R.id.title_bar_root).setBackgroundColor(getResources().getColor(android.R.color.transparent));
                } else {
                    mContentView.findViewById(R.id.title_bar_root).setBackgroundColor(getResources().getColor(R.color.black));
                }
                break;
            case BRING_TO_FRONT:
                mContentView.bringToFront();
                break;
        }
    }

}
