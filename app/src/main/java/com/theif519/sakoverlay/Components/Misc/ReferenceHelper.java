package com.theif519.sakoverlay.Components.Misc;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.theif519.sakoverlay.Components.BaseComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by theif519 on 1/4/2016.
 *
 * TODO: Need to have a POJO to contain the BaseComponent, and list of Conditionals and Actions rather than a simple map.
 * TODO: This way I can use an Observable easily to map each.
 */
public class ReferenceHelper {
    List<ReferenceType<BaseComponent>> mReferenceList;
    private static final String[] STATEMENTS = { "IF", "ELSE IF", "ELSE"};

    public ReferenceHelper() {
        mReferenceList = new ArrayList<>();
    }

    public ReferenceHelper add(ReferenceType<BaseComponent>... refs){
        Collections.addAll(mReferenceList, refs);
        return this;
    }

    public List<ReferenceType<?>> getAllReferences(){
        return Stream.of(mReferenceList)
                .collect(Collectors.toList());
    }

    public String[] getStatements(){
        return STATEMENTS;
    }

}
