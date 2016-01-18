package com.theif519.sakoverlay.Components.View;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.LinearLayout;

import com.theif519.sakoverlay.R;

/**
 * Created by theif519 on 1/18/2016.
 */
public class MaxDimensionLayout extends LinearLayout {

    public MaxDimensionLayout(Context context) {
        this(context, null);
    }

    public MaxDimensionLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MaxDimensionLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, 0, 0);
    }

    private int mMaxHeight, mMaxWidth;

    public MaxDimensionLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.MaxDimensionLayout);
        try {
            mMaxHeight = (int) array.getDimension(R.styleable.MaxDimensionLayout_maxDimensionHeight, -1);
            mMaxWidth = (int) array.getDimension(R.styleable.MaxDimensionLayout_maxDimensionWidth, -1);
        } finally {
            array.recycle();
        }
    }

    public int getMaxWidth() {
        return mMaxWidth;
    }

    public void setMaxWidth(int mMaxWidth) {
        this.mMaxWidth = mMaxWidth;
        requestLayout();
        invalidate();
    }

    public int getMaxHeight() {
        return mMaxHeight;
    }

    public void setMaxHeight(int mMaxHeight) {
        this.mMaxHeight = mMaxHeight;
        requestLayout();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int newWidth, newHeight;
        if(mMaxWidth != -1 && mMaxWidth < widthMeasureSpec){
            newWidth = MeasureSpec.makeMeasureSpec(
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mMaxWidth, getResources().getDisplayMetrics()),
                    MeasureSpec.AT_MOST
            );
        } else newWidth = widthMeasureSpec;
        if(mMaxHeight != -1 && mMaxHeight < heightMeasureSpec){
            newHeight = MeasureSpec.makeMeasureSpec(
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mMaxHeight, getResources().getDisplayMetrics()),
                    MeasureSpec.AT_MOST
            );
        } else newHeight = heightMeasureSpec;
        super.onMeasure(newWidth, newHeight);
    }
}
