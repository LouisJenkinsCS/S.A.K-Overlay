package com.theif519.sakoverlay.Components;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.theif519.sakoverlay.Core.Views.AutoResizeTextView;
import com.theif519.sakoverlay.R;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by theif519 on 12/28/2015.
 */
public class TextComponent extends BaseComponent {

    protected TextView TEXT_VIEW;
    private TextView mValue;
    public static final String IDENTIFIER = "Text View";
    private static final String TEXT_VALUE = "Text Value";

    public TextComponent(Context context) {
        super(context);
    }

    public TextComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View createView(Context context) {
        TEXT_VIEW = new AutoResizeTextView(context);
        getViewTreeObserver().addOnGlobalLayoutListener(() -> TEXT_VIEW.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, getHeight(), getResources().getDisplayMetrics())));
        TEXT_VIEW.setText("Default Text!");
        TEXT_VIEW.setTextColor(ColorStateList.valueOf(context.getResources().getColor(R.color.black)));
        TEXT_VIEW.setGravity(Gravity.CENTER);
        TEXT_VIEW.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return TEXT_VIEW;
    }

    @Override
    protected void addOptionDialog(ViewGroup layout) {
        super.addOptionDialog(layout);
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup v = (ViewGroup) inflater.inflate(R.layout.component_text, null);
        layout.addView(createCategory("Text Component", v));
        layout.addView(v);
        mValue = (TextView) layout.findViewById(R.id.component_text_value);
    }

    @Override
    protected void clearResults(ViewGroup layout) {
        super.clearResults(layout);
        mValue.setText(TEXT_VIEW.getText());
    }

    @Override
    protected void handleResults(ViewGroup layout) {
        super.handleResults(layout);
        TEXT_VIEW.setText(mValue.getText());
    }

    @Override
    public JSONObject serialize() {
        try {
            return super.serialize()
                    .put(TEXT_VALUE, TEXT_VIEW.getText());
        } catch (JSONException e) {
            throw new RuntimeException("Error serializing TextComponent: Threw a JSONException with message \"" + e.getMessage() + "\"");
        }
    }

    @Override
    public void deserialize(JSONObject obj) {
        super.deserialize(obj);
        try {
            TEXT_VIEW.setText(obj.getString(TEXT_VALUE));
        } catch (JSONException e) {
            throw new RuntimeException("Error deserializing TextComponent: Threw a JSONException with message \"" + e.getMessage() + "\"");
        }
    }

    public class TextConditionals extends BaseConditionals {
        public boolean isEmpty(){
            return TEXT_VIEW.getText().toString().isEmpty();
        }
    }

    public class TextActions extends BaseActions {
        public void setText(String text) {
            TEXT_VIEW.setText(text);
        }

        public void setTextSize(int size){
            TEXT_VIEW.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        }
    }

    public class TextGetters {
        public String getText(){
            return TEXT_VIEW.getText().toString();
        }
    }
}
