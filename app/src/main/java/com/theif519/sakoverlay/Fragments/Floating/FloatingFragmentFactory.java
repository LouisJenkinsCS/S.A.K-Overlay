package com.theif519.sakoverlay.Fragments.Floating;

import android.util.ArrayMap;

import com.theif519.sakoverlay.Misc.Globals;

/**
 * Created by theif519 on 10/31/2015.
 * <p/>
 * A simple factory for restoring FloatingFragments from their serialized state. It works by passing in
 * the array map, where it will obtain the LAYOUT_TAG from the map, and depending on what it is, it inflates
 * it and passes the map to it for it handle setting itself up.
 */
public class FloatingFragmentFactory {
    // Singleton
    private static final FloatingFragmentFactory INSTANCE = new FloatingFragmentFactory();

    public static FloatingFragmentFactory getInstance() {
        return INSTANCE;
    }

    /**
     * Obtain a FloatingFragment from a passed serialized attribute map.
     *
     * @param map Map filled with Attributes.
     * @return The fragment, or null if not found.
     */
    public FloatingFragment getFragment(ArrayMap<String, String> map) {
        FloatingFragment fragment = createFragment(map.get(Globals.Keys.LAYOUT_TAG));
        if (fragment == null) return null;
        fragment.mMappedContext = map;
        return fragment;
    }

    /**
     * Create a fragment from layout tag.
     *
     * @param layoutTag Layout Tag as IDENTIFIER.
     * @return FloatingFragment, or null if tag is invalid.
     */
    private FloatingFragment createFragment(String layoutTag) {
        switch (layoutTag) {
            case StickyNoteFragment.IDENTIFIER:
                return StickyNoteFragment.newInstance();
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
