package com.theif519.sakoverlay.Views.DynamicComponents;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;

import com.theif519.sakoverlay.R;
import com.theif519.sakoverlay.Views.AutoResizeTextView;

/**
 * Created by theif519 on 12/28/2015.
 */
public class TextComponent extends BaseComponent {

    public static final String IDENTIFIER = "TextView";

    public TextComponent(Context context) {
        super(context);
    }

    public TextComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void addView(Context context, ViewGroup container) {
        AutoResizeTextView view = new AutoResizeTextView(context);
        container.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            view.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, container.getHeight(), getResources().getDisplayMetrics()));
        });
        view.setText("Default Text!");
        view.setTextColor(ColorStateList.valueOf(context.getResources().getColor(R.color.black)));
        view.setGravity(Gravity.CENTER);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        container.addView(view);
    }
}
