package com.theif519.sakoverlay.Core.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.theif519.sakoverlay.Core.POJO.MenuOptionInfo;
import com.theif519.sakoverlay.R;

import java.util.List;

/**
 * Created by theif519 on 12/15/2015.
 */
public class MenuOptionsAdapter extends ArrayAdapter<MenuOptionInfo> {

    private static final int TYPE_OPTION = 0, TYPE_SEPARATOR = 1;

    private List<MenuOptionInfo> mMenuOptions;

    public MenuOptionsAdapter(List<MenuOptionInfo> list, Context context) {
        super(context, R.layout.menu_option, list);
        mMenuOptions = list;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return this.getItem(position).getType() == MenuOptionInfo.MenuOptionType.MENU_OPTION ? TYPE_OPTION : TYPE_SEPARATOR;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MenuOptionsHolder holder;
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            switch(getItemViewType(position)){
                case TYPE_SEPARATOR:
                    convertView = inflater.inflate(R.layout.menu_option_separator, null);
                    break;
                case TYPE_OPTION:
                    convertView = inflater.inflate(R.layout.menu_option, null);
                    break;
                default:
                    throw new RuntimeException("Invalid Menu Type!");
            }
            holder = new MenuOptionsHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (MenuOptionsHolder) convertView.getTag();
        }
        holder.setup(mMenuOptions.get(position));
        return convertView;
    }

    private static class MenuOptionsHolder {
        private ImageView mDescriptionIcon;
        private TextView mDescriptionText;

        public MenuOptionsHolder(View view) {
            mDescriptionIcon = (ImageView) view.findViewById(R.id.menu_option_icon);
            mDescriptionText = (TextView) view.findViewById(R.id.menu_option_description);
        }

        public void setup(MenuOptionInfo info) {
            Log.i(getClass().getName(), "Setup: " + info);
            if (info.getType() == MenuOptionInfo.MenuOptionType.MENU_OPTION) {
                if (info.getIconResourceId() != null) {
                    mDescriptionIcon.setImageResource(info.getIconResourceId());
                } else mDescriptionIcon.setVisibility(View.GONE);
            }
            mDescriptionText.setText(info.getDescriptionText());
        }
    }
}
