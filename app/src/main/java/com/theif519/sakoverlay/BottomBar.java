package com.theif519.sakoverlay;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

/**
 * Created by theif519 on 11/7/2015.
 */
public class BottomBar extends FrameLayout {

    public BottomBar(Context context) {
        this(context, null);
    }

    public BottomBar(final Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.bottom_bar, this);
    }
}
