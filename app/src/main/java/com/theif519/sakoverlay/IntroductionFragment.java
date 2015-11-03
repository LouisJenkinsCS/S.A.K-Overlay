package com.theif519.sakoverlay;

import android.os.Bundle;
import android.util.ArrayMap;

/**
 * Created by theif519 on 10/31/2015.
 */
public class IntroductionFragment extends FloatingFragment {

    public static final String LAYOUT_TAG = "Introduction";
    private static final String TITLE = "Welcome";

    public static IntroductionFragment newInstance(){
        IntroductionFragment fragment = new IntroductionFragment();
        Bundle args = new Bundle();
        args.putString(TITLE_KEY, TITLE);
        args.putInt(LAYOUT_ID_KEY, R.layout.introduction);
        args.putString(LAYOUT_TAG_KEY, LAYOUT_TAG);
        fragment.setArguments(args);
        return fragment;
    }

    public IntroductionFragment deserialize(ArrayMap<String, String> map){
        IntroductionFragment fragment = IntroductionFragment.newInstance();
        fragment.mappedData = map;
        return fragment;
    }
}
