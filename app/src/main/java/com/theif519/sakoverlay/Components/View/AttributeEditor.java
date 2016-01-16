package com.theif519.sakoverlay.Components.View;

import android.content.Context;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.theif519.sakoverlay.R;

import java.util.Map;

/**
 * Created by theif519 on 1/16/2016.
 */
public class AttributeEditor extends LinearLayout {

    private TextView mComponentName, mCategoryName;
    private PopupMenu mMenu;
    private ViewFlipper mFlipper;
    private Map<String, ViewGroup> mMappedCategories;

    public AttributeEditor(Context context, String componentName) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.component_attribute_editor, this);
        mFlipper = (ViewFlipper) findViewById(R.id.component_attribute_editor_flipper);
        mComponentName = (TextView) findViewById(R.id.component_attribute_editor_title);
        mComponentName.setText(componentName);
        mCategoryName = (TextView) findViewById(R.id.component_attribute_editor_category);
        mMappedCategories = new ArrayMap<>();
        mMenu = new PopupMenu(context, mCategoryName);
        mMenu.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            mFlipper.setDisplayedChild(mFlipper.indexOfChild(mMappedCategories.get(title)));
            mCategoryName.setText(title);
            return true;
        });
    }

    public AttributeEditor add(String category, View v) {
        if (!mMappedCategories.containsKey(category)) {
            mMenu.getMenu().add(Menu.NONE, Menu.NONE, mMappedCategories.size(), category);
            LinearLayout layout = new LinearLayout(getContext());
            layout.setOrientation(VERTICAL);
            layout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            layout.addView(v);
            mMappedCategories.put(category, layout);
            mFlipper.addView(layout);
        } else {
            mMappedCategories.get(category).addView(v);
        }
        return this;
    }
}
