package com.theif519.sakoverlay.Misc;

import com.theif519.utils.Misc.MutableObject;

/**
 * Created by theif519 on 11/13/2015.
 * <p/>
 * This file, inspired by that guy who went last on Thursday of the first week (forgot his name, but not his work), is used
 * to keep track of all global constants which are generic enough for other classes to reuse. It is used
 * to keep an organized track of all variables which are considered global, and/or reusable enough that
 * other classes may also utilize them. Not all constants go here, certain ones (I.E FloatingFragment and it's
 * subclasses IDENTIFIER variable) would create conflicts if they were all accessed here.
 * <p/>
 * Globals are organized, insofar, by Keys (for key-value pairs) and miscallaneous others. Keys, as of yet,
 * are split between normal Keys, and Options, both explained below.
 */
public final class Globals {

    public static final MutableObject<Float> SCALE = new MutableObject<>(1f);
    public static final MutableObject<Integer> MAX_X = new MutableObject<>(0), MAX_Y = new MutableObject<>(0);
    public static final String JSON_FILENAME = "SerializedFloatingFragments.json";
    public static final int RECORDER_PERMISSION_RETVAL = 1;
    public static final int OVERLAY_NOTIFICATION_ID = 90;
    public static final int RECORDER_NOTIFICATION_ID = 91;
    public static final String RECORDER_FILE_SAVE_PATH = "/sdcard/Recordings/";

    /**
     * Used to keep track of various and/or generic keys.
     */
    public final class Keys {

        /*
                Used to save key-values for certain attributes, either as a form of IPC (Inter-Process Communication,
                think Bundle/Intent) or for serialization/deserialization.
         */
        public static final String X_COORDINATE = "X Coordinate", Y_COORDINATE = "Y Coordinate", Z_COORDINATE = "Z Coordinate",
                MINIMIZED = "Minimized", WIDTH = "Width", HEIGHT = "Height", LAYOUT_TAG = "Layout Tag", SNAP_MASK = "Snap Mask",
                MAXIMIZED = "Maximized";
        public static final String RECORDER_COMMAND_REQUEST = "Recorder Command Request Key", RECORDER_COMMAND = "Recorder Command";

        private Keys() {
        }
    }

}
