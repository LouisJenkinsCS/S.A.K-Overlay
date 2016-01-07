package com.theif519.sakoverlay.Components.Misc;

import android.content.Context;
import android.util.TypedValue;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;

import com.annimon.stream.Stream;
import com.theif519.sakoverlay.Components.View.ComponentConstructIf;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    private TypeMode mCurrentMode = TypeMode.STATEMENT;
    private LinearLayout mMainLayout, mCurrentLayout;
    private WeakReference<Context> mContext;
    private ReferenceHelper mHelper;
    private ReferenceType<?> mCurrentReference;

    public ConstructHelper(Context context, ReferenceHelper helper) {
        mContext = new WeakReference<>(context);
        mHelper = helper;
        mMainLayout = new LinearLayout(context);
        mMainLayout.setOrientation(LinearLayout.VERTICAL);
        mMainLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mMainLayout.addView(mCurrentLayout = new LinearLayout(context));
        mCurrentLayout.setOrientation(LinearLayout.HORIZONTAL);
        mCurrentLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        TextView textView = new TextView(context);
        textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        textView.setText("Click Here");
        textView.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, context.getResources().getDisplayMetrics()));
        createMenu(textView);
        mCurrentLayout.addView(textView);
        mCurrentLayout.addView(new ComponentConstructIf(context, helper));
    }

    public View getView(){
        return mMainLayout;
    }

    private void createMenu(View v){
        int groupStatement = 1, groupComponents = 2;
        int statementIf = 1, statementElse = 2;
        v.setOnClickListener(v1 -> {
            // TODO: You MUST add these to a submenu, NOT the current menu!
            PopupMenu menu = new PopupMenu(mContext.get(), v);
            menu.getMenu().add(groupStatement, Menu.NONE, Menu.NONE, "Statements");
            menu.getMenu().addSubMenu(groupStatement, statementIf, statementIf, "If");
            menu.getMenu().addSubMenu(groupStatement, statementElse, statementIf, "Else");
            menu.getMenu().add(groupComponents, Menu.NONE, Menu.NONE, "Components");
            Stream.of(mHelper.getAllReferences())
                    .flatMap(ref -> Stream.of(ref.getId()))
                    .forEach(id -> menu.getMenu().addSubMenu(groupComponents, Menu.NONE, Menu.NONE, id));
            menu.show();
        });
    }

    private void configureSpinner(Spinner spinner) {
        List<String> options = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext.get(), android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (mCurrentMode.isPossible(TypeMask.STATEMENT)) {
            Collections.addAll(options, mHelper.getStatements());
        }
        if (mCurrentReference == null) {
            Stream.of(mHelper.getAllReferences())
                    .map(ReferenceType::getId)
                    .forEach(options::add);
        } else {
            if (mCurrentMode.isPossible(TypeMask.CONDITIONAL)) {
                Stream.of(mCurrentReference.getConditionals().getAllMethods())
                        .map(MethodWrapper::getMethodName)
                        .forEach(options::add);
            }
            if(mCurrentMode.isPossible(TypeMask.ACTION)){
                Stream.of(mCurrentReference.getActions().getAllMethods())
                        .map(MethodWrapper::getMethodName)
                        .forEach(options::add);
            }
        }
        adapter.notifyDataSetChanged();
        spinner.setAdapter(adapter);
    }
}