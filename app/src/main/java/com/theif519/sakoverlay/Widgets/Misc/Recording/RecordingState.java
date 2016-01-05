package com.theif519.sakoverlay.Widgets.Misc.Recording;

/**
 * Created by theif519 on 12/25/2015.
 */
public enum RecordingState {
    DEAD(1),
    PREPARED(1 << 1),
    STARTED(1 << 2),
    PAUSED(1 << 3),
    STOPPED(1 << 4);

    private int mMask;

    RecordingState(int bitmask) {
        mMask = bitmask;
    }

    /**
     * Very convenient method to get all masks at once, which allows getting all but one or two
     * super easy to do. It loops through each state then bitwise OR's them into one.
     *
     * @return All bitmasks together.
     */
    public static int getAllMask() {
        int totalMask = 0;
        for (RecordingState state : values()) {
            totalMask |= state.getMask();
        }
        return totalMask;
    }

    public int getMask() {
        return mMask;
    }

    @Override
    public String toString() {
        switch (this) {
            case DEAD:
                return "Dead";
            case PREPARED:
                return "Prepared";
            case STARTED:
                return "Recording";
            case PAUSED:
                return "Paused";
            case STOPPED:
                return "Stopped";
            default:
                return null;
        }
    }
}
