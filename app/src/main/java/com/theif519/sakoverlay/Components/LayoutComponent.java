package com.theif519.sakoverlay.Components;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by theif519 on 12/28/2015.
 */
public class LayoutComponent extends  BaseComponent {

    public static final String IDENTIFIER = "Layout";
    public static final String TEXT_VALUE = "Layout";

    public LayoutComponent(Context context, String key) {
        super(context, key);
    }

    @Override
    protected View createView(Context context) {
        return new LinearLayout(context);
    }
}
