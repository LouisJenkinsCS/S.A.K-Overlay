package com.theif519.sakoverlay.Misc;

import android.graphics.Bitmap;

/**
 * Created by theif519 on 11/19/2015.
 */
public class VideoInfo {
    private String mDescription, mDuration, mTimestamp, mFileSize;
    private Bitmap mThumbnail;

    public VideoInfo(String mDescription, String mDuration, String mTimestamp, String mFileSize, Bitmap mThumbnail) {
        this.mDescription = mDescription;
        this.mDuration = mDuration;
        this.mTimestamp = mTimestamp;
        this.mThumbnail = mThumbnail;
        this.mFileSize = mFileSize;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    public String getDuration() {
        return mDuration;
    }

    public void setDuration(String mDuration) {
        this.mDuration = mDuration;
    }

    public String getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(String mTimestamp) {
        this.mTimestamp = mTimestamp;
    }

    public String getFileSize(){
        return mFileSize;
    }

    public void setFileSize(String mFileSize){
        this.mFileSize = mFileSize;
    }

    public Bitmap getThumbnail() {
        return mThumbnail;
    }

    public void setThumbnail(Bitmap mThumbnail) {
        this.mThumbnail = mThumbnail;
    }
}
