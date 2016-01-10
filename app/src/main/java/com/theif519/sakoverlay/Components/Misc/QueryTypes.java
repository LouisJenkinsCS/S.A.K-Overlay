package com.theif519.sakoverlay.Components.Misc;

/**
 * Created by theif519 on 1/10/2016.
 */
public class QueryTypes {
    public static final int REFERENCES = 1;
    public static final int CONDITIONALS = 1 << 1;
    public static final int ACTIONS = 1 << 2;
    public static final int STATEMENTS = 1 << 3;
    public static final int STATEMENTS_IF = 1 << 4;
    public static final int STATEMENTS_ELSE_IF = 1 << 5;
    public static final int STATEMENTS_ELSE = 1 << 6;

    private QueryTypes(){}
}
