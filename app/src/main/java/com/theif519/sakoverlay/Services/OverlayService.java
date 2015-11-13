package com.theif519.sakoverlay.Services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.widget.Toast;

import com.theif519.sakoverlay.Activities.MainActivity;
import com.theif519.sakoverlay.R;

/**
 * Created by theif519 on 11/5/2015.
 */
public class OverlayService extends IntentService {

    public static final String START_NOTIFICATION = "Start Notification";

    public OverlayService() {
        super("S.A.K-Overlay Service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(intent.getBooleanExtra(START_NOTIFICATION, false)){
            setupForegroundNotification();
        } else Toast.makeText(OverlayService.this, "Was unable to start notification!", Toast.LENGTH_SHORT).show();
    }

    private void setupForegroundNotification(){
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
