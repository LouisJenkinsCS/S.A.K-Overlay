package com.theif519.sakoverlay;

import android.util.ArrayMap;
import android.widget.EditText;

/**
 * Created by theif519 on 10/31/2015.
 */
public class StickyNoteFragment extends FloatingFragment {


    protected static final String CONTENTS_KEY = "Contents";
    public static final String IDENTIFIER = "Sticky Note";

    public static StickyNoteFragment newInstance(){
        StickyNoteFragment fragment = new StickyNoteFragment();
        fragment.LAYOUT_ID = R.layout.sticky_note;
        fragment.LAYOUT_TAG = IDENTIFIER;
        fragment.ICON_ID = R.drawable.sticky_note;
        return fragment;
    }


    @Override
    public ArrayMap<String, String> serialize() {
        ArrayMap<String, String> map = super.serialize();
        map.put(CONTENTS_KEY, ((EditText) getContentView().findViewById(R.id.sticky_note_edit_text)).getText().toString());
        return map;
    }

    @Override
    public void unpack() {
        super.unpack();
        ((EditText) getContentView().findViewById(R.id.sticky_note_edit_text)).setText(mappedData.get(CONTENTS_KEY));
    }
}
