package com.theif519.sakoverlay.Components.Misc;

import java.util.List;

/**
 * Created by theif519 on 1/4/2016.
 *
 * TODO: Need to have a POJO to contain the BaseComponent, and list of Conditionals and Actions rather than a simple map.
 * TODO: This way I can use an Observable easily to map each.
 */
public class ReferenceHelper {
    List<ReferenceType<?>> mReferenceList;

    public ReferenceHelper(List<ReferenceType<?>> references) {
        this.mReferenceList = references;
    }
    
}
