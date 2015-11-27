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

import com.theif519.sakoverlay.Fragments.Floating.FloatingFragment;
import com.theif519.sakoverlay.Fragments.Floating.FloatingFragmentFactory;
import com.theif519.sakoverlay.Fragments.Floating.GoogleMapsFragment;
import com.theif519.sakoverlay.Fragments.Floating.IntroductionFragment;
import com.theif519.sakoverlay.Fragments.Floating.ScreenRecorderFragment;
import com.theif519.sakoverlay.Fragments.Floating.StickyNoteFragment;
import com.theif519.sakoverlay.Fragments.Floating.WebBrowserFragment;
import com.theif519.sakoverlay.Misc.Globals;
import com.theif519.sakoverlay.R;
import com.theif519.sakoverlay.Services.NotificationService;
import com.theif519.utils.Misc.ServiceTools;
import com.theif519.utils.Serialization.JSONDeserializer;
import com.theif519.utils.Serialization.JSONSerializer;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


// TODO: Find a better way to register callbacks for button press, and menu options!
// TODO: Change from popup menu to a dialog menu.
public class MainActivity extends Activity {

    /*
        WeakReference - Allows me to keep track of fragments, without keeping them around after they are removed
        permanently "destroyed" by the FragmentManager/FragmentTransaction.

        To give an example of what I mean, imagine that this list, if I used strong referencing, would keep
        the Garbage Collector from collecting these fragments as I kept a strong reference to them from this list.
        Now, I COULD, of course, remove the fragments from this list as well when they are finished, but...

        Well, it was a way I could experiment, so why not?
     */
    private List<WeakReference<FloatingFragment>> mFragments = new ArrayList<>();
    public static final String JSON_FILENAME = "SerializedFloatingFragments.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        setContentView(R.layout.activity_main);
        setupActionBar();
        ServiceTools.startService(this, NotificationService.class, new ServiceTools.SetupIntent() {
            @Override
            public void setup(Intent intent) {
                intent.putExtra(NotificationService.START_NOTIFICATION, true);
            }
        });
        findViewById(R.id.dock_launcher_button_home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                FloatingFragment fragment = IntroductionFragment.newInstance();
                mFragments.add(new WeakReference<>(fragment));
                getFragmentManager().beginTransaction().add(R.id.main_layout, fragment).commit();
            }
        });
        findViewById(R.id.dock_launcher_button_sticky_note).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FloatingFragment fragment = StickyNoteFragment.newInstance();
                mFragments.add(new WeakReference<>(fragment));
                getFragmentManager().beginTransaction().add(R.id.main_layout, fragment).commit();
            }
        });
        findViewById(R.id.dock_launcher_button_google_maps).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FloatingFragment fragment = GoogleMapsFragment.newInstance();
                mFragments.add(new WeakReference<>(fragment));
                getFragmentManager().beginTransaction().add(R.id.main_layout, fragment).commit();
            }
        });
        findViewById(R.id.dock_launcher_web_browser_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FloatingFragment fragment = WebBrowserFragment.newInstance();
                mFragments.add(new WeakReference<>(fragment));
                getFragmentManager().beginTransaction().add(R.id.main_layout, fragment).commit();
            }
        });
        findViewById(R.id.dock_launcher_button_screen_recorder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FloatingFragment fragment = ScreenRecorderFragment.newInstance();
                if (fragment == null) {
                    Toast.makeText(MainActivity.this, "Only one instance of Screen Recorder may exist at a time!", Toast.LENGTH_SHORT).show();
                    return;
                }
                mFragments.add(new WeakReference<>(fragment));
                getFragmentManager().beginTransaction().add(R.id.main_layout, fragment).commit();
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
        final File jsonFile = new File(getExternalFilesDir(null), JSON_FILENAME);
        if(jsonFile.exists()){
            new JSONDeserializer() {
                @Override
                protected void onPreExecute() {
                    this.file = jsonFile;
                }

                @Override
                protected void onPostExecute(List<ArrayMap<String, String>> mapList) {
                    FloatingFragmentFactory factory = FloatingFragmentFactory.getInstance();
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    for(ArrayMap<String, String> map: mapList){
                        FloatingFragment fragment = factory.getFragment(map);
                        mFragments.add(new WeakReference<>(fragment));
                        transaction.add(R.id.main_layout, fragment);
                    }
                    transaction.commit();
                }
            }.execute();
        }
    }

    private void setupActionBar(){
        ActionBar actionbar = getActionBar();
        if(actionbar == null) throw new RuntimeException("ActionBar is null!");
        actionbar.setDisplayShowCustomEnabled(true);
        actionbar.setHomeButtonEnabled(false);
        actionbar.setCustomView(R.layout.menu_bar);
        final View icon = actionbar.getCustomView().findViewById(R.id.menu_bar_icon);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Clicked icon!", Toast.LENGTH_SHORT).show();
                View view = getLayoutInflater().inflate(R.layout.menu_icon_dropdown, null);
                view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                PopupWindow window = new PopupWindow(view);
                window.setFocusable(true);
                window.setBackgroundDrawable(new BitmapDrawable());
                window.setOutsideTouchable(true);
                window.setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                window.showAtLocation(findViewById(R.id.main_layout), Gravity.NO_GRAVITY, 0, 0);
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @SuppressWarnings("unchecked")
    private void serializeFloatingFragments(){
        List<ArrayMap<String, String>> mapList = new ArrayList<>();
        for(WeakReference<FloatingFragment> fragmentWeakReference: mFragments){
            // Atomic operation, once obtained as strong reference, it is safe to dereference.
            FloatingFragment fragment = fragmentWeakReference.get();
            // A fragment is dead when it is dismissed and is still contained in this list.
            if(fragment != null && !fragment.isDead()){
                mapList.add(fragment.serialize());
            }
        }
        new JSONSerializer() {
            @Override
            protected void onPreExecute() {
                this.file = new File(getExternalFilesDir(null), JSON_FILENAME);
            }
        }.execute(mapList.toArray(new ArrayMap[0]));
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
}
