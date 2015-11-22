package com.theif519.sakoverlay.Builders;

import android.content.Context;
import android.view.View;

import com.theif519.sakoverlay.Views.ExpandableListViewMenu;

/**
 * Created by theif519 on 11/21/2015.
 */
public class ExpandableListViewMenuBuilder{

    /**
     * Any and all menu options must implement this.
     */
    interface MenuOption {
        /**
         * When the adapter must inflate this option, this is called.
         * @return Inflated view.
         */
        View inflate(Context context);

        /**
         * When this option is selected, this callback is invoked.
         */
        void onSelected();
    }

    private Context mContext;

    public ExpandableListViewMenuBuilder(Context context) {
        mContext = context;
    }

    public ExpandableListViewMenuBuilder setView(int resourceId){
        // TODO: Implement this so it sets the menu's main view.
        return this;
    }

    public ExpandableListViewMenuBuilder setView(View view){
        // TODO: Implement this so it sets the menu's main view.
        return this;
    }

    public ExpandableListViewMenuBuilder setNestedView(View view){
        // TODO: Implement this so it sets the current nested view to the passed view.
        return this;
    }

    public ExpandableListViewMenuBuilder setNestedView(int resourceId){
        // TODO: Implement this so it sets the current nested view to the passed view.
        return this;
    }

    public ExpandableListViewMenuBuilder addOption(MenuOption option){
        // TODO: Implement this so it sets up a new root menu.
        return this;
    }

    public ExpandableListViewMenuBuilder addNestedOption(MenuOption option){
        // TODO: Implement this so it sets up a nested root menu for the last selected menu. Should have same functionality as addMenu if there is none.
        return this;
    }

    public ExpandableListViewMenu build(){
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
