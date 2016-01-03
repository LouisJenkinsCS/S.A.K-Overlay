package com.theif519.sakoverlay.Views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.theif519.sakoverlay.R;

/**
 * Created by theif519 on 1/3/2016.
 */
public class ComponentAttributeEditor extends LinearLayout {

    public ComponentAttributeEditor(Context context) {
        this(context, null);
    }

    public ComponentAttributeEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.component_attribute_editor, this);
    }
}
