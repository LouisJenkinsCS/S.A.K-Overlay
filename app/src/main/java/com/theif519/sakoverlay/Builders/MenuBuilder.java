package com.theif519.sakoverlay.Builders;

import android.content.Context;
import android.view.View;

import com.annimon.stream.Stream;
import com.theif519.sakoverlay.Adapters.MenuOptionsAdapter;
import com.theif519.sakoverlay.POJO.MenuOptionInfo;
import com.theif519.sakoverlay.Views.DropdownMenu;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by theif519 on 12/15/2015.
 */
public class MenuBuilder {

    private List<MenuOptionInfo> mMenuOptions;
    private Runnable mOnEachClickCallback;

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

    /**
     * If this is called, the callback will be called after each onClick listener by wrapping the old onClickListener
     * with this callback. Useful for dimissing PopupWindows after selecting.
     *
     * @param callback Called after View.OnClickListener
     * @return This instance, used for chaining together multiple operations.
     */
    public MenuBuilder setOnClickCallback(Runnable callback) {
        mOnEachClickCallback = callback;
        return this;
    }

    public DropdownMenu create(Context context) {
        if (mOnEachClickCallback != null) {
            Stream
                    .of(mMenuOptions)
                    .filter(option -> option.getType() == MenuOptionInfo.MenuOptionType.MENU_OPTION && option.getCallback() != null)
                    .forEach(option -> option.setCallback(
                            v -> {
                                option.getCallback().onClick(v);
                                mOnEachClickCallback.run();
                            }
                    ));
        }
        return new DropdownMenu(context)
                .setMenuOptionsAdapter(new MenuOptionsAdapter(mMenuOptions, context));
    }
}
