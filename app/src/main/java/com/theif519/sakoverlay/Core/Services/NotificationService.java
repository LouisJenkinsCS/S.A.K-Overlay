package com.theif519.sakoverlay.Core.Services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.widget.Toast;

import com.theif519.sakoverlay.Core.Activities.MainActivity;
import com.theif519.sakoverlay.Core.Misc.Globals;
import com.theif519.sakoverlay.R;

/**
 * Created by theif519 on 11/5/2015.
 * <p/>
 * This class is an extremely early attempt at creating a service explicitly for showing a notification
 * to the user. I would rework it but even as I type this, I am running out of time for what I can do.
 */
public class NotificationService extends IntentService {

    public static final String START_NOTIFICATION = "Start Notification";

    public NotificationService() {
        super("S.A.K-Overlay Service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent.getBooleanExtra(START_NOTIFICATION, false)) {
            setupForegroundNotification();
        } else
            Toast.makeText(NotificationService.this, "Was unable to start notification!", Toast.LENGTH_SHORT).show();
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
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.sak_overlay_icon))
                .setContentIntent(pIntent)
                .setOngoing(true)
                .build();
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(Globals.OVERLAY_NOTIFICATION_ID, notification);
    }
}
