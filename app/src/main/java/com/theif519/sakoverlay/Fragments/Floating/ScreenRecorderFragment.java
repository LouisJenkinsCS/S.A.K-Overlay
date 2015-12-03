package com.theif519.sakoverlay.Fragments.Floating;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Point;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.theif519.sakoverlay.Adapters.VideoInfoAdapter;
import com.theif519.sakoverlay.Async.MediaThumbnailGenerator;
import com.theif519.sakoverlay.Misc.Globals;
import com.theif519.sakoverlay.POD.RecorderInfo;
import com.theif519.sakoverlay.POD.VideoInfo;
import com.theif519.sakoverlay.R;
import com.theif519.sakoverlay.Services.RecorderService;
import com.theif519.utils.Misc.FileRetriever;

import java.io.File;
import java.util.List;

import rx.Subscription;
import rx.functions.Action1;

/**
 * Created by theif519 on 11/12/2015.
 */
public class ScreenRecorderFragment extends FloatingFragment {

    public static Boolean INSTANCE_EXISTS = false;

    protected static final String IDENTIFIER = "Screen Recorder";

    private TextView mStateText;

    public static ScreenRecorderFragment newInstance() {
        if (INSTANCE_EXISTS) return null;
        ScreenRecorderFragment fragment = new ScreenRecorderFragment();
        fragment.LAYOUT_ID = R.layout.screen_recorder;
        fragment.LAYOUT_TAG = IDENTIFIER;
        fragment.ICON_ID = R.drawable.screen_recorder;
        INSTANCE_EXISTS = true;
        return fragment;
    }

    private RecorderService mServiceHandle;

    private Subscription mStateChangeHandler;

    private ServiceConnection mServiceConnectionHandler;

    private boolean mIsRunning = false;

    @Override
    protected void setup() {
        super.setup();
        mStateText = (TextView) getActivity().findViewById(R.id.screen_recorder_state_text);
        getContentView().findViewById(R.id.screen_recorder_record_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsRunning) {
                    mServiceHandle.stop();
                } else {
                    createDialog();
                }
            }
        });
        final ListView listView = (ListView) getContentView().findViewById(R.id.screen_recorder_file_list);
        new MediaThumbnailGenerator() {
            @Override
            protected void onPostExecute(List<VideoInfo> videoInfos) {
                listView.setEmptyView(((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.list_view_video_info_empty, null));
                listView.setAdapter(new VideoInfoAdapter(getActivity(), videoInfos));
            }
        }.execute(FileRetriever.getFiles(Globals.RECORDER_FILE_SAVE_PATH).toArray(new File[0]));
        getActivity().bindService(new Intent(getActivity(), RecorderService.class), mServiceConnectionHandler = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mServiceHandle = ((RecorderService.RecorderBinder) service).getService();
                mStateChangeHandler = ((RecorderService.RecorderBinder) service).observeStateChanges()
                        .distinctUntilChanged().subscribe(new Action1<RecorderService.RecorderState>() {
                            @Override
                            public void call(RecorderService.RecorderState recorderState) {
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

    private void createDialog() {
        final AlertDialog dialog = new AlertDialog.Builder(getActivity()).setView(R.layout.dialog_recorder_details)
                .setTitle("Recorder Info").setPositiveButton("Start", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText width = (EditText) ((AlertDialog) dialog).findViewById(R.id.dialog_recorder_resolution_width);
                        EditText height = (EditText) ((AlertDialog) dialog).findViewById(R.id.dialog_recorder_resolution_height);
                        CheckBox audio = (CheckBox) ((AlertDialog) dialog).findViewById(R.id.dialog_recorder_audio_checkbox);
                        EditText fileName = (EditText) ((AlertDialog) dialog).findViewById(R.id.dialog_recorder_filename_name);
                        mServiceHandle.start(new RecorderInfo(Integer.parseInt(width.getText().toString()), Integer.parseInt(height.getText().toString()),
                                audio.isChecked(), fileName.getText().toString()));
                        dialog.dismiss();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
        Point size = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getRealSize(size);
        ((EditText) dialog.findViewById(R.id.dialog_recorder_resolution_width)).setText(Integer.toString(size.x));
        ((EditText) dialog.findViewById(R.id.dialog_recorder_resolution_height)).setText(Integer.toString(size.y));
    }

    @Override
    public void onStart() {
        super.onStart();
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
