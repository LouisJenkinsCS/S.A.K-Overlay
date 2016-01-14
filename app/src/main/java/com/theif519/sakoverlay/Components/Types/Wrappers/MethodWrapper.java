package com.theif519.sakoverlay.Components.Types.Wrappers;

import android.util.Log;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.theif519.sakoverlay.Core.Rx.Transformers;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;

/**
 * Created by theif519 on 1/5/2016.
 */
public class MethodWrapper<T> {

    @Retention(RetentionPolicy.RUNTIME)
    public @interface MethodDescriptions {
        String methodDescription() default "";

        String[] parameterNames() default {};

        String[] parameterDescriptions() default {};

        String returnDescription() default "";
    }

    private T mInstance;
    private Method mMethod;
    private String mMethodDescription, mMethodDeclaration;
    private List<ParameterWrapper<?>> mParameters = new ArrayList<>();
    private ReturnWrapper<?> mReturn;

    public MethodWrapper(T instance, Method method) {
        mMethod = method;
        mInstance = instance;
        Observable.just(method)
                .compose(Transformers.backgroundIO())
                .doOnNext(this::parseAnnotations)
                .map(this::parseDeclaration)
                .subscribe(declaration -> {
                    mMethodDeclaration = declaration;
                    StringBuilder logMsg = new StringBuilder(String.format("Parsed: { Return: %s; Name: %s; Parameters[]: { ", mReturn, mMethod.getName()));
                    Stream.of(mParameters)
                            .map(parameter -> "\"" + parameter + "\" ")
                            .forEach(logMsg::append);
                    logMsg.append("}; }");
                    Log.i(getClass().getName(), logMsg.toString());
                });
    }

    public Object invoke(Object... params) {
        try {
            return mMethod.invoke(mInstance, params);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Error while attempting to invoke method: \"" + e.getMessage() + "\"");
        }
    }

    private void parseAnnotations(Method method) {
        MethodDescriptions methodDescriptions = method.getAnnotation(MethodDescriptions.class);
        if (methodDescriptions != null) {
            mMethodDescription = methodDescriptions.methodDescription();
            if(mMethodDescription.isEmpty()){
                mMethodDescription = null;
            }
            String[] parameterDescriptions = methodDescriptions.parameterDescriptions();
            String[] parameterNames = methodDescriptions.parameterNames();
            Class<?>[] parameterTypes = method.getParameterTypes();
            Class<?> returnType = method.getReturnType();
            for (int i = 0; i < parameterTypes.length; i++) {
                mParameters.add(new ParameterWrapper<>(
                        i < parameterNames.length ? parameterNames[i] : null,
                        i < parameterDescriptions.length ? parameterDescriptions[i] : null,
                        parameterTypes[i]
                ));
            }
            mReturn = new ReturnWrapper<>(methodDescriptions.returnDescription(), returnType);
        } else {
            Stream.of(method.getParameterTypes())
                    .map(ParameterWrapper::raw)
                    .forEach(mParameters::add);
            mReturn = ReturnWrapper.raw(method.getReturnType());
        }
    }

    private String parseDeclaration(Method method) {
        String declaration = String.format("%s %s(", mReturn, mMethod.getName());
        boolean preceded = false;
        for (ParameterWrapper<?> wrapper : mParameters) {
            if (preceded) declaration += ", ";
            declaration += wrapper;
            preceded = true;
        }
        declaration += ")";
        return declaration;
    }

    public int getParameterCount(){
        return mParameters.size();
    }

    public Stream<ParameterWrapper<?>> getParameters() {
        return Stream.of(mParameters);
    }

    public ReturnWrapper<?> getReturn() {
        return mReturn;
    }

    public String getDeclaration() {
        return mMethodDeclaration;
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(mMethodDescription);
    }

    public String getMethodName() {
        return mMethod.getName();
    }
}