package com.theif519.sakoverlay.FloatingFragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.projection.MediaProjectionManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.theif519.sakoverlay.Misc.Globals;
import com.theif519.sakoverlay.R;
import com.theif519.sakoverlay.Services.RecorderService;

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

    private boolean mIsRunning = false;

    @Override
    protected void setup() {
        super.setup();
        mStateText = (TextView) getActivity().findViewById(R.id.screen_recorder_state_text);
        getContentView().findViewById(R.id.screen_recorder_record_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsRunning) {
                    LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getActivity());
                    Intent intent = new Intent(Globals.Keys.RECORDER_COMMAND_REQUEST_KEY);
                    intent.putExtra(Globals.Keys.RECORDER_COMMAND_KEY, RecorderService.RecorderCommand.STOP);
                    manager.sendBroadcast(intent);
                } else {
                    createDialog();
                    mIsRunning = true;
                }
            }
        });
        getActivity().startService(new Intent(getActivity(), RecorderService.class));
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
            manager.sendBroadcast(new Intent(Globals.Keys.RECORDER_PERMISSIONS_RESPONSE_KEY));
            return;
        }
        if (resultCode != Activity.RESULT_OK) {
            Toast.makeText(getActivity(), "Permission Denied!", Toast.LENGTH_LONG).show();
            manager.sendBroadcast(new Intent(Globals.Keys.RECORDER_PERMISSIONS_RESPONSE_KEY));
            return;
        }
        Intent intent = new Intent(Globals.Keys.RECORDER_PERMISSIONS_RESPONSE_KEY);
        intent.putExtra(Globals.Keys.RECORDER_PERMISSIONS_RESPONSE_KEY, true);
        intent.putExtra(RESULT_CODE_KEY, resultCode);
        intent.putExtra(Intent.EXTRA_INTENT, data);
        manager.sendBroadcast(intent);
    }

    public void createDialog() {
        final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.dialog_recorder_details);
        final EditText width = (EditText) dialog.findViewById(R.id.dialog_recorder_resolution_width);
        final EditText height = (EditText) dialog.findViewById(R.id.dialog_recorder_resolution_height);
        final CheckBox audio = (CheckBox) dialog.findViewById(R.id.dialog_recorder_audio_checkbox);
        final EditText fileName = (EditText) dialog.findViewById(R.id.dialog_recorder_filename_name);
        dialog.findViewById(R.id.dialog_recorder_button_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Globals.Keys.RECORDER_COMMAND_REQUEST_KEY);
                intent.putExtra(Globals.Keys.RECORDER_COMMAND_KEY, RecorderService.RecorderCommand.START);
                intent.putExtra(Globals.Keys.WIDTH_KEY, Integer.parseInt(width.getText().toString()));
                intent.putExtra(Globals.Keys.HEIGHT_KEY, Integer.parseInt(height.getText().toString()));
                intent.putExtra(Globals.Keys.AUDIO_ENABLED_KEY, audio.isChecked());
                intent.putExtra(Globals.Keys.FILENAME_KEY, fileName.getText().toString());
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
                dialog.dismiss();
            }
        });
        dialog.findViewById(R.id.dialog_recorder_button_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private BroadcastReceiver mStateChange, mCommandResponse, mServiceHasEnded, mPermissionAsked;

    private void setupReceivers() {
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getActivity());
        manager.registerReceiver(mStateChange = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mStateText.setText(intent.getSerializableExtra(Globals.Keys.RECORDER_STATE_KEY).toString());
            }
        }, new IntentFilter(Globals.Keys.RECORDER_STATE_CHANGE_KEY));
        manager.registerReceiver(mCommandResponse = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (!intent.getBooleanExtra(Globals.Keys.RECORDER_COMMAND_EXECUTED_KEY, false)) {
                    Toast.makeText(getActivity(), "Command Error: " + intent.getStringExtra(Globals.Keys.RECORDER_ERROR_MESSAGE_KEY), Toast.LENGTH_SHORT).show();
                    mIsRunning = !mIsRunning;
                }
            }
        }, new IntentFilter(Globals.Keys.RECORDER_COMMAND_RESPONSE_KEY));
        manager.registerReceiver(mServiceHasEnded = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(getActivity(), "RecorderService has ended!", Toast.LENGTH_SHORT).show();
            }
        }, new IntentFilter(Globals.Keys.RECORDER_STATE_HAS_ENDED_KEY));
        manager.registerReceiver(mPermissionAsked = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                startActivityForResult(
                        ((MediaProjectionManager) getActivity().getSystemService(Context.MEDIA_PROJECTION_SERVICE))
                                .createScreenCaptureIntent(), Globals.RECORDER_PERMISSION_RETVAL
                );
            }
        }, new IntentFilter(Globals.Keys.RECORDER_PERMISSIONS_REQUEST_KEY));
    }

    private void destroyReceivers() {
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getActivity());
        manager.unregisterReceiver(mStateChange);
        manager.unregisterReceiver(mCommandResponse);
        manager.unregisterReceiver(mServiceHasEnded);
        manager.unregisterReceiver(mPermissionAsked);
    }

    private boolean mReceivedMissedChange = false;

    private void receiveMissedChange() {
        if (mReceivedMissedChange) {
            final LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getActivity());
            manager.registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    mStateText.setText(intent.getStringExtra(Globals.Keys.RECORDER_STATE_KEY));
                    manager.unregisterReceiver(this);
                    mReceivedMissedChange = true;
                }
            }, new IntentFilter(Globals.Keys.RECORDER_STATE_RESPONSE_KEY));
            mReceivedMissedChange = false;
            manager.sendBroadcast(new Intent(Globals.Keys.RECORDER_STATE_REQUEST_KEY));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        setupReceivers();
        receiveMissedChange();
    }

    @Override
    public void onStop() {
        super.onStop();
        destroyReceivers();
    }
}
