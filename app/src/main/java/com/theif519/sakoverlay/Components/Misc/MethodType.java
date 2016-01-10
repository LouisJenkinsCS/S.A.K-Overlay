package com.theif519.sakoverlay.Components.Misc;

import android.support.annotation.NonNull;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import java.util.Map;

/**
 * Created by theif519 on 1/10/2016.
 */
public class MethodType<T> {
    Map<String, MethodWrapper<T>> mMethods;

    protected MethodType(MethodWrapper<T>... methods) {
        mMethods = Stream.of(methods)
                .collect(Collectors.toMap(MethodWrapper::getMethodName, wrapper -> wrapper));
    }

    public boolean contains(@NonNull MethodWrapper<T> wrapper){
        return mMethods.containsValue(wrapper);
    }

    public boolean contains(@NonNull String methodName){
        return mMethods.containsKey(methodName);
    }

    public void add(@NonNull MethodWrapper<T> condition) {
        mMethods.put(condition.getMethodName(), condition);
    }

    public void remove(@NonNull MethodWrapper<T> condition) {
        if (mMethods.remove(condition.getMethodName()) == null) {
            throw new RuntimeException("Attempted to remove a method which did not exist!");
        }
    }

    public void remove(@NonNull String methodName){
        if(mMethods.remove(methodName) == null){
            throw new RuntimeException("Attempted to remove a method with the name: \"" + methodName + "\" which did not exist!");
        }
    }

    public Stream<MethodWrapper<T>> getAllMethods() {
        return Stream.of(mMethods.values());
    }

    public Stream<Map.Entry<String, MethodWrapper<T>>> getMappedMethods(){
        return Stream.of(mMethods);
    }
}
