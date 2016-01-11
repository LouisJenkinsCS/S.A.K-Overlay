package com.theif519.sakoverlay.Components.View;

import android.content.Context;
import android.util.Pair;
import android.widget.LinearLayout;

import com.theif519.sakoverlay.Components.Misc.ConstructStatement;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.CompositeSubscription;

import static com.theif519.sakoverlay.Components.Misc.QueryTypes.ACTIONS;
import static com.theif519.sakoverlay.Components.Misc.QueryTypes.CONDITIONALS;
import static com.theif519.sakoverlay.Components.Misc.QueryTypes.REFERENCES;
import static com.theif519.sakoverlay.Components.Misc.QueryTypes.STATEMENTS;
import static com.theif519.sakoverlay.Components.Misc.QueryTypes.STATEMENTS_ELSE;
import static com.theif519.sakoverlay.Components.Misc.QueryTypes.STATEMENTS_ELSE_IF;
import static com.theif519.sakoverlay.Components.Misc.QueryTypes.STATEMENTS_IF;

/**
 * Created by theif519 on 1/8/2016.
 */
public class LineOfCode extends LinearLayout {

    private List<Code> mCodeChain = new ArrayList<>();
    private int mNextInstruction;
    private int mMode = 0;
    private boolean mIsFinished = false;
    private CompositeSubscription mSubscriptions = new CompositeSubscription();
    private BehaviorSubject<Pair<Integer, Boolean>> mLineChanged = BehaviorSubject.create();

    public LineOfCode(Context context) {
        super(context);
        setOrientation(HORIZONTAL);
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        mNextInstruction = REFERENCES | STATEMENTS;
    }

    private void spawnCode() {
        Code code = new Code(getContext());
        code.setPrevious(mCodeChain.get(mCodeChain.size() - 1));
        code.setInstructionType(mNextInstruction);
        mCodeChain.add(code);
        addView(code);
        code.observeSelectionChanges()
                .subscribe(pair -> {
                    mIsFinished = false;
                    // Remove excess code if the code is not the very last in the list.
                    if(code != mCodeChain.get(mCodeChain.size() - 1)) {
                        for (int i = 0; i < mCodeChain.size(); i++) {
                            Code current = mCodeChain.get(i);
                            if (current == code) {
                                for (int j = i + 1; j < mCodeChain.size(); j++) {
                                    Code excess = mCodeChain.remove(mCodeChain.size() - 1);
                                    excess.finished();
                                    removeView(excess);
                                }
                            }
                        }
                    }
                    // Based on the type, handle the returned object.
                    switch (pair.first) {
                        case STATEMENTS:
                            switch ((String) pair.second) {
                                case ConstructStatement.IF_STRING:
                                    mMode = STATEMENTS_IF;
                                    break;
                                case ConstructStatement.ELSE_IF_STRING:
                                    mMode = STATEMENTS_ELSE_IF;
                                    break;
                                case ConstructStatement.ELSE_STRING:
                                    mMode = STATEMENTS_ELSE;
                                    mIsFinished = true;
                                    break;
                                default:
                                    throw new RuntimeException("Invalid statement string returned from child Code!");
                            }
                            mNextInstruction = REFERENCES;
                        case REFERENCES:
                            switch (mMode) {
                                case STATEMENTS_IF:
                                case STATEMENTS_ELSE_IF:
                                    mNextInstruction = CONDITIONALS;
                                    break;
                                case STATEMENTS_ELSE:
                                    mIsFinished = true;
                                    break;
                                case 0:
                                    mMode = STATEMENTS;
                                    mNextInstruction = ACTIONS;
                                    break;
                                default:
                                    throw new RuntimeException("Invalid Mode for Reference!");
                            }
                            break;
                        case CONDITIONALS:
                            switch (mMode) {
                                case STATEMENTS_IF:
                                case STATEMENTS_ELSE_IF:
                                    mIsFinished = true;
                                    break;
                                default:
                                    throw new RuntimeException("Invalid: Conditional method chosen in incompatible statement!");
                            }
                        case ACTIONS:
                            switch (mMode) {
                                case STATEMENTS_IF:
                                case STATEMENTS_ELSE_IF:
                                case STATEMENTS_ELSE:
                                    throw new RuntimeException("Invalid: Action method chosen in a statement!");
                                case STATEMENTS:
                                    mIsFinished = true;
                            }
                    }
                    mLineChanged.onNext(Pair.create(mMode, mIsFinished));
                    if(!mIsFinished){
                        spawnCode();
                    }
                });
    }

    public Observable<Pair<Integer, Boolean>> observeLineChange() {
        return mLineChanged.asObservable();
    }
}