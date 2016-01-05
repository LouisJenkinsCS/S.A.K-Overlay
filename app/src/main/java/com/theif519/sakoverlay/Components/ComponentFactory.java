package com.theif519.sakoverlay.Components;

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
            case EditTextComponent.IDENTIFIER:
                return new EditTextComponent(context);
            default:
                return null;
        }
    }
}
