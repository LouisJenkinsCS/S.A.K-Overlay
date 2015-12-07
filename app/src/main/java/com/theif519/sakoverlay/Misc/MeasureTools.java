package com.theif519.sakoverlay.Misc;

import android.content.Context;
import android.graphics.Point;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListAdapter;

/**
 * Created by theif519 on 11/23/2015.
 *
 * This Utility tool is used to aid in the arduous task of moving and resizing scaled views. They serve
 * not only as reusable, but also readable. They offer ways to scale (and unscale) views and sizes.
 */
public final class MeasureTools {

    public static int scaleDifferenceWidth(View v) {
        return (int)(v.getWidth() - (v.getWidth() * Globals.SCALE.get()));
    }

    public static int scaleDifferenceHeight(View v){
        return (int)(v.getHeight() - (v.getHeight() * Globals.SCALE.get()));
    }

    public static int scaleDifference(float f){
        return (int)(f - (f * Globals.SCALE.get()));
    }

    public static int scale(float width) {
        return (int)(width * Globals.SCALE.get());
    }

    public static int scaleWidth(View v){
        return scale(v.getWidth());
    }

    public static int scaleHeight(View v){
        return scale(v.getHeight());
    }

    public static Point getScaledCoordinates(View v){
        return new Point((int) v.getX() + scaleDeltaWidth(v), (int) v.getY() + scaleDeltaHeight(v));
    }

    public static int scaleDeltaWidth(View v){
        return scaleDifferenceWidth(v)/2;
    }

    public static int scaleDeltaHeight(View v){
        return scaleDifferenceHeight(v)/2;
    }

    public static int scaleDelta(float f){
        return scaleDifference(f) / 2;
    }

    public static int scaleInverse(float f){
        return (int)(f / Globals.SCALE.get());
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
