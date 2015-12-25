package com.theif519.sakoverlay.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.theif519.sakoverlay.POJO.PermissionInfo;
import com.theif519.sakoverlay.R;
import com.theif519.sakoverlay.Sessions.Recording.RecordingInfo;
import com.theif519.sakoverlay.Sessions.Recording.RecordingSession;
import com.theif519.sakoverlay.Sessions.Recording.RecordingState;

import rx.Observable;

/**
 * Created by theif519 on 11/12/2015.
 * <p>
 * RecorderService is a service made explicitly for capturing and maintaining a video stream
 * while in the application is in the background. It uses the API level 21 MediaProjection API
 * to accomplish this, and maintains the state of the current recording.
 */
public class RecorderService extends Service {

    private static final String EXTRA_RESULT_CODE = "Result Code";
    private static final String EXTRA_DATA = "Data";

    public static Intent createIntent(Context context, int resultCode, Intent data) {
        Intent intent = new Intent(context, RecorderService.class);
        intent.putExtra(EXTRA_RESULT_CODE, resultCode);
        intent.putExtra(EXTRA_DATA, data);
        return intent;
    }

    private RecordingSession mSession;
    private RecordingState mState;
    private RecordingInfo mLastRecordingInfo;

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
        mSession
                .observeStateChanges()
                .subscribe(state -> mState = state);
        //setupFloatingView();
        return START_NOT_STICKY;
    }

    /**
     * We add the view controller to the WindowManager, allowing us to draw over other applications.
     * The controller can control the state of the recorder, but also is controlled by the state changes
     * as well. I.E, while in STOPPED, it can call START, but if say the ScreenRecorderWidget makes a state change,
     * then the controller's next option will be STOP as the recording will have already been STARTED.
     * <p>
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
        mSession
                .observeStateChanges() // Luckily, Rx Observables can also be observed from the same thread, easily.
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
                                    if (mLastRecordingInfo != null) {
                                        start(mLastRecordingInfo);
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

    public void die() {
        mSession.release();
        stopForeground(true);
        stopSelf();
    }

    public boolean stop() {
        if (mSession.stop()) {
            return true;
        } else {
            Toast.makeText(this, "Error: " + mSession.getLastErrorMessage(), Toast.LENGTH_LONG).show();
            return false;
        }
    }

    public boolean start(RecordingInfo info) {
        StringBuilder errMsg = new StringBuilder();
        if(!info.isValid(errMsg)){
            Toast.makeText(this, "Error: " + errMsg, Toast.LENGTH_LONG).show();
            return false;
        }
        if(!mSession.prepare(info)){
            Toast.makeText(this, "Error: " + mSession.getLastErrorMessage(), Toast.LENGTH_LONG).show();
            return false;
        }
        if (mSession.start()) {
            mLastRecordingInfo = info;
            return true;
        } else {
            Toast.makeText(this, "Error: " + mSession.getLastErrorMessage(), Toast.LENGTH_LONG).show();
            return false;
        }
    }

    public class RecorderBinder extends Binder {
        public RecorderService getService() {
            return RecorderService.this;
        }

        public Observable<RecordingState> observeStateChanges() {
            return mSession.observeStateChanges();
        }
    }

}
