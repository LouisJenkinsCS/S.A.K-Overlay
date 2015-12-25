package com.theif519.sakoverlay.Sessions;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
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

    /**
     * Enumerations used to describe the current state of the object, even
     * having a direct string representation. It uses a bitmask to allow
     * more than one state to be compared, specifically for RecorderCommand.
     */
    public enum RecorderState {
        DEAD(1),
        PREPARED(1 << 1),
        STARTED(1 << 2),
        PAUSED(1 << 3),
        STOPPED(1 << 4);

        private int mMask;

        RecorderState(int bitmask) {
            mMask = bitmask;
        }

        /**
         * Very convenient method to get all masks at once, which allows getting all but one or two
         * super easy to do. It loops through each state then bitwise OR's them into one.
         *
         * @return All bitmasks together.
         */
        public static int getAllMask() {
            int totalMask = 0;
            for (RecorderState state : values()) {
                totalMask |= state.getMask();
            }
            return totalMask;
        }

        public int getMask() {
            return mMask;
        }

        @Override
        public String toString() {
            switch (this) {
                case DEAD:
                    return "Dead";
                case PREPARED:
                    return "Prepared";
                case STARTED:
                    return "Recording";
                case PAUSED:
                    return "Paused";
                case STOPPED:
                    return "Stopped";
                default:
                    return null;
            }
        }
    }

    /**
     * This enumeration is used to encapsulate a given command from a bound activity/fragment, and
     * is used as a helper to determine whether or not given command is possible given the current state.
     * <p>
     * | = Bitwise OR
     * <p>
     * & = Bitwise AND
     * <p>
     * &~ = Bitwise NAND
     */
    public enum RecorderCommand {
        PREPARE(
                RecorderState.DEAD.getMask() | RecorderState.STOPPED.getMask()
        ),
        START(
                RecorderState.PREPARED.getMask()
        ),
        PAUSE(
                RecorderState.STARTED.getMask()
        ),
        STOP(
                RecorderState.STARTED.getMask() | RecorderState.PAUSED.getMask()
        ),
        DIE(
                RecorderState.getAllMask() & ~RecorderState.DEAD.getMask()
        );

        private int mPossibleStatesMask;

        RecorderCommand(int possibleStates) {
            mPossibleStatesMask = possibleStates;
        }

        /**
         * Determines whether or not the command is possible by checking if the bit for the possible state
         * is set.
         *
         * @param state State to check.
         * @return True if it is a possible command for the given state.
         */
        public boolean isPossible(RecorderState state) {
            return (mPossibleStatesMask & state.getMask()) != 0;
        }

        @Override
        public String toString() {
            switch (this) {
                case PREPARE:
                    return "Prepare";
                case START:
                    return "Start";
                case PAUSE:
                    return "Pause";
                case STOP:
                    return "Stop";
                case DIE:
                    return "Die";
                default:
                    return null;
            }
        }
    }

    private static final String TAG = RecordingSession.class.getName();

    private RecorderState mState = RecorderState.DEAD;
    private BehaviorSubject<RecorderState> mStateChanges;
    private StringBuilder mErrorMessage;
    private MediaProjection mProjection;
    private VirtualDisplay mDisplay;
    private MediaRecorder mRecorder;
    private WeakReference<Context> mContext;

    public RecordingSession(Context context, PermissionInfo permissionInfo) {
        mContext = new WeakReference<>(context);
        mRecorder = new MediaRecorder();
        mProjection = ((MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE))
                .getMediaProjection(permissionInfo.getResultCode(), permissionInfo.getIntent());
        if (mProjection == null) {
            throw new IllegalStateException("MediaProjectionManager returned null for the passed permissionInfo");
        }
        mStateChanges = BehaviorSubject.create();
        mState = RecorderState.DEAD;
        mStateChanges.onNext(mState);
        mErrorMessage = new StringBuilder();
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
                metrics.densityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mRecorder.getSurface(), null, null);
    }

    public boolean prepare(RecorderInfo info) {
        if(!isPossible(RecorderCommand.PREPARE)){
            invalidCommand(RecorderCommand.PREPARE);
            return false;
        }
        if(!info.isValid(mErrorMessage)){
            return false;
        }
        reset();
        try {
            mRecorder = new MediaRecorder();
            if (info.isAudioEnabled()) mRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
            mRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            if (info.isAudioEnabled()) mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setVideoFrameRate(60);
            mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mRecorder.setVideoSize(info.getWidth(), info.getHeight());
            mRecorder.setOutputFile(Globals.RECORDER_FILE_SAVE_PATH + info.getFileName());
            mRecorder.setVideoEncodingBitRate(4 * 1000 * 1000);
            Log.i(TAG, "Preparing Recorder...");
            mRecorder.prepare();
            Log.i(TAG, "Creating Virtual Display...");
            mDisplay = createVirtualDisplay(info.getWidth(), info.getHeight());
        } catch (RuntimeException | IOException ex) {
            logErrorAndChangeState(ex);
            return false;
        }
        changeState(RecorderState.PREPARED);
        return true;
    }

    public boolean start() {
        if(!isPossible(RecorderCommand.START)){
            invalidCommand(RecorderCommand.START);
            return false;
        }
        try {
            Log.i(getClass().getName(), "Started!");
            mRecorder.start();
            changeState(RecorderState.STARTED);
        } catch (IllegalStateException e) {
            logErrorAndChangeState(e);
            return false;
        }
        return true;
    }

    public boolean stop() {
        if(!isPossible(RecorderCommand.STOP)){
            invalidCommand(RecorderCommand.STOP);
            return false;
        }
        try {
            Log.i(TAG, "Stopping Projection...");
            mProjection.stop();
            Log.i(TAG, "Stopping Recorder...");
            mRecorder.stop();
            Log.i(TAG, "Resetting Screen Recorder...");
            mRecorder.reset();
            Log.i(TAG, "Releasing Media Recorder...");
            mRecorder.release();
            mRecorder = null;
            Log.i(TAG, "Releasing VirtualDisplay...");
            mDisplay.release();
            mDisplay = null;
            changeState(RecorderState.STOPPED);
            return true;
        } catch (IllegalStateException e) {
            logErrorAndChangeState(e);
            return false;
        }
    }

    public void reset(){
        if(mRecorder != null){
            mRecorder.reset();
            mRecorder.release();
            mRecorder = null;
        }
        if(mDisplay != null){
            mDisplay.release();
            mDisplay = null;
        }
        changeState(RecorderState.STOPPED);
    }

    public void release(){
        reset();
        changeState(RecorderState.DEAD);
    }

    private void changeState(RecorderState state){
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

    public Observable<RecorderState> observeStateChanges(){
        return mStateChanges.asObservable();
    }

    public boolean isPossible(RecorderCommand command){
        return command.isPossible(mState);
    }

    private void invalidCommand(RecorderCommand command){
        mErrorMessage.delete(0, mErrorMessage.length());
        mErrorMessage.append("Invalid { Command -> \"");
        mErrorMessage.append(command);
        mErrorMessage.append("\" : State -> \"");
        mErrorMessage.append(mState);
        mErrorMessage.append("\" }");
    }
}
