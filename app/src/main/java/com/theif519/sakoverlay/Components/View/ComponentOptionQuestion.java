package com.theif519.sakoverlay.Components.View;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.theif519.sakoverlay.R;

/**
 * Created by theif519 on 12/30/2015.
 */
public class ComponentOptionQuestion extends LinearLayout {

    private EditText mAnswer;
    private TextView mQuestion;

    public ComponentOptionQuestion(Context context) {
        this(context, null);
    }

    public ComponentOptionQuestion(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.component_option_question, this);
        mAnswer = (EditText) findViewById(R.id.component_option_question_answer);
        mQuestion = (TextView) findViewById(R.id.component_option_question_description);
    }

    public ComponentOptionQuestion(Context context, String question, int inputType){
        this(context);
        mQuestion.setText(question);
        mAnswer.setInputType(inputType);
    }

    public String getAnswer(){
        return mAnswer.getText().toString();
    }
}
