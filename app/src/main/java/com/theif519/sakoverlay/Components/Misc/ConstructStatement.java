package com.theif519.sakoverlay.Components.Misc;

/**
 * Created by theif519 on 1/8/2016.
 */
public enum ConstructStatement {
    IF, ELSE_IF, ELSE;

    public static final String IF_STRING = "if", ELSE_IF_STRING = "else if", ELSE_STRING = "else";

    @Override
    public String toString() {
        switch(this){
            case IF:
                return IF_STRING;
            case ELSE_IF:
                return ELSE_IF_STRING;
            case ELSE:
                return ELSE_STRING;
            default:
                return null;
        }
    }

    public static ConstructStatement from(String val){
        switch (val){
            case IF_STRING:
                return IF;
            case ELSE_IF_STRING:
                return ELSE_IF;
            case ELSE_STRING:
                return ELSE;
            default:
                return null;
        }
    }
}
