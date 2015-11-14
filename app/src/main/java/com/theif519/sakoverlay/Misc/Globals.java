package com.theif519.sakoverlay.Misc;

/**
 * Created by theif519 on 11/13/2015.
 * <p/>
 * This file, inspired by that guy who went last on Thursday (forgot his name, but not his work), is used
 * to keep track of all global constants which are generic enough for other classes to reuse. It should be noted,
 * once again, only ones which are GENERIC are reused. Hence, ones which each class has a specificified value,
 * even if they have the same name (I.E FloatingFragment subclasses IDENTIFIER constant is different for each subclass).
 * <p/>
 * Global has two roots in the hierarchy, "Mutable" and "Immutable".
 * <p/>
 * Global.Mutable contains globals which can be changed, while Global.Immutable cannot be changed after their declaration.
 * Both are thread-safe.
 * <p/>
 * The overall layout looks like this
 * Globals
 * |->  Immutable
 * |-> Numbers
 * |-> Enumerations
 * |-> Strings
 * |->  Mutable
 * |-> Numbers
 * |-> Enumerations
 * |-> Strings
 * <p/>
 * Hence, while accessing these globals may feel arduous, especially if you do not include the entire package, I.E
 * <p/>
 * com.theif519.sakoverlay.misc.Globals.Immutable.Enumerations.RecorderState
 * <p/>
 * Now that, is a mouthful. However, of course, this can be remedied by importing the Globals package,
 * reducing it to...
 * <p/>
 * Globals.Immutable.Enumerations.RecorderState
 * <p/>
 * Long-Winded, however, it will allow me to add as many constants as I want and allow it to be easily
 * managed in the long-run.
 */
public final class Globals {

    /**
     * In the conventional sense of the word, a Constant is Immutable by default. In fact, one could argue
     * that a mutable constant isn't really a constant. Forget those people. Constants, in this use-case,
     * are used to keep track of global and hence thread-safe primitives and/or objects which can be used
     * reliably without side-effects. These are immutable.
     */
    public final class Immutable {

        private Immutable() {
        }

        public final class Strings {

            private Strings() {
            }

            ////////////////////////////////////////////////////////////////////////////////////////////////
            //                                                                                            //
            //                                          Keys                                              //
            //                                                                                            //
            ////////////////////////////////////////////////////////////////////////////////////////////////

            /*
                Keys used to save key-values for certain attributes, either as a form of IPC (Inter-Process Communication,
                think Bundle/Intent) or for serialization/deserialization.
             */
            public static final String X_KEY = "X Coordinate", Y_KEY = "Y Coordinate", MINIMIZED_KEY = "Minimized",
                    WIDTH_KEY = "Width", HEIGHT_KEY = "Height", LAYOUT_TAG_KEY = "Layout Tag", AUDIO_ENABLED_KEY = "Audio Enabled",
                    FILENAME_KEY = "Filename";

            /*
                Keys used to identify certain options which can toggle/alter functionality of a view and/or fragment.
             */
            public static final String TRANSPARENCY_TOGGLE_OPTION = "Transparency Toggle", BRING_TO_FRONT_OPTION = "Bring to Front";
        }

        public final class Enumerations {

            private Enumerations() {
            }

        }

        public final class Numbers {

            private Numbers() {
            }

            public static final int RECORDER_PERMISSION_RETVAL = 1;
        }

    }

    /**
     * Converse to the conventional sense of the word, these Constants aren't actually constant, they are just
     * global declarations of primitives and/or reference objects. They are mutable, however either they are made
     * thread-safe either through synchronicity, atomicity, or are naturally only accessed from the UI Thread, hence
     * thread-safety not being an issue.
     */
    public final class Mutable {

        private Mutable() {
        }

        public final class Strings {

            private Strings() {
            }

        }

        public final class Enumerations {

            private Enumerations() {
            }

        }

        public final class Numbers {

            private Numbers() {
            }

        }

    }


}
