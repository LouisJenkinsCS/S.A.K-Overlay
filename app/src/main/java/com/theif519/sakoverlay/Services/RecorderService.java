package com.theif519.sakoverlay.Services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.theif519.sakoverlay.Activities.PermissionActivity;
import com.theif519.sakoverlay.Misc.Globals;
import com.theif519.sakoverlay.POD.PermissionInfo;
import com.theif519.sakoverlay.R;
import com.theif519.sakoverlay.Rx.RxBus;

import java.io.IOException;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.subjects.PublishSubject;

/**
 * Created by theif519 on 11/12/2015.
 */
public class RecorderService extends Service {

    public interface PermissionsCallback {
        void permissionsGranted(Intent intent);

        void permissionsDenied(Intent intent);
    }

    /**
     * Enumerations used to describe the current state of the object, even
     * having a direct string representation. It utilizes bitmasking to allow
     * more than one state to be compared, specifically for RecorderCommand.
     */
    public enum RecorderState {
        // Examples assume one byte.
        DEAD(1), // 0000 0001
        STARTED(1 << 1), // 0001 0000
        PAUSED(1 << 2), // 0010 0000
        STOPPED(1 << 3); // 0100 0000

        private int mMask;

        public int getMask() {
            return mMask;
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

        RecorderState(int bitmask) {
            mMask = bitmask;
        }

        @Override
        public String toString() {
            switch (this) {
                case DEAD:
                    return "Dead";
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
     * <p/>
     * | = Bitwise OR
     * <p/>
     * & = Bitwise AND
     * <p/>
     * &~ = Bitwise NAND
     */
    public enum RecorderCommand {
        START(
                RecorderState.getAllMask() & ~RecorderState.STARTED.getMask()
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

        private int mPossibleStatesMask;

        RecorderCommand(int possibleStates) {
            mPossibleStatesMask = possibleStates;
        }

        @Override
        public String toString() {
            switch (this) {
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

    public class RecorderBinder extends Binder {
        public RecorderService getService() {
            return RecorderService.this;
        }

        public Observable<RecorderState> observeStateChanges(){
            return mStateChangeObserver.asObservable();
        }
    }

    /*
        Current state of recorder.
     */
    private RecorderState mState = RecorderState.DEAD;

    private MediaProjection mProjection;

    private VirtualDisplay mDisplay;

    private MediaRecorder mRecorder;

    private Subscription mPermissionSubscriber;

    private PublishSubject<RecorderState> mStateChangeObserver;

    /**
     * Basic initialization block, mostly obtained from a guide which I modified from.
     *
     * @param width        Width of the display
     * @param height       Height of the display
     * @param audioEnabled Whether or not audio was selected
     * @param fileName     Name of file to create.
     */
    private boolean initialize(int width, int height, boolean audioEnabled, String fileName) {
        Log.i(getClass().getName(), "Initializing Screen Recorder...");
        try {
            mRecorder = new MediaRecorder();
            if (audioEnabled) mRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
            mRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            if(audioEnabled) mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mRecorder.setVideoEncodingBitRate(512 * 1000);
            mRecorder.setVideoFrameRate(30);
            mRecorder.setVideoSize(width, height);
            mRecorder.setOutputFile(Globals.RECORDER_FILE_SAVE_PATH + fileName);
            return true;
        } catch (RuntimeException ex) {
            logErrorAndChangeState(ex);
            return false;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mPermissionSubscriber = RxBus.await(PermissionInfo.class).subscribe(new Action1<PermissionInfo>() {
            @Override
            public void call(PermissionInfo permissionInfo) {
                if (mProjection == null) {
                    mProjection = ((MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE))
                            .getMediaProjection(permissionInfo.getResultCode(), permissionInfo.getIntent());
                }
                mPermissionSubscriber.unsubscribe();
            }
        });
        mStateChangeObserver = PublishSubject.create();
        setupForegroundNotification();
        changeState(RecorderState.STOPPED);
        //android.os.Debug.waitForDebugger();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new RecorderBinder();
    }

    /**
     * Only used to tell the OS that we want to be restarted if we are killed. After the service is created,
     * any and all IPC is done through broadcasts, not startService().
     *
     * @param intent  Intent
     * @param flags   Flags
     * @param startId StartId
     * @return START_STICKY - Restart if killed by OS.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    /**
     * Convenience method to create a virtual display.
     *
     * @param width  Width of display.
     * @param height Heigth of virtual display.
     * @return Initialized virtual display.
     */
    private VirtualDisplay createVirtualDisplay(int width, int height) {
        Log.i(getClass().getName(), "Creating Virtual Display...");
        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
        return mProjection.createVirtualDisplay(getClass().getName(), width, height,
                metrics.densityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mRecorder.getSurface(), null, null);
    }

    public RecorderState getState() {
        return mState;
    }

    /**
     * Used to log any errors and handle it by changing and broadcasting it's state too, although not sure
     * if it would cause a crash or not.
     *
     * @param ex Exception.
     */
    private void logErrorAndChangeState(Throwable ex) {
        String msg = ex.getMessage() == null ? "" : ex.getMessage();
        Log.wtf(getClass().getName(), "An Error of type: \"" + ex.getClass().getName() + "\" was thrown, during" +
                "the recorded state: \"" + mState.toString() + "\", with the message: \"" + msg + "\"!", ex);
        Toast.makeText(RecorderService.this, "Error->\"" + ex.getMessage() + "\"", Toast.LENGTH_LONG).show();
        die();
    }

    /**
     * Change the state and broadcast it to any listeners.
     *
     * @param state The state of the recorder.
     */
    private void changeState(RecorderState state) {
        mState = state;
        mStateChangeObserver.onNext(state);
        Log.i(getClass().getName(), "Sent Broadcast Receiver and State is " + mState.toString());
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mPermissionSubscriber.unsubscribe();
    }

    private void setupForegroundNotification() {
        Intent endIntent = new Intent(Globals.Keys.RECORDER_COMMAND_REQUEST);
        endIntent.putExtra(Globals.Keys.RECORDER_COMMAND, RecorderCommand.DIE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, endIntent, 0);
        Notification notification = new Notification.Builder(getApplicationContext())
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Tap to End Service!")
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.sak_overlay_icon))
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
        startForeground(Globals.RECORDER_NOTIFICATION_ID, notification);
    }

    public void die(){
        if(!RecorderCommand.DIE.isPossible(mState)) return;
        if(mRecorder != null){
            mRecorder.reset();
            mRecorder.release();
        }
        if (mDisplay != null) {
            mDisplay.release();
        }
        if(mProjection != null) {
            mProjection.stop();
        }
        changeState(RecorderState.DEAD);
        stopSelf();
    }

    public boolean stop(){
        if(!RecorderCommand.STOP.isPossible(mState)) return false;
        try {
            Log.i(getClass().getName(), "Stopping recorder...");
            mRecorder.stop();
            Log.i(getClass().getName(), "Resetting Screen Recorder...");
            mRecorder.reset();
            Log.i(getClass().getName(), "Releasing VirtualDisplay...");
            mDisplay.release();
            mDisplay = null;
            changeState(RecorderState.STOPPED);
            return true;
        } catch (IllegalStateException e) {
            logErrorAndChangeState(e);
            return false;
        }
    }

    public boolean start(int width, int height, boolean audioEnabled, String fileName) {
        if(!RecorderCommand.START.isPossible(mState)) return false;
        Log.i(getClass().getName(), "Checking for permissions...");
        if (mProjection == null) {
            Log.i(getClass().getName(), "Starting activity for permission...");
            Intent intent = new Intent(this, PermissionActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return false;
        }
        String errMsg;
        if(!(errMsg = checkStartParameters(width, height, fileName)).isEmpty()){
            Toast.makeText(RecorderService.this, errMsg, Toast.LENGTH_LONG).show();
            return false;
        }
        if(!initialize(width, height, audioEnabled, fileName)){
            return false;
        }
        try {
            Log.i(getClass().getName(), "Preparing Recorder...");
            mRecorder.prepare();
            mDisplay = createVirtualDisplay(width, height);
            Log.i(getClass().getName(), "Started!");
            mRecorder.start();
            changeState(RecorderState.STARTED);
        } catch (IOException | IllegalStateException e) {
            logErrorAndChangeState(e);
            return false;
        }
        return true;
    }

    private String checkStartParameters(int width, int height, String fileName){
        StringBuilder errMsg = new StringBuilder();
        if(width == 0){
            errMsg.append("Width must be larger than or equal to 0!\n");
        }
        if(height == 0){
            errMsg.append("Height must be large than or equal to 0!\n");
        }
        if(fileName == null){
            errMsg.append("Filename cannot be left null!");
        }
        if(fileName != null && fileName.isEmpty()){
            errMsg.append("Filename cannot be left empty!");
        }
        return errMsg.toString();
    }

}
