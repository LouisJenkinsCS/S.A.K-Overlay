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
import com.theif519.sakoverlay.ReactiveX.EventBus.RxBus;

public class PermissionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        ActionBar actionBar = getActionBar();
        if(actionBar != null){
            actionBar.hide();
        } else {
            android.support.v7.app.ActionBar supportActionBar = getSupportActionBar();
            if(supportActionBar != null){
                supportActionBar.hide();
            }
        }
        setContentView(R.layout.transparent_activity);
        Intent intent = getIntent();
        if(intent == null){
            finish();
            return;
        }
        startActivityForResult(
                ((MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE))
                        .createScreenCaptureIntent(), Globals.RECORDER_PERMISSION_RETVAL
        );
    }

    @Override
    public void onActivityResult(int requestCode, final int resultCode, final Intent data) {
        if (requestCode == Globals.RECORDER_PERMISSION_RETVAL && resultCode == RESULT_OK) {
            RxBus.post(new PermissionInfo(data, resultCode));
        }
        finish();
    }
}
