package com.theif519.sakoverlay.Views.DynamicComponents;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.theif519.sakoverlay.R;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by theif519 on 1/1/2016.
 */
public class EditTextComponent extends TextComponent {

    private Spinner mInputType;

    private static final String INPUT_TYPE_DATETIME = "Date/Time", INPUT_TYPE_TEXT = "Text", INPUT_TYPE_PHONE = "Phone", INPUT_TYPE_NUMBER = "Number";

    private static final String INPUT_TYPE = "InputType";

    public static final String IDENTIFIER = "EditText";

    public EditTextComponent(Context context) {
        super(context);
    }

    public EditTextComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View createView(Context context) {
        this.TEXT_VIEW = new EditText(context);
        TEXT_VIEW.setTextColor(ColorStateList.valueOf(context.getResources().getColor(R.color.black)));
        TEXT_VIEW.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        TEXT_VIEW.setInputType(InputType.TYPE_CLASS_TEXT);
        TEXT_VIEW.setGravity(Gravity.BOTTOM | Gravity.START);
        TEXT_VIEW.setCursorVisible(true);
        TEXT_VIEW.getBackground().setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_IN);
        return TEXT_VIEW;
    }

    @Override
    protected void addOptionDialog(ViewGroup layout) {
        super.addOptionDialog(layout);
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup v = (ViewGroup) inflater.inflate(R.layout.component_text_editable, null);
        layout.addView(createCategory("EditText Component", v));
        layout.addView(v);
        mInputType = (Spinner) layout.findViewById(R.id.component_text_editable_input_type);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item,
                new String[] { INPUT_TYPE_TEXT, INPUT_TYPE_NUMBER, INPUT_TYPE_DATETIME, INPUT_TYPE_PHONE });
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mInputType.setAdapter(adapter);
    }

    @Override
    protected void clearResults(ViewGroup layout) {
        super.clearResults(layout);
        mInputType.setSelection(0);
    }

    @Override
    protected void handleResults(ViewGroup layout) {
        super.handleResults(layout);
        int inputType = 0;
        switch((String) mInputType.getSelectedItem()){
            case INPUT_TYPE_TEXT:
                inputType = InputType.TYPE_CLASS_TEXT;
                break;
            case INPUT_TYPE_NUMBER:
                inputType = InputType.TYPE_CLASS_NUMBER;
                break;
            case INPUT_TYPE_PHONE:
                inputType = InputType.TYPE_CLASS_PHONE;
                break;
            case INPUT_TYPE_DATETIME:
                inputType = InputType.TYPE_CLASS_DATETIME;
                break;
        }
        TEXT_VIEW.setInputType(inputType);
    }

    @Override
    public JSONObject serialize() {
        try {
            return super.serialize()
                    .put(INPUT_TYPE, TEXT_VIEW.getInputType());
        } catch (JSONException e) {
            throw new RuntimeException("Error serializing EditTextComponent: Threw a JSONException with message \"" + e.getMessage() + "\"");
        }
    }

    @Override
    public void deserialize(JSONObject obj) {
        super.deserialize(obj);
        try {
            TEXT_VIEW.setInputType(obj.getInt(INPUT_TYPE));
        } catch (JSONException e) {
            throw new RuntimeException("Error deserializing EditTextComponent: Threw a JSONException with message \"" + e.getMessage() + "\"");
        }
    }
}
