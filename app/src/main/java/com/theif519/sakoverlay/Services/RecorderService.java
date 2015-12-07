package com.theif519.sakoverlay.Services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.theif519.sakoverlay.Activities.PermissionActivity;
import com.theif519.sakoverlay.Misc.Globals;
import com.theif519.sakoverlay.POD.PermissionInfo;
import com.theif519.sakoverlay.POD.RecorderInfo;
import com.theif519.sakoverlay.R;
import com.theif519.sakoverlay.Rx.RxBus;

import java.io.IOException;

import rx.Observable;
import rx.functions.Action1;
import rx.subjects.PublishSubject;

/**
 * Created by theif519 on 11/12/2015.
 *
 * RecorderService is a service made explicitly for capturing and maintaining a video stream
 * while in the application is in the background. It uses the API level 21 MediaProjection API
 * to accomplish this, and maintains the state of the current recording.
 */
public class RecorderService extends Service {

    /**
     * Enumerations used to describe the current state of the object, even
     * having a direct string representation. It uses a bitmask to allow
     * more than one state to be compared, specifically for RecorderCommand.
     */
    public enum RecorderState {
        DEAD(1),
        STARTED(1 << 1),
        PAUSED(1 << 2),
        STOPPED(1 << 3);

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

    /**
     * The IBinder returned when we are bound to an activity/fragment. It allows who we are bound to
     * to maintain a handle to this instance (which in and of itself allows it to manipulate the current state)
     * as well as an observable to be notified on any state changes.
     */
    public class RecorderBinder extends Binder {
        public RecorderService getService() {
            return RecorderService.this;
        }

        public Observable<RecorderState> observeStateChanges() {
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

    /*
        Is used to publish/subscribe any state changes to the recorder.
     */
    private PublishSubject<RecorderState> mStateChangeObserver;

    /**
     *  Initializes the MediaRecorder with the user's requested data if it gets through the checks
     *  in start().
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
            if (audioEnabled) mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
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
        /*
            Whenever we receive a permission response, if the MediaProjection instance is null, we initialize it here.
         */
       RxBus.subscribe(PermissionInfo.class).subscribe(new Action1<PermissionInfo>() {
            @Override
            public void call(PermissionInfo permissionInfo) {
                if (mProjection == null) {
                    mProjection = ((MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE))
                            .getMediaProjection(permissionInfo.getResultCode(), permissionInfo.getIntent());
                }
            }
        });
        mStateChangeObserver = PublishSubject.create();
        setupForegroundNotification();
        setupFloatingView();
        changeState(RecorderState.STOPPED);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new RecorderBinder();
    }

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
        Log.i(getClass().getName(), "Published to Subscribers that the State is " + mState.toString());
    }

    /**
     * As it is possible for S.A.K-Overlay to be killed in the background while recording, adding a Foreground
     * notification reduces the chances of it happening drastically. As the service run locally in the same process,
     * S.A.K-Overlay will not be killed off unless the service is as well.
     *
     * Consequently, as the overlay can be quite heavy on RAM, making it a juicier target for the OOM killer, this service,
     * if the system is down to critically low memory, is also more likely to be killed as well. I will hopefully remedy this
     * by creating a separate process for this to run in, but that by itself comes with a lot of complication as well.
     */
    private void setupForegroundNotification() {
        Intent endIntent = new Intent(Globals.Keys.RECORDER_COMMAND_REQUEST);
        endIntent.putExtra(Globals.Keys.RECORDER_COMMAND, RecorderCommand.DIE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, endIntent, 0);
        Notification notification = new Notification.Builder(getApplicationContext())
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Generic Foreground Notification")
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.sak_overlay_icon))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
        startForeground(Globals.RECORDER_NOTIFICATION_ID, notification);
    }

    /**
     * We add the view controller to the WindowManager, allowing us to draw over other applications.
     * The controller can control the state of the recorder, but also is controlled by the state changes
     * as well. I.E, while in STOPPED, it can call START, but if say the ScreenRecorderFragment makes a state change,
     * then the controller's next option will be STOP as the recording will have already been STARTED.
     *
     * Pretty much what I am getting at is that while the observable acts in an asynchronous manner, the controller
     * acts in a synchronized way with the recorder's state.
     */
    private void setupFloatingView() {
        final WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 0;
        final ViewGroup layout = (ViewGroup) ((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.screen_recorder_controller_view, null);
        final ImageButton controller = (ImageButton) layout.findViewById(R.id.screen_recorder_controller_button);
        final TextView stateText = (TextView) layout.findViewById(R.id.screen_recorder_controller_state);
        mStateChangeObserver // Luckily, Rx Observables can also be observed from the same thread, easily.
                .subscribe(new Action1<RecorderState>() { // Here we handle any state changes below.
                    @Override
                    public void call(RecorderState recorderState) {
                        stateText.setText(recorderState.toString());
                        switch (recorderState) {
                            case DEAD:
                                manager.removeView(layout);
                                break;
                            case STARTED:
                                controller.setImageResource(R.drawable.stop);
                                break;
                            case STOPPED:
                                controller.setImageResource(R.drawable.play);
                                break;
                        }
                    }
                });
        /*
            Interesting note: The WindowManager does not need its own bounds checking. Man, if only that were the case
            with scaled views, my life would have been made 10x easier.
        */
        controller.setOnTouchListener(new View.OnTouchListener() {
            int initialX, initialY, initialTouchY, initialTouchX;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                /*
                    If it is not a single tap, then assume it is a move event.
                 */
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = (int) event.getRawX();
                        initialTouchY = (int) event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        if((int) (event.getRawX()) == initialTouchX || (int)(event.getRawY()) == initialTouchY){
                            switch (mState) {
                                case STARTED:
                                    stop();
                                    break;
                                case STOPPED:
                                    if (mLastRecorderInfo != null) {
                                        start(mLastRecorderInfo);
                                    } else {
                                        Toast.makeText(RecorderService.this, "Requires previous recording session to start!", Toast.LENGTH_SHORT).show();
                                    }
                            }
                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        manager.updateViewLayout(layout, params);
                        return true;
                    default:
                        return false;
                }
            }
        });
        manager.addView(layout, params);
    }

    /**
     * Sends the command to die, which will release any and all resources if they are currently being
     * held, changes its state to DEAD, stops the foreground notification, and then stops itself.
     */
    public void die() {
        if (!RecorderCommand.DIE.isPossible(mState)) return;
        if (mRecorder != null) { // Note that if we call release() without reset(), it may throw an IllegalStateException
            mRecorder.reset();
            mRecorder.release();
        }
        if (mDisplay != null) {
            mDisplay.release();
        }
        if (mProjection != null) {
            mProjection.stop();
        }
        changeState(RecorderState.DEAD);
        stopForeground(true);
        stopSelf();
    }

    /**
     * Stops the recording if it's already started.
     * @return True if possible, false is bad state.
     */
    public boolean stop() {
        if (!RecorderCommand.STOP.isPossible(mState)) return false;
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

    private RecorderInfo mLastRecorderInfo;

    /**
     * Start the recorder, if possible, with the passed information. As can be seen above, the last passed
     * information is kept in memory, so we can easily reuse it from the Controller. There are also simple
     * checks in place to ensure that everything is as it should be.
     * @param info Information for recording.
     * @return True if possible, false is bad state or an error/bad input.
     */
    public boolean start(RecorderInfo info) {
        mLastRecorderInfo = info;
        int width = info.getWidth(), height = info.getHeight();
        boolean audioEnabled = info.isAudioEnabled();
        String fileName = info.getFileName();
        if (!RecorderCommand.START.isPossible(mState)) return false;
        Log.i(getClass().getName(), "Checking for permissions...");
        if (mProjection == null) {
            Log.i(getClass().getName(), "Starting activity for permission...");
            Intent intent = new Intent(this, PermissionActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return false;
        }
        String errMsg;
        if (!(errMsg = checkStartParameters(width, height, fileName)).isEmpty()) {
            Toast.makeText(RecorderService.this, errMsg, Toast.LENGTH_LONG).show();
            return false;
        }
        if (!initialize(width, height, audioEnabled, fileName)) {
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

    /**
     * Helper function since the checking of input is making the start() function get too long.
     * It checks to see if they are valid, and if not appends an error message.
     * @param width Width. Cannot be 0.
     * @param height Height. Cannot be 0.
     * @param fileName Filename. Cannot be null.
     * @return null if good, a string describing the error if bad.
     */
    private String checkStartParameters(int width, int height, String fileName) {
        StringBuilder errMsg = new StringBuilder();
        if (width == 0) {
            errMsg.append("Width must be larger than or equal to 0!\n");
        }
        if (height == 0) {
            errMsg.append("Height must be large than or equal to 0!\n");
        }
        if (fileName == null) {
            errMsg.append("Filename cannot be left null!");
        }
        if (fileName != null && fileName.isEmpty()) {
            errMsg.append("Filename cannot be left empty!");
        }
        return errMsg.toString();
    }

}
