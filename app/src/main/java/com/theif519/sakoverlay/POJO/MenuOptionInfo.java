package com.theif519.sakoverlay.POJO;

import android.view.View;

/**
 * Created by theif519 on 12/15/2015.
 */
public class MenuOptionInfo {
    private String mDescriptionText;
    private Integer mIconResourceId;
    private MenuOptionType mType;
    private View.OnClickListener mCallback;

    public enum MenuOptionType {
        SEPARATOR,
        MENU_OPTION
    }

    public MenuOptionInfo(String text, Integer iconResId, View.OnClickListener callback, MenuOptionType type){
        mDescriptionText = text;
        mIconResourceId = iconResId;
        mCallback = callback;
        mType = type;
    }

    public String getDescriptionText() {
        return mDescriptionText;
    }

    public MenuOptionInfo setDescriptionText(String mDescriptionText) {
        this.mDescriptionText = mDescriptionText;
        return this;
    }

    public Integer getIconResourceId() {
        return mIconResourceId;
    }

    public MenuOptionInfo setIconResourceId(Integer resId) {
        mIconResourceId = resId;
        return this;
    }

    public View.OnClickListener getCallback() {
        return mCallback;
    }

    public MenuOptionInfo setCallback(View.OnClickListener mCallback) {
        this.mCallback = mCallback;
        return this;
    }

    public MenuOptionType getType() {
        return mType;
    }

    public MenuOptionInfo setType(MenuOptionType mType) {
        this.mType = mType;
        return this;
    }
}
