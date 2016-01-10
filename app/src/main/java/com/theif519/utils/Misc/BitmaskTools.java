package com.theif519.utils.Misc;

/**
 * Created by theif519 on 1/10/2016.
 */
public class BitmaskTools {
    public static boolean isSet(int mask, int bit){
        return (mask & bit) != 0;
    }
    public static int set(int mask, int bit){
        return mask |= bit;
    }
}
