package com.theif519.sakoverlay;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;

/**
 * Created by theif519 on 10/28/2015.
 */
public class StickyNote extends LinearLayout {

    private EditText mText;

    public StickyNote(Context context) {
        this(context, null);
    }

    public StickyNote(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.sticky_note, this);
        mText = (EditText) findViewById(R.id.sticky_note_edit_text);
    }
}
