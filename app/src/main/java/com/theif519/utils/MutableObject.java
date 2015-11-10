package com.theif519.utils;

/**
 * Created by theif519 on 11/9/2015.
 *
 * Warning: NOT THREAD SAFE!
 */
public class MutableObject<T> {
    public T value;

    public MutableObject(T value) {
        this.value = value;
    }
}
