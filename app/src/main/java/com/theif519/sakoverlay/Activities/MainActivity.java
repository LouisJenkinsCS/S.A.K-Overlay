package com.theif519.sakoverlay.Activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.theif519.sakoverlay.Async.FloatingFragmentDeserializer;
import com.theif519.sakoverlay.Async.FloatingFragmentSerializer;
import com.theif519.sakoverlay.Fragments.Floating.FloatingFragment;
import com.theif519.sakoverlay.Fragments.Floating.FloatingFragmentFactory;
import com.theif519.sakoverlay.Fragments.Floating.GoogleMapsFragment;
import com.theif519.sakoverlay.Fragments.Floating.ScreenRecorderFragment;
import com.theif519.sakoverlay.Fragments.Floating.StickyNoteFragment;
import com.theif519.sakoverlay.Fragments.Floating.WebBrowserFragment;
import com.theif519.sakoverlay.Misc.Globals;
import com.theif519.sakoverlay.R;
import com.theif519.sakoverlay.Services.NotificationService;
import com.theif519.utils.Misc.ServiceTools;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by theif519 on ???. Forgot the date
 * <p/>
 * This is the entry point for the program, and also acts as the main context for all Fragments, Views,
 * Toasts, Services, etc. It's main purpose is to do the following before relinquishing the main thread
 * to handle other matters, with exception to it's life cycle methods.
 * <p/>
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // The below sets up Immersize mode full screne, which unfortunately requires API level 21.
        makeImmersive(getWindow().getDecorView());
        setContentView(R.layout.activity_main);
        setupPopupWindow();
        setupActionBar();
        // We start the service if it hasn't already been started.
        ServiceTools.startService(this, NotificationService.class, new ServiceTools.SetupIntent() {
            @Override
            public void setup(Intent intent) {
                intent.putExtra(NotificationService.START_NOTIFICATION, true);
            }
        });
        // Any time there is a configuration change, we update the bounds of the screen here.
        findViewById(R.id.main_layout).getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Globals.MAX_X.set(findViewById(R.id.main_layout).getWidth());
                Globals.MAX_Y.set(findViewById(R.id.main_layout).getHeight());
            }
        });
        Globals.SCALE_X.set(getDimension(R.dimen.default_scale_x));
        Globals.SCALE_Y.set(getDimension(R.dimen.default_scale_y));
        // Now that everything else is setup, we can make the call to deserialize.
        deserializeFloatingFragments();
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
        final View icon = actionbar.getCustomView().findViewById(R.id.menu_bar_icon);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMenuPopup.showAtLocation(findViewById(R.id.main_layout), Gravity.NO_GRAVITY, 0, getActionBar().getHeight());
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        // As unfortunately when the view focus changes, it loses it's "Immersion", I have to reset it whenever we regain focus.
        makeImmersive(getWindow().getDecorView());
    }

    /*
        WeakReference to current floating fragments. It allows a seamless and the most efficient way
        of keeping track of each fragment without having to worry about leaking it, or having it
        modify a public static collection. This is considered the best practice.

        Why do we need a reference for each FloatingFragment, you may ask? Because we cannot serialize each
        fragment without it. Why do we use a WeakReference? Because each fragment can be destroyed without the
        MainActivity knowing (which is a good thing).
     */
    private List<WeakReference<FloatingFragment>> mFragments = new ArrayList<>();

    /**
     * Sets up and begins the AsyncTask. Unfortunately, we cannot reuse an AsyncTask after using it, or else
     * we would recycle it. A handler would be too wasteful as it would just spin/block and do absolutely nothing,
     * no matter how "cheap" threads are.
     *
     * It must also be noted that, while I could not fix the issue it is noted here, that sometimes
     * onPuase() AND onStop() is called immediately after onCreate(), and sometimes even onDestroy() (???)
     * so it may throw an IllegalStateException because onSaveInstanceState has already been called and these operations
     * cannot be started. This results in a corrupted file (???) as it completely disappears when this happens.
     *
     * I was thinking of using Rx to handle this, but it wouldn't really solve the actual problem. From what else
     * I can pinpoint, the issue is that .commit() is called after the MainActivity is in the Background. Some probable
     * "solutions" in the mean time is probably to setup a Runnable that the MainActivity can check for, in case
     * it needs to inflate each FloatingFragment later.
     */
    private void deserializeFloatingFragments() {
        final File jsonFile = new File(getExternalFilesDir(null), Globals.JSON_FILENAME);
        if (jsonFile.exists()) {
            new FloatingFragmentDeserializer() {
                @Override
                protected void onPreExecute() {
                    this.file = jsonFile;
                } // Sets the file handle.

                @Override
                protected void onPostExecute(List<ArrayMap<String, String>> mapList) {
                    FloatingFragmentFactory factory = FloatingFragmentFactory.getInstance();
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    for (ArrayMap<String, String> map : mapList) {
                        addFragment(factory.getFragment(map));
                    }
                    transaction.commit(); // This may cause a crash if onStop() called before finish.
                }
            }.execute();
        }
    }

    /**
     * Simply serializes each view. By utilizing WeakReferences, I do not need to worry about
     * memory leaks, nor the issues with a static variable. It basically calls serialize() on each
     * fragment, then flushes it to disk.
     */
    @SuppressWarnings("unchecked")
    private void serializeFloatingFragments() {
        List<ArrayMap<String, String>> mapList = new ArrayList<>();
        for (WeakReference<FloatingFragment> fragmentWeakReference : mFragments) {
            // Atomic operation, once obtained as strong reference, it is safe to dereference.
            FloatingFragment fragment = fragmentWeakReference.get();
            // A fragment is dead when it is dismissed and is still contained in this list.
            if (fragment != null && !fragment.isDead()) {
                mapList.add(fragment.serialize());
            }
        }
        new FloatingFragmentSerializer() {
            @Override
            protected void onPreExecute() {
                this.file = new File(getExternalFilesDir(null), Globals.JSON_FILENAME);
            }
        }.execute(mapList.toArray(new ArrayMap[mapList.size()]));
    }

    /**
     * Convenience method to quickly add a new FloatingFragment to the list and to the FragmentManager.
     * @param fragment Fragment to add.
     */
    private void addFragment(FloatingFragment fragment) {
        if (fragment == null) {
            Toast.makeText(MainActivity.this, "There can only be one instance of this widget!", Toast.LENGTH_LONG).show();
            return;
        }
        mFragments.add(new WeakReference<>(fragment));
        getFragmentManager().beginTransaction().add(R.id.main_layout, fragment).commit();
    }

    @Override
    protected void onPause() {
        super.onPause();
        serializeFloatingFragments();
    }

    /**
     * When back button is pressed, it moves this instance of MainActivity to the backstack, helps with going
     * back to the previous application open. It also serializes as if it was onPause.
     */
    @Override
    public void onBackPressed() {
        serializeFloatingFragments();
        moveTaskToBack(true);
    }

    private void setupPopupWindow() {
        View view = getLayoutInflater().inflate(R.layout.menu_icon_dropdown, null);
        // As stated above, when focus changes, including from a PopupWindow, it loses Immersive Mode, hence we reset here.
        makeImmersive(view);
        mMenuPopup = new PopupWindow(view);
        mMenuPopup.getContentView().findViewById(R.id.menu_bar_browser_option).findViewById(R.id.menu_child_item_clickable).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFragment(WebBrowserFragment.newInstance());
                mMenuPopup.dismiss();
            }
        });
        mMenuPopup.getContentView().findViewById(R.id.menu_bar_maps_option).findViewById(R.id.menu_child_item_clickable).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFragment(GoogleMapsFragment.newInstance());
                mMenuPopup.dismiss();
            }
        });
        mMenuPopup.getContentView().findViewById(R.id.menu_bar_recorder_option).findViewById(R.id.menu_child_item_clickable).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFragment(ScreenRecorderFragment.newInstance());
                mMenuPopup.dismiss();
            }
        });
        mMenuPopup.getContentView().findViewById(R.id.menu_bar_sticky_option).findViewById(R.id.menu_child_item_clickable).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFragment(StickyNoteFragment.newInstance());
                mMenuPopup.dismiss();
            }
        });
        mMenuPopup.setFocusable(true);
        mMenuPopup.setBackgroundDrawable(new BitmapDrawable());
        mMenuPopup.setOutsideTouchable(true);
        mMenuPopup.setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    /**
     * Convenience method to retrieve a dimension.
     * @param dimenId R id of the dimension
     * @return Dimension as a float.
     */
    private float getDimension(int dimenId){
        TypedValue value = new TypedValue();
        getResources().getValue(dimenId, value, true);
        return value.getFloat();
    }

    /**
     * Convenience method to make a view Immersive. In the future, I will be for the user's API level, to
     * make sure we do not launch ImmersiveMode when below API Level 21. However, I do not have time for that.
     * @param view View to make immersive.
     */
    private void makeImmersive(View view){
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
}
