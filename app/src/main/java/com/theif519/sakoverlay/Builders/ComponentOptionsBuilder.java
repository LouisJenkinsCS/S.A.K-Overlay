package com.theif519.sakoverlay.Builders;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.annimon.stream.Stream;
import com.theif519.sakoverlay.POJO.ComponentQuestion;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by theif519 on 12/29/2015.
 */
public class ComponentOptionsBuilder {

    private List<ComponentQuestion> mList;
    private String mTitle = "";

    public ComponentOptionsBuilder() {
        mList = new ArrayList<>();
    }

    public interface AnswerProcessor {
        boolean onAnswer(String answer);
    }

    public ComponentOptionsBuilder setTitle(String title){
        mTitle = title;
        return this;
    }

    public ComponentOptionsBuilder addQuestion(String question, int type, AnswerProcessor callback) {
        mList.add(new ComponentQuestion(mList.size(), question, type, callback));
        return this;
    }

    public AlertDialog build(Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        Stream
                .of(mList)
                .map(question -> question.build(context))
                .forEach(layout::addView);
        return new AlertDialog.Builder(context)
                .setTitle(mTitle)
                .setView(layout)
                .setPositiveButton("OK!", (thisDialog, which) -> {
                    boolean isValid = true;
                    for(int i = 0; i < mList.size(); i++){
                        View v = layout.findViewWithTag(i);
                        if(v != null){
                            ComponentQuestion question = mList.get(i);
                            if(question.getProcessorCallback() != null){
                                EditText editText = (EditText) v.findViewWithTag("Answer");
                                if(!question.getProcessorCallback().onAnswer(editText.getText().toString())){
                                    isValid = false;
                                }
                            }
                        }
                    }
                    if(isValid){
                        thisDialog.dismiss();
                    }
                })
                .setNegativeButton("No!", (thisDialog, which) -> thisDialog.dismiss())
                .create();
    }
}
