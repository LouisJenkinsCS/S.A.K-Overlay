package com.theif519.sakoverlay.Components.View;

import android.content.Context;
import android.util.Pair;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.TextView;

import com.annimon.stream.Optional;
import com.theif519.sakoverlay.Components.Misc.ComponentCodePopupMenu;
import com.theif519.sakoverlay.Components.Misc.MethodWrapper;
import com.theif519.sakoverlay.Components.Misc.ReferenceHelper;
import com.theif519.sakoverlay.Components.Misc.ReferenceType;
import com.theif519.sakoverlay.Core.Rx.Transformers;

import java.util.Map;

import rx.Observable;
import rx.Subscription;
import rx.subjects.BehaviorSubject;

import static com.theif519.sakoverlay.Components.Misc.QueryTypes.ACTIONS;
import static com.theif519.sakoverlay.Components.Misc.QueryTypes.CONDITIONALS;
import static com.theif519.sakoverlay.Components.Misc.QueryTypes.REFERENCES;
import static com.theif519.sakoverlay.Components.Misc.QueryTypes.STATEMENTS;

/**
 * Created by theif519 on 1/7/2016.
 */
public class Code extends TextView {

    private ComponentCodePopupMenu mMenu;
    private BehaviorSubject<Pair<Integer, Object>> mSelection = BehaviorSubject.create();
    private Subscription mSubscription;
    private ReferenceType<?> mReference;

    public Code(Context context) {
        super(context);
        mMenu = new ComponentCodePopupMenu(context, this);
        setText("Select...");
        setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics()));
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setOnClickListener(v -> mMenu.show());
    }

    public Code setSelections(int mask, Optional<ReferenceType<?>> ref) {
        mMenu.setOptions(mask, ref);
        ref.ifPresent(reference -> mReference = reference);
        return this;
    }

    public void finished(){
        mSubscription.unsubscribe();
    }

    public Observable<Pair<Integer, Object>> observeSelectionChanges() {
        return mSelection.asObservable();
    }

    private void setup() {
        mSubscription = mMenu.observeSelection()
                .map(pair -> {
                    Pair<Integer, Object> selection;
                    switch (pair.first) {
                        case STATEMENTS:
                            selection = Pair.create(pair.first, pair.second);
                            setText(pair.second + "(");
                            break;
                        case CONDITIONALS:
                        case ACTIONS:
                            MethodWrapper<?> methodWrapper = mReference.getAllMappedMethods()
                                    .filter(mappedMethod -> pair.second.equals(mappedMethod.getKey()))
                                    .map(Map.Entry::getValue)
                                    .findFirst()
                                    .orElseThrow(() -> new RuntimeException("Was unable to find the method associated with the returned method name: \"" + pair.second + "\"."));
                            String suffix;
                            if (methodWrapper.getParameterTypes().length == 0) {
                                suffix = "()";
                            } else suffix = "(...)";
                            setText(pair.second + suffix);
                            selection = Pair.create(pair.first, methodWrapper);
                            break;
                        case REFERENCES:
                            selection = Pair.create(pair.first, ReferenceHelper.getInstance().get(pair.second)
                                    .orElseThrow(() -> new RuntimeException("Was unable to find the ReferenceType associated with returned Reference ID: \"" + pair.second + "\"")));
                            setText(pair.second + ".");
                            break;
                        default:
                            throw new RuntimeException("Invalid Selection Type!");
                    }
                    return selection;
                })
                .compose(Transformers.backgroundIO())
                .subscribe(mSelection::onNext);
    }
}
