package com.theif519.utils.Misc;

/**
 * Created by theif519 on 11/9/2015.
 * <p/>
 * MutableObject is used to represent a final object which can be changed at Runtime. What this means is that
 * it's reference is finalize, while the value it holds can be changed. MutableInteger, MutableDouble, etc, all exist
 * but use API level 21, and I do not want to rely on it in the case I attempt make it usable in lower level devices.
 * <p/>
 * Warning: NOT THREAD SAFE!
 */
public class MutableObject<T> {
    private T value;

    public MutableObject(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }

    public void set(T val) {
        value = val;
    }
}
