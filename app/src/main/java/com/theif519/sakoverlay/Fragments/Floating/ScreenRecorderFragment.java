package com.theif519.sakoverlay.Fragments.Floating;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Point;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.annotations.Expose;
import com.theif519.sakoverlay.Adapters.VideoInfoAdapter;
import com.theif519.sakoverlay.Async.MediaThumbnailGenerator;
import com.theif519.sakoverlay.Misc.Globals;
import com.theif519.sakoverlay.POJO.RecorderInfo;
import com.theif519.sakoverlay.POJO.VideoInfo;
import com.theif519.sakoverlay.R;
import com.theif519.sakoverlay.Services.RecorderService;
import com.theif519.utils.Misc.FileRetriever;

import java.io.File;
import java.util.List;

import rx.Subscription;

/**
 * Created by theif519 on 11/12/2015.
 * <p>
 * ScreenRecorder FloatingFragment right now is very ugly, and I'm not just saying that. UI-wise, it is
 * far from complete, however, that does not mean it is lacking in functionality.
 * <p>
 * ScreenRecorder launches and binds to the RecorderService, which allows it to act as both
 * a controller and to be controlled (thanks to RxJava). It can call start(), stop(), pause() and die()
 * on the RecorderService, but for example, start() and stop() can be called from the same button, which is determined
 * by the state (which we observe). Hence, when the state is STARTED, the next command is STOPPED. And of course
 * vice verse. This is extremely useful as the controller button (attached to the WindowManager) can change the state
 * at will and also has the same controller/controller relationship as this FloatingFragment.
 * <p>
 * It also has a ListAdapter for previous screen recorderings, which display it's description and duration
 * and more information the user may be interested in. When this FloatingFragment is killed, the service dies with it.
 */
public class ScreenRecorderFragment extends FloatingFragment {

    public ScreenRecorderFragment() {
        LAYOUT_ID = R.layout.screen_recorder;
        ICON_ID = R.drawable.screen_recorder;
        LAYOUT_TAG = IDENTIFIER;
    }

    protected static final String IDENTIFIER = "Screen Recorder";

    /*
        There can be only one instance of this class. Imagine the nightmare of having two or more of these, it'd
        be useless and redundantly redundant.
     */
    public static Boolean INSTANCE_EXISTS = false;
    @Expose
    private TextView mStateText;
    /*
        We maintain a handle to the RecorderService, obtained through the IBinder returned in ServiceConnection.
     */
    private RecorderService mServiceHandle;
    /*
        We also keep a subscription to the state change observable we are subscribed to so we can unsubscribe in onPause.
     */
    private Subscription mStateChangeHandler;
    /*
        We must maintain a reference to this so we may unbind later on.
     */
    private ServiceConnection mServiceConnectionHandler;
    /*
        Much of a finite-state machine, huh? This is manipulated based on state change, and hence determines
        whether the button calls START or STOP. Simple for now, but gets the job done.
     */
    private boolean mIsRunning = false;

    @Override
    protected void setup() {
        super.setup();
        mStateText = (TextView) getActivity().findViewById(R.id.screen_recorder_state_text);
        getContentView().findViewById(R.id.screen_recorder_record_button).setOnClickListener(v -> {
            if (mIsRunning) {
                mServiceHandle.stop();
            } else {
                createDialog();
            }
        });
        final ListView listView = (ListView) getContentView().findViewById(R.id.screen_recorder_file_list);
        new MediaThumbnailGenerator() {
            @Override
            protected void onPostExecute(List<VideoInfo> videoInfos) {
                Activity activity = getActivity();
                // As activity CAN be destroyed in onPostExecute, we must check here to prevent a null pointer exception
                if (activity == null) return;
                listView.setEmptyView(((LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.list_view_video_info_empty, null));
                listView.setAdapter(new VideoInfoAdapter(activity, videoInfos));
            }
        }.execute(FileRetriever.getFiles(Globals.RECORDER_FILE_SAVE_PATH).toArray(new File[0]));
        getActivity().bindService(new Intent(getActivity(), RecorderService.class), mServiceConnectionHandler = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                // In our IBinder, we declared a method to retrieve a handle to the service.
                // TODO: Research into whether or not this should become a WeakReference
                mServiceHandle = ((RecorderService.RecorderBinder) service).getService();
                // The observable to subscribe to for each state change.
                mStateChangeHandler = ((RecorderService.RecorderBinder) service)
                        .observeStateChanges() // Method we declared, returns an observable we can observe.
                        .distinctUntilChanged() // Not needed, but if someone (in the future of course) requests the current state, we don't want to update the textview twice.
                        .subscribe(recorderState -> {
                            mStateText.setText(recorderState.toString());
                            switch (recorderState) {
                                case STARTED:
                                    mIsRunning = true;
                                    ((TextView) getContentView().findViewById(R.id.screen_recorder_record_button)).setText("Stop");
                                    break;
                                case STOPPED:
                                    mIsRunning = false;
                                    ((TextView) getContentView().findViewById(R.id.screen_recorder_record_button)).setText("Start");
                            }
                        });
                mStateText.setText(mServiceHandle.getState().toString());
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Toast.makeText(getActivity(), "RecorderService disconnected...", Toast.LENGTH_SHORT).show();
                mStateChangeHandler.unsubscribe();
            }
        }, Context.BIND_AUTO_CREATE);
        getActivity().startService(new Intent(getActivity(), RecorderService.class));
    }

    /**
     * Creates a dialog which is shown to the user to ask for the information needed to record. I.E, width and height
     * of recording, by default is set to the max resolution, whether or not to record audio, and the name of the file.
     */
    private void createDialog() {
        final AlertDialog dialog = new AlertDialog.Builder(getActivity()).setView(R.layout.dialog_recorder_details)
                .setTitle("Recorder Info").setPositiveButton("Start", (dialog1, which) -> {
                    EditText width = (EditText) ((AlertDialog) dialog1).findViewById(R.id.dialog_recorder_resolution_width);
                    EditText height = (EditText) ((AlertDialog) dialog1).findViewById(R.id.dialog_recorder_resolution_height);
                    CheckBox audio = (CheckBox) ((AlertDialog) dialog1).findViewById(R.id.dialog_recorder_audio_checkbox);
                    EditText fileName = (EditText) ((AlertDialog) dialog1).findViewById(R.id.dialog_recorder_filename_name);
                    mServiceHandle.start(new RecorderInfo(Integer.parseInt(width.getText().toString()), Integer.parseInt(height.getText().toString()),
                            audio.isChecked(), fileName.getText().toString()));
                    dialog1.dismiss();
                }).setNegativeButton("Cancel", (dialog1, which) -> {
                    dialog1.dismiss();
                }).show();
        Point size = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getRealSize(size);
        ((EditText) dialog.findViewById(R.id.dialog_recorder_resolution_width)).setText(Integer.toString(size.x));
        ((EditText) dialog.findViewById(R.id.dialog_recorder_resolution_height)).setText(Integer.toString(size.y));
    }

    /**
     * As we unregister in onPause, we may need to get any missed states. We do so here.
     */
    @Override
    public void onStart() {
        super.onStart();
        // Note: onCreate() -> onStart() -> onCreateView() -> Setup(). Hence state text may not have been initialized so we check.
        if (mStateText != null) mStateText.setText(mServiceHandle.getState().toString());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unbindService(mServiceConnectionHandler);
        if (mServiceHandle != null) {
            mServiceHandle.die();
        }
        INSTANCE_EXISTS = false;
    }
}
