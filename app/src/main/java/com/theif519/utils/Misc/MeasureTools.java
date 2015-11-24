package com.theif519.utils.Misc;

import android.content.Context;
import android.graphics.Point;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListAdapter;

import com.theif519.sakoverlay.Misc.Globals;

/**
 * Created by theif519 on 11/23/2015.
 */
public final class MeasureTools {

    public static float scaleDiff(float num, float ratio) {
        return num - (num * ratio);
    }

    public static float scale(float num, float ratio) {
        return num * ratio;
    }

    public static int scaleDiffToInt(float num, float ratio) {
        return (int) scaleDiff(num, ratio);
    }

    public static int scaleToInt(float num, float ratio) {
        return (int) scale(num, ratio);
    }

    public static Point getScaledCoordinates(View view){
        return new Point((int) view.getX() + scaleDiffToInt(view.getWidth(), Globals.SCALE_X.get())/2,
                (int) view.getY() + scaleDiffToInt(view.getHeight(), Globals.SCALE_Y.get())/2);
    }

    public static int measureContentWidth(Context context, ListAdapter listAdapter) {
        ViewGroup mMeasureParent = null;
        int maxWidth = 0;
        View itemView = null;
        int itemType = 0;

        final ListAdapter adapter = listAdapter;
        final int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int count = adapter.getCount();
        for (int i = 0; i < count; i++) {
            final int positionType = adapter.getItemViewType(i);
            if (positionType != itemType) {
                itemType = positionType;
                itemView = null;
            }

            if (mMeasureParent == null) {
                mMeasureParent = new FrameLayout(context);
            }

            itemView = adapter.getView(i, itemView, mMeasureParent);
            itemView.measure(widthMeasureSpec, heightMeasureSpec);

            final int itemWidth = itemView.getMeasuredWidth();

            if (itemWidth > maxWidth) {
                maxWidth = itemWidth;
            }
        }

        return maxWidth;
    }

}
