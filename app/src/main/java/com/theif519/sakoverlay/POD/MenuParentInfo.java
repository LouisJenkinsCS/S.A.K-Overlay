package com.theif519.sakoverlay.POD;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by theif519 on 11/23/2015.
 */
public class MenuParentInfo {
    private String mDescription;
    ArrayList<MenuChildInfo> mChildren;

    public MenuParentInfo() {

    }

    public MenuParentInfo addChild(MenuChildInfo child){
        mChildren.add(child);
        return this;
    }

    public int getChildCount(){
        return mChildren.size();
    }

    public MenuChildInfo getChildAt(int index){
        try {
            return mChildren.get(index);
        } catch(IndexOutOfBoundsException e){
            Log.w(getClass().getName(), e.getMessage());
            return null;
        }
    }

    public MenuParentInfo addAllChildren(ArrayList<MenuChildInfo> children){
        mChildren.addAll(children);
        return this;
    }

    public MenuParentInfo setDescription(String description){
        mDescription = description;
        return this;
    }

    public String getDescription(){
        return mDescription;
    }
}
