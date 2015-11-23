package com.theif519.sakoverlay.FloatingFragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.media.projection.MediaProjectionManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.theif519.sakoverlay.Adapters.VideoInfoAdapter;
import com.theif519.sakoverlay.Async.MediaThumbnailGenerator;
import com.theif519.sakoverlay.Misc.Globals;
import com.theif519.sakoverlay.Beans.VideoInfo;
import com.theif519.sakoverlay.R;
import com.theif519.sakoverlay.Services.RecorderService;
import com.theif519.sakoverlay.Views.ListViewVideoInfo;
import com.theif519.utils.Misc.FileRetriever;

import java.io.File;
import java.util.List;

/**
 * Created by theif519 on 11/12/2015.
 */
public class ScreenRecorderFragment extends FloatingFragment {

    public static Boolean INSTANCE_EXISTS = false;

    public static final String IDENTIFIER = "Screen Recorder";

    public static final String RESULT_CODE_KEY = "Result Code", DATA_INTENT_KEY = "Data Intent";

    private static final int DISPLAY_WIDTH = 480;
    private static final int DISPLAY_HEIGHT = 640;

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

    private boolean mIsRunning = false, mFinishedSetup = false;

    @Override
    protected void setup() {
        super.setup();
        mStateText = (TextView) getActivity().findViewById(R.id.screen_recorder_state_text);
        getContentView().findViewById(R.id.screen_recorder_record_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsRunning) {
                    LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getActivity());
                    Intent intent = new Intent(Globals.Keys.RECORDER_COMMAND_REQUEST);
                    intent.putExtra(Globals.Keys.RECORDER_COMMAND, RecorderService.RecorderCommand.STOP);
                    manager.sendBroadcast(intent);
                    mIsRunning = false;
                } else {
                    mIsRunning = true;
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
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Toast.makeText(getActivity(), "You selected: "
                                + ((ListViewVideoInfo) parent.getItemAtPosition(position)).getDescription(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }.execute(FileRetriever.getFiles(Globals.RECORDER_FILE_SAVE_PATH).toArray(new File[0]));
        getActivity().startService(new Intent(getActivity(), RecorderService.class));
        mFinishedSetup = true;
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(Globals.Keys.RECORDER_STATE_REQUEST));
    }

    @Override
    public void onActivityResult(int requestCode, final int resultCode, final Intent data) {
        Log.i(getClass().getName(), "Asking user for permissions...");
        /*
            Note that an empty intent is equivalent to refusing permissions, as we explicitly check
            in the service whether or not we receive an OKAY and valid input.
         */
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getActivity());
        if (requestCode != Globals.RECORDER_PERMISSION_RETVAL) {
            Toast.makeText(getActivity(), "Received an unknown request code! Aborting!", Toast.LENGTH_LONG).show();
            manager.sendBroadcast(new Intent(Globals.Keys.RECORDER_PERMISSIONS_RESPONSE));
            mIsRunning = false;
            return;
        }
        if (resultCode != Activity.RESULT_OK) {
            Toast.makeText(getActivity(), "Permission Denied!", Toast.LENGTH_LONG).show();
            manager.sendBroadcast(new Intent(Globals.Keys.RECORDER_PERMISSIONS_RESPONSE));
            mIsRunning = false;
            return;
        }
        Intent intent = new Intent(Globals.Keys.RECORDER_PERMISSIONS_RESPONSE);
        intent.putExtra(Globals.Keys.RECORDER_PERMISSIONS_RESPONSE, true);
        intent.putExtra(RESULT_CODE_KEY, resultCode);
        intent.putExtra(Intent.EXTRA_INTENT, data);
        manager.sendBroadcast(intent);
    }

    public void createDialog() {
        final AlertDialog dialog = new AlertDialog.Builder(getActivity()).setView(R.layout.dialog_recorder_details)
                .setTitle("Recorder Info").setPositiveButton("Start", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText width = (EditText) ((AlertDialog) dialog).findViewById(R.id.dialog_recorder_resolution_width);
                        EditText height = (EditText) ((AlertDialog) dialog).findViewById(R.id.dialog_recorder_resolution_height);
                        CheckBox audio = (CheckBox) ((AlertDialog) dialog).findViewById(R.id.dialog_recorder_audio_checkbox);
                        EditText fileName = (EditText) ((AlertDialog) dialog).findViewById(R.id.dialog_recorder_filename_name);
                        Intent intent = new Intent(Globals.Keys.RECORDER_COMMAND_REQUEST);
                        intent.putExtra(Globals.Keys.RECORDER_COMMAND, RecorderService.RecorderCommand.START);
                        intent.putExtra(Globals.Keys.WIDTH, Integer.parseInt(width.getText().toString()));
                        intent.putExtra(Globals.Keys.HEIGHT, Integer.parseInt(height.getText().toString()));
                        intent.putExtra(Globals.Keys.AUDIO_ENABLED_KEY, audio.isChecked());
                        intent.putExtra(Globals.Keys.FILENAME_KEY, fileName.getText().toString());
                        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
                        dialog.dismiss();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mIsRunning = false;
                        dialog.dismiss();
                    }
                }).show();
        Point size = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getRealSize(size);
        ((EditText) dialog.findViewById(R.id.dialog_recorder_resolution_width)).setText(Integer.toString(size.x));
        ((EditText) dialog.findViewById(R.id.dialog_recorder_resolution_height)).setText(Integer.toString(size.y));
    }

    private BroadcastReceiver mStateChange, mCommandResponse, mServiceHasEnded, mPermissionAsked, mStateResponse;

    private void setupReceivers() {
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getActivity());
        IntentFilter stateFilter = new IntentFilter(Globals.Keys.RECORDER_STATE_CHANGE);
        stateFilter.addAction(Globals.Keys.RECORDER_STATE_RESPONSE);
        manager.registerReceiver(mStateChange = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mStateText.setText(intent.getSerializableExtra(Globals.Keys.RECORDER_STATE).toString());
            }
        }, stateFilter);
        manager.registerReceiver(mCommandResponse = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (!intent.getBooleanExtra(Globals.Keys.RECORDER_COMMAND_EXECUTED, false)) {
                    Toast.makeText(getActivity(), "Command Error: " + intent.getStringExtra(Globals.Keys.RECORDER_ERROR_MESSAGE), Toast.LENGTH_SHORT).show();
                    mIsRunning = !mIsRunning;
                }
            }
        }, new IntentFilter(Globals.Keys.RECORDER_COMMAND_RESPONSE));
        manager.registerReceiver(mServiceHasEnded = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(getActivity(), "RecorderService has ended!", Toast.LENGTH_SHORT).show();
            }
        }, new IntentFilter(Globals.Keys.RECORDER_STATE_HAS_ENDED));
        manager.registerReceiver(mPermissionAsked = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                startActivityForResult(
                        ((MediaProjectionManager) getActivity().getSystemService(Context.MEDIA_PROJECTION_SERVICE))
                                .createScreenCaptureIntent(), Globals.RECORDER_PERMISSION_RETVAL
                );
            }
        }, new IntentFilter(Globals.Keys.RECORDER_PERMISSIONS_REQUEST));
    }

    private void destroyReceivers() {
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getActivity());
        manager.unregisterReceiver(mStateChange);
        manager.unregisterReceiver(mCommandResponse);
        manager.unregisterReceiver(mServiceHasEnded);
        manager.unregisterReceiver(mPermissionAsked);
    }

    @Override
    public void onStart() {
        super.onStart();
        setupReceivers();
        // In the case that we missed a state change, we ask for it here.
        if(mFinishedSetup) LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(Globals.Keys.RECORDER_STATE_REQUEST));
    }

    @Override
    public void onStop() {
        super.onStop();
        destroyReceivers();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        INSTANCE_EXISTS = false;
    }
}
