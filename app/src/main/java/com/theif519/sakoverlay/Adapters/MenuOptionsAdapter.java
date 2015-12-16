package com.theif519.sakoverlay.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.theif519.sakoverlay.POJO.MenuOptionInfo;
import com.theif519.sakoverlay.R;

import java.util.List;

/**
 * Created by theif519 on 12/15/2015.
 */
public class MenuOptionsAdapter extends ArrayAdapter<MenuOptionInfo> {

    private static final int TYPE_SEPARATOR = 1, TYPE_OPTION = 2;

    private List<MenuOptionInfo> mMenuOptions;
    private Context mContext;

    public MenuOptionsAdapter(List<MenuOptionInfo> list, Context context) {
        super(context, R.layout.menu_option, list);
        mMenuOptions = list;
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MenuOptionsHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.menu_option, null);
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

        public MenuOptionsHolder(View view, MenuOptionInfo info) {
            mDescriptionIcon = (ImageView) view.findViewById(R.id.menu_option_icon);
            mDescriptionText = (TextView) view.findViewById(R.id.menu_option_description);
        }

        public void setup(View v, MenuOptionInfo info) {
            Log.i(getClass().getName(), "Setup: " + info);
            if (info.getType() == MenuOptionInfo.MenuOptionType.MENU_OPTION) {
                if (info.getIconResourceId() != null) {
                    if(mDescriptionIcon == null) {
                        mDescriptionIcon = (ImageView) v.findViewById(R.id.menu_option_icon);
                    }
                    mDescriptionIcon.setImageResource(info.getIconResourceId());
                }
            }
            mDescriptionText.setText(info.getDescriptionText());
        }
    }
}
