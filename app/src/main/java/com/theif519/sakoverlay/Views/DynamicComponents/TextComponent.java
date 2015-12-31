package com.theif519.sakoverlay.Views.DynamicComponents;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.theif519.sakoverlay.R;
import com.theif519.sakoverlay.Views.AutoResizeTextView;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by theif519 on 12/28/2015.
 */
public class TextComponent extends BaseComponent {

    private AutoResizeTextView mTextView;
    private TextView mValue;
    public static final String IDENTIFIER = "TextView";
    private static final String TEXT_VALUE = "Text Value";

    public TextComponent(Context context) {
        super(context);
    }

    public TextComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View createView(Context context) {
        mTextView = new AutoResizeTextView(context);
        getViewTreeObserver().addOnGlobalLayoutListener(() -> mTextView.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, getHeight(), getResources().getDisplayMetrics())));
        mTextView.setText("Default Text!");
        mTextView.setTextColor(ColorStateList.valueOf(context.getResources().getColor(R.color.black)));
        mTextView.setGravity(Gravity.CENTER);
        mTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return mTextView;
    }

    @Override
    protected void addOptionDialog(ViewGroup layout) {
        super.addOptionDialog(layout);
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.component_text, layout);
        mValue = (TextView) layout.findViewById(R.id.component_text_value);
    }

    @Override
    protected void sanitizeResults(ViewGroup layout, StringBuilder errMsg) {
        super.sanitizeResults(layout, errMsg);
        if(mValue.getText().toString().isEmpty()){
            errMsg.append("Text cannot be left empty!");
            errMsg.append("\n");
        }
    }

    @Override
    protected void clearResults(ViewGroup layout) {
        super.clearResults(layout);
        mValue.setText(mTextView.getText());
    }

    @Override
    protected void handleResults(ViewGroup layout) {
        super.handleResults(layout);
        mTextView.setText(mValue.getText());
    }

    @Override
    public JSONObject serialize() {
        try {
            return super.serialize()
                    .put(TEXT_VALUE, mTextView.getText());
        } catch (JSONException e) {
            throw new RuntimeException("Error serializing TextComponent: Threw a JSONException with message \"" + e.getMessage() + "\"");
        }
    }

    @Override
    public void deserialize(JSONObject obj) {
        super.deserialize(obj);
        try {
            mTextView.setText(obj.getString(TEXT_VALUE));
        } catch (JSONException e) {
            throw new RuntimeException("Error deserializing TextComponent: Threw a JSONException with message \"" + e.getMessage() + "\"");
        }
    }
}
