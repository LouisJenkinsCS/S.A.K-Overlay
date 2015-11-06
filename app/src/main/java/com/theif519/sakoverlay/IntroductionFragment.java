package com.theif519.sakoverlay;

import android.util.ArrayMap;

/**
 * Created by theif519 on 10/31/2015.
 */
public class IntroductionFragment extends FloatingFragment {

    public static final String IDENTIFIER = "Introduction";

    public static IntroductionFragment newInstance(){
        IntroductionFragment fragment = new IntroductionFragment();
        fragment.TITLE = "Welcome!";
        fragment.LAYOUT_TAG = IDENTIFIER;
        fragment.LAYOUT_ID = R.layout.introduction;
        return fragment;
    }

    public IntroductionFragment deserialize(ArrayMap<String, String> map){
        IntroductionFragment fragment = IntroductionFragment.newInstance();
        fragment.mappedData = map;
        return fragment;
    }
}
