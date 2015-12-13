package com.theif519.sakoverlay.Fragments.Floating;

import com.theif519.sakoverlay.R;
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
        FloatingFragment fragment = getFragment(data.getTag());
        if(fragment != null){
            fragment.setUniqueId(data.getId());
            fragment.deserialize(data.getData());
        }
        return fragment;
    }

    /**
     * Create a fragment from layout tag.
     *
     * @param layoutTag Layout Tag as IDENTIFIER.
     * @return FloatingFragment, or null if tag is invalid.
     */
    private static FloatingFragment getFragment(String layoutTag) {
        switch (layoutTag) {
            case StickyNoteFragment.IDENTIFIER:
                return new StickyNoteFragment();
            case GoogleMapsFragment.IDENTIFIER:
                return new GoogleMapsFragment();
            case WebBrowserFragment.IDENTIFIER:
                return new WebBrowserFragment();
            case ScreenRecorderFragment.IDENTIFIER:
                return new ScreenRecorderFragment();
            default:
                return null;
        }
    }

    private static int getFragmentResourceId(String layoutTag){
        switch (layoutTag) {
            case StickyNoteFragment.IDENTIFIER:
                return R.layout.sticky_note;
            case GoogleMapsFragment.IDENTIFIER:
                return R.layout.google_maps;
            case WebBrowserFragment.IDENTIFIER:
                return R.layout.web_browser;
            case ScreenRecorderFragment.IDENTIFIER:
                return R.layout.screen_recorder;
            default:
                return R.layout.default_fragment;
        }
    }

    private static int getFragmentIconResourceId(String layoutTag){
        switch (layoutTag) {
            case StickyNoteFragment.IDENTIFIER:
                return R.drawable.sticky_note;
            case GoogleMapsFragment.IDENTIFIER:
                return R.drawable.maps;
            case WebBrowserFragment.IDENTIFIER:
                return R.drawable.browser;
            case ScreenRecorderFragment.IDENTIFIER:
                return R.drawable.screen_recorder;
            default:
                return R.drawable.close;
        }
    }
}
