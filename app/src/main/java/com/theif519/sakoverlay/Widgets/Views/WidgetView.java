package com.theif519.sakoverlay.Widgets.Views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.theif519.sakoverlay.R;

/**
 * Created by theif519 on 2/5/2016.
 */
public class WidgetView extends RelativeLayout {

    public WidgetView(Context context, int viewResId) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.widget_view, this);
        LayoutInflater.from(context).inflate(viewResId, (ViewGroup) findViewById(R.id.widget_view_container));
    }

    // TODO: Move onMove and onResize here.

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        /*
            Here we can implement a way to determine if we should resize based on if the user has
            touched a portion of the TitleBar, and anything else. This way there's no need to worry
            about the extra touches getting through to the child view since we can just return
            true to consume the event.

            TODO: Do the above
         */
        return super.onInterceptTouchEvent(ev);
    }
}
