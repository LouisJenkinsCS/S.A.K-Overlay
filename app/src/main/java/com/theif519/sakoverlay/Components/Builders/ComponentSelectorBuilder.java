package com.theif519.sakoverlay.Components.Builders;

import android.content.Context;
import android.widget.ExpandableListView;

import com.theif519.sakoverlay.Components.Adapters.ComponentSelectorAdapter;
import com.theif519.sakoverlay.Components.BaseComponent;
import com.theif519.sakoverlay.Components.ComponentFactory;
import com.theif519.sakoverlay.Components.POJO.ComponentSelectorCategory;
import com.theif519.sakoverlay.Components.POJO.ComponentSelectorItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by theif519 on 1/3/2016.
 */
public class ComponentSelectorBuilder {

    public interface OnCreateListener {
        void onCreate(BaseComponent component, String id);
    }

    private OnCreateListener mListener;
    private List<ComponentSelectorCategory> mCategories;
    private ComponentSelectorCategory mCurrent;
    private ComponentSelectorItem.ComponentCreator mDefault = ComponentFactory::getComponent;

    public ComponentSelectorBuilder() {
        mCategories = new ArrayList<>();
    }

    public ComponentSelectorBuilder addCategory(String categoryName){
        mCategories.add(mCurrent = new ComponentSelectorCategory(categoryName));
        return this;
    }

    public ComponentSelectorBuilder addComponent(String componentName, ComponentSelectorItem.ComponentCreator creator){
        if(mCurrent == null){
            throw new RuntimeException("Missing category!");
        }
        mCurrent.add(new ComponentSelectorItem(componentName, creator == null ? mDefault : creator));
        return this;
    }

    public ComponentSelectorBuilder onCreate(OnCreateListener listener){
        mListener = listener;
        return this;
    }

    public ComponentSelectorAdapter build(Context context, ExpandableListView listView){
        ComponentSelectorAdapter adapter = new ComponentSelectorAdapter(context, mCategories);
        listView.setAdapter(adapter);
        listView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            if(mListener == null) return false;
            ComponentSelectorItem item = adapter.getChild(groupPosition, childPosition);
            mListener.onCreate(item.create(context), item.toString());
            return true;
        });
        return adapter;
    }
}
