package com.theif519.sakoverlay.Sessions.Recording;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.theif519.sakoverlay.Misc.Globals;
import com.theif519.sakoverlay.POJO.PermissionInfo;

import java.io.IOException;
import java.lang.ref.WeakReference;

import rx.Observable;
import rx.subjects.BehaviorSubject;

/**
 * Created by theif519 on 12/24/2015.
 */
public class RecordingSession {

    private static final String TAG = RecordingSession.class.getName();

    private RecordingState mState = RecordingState.DEAD;
    private BehaviorSubject<RecordingState> mStateChanges;
    private StringBuilder mErrorMessage;
    private MediaProjection mProjection;
    private VirtualDisplay mDisplay;
    private MediaRecorder mRecording;
    private WeakReference<Context> mContext;
    private PermissionInfo mPermissionInfo;

    public RecordingSession(@NonNull Context context, @NonNull PermissionInfo permissionInfo) {
        mContext = new WeakReference<>(context);
        mRecording = new MediaRecorder();
        mStateChanges = BehaviorSubject.create();
        mState = RecordingState.DEAD;
        mStateChanges.onNext(mState);
        mErrorMessage = new StringBuilder();
        mPermissionInfo = permissionInfo;
    }

    /**
     * Convenience method to create a virtual display.
     *
     * @param width  Width of display.
     * @param height Heigth of virtual display.
     * @return Initialized virtual display.
     */
    private VirtualDisplay createVirtualDisplay(int width, int height) {
        Context context = mContext.get();
        if (context == null) return null;
        Log.i(getClass().getName(), "Creating Virtual Display...");
        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
        return mProjection.createVirtualDisplay(getClass().getName(), width, height,
                metrics.densityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mRecording.getSurface(), null, null);
    }

    public boolean prepare(RecordingInfo info) {
        if(!isPossible(RecordingCommand.PREPARE)){
            invalidCommand(RecordingCommand.PREPARE);
            return false;
        }
        if(!info.isValid(mErrorMessage)){
            return false;
        }
        reset();
        Context context = mContext.get();
        if(context == null){
            mErrorMessage.delete(0, mErrorMessage.length());
            mErrorMessage.append("Context has been garbage collected! Probably memory leak detected!");
            return false;
        }
        mProjection = ((MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE))
                .getMediaProjection(mPermissionInfo.getResultCode(), mPermissionInfo.getIntent());
        if (mProjection == null) {
            throw new IllegalStateException("MediaProjectionManager returned null for the passed permissionInfo");
        }
        try {
            mRecording = new MediaRecorder();
            if (info.isAudioEnabled()) mRecording.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
            mRecording.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mRecording.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            if (info.isAudioEnabled()) mRecording.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecording.setVideoFrameRate(60);
            mRecording.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mRecording.setVideoSize(info.getWidth(), info.getHeight());
            mRecording.setOutputFile(Globals.RECORDER_FILE_SAVE_PATH + info.getFileName());
            mRecording.setVideoEncodingBitRate(4 * 1000 * 1000);
            Log.i(TAG, "Preparing Recording...");
            mRecording.prepare();
            Log.i(TAG, "Creating Virtual Display...");
            mDisplay = createVirtualDisplay(info.getWidth(), info.getHeight());
        } catch (RuntimeException | IOException ex) {
            logErrorAndChangeState(ex);
            return false;
        }
        changeState(RecordingState.PREPARED);
        return true;
    }

    public boolean start() {
        if(!isPossible(RecordingCommand.START)){
            invalidCommand(RecordingCommand.START);
            return false;
        }
        try {
            Log.i(getClass().getName(), "Started!");
            mRecording.start();
            changeState(RecordingState.STARTED);
        } catch (IllegalStateException e) {
            logErrorAndChangeState(e);
            return false;
        }
        return true;
    }

    public boolean stop() {
        if(!isPossible(RecordingCommand.STOP)){
            invalidCommand(RecordingCommand.STOP);
            return false;
        }
        try {
            Log.i(TAG, "Stopping Projection...");
            mProjection.stop();
            Log.i(TAG, "Stopping Recording...");
            mRecording.stop();
            Log.i(TAG, "Resetting Screen Recording...");
            mRecording.reset();
            Log.i(TAG, "Releasing Media Recording...");
            mRecording.release();
            mRecording = null;
            Log.i(TAG, "Releasing VirtualDisplay...");
            mDisplay.release();
            mDisplay = null;
            changeState(RecordingState.STOPPED);
            return true;
        } catch (IllegalStateException e) {
            logErrorAndChangeState(e);
            return false;
        }
    }

    public void reset(){
        if(mRecording != null){
            mRecording.reset();
            mRecording.release();
            mRecording = null;
        }
        if(mDisplay != null){
            mDisplay.release();
            mDisplay = null;
        }
        if(mProjection != null){
            mProjection.stop();
            mProjection = null;
        }
        changeState(RecordingState.STOPPED);
    }

    public void release(){
        reset();
        changeState(RecordingState.DEAD);
    }

    private void changeState(RecordingState state){
        mState = state;
        mStateChanges.onNext(mState);
    }

    public String getLastErrorMessage(){
        String msg = mErrorMessage.toString();
        return msg.isEmpty() ? "No Error!" : msg;
    }

    /**
     * Used to log any errors and handle it by changing and broadcasting it's state too, although not sure
     * if it would cause a crash or not.
     *
     * @param ex Exception.
     */
    private void logErrorAndChangeState(Throwable ex) {
        String msg = ex.getMessage() == null ? "" : ex.getMessage();
        mErrorMessage.delete(0, mErrorMessage.length());
        mErrorMessage.append(msg);
        Log.wtf(getClass().getName(), "An Error of type: \"" + ex.getClass().getName() + "\" was thrown, during" +
                "the recorded state: \"" + mState.toString() + "\", with the message: \"" + msg + "\"!", ex);
        release();
    }

    public Observable<RecordingState> observeStateChanges(){
        return mStateChanges.asObservable();
    }

    public boolean isPossible(RecordingCommand command){
        return command.isPossible(mState);
    }

    private void invalidCommand(RecordingCommand command){
        mErrorMessage.delete(0, mErrorMessage.length());
        mErrorMessage.append("Invalid { Command -> \"");
        mErrorMessage.append(command);
        mErrorMessage.append("\" : State -> \"");
        mErrorMessage.append(mState);
        mErrorMessage.append("\" }");
    }
}
