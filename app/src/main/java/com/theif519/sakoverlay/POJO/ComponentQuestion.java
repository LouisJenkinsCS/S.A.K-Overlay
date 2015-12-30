package com.theif519.sakoverlay.POJO;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.theif519.sakoverlay.Builders.ComponentOptionsBuilder;
import com.theif519.sakoverlay.R;

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
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout container = (LinearLayout) inflater.inflate(R.layout.component_option_question, null);
        TextView textView = (TextView) container.findViewById(R.id.component_option_question_description);
        textView.setText(mQuestion);
        EditText editText = (EditText) container.findViewById(R.id.component_option_question_answer);
        editText.setTag("Answer");
        container.setTag(mIndex);
        return container;
    }
}
