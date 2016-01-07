package com.theif519.sakoverlay.Components.Activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.theif519.sakoverlay.Components.BaseComponent;
import com.theif519.sakoverlay.Components.Builders.ComponentSelectorBuilder;
import com.theif519.sakoverlay.Components.ButtonComponent;
import com.theif519.sakoverlay.Components.EditTextComponent;
import com.theif519.sakoverlay.Components.LayoutComponent;
import com.theif519.sakoverlay.Components.Misc.ReferenceHelper;
import com.theif519.sakoverlay.Components.Misc.ReferenceType;
import com.theif519.sakoverlay.Components.TextComponent;
import com.theif519.sakoverlay.Components.View.NonModalDrawerLayout;
import com.theif519.sakoverlay.R;
import com.theif519.sakoverlay.Core.Rx.RxBus;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by theif519 on 12/26/2015.
 */
public class LayoutCreatorActivity extends Activity {

    private ReferenceHelper mHelper = new ReferenceHelper();

    private ViewGroup mLayout;
    private NonModalDrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout_creater);
        mDrawerLayout = (NonModalDrawerLayout) findViewById(R.id.layout_creator_root);
        mDrawerLayout.setScrimColor(getResources().getColor(android.R.color.transparent));
        mLayout = (ViewGroup) findViewById(R.id.layout_view);
        new ComponentSelectorBuilder()
                .addCategory("Text")
                .addComponent(TextComponent.IDENTIFIER, null)
                .addComponent(ButtonComponent.IDENTIFIER, null)
                .addComponent(EditTextComponent.IDENTIFIER, null)
                .addCategory("Grouping")
                .addComponent(LayoutComponent.IDENTIFIER, null)
                .addCategory("Image")
                .addCategory("Web")
                .onCreate(this::addComponent)
                .build(this, (ExpandableListView) findViewById(R.id.component_selector_list));
        RxBus.observe(View.class)
                .subscribe(v -> {
                    ViewGroup viewGroup = (ViewGroup) findViewById(R.id.component_attribute_editor_container);
                    viewGroup.removeAllViews();
                    viewGroup.addView(v);
                });
        mDrawerLayout.setModalView(findViewById(R.id.layout_creator_attributes));
        findViewById(R.id.layout_close).setOnClickListener(v -> {
            Log.i(getClass().getName(), "Serialized Data: { " + new String(serialize()) + " }");
            finish();
        });
    }

    private void addComponent(BaseComponent component, String id) {
        mHelper.add(ReferenceType.from(component, id, component.getConditionalClass(), component.getActionClass()));
        component.setHelper(mHelper);
        mLayout.addView(component);
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    private byte[] serialize() {
        JSONArray array = new JSONArray();
        for (int i = 0; i < mLayout.getChildCount(); i++) {
            View v = mLayout.getChildAt(i);
            if (v instanceof BaseComponent) {
                BaseComponent component = (BaseComponent) v;
                array.put(component.serialize());
            }
        }
        return array.toString().getBytes();
    }

    private void deserialize(byte[] data) {
        try {
            JSONArray array = new JSONArray(new String(data));
            // Get Widget from Factory, pass context and JSONObject serialized data.
        } catch (JSONException e) {
            throw new RuntimeException("Error parsing JSONArray: Threw a JSONException with message \"" + e.getMessage() + "\"");
        }
    }
}
