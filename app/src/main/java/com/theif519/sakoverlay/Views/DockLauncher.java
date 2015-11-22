package com.theif519.sakoverlay.Views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.theif519.sakoverlay.R;

/**
 * Created by theif519 on 11/22/2015.
 */
public class DockLauncher extends LinearLayout {

    public DockLauncher(Context context) {
        this(context, null);
    }

    public DockLauncher(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.dock_launcher, this);
    }
}
