package com.theif519.sakoverlay.FloatingFragments;

import android.util.ArrayMap;

import com.theif519.sakoverlay.Misc.Globals;

/**
 * Created by theif519 on 10/31/2015.
 */
public class FloatingFragmentFactory {
    private static final FloatingFragmentFactory INSTANCE = new FloatingFragmentFactory();

    public static FloatingFragmentFactory getInstance() {
        return INSTANCE;
    }

    public FloatingFragment getFragment(ArrayMap<String, String> map) {
        FloatingFragment fragment = createFragment(map.get(Globals.Keys.LAYOUT_TAG));
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
            case ScreenRecorderFragment.IDENTIFIER:
                return ScreenRecorderFragment.newInstance();
            default:
                return null;
        }
    }
}
