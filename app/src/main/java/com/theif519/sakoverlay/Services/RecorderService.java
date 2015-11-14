package com.theif519.sakoverlay.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.Toast;

import com.theif519.sakoverlay.FloatingFragments.ScreenRecorderFragment;
import com.theif519.utils.Logging.LogBuilder;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by theif519 on 11/12/2015.
 */
public class RecorderService extends Service {

    public enum RecorderState {
        DEAD("Dead"),
        ALIVE("Alive"),
        INITIALIZED("Initialized"),
        PREPARED("Prepared"),
        STARTED("Recording"),
        PAUSED("Paused"),
        STOPPED("Finished");

        private String mState;

        RecorderState(String state) {
            mState = state;
        }

        @Override
        public String toString() {
            return mState;
        }
    }

    public static final String RECORDER_STATE_CHANGE = "Recorder State Change";

    public static final String RECORDER_STATE_KEY = "Recorder State";

    private RecorderState mState = RecorderState.DEAD;

    private MediaProjection mProjection;

    private MediaProjectionManager mManager;

    private VirtualDisplay mDisplay;

    private MediaRecorder mRecorder;

    private static Logger mLogger;

    private static final int DISPLAY_WIDTH = 480;
    private static final int DISPLAY_HEIGHT = 640;

    private void initialize(int width, int height, boolean audioEnabled, String filename){
        mRecorder = new MediaRecorder();
        if(audioEnabled) mRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mRecorder.setVideoEncodingBitRate(512 * 1000);
        mRecorder.setVideoFrameRate(30);
        mRecorder.setVideoSize(width, height);
        mRecorder.setOutputFile("/sdcard/" + filename);
        changeState(RecorderState.INITIALIZED);

    }

    @Override
    public void onCreate() {
        super.onCreate();
        //android.os.Debug.waitForDebugger();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if(mLogger == null){
            try {
                mLogger = new LogBuilder(RecorderService.class.getName())
                        .addHandlers(new FileHandler(new File(getExternalFilesDir(null), "RecorderService.log").getPath()))
                        .setLevel(Level.ALL).build();
            } catch (IOException e) {
                // Remove later
                throw new RuntimeException(e);
            }
        }
        if(mProjection == null) {
            int resultCode = intent.getIntExtra(ScreenRecorderFragment.RESULT_CODE_KEY, -1);
            Intent data = (Intent) intent.getExtras().get(Intent.EXTRA_INTENT);
            mManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            mProjection = mManager.getMediaProjection(resultCode, data);
            mProjection.registerCallback(new MediaProjection.Callback() {
                @Override
                public void onStop() {
                    super.onStop();
                    Toast.makeText(getApplicationContext(), "Stopped recording!", Toast.LENGTH_LONG).show();
                }
            }, null);
        }
        if(mState == RecorderState.DEAD) changeState(RecorderState.ALIVE);
        return new RecorderBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }


    private void prepareRecorder() {
        try {
            mRecorder.prepare();
            changeState(RecorderState.PREPARED);
        } catch (IllegalStateException | IOException e) {
            logErrorAndChangeState(e);
        }
    }

    private VirtualDisplay createVirtualDisplay(){
        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
        return mProjection.createVirtualDisplay(getClass().getName(), DISPLAY_WIDTH, DISPLAY_HEIGHT,
                metrics.densityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mRecorder.getSurface(), null, null);
    }

    public RecorderState getState(){
        return mState;
    }

    /**
     * A simple subclass of Binder which allows the user to register onStateChangeListeners, which
     * are called whenever the state of the recording changes. I.E, can be used to update a text view
     * with the current state.
     */
    public class RecorderBinder extends Binder {

        /**
         * Returns the instance of the service bound to, singleton style.
         * @return Instance of RecorderService bound to.
         */
        public RecorderService getService() {
            return RecorderService.this;
        }

    }

    private void logErrorAndChangeState(Throwable ex){
        String msg = ex.getMessage() == null ? "" : ex.getMessage();
        mLogger.log(Level.SEVERE, "An Error of type: \"" + ex.getClass().getName() + "\" was thrown, during" +
                "the recorded state: \"" + mState.toString() + "\", with the message: \"" + msg + "\"!");
        changeState(RecorderState.DEAD);
    }

    private void changeState(RecorderState state){
        mState = state;
        Intent intent = new Intent(RECORDER_STATE_CHANGE);
        intent.putExtra(RECORDER_STATE_KEY, mState);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        mLogger.log(Level.INFO, "Sent Broadcast Receiver and State is " + mState.toString());
    }

    public void startRecording(int width, int height, boolean audioEnabled, String filename){
        initialize(width, height, audioEnabled, filename);
        prepareRecorder();
        try {
            mDisplay = createVirtualDisplay();
            mRecorder.start();
            changeState(RecorderState.STARTED);
        } catch (IllegalStateException e){
            logErrorAndChangeState(e);
        }
    }

    public void pauseRecording() {

    }

    @Override
    public void onDestroy() {
        mLogger.log(Level.INFO, "State: " + mState + ", being destroyed!");
        mLogger.getHandlers()[0].flush();
        super.onDestroy();
    }

    public void stopRecording() {
        try {
            mRecorder.stop();
            mRecorder.reset();
            mDisplay.release();
            changeState(RecorderState.STOPPED);
            stopSelf();
        } catch(IllegalStateException e){
            logErrorAndChangeState(e);
        }
    }

}
