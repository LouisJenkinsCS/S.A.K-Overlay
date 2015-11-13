package com.theif519.sakoverlay.FloatingFragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.theif519.sakoverlay.R;

import java.io.IOException;

/**
 * Created by theif519 on 11/12/2015.
 */
public class ScreenRecorderFragment extends FloatingFragment {

    private boolean mIsRunning = false;

    public static final String IDENTIFIER = "Screen Recorder";

    private static final int DISPLAY_WIDTH = 480;
    private static final int DISPLAY_HEIGHT = 640;


    private MediaProjectionManager mManager;
    private MediaProjection mProjection;
    private MediaRecorder mRecorder;
    private VirtualDisplay mDisplay;

    public static ScreenRecorderFragment newInstance() {
        ScreenRecorderFragment fragment = new ScreenRecorderFragment();
        fragment.LAYOUT_ID = R.layout.screen_recorder;
        fragment.LAYOUT_TAG = IDENTIFIER;
        fragment.ICON_ID = R.drawable.screen_recorder;
        return fragment;
    }

    @Override
    protected void setup() {
        super.setup();
        getContentView().findViewById(R.id.screen_recorder_record_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsRunning = !mIsRunning){
                    recordScreen();
                } else {
                    mRecorder.stop();
                    mRecorder.reset();
                    mDisplay.release();
                }
            }
        });
        mManager = (MediaProjectionManager) getActivity().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        mRecorder = new MediaRecorder();
        initRecorder();
        prepareRecorder();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode != com.theif519.sakoverlay.Services.RecorderService.PERMISSION_CODE){
            Toast.makeText(getActivity(), "Received an unknown request code!Aborting!", Toast.LENGTH_LONG).show();
            return;
        }
        if(resultCode != Activity.RESULT_OK){
            Toast.makeText(getActivity(), "Permission Denied!", Toast.LENGTH_SHORT).show();
            return;
        }
        com.theif519.utils.ServiceTools.startService(getActivity(), com.theif519.sakoverlay.Services.RecorderService.class, null);
        mProjection = mManager.getMediaProjection(resultCode, data);
        mProjection.registerCallback(new MediaProjection.Callback() {
            @Override
            public void onStop() {
                super.onStop();
                Toast.makeText(getActivity(), "Stopped recording!", Toast.LENGTH_LONG).show();
            }
        }, null);
        mDisplay = createVirtualDisplay();
        mRecorder.start();
    }

    private void prepareRecorder() {
        try {
            mRecorder.prepare();
        } catch (IllegalStateException | IOException e) {
            Log.e(getClass().getName(), e.getMessage());
        }
    }

    /**
     * Used to setup the MediaRecorder, configuring it.
     */
    private void initRecorder() {
        mRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mRecorder.setVideoEncodingBitRate(512 * 1000);
        mRecorder.setVideoFrameRate(30);
        mRecorder.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT);
        mRecorder.setOutputFile("/sdcard/capture.mp4");
    }

    /**
     * Convenience function to speedily create a new VirtualDisplay, as it's parameter list is quite long.
     * @return A Virtual Display bound to the MediaRecorder's Surface
     */
    private VirtualDisplay createVirtualDisplay(){
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
         return mProjection.createVirtualDisplay(getClass().getName(), DISPLAY_WIDTH, DISPLAY_HEIGHT,
                metrics.densityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mRecorder.getSurface(), null, null);
    }

    private void recordScreen(){
        if (mProjection == null) {
            startActivityForResult(mManager.createScreenCaptureIntent(), com.theif519.sakoverlay.Services.RecorderService.PERMISSION_CODE);
            return;
        }
        mDisplay = createVirtualDisplay();
        mRecorder.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mIsRunning){
            mRecorder.stop();
            mRecorder.release();
        }
    }
}
