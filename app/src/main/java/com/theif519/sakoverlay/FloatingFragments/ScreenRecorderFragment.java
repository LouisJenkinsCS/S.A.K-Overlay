package com.theif519.sakoverlay.FloatingFragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.projection.MediaProjectionManager;
import android.os.IBinder;
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
import com.theif519.sakoverlay.Services.RecorderService.RecorderState;

/**
 * Created by theif519 on 11/12/2015.
 */
public class ScreenRecorderFragment extends FloatingFragment {

    public static Boolean INSTANCE_EXISTS = false;

    private RecorderState mState = RecorderState.DEAD;

    public static final String IDENTIFIER = "Screen Recorder";

    public static final String RESULT_CODE_KEY = "Result Code", DATA_INTENT_KEY = "Data Intent";

    private static final int DISPLAY_WIDTH = 480;
    private static final int DISPLAY_HEIGHT = 640;

    private MediaProjectionManager mManager;

    private RecorderService mService;
    private RecorderService.RecorderBinder mBinder;

    private TextView mStateText;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //Toast.makeText(getActivity(), "Bound to RecorderService!", Toast.LENGTH_SHORT).show();
            mBinder = (RecorderService.RecorderBinder) service;
            mService = mBinder.getService();
            //mStateText.setText("Bound...");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBinder = null;
            Toast.makeText(getActivity(), "Unbound from RecorderService!", Toast.LENGTH_SHORT).show();
            //mStateText.setText("Unbound...");
        }
};

    private BroadcastReceiver mStateChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mState = (RecorderState) intent.getSerializableExtra(Globals.Keys.RECORDER_STATE_KEY);
            ((TextView) getContentView().findViewById(R.id.screen_recorder_state_text)).setText(mState.toString());
            //Toast.makeText(getActivity(), "State Changed to: " + mState.toString(), Toast.LENGTH_SHORT).show();
        }
    };

    public static ScreenRecorderFragment newInstance() {
        if (INSTANCE_EXISTS) return null;
        ScreenRecorderFragment fragment = new ScreenRecorderFragment();
        fragment.LAYOUT_ID = R.layout.screen_recorder;
        fragment.LAYOUT_TAG = IDENTIFIER;
        fragment.ICON_ID = R.drawable.screen_recorder;
        INSTANCE_EXISTS = true;
        return fragment;
    }

    @Override
    protected void setup() {
        super.setup();
        getContentView().findViewById(R.id.screen_recorder_record_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mService == null || mBinder == null) {
                    startActivityForResult(mManager.createScreenCaptureIntent(), Globals.RECORDER_PERMISSION_RETVAL);
                    return;
                }
                if (mState != RecorderState.STARTED) {
                    createDialog();
                } else {
                    mService.stopRecording();
                }
            }
        });
        mStateText = (TextView) getContentView().findViewById(R.id.screen_recorder_state_text);
        mManager = (MediaProjectionManager) getActivity().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }

    @Override
    public void onActivityResult(int requestCode, final int resultCode, final Intent data) {
        if (requestCode != Globals.RECORDER_PERMISSION_RETVAL) {
            Toast.makeText(getActivity(), "Received an unknown request code!Aborting!", Toast.LENGTH_LONG).show();
            return;
        }
        if (resultCode != Activity.RESULT_OK) {
            Toast.makeText(getActivity(), "Permission Denied!", Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(getActivity(), com.theif519.sakoverlay.Services.RecorderService.class);
        Log.i(getClass().getName(), "Result Code: " + resultCode + "Intent NULL: " + (data == null));
        intent.putExtra(RESULT_CODE_KEY, resultCode);
        intent.putExtra(Intent.EXTRA_INTENT, data);
        getActivity().bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                //Toast.makeText(getActivity(), "Bound to RecorderService!", Toast.LENGTH_SHORT).show();
                mBinder = (RecorderService.RecorderBinder) service;
                mService = mBinder.getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mService = null;
                mBinder = null;
                //Toast.makeText(getActivity(), "Unbound from RecorderService!", Toast.LENGTH_SHORT).show();
            }
        }, Context.BIND_AUTO_CREATE);
    }

    public void createDialog(){
        final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.dialog_recorder_details);
        final EditText width = (EditText) dialog.findViewById(R.id.dialog_recorder_resolution_width);
        final EditText height = (EditText) dialog.findViewById(R.id.dialog_recorder_resolution_height);
        final CheckBox audio = (CheckBox) dialog.findViewById(R.id.dialog_recorder_audio_checkbox);
        final EditText fileName = (EditText) dialog.findViewById(R.id.dialog_recorder_filename_name);
        dialog.findViewById(R.id.dialog_recorder_button_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mService == null) {
                    Toast.makeText(getActivity(), "Service died!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    return;
                }
                mService.startRecording(
                        Integer.parseInt(width.getText().toString()),
                        Integer.parseInt(height.getText().toString()),
                        audio.isChecked(),
                        fileName.getText().toString()
                );
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

    @Override
    public void onResume() {
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mStateChangeReceiver, new IntentFilter(Globals.Keys.RECORDER_STATE_CHANGE_KEY));
        super.onResume();
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mStateChangeReceiver);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
