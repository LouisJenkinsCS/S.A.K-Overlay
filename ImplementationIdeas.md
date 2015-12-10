S.A.K-Overlay Ideas
1. Switch to View.animate().translateX/Y for move()
2. Switch bounds checking to ACTION_UP only
3. Create a Session object and a Session Manager handler thread which will keep track of all information needed, and also handle serialization and deserialization.
4. Make ViewProperties callable from another thread by checking for myLooper() == getMainLooper, and if not UI thread post to either activity or view's handler.
5. Implement MenuOptions in the InfoBar, have the current option be based on the current selected Widget.
6. Add animations for Snap and Shake (Snap mimics AeroSnap's animation, Shake as well)
7. Reimplement Screen Recorder; Change MediaRecorder to MediaCodec; Add Pause feature; Make the controller look and design better; Implement Buffering and Streaming
8. Add a Development section for when things screw up so badly they are no longer usable.
9. Implement LazyInflater



SQLite to handle Widget serialization
- Since SQLite can easily handle more than one type, automatically converting between each, that fact by itself would make it immensely better and dynamic than anything else.
    + SQLite allows you to store objects as BLOBs, hence anything too complicated to turn into a String, Float, or Integer, can be serialized directly as such.
- I can utilize GSON to handle serializing inner classes and references to said classes. Including ViewProperties, which is pretty amazing, all i'd have to do is call update() and it would take care of it all for me. Super!
    + Hence, I can actually store each object directly by LayoutTag as the primary key, and the actual data separately (as they need to be casted to the same class to work appropriately).
- Since SQLite allows you to update individual rows, it will make it easy to delete and update existing Widgets, as well as create new ones.
- SessionManager
    + Checks SQLite database for any existing session data
        * If there is some, it will grab the JSON data and LayoutTag from the SQLite database
        * Have GSON reconstruct that data into original object
        * Pass said data to UI thread handler to be reconstructed
    + When new widget created, UI thread can post necessary data to SessionManager to create a new table
    + After each ACTION_UP event finished, it can make post to SessionManager to update database.
        * Since it's all local, there wouldn't be an actual problem.
        * Due to this, the UI thread can remain 100% responsive and the SessionManager can maintain 100% accuracy of the current session
            - Also delegates all serialization from the UI thread, not just file write.
- Each Widget subclass can create it's own custom Deserializer which extends the base Deserializer, hence it would be more efficient overall as well.
    + Same for Serializer as well
    + Gets rid of Factory Method