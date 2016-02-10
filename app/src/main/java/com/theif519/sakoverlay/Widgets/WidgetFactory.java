package com.theif519.sakoverlay.Widgets;

import com.annimon.stream.Optional;
import com.theif519.sakoverlay.Widgets.POJO.WidgetSessionData;

/**
 * Created by theif519 on 10/31/2015.
 * <p/>
 * A simple factory for restoring FloatingFragments from their serialized state. It works by passing in
 * the array map, where it will obtain the LAYOUT_TAG from the map, and depending on what it is, it inflates
 * it and passes the map to it for it handle setting itself up.
 */
public final class WidgetFactory {

    /**
     * Deserialize a Widget based on the passed serialized information.
     *
     * @param data Data to recreate Widget
     * @return The Widget, or null if not found.
     */
    public static Optional<BaseWidget> getWidget(WidgetSessionData data) {
        BaseWidget fragment = createWidget(data.getTag());
        if(fragment != null){
            fragment.setUniqueId(data.getId());
            fragment.deserialize(data.getData());
        }
        return Optional.ofNullable(fragment);
    }

    /**
     * Create a fragment from layout tag.
     *
     * @param layoutTag Layout Tag as IDENTIFIER.
     * @return BaseWidget, or null if tag is invalid.
     */
    private static BaseWidget createWidget(String layoutTag) {
        switch (layoutTag) {
            case NotePadWidget.IDENTIFIER:
                return new NotePadWidget();
            case GoogleMapsWidget.IDENTIFIER:
                return new GoogleMapsWidget();
            case WebBrowserWidget.IDENTIFIER:
                return new WebBrowserWidget();
            case ScreenRecorderWidget.IDENTIFIER:
                return new ScreenRecorderWidget();
            default:
                return null;
        }
    }
}
