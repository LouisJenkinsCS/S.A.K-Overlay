package com.theif519.sakoverlay.Sessions.Recording;

/**
 * Created by theif519 on 12/1/2015.
 * <p/>
 * Encapsulates ScreenRecorder information passed to the RecorderService.
 */
public class RecordingInfo {
    boolean mAudioEnabled;
    String mFileName;
    private int mWidth, mHeight;

    public RecordingInfo(int width, int height, boolean audioEnabled, String fileName) {
        mWidth = width;
        mHeight = height;
        mAudioEnabled = audioEnabled;
        mFileName = fileName;
    }

    public boolean isValid(StringBuilder errMsg){
        errMsg.delete(0, errMsg.length());
        if (mWidth == 0) {
            errMsg.append("Width must be larger than or equal to 0!\n");
        }
        if (mHeight == 0) {
            errMsg.append("Height must be large than or equal to 0!\n");
        }
        if (mFileName == null) {
            errMsg.append("Filename cannot be left null!");
        }
        if (mFileName != null && mFileName.isEmpty()) {
            errMsg.append("Filename cannot be left empty!");
        }
        return errMsg.toString().isEmpty();
    }

    public int getWidth() {
        return mWidth;
    }

    public void setWidth(int Width) {
        this.mWidth = mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public void setHeight(int Height) {
        this.mHeight = mHeight;
    }

    public boolean isAudioEnabled() {
        return mAudioEnabled;
    }

    public void setAudioEnabled(boolean AudioEnabled) {
        this.mAudioEnabled = mAudioEnabled;
    }

    public String getFileName() {
        return mFileName;
    }

    public void setFileName(String FileName) {
        this.mFileName = mFileName;
    }
}
