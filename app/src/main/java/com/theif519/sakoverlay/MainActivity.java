package com.theif519.sakoverlay;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.util.ArrayMap;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

import com.theif519.utils.JSONDeserializer;
import com.theif519.utils.JSONSerializer;
import com.theif519.utils.ServiceTools;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


// TODO: Find a better way to register callbacks for button press, and menu options!
// TODO: Change from popup menu to a dialog menu.
public class MainActivity extends Activity {

    private List<WeakReference<FloatingFragment>> mFragments = new ArrayList<>();

    // Because MutableInt requires API level 23, and I'm too lazy to make a new class, and this handles any thread safety issues.
    public static final AtomicInteger maxX = new AtomicInteger(0), maxY = new AtomicInteger(0);

    private static final HandlerThread WORKER_THREAD;

    private static final Handler WORKER_HANDLE;

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

    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String JSON_FILENAME = "SerializedPopupWindowExtenderInformation.json";

    public void startServiceIfNotRunning(){
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service: manager.getRunningServices(Integer.MAX_VALUE)){
            if(service.service.getClassName().equals(OverlayService.class.getName())){
                return;
            }
        }
        Intent intent = new Intent(this, OverlayService.class);
        startService(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
        setContentView(R.layout.activity_main);
        ServiceTools.startService(this, OverlayService.class, new ServiceTools.SetupIntent() {
            @Override
            public void setup(Intent intent) {
                intent.putExtra(OverlayService.START_NOTIFICATION, true);
            }
        });
        findViewById(R.id.home_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                FloatingFragment fragment = FloatingFragment.newInstance(R.layout.introduction, "Introduction", "Introduction");
                mFragments.add(new WeakReference<>(fragment));
                getFragmentManager().beginTransaction().add(R.id.main_layout, fragment).commit();
            }
        });
        findViewById(R.id.sticky_note_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FloatingFragment fragment = StickyNoteFragment.newInstance("Sticky Note");
                mFragments.add(new WeakReference<>(fragment));
                getFragmentManager().beginTransaction().add(R.id.main_layout, fragment).commit();
            }
        });
        findViewById(R.id.google_maps_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FloatingFragment fragment = GoogleMapsFragment.newInstance();
                mFragments.add(new WeakReference<>(fragment));
                getFragmentManager().beginTransaction().add(R.id.main_layout, fragment).commit();
            }
        });
        findViewById(R.id.main_layout).getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                maxX.set(findViewById(R.id.main_layout).getWidth());
                maxY.set(findViewById(R.id.main_layout).getHeight());
            }
        });
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
    @Override
    protected void onPause() {
        List<ArrayMap<String, String>> mapList = new ArrayList<>();
        super.onPause();
        for(WeakReference<FloatingFragment> fragmentWeakReference: mFragments){
            FloatingFragment fragment = fragmentWeakReference.get();
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
    protected void onDestroy() {
        super.onDestroy();
        WORKER_THREAD.quitSafely();
    }
}
