package com.theif519.sakoverlay.Views.DynamicComponents;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;

import com.theif519.sakoverlay.Builders.ComponentOptionsBuilder;
import com.theif519.sakoverlay.R;
import com.theif519.sakoverlay.Views.AutoResizeTextView;

/**
 * Created by theif519 on 12/28/2015.
 */
public class TextComponent extends BaseComponent {

    private AutoResizeTextView mTextView;
    public static final String IDENTIFIER = "TextView";

    public TextComponent(Context context) {
        super(context);
    }

    public TextComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void addView(Context context, ViewGroup container) {
        mTextView = new AutoResizeTextView(context);
        container.getViewTreeObserver().addOnGlobalLayoutListener(() -> mTextView.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, container.getHeight(), getResources().getDisplayMetrics())));
        mTextView.setText("Default Text!");
        mTextView.setTextColor(ColorStateList.valueOf(context.getResources().getColor(R.color.black)));
        mTextView.setGravity(Gravity.CENTER);
        mTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        container.addView(mTextView);
    }

    @Override
    protected void onLongPress() {
        new ComponentOptionsBuilder()
                .setTitle("Text View Editor")
                .addQuestion("Value: ", InputType.TYPE_CLASS_TEXT, msg -> {
                    if (msg == null) {
                        return false;
                    } else mTextView.setText(msg);
                    return true;
                })
                .build(getContext())
                .show();
    }
}
