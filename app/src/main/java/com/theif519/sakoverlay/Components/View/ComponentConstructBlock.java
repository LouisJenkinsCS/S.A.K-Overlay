package com.theif519.sakoverlay.Components.View;

import android.content.Context;
import android.widget.LinearLayout;

import com.theif519.sakoverlay.Components.Misc.ReferenceHelper;

/**
 * Created by theif519 on 1/8/2016.
 */
public class ComponentConstructBlock extends LinearLayout {

    enum NestDirection {
        DOWN(-1), NONE(0), UPPER(1);

        private int num;

        NestDirection(int val) {
            num = val;
        }

        int getVal() {
            return num;
        }
    }

    private ReferenceHelper mHelper;
    private int nested = 0;

    public ComponentConstructBlock(Context context, ReferenceHelper helper) {
        super(context);
        setOrientation(VERTICAL);
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mHelper = helper;
        generateConstructLine();
    }

    private void generateConstructLine() {
        ComponentConstructLine line = new ComponentConstructLine(getContext(), mHelper, nested);
        line.observeLineFinish()
                .subscribe(nestDirection -> {
                    nested += nestDirection.getVal();
                    nested = Math.max(nested, 0);
                    generateConstructLine();
                });
        addView(line);
    }
}