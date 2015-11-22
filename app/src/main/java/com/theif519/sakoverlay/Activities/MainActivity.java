package com.theif519.sakoverlay.Activities;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.*;
import android.os.Process;
import android.util.ArrayMap;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Toast;

import com.theif519.sakoverlay.FloatingFragments.FloatingFragment;
import com.theif519.sakoverlay.FloatingFragments.FloatingFragmentFactory;
import com.theif519.sakoverlay.FloatingFragments.GoogleMapsFragment;
import com.theif519.sakoverlay.FloatingFragments.IntroductionFragment;
import com.theif519.sakoverlay.FloatingFragments.ScreenRecorderFragment;
import com.theif519.sakoverlay.FloatingFragments.StickyNoteFragment;
import com.theif519.sakoverlay.FloatingFragments.WebBrowserFragment;
import com.theif519.sakoverlay.R;
import com.theif519.sakoverlay.Services.NotificationService;
import com.theif519.utils.Misc.MutableObject;
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

    private static final MutableObject<Integer> MAX_X = new MutableObject<>(0), MAX_Y = new MutableObject<>(0);

    private static final MutableObject<Float> SCALE_X = new MutableObject<>(1.0f), SCALE_Y = new MutableObject<>(1.0f);

    public static final HandlerThread WORKER_THREAD;

    public static final Handler WORKER_HANDLE;

    /*
        This block of code initializes the handler thread which manages all background tasks,
        and due to the fact the Handler requires the looper of the HandlerThread, we must block
        until it has been initialized, hence the application may be slow to start up, sadly.
     */
    static {
        WORKER_THREAD = new HandlerThread("Generic Worker", Process.THREAD_PRIORITY_BACKGROUND);
        WORKER_THREAD.start();
        WORKER_HANDLE = new Handler(WORKER_THREAD.getLooper());
    }

    private static final String TAG = MainActivity.class.getName();
    public static final String JSON_FILENAME = "SerializedFloatingFragments.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
        setContentView(R.layout.activity_main);
        ServiceTools.startService(this, NotificationService.class, new ServiceTools.SetupIntent() {
            @Override
            public void setup(Intent intent) {
                intent.putExtra(NotificationService.START_NOTIFICATION, true);
            }
        });
        findViewById(R.id.menu_launcher_button_home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                FloatingFragment fragment = IntroductionFragment.newInstance();
                mFragments.add(new WeakReference<>(fragment));
                getFragmentManager().beginTransaction().add(R.id.main_layout, fragment).commit();
            }
        });
        findViewById(R.id.menu_launcher_button_sticky_note).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FloatingFragment fragment = StickyNoteFragment.newInstance();
                mFragments.add(new WeakReference<>(fragment));
                getFragmentManager().beginTransaction().add(R.id.main_layout, fragment).commit();
            }
        });
        findViewById(R.id.menu_launcher_button_google_maps).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FloatingFragment fragment = GoogleMapsFragment.newInstance();
                mFragments.add(new WeakReference<>(fragment));
                getFragmentManager().beginTransaction().add(R.id.main_layout, fragment).commit();
            }
        });
        findViewById(R.id.web_browser_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FloatingFragment fragment = WebBrowserFragment.newInstance();
                mFragments.add(new WeakReference<>(fragment));
                getFragmentManager().beginTransaction().add(R.id.main_layout, fragment).commit();
            }
        });
        findViewById(R.id.menu_launcher_button_screen_recorder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FloatingFragment fragment = ScreenRecorderFragment.newInstance();
                if(fragment == null){
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
                MAX_X.value = findViewById(R.id.main_layout).getWidth();
                MAX_Y.value = findViewById(R.id.main_layout).getHeight();
            }
        });
        TypedValue value = new TypedValue();
        getResources().getValue(R.dimen.default_scale_x, value, true);
        SCALE_X.value = value.getFloat();
        getResources().getValue(R.dimen.default_scale_y, value, true);
        SCALE_Y.value = value.getFloat();
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

    public static int getMaxX(){
        return MAX_X.value;
    }

    public static int getMaxY(){
        return MAX_Y.value;
    }

    public static float getScaleX(){
        return SCALE_X.value;
    }

    public static float getScaleY(){
        return SCALE_Y.value;
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
