package com.theif519.sakoverlay.POJO;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by theif519 on 11/19/2015.
 * <p/>
 * A Java Bean which encapsulates information about a video, describing it's title, duration, timestamp, file size, and
 * also containing a bitmap for the thumbnail. Used primarily for showing videos inside of ScreenRecorder
 * and is made parcelable in case it needs to be marshalled.
 * <p/>
 * It is made parcelable in the case that I move RecorderService to it's own process.
 */
public class VideoInfo implements Parcelable {

    public static final Creator<VideoInfo> CREATOR = new Creator<VideoInfo>() {
        @Override
        public VideoInfo createFromParcel(Parcel in) {
            return new VideoInfo(in);
        }

        @Override
        public VideoInfo[] newArray(int size) {
            return new VideoInfo[size];
        }
    };
    private String mDescription, mDuration, mTimestamp, mFileSize, mFilePath;
    private Bitmap mThumbnail;

    public VideoInfo() {

    }

    protected VideoInfo(Parcel in) {
        mDescription = in.readString();
        mDuration = in.readString();
        mTimestamp = in.readString();
        mFileSize = in.readString();
        mThumbnail = in.readParcelable(Bitmap.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mDescription);
        dest.writeString(mDuration);
        dest.writeString(mTimestamp);
        dest.writeString(mFileSize);
        dest.writeParcelable(mThumbnail, flags);
    }

    public String getDescription() {
        return mDescription;
    }

    public VideoInfo setDescription(String mDescription) {
        this.mDescription = mDescription;
        return this;
    }

    public String getDuration() {
        return mDuration;
    }

    public VideoInfo setDuration(String mDuration) {
        this.mDuration = mDuration;
        return this;
    }

    public String getTimestamp() {
        return mTimestamp;
    }

    public VideoInfo setTimestamp(String mTimestamp) {
        this.mTimestamp = mTimestamp;
        return this;
    }

    public String getFileSize() {
        return mFileSize;
    }

    public VideoInfo setFileSize(String mFileSize) {
        this.mFileSize = mFileSize;
        return this;
    }

    public Bitmap getThumbnail() {
        return mThumbnail;
    }

    public VideoInfo setThumbnail(Bitmap mThumbnail) {
        this.mThumbnail = mThumbnail;
        return this;
    }

    public String getFilePath() {
        return mFilePath;
    }

    public VideoInfo setFilePath(String filePath) {
        mFilePath = filePath;
        return this;
    }
}
