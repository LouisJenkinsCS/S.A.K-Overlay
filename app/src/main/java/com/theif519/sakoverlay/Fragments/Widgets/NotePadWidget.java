package com.theif519.sakoverlay.Fragments.Widgets;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

import com.theif519.sakoverlay.R;
import com.theif519.sakoverlay.Rx.Transformers;
import com.theif519.sakoverlay.Sessions.Widgets.WidgetSessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import rx.subjects.PublishSubject;

/**
 * Created by theif519 on 10/31/2015.
 * <p>
 * Right now Sticky Note is more of a filler. It serializes/saves the user's content, but unless you create
 * more than one of them, they will not be saved, nor can they be saved meaningfully, and since there is a
 * bug right now, the persistence of such notes are volatile. However, as stated before, it is very
 * proof of concept. It is the only BaseWidget subclass thus far to serialize it's own data, and it
 * also looks pretty. What can I say? It's a demo.
 * <p>
 * In the future, Sticky Note will become NotePad, a personalized note-tasking assistant. It will be
 * able to do the following...
 * <p>
 * 1) Open up txt, .md, .json, .log, etc. files. It will not support proprietary formats, like .pdf
 * and .docx. LazyInflater, whenever that gets implemented, may allow other people to create their own,
 * if this app kicks off well enough.
 * <p>
 * 2) Be able to save any data, and preferably have syntax highlighting. Yup, you guessed it, Notepad will have
 * programming syntax. Who would of thought?
 * <p>
 * 3) Tabs. The ability to have more than one at any given time, separated in a tab view, or whatever it is called.
 * This is actually very easily doable. I just do not have the time. Finals and all that.
 */
public class NotePadWidget extends BaseWidget {

    public NotePadWidget() {
        mLayoutId = R.layout.sticky_note;
        mIconId = R.drawable.sticky_note;
        LAYOUT_TAG = IDENTIFIER;
    }

    protected static final String CONTENTS_KEY = "Contents";
    protected static final String IDENTIFIER = "Sticky Note";

    private String mContents;
    private PublishSubject<String> mTextChangeEvent = PublishSubject.create();

    @Override
    protected JSONObject pack() {
        try {
            return super.pack()
                    .put(CONTENTS_KEY, mContents);
        } catch (JSONException e) {
            Log.w(getClass().getName(), e.getMessage());
            return super.pack();
        }
    }

    @Override
    protected void unpack(JSONObject obj) {
        super.unpack(obj);
        try {
            mContents = obj.getString(CONTENTS_KEY);
        } catch (JSONException e) {
            Log.w(getClass().getName(), e.getMessage());
        }
    }

    @Override
    protected void setup() {
        super.setup();
        EditText editText = (EditText) getContentView().findViewById(R.id.sticky_note_edit_text);
        if (mContents != null) {
            editText.setText(mContents);
        }
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mTextChangeEvent.onNext(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mTextChangeEvent
                .asObservable()
                .compose(Transformers.backgroundIO())
                .throttleWithTimeout(1, TimeUnit.SECONDS)
                .subscribe(str -> {
                    mContents = str;
                    WidgetSessionManager
                            .getInstance()
                            .updateSession(this);
                });
    }
}