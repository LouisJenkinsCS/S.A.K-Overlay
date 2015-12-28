package com.theif519.sakoverlay.Views.DynamicComponents;

import android.content.Context;

/**
 * Created by theif519 on 12/28/2015.
 */
public class ComponentFactory {

    public static BaseComponent getComponent(Context context, String tag){
        if(tag == null) return null;
        switch(tag){
            case TextComponent.IDENTIFIER:
                return new TextComponent(context);
            case LayoutComponent.IDENTIFIER:
                return new LayoutComponent(context);
            default:
                return null;
        }
    }
}
