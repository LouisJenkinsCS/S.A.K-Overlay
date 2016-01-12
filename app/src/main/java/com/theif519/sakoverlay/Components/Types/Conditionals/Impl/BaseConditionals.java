package com.theif519.sakoverlay.Components.Types.Conditionals.Impl;

import android.view.View;

/**
 * Created by theif519 on 1/12/2016.
 */
public class BaseConditionals extends Conditionals {
    private View mView;
    public BaseConditionals(View v) {
        super(v);
        mView = v;
    }

    public boolean isVisible(){
        return mView.getVisibility() == View.VISIBLE;
    }

    public boolean isEnabled(){
        return mView.isEnabled();
    }
}