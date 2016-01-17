package com.theif519.sakoverlay.Components;

import android.content.Context;
import android.util.Log;

import com.theif519.sakoverlay.Components.Misc.ReferenceHelper;

/**
 * Created by theif519 on 12/28/2015.
 */
public class ComponentFactory {

    public static BaseComponent getComponent(Context context, String tag){
        if(tag == null) return null;
        switch(tag){
            case TextComponent.TEXT_VALUE:
                return new TextComponent(context, generateName(TextComponent.IDENTIFIER));
            case LayoutComponent.TEXT_VALUE:
                return new LayoutComponent(context, generateName(LayoutComponent.IDENTIFIER));
            case EditTextComponent.TEXT_VALUE:
                return new EditTextComponent(context, generateName(EditTextComponent.IDENTIFIER));
            case ButtonComponent.TEXT_VALUE:
                return new ButtonComponent(context, generateName(ButtonComponent.IDENTIFIER));
            default:
                return null;
        }
    }

    public static String generateName(String identifier){
        for(int i = 0; i < Integer.MAX_VALUE; i++){
            String generated = identifier + i;
            if(!ReferenceHelper.getInstance().contains(generated)) return identifier;
        }
        Log.wtf(ComponentFactory.class.getSimpleName(), "Somehow all generated keys from 0 to " + Integer.MAX_VALUE + " has been taken!");
        return null;
    }
}
