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
import com.theif519.sakoverlay.POJO.PermissionInfo;
import com.theif519.sakoverlay.Sessions.RecorderInfo;
import com.theif519.sakoverlay.R;
import com.theif519.sakoverlay.Rx.RxBus;
import com.theif519.sakoverlay.Sessions.RecordingSession;

import java.io.IOException;

import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by theif519 on 11/12/2015.
 * <p/>
 * RecorderService is a service made explicitly for capturing and maintaining a video stream
 * while in the application is in the background. It uses the API level 21 MediaProjection API
 * to accomplish this, and maintains the state of the current recording.
 */
public class RecorderService extends Service {

    private static final String EXTRA_RESULT_CODE = "Result Code";
    private static final String EXTRA_DATA = "Data";

    public static Intent createIntent(Context context, int resultCode, Intent data){
        Intent intent = new Intent(context, RecorderService.class);
        intent.putExtra(EXTRA_RESULT_CODE, resultCode);
        intent.putExtra(EXTRA_DATA, data);
        return intent;
    }

    private RecordingSession mSession;
    private MediaProjection mProjection;
    private VirtualDisplay mDisplay;
    private MediaRecorder mRecorder;
    private PublishSubject<RecordingSession.RecorderState> mStateChangeObserver;
    private RecorderInfo mLastRecorderInfo;

    @Override
    public void onCreate() {
        super.onCreate();
        /*
            Whenever we receive a permission response, if the MediaProjection instance is null, we initialize it here.
         */
        RxBus.observe(PermissionInfo.class).subscribe(permissionInfo -> {
            if (mProjection == null) {
                mProjection = ((MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE))
                        .getMediaProjection(permissionInfo.getResultCode(), permissionInfo.getIntent());
            }
        });
        mStateChangeObserver = PublishSubject.create();
        setupFloatingView();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new RecorderBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0);
        Intent data = intent.getParcelableExtra(EXTRA_DATA);
        if (resultCode == 0 || data == null) {
            throw new IllegalStateException("Result code or data missing.");
        }
        PermissionInfo info = new PermissionInfo(data, resultCode);
        mSession = new RecordingSession(this, info);
        return START_NOT_STICKY;
    }

    /**
     * We add the view controller to the WindowManager, allowing us to draw over other applications.
     * The controller can control the state of the recorder, but also is controlled by the state changes
     * as well. I.E, while in STOPPED, it can call START, but if say the ScreenRecorderWidget makes a state change,
     * then the controller's next option will be STOP as the recording will have already been STARTED.
     * <p/>
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
                .subscribe(recorderState -> {
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
                        if ((int) (event.getRawX()) == initialTouchX || (int) (event.getRawY()) == initialTouchY) {
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
     *
     * @return True if possible, false is bad state.
     */
    public boolean stop() {
        return mSession.stop();
    }

    /**
     * Start the recorder, if possible, with the passed information. As can be seen above, the last passed
     * information is kept in memory, so we can easily reuse it from the Controller. There are also simple
     * checks in place to ensure that everything is as it should be.
     *
     * @param info Information for recording.
     * @return True if possible, false is bad state or an error/bad input.
     */
    public boolean start() {
        return mSession.start();
    }

    /**
     * Helper function since the checking of input is making the start() function get too long.
     * It checks to see if they are valid, and if not appends an error message.
     *
     * @param width    Width. Cannot be 0.
     * @param height   Height. Cannot be 0.
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

}
