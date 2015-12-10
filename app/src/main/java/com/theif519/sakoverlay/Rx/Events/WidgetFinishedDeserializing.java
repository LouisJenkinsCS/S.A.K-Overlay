package com.theif519.sakoverlay.Rx.Events;

import com.theif519.sakoverlay.Fragments.Floating.FloatingFragment;

/**
 * Created by theif519 on 12/10/2015.
 */
public class WidgetFinishedDeserializing {
    public FloatingFragment fragment;

    public WidgetFinishedDeserializing(FloatingFragment fragment) {
        this.fragment = fragment;
    }

    public FloatingFragment getFragment() {
        return fragment;
    }
}
