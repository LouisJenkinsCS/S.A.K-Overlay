package com.theif519.sakoverlay;

import android.util.ArrayMap;

/**
 * Created by theif519 on 10/31/2015.
 */
public class FloatingFragmentFactory {
    private static final FloatingFragmentFactory INSTANCE = new FloatingFragmentFactory();

    public static FloatingFragmentFactory getInstance() {
        return INSTANCE;
    }

    public FloatingFragment getFragment(ArrayMap<String, String> map) {
        FloatingFragment fragment = createFragment(map.get(FloatingFragment.LAYOUT_TAG_KEY));
        if(fragment == null) return null;
        fragment.mContext = map;
        return fragment;
    }

    private FloatingFragment createFragment(String layoutTag) {
        switch (layoutTag) {
            case StickyNoteFragment.IDENTIFIER:
                return StickyNoteFragment.newInstance();
            case IntroductionFragment.IDENTIFIER:
                return IntroductionFragment.newInstance();
            case GoogleMapsFragment.IDENTIFIER:
                return GoogleMapsFragment.newInstance();
            case WebBrowserFragment.IDENTIFIER:
                return WebBrowserFragment.newInstance();
            default:
                return null;
        }
    }
}
