package com.theif519.sakoverlay.Components.View;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.theif519.sakoverlay.Components.Types.ReferenceType;
import com.theif519.sakoverlay.Components.Types.Wrappers.MethodWrapper;
import com.theif519.sakoverlay.Components.Types.Wrappers.ParameterWrapper;
import com.theif519.sakoverlay.R;

import java.lang.ref.WeakReference;

/**
 * Created by theif519 on 1/12/2016.
 */
public class ParameterFillerDialog {

    private WeakReference<Context> mContextReference;
    private MethodWrapper<?> mWrapper;
    private View mParameterFillerView;

    public ParameterFillerDialog(Context context, MethodWrapper<?> wrapper) {
        mContextReference = new WeakReference<>(context);
        mWrapper = wrapper;
        mParameterFillerView = LayoutInflater.from(context).inflate(R.layout.component_parameter_filler, null);
        setup();
    }

    public void show() {
        TextView title = new TextView(getContext());
        title.setText(mWrapper.getDeclaration());
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setPadding(10, 10, 10, 10);
        title.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16, getContext().getResources().getDisplayMetrics()));
        new AlertDialog.Builder(getContext())
                .setCustomTitle(title)
                .setView(mParameterFillerView)
                .setPositiveButton("OK!", ((dialog, which) -> {
                }))
                .setNegativeButton("NO!", ((dialog1, which1) -> {
                }))
                .show();
    }

    private void setup() {
        if (mWrapper.getDescription().isPresent()) {
            ((TextView) mParameterFillerView.findViewById(R.id.component_parameter_filler_method_description))
                    .setText(mWrapper.getDescription().get());
        } else {
            mParameterFillerView.findViewById(R.id.component_parameter_filler_method_description_root)
                    .setVisibility(View.GONE);
        }
        if (mWrapper.getParameterCount() != 0) {
            mWrapper.getParameters()
                    .map(param -> {
                        createInputView(param);
                        return param;
                    })
                    .filter(param -> param.getDescription() != null && param.getName() != null)
                    .forEach(this::generateParameterField);
        } else {
            mParameterFillerView.findViewById(R.id.component_parameter_filler_method_parameters_root)
                    .setVisibility(View.GONE);
        }
        if (mWrapper.getReturn().getDescription() != null) {
            ((TextView) mParameterFillerView.findViewById(R.id.component_parameter_filler_method_return_description))
                    .setText(mWrapper.getReturn().getDescription());
            ((TextView) mParameterFillerView.findViewById(R.id.component_parameter_filler_method_return_type))
                    .setText(mWrapper.getReturn().toString());
        } else {
            mParameterFillerView.findViewById(R.id.component_parameter_filler_method_return_root)
                    .setVisibility(View.GONE);
        }
    }

    private void generateParameterField(ParameterWrapper<?> param) {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.component_parameter_field,
                ((ViewGroup) mParameterFillerView.findViewById(R.id.component_parameter_filler_method_parameters_container)));
        ((TextView) v.findViewById(R.id.component_parameter_field_name)).setText(param.getName());
        ((TextView) v.findViewById(R.id.component_parameter_field_description)).setText(param.getDescription());
    }

    private void createInputView(ParameterWrapper<?> parameter) {
        View inputView = LayoutInflater.from(getContext()).inflate(R.layout.component_parameter_input,
                (ViewGroup) mParameterFillerView.findViewById(R.id.component_parameter_filler_container));
        ((TextView) inputView.findViewById(R.id.component_parameter_input_type)).setText(parameter.getType().getSimpleName());
        if (parameter.getDescription() == null) {
            // TODO: Generate "arg" + index
            ((TextView) inputView.findViewById(R.id.component_parameter_input_name)).setText("arg0");
        } else {
            ((TextView) inputView.findViewById(R.id.component_parameter_input_name)).setText(parameter.getName());
        }
        View v = generateInputView(parameter.getType());
        v.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ((ViewGroup) inputView.findViewById(R.id.component_parameter_input_container)).addView(v);
    }

    private View generateInputView(Class<?> type){
        View inputView;
        if(type == int.class || type == Integer.class || type ==  long.class || type == Long.class){
            EditText editText = (EditText) (inputView = new EditText(getContext()));
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        } else if (type == double.class || type == Double.class || type == float.class || type == Float.class){
            EditText editText = (EditText) (inputView = new EditText(getContext()));
            editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        } else if(type == String.class){
            EditText editText = (EditText) (inputView = new EditText(getContext()));
            editText.setInputType(InputType.TYPE_CLASS_TEXT);
        } else if(type == ReferenceType.class){
            // TODO: Here, create a new Code that specifies the need for a ReferenceType.
            throw new UnsupportedOperationException("Currently unable to generate an input view for a ReferenceType argument!");
        } else if(type == Boolean.class || type == boolean.class){
            ToggleButton booleanButton = (ToggleButton) (inputView = new ToggleButton(getContext()));
            booleanButton.setTextOff("False");
            booleanButton.setTextOn("True");
            booleanButton.setChecked(false);
        } else {
            throw new RuntimeException("Was unable to generate for the class type: \"" + type.getSimpleName() + "\"");
        }
        return inputView;
    }

    private Context getContext() {
        Context context = mContextReference.get();
        if (context == null) {
            throw new RuntimeException("Potential leakage of ParameterFillerDialog instance!");
        }
        return context;
    }
}
