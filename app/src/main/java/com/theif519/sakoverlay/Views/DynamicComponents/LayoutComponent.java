package com.theif519.sakoverlay.Views.DynamicComponents;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * Created by theif519 on 12/28/2015.
 */
public class LayoutComponent extends  BaseComponent {

    public static final String IDENTIFIER = "LinearLayout";

    public LayoutComponent(Context context) {
        super(context);
    }

    public LayoutComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void addView(Context context, ViewGroup container) {
        container.addView(new LinearLayout(context));
    }
}
