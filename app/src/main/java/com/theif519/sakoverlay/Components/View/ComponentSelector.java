package com.theif519.sakoverlay.Components.View;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.theif519.sakoverlay.R;

/**
 * Created by theif519 on 1/3/2016.
 */
public class ComponentSelector extends LinearLayout {

    public ComponentSelector(Context context) {
        this(context, null);
    }

    public ComponentSelector(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.component_selector, this);
    }
}
