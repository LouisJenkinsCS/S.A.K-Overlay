package com.theif519.sakoverlay.Builders;

import android.content.Context;
import android.view.View;

import com.theif519.sakoverlay.Views.ExpandableMenu;

/**
 * Created by theif519 on 11/21/2015.
 */
public class ExpandableMenuBuilder {

    /**
     * Any and all menu options must implement this.
     */
    interface MenuOption {
        /**
         * When this option is selected, this callback is invoked.
         */
        View inflateOnSelected();
    }

    private Context mContext;

    public ExpandableMenuBuilder(Context context) {
        mContext = context;
    }

    public ExpandableMenuBuilder setView(int resourceId){
        // TODO: Implement this so it sets the menu's main view.
        return this;
    }

    public ExpandableMenuBuilder setView(View view){
        // TODO: Implement this so it sets the menu's main view.
        return this;
    }

    public ExpandableMenuBuilder setNestedView(View view){
        // TODO: Implement this so it sets the current nested view to the passed view.
        return this;
    }

    public ExpandableMenuBuilder setNestedView(int resourceId){
        // TODO: Implement this so it sets the current nested view to the passed view.
        return this;
    }

    public ExpandableMenuBuilder addOption(MenuOption option){
        // TODO: Implement this so it sets up a new root menu.
        return this;
    }

    public ExpandableMenuBuilder addNestedOption(MenuOption option){
        // TODO: Implement this so it sets up a nested root menu for the last selected menu. Should have same functionality as addMenu if there is none.
        return this;
    }

    public ExpandableMenu build(){
        // TODO: Implement this so it sets up the new menu.
        return null;
    }

    /*
        ExpandableListViewMenu menu = ExpandableListViewMenu.Builder(getActivity())
            .addOption(new CustomMenuOption("New"), null)
                .addNestedOption("Project", callback)
                .addNestedOption("Document", callback)
            .addOption("Open", callback)
            .addOption("Close", callback)
        .build();
     */
}
