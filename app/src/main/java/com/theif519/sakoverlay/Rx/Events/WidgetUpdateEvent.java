package com.theif519.sakoverlay.Rx.Events;

import com.theif519.sakoverlay.Fragments.Widgets.BaseWidget;

/**
 * Created by theif519 on 12/10/2015.
 */
public class WidgetUpdateEvent {
    public BaseWidget fragment;

    public WidgetUpdateEvent(BaseWidget fragment) {
        this.fragment = fragment;
    }

    public BaseWidget getFragment() {
        return fragment;
    }
}
