Ideas

- Buttons
    + Circular
    + Picture background
            - Meaningful
                + Messenger Icon for Messenger app
                + Phone icon for Phone Dialer
                + etc.
    + Dynamic
        * Long-Pressable
            - Menu
                + Options
                    * Delete
                    * Move
                    * Duplicate
                    * Modify
        * Move-able/Draggable
            - It's position is remembered upon next use of app.
- Scroll-Bar
    + Dynamic
        * Add buttons
            - Decides on it's own where to go.
- Fragment
    + Dynamic
        * Size
            - Should vary
        * Draggable
            - Remembers where user placed it.
        * Long Pressable
            - Menu
                + Options
                    * Close
                    * Move
                    * Resize
        * Resizable
            - Chosen by user.
        * Multiple sublayers
            - More than one fragment
            - Allows multiple activities.
- Uses
    + Simple Calculator
    + Note pad
        * Remembers what was written
    + Messenger
        * Support SMS/MMS
            - MMS media save-able
        * Forwards messages to the system messenger
    + Phone Dialer
        * Shows contacts
        * Forwards calls to system dialer
    + Google Maps
        * Current location
            - If GPS is on
        * Allow user to set a location
            - Saved on next use
                + If API permits it
    + Browser
        * Embedded WebView
            - Floating if opened from a link
- Menu
    + Use PopupMenu
        * Custom Theme
    + Appears
        * On Long-Press
            - Appears above view
- Misc.
    + Extends or implement a custom view
        * All widgets should implement/extend from
        * Implements a LongPress option
            - Creates PopupMenu
                + Options declared in an abstract method
    + Processing on a worker thread
        * Create a custom Handler
            - Maybe a custom looper
            - Message Queue
                + Fun experience