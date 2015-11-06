package com.theif519.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;

/**
 * Created by theif519 on 11/5/2015.
 */
public class ServiceTools {

    public interface SetupIntent {
        void setup(Intent intent);
    }

    /**
     * Modified from http://stackoverflow.com/a/5921190/4111188.
     *
     * Normally I see code, and think "Okay, that's cool, but that's cool to use as a reference", hence
     * I don't do this often (as I never ever copy-paste, just learn from it as I would a text book,
     * as no one every cites the textbook or the code their professor wrote in class, because that's just
     * how you learn)... but this is just amazing and purely modular solution and deserves recognition.
     *
     * What it does is from the context and class of the service given, it will, go through all activities
     * in ActivityManager, then if the class name (because for whatever reason that is the ONLY way to determine
     * what class is what) matches, then it's already been created, and we can return. Otherwise it will start
     * the service anyway. Hence avoiding spawning multiple services.
     *
     * What I modified was that his returned true and false, while mine just goes ahead and spawn a new server.
     *
     * What makes this code so amazing is that it can literally be used anywhere, for anything regarding
     * a service, hence why I added it to my Utils.
     * @param context Context.
     * @param serviceClass Class of the service to check for and start if absent.
     */
    public static void startService(Context context, Class<?> serviceClass, SetupIntent callback){
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return;
            }
        }
        Intent intent = new Intent(context, serviceClass);
        if(callback != null) callback.setup(intent);
        context.startService(intent);
    }
}
