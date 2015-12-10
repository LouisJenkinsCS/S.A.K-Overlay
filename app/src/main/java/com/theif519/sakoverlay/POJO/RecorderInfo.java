package com.theif519.sakoverlay.POJO;

/**
 * Created by theif519 on 12/1/2015.
 * <p/>
 * Encapsulates ScreenRecorder information passed to the RecorderService.
 */
public class RecorderInfo {
    boolean mAudioEnabled;
    String mFileName;
    private int mWidth, mHeight;

    public RecorderInfo(int width, int height, boolean audioEnabled, String fileName) {
        mWidth = width;
        mHeight = height;
        mAudioEnabled = audioEnabled;
        mFileName = fileName;
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
