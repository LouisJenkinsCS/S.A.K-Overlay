package com.theif519.sakoverlay.Rx.Events;

import com.theif519.sakoverlay.Fragments.Widgets.BaseWidget;

/**
 * Created by theif519 on 12/10/2015.
 */
public class WidgetFinishedDeserializing {
    public BaseWidget fragment;

    public WidgetFinishedDeserializing(BaseWidget fragment) {
        this.fragment = fragment;
    }

    public BaseWidget getFragment() {
        return fragment;
    }
}
