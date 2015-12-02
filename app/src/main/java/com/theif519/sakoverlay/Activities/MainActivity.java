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
import com.theif519.sakoverlay.Async.FloatingFragmentSerializer;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


// TODO: Find a better way to register callbacks for button press, and menu options!
// TODO: Change from popup menu to a dialog menu.
public class MainActivity extends Activity {

    private PopupWindow mMenuPopup;


    public static final String JSON_FILENAME = "SerializedFloatingFragments.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        setContentView(R.layout.activity_main);
        setupPopupWindow();
        setupActionBar();
        ServiceTools.startService(this, NotificationService.class, new ServiceTools.SetupIntent() {
            @Override
            public void setup(Intent intent) {
                intent.putExtra(NotificationService.START_NOTIFICATION, true);
            }
        });
        findViewById(R.id.main_layout).getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Globals.MAX_X.set(findViewById(R.id.main_layout).getWidth());
                Globals.MAX_Y.set(findViewById(R.id.main_layout).getHeight());
            }
        });
        TypedValue value = new TypedValue();
        getResources().getValue(R.dimen.default_scale_x, value, true);
        Globals.SCALE_X.set(value.getFloat());
        getResources().getValue(R.dimen.default_scale_y, value, true);
        Globals.SCALE_Y.set(value.getFloat());
        deserializeFloatingFragments(); // Deserialize.
    }

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
                Toast.makeText(MainActivity.this, "Clicked icon!", Toast.LENGTH_SHORT).show();
                mMenuPopup.showAtLocation(findViewById(R.id.main_layout), Gravity.NO_GRAVITY, 0, getActionBar().getHeight());
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private List<WeakReference<FloatingFragment>> mFragments = new ArrayList<>();
    private void deserializeFloatingFragments(){
        final File jsonFile = new File(getExternalFilesDir(null), JSON_FILENAME);
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
                    transaction.commit();
                }
            }.execute();
        }
    }
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
                this.file = new File(getExternalFilesDir(null), JSON_FILENAME);
            }
        }.execute(mapList.toArray(new ArrayMap[0]));
    }
    private void addFragment(FloatingFragment fragment) {
        if(fragment == null) {
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

    @Override
    public void onBackPressed() {
        serializeFloatingFragments();
        moveTaskToBack(true);
    }

    private int getActionBarHeight() {
        TypedValue val = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.actionBarSize, val, true);
        return TypedValue.complexToDimensionPixelSize(val.data, getResources().getDisplayMetrics());
    }

    private void setupPopupWindow() {
        View view = getLayoutInflater().inflate(R.layout.menu_icon_dropdown, null);
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
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
}
