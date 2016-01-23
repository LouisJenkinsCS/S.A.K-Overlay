package com.theif519.sakoverlay.Components.Types;

import com.annimon.stream.Stream;
import com.theif519.sakoverlay.Components.Types.Actions.ActionType;
import com.theif519.sakoverlay.Components.Types.Conditionals.ConditionalType;
import com.theif519.sakoverlay.Components.Types.Wrappers.MethodWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by theif519 on 1/5/2016.
 */
public class ReferenceType<T> {
    private ConditionalType<T> mConditionals;
    private ActionType<T> mActions;
    private List<ReferenceType<?>> mReferences;
    private String mIdentifier;
    private Class<T> mInstanceClass;

    public static <T> ReferenceType<T> from(IReference<T> instance) {
        return new ReferenceType<>(instance, null);
    }

    @SuppressWarnings("unchecked")
    private ReferenceType(IReference<T> ref, List<ReferenceType<?>> references) {
        mIdentifier = ref.getKey();
        mInstanceClass = ref.getType();
        mConditionals = ref.getConditionals() == null ? ConditionalType.<T>empty() : ConditionalType.from((T) ref, ref.getConditionals());
        mActions = ref.getActions() == null ? ActionType.<T>empty() : ActionType.from((T) ref, ref.getActions());
        mReferences = references == null ? new ArrayList<>() : references;
    }

    public ReferenceType<T> addAction(MethodWrapper<T> action) {
        mActions.add(action);
        return this;
    }

    public ReferenceType<T> addCondition(MethodWrapper<T> condition) {
        mConditionals.add(condition);
        return this;
    }

    public ReferenceType<T> addReference(ReferenceType<?> reference) {
        mReferences.add(reference);
        return this;
    }

    public Stream<MethodWrapper<T>> getAllMethods() {
        return Stream.concat(mConditionals.getAllMethods(), mActions.getAllMethods());
    }

    public Stream<Map.Entry<String, MethodWrapper<T>>> getAllMappedMethods(){
        return Stream.concat(mConditionals.getMappedMethods(), mActions.getMappedMethods());
    }

    public Class<T> getInstanceClass(){
        return mInstanceClass;
    }

    public ConditionalType<T> getConditionals() {
        return mConditionals;
    }

    public ActionType<T> getActions() {
        return mActions;
    }

    @SuppressWarnings("unchecked")
    public ReferenceType<T>[] getReferences() {
        return mReferences.toArray(new ReferenceType[mReferences.size()]);
    }

    public String getName() {
        return mIdentifier;
    }

    public ReferenceType<T> setId(String id) {
        mIdentifier = id;
        return this;
    }

    public ReferenceType<T> removeAction(MethodWrapper<T> action) {
        mActions.remove(action);
        return this;
    }

    public ReferenceType<T> removeCondition(MethodWrapper<T> condition) {
        mConditionals.remove(condition);
        return this;
    }

    public ReferenceType<T> removeReference(ReferenceType<?> reference) {
        mReferences.remove(reference);
        return this;
    }
}