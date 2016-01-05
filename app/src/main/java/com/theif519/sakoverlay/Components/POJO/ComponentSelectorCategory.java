package com.theif519.sakoverlay.Components.POJO;

import com.theif519.sakoverlay.Components.POJO.ComponentSelectorItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by theif519 on 1/3/2016.
 */
public class ComponentSelectorCategory {
    private String mCategoryTitle;
    private List<ComponentSelectorItem> mChildren;

    public ComponentSelectorCategory(String mCategoryTitle) {
        this.mCategoryTitle = mCategoryTitle;
        mChildren = new ArrayList<>();
    }

    public ComponentSelectorItem get(int index){
        return mChildren.get(index);
    }

    public int size(){
        return mChildren.size();
    }

    @Override
    public String toString() {
        return mCategoryTitle;
    }

    public void add(ComponentSelectorItem item){
        mChildren.add(item);
    }
}
