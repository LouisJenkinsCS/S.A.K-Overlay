package com.example.theif519.saklauncher;

import android.os.Bundle;
import android.util.ArrayMap;
import android.widget.EditText;

/**
 * Created by theif519 on 10/31/2015.
 */
public class StickyNoteFragment extends FloatingFragment {

    protected static final String CONTENTS_KEY = "Contents";
    public static final String LAYOUT_TAG = "Sticky Note";

    public static StickyNoteFragment newInstance(String title){
        StickyNoteFragment fragment = new StickyNoteFragment();
        Bundle args = new Bundle();
        args.putString(TITLE_KEY, title);
        args.putInt(LAYOUT_ID_KEY, R.layout.sticky_note);
        args.putString(LAYOUT_TAG_KEY, LAYOUT_TAG);
        fragment.setArguments(args);
        return fragment;
    }

    public static StickyNoteFragment deserialize(ArrayMap<String, String> map){
        StickyNoteFragment fragment = StickyNoteFragment.newInstance("Sticky Note");
        fragment.mappedData = map;
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
