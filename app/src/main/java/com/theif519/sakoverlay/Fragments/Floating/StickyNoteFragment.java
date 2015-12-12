package com.theif519.sakoverlay.Fragments.Floating;

import android.widget.EditText;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.theif519.sakoverlay.R;

import java.util.concurrent.TimeUnit;

/**
 * Created by theif519 on 10/31/2015.
 * <p/>
 * Right now Sticky Note is more of a filler. It serializes/saves the user's content, but unless you create
 * more than one of them, they will not be saved, nor can they be saved meaningfully, and since there is a
 * bug right now, the persistence of such notes are volatile. However, as stated before, it is very
 * proof of concept. It is the only FloatingFragment subclass thus far to serialize it's own data, and it
 * also looks pretty. What can I say? It's a demo.
 * <p/>
 * In the future, Sticky Note will become NotePad, a personalized note-tasking assistant. It will be
 * able to do the following...
 * <p/>
 * 1) Open up txt, .md, .json, .log, etc. files. It will not support proprietary formats, like .pdf
 * and .docx. LazyInflater, whenever that gets implemented, may allow other people to create their own,
 * if this app kicks off well enough.
 * <p/>
 * 2) Be able to save any data, and preferably have syntax highlighting. Yup, you guessed it, Notepad will have
 * programming syntax. Who would of thought?
 * <p/>
 * 3) Tabs. The ability to have more than one at any given time, separated in a tab view, or whatever it is called.
 * This is actually very easily doable. I just do not have the time. Finals and all that.
 */
public class StickyNoteFragment extends FloatingFragment {


    protected static final String CONTENTS_KEY = "Contents";
    protected static final String IDENTIFIER = "Sticky Note";

    private String mContents;

    public static StickyNoteFragment newInstance() {
        StickyNoteFragment fragment = new StickyNoteFragment();
        fragment.LAYOUT_ID = R.layout.sticky_note;
        fragment.LAYOUT_TAG = IDENTIFIER;
        fragment.ICON_ID = R.drawable.sticky_note;
        return fragment;
    }

    @Override
    protected void setup() {
        super.setup();
        RxTextView.textChanges((EditText) getContentView().findViewById(R.id.sticky_note_edit_text))
                .throttleLast(1, TimeUnit.SECONDS)
                .subscribe(str -> mContents = str.toString());
    }
}
