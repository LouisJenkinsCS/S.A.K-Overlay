package com.theif519.sakoverlay.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.theif519.sakoverlay.Activities.MainActivity;
import com.theif519.sakoverlay.FloatingFragments.ScreenRecorderFragment;
import com.theif519.sakoverlay.Misc.Globals;
import com.theif519.sakoverlay.R;

import java.io.IOException;

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
     * having a direct string representation. For this example, my fragment has a
     * text-view which is updated with each state change.
     */
    public enum RecorderState {
        DEAD(1),
        ALIVE(1 << 1),
        INITIALIZED(1 << 2),
        PREPARED(1 << 3),
        STARTED(1 << 4),
        PAUSED(1 << 5),
        STOPPED(1 << 6);

        private int mMask;

        public int getMask() {
            return mMask;
        }

        /**
         * Very convenient method to get all masks at once, which allows getting all but one or two
         * super easy to do. It loops through each state then bitwise OR's them into one.
         * @return All bitmasks together.
         */
        public static int getAllMask(){
            int totalMask = 0;
            for(RecorderState state: values()){
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
                case ALIVE:
                    return "Alive";
                case INITIALIZED:
                    return "Initialized";
                case PREPARED:
                    return "Prepared";
                case STARTED:
                    return "Recording";
                case PAUSED:
                    return "Paused";
                case STOPPED:
                    return "Finished";
                default:
                    return null;
            }
        }
    }

    /**
     * This enumeration is used to not only represent a command, but also keeps track of the possible states
     * by bitwise OR'ing the states together to make a nice and efficient approach.
     */
    public enum RecorderCommand {
        START(
                RecorderState.getAllMask() &~ RecorderState.STARTED.getMask()
        ),
        PAUSE(
                RecorderState.STARTED.getMask()
        ),
        STOP(
                RecorderState.STARTED.getMask() | RecorderState.PAUSED.getMask()
        ),
        DIE(
                RecorderState.getAllMask() &~ RecorderState.DEAD.getMask()
        );

        /**
         * Determines whether or not the command is possible by checking if the bit for the possible state
         * is set.
         * @param state State to check.
         * @return True if it is a possible command for the given state.
         */
        public boolean isPossible(RecorderState state){
            return (mPossibleStatesMask & state.getMask()) != 0;
        }

        private int mPossibleStatesMask;

        RecorderCommand(int possibleStates) {
            mPossibleStatesMask = possibleStates;
        }

        @Override
        public String toString() {
            switch(this){
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

    /*
        Current state of recorder.
     */
    private RecorderState mState = RecorderState.DEAD;

    /*
        Whether notification is running or not.
     */
    private boolean mNotificationRunning = false;

    /*
        Callback to be called after the permission has been granted. As this service runs on the main thread,
        we do not have to worry about thread safety.
     */
    private PermissionsCallback mCallback;

    private MediaProjection mProjection;

    private MediaProjectionManager mManager;

    private VirtualDisplay mDisplay;

    private MediaRecorder mRecorder;

    /**
     * Basic initialization block, mostly obtained from a guide which I modified from.
     *
     * @param width        Width of the display
     * @param height       Height of the display
     * @param audioEnabled Whether or not audio was selected
     * @param filename     Name of file to create.
     */
    private void initialize(int width, int height, boolean audioEnabled, String filename) {
        try {
            mRecorder = new MediaRecorder();
            if (audioEnabled) mRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
            mRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            if (audioEnabled) mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setVideoEncodingBitRate(512 * 1000);
            mRecorder.setVideoFrameRate(30);
            mRecorder.setVideoSize(width, height);
            mRecorder.setOutputFile(Environment.getExternalStorageDirectory().getPath() + filename);
            changeState(RecorderState.INITIALIZED);
        } catch (RuntimeException ex) {
            logErrorAndChangeState(ex);
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        setupReceivers();
        //android.os.Debug.waitForDebugger();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * The entry point of our program, which occurs after startService() is called. I merely launch the notifications
     * if it is not already running, and setup the MediaProject if it is not already.
     *
     * @param intent  Intent
     * @param flags   Flags
     * @param startId StartId
     * @return START_STICKY - Restart if killed by OS.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!mNotificationRunning) {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(Globals.Keys.RECORDER_STATE_END_SERVICE_KEY), 0);
            Notification notification = new Notification.Builder(getApplicationContext())
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(getString(R.string.overlay_notification_text))
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .build();
            startForeground(Globals.RECORDER_NOTIFICATION_ID, notification);
            mNotificationRunning = true;
        }
        /*
            If the MediaProject is pointing to null, it means we either A) have just started, or B)
            ended the service, but the garbage collect hasn't collected us yet, hence we are being reused.
            So we must obtain permissions here.
         */
        if (mProjection == null) {
            obtainPermissions(null);
            int resultCode = intent.getIntExtra(ScreenRecorderFragment.RESULT_CODE_KEY, -1);
            Intent data = (Intent) intent.getExtras().get(Intent.EXTRA_INTENT);
            mManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            mProjection = mManager.getMediaProjection(resultCode, data);
        }
        if (mState == RecorderState.DEAD) changeState(RecorderState.ALIVE);
        return START_STICKY;
    }

    private BroadcastReceiver mLastPermissionsReceiver;


    /**
     * This method is asynchronous in nature, as registering the receiver is as well, same with broadcasting.
     * Hence, we declare a callback to be called when finished.
     *
     * @param callback Callback to be called once received.
     */
    private void obtainPermissions(final PermissionsCallback callback) {
        final LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        if (mLastPermissionsReceiver != null) manager.unregisterReceiver(mLastPermissionsReceiver);
        manager.registerReceiver(mLastPermissionsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                /*
                    Labeled break for if we get bad input, it will skip this block straight to the bottom.
                 */
                badInput:
                if (intent.getBooleanExtra(Globals.Keys.RECORDER_PERMISSIONS_RESPONSE_KEY, false)) {
                    int resultCode = intent.getIntExtra(ScreenRecorderFragment.RESULT_CODE_KEY, -1);
                    Intent data = (Intent) intent.getExtras().get(Intent.EXTRA_INTENT);
                    // If we get bad input, we count it as a permission failure.
                    if (data == null) break badInput;
                    mProjection = ((MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE)).getMediaProjection(
                            resultCode, data
                    );
                    if (mProjection == null) break badInput;
                    if (callback != null) callback.permissionsGranted(intent);
                    manager.unregisterReceiver(this);
                    return;
                }
                /*
                    If we get bad input or if the response is false, we call permissionDenied callback if
                    declared, then unregister this listener and return.
                 */
                if (callback != null) callback.permissionsDenied(intent);
                manager.unregisterReceiver(this);
                // We already processed our permission request, remove it so it will not be unregistered twice.
                mLastPermissionsReceiver = null;
            }
        }, new IntentFilter(Globals.Keys.RECORDER_PERMISSIONS_RESPONSE_KEY));
        /*
            We send the broadcast after to prevent any race conditions where we do not register a listener before we send
            the broadcast to receive one.
         */
        manager.sendBroadcast(new Intent(Globals.Keys.RECORDER_PERMISSIONS_REQUEST_KEY));
    }

    private void setupReceivers() {
        final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        /*
            When we receive a request for the current state, we send a response with the state
            in an intent.
         */
        broadcastManager.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Intent responseIntent = new Intent(Globals.Keys.RECORDER_STATE_RESPONSE_KEY);
                responseIntent.putExtra(Globals.Keys.RECORDER_STATE_KEY, mState);
                broadcastManager.sendBroadcast(intent);
            }
        }, new IntentFilter(Globals.Keys.RECORDER_STATE_REQUEST_KEY));
        /*
            When we receive a request to change the state, first we check if the state is eligible,
            and if we are not we send back false with an extra message explaining why we cannot do so.
            Otherwise we send back true.

            Note: We reuse the Intent-Filter RECORDER_STATE_CHANGE_RESPONSE_KEY for the key to hold
            whether or not we changed our state or not. Lack of creativity.
         */
        broadcastManager.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Intent responseIntent = new Intent(Globals.Keys.RECORDER_STATE_CHANGE_RESPONSE_KEY);
                RecorderState state = (RecorderState) intent.getSerializableExtra(Globals.Keys.RECORDER_STATE_KEY);
                responseIntent.putExtra(Globals.Keys.RECORDER_STATE_CHANGE_RESPONSE_KEY, true);
                broadcastManager.sendBroadcast(intent);
                handleStateChange(state);
            }
        }, new IntentFilter(Globals.Keys.RECORDER_STATE_CHANGE_REQUEST_KEY));
        broadcastManager.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // We end the current service based on state here, then stop self!
                stopSelf();
                ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(Globals.RECORDER_NOTIFICATION_ID);
                changeState(RecorderState.DEAD);

            }
        }, new IntentFilter(Globals.Keys.RECORDER_STATE_END_SERVICE_KEY));
    }

    private void handleStateChange(RecorderState state) {
        switch (mState) {
            case DEAD:
                if ()
        }
    }

    /**
     * Used to prepare the recorder.
     */
    private void prepareRecorder() {
        try {
            mRecorder.prepare();
            changeState(RecorderState.PREPARED);
        } catch (IllegalStateException | IOException e) {
            logErrorAndChangeState(e);
        }
    }

    /**
     * Convenience method to create a virtual display.
     *
     * @param width  Width of display.
     * @param height Heigth of virtual display.
     * @return Initialized virtual display.
     */
    private VirtualDisplay createVirtualDisplay(int width, int height) {
        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
        return mProjection.createVirtualDisplay(getClass().getName(), width, height,
                metrics.densityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mRecorder.getSurface(), null, null);
    }

    public RecorderState getState() {
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
         *
         * @return Instance of RecorderService bound to.
         */
        public RecorderService getService() {
            return RecorderService.this;
        }

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
        changeState(RecorderState.DEAD);
        stopSelf();
    }

    /**
     * Change the state and broadcast it to any listeners.
     *
     * @param state The state of the recorder.
     */
    private void changeState(RecorderState state) {
        mState = state;
        Intent intent = new Intent(Globals.Keys.RECORDER_STATE_CHANGE_KEY);
        intent.putExtra(Globals.Keys.RECORDER_STATE_KEY, mState);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        Log.i(getClass().getName(), "Sent Broadcast Receiver and State is " + mState.toString());
    }

    /**
     * Begin recording. This is called by the Fragment which is bound to this service.
     *
     * @param width        Width.
     * @param height       Height.
     * @param audioEnabled Is Audio Enabled?
     * @param filename     Name of file.
     */
    public void startRecording(int width, int height, boolean audioEnabled, String filename) {
        initialize(width, height, audioEnabled, filename);
        prepareRecorder();
        try {
            mDisplay = createVirtualDisplay(width, height);
            mRecorder.start();
            changeState(RecorderState.STARTED);
        } catch (IllegalStateException e) {
            logErrorAndChangeState(e);
        }
    }

    public void pauseRecording() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * Cease and desist all recording this instant!
     * <p/>
     * Stops the recorder, resets it, then releases it, which SHOULD be the in the right order
     * of it's state. However, the issue is when I call changeState(), for some reason I get an ANR
     * and the systemui goes under??? Why.
     */
    public void stopRecording() {
        try {
            mRecorder.stop();
            mDisplay.release();
            mRecorder.release();
            mProjection.stop();
            changeState(RecorderState.STOPPED);
        } catch (IllegalStateException e) {
            logErrorAndChangeState(e);
        }
    }

    private void setupForegroundNotification() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(getApplicationContext())
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.overlay_notification_text))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
                .setContentIntent(pIntent)
                .setOngoing(true)
                .build();
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1, notification);
        //startForeground(1, notification);
    }

}
