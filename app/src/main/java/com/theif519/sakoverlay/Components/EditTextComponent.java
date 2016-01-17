package com.theif519.sakoverlay.Components;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.annimon.stream.Optional;
import com.theif519.sakoverlay.Components.Misc.AttributeMenuHelper;
import com.theif519.sakoverlay.Components.Misc.BaseViewManager;
import com.theif519.sakoverlay.R;

import org.json.JSONException;
import org.json.JSONObject;

import rx.Observable;

/**
 * Created by theif519 on 1/1/2016.
 */
public class EditTextComponent extends TextComponent {

    private Spinner mInputType;

    private static final String INPUT_TYPE_DATETIME = "Date/Time", INPUT_TYPE_TEXT = "Text", INPUT_TYPE_PHONE = "Phone", INPUT_TYPE_NUMBER = "Number";

    private static final String INPUT_TYPE = "InputType";

    public static final String IDENTIFIER = "EditText";
    public static final String TEXT_VALUE = "Edit Text";

    public EditTextComponent(Context context, String key) {
        super(context, key);
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
    protected AttributeMenuHelper createAttributeMenu() {
        return super.createAttributeMenu()
                .add("Editable", createEditTextAttrs());
    }

    private BaseViewManager createEditTextAttrs() {
        ViewGroup layout = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.component_text_editable, null);
        final Spinner inputType = (Spinner) layout.findViewById(R.id.component_text_editable_input_type);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item,
                new String[]{INPUT_TYPE_TEXT, INPUT_TYPE_NUMBER, INPUT_TYPE_DATETIME, INPUT_TYPE_PHONE});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        inputType.setAdapter(adapter);
        return new BaseViewManager(layout) {
            @Override
            public Optional<String> validate() {
                return Optional.empty();
            }

            @Override
            public void handle() {
                switch ((String) inputType.getSelectedItem()) {
                    case INPUT_TYPE_TEXT:
                        TEXT_VIEW.setInputType(InputType.TYPE_CLASS_TEXT);
                        break;
                    case INPUT_TYPE_NUMBER:
                        TEXT_VIEW.setInputType(InputType.TYPE_CLASS_NUMBER);
                        break;
                    case INPUT_TYPE_PHONE:
                        TEXT_VIEW.setInputType(InputType.TYPE_CLASS_PHONE);
                        break;
                    case INPUT_TYPE_DATETIME:
                        TEXT_VIEW.setInputType(InputType.TYPE_CLASS_DATETIME);
                        break;
                }
            }

            @Override
            public void reset() {
                switch (TEXT_VIEW.getInputType()) {
                    case InputType.TYPE_CLASS_TEXT:
                        inputType.setSelection(0);
                        break;
                    case InputType.TYPE_CLASS_NUMBER:
                        inputType.setSelection(1);
                        break;
                    case InputType.TYPE_CLASS_DATETIME:
                        inputType.setSelection(2);
                        break;
                    case InputType.TYPE_CLASS_PHONE:
                        inputType.setSelection(3);
                        break;
                }
            }

            @NonNull
            @Override
            public Observable<Void> observeStateChanges() {
                return Observable.never();
            }
        };
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
