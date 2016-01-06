package com.theif519.sakoverlay.Components.Misc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by theif519 on 1/5/2016.
 */
public class ReferenceType<T> {
    private ConditionalType<T> mConditionals;
    private ActionType<T> mActions;
    private List<ReferenceType<?>> mReferences;

    public static <T> ReferenceType<T> from(T instance, Class<? extends Conditionals> conditionalClass, Class<? extends Actions> actionClass){
        return new ReferenceType<>(ConditionalType.from(instance, conditionalClass), ActionType.from(instance, actionClass), null);
    }

    public ReferenceType(ConditionalType<T> conditionals, ActionType<T> actions, List<ReferenceType<?>> references) {
        mConditionals = conditionals == null ? ConditionalType.<T>empty() : conditionals;
        mActions = actions == null ? ActionType.<T>empty() : actions;
        mReferences = references == null ? new ArrayList<>() : references;
    }

    public ReferenceType<T> addAction(MethodWrapper<T> action){
        mActions.add(action);
        return this;
    }

    public ReferenceType<T> addCondition(MethodWrapper<T> condition){
        mConditionals.add(condition);
        return this;
    }

    public ReferenceType<T> addReference(ReferenceType<?> reference){
        mReferences.add(reference);
        return this;
    }

    public ConditionalType<T> getConditionals(){
        return mConditionals;
    }

    public ActionType<T> getActions(){
        return mActions;
    }

    @SuppressWarnings("unchecked")
    public ReferenceType<T>[] getReferences(){
        return mReferences.toArray(new ReferenceType[mReferences.size()]);
    }

    public ReferenceType<T> removeAction(MethodWrapper<T> action){
        mActions.remove(action);
        return this;
    }

    public ReferenceType<T> removeCondition(MethodWrapper<T> condition){
        mConditionals.remove(condition);
        return this;
    }

    public ReferenceType<T> removeReference(ReferenceType<?> reference){
        mReferences.remove(reference);
        return this;
    }
}