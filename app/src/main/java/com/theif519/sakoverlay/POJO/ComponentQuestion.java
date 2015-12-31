package com.theif519.sakoverlay.POJO;

import android.content.Context;

import com.theif519.sakoverlay.Builders.ComponentOptionsBuilder;
import com.theif519.sakoverlay.Views.ComponentOptionQuestion;

/**
 * Created by theif519 on 12/29/2015.
 */
public class ComponentQuestion implements ComponentOptionsBuilder.IViewCallbacks<ComponentOptionQuestion, String> {
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

    @Override
    public ComponentOptionQuestion createView(Context context) {
        return new ComponentOptionQuestion(context, mQuestion, mInputType);
    }

    @Override
    public String getResult(ComponentOptionQuestion view) {
        return view.getAnswer();
    }

    @Override
    public boolean isResultValid(String result) {
        return result != null;
    }

    @Override
    public String getBadResultMessage(String result) {
        return "Result was null!";
    }

    @Override
    public void handleResult(String result) {
        mProcessorCallback.onAnswer(result);
    }
}
