package com.theif519.sakoverlay.Builders;

import android.app.AlertDialog;
import android.content.Context;
import android.util.ArrayMap;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.annimon.stream.Stream;
import com.theif519.sakoverlay.POJO.ComponentQuestion;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by theif519 on 12/29/2015.
 */
public class ComponentOptionsBuilder {

    public interface IViewCallbacks<ViewType extends View, ReturnType> {
        ViewType createView(Context context);

        ReturnType getResult(ViewType view);

        boolean isResultValid(ReturnType result);

        String getBadResultMessage(ReturnType result);

        void handleResult(ReturnType result);
    }

    public abstract class NoResultViewCallback<T extends  View> implements IViewCallbacks<T, Void> {
        @Override
        public Void getResult(T view) {
            return null;
        }

        @Override
        public boolean isResultValid(Void result) {
            return true;
        }

        @Override
        public String getBadResultMessage(Void result) {
            return null;
        }

        @Override
        public void handleResult(Void result) {

        }
    }

    private List<IViewCallbacks> mList;
    private String mTitle = "";

    public ComponentOptionsBuilder() {
        mList = new ArrayList<>();
    }

    public interface AnswerProcessor {
        boolean onAnswer(String answer);
    }

    public ComponentOptionsBuilder setTitle(String title) {
        mTitle = title;
        return this;
    }

    public ComponentOptionsBuilder addQuestion(String question, int type, AnswerProcessor callback) {
        mList.add(new ComponentQuestion(mList.size(), question, type, callback));
        return this;
    }

    // TODO
    public ComponentOptionsBuilder addView(IViewCallbacks callbacks){
        return this;
    }

    // TODO
    public ComponentOptionsBuilder addViewNoResult(NoResultViewCallback callback){
        return this;
    }


    /*
        TODO: Fix this! The interface needs to be badly redesigned (when I'm not drunk off Rum!!!), and it's throwing
        TODO: Unchecked Cast warnings all over the place. Refactor!
     */
    public AlertDialog build(Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        ArrayMap<IViewCallbacks, View> map = new ArrayMap<>();
        for(IViewCallbacks callbacks : mList){
            map.put(callbacks, callbacks.createView(context));
        }
        return new AlertDialog.Builder(context)
                .setTitle(mTitle)
                .setView(layout)
                .setPositiveButton("OK!", (thisDialog, which) -> {
                    boolean isValid = true;
                    StringBuilder errMsg = new StringBuilder();
                    Stream
                            .of(map)
                            .forEach(entry -> {
                                View v = entry.getValue();
                                IViewCallbacks callbacks = entry.getKey();
                                Object result = callbacks.getResult(v);
                                if (callbacks.isResultValid(result)) {
                                    callbacks.handleResult(result);
                                    thisDialog.dismiss();
                                } else {
                                    errMsg.append(callbacks.getBadResultMessage(result));
                                }
                            });
                    if (((AlertDialog) thisDialog).isShowing()) {
                        Toast.makeText(context, "Bad User Input: \"" + errMsg.toString() + "\"", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("No!", (thisDialog, which) -> thisDialog.dismiss())
                .create();
    }
}
