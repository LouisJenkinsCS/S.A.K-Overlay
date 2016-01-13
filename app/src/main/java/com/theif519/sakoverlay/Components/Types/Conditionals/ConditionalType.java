package com.theif519.sakoverlay.Components.Types.Conditionals;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.theif519.sakoverlay.Components.Types.Wrappers.MethodWrapper;
import com.theif519.sakoverlay.Components.Types.Conditionals.Impl.Conditionals;
import com.theif519.sakoverlay.Components.Types.MethodType;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by theif519 on 1/5/2016.
 */
public class ConditionalType<T> extends MethodType<T> {

    public static <T> ConditionalType<T> empty(){
        return new ConditionalType<>(null);
    }

    @SuppressWarnings("unchecked")
    public static  <T> ConditionalType<T> from(T instance, Class<? extends Conditionals> clazz) {
        List<Method> ignoredMethods = Stream
                .of(Object.class.getMethods())
                .collect(Collectors.toList());
        List<MethodWrapper<T>> wrappers = Stream.of(clazz.getMethods())
                .filter(m -> !ignoredMethods.contains(m))
                .map(m -> new MethodWrapper<>(instance, m))
                .collect(Collectors.toList());
        return new ConditionalType<T>(wrappers.toArray(new MethodWrapper[wrappers.size()]));
    }

    @SuppressWarnings("unchecked")
    protected ConditionalType(MethodWrapper<T>... conditionals) {
        super(conditionals);
    }
}