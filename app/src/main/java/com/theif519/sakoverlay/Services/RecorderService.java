package com.theif519.sakoverlay.Services;

import android.app.IntentService;
import android.content.Intent;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Environment;

/**
 * Created by theif519 on 11/12/2015.
 */
public class RecorderService extends IntentService {

    public static final String ACTIVATE = "Activate", DENSITY_KEY = "Density", WIDTH_KEY = "Width", HEIGHT_KEY = "Height";

    public static final int PERMISSION_CODE = 1;

    private boolean isRunning = false;

    private MediaProjection mProjection;

    private MediaProjectionManager mManager;

    private VirtualDisplay mDisplay;

    private MediaRecorder mRecorder;

    private static final int DISPLAY_WIDTH = 480;
    private static final int DISPLAY_HEIGHT = 640;

    private void initialize(){
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mRecorder.setVideoEncodingBitRate(512 * 1000);
        mRecorder.setVideoFrameRate(30);
        mRecorder.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT);
        mRecorder.setOutputFile(Environment.getExternalStorageDirectory().getPath() + "test.mp4");
        isRunning = true;

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(mRecorder == null || !isRunning){
            initialize();
            mRecorder.start();
        } else mRecorder.stop();
    }

    public RecorderService(String name) {
        super(name);
    }

}
