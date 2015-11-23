package com.theif519.sakoverlay.Beans;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import rx.Observable;
import rx.Observer;

/**
 * Created by theif519 on 11/23/2015.
 */
public class MenuChildInfo implements Parcelable {
    private String mDescription;
    private Bitmap mIcon;
    private boolean mIsSelectable;
    private Observable<?> mObservable;

    public MenuChildInfo(){

    }

    protected MenuChildInfo(Parcel in) {
        mDescription = in.readString();
        mIcon = in.readParcelable(Bitmap.class.getClassLoader());
        mIsSelectable = in.readByte() != 0;
    }

    public static final Creator<MenuChildInfo> CREATOR = new Creator<MenuChildInfo>() {
        @Override
        public MenuChildInfo createFromParcel(Parcel in) {
            return new MenuChildInfo(in);
        }

        @Override
        public MenuChildInfo[] newArray(int size) {
            return new MenuChildInfo[size];
        }
    };

    public MenuChildInfo setDescription(String description){
        mDescription = description;
        return this;
    }

    public String getDescription(){
        return mDescription;
    }

    public MenuChildInfo setIcon(Bitmap icon){
        mIcon = icon;
        return this;
    }

    public MenuChildInfo setSelectable(boolean isSelectable){
        mIsSelectable = isSelectable;
        return this;
    }

    public boolean isSelectable(){
        return mIsSelectable;
    }

    public Bitmap getIcon(){
        return mIcon;
    }

    public MenuChildInfo setObservable(Observable<?> observable){
        mObservable = observable;
        return this;
    }

    public MenuChildInfo alertObserver(){
        // TODO: Find some way to broadcast event.
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mDescription);
        dest.writeParcelable(mIcon, flags);
        dest.writeByte((byte) (mIsSelectable ? 1 : 0));
    }
}
