package com.theif519.sakoverlay.Components.Types.Actions.Impl;

import android.view.View;

import com.theif519.sakoverlay.Components.Types.Wrappers.MethodWrapper;

/**
 * Created by theif519 on 1/12/2016.
 */
public class BaseActions extends Actions {

    private View mView;

    public BaseActions(View v) {
        super(v);
        mView = v;
    }

    public void setVisible(boolean state) {
        mView.setVisibility(state ? View.VISIBLE : View.INVISIBLE);
    }

    public void setX(float x) {
        mView.setX(x);
    }

    public void setY(float y) {
        mView.setY(y);
    }

    public void setWidth(int width) {
        mView.getLayoutParams().width = width;
        mView.requestLayout();
    }

    public void setHeight(int height) {
        mView.getLayoutParams().height = height;
        mView.requestLayout();
    }

    @MethodWrapper.MethodDescriptions(
            methodDescription = "Changes the visibility of the view.",
            parameterNames = {
                    "state"
            },
            parameterDescriptions = {
                    "Whether or not the view is visible"
            }
    )
    public void setEnabled(boolean state) {
        mView.setEnabled(state);
    }
}