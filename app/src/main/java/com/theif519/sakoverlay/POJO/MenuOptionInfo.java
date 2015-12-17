package com.theif519.sakoverlay.POJO;

/**
 * Created by theif519 on 12/15/2015.
 */
public class MenuOptionInfo {
    private String mDescriptionText;
    private Integer mIconResourceId;
    private MenuOptionType mType;
    private Runnable mCallback;

    public enum MenuOptionType {
        SEPARATOR,
        MENU_OPTION
    }

    public MenuOptionInfo(String text, Integer iconResId, Runnable callback, MenuOptionType type){
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

    public Runnable getCallback() {
        return mCallback;
    }

    public MenuOptionInfo setCallback(Runnable mCallback) {
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

    @Override
    public String toString() {
        return "MenuOptionInfo{" +
                "mDescriptionText='" + mDescriptionText + '\'' +
                ", mIconResourceId=" + mIconResourceId +
                ", mType=" + mType +
                ", mCallback=" + mCallback +
                '}';
    }
}
