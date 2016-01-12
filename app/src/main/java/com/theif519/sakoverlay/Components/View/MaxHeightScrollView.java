package com.theif519.sakoverlay.Components.View;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ScrollView;

import com.theif519.sakoverlay.R;

/**
 * Created by theif519 on 1/12/2016.
 */
public class MaxHeightScrollView extends ScrollView {

    private int mMaxHeight = -1;

    public MaxHeightScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.MaxHeightScrollView);
        try {
            mMaxHeight = (int) array.getDimension(R.styleable.MaxHeightScrollView_maxHeight, -1);
        } finally {
            array.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        heightMeasureSpec = mMaxHeight == -1 ? heightMeasureSpec : MeasureSpec.makeMeasureSpec(
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mMaxHeight, getResources().getDisplayMetrics()),
                MeasureSpec.AT_MOST
        );
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
