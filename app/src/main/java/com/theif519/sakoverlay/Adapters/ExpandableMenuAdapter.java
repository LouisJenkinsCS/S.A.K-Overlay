package com.theif519.sakoverlay.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.theif519.sakoverlay.Beans.MenuChildInfo;
import com.theif519.sakoverlay.Beans.MenuParentInfo;
import com.theif519.sakoverlay.R;

import java.util.ArrayList;

/**
 * Created by theif519 on 11/22/2015.
 */
public class ExpandableMenuAdapter extends BaseExpandableListAdapter {

    private static final int PARENT_RESOURCE_ID = R.layout.list_view_default_parent;

    private static final int CHILD_RESOURCE_ID = R.layout.list_view_default_child;

    private Context mContext;
    private ArrayList<MenuParentInfo> mParents;

    public ExpandableMenuAdapter(Context context, ArrayList<MenuParentInfo> parents) {
        mParents = parents;
        mContext = context;
    }

    @Override
    public int getGroupCount() {
        return 0;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mParents.get(groupPosition).getChildCount();
    }

    @Override
    public MenuParentInfo getGroup(int groupPosition) {
        return mParents.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mParents.get(groupPosition).getChildAt(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        View row = convertView;
        MenuParentInfoHolder holder = null;
        if(row == null){
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(CHILD_RESOURCE_ID, parent, false);
            holder = new MenuParentInfoHolder(row);
            row.setTag(holder);
        } else {
            holder = (MenuParentInfoHolder) row.getTag();
        }
        holder.setup(mParents.get(groupPosition));
        return row;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        View row = convertView;
        MenuChildInfoHolder holder = null;
        if(row == null){
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(PARENT_RESOURCE_ID, parent, false);
            holder = new MenuChildInfoHolder(row);
            row.setTag(holder);
        } else {
            holder = (MenuChildInfoHolder) row.getTag();
        }
        holder.setup(mParents.get(groupPosition).getChildAt(childPosition));
        return row;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return mParents.get(groupPosition).getChildAt(childPosition).isSelectable();
    }

    private class MenuParentInfoHolder {
        private TextView mDescription;

        public MenuParentInfoHolder(View view, MenuParentInfo info){
            this(view);
            setup(info);
        }

        public MenuParentInfoHolder(View view){
            mDescription = (TextView) view.findViewById(R.id.list_view_default_parent_description);
        }

        public void setup(MenuParentInfo info){
            mDescription.setText(info.getDescription());
        }
    }

    private class MenuChildInfoHolder {
        private TextView mDescription;
        private ImageView mIcon;

        public MenuChildInfoHolder(View view, MenuChildInfo info){
            this(view);
            setup(info);
        }

        public MenuChildInfoHolder(View view){
            mDescription = (TextView) view.findViewById(R.id.list_view_default_child_description);
            mIcon = (ImageView) view.findViewById(R.id.list_view_default_child_icon);
        }

        public void setup(MenuChildInfo info){
            mDescription.setText(info.getDescription());
            mIcon.setImageBitmap(info.getIcon());
        }
    }

}
