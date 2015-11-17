package com.theif519.sakoverlay.Services;

import android.app.Notification;
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
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

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
     * having a direct string representation. It utilizes bitmasking to allow
     * more than one state to be compared, specifically for RecorderCommand.
     */
    public enum RecorderState {
        // Examples assume one byte.
        DEAD(1), // 0000 0001
        ALIVE(1 << 1), // 0000 0010
        INITIALIZED(1 << 2), // 0000 0100
        PREPARED(1 << 3), // 0000 1000
        STARTED(1 << 4), // 0001 0000
        PAUSED(1 << 5), // 0010 0000
        STOPPED(1 << 6); // 0100 0000

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
            return totalMask; // 0111 1111
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
     *
     * | = Bitwise OR
     *
     * & = Bitwise AND
     *
     * &~ = Bitwise NAND
     *
     * This utilizes a heavily modified version of the Command Design Pattern. It cannot be its own object
     * as it relies on private members of this service, and it would be a headache and a half to implement
     * it as one.
     */
    public enum RecorderCommand {
        // Examples assume one byte
        START( // 0110 1111
                RecorderState.getAllMask() &~ RecorderState.STARTED.getMask()
        ),
        PAUSE( // 0001 0000
                RecorderState.STARTED.getMask()
        ),
        STOP( // 0011 0000
                RecorderState.STARTED.getMask() | RecorderState.PAUSED.getMask()
        ),
        DIE( // 0111 1110
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

    private MediaProjection mProjection;

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
        Log.i(getClass().getName(), "Initializing Screen Recorder...");
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
            mRecorder.setOutputFile(getApplicationContext().getExternalFilesDir(null).getPath() + filename);
            changeState(RecorderState.INITIALIZED);
        } catch (RuntimeException ex) {
            logErrorAndChangeState(ex);
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        setupReceivers();
        setupForegroundNotification();
        changeState(RecorderState.ALIVE);
        //android.os.Debug.waitForDebugger();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * =Only used to tell the OS that we want to be restarted if we are killed. After the service is created,
     * any and all IPC is done through broadcasts, not startService().
     *
     * @param intent  Intent
     * @param flags   Flags
     * @param startId StartId
     * @return START_STICKY - Restart if killed by OS.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
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
        Log.i(getClass().getName(), "Asking host to obtain user's permission...");
        final LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        // We unregister the previous to prevent having more than one of the same receivers being answered.
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
                    mLastPermissionsReceiver = null;
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
            When we receive a command, we first determine whether or not the command is possible in the current
            given state. Thanks to the enumerations and clever use of bitmasking (I astound even myself),
            this is easily possible. If it is not possible to execute the given command, it sends back
            an extra message describing the error (in this case, the description is the attempted command
            and the current state). Otherwise, the command is handled.
         */
        broadcastManager.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Intent responseIntent = new Intent(Globals.Keys.RECORDER_COMMAND_RESPONSE_KEY);
                RecorderCommand command = (RecorderCommand) intent.getSerializableExtra(Globals.Keys.RECORDER_COMMAND_KEY);
                boolean isPossible = command.isPossible(mState);
                responseIntent.putExtra(Globals.Keys.RECORDER_COMMAND_EXECUTED_KEY, isPossible);
                Log.i(getClass().getName(), "Received Command Request: { "
                + "State : " + mState.toString() + ", Command : " + command.toString() + ", IsPossible: " + isPossible + " }");
                if(!isPossible){
                    responseIntent.putExtra(Globals.Keys.RECORDER_ERROR_MESSAGE_KEY,
                            "Cannot execute command " + command.toString() + " during state " + mState.toString());
                }
                broadcastManager.sendBroadcast(responseIntent);
                if(isPossible) handleCommand(command, intent);
            }
        }, new IntentFilter(Globals.Keys.RECORDER_COMMAND_REQUEST_KEY));
    }

    /**
     * Handles the command sent if and only if the command is possible. Note that there are no checks here,
     * as it was already checked before passing. Utilizing a simple switch statement, it is easy to basically
     * fall through each and handle them accordingly. Note also, in START, we must obtain permissions if we have not
     * done so already.
     * @param command Command to execute.
     * @param extras Intent passed along with command.
     */
    private void handleCommand(RecorderCommand command, final Intent extras) {
        Log.i(getClass().getName(), "Handling command: " + command.toString());
        switch (command) {
            case START:
                // Remember project is null only after we finish or if this is the first time running. Hence we acquire permissions.
                if(mProjection == null){
                    obtainPermissions(new PermissionsCallback() {
                        @Override
                        public void permissionsGranted(Intent intent) {
                            Log.i(getClass().getName(), "Obtained permissions from user!");
                            startRecording(extras.getIntExtra(Globals.Keys.WIDTH_KEY, 0),
                                    extras.getIntExtra(Globals.Keys.HEIGHT_KEY, 0), extras.getBooleanExtra(Globals.Keys.AUDIO_ENABLED_KEY, false),
                                    extras.getStringExtra(Globals.Keys.FILENAME_KEY));
                        }

                        @Override
                        public void permissionsDenied(Intent intent) {
                            Log.i(getClass().getName(), "Permission Denied, most likely due to bad input!");
                        }
                    });
                    return;
                }
                /*
                    Unfortunately I can't think of a better way to do this. We want to have startRecording called
                    at a later date, which by itself is a function call, but we also want it to call it now if projection
                    isn't null.
                 */
                startRecording(extras.getIntExtra(Globals.Keys.WIDTH_KEY, 0),
                        extras.getIntExtra(Globals.Keys.HEIGHT_KEY, 0), extras.getBooleanExtra(Globals.Keys.AUDIO_ENABLED_KEY, false),
                        extras.getStringExtra(Globals.Keys.FILENAME_KEY));
                break;
            case STOP:
                stopRecording();
                break;
            case DIE:
                mRecorder.reset();
                if(mDisplay != null){
                    mDisplay.release();
                    mDisplay = null;
                }
                mRecorder.release();
                mRecorder = null;
                mProjection.stop();
                mProjection = null;
                changeState(RecorderState.DEAD);
                stopSelf();
                break;
        }
    }

    /**
     * Used to prepare the recorder.
     */
    private void prepareRecorder() {
        Log.i(getClass().getName(), "Preparing Screen Recorder...");
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
        Log.i(getClass().getName(), "Creating VirtualDisplay...");
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
        Log.i(getClass().getName(), "Error-Checking parameters...");
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        Intent intent = new Intent(Globals.Keys.RECORDER_COMMAND_RESPONSE_KEY);
        String errMsg = null;
        if(width == 0 || height == 0){
            errMsg = "Width and Height cannot be 0!";
        } else if(filename == null || filename.isEmpty()){
            errMsg = "Filename cannot be null or empty!";
        } else intent.putExtra(Globals.Keys.RECORDER_COMMAND_EXECUTED_KEY, true);
        if(errMsg != null){
            intent.putExtra(Globals.Keys.RECORDER_ERROR_MESSAGE_KEY, errMsg);
            manager.sendBroadcast(intent);
            return;
        }
        manager.sendBroadcast(intent);
        Log.i(getClass().getName(), "Parameter-Check: OKAY!");
        initialize(width, height, audioEnabled, filename);
        prepareRecorder();
        try {
            mDisplay = createVirtualDisplay(width, height);
            mRecorder.start();
            Log.i(getClass().getName(), "Starting Screen Recorder...");
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
        Log.i(getClass().getName(), "Stopping Screen Recorder...");
        try {
            mRecorder.stop();
            Log.i(getClass().getName(), "Resetting Screen Recorder...");
            mRecorder.reset();
            Log.i(getClass().getName(), "Releasing VirtualDisplay...");
            mDisplay.release();
            mDisplay = null;
            changeState(RecorderState.STOPPED);
        } catch (IllegalStateException e) {
            logErrorAndChangeState(e);
        }
    }

    private void setupForegroundNotification() {
        Intent endIntent = new Intent(Globals.Keys.RECORDER_COMMAND_REQUEST_KEY);
        endIntent.putExtra(Globals.Keys.RECORDER_COMMAND_KEY, RecorderCommand.DIE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, endIntent, 0);
        Notification notification = new Notification.Builder(getApplicationContext())
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Tap to End Service!")
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
        startForeground(Globals.RECORDER_NOTIFICATION_ID, notification);
    }

}
