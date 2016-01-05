package com.theif519.sakoverlay.Components.Misc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by theif519 on 1/5/2016.
 */
public class ActionType<T> {
    List<MethodWrapper<T>> mActions;

    public ActionType(List<MethodWrapper<T>> actions) {
        mActions = actions == null ? new ArrayList<>() : actions;
    }

    public void add(MethodWrapper<T> condition) {
        mActions.add(condition);
    }

    public void remove(MethodWrapper<T> condition) {
        if (!mActions.remove(condition)) {
            throw new RuntimeException("Attempted to remove a condition which did not exist!");
        }
    }

    public MethodWrapper<T> get(int index) {
        return mActions.get(index);
    }

    @SuppressWarnings("unchecked")
    public MethodWrapper<T>[] getAll() {
        return mActions.toArray(new MethodWrapper[mActions.size()]);
    }
}
