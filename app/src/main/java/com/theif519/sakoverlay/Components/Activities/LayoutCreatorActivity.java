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
import com.theif519.sakoverlay.Components.Misc.AttributeMenuHelper;
import com.theif519.sakoverlay.Components.Misc.ReferenceHelper;
import com.theif519.sakoverlay.Components.TextComponent;
import com.theif519.sakoverlay.Components.Types.ReferenceType;
import com.theif519.sakoverlay.Components.View.NonModalDrawerLayout;
import com.theif519.sakoverlay.R;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by theif519 on 12/26/2015.
 */
public class LayoutCreatorActivity extends Activity {

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
                .addComponent(TextComponent.TEXT_VALUE, null)
                .addComponent(ButtonComponent.TEXT_VALUE, null)
                .addComponent(EditTextComponent.TEXT_VALUE, null)
                .addCategory("Grouping")
                .addComponent(LayoutComponent.TEXT_VALUE, null)
                .addCategory("Image")
                .addCategory("Web")
                .onCreate(this::addComponent)
                .build(this, (ExpandableListView) findViewById(R.id.component_selector_list));
        mDrawerLayout.setModalView(findViewById(R.id.layout_creator_attributes_container));
        AttributeMenuHelper.initialize(this, (ViewGroup) findViewById(R.id.layout_creator_attributes_container));
        findViewById(R.id.layout_close).setOnClickListener(v -> {
            Log.i(getClass().getName(), "Serialized Data: { " + new String(serialize()) + " }");
            finish();
        });
    }

    // TODO: Make generic so that "<T extends BaseComponent> void addComponent(T component, Class<T> clazz, String name)" is the declaration.
    private void addComponent(BaseComponent component, String id) {
        ReferenceHelper.getInstance().add(ReferenceType.from(component));
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
