package com.theif519.sakoverlay.FloatingFragments;

import com.theif519.sakoverlay.R;

/**
 * Created by theif519 on 10/31/2015.
 */
public class IntroductionFragment extends FloatingFragment {

    public static final String IDENTIFIER = "Introduction";

    public static IntroductionFragment newInstance(){
        IntroductionFragment fragment = new IntroductionFragment();
        fragment.LAYOUT_TAG = IDENTIFIER;
        fragment.LAYOUT_ID = R.layout.introduction;
        return fragment;
    }

}
