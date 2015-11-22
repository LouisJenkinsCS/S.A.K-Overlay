package com.theif519.sakoverlay.Views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextClock;

import com.theif519.sakoverlay.R;

/**
 * Created by theif519 on 11/21/2015.
 */
public class MenuBar extends RelativeLayout {

    private TextClock mClock;
    private ImageButton mIcon;

    public MenuBar(Context context) {
        this(context, null);
    }

    public MenuBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.menu_bar, this);
        mClock = (TextClock) findViewById(R.id.menu_bar_clock);
        mIcon = (ImageButton) findViewById(R.id.menu_bar_icon);
        mClock.setFormat12Hour("hh:mm a\nMM/dd/yyyy");
    }


}
