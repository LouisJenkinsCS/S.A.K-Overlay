package com.theif519.sakoverlay.POJO;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.theif519.sakoverlay.Builders.ComponentOptionsBuilder;

/**
 * Created by theif519 on 12/29/2015.
 */
public class ComponentQuestion {
    private String mQuestion;
    private int mInputType, mIndex;
    private ComponentOptionsBuilder.AnswerProcessor mProcessorCallback;


    public ComponentQuestion(int mIndex, String mQuestion, int mInputType, ComponentOptionsBuilder.AnswerProcessor mProcessorCallback) {
        this.mIndex = mIndex;
        this.mQuestion = mQuestion;
        this.mInputType = mInputType;
        this.mProcessorCallback = mProcessorCallback;
    }

    public int getIndex(){
        return mIndex;
    }

    public void setIndex(int mIndex){
        this.mIndex = mIndex;
    }

    public String getQuestion() {
        return mQuestion;
    }

    public void setQuestion(String mQuestion) {
        this.mQuestion = mQuestion;
    }

    public int getInputType() {
        return mInputType;
    }

    public void setInputType(int mInputType) {
        this.mInputType = mInputType;
    }

    public ComponentOptionsBuilder.AnswerProcessor getProcessorCallback() {
        return mProcessorCallback;
    }

    public void setProcessorCallback(ComponentOptionsBuilder.AnswerProcessor mProcessorCallback) {
        this.mProcessorCallback = mProcessorCallback;
    }

    public View build(Context context){
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.HORIZONTAL);
        TextView textView = new TextView(context);
        textView.setText(mQuestion);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(5, 5, 5, 5);
        textView.setLayoutParams(params);
        container.addView(textView);
        EditText editText = new EditText(context);
        editText.setInputType(mInputType);
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(5, 5, 5, 5);
        editText.setTag("Answer");
        container.addView(editText);
        container.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        container.setTag(mIndex);
        return container;
    }
}
