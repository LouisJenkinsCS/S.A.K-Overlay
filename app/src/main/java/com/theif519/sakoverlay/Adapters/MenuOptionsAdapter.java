package com.theif519.sakoverlay.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.theif519.sakoverlay.POJO.MenuOptionInfo;
import com.theif519.sakoverlay.R;

import java.util.List;

/**
 * Created by theif519 on 12/15/2015.
 */
public class MenuOptionsAdapter extends BaseAdapter {

    private static final int TYPE_SEPARATOR = 1, TYPE_OPTION = 2;

    private List<MenuOptionInfo> mMenuOptions;
    private Context mContext;

    public MenuOptionsAdapter(List<MenuOptionInfo> list, Context context) {
        mMenuOptions = list;
        mContext = context;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return mMenuOptions.get(position).getType() == MenuOptionInfo.MenuOptionType.MENU_OPTION ? 2 : 1;
    }

    @Override
    public int getCount() {
        return mMenuOptions.size();
    }

    @Override
    public MenuOptionInfo getItem(int position) {
        return mMenuOptions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MenuOptionsHolder holder;
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            switch(getItemViewType(position)){
                case TYPE_OPTION:
                    convertView = inflater.inflate(R.layout.menu_child_item, null);
                    break;
                case TYPE_SEPARATOR:
                    convertView = inflater.inflate(R.layout.menu_option_separator, null);
                    break;
            }
            holder = new MenuOptionsHolder(convertView, mMenuOptions.get(position));
            convertView.setTag(holder);
        } else {
            holder = (MenuOptionsHolder) convertView.getTag();
        }
        holder.setup(convertView, mMenuOptions.get(position));
        return convertView;
    }

    private static class MenuOptionsHolder {
        private ImageView mDescriptionIcon;
        private TextView mDescriptionText;

        public MenuOptionsHolder(View view, MenuOptionInfo info){
            if(info.getType() == MenuOptionInfo.MenuOptionType.MENU_OPTION){
                mDescriptionIcon = (ImageView) view.findViewById(R.id.menu_child_item_icon);
            }
            mDescriptionText = (TextView) view.findViewById(R.id.menu_option_description);
        }

        public void setup(View v, MenuOptionInfo info){
            if(info.getType() == MenuOptionInfo.MenuOptionType.MENU_OPTION){
                v.findViewById(R.id.menu_child_item_clickable).setOnClickListener(info.getCallback());
                if(info.getIconResourceId() != null){
                    mDescriptionIcon.setImageResource(info.getIconResourceId());
                } else {
                    mDescriptionIcon.setVisibility(View.GONE);
                }
            }
            mDescriptionText.setText(info.getDescriptionText());
        }
    }
}
