package com.theif519.sakoverlay.Components.Misc;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.theif519.sakoverlay.Components.View.BlockOfCode;

import java.lang.ref.WeakReference;

/**
 * Created by theif519 on 1/7/2016.
 */
public class ConstructHelper {

    enum TypeMask {
        REFERENCE(1),
        STATEMENT(1 << 1),
        CONDITIONAL(1 << 2),
        ACTION(1 << 3),
        GETTER(1 << 4),
        SETTER(1 << 5);

        private int mMask;

        TypeMask(int mask) {
            mMask = mask;
        }

        public int getMask() {
            return mMask;
        }
    }

    enum TypeMode {
        IF(TypeMask.CONDITIONAL.getMask()),
        ELSE(TypeMask.STATEMENT.getMask() | TypeMask.ACTION.getMask()),
        STATEMENT(TypeMask.STATEMENT.getMask() | TypeMask.ACTION.getMask());

        private int mMask;

        TypeMode(int mask) {
            mMask = mask;
        }

        public boolean isPossible(TypeMask mask) {
            return (mask.getMask() & mMask) != 0;
        }
    }

    private LinearLayout mMainLayout, mCurrentLayout;
    private WeakReference<Context> mContext;

    public ConstructHelper(Context context) {
        mContext = new WeakReference<>(context);
        mMainLayout = new LinearLayout(context);
        mMainLayout.setOrientation(LinearLayout.VERTICAL);
        mMainLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mMainLayout.addView(mCurrentLayout = new LinearLayout(context));
        mCurrentLayout.setOrientation(LinearLayout.HORIZONTAL);
        mCurrentLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mCurrentLayout.addView(new BlockOfCode(context));
    }

    public View getView(){
        return mMainLayout;
    }
}