package com.theif519.sakoverlay.Components.View;

import android.content.Context;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;

import com.theif519.sakoverlay.Components.Misc.MethodWrapper;
import com.theif519.sakoverlay.Components.Types.ReferenceType;

import java.lang.ref.WeakReference;

/**
 * Created by theif519 on 1/12/2016.
 */
public class ParameterFillerDialog {

    private WeakReference<Context> mContextReference;
    private MethodWrapper<?> mWrapper;

    public ParameterFillerDialog(Context context, MethodWrapper<?> wrapper) {
        mContextReference = new WeakReference<Context>(context);
        mWrapper = wrapper;
    }

    private View generateLayout(){
        return null;
    }

    private View generateInputView(Class<?> type) {
        Context context = mContextReference.get();
        if (context == null) {
            throw new RuntimeException("Potential leakage of ParameterFillerDialog instance!");
        }
        View v = null;
        if (!isReferenceType(type)) {
            EditText editText = (EditText) (v = new EditText(context));
            int inputType = 0;
            if (isNumber(type)) {
                inputType |= InputType.TYPE_CLASS_NUMBER;
                if (isFraction(type)) {
                    inputType |= InputType.TYPE_NUMBER_FLAG_DECIMAL;
                }
            } else if(isString(type)){
                inputType |= InputType.TYPE_CLASS_TEXT;
            } else {
                throw new RuntimeException("Was unable to generate for the class type: \"" + type.getSimpleName() + "\"");
            }
        } else {
            // TODO: Here, create a new Code that specifies the need for a ReferenceType.
            throw new UnsupportedOperationException("Currently unable to generate an input view for a ReferenceType argument!");
        }
        return v;
    }

    private boolean isNumber(Class<?> clazz) {
        return Number.class.isAssignableFrom(clazz);
    }

    private boolean isFraction(Class<?> type) {
        return Double.class == type || double.class == type || Float.class == type || float.class == type;
    }

    private boolean isString(Class<?> type) {
        return type == String.class;
    }

    private boolean isReferenceType(Class<?> type) {
        return type == ReferenceType.class;
    }
}
