package com.theif519.sakoverlay.Components.Types.Conditionals.Impl;

import android.view.View;
import android.widget.TextView;

/**
 * Created by theif519 on 1/12/2016.
 */
public class TextConditionals extends BaseConditionals {

    private TextView mView;

    public TextConditionals(View v) {
        super(v);
        mView = (TextView) v;
    }

    public boolean isEmpty(){
        return mView.getText().toString().isEmpty();
    }
}
