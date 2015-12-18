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

    public MenuBuilder addOption(String descriptionText, Integer iconResourceId, Runnable callback) {
        mMenuOptions.add(new MenuOptionInfo(descriptionText, iconResourceId, callback, MenuOptionInfo.MenuOptionType.MENU_OPTION));
        return this;
    }

    public MenuBuilder addSeparator(String descriptionText) {
        mMenuOptions.add(new MenuOptionInfo(descriptionText, null, null, MenuOptionInfo.MenuOptionType.SEPARATOR));
        return this;
    }

    public PopupWindow create(Context context) {
        PopupWindow window = new PopupWindow(new ListView(context), 300, 500, true);
        MenuOptionsAdapter adapter = new MenuOptionsAdapter(mMenuOptions, context);
        ListView listView = (ListView) window.getContentView();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(((parent, view, position, id) -> {
            MenuOptionInfo info = adapter.getItem(position);
            if (info.getCallback() != null) {
                info.getCallback().run();
            }
            if (info.getType() == MenuOptionInfo.MenuOptionType.MENU_OPTION) {
                window.dismiss();
            }
        }));
        listView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        window.setBackgroundDrawable(new BitmapDrawable());
        window.setOutsideTouchable(true);
        return window;
    }
}
