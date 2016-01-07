package com.theif519.sakoverlay.Components.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.theif519.sakoverlay.Components.POJO.ComponentSelectorCategory;
import com.theif519.sakoverlay.Components.POJO.ComponentSelectorItem;
import com.theif519.sakoverlay.R;

import java.util.List;

/**
 * Created by theif519 on 1/7/2016.
 */
public class ComponentConstructAdapter extends BaseExpandableListAdapter {

    private static final class ViewHolder {
        TextView textLabel;
    }

    private final List<ComponentSelectorCategory> itemList;
    private final LayoutInflater inflater;

    public ComponentConstructAdapter(Context context, List<ComponentSelectorCategory> itemList) {
        this.inflater = LayoutInflater.from(context);
        this.itemList = itemList;
    }

    @Override
    public ComponentSelectorItem getChild(int groupPosition, int childPosition) {
        return itemList.get(groupPosition).get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return itemList.get(groupPosition).size();
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, final ViewGroup parent) {
        View resultView = convertView;
        ViewHolder holder;
        if (resultView == null) {
            resultView = inflater.inflate(R.layout.component_selector_item, null); //TODO change layout id
            holder = new ViewHolder();
            holder.textLabel = (TextView) resultView.findViewById(R.id.component_selector_item_title); //TODO change view id
            resultView.setTag(holder);
        } else {
            holder = (ViewHolder) resultView.getTag();
        }
        final ComponentSelectorItem item = getChild(groupPosition, childPosition);
        holder.textLabel.setText(item.toString());
        return resultView;
    }

    @Override
    public ComponentSelectorCategory getGroup(int groupPosition) {
        return itemList.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return itemList.size();
    }

    @Override
    public long getGroupId(final int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View theConvertView, ViewGroup parent) {
        View resultView = theConvertView;
        ViewHolder holder;
        if (resultView == null) {
            resultView = inflater.inflate(R.layout.component_selector_category, null); //TODO change layout id
            holder = new ViewHolder();
            holder.textLabel = (TextView) resultView.findViewById(R.id.component_selector_category_title); //TODO change view id
            resultView.setTag(holder);
        } else {
            holder = (ViewHolder) resultView.getTag();
        }
        final ComponentSelectorCategory item = getGroup(groupPosition);
        holder.textLabel.setText(item.toString());
        return resultView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
