package com.example.theif519.saklauncher;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

/**
 * Created by theif519 on 10/28/2015.
 */
public class Introduction extends LinearLayout {

    public Introduction(Context context) {
        this(context, null);
    }

    public Introduction(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.introduction, this);
    }
}
