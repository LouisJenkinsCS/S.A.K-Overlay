package com.theif519.sakoverlay.Components.Misc;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by theif519 on 1/5/2016.
 */
public class ConditionalType<T> {
    List<MethodWrapper<T>> mConditionals;

    public static <T> ConditionalType<T> empty(){
        return new ConditionalType<>(null);
    }

    public static  <T> ConditionalType<T> from(T instance, Class<? extends Conditionals> clazz) {
        List<MethodWrapper<T>> wrappers = new ArrayList<>();
        List<Method> ignoredMethods = Stream
                .of(Object.class.getMethods())
                .collect(Collectors.toList());
        Stream.of(clazz.getMethods())
                .filter(m -> !ignoredMethods.contains(m))
                .map(m -> new MethodWrapper<>(instance, m))
                .forEach(wrappers::add);
        return new ConditionalType<>(wrappers);
    }

    private ConditionalType(List<MethodWrapper<T>> conditionals) {
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
    public MethodWrapper<T>[] getAllMethods() {
        return mConditionals.toArray(new MethodWrapper[mConditionals.size()]);
    }
}