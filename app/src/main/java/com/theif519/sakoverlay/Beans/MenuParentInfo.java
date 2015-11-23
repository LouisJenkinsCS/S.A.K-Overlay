package com.theif519.sakoverlay.Beans;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by theif519 on 11/23/2015.
 */
public class MenuParentInfo implements Parcelable {
    private String mDescription;
    ArrayList<MenuChildInfo> mChildren;

    public MenuParentInfo() {

    }

    protected MenuParentInfo(Parcel in) {
        mDescription = in.readString();
        mChildren = in.createTypedArrayList(MenuChildInfo.CREATOR);
    }

    public static final Creator<MenuParentInfo> CREATOR = new Creator<MenuParentInfo>() {
        @Override
        public MenuParentInfo createFromParcel(Parcel in) {
            return new MenuParentInfo(in);
        }

        @Override
        public MenuParentInfo[] newArray(int size) {
            return new MenuParentInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mDescription);
        dest.writeTypedList(mChildren);
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
