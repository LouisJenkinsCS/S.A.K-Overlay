package com.theif519.sakoverlay.Builders;

import android.content.Context;

import com.theif519.sakoverlay.Views.ExpandableListViewMenu;

/**
 * Created by theif519 on 11/21/2015.
 */
public class MenuBuilder {

    interface OnOptionSelectedListener {
        void onOptionSelected(String option);
    }

    private Context mContext;

    public MenuBuilder(Context context) {
        mContext = context;
    }

    public MenuBuilder addOption(String option, OnOptionSelectedListener listener){
        // TODO: Implement this so it sets up a new root menu.
        return this;
    }

    public MenuBuilder addNestedOption(String option, OnOptionSelectedListener listener){
        // TODO: Implement this so it sets up a nested root menu for the last selected menu. Should have same functionality as addMenu if there is none.
        return this;
    }

    public ExpandableListViewMenu build(){
        // TODO: Implement this so it sets up the new menu.
        return null;
    }

    /*
        ExpandableListViewMenu menu = ExpandableListViewMenu.Builder(getActivity())
            .addNestedOption("New")
                .addOption("Project", callback)
                .addOption("Document", callback)
                .back()
            .addOption("Open", callback)
            .addOption("Close", callback)
        .build();
     */
}
