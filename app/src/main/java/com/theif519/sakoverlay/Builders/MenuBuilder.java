package com.theif519.sakoverlay.Builders;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.theif519.sakoverlay.Adapters.MenuOptionsAdapter;
import com.theif519.sakoverlay.POJO.MenuOptionInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by theif519 on 12/15/2015.
 */
public class MenuBuilder {

    private List<MenuOptionInfo> mMenuOptions;

    public MenuBuilder() {
        mMenuOptions = new ArrayList<>();
    }

    public MenuBuilder addOption(String descriptionText, Integer iconResourceId, View.OnClickListener onClick) {
        mMenuOptions.add(new MenuOptionInfo(descriptionText, iconResourceId, onClick, MenuOptionInfo.MenuOptionType.MENU_OPTION));
        return this;
    }

    public MenuBuilder addSeperator(String descriptionText, Integer iconResourceId) {
        mMenuOptions.add(new MenuOptionInfo(descriptionText, iconResourceId, null, MenuOptionInfo.MenuOptionType.SEPARATOR));
        return this;
    }

    public PopupWindow create(Context context) {
        PopupWindow window = new PopupWindow(new ListView(context), 300, 500, true);
        ((ListView) window.getContentView()).setAdapter(new MenuOptionsAdapter(mMenuOptions, context));
        ((ListView) window.getContentView()).setOnItemClickListener(((parent, view, position, id) -> {
            mMenuOptions.get(position).getCallback().onClick(null);
            window.dismiss();
        }));
        window.setBackgroundDrawable(new BitmapDrawable());
        window.setOutsideTouchable(true);
        return window;
    }
}
