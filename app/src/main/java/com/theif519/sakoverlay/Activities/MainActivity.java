package com.theif519.sakoverlay.Activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.theif519.sakoverlay.Builders.MenuBuilder;
import com.theif519.sakoverlay.Fragments.Widgets.BaseWidget;
import com.theif519.sakoverlay.Fragments.Widgets.GoogleMapsWidget;
import com.theif519.sakoverlay.Fragments.Widgets.ScreenRecorderWidget;
import com.theif519.sakoverlay.Fragments.Widgets.NotePadWidget;
import com.theif519.sakoverlay.Fragments.Widgets.WebBrowserWidget;
import com.theif519.sakoverlay.Misc.Globals;
import com.theif519.sakoverlay.POJO.MenuOptions;
import com.theif519.sakoverlay.R;
import com.theif519.sakoverlay.Rx.RxBus;
import com.theif519.sakoverlay.Services.NotificationService;
import com.theif519.sakoverlay.Sessions.SessionManager;
import com.theif519.utils.Misc.MutableObject;
import com.theif519.utils.Misc.ServiceTools;
import com.theif519.utils.Misc.ShakeDetector;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;


/**
 * Created by theif519 on ???. Forgot the date
 * <p>
 * This is the entry point for the program, and also acts as the main context for all Fragments, Views,
 * Toasts, Services, etc. It's main purpose is to do the following before relinquishing the main thread
 * to handle other matters, with exception to it's life cycle methods.
 * <p>
 * 1) Inflate and initialize the menu PopupWindow ahead of time.
 * 2) Deserialize in onCreate() through a factory
 * 3) Keep WeakReferences to all FloatingFragments
 * 4) Serialize all FloatingFragments in onPause() through it's WeakReferences
 * 5) Maintain consistency by updating the maximum vertical and horizontal size of the screen on configuration change.
 */
public class MainActivity extends Activity {

    /*
        This is the menu PopupWindow inflated and created ahead of time. By inflating ahead of time,
        it reduces the f of the heap marginally, by not having to create and destroy each time.

        And of course it is faster this way.
     */
    private PopupWindow mMenuPopup;
    private SensorManager mSensorManager;
    private ShakeDetector mShakeDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // The below sets up Immersize mode full screne, which unfortunately requires API level 21.
        makeImmersive(getWindow().getDecorView());
        setContentView(R.layout.activity_main);
        setupActionBar();
        setupPopupWindow();
        RxBus.publish("Starting Overlay Notification Service if not already...");
        // We start the service if it hasn't already been started.
        ServiceTools.startService(this, NotificationService.class, intent -> intent.putExtra(NotificationService.START_NOTIFICATION, true));
        RxBus.publish("Waiting for layout to finish inflating...");
        // Set the initial MAX_X and MAX_Y when the root view is fully inflated as well as deserialize the floating fragments.
        findViewById(R.id.main_layout).post(() -> {
            updateMaxCoordinates();
            // The floating fragments are assured to be called after MainActivity has finished inflating it's view.
            SessionManager
                    .getInstance()
                    .restoreSession(this)
                    .defaultIfEmpty(BaseWidget.INVALID_WIDGET)
                    .subscribe(this::createWidget);
        });
        // We also setup the floating fragment scale in onCreate as it should never change.
        Globals.SCALE.set(getDimension(R.dimen.floating_fragment_scale));
        // Any time there is a configuration change, we update the bounds of the screen here.
        RxBus.observe(Configuration.class)
                .subscribe(C -> updateMaxCoordinates());
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(count -> RxBus.publish(Integer.valueOf(count)));
    }

    /**
     * Note that the ActionBar is custom, and hence requires quite a bit of setup each time. Luckily, this only needs
     * to be called once, when the Activity is created.
     */
    private void setupActionBar() {
        ActionBar actionbar = getActionBar();
        if (actionbar == null) throw new RuntimeException("ActionBar is null!");
        actionbar.setDisplayShowCustomEnabled(true);
        actionbar.setHomeButtonEnabled(false);
        actionbar.setCustomView(R.layout.menu_bar);
        makeImmersive(actionbar.getCustomView());
        final ImageView optionIcon = (ImageView) actionbar.getCustomView().findViewById(R.id.menu_bar_options);
        final View icon = actionbar.getCustomView().findViewById(R.id.menu_bar_icon);
        final TextView info = (TextView) findViewById(R.id.menu_bar_info);
        MutableObject<Subscription> subscription = new MutableObject<>(null);
        MutableObject<MenuOptions> previousOptions = new MutableObject<>(null);
        RxBus.observe(MenuOptions.class)
                .subscribe(option -> {
                            // If we have a previous subscription, we need to unsubscribe and also alert that it isn't showing.
                            if (subscription.get() != null) {
                                previousOptions.get().setIsShowing(false);
                                subscription.get().unsubscribe();
                            }
                            optionIcon.setVisibility(View.VISIBLE);
                            optionIcon.setImageResource(option.getIconResId());
                            info.setVisibility(View.VISIBLE);
                            info.setText(option.getIdentifier());
                            optionIcon.setOnClickListener(v -> option.getMenu().showAtLocation(
                                            findViewById(R.id.main_layout),
                                            Gravity.NO_GRAVITY,
                                            (int) optionIcon.getX(),
                                            getActionBar().getHeight()
                                    )
                            );
                            // Now we can update the previousOptions to this.
                            previousOptions.set(option);
                            // And alert that it is showing so it knows whether or not it has user focus.
                            option.setIsShowing(true);
                            // When the menu options owner gets destroyed, we unsubscribe and set everything to blank.
                            subscription.set(option
                                            .onOwnerDead()
                                            .subscribe(ignored -> {
                                                subscription.get().unsubscribe();
                                                subscription.set(null);
                                                previousOptions.set(null);
                                                optionIcon.setVisibility(View.GONE);
                                                info.setVisibility(View.GONE);
                                            })
                            );
                        }
                );
        icon.setOnClickListener(v -> mMenuPopup.showAtLocation(findViewById(R.id.main_layout), Gravity.NO_GRAVITY, 0, getActionBar().getHeight()));
        RxBus.observe(String.class)
                .subscribe(msg -> {
                    runOnUiThread(() -> {
                        info.setText(msg);
                        optionIcon.setVisibility(View.GONE);
                    });
                });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        // As unfortunately when the view focus changes, it loses it's "Immersion", I have to reset it whenever we regain focus.
        makeImmersive(getWindow().getDecorView());
    }

    /**
     * Convenience method to quickly add a new BaseWidget to the list and to the FragmentManager.
     *
     * @param widget Fragment to add.
     */
    private void createWidget(BaseWidget widget) {
        if (widget == null) {
            Toast.makeText(MainActivity.this, "There can only be one instance of this widget!", Toast.LENGTH_LONG).show();
            return;
        }
        if(widget == BaseWidget.INVALID_WIDGET){
            RxBus.publish("Created new session!");
            return;
        }
        getFragmentManager().beginTransaction().add(R.id.main_layout, widget).commit();
    }

    private void createWidgetSession(BaseWidget widget){
        SessionManager
                .getInstance()
                .appendSession(widget)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(widget::setUniqueId)
                .subscribe(ignored -> createWidget(widget));
    }

    /**
     * When back button is pressed, it moves this instance of MainActivity to the backstack, helps with going
     * back to the previous application open. It also serializes as if it was onPause.
     */
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private void setupPopupWindow() {
        RxBus.publish("Inflating Menu options...");
        mMenuPopup = new MenuBuilder()
                .addSeparator("Applications")
                .addOption("Web Browser", R.drawable.browser, () -> createWidgetSession(new WebBrowserWidget()))
                .addOption("Google Maps", R.drawable.maps, () -> createWidgetSession(new GoogleMapsWidget()))
                .addOption("Sticky Note", R.drawable.sticky_note, () -> createWidgetSession(new NotePadWidget()))
                .addOption("Screen Recorder", R.drawable.screen_recorder, () -> createWidgetSession(new ScreenRecorderWidget()))
                .create(this);
        makeImmersive(mMenuPopup.getContentView());
    }

    /**
     * Convenience method to retrieve a dimension.
     *
     * @param dimenId R id of the dimension
     * @return Dimension as a float.
     */
    private float getDimension(int dimenId) {
        TypedValue value = new TypedValue();
        getResources().getValue(dimenId, value, true);
        return value.getFloat();
    }

    /**
     * Convenience method to make a view Immersive. In the future, I will be for the user's API level, to
     * make sure we do not launch ImmersiveMode when below API Level 21. However, I do not have time for that.
     *
     * @param view View to make immersive.
     */
    private void makeImmersive(View view) {
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    /**
     * Due to the unfortunate fact that onConfigurationChange is called BEFORE all views are measured, we must
     * add a GlobalLayoutListener to the ViewTreeObserver. This means that, we have to poll on it until we see
     * that the overall width and height have changed.
     * <p>
     * However, once again, we only have to deal with this once. We broadcast to any subscribers that are listening
     * for an accurate onConfigurationChange, which includes this same thread (Read: Even though we are our own subscriber,
     * we are also our own publisher too).
     *
     * @param newConfig New configuration.
     */
    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        final View root = findViewById(R.id.main_root);
        final int oldWidth = root.getWidth(), oldHeight = root.getHeight();
        root.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (oldWidth != root.getWidth() && oldHeight != root.getHeight()) {
                    /*
                     We publish the Configuration object directly, as it's easily identifiable without having to create an object explicitly for
                     configuration changes. While it does leak it for a short time, it is better than creating a new object with little purpose at
                     all.
                    */
                    RxBus.publish(newConfig);
                    root.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(mShakeDetector, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
    }



    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(mShakeDetector);
    }

    /**
     * Helper function to update the maxmimum coordinates, contained in the Misc.Globals class.
     */
    private void updateMaxCoordinates() {
        Globals.MAX_X.set(findViewById(R.id.main_layout).getWidth());
        Globals.MAX_Y.set(findViewById(R.id.main_layout).getHeight());
    }
}
