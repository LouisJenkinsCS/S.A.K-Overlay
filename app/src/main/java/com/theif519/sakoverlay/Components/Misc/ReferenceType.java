package com.theif519.sakoverlay.Components.Misc;

import java.util.List;

/**
 * Created by theif519 on 1/5/2016.
 */
public class ReferenceType<T> {
    ConditionalType<T> mConditionals;
    ActionType<T> mActions;
    List<ReferenceType<?>> mReferences;

    public ReferenceType(ConditionalType<T> conditionals, ActionType<T> actions, List<ReferenceType<?>> references) {
        mConditionals = conditionals;
        mActions = actions;
        mReferences = references;
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