package com.theif519.sakoverlay.Activities;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.theif519.sakoverlay.Misc.Globals;
import com.theif519.sakoverlay.POD.PermissionInfo;
import com.theif519.sakoverlay.R;
import com.theif519.sakoverlay.Rx.RxBus;

/**
 * Created by Louis Jenkins
 * <p/>
 * This activity is essentially called to obtain the CaptureScreenPermission (or whatever it's called)
 * from the user. Instead of relying on the MainActivity, which would be bad as it would over complicate it,
 * it's easier to create it here. The activity is entirely transparent, hence it attempts to not ruin
 * the User Experience. Although the closing animation is shown, there is nothing I can do about that.
 */
public class PermissionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        } else {
            android.support.v7.app.ActionBar supportActionBar = getSupportActionBar();
            if (supportActionBar != null) {
                supportActionBar.hide();
            }
        }
        setContentView(R.layout.transparent_activity);
        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }
        /*
            We get the intent here. A service cannot startActivityForResult(), hence I need an activity to create another
            activity to return the intent to this activity so it can send it back to the service that started this activity.

            Complicated, but necessary.
         */
        startActivityForResult(
                ((MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE))
                        .createScreenCaptureIntent(), Globals.RECORDER_PERMISSION_RETVAL
        );
    }

    @Override
    public void onActivityResult(int requestCode, final int resultCode, final Intent data) {
        if (requestCode == Globals.RECORDER_PERMISSION_RETVAL && resultCode == RESULT_OK) {
            // Publish the event to the event bus. I use this over BroadcastReceiver, because... reasons. I wanted a chance to test it out.
            RxBus.publish(new PermissionInfo(data, resultCode));
        }
        finish();
    }
}
