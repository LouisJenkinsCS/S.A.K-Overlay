package com.theif519.sakoverlay.Components.Types.Actions;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.theif519.sakoverlay.Components.Misc.MethodWrapper;
import com.theif519.sakoverlay.Components.Types.Actions.Impl.Actions;
import com.theif519.sakoverlay.Components.Types.MethodType;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by theif519 on 1/5/2016.
 */
public class ActionType<T> extends MethodType<T> {

    public static <T> ActionType<T> empty(){
        return new ActionType<>(null);
    }

    @SuppressWarnings("unchecked")
    public static <T> ActionType<T> from(T instance, Class<? extends Actions> clazz) {
        List<Method> ignoredMethods = Stream
                .of(Object.class.getMethods())
                .collect(Collectors.toList());
        List<MethodWrapper<T>> wrappers = Stream.of(clazz.getMethods())
                .filter(m -> !ignoredMethods.contains(m))
                .map(m -> new MethodWrapper<>(instance, m))
                .collect(Collectors.toList());
        return new ActionType<T>(wrappers.toArray(new MethodWrapper[wrappers.size()]));
    }

    @SuppressWarnings("unchecked")
    protected ActionType(MethodWrapper<T>... actions) {
        super(actions);
    }
}
