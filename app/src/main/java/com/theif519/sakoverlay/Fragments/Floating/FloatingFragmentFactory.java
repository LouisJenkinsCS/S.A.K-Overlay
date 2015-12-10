package com.theif519.sakoverlay.Fragments.Floating;

import com.google.gson.Gson;
import com.theif519.sakoverlay.Sessions.WidgetSessionData;

/**
 * Created by theif519 on 10/31/2015.
 * <p/>
 * A simple factory for restoring FloatingFragments from their serialized state. It works by passing in
 * the array map, where it will obtain the LAYOUT_TAG from the map, and depending on what it is, it inflates
 * it and passes the map to it for it handle setting itself up.
 */
public final class FloatingFragmentFactory {

    /**
     * Deserialize a Widget based on the passed serialized information.
     *
     * @param data Data to recreate Widget
     * @return The Widget, or null if not found.
     */
    public static FloatingFragment getFragment(WidgetSessionData data) {
        Class<? extends FloatingFragment> clazz = getFragmentClass(data.getTag());
        if (clazz == null) return null;
        return new Gson().fromJson(new String(data.getData()), clazz);
    }

    /**
     * Create a fragment from layout tag.
     *
     * @param layoutTag Layout Tag as IDENTIFIER.
     * @return FloatingFragment, or null if tag is invalid.
     */
    private static Class<? extends FloatingFragment> getFragmentClass(String layoutTag) {
        switch (layoutTag) {
            case StickyNoteFragment.IDENTIFIER:
                return StickyNoteFragment.class;
            case GoogleMapsFragment.IDENTIFIER:
                return GoogleMapsFragment.class;
            case WebBrowserFragment.IDENTIFIER:
                return WebBrowserFragment.class;
            case ScreenRecorderFragment.IDENTIFIER:
                return ScreenRecorderFragment.class;
            default:
                return null;
        }
    }
}
