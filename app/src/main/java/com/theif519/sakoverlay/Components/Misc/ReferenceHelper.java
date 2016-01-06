package com.theif519.sakoverlay.Components.Misc;

import com.theif519.sakoverlay.Components.BaseComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by theif519 on 1/4/2016.
 *
 * TODO: Need to have a POJO to contain the BaseComponent, and list of Conditionals and Actions rather than a simple map.
 * TODO: This way I can use an Observable easily to map each.
 */
public class ReferenceHelper {
    List<ReferenceType<BaseComponent>> mReferenceList;

    public ReferenceHelper() {
        mReferenceList = new ArrayList<>();
    }

    public ReferenceHelper add(ReferenceType<BaseComponent>... refs){
        for(ReferenceType<BaseComponent> ref: refs){
            mReferenceList.add(ref);
        }
        return this;
    }

}
