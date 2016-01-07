package com.theif519.sakoverlay.Components.Misc;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by theif519 on 1/5/2016.
 */
public class ActionType<T> {
    List<MethodWrapper<T>> mActions;

    public static <T> ActionType<T> empty(){
        return new ActionType<>(null);
    }

    public static <T> ActionType<T> from(T instance, Class<? extends Actions> clazz) {
        List<MethodWrapper<T>> wrappers = new ArrayList<>();
        List<Method> ignoredMethods = Stream
                .of(Object.class.getMethods())
                .collect(Collectors.toList());
        Stream.of(clazz.getMethods())
                .filter(m -> !ignoredMethods.contains(m))
                .map(m -> new MethodWrapper<>(instance, m))
                .forEach(wrappers::add);
        return new ActionType<>(wrappers);
    }


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
    public MethodWrapper<T>[] getAllMethods() {
        return mActions.toArray(new MethodWrapper[mActions.size()]);
    }
}
