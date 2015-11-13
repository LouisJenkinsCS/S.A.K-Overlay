package com.theif519.sakoverlay.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.theif519.sakoverlay.Services.OverlayService;

/**
 * Created by theif519 on 11/5/2015.
 */
public class OnBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent in = new Intent(context, OverlayService.class);
        in.putExtra(OverlayService.START_NOTIFICATION, true);
        context.startService(in);
    }
}
