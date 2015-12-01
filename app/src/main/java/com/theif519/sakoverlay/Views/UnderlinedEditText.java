package com.theif519.sakoverlay.Views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.EditText;

import com.theif519.sakoverlay.R;
import com.theif519.utils.Misc.AttributeRetriever;

/**
 * Created by lpj11535 on 11/30/2015.
 */
public class UnderlinedEditText extends EditText {

    private Paint mPaint = new Paint();

    Color mUnderlineColor;

    public UnderlinedEditText(Context context) {
        this(context, null);
    }

    public UnderlinedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
        AttributeRetriever.fillAttributes(getClass(), this, context, attrs);
    }

    private void initPaint() {
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(getResources().getColor(R.color.black));
    }

    @AttributeRetriever.AttributeHelper(source = "uetColor")
    private void setColor(int colorCode){
        mPaint.setColor(colorCode);
    }

    @Override protected void onDraw(Canvas canvas) {
        int left = getLeft();
        int right = getRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int height = getHeight();
        int lineHeight = getLineHeight();
        int count = (height-paddingTop-paddingBottom) / lineHeight;
        for (int i = 0; i < count; i++) {
            int baseline = lineHeight * (i+1) + paddingTop;
            canvas.drawLine(left+paddingLeft, baseline, right-paddingRight, baseline, mPaint);
        }
        super.onDraw(canvas);
    }
}
