package com.theif519.sakoverlay.Components.POJO;

import android.content.Context;
import android.support.annotation.NonNull;

import com.theif519.sakoverlay.Components.BaseComponent;

/**
 * Created by theif519 on 1/3/2016.
 */
public class ComponentSelectorItem {

    public interface ComponentCreator {
        @NonNull
        BaseComponent createComponent(Context context, String title);
    }

    private ComponentCreator mCreator;

    private String mItemTitle;

    public ComponentSelectorItem(String mItemTitle, ComponentCreator creator) {
        this.mItemTitle = mItemTitle;
        this.mCreator = creator;
    }

    @NonNull
    public BaseComponent create(Context context){
        return mCreator.createComponent(context, mItemTitle);
    }

    @Override
    public String toString() {
        return mItemTitle;
    }
}
