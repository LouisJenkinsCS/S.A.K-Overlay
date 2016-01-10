package com.theif519.sakoverlay.Components.View;

import android.content.Context;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.widget.LinearLayout;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.theif519.sakoverlay.Components.Misc.ConstructStatement;
import com.theif519.sakoverlay.Components.Misc.MethodWrapper;
import com.theif519.sakoverlay.Components.Misc.ReferenceHelper;
import com.theif519.sakoverlay.Components.Misc.ReferenceType;
import static com.theif519.sakoverlay.Components.Misc.QueryTypes.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import rx.Observable;
import rx.subjects.BehaviorSubject;

/**
 * Created by theif519 on 1/8/2016.
 */
public class ComponentConstructLine extends LinearLayout {

    private ReferenceHelper mHelper;
    private List<Code> mChain = new ArrayList<>();
    private Queue<Integer> mModeQueue = new ArrayDeque<>();
    private ReferenceType<?> mCurrentReference;
    private boolean mIsFinished;
    private boolean isStatement;
    BehaviorSubject<ComponentConstructBlock.NestDirection> mFinished = BehaviorSubject.create();

    public ComponentConstructLine(Context context, ReferenceHelper helper, int timesNested) {
        super(context);
        setOrientation(HORIZONTAL);
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        mHelper = helper;
        if (timesNested != 0) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) getLayoutParams();
            params.setMarginStart((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50 * timesNested, getResources().getDisplayMetrics()));
            requestLayout();
            Log.i(getClass().getName(), String.format("Times Nested: %d; Margin Start: %d",
                    timesNested, params.getMarginStart()));
        }
        mModeQueue.add(REFERENCES | STATEMENTS);
        generateConstructView();
    }

    public void generateConstructView() {
        if (mIsFinished) return;
        Code view = new Code(getContext(), mHelper);
        view.setPaddingRelative(getPaddingStart() + (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics()),
                getPaddingTop(), getPaddingEnd(), getPaddingBottom());
        int nextOptions = mModeQueue.remove();
        view.setOptions(nextOptions, Optional.ofNullable(mCurrentReference));
        addView(view);
        view.observeSelectionChanges()
                .subscribe(newMode -> {
                    updateMode(newMode);
                    if (!mIsFinished) {
                        generateConstructView();
                    }
                });
    }

    private void updateMode(String newMode) {
        ConstructStatement statement = ConstructStatement.from(newMode);
        if (statement != null) {
            switch (statement) {
                case IF:
                case ELSE_IF:
                    mModeQueue.add(REFERENCES);
                    mModeQueue.add(CONDITIONALS);
                    isStatement = true;
                    // End Line or add Optional Operator.
                    break;
                case ELSE:
                    setFinished(ComponentConstructBlock.NestDirection.UPPER);
                    break;
            }
        } else {
            if (mCurrentReference != null && referenceContainsMethod(newMode)) {
                setFinished(isStatement ? ComponentConstructBlock.NestDirection.UPPER : ComponentConstructBlock.NestDirection.NONE);
                return;
            }
            if (mModeQueue.isEmpty()) {
                mModeQueue.add(ACTIONS);
            }
            getCurrentReference(newMode);
        }
    }

    private void setFinished(ComponentConstructBlock.NestDirection direction) {
        mIsFinished = true;
        mFinished.onNext(direction);
    }

    public Observable<ComponentConstructBlock.NestDirection> observeLineFinish() {
        return mFinished.asObservable();
    }
}