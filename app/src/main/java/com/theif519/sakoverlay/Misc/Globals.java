package com.theif519.sakoverlay.Misc;

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

    public final class Keys {

        private Keys() {
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////
        //                                                                                            //
        //                                          Keys                                              //
        //                                                                                            //
        ////////////////////////////////////////////////////////////////////////////////////////////////

        /*
                Used to save key-values for certain attributes, either as a form of IPC (Inter-Process Communication,
                think Bundle/Intent) or for serialization/deserialization.
         */
        public static final String X_KEY = "X Coordinate", Y_KEY = "Y Coordinate", MINIMIZED_KEY = "Minimized",
                WIDTH_KEY = "Width", HEIGHT_KEY = "Height", LAYOUT_TAG_KEY = "Layout Tag";


        public static final String AUDIO_ENABLED_KEY = "Audio Enabled", FILENAME_KEY = "Filename";

        /*
            RecorderService's key-values to serve as a form of IPC between the service and the FloatingFragment,
            in this case, ScreenRecorderFragment.

            RECORDER_STATE_KEY - Passed through an intent in a bundle as a key for the value state.

            RECORDER_STATE_REQUEST_KEY - Sent when ScreenRecorderFragment requests the current state of the recorder.

            RECORDER_STATE_RESPONSE_KEY - Response from the RecorderService to broadcast the state to any listeners.

            RECORDER_STATE_CHANGE_KEY - Broadcasted when the state of the RecorderService changes, to all listeners.

            RECORDER_STATE_REQUEST_KEY - When the ScreenRecorderFragment attempts to change the state of the RecorderService.

            RECORDER_STATE_RESPONSE_KEY - Response from the RecorderService regarding whether or not it obliged.

            RECORDER_STATE_END_SERVICE_KEY - Request to end the service.

            RECORDER_STATE_HAS_ENDED_KEY - Broadcast telling that it will be ending.
         */
        public static final String RECORDER_STATE_KEY = "Recorder State",RECORDER_STATE_REQUEST_KEY = "Recorder State Request",
                RECORDER_STATE_RESPONSE_KEY = "Recorder State Response", RECORDER_STATE_CHANGE_KEY = "Recorder State Change",
                RECORDER_STATE_CHANGE_REQUEST_KEY = "Recorder State Change Request", RECORDER_STATE_CHANGE_RESPONSE_KEY = "Recorder State Change Response",
                RECORDER_STATE_END_SERVICE_KEY = "Recorder State End Service", RECORDER_STATE_HAS_ENDED_KEY = "Recorder State Has Ended Key",
                RECORDER_PERMISSIONS_REQUEST_KEY = "Recorder Permissions Request", RECORDER_PERMISSIONS_RESPONSE_KEY = "Record Permissions Response";

        ////////////////////////////////////////////////////////////////////////////////////////////////
        //                                                                                            //
        //                                          Options                                           //
        //                                                                                            //
        ////////////////////////////////////////////////////////////////////////////////////////////////

        /*
            Base Floating-Fragment options.
         */
        public static final String TRANSPARENCY_TOGGLE_OPTION = "Transparency Toggle", BRING_TO_FRONT_OPTION = "Bring to Front";

        /*
            WebBrowserFragment options.
         */
        public static final String HOME_OPTION = "Home", REFRESH_OPTION = "Refresh";
    }

    public static final int RECORDER_PERMISSION_RETVAL = 1;

    public static final int RECORDER_NOTIFICATION_ID = 90;

}
