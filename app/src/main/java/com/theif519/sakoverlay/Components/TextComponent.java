package com.theif519.sakoverlay.Components;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.annimon.stream.Optional;
import com.theif519.sakoverlay.Components.Misc.AttributeMenuHelper;
import com.theif519.sakoverlay.Components.Misc.BaseViewManager;
import com.theif519.sakoverlay.Components.Types.Actions.Impl.Actions;
import com.theif519.sakoverlay.Components.Types.Actions.Impl.TextActions;
import com.theif519.sakoverlay.Components.Types.Conditionals.Impl.Conditionals;
import com.theif519.sakoverlay.Components.Types.Conditionals.Impl.TextConditionals;
import com.theif519.sakoverlay.Core.Views.AutoResizeTextView;
import com.theif519.sakoverlay.R;

import org.json.JSONException;
import org.json.JSONObject;

import rx.Observable;

/**
 * Created by theif519 on 12/28/2015.
 */
public class TextComponent extends BaseComponent {

    protected TextView TEXT_VIEW;
    public static final String IDENTIFIER = "TextView";
    public static final String TEXT_VALUE = "Text Value";

    public TextComponent(Context context, String key) {
        super(context, key);
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

    @Override
    protected AttributeMenuHelper createAttributeMenu() {
        return super.createAttributeMenu()
                .add("Text", createTextAttrs());
    }

    private BaseViewManager createTextAttrs(){
        ViewGroup layout = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.component_text, null);
        final TextView value = (TextView) layout.findViewById(R.id.component_text_value);
        return new BaseViewManager(layout) {
            @Override
            public Optional<String> validate() {
                return Optional.empty();
            }

            @Override
            public void handle() {
                TEXT_VIEW.setText(value.getText());
            }

            @Override
            public void reset() {
                value.setText(TEXT_VIEW.getText());
            }

            @NonNull
            @Override
            public Observable<Void> observeStateChanges() {
                return Observable.never();
            }
        };
    }

    @Override
    public Class<? extends Conditionals> getConditionalClass() {
        return TextConditionals.class;
    }

    @Override
    public Class<? extends Actions> getActionClass() {
        return TextActions.class;
    }
}
