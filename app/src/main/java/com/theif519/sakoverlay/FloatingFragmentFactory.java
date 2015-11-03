package com.theif519.sakoverlay;

import android.util.ArrayMap;

/**
 * Created by theif519 on 10/31/2015.
 */
public class FloatingFragmentFactory {
    private static final FloatingFragmentFactory INSTANCE = new FloatingFragmentFactory();

    public static FloatingFragmentFactory getInstance(){
        return INSTANCE;
    }

    public FloatingFragment getFragment(ArrayMap<String, String> map){
        switch(map.get(FloatingFragment.LAYOUT_TAG_KEY)){
            case StickyNoteFragment.LAYOUT_TAG:
                return StickyNoteFragment.deserialize(map);
            case IntroductionFragment.LAYOUT_TAG:
                return IntroductionFragment.newInstance();
            default:
                return null;
        }
    }
}
