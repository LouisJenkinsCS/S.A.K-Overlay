package com.example.theif519.saklauncher;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

/**
 * Created by theif519 on 10/29/2015.
 */
public class MockActionBar extends RelativeLayout {

    public MockActionBar(Context context) {
        this(context, null);
    }

    public MockActionBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.mock_action_bar, this);
    }
}
