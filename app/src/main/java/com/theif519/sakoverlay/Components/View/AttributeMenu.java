package com.theif519.sakoverlay.Components.View;

import android.content.Context;
import android.widget.LinearLayout;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.theif519.sakoverlay.Components.Misc.BaseViewManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by theif519 on 1/17/2016.
 */
public class AttributeMenu extends LinearLayout {

    List<BaseViewManager> mViewManagers = new ArrayList<>();

    public AttributeMenu(Context context) {
        super(context);
        setOrientation(VERTICAL);
    }

    public void add(BaseViewManager manager){
        addView(manager.getView());
        mViewManagers.add(manager);
        manager.observeStateChanges()
                .subscribe(ignoredParam -> manager.reset());
    }

    public void remove(BaseViewManager manager){
        removeView(manager.getView());
        mViewManagers.remove(manager);
    }

    public void removeAll(){
        removeAllViews();
        mViewManagers.clear();
    }

    public void reset(){
        Stream.of(mViewManagers)
                .forEach(BaseViewManager::reset);
    }

    public Optional<String> validate() {
        StringBuilder errMsg = new StringBuilder();
        Stream.of(mViewManagers)
                .map(BaseViewManager::validate)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(err -> errMsg.append(err).append("\n"));
        if(errMsg.toString().isEmpty()){
            return Optional.empty();
        } else {
            return Optional.of(errMsg.toString());
        }
    }

    public void handle(){
        Stream.of(mViewManagers)
                .forEach(BaseViewManager::handle);
    }
}
