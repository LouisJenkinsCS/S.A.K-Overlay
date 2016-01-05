package com.theif519.sakoverlay.Components.Misc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by theif519 on 1/5/2016.
 */
public class ConditionalType<T> {
    List<MethodWrapper<T>> mConditionals;

    public ConditionalType(List<MethodWrapper<T>> conditionals) {
        mConditionals = conditionals == null ? new ArrayList<>() : conditionals;
    }

    public void add(MethodWrapper<T> condition) {
        mConditionals.add(condition);
    }

    public void remove(MethodWrapper<T> condition) {
        if (!mConditionals.remove(condition)) {
            throw new RuntimeException("Attempted to remove a condition which did not exist!");
        }
    }

    public MethodWrapper<T> get(int index) {
        return mConditionals.get(index);
    }

    @SuppressWarnings("unchecked")
    public MethodWrapper<T>[] getAll() {
        return mConditionals.toArray(new MethodWrapper[mConditionals.size()]);
    }
}