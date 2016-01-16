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

    @MethodWrapper.MethodDescriptions(
            methodDescription = "Changes the visibility of the view.",
            parameterNames = {
                    "state"
            },
            parameterDescriptions = {
                    "Whether or not the view is visible"
            }
    )
    public void setVisible(boolean state) {
        mView.setVisibility(state ? View.VISIBLE : View.INVISIBLE);
    }

    @MethodWrapper.MethodDescriptions(
            methodDescription = "Sets the view's X coordinate.",
            parameterNames = {
                    "x"
            },
            parameterDescriptions = {
                    "New X coordinate of the view."
            }
    )
    public void setX(float x) {
        mView.setX(x);
    }

    @MethodWrapper.MethodDescriptions(
            methodDescription = "Sets the view's Y coordinate.",
            parameterNames = {
                    "y"
            },
            parameterDescriptions = {
                    "New Y coordinate of the view."
            }
    )
    public void setY(float y) {
        mView.setY(y);
    }

    @MethodWrapper.MethodDescriptions(
            methodDescription = "Sets the view's width.",
            parameterNames = {
                    "width"
            },
            parameterDescriptions = {
                    "New width of the view."
            }
    )
    public void setWidth(int width) {
        mView.getLayoutParams().width = width;
        mView.requestLayout();
    }

    @MethodWrapper.MethodDescriptions(
            methodDescription = "Sets the view's height.",
            parameterNames = {
                    "height"
            },
            parameterDescriptions = {
                    "New height of the view."
            }
    )
    public void setHeight(int height) {
        mView.getLayoutParams().height = height;
        mView.requestLayout();
    }

    @MethodWrapper.MethodDescriptions(
            methodDescription = "Changes whether or not this view is enabled.",
            parameterNames = {
                    "state"
            },
            parameterDescriptions = {
                    "Whether or not the view is enabled"
            }
    )
    public void setEnabled(boolean state) {
        mView.setEnabled(state);
    }
}