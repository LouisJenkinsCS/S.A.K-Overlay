package com.theif519.sakoverlay.Components.View;

import android.content.Context;
import android.util.ArrayMap;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.theif519.sakoverlay.Components.Misc.BaseViewManager;
import com.theif519.sakoverlay.R;

import java.util.Map;
import java.util.Random;

/**
 * Created by theif519 on 1/16/2016.
 */
public class AttributeMenuManager extends LinearLayout {

    private TextView mComponentName, mCategoryName;
    private PopupMenu mMenu;
    private ViewFlipper mFlipper;
    private Map<String, Pair<Integer, AttributeMenu>> mMappedCategories = new ArrayMap<>();

    public AttributeMenuManager(Context context, String componentName) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.component_attribute_editor, this);
        mFlipper = (ViewFlipper) findViewById(R.id.component_attribute_editor_flipper);
        mComponentName = (TextView) findViewById(R.id.component_attribute_editor_title);
        mComponentName.setText(componentName);
        mCategoryName = (TextView) findViewById(R.id.component_attribute_editor_category);
        mMenu = new PopupMenu(context, mCategoryName);
        mMenu.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            mFlipper.setDisplayedChild(mFlipper.indexOfChild(mMappedCategories.get(title).second));
            mCategoryName.setText(title);
            return true;
        });
        mCategoryName.setOnClickListener(v -> mMenu.show());
        findViewById(R.id.component_attribute_editor_submit).setOnClickListener(v -> {
            StringBuilder errMsg = new StringBuilder();
            getAttributeMenus()
                    .filter(menu -> menu.validate().isPresent())
                    .forEach(errMsg::append);
            if(errMsg.toString().isEmpty()){
                getAttributeMenus()
                        .forEach(AttributeMenu::handle);
            } else {
                Toast.makeText(getContext(), errMsg.toString(), Toast.LENGTH_LONG).show();
            }
        });
        // TODO: Probably install a listener to mFlipper's LayoutChange to monitor when View changes to change text view with it.
    }

    public void add(String category, BaseViewManager manager) {
        if (!mMappedCategories.containsKey(category)) {
            int id = new Random().nextInt();
            mMenu.getMenu().add(Menu.NONE, id, mMappedCategories.size(), category);
            AttributeMenu menu = new AttributeMenu(getContext());
            mMappedCategories.put(category, Pair.create(mMappedCategories.size(), menu));
            mFlipper.addView(menu);
        } else {
            mMappedCategories.get(category).second.add(manager);
        }
    }

    public void remove(String category) {
        if (!mMappedCategories.containsKey(category)) {
            throw new RuntimeException("No category \"" + category + "\" was found in AttributeMenuManager map!");
        }
        Pair<Integer, AttributeMenu> menuPair = mMappedCategories.get(category);
        int id = menuPair.first;
        AttributeMenu menu = menuPair.second;
        menu.removeAll();
        mFlipper.removeView(menu);
        mMenu.getMenu().removeItem(id);
    }

    public void reset() {
        getAttributeMenus()
                .forEach(AttributeMenu::reset);
    }

    public Optional<String> validate() {
        StringBuilder errMsg = new StringBuilder();
        getAttributeMenus()
                .map(AttributeMenu::validate)
                .filter(Optional::isPresent)
                .forEach(errMsg::append);
        return errMsg.toString().isEmpty() ? Optional.empty() : Optional.of(errMsg.toString());
    }

    public void handle() {
        getAttributeMenus()
                .forEach(AttributeMenu::handle);
    }

    private Stream<AttributeMenu> getAttributeMenus() {
        return Stream.of(mMappedCategories)
                .map(Map.Entry::getValue)
                .map(pair -> pair.second);
    }
}