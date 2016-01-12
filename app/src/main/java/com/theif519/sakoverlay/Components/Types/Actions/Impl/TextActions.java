package com.theif519.sakoverlay.Components.Types.Actions.Impl;

import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

/**
 * Created by theif519 on 1/12/2016.
 */
public class TextActions extends BaseActions {

    private TextView mView;

    public TextActions(View v) {
        super(v);
        mView = (TextView) v;
    }

    public void setText(String text) {
        mView.setText(text);
    }

    public void setTextSize(int size){
        mView.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
    }
}