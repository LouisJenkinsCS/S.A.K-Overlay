package com.theif519.sakoverlay;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

/**
 * Created by theif519 on 11/7/2015.
 */
public class TitleBar extends RelativeLayout {

    public TitleBar(Context context) {
        this(context, null);
    }

    public TitleBar(final Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.title_bar, this);
    }

}
