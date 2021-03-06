package com.theif519.sakoverlay.Components.View;

import android.content.Context;
import android.util.Pair;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Space;

import com.theif519.sakoverlay.R;

import rx.Observable;
import rx.Subscription;
import rx.subjects.BehaviorSubject;

/**
 * Created by theif519 on 1/11/2016.
 */
public class LineWrapper extends LinearLayout {

    private ViewGroup mContainer;
    private LineOfCode mLine;
    private ImageButton mNext, mDelete;
    private Space mNestingSpace;
    private ViewGroup mButtonGroup;
    private BehaviorSubject<Pair<Integer, Boolean>> mLineChanged = BehaviorSubject.create();
    private Subscription mLineChangeSubscription;
    private int mCurrentNesting, mType;
    private boolean mIsFinished = false;

    public LineWrapper(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.component_loc_wrapper, this);
        mButtonGroup = (ViewGroup) findViewById(R.id.component_loc_wrapper_button_group);
        mNestingSpace = (Space) findViewById(R.id.component_loc_wrapper_nesting);
        mContainer = (ViewGroup) findViewById(R.id.component_loc_wrapper_container);
        mNext = (ImageButton) findViewById(R.id.component_loc_wrapper_button_next);
        mDelete = (ImageButton) findViewById(R.id.component_loc_wrapper_button_delete);
        mLine = new LineOfCode(context);
        mLine.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            mButtonGroup.getLayoutParams().height = mLine.getHeight();
            mButtonGroup.requestLayout();
        });
        mContainer.addView(mLine);
        setup();
    }

    public LineWrapper setOnNextListener(OnClickListener listener){
        mNext.setOnClickListener(listener);
        return this;
    }

    public LineWrapper setNesting(int nesting){
        mNestingSpace.getLayoutParams().width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25 * (mCurrentNesting = nesting), getResources().getDisplayMetrics());
        mNestingSpace.requestLayout();
        return this;
    }

    public int getNesting(){
        return mCurrentNesting;
    }

    public boolean isFinished(){
        return mIsFinished;
    }

    public int getType(){
        return mType;
    }

    public LineWrapper setOnDeleteListener(OnClickListener listener){
        mDelete.setOnClickListener(listener);
        return this;
    }

    public void finish(){
        mLine.finish();
        mLineChangeSubscription.unsubscribe();
    }

    public Observable<Pair<Integer, Boolean>> observeLineChanges(){
        return mLineChanged.asObservable();
    }

    private void setup() {
        mLineChangeSubscription = mLine.observeLineChange()
                .subscribe(pair -> {
                    mIsFinished = pair.second;
                    mType = pair.first;
                    mNext.setVisibility(mIsFinished ? VISIBLE : GONE);
                    mLineChanged.onNext(pair);
                });
    }
}
