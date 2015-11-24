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

    /**
     * Used to keep track of various and/or generic keys.
     */
    public final class Keys {

        private Keys() {
        }

        /*
                Used to save key-values for certain attributes, either as a form of IPC (Inter-Process Communication,
                think Bundle/Intent) or for serialization/deserialization.
         */
        public static final String X_COORDINATE = "X Coordinate", Y_COORDINATE = "Y Coordinate", MINIMIZED = "Minimized",
                WIDTH = "Width", HEIGHT = "Height", LAYOUT_TAG = "Layout Tag";


        public static final String AUDIO_ENABLED_KEY = "Audio Enabled", FILENAME_KEY = "Filename";

        /*
            RecorderService's key-values to serve as a form of IPC between the service and the FloatingFragment,
            in this case, ScreenRecorderFragment.

            RECORDER_STATE - Passed through an intent in a bundle as a key for the value state.

            RECORDER_STATE_REQUEST - Sent when ScreenRecorderFragment requests the current state of the recorder.

            RECORDER_COMMAND_REQUEST - Requests that a command be executed.

            RECORDER_COMMAND_RESPONSE - Whether or not the command was honored.

            RECORDER_STATE_REQUEST - When the ScreenRecorderFragment attempts to change the state of the RecorderService.

            RECORDER_STATE_RESPONSE - Response from the RecorderService regarding whether or not it obliged.

            RECORDER_STATE_END_SERVICE_KEY - Request to end the service.

            RECORDER_STATE_HAS_ENDED - Broadcast telling that it will be ending.

            RECORDER_COMMAND - Command sent to recorder.
         */
        public static final String RECORDER_STATE = "Recorder State", RECORDER_STATE_REQUEST = "Recorder State Request",
                RECORDER_STATE_RESPONSE = "Recorder State Response", RECORDER_STATE_CHANGE = "Recorder State Change",
                RECORDER_STATE_HAS_ENDED = "Recorder State Has Ended Key", RECORDER_PERMISSIONS_REQUEST = "Recorder Permissions Request",
                RECORDER_PERMISSIONS_RESPONSE = "Recorder Permissions Response", RECORDER_COMMAND_REQUEST = "Recorder Command Request Key",
                RECORDER_COMMAND_RESPONSE = "Recorder Command Response", RECORDER_COMMAND = "Recorder Command",
                RECORDER_COMMAND_EXECUTED = "Recorder Command Executed", RECORDER_ERROR_MESSAGE = "Recorder Error Message";
    }

    public static final MutableObject<Float> SCALE_X = new MutableObject<>(1f), SCALE_Y = new MutableObject<>(1f);

    public static final MutableObject<Integer> MAX_X = new MutableObject<>(0), MAX_Y = new MutableObject<>(0);

    public static final int RECORDER_PERMISSION_RETVAL = 1;

    public static final int RECORDER_NOTIFICATION_ID = 90;

    public static final String RECORDER_FILE_SAVE_PATH = "/sdcard/Recordings/";

}
