package com.theif519.sakoverlay.Components;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.theif519.sakoverlay.Components.Misc.AttributeMenuHelper;
import com.theif519.sakoverlay.Components.Misc.BaseViewManager;
import com.theif519.sakoverlay.Components.Misc.ConstructHelper;
import com.theif519.sakoverlay.R;

import org.json.JSONObject;

/**
 * Created by theif519 on 1/2/2016.
 */
public class ButtonComponent extends TextComponent {

    public static final String IDENTIFIER = "Button";
    public static final String TEXT_VALUE = "Button";

    private Button mOnClickButton;

    public ButtonComponent(Context context, String key) {
        super(context, key);
    }

    @Override
    protected View createView(Context context) {
        TEXT_VIEW = new Button(context);
        TEXT_VIEW.setTextColor(ColorStateList.valueOf(context.getResources().getColor(R.color.black)));
        TEXT_VIEW.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return TEXT_VIEW;
    }

    @Override
    protected AttributeMenuHelper createAttributeMenu() {
        return super.createAttributeMenu()
                .add("Callbacks", createButtonAttrs());
    }

    private BaseViewManager createButtonAttrs(){
        ViewGroup layout = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.component_text_button, null);
        final Button onClick = (Button) layout.findViewById(R.id.component_text_button_onclick);
        onClick.setOnClickListener(v -> new AlertDialog.Builder(getContext())
                .setTitle("Button onClick")
                .setView(new ConstructHelper(getContext()).getView())
                .setPositiveButton("OK!", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setNegativeButton("NO!", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show());
        return BaseViewManager.plain(layout);
    }

    @Override
    public JSONObject serialize() {
        return super.serialize();
    }

    @Override
    public void deserialize(JSONObject obj) {
        super.deserialize(obj);
    }
}
