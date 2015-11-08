package com.theif519.sakoverlay;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

/**
 * Created by theif519 on 11/7/2015.
 */
public class FloatingFragmentLayout extends RelativeLayout {

    private ViewGroup mLayout;

    public FloatingFragmentLayout(Context context) {
        this(context, null);
    }

    public FloatingFragmentLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.default_fragment, this);

    }




}
