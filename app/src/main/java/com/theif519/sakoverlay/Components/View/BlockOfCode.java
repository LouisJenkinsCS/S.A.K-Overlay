package com.theif519.sakoverlay.Components.View;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import rx.subscriptions.CompositeSubscription;

import static com.theif519.sakoverlay.Components.Types.QueryTypes.STATEMENTS;
import static com.theif519.sakoverlay.Components.Types.QueryTypes.STATEMENTS_ELSE;
import static com.theif519.sakoverlay.Components.Types.QueryTypes.STATEMENTS_ELSE_IF;
import static com.theif519.sakoverlay.Components.Types.QueryTypes.STATEMENTS_IF;

/**
 * Created by theif519 on 1/8/2016.
 */
public class BlockOfCode extends LinearLayout {

    private List<LineWrapper> mLineChain = new ArrayList<>();
    private CompositeSubscription mSubscriptions = new CompositeSubscription();

    public BlockOfCode(Context context) {
        super(context);
        setOrientation(VERTICAL);
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        generateLine(0);
    }

    private void generateLine(int index){
        LineWrapper wrapper = new LineWrapper(getContext());
        wrapper.setOnNextListener(v -> {
            for(int i = 0; i < mLineChain.size(); i++){
                if(wrapper == mLineChain.get(i)){
                    generateLine(++i);
                    break;
                }
            }
        });
        wrapper.setOnDeleteListener(v -> deleteLine(wrapper));
        if(index != 0){
            LineWrapper prev = mLineChain.get(index - 1);
            switch(prev.getType()){
                case STATEMENTS:
                    wrapper.setNesting(prev.getNesting());
                    break;
                case STATEMENTS_IF:
                    wrapper.setNesting(prev.getNesting()+1);
                    break;
                case STATEMENTS_ELSE_IF:
                case STATEMENTS_ELSE:
                    wrapper.setNesting(prev.getNesting());
                    prev.setNesting(prev.getNesting()-1);
                    break;
            }
        }
        mLineChain.add(index, wrapper);
        addView(wrapper, index);
    }

    private void deleteLine(LineWrapper wrapper){
        if(mLineChain.get(0) != wrapper) {
            mLineChain.remove(wrapper);
            removeView(wrapper);
            wrapper.finish();
        } else {
            Toast.makeText(getContext(), "There must be at least one line!", Toast.LENGTH_LONG).show();
        }
    }

}