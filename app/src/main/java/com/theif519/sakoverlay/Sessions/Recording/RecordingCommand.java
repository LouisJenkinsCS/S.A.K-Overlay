package com.theif519.sakoverlay.Sessions.Recording;

/**
 * Created by theif519 on 12/25/2015.
 */
public enum RecordingCommand {
    PREPARE(
            RecordingState.DEAD.getMask() | RecordingState.STOPPED.getMask()
    ),
    START(
            RecordingState.PREPARED.getMask()
    ),
    PAUSE(
            RecordingState.STARTED.getMask()
    ),
    STOP(
            RecordingState.STARTED.getMask() | RecordingState.PAUSED.getMask()
    ),
    DIE(
            RecordingState.getAllMask() & ~RecordingState.DEAD.getMask()
    );

    private int mPossibleStatesMask;

    RecordingCommand(int possibleStates) {
        mPossibleStatesMask = possibleStates;
    }

    /**
     * Determines whether or not the command is possible by checking if the bit for the possible state
     * is set.
     *
     * @param state State to check.
     * @return True if it is a possible command for the given state.
     */
    public boolean isPossible(RecordingState state) {
        return (mPossibleStatesMask & state.getMask()) != 0;
    }

    @Override
    public String toString() {
        switch (this) {
            case PREPARE:
                return "Prepare";
            case START:
                return "Start";
            case PAUSE:
                return "Pause";
            case STOP:
                return "Stop";
            case DIE:
                return "Die";
            default:
                return null;
        }
    }
}
