package com.theif519.sakoverlay.POJO;

import android.widget.PopupWindow;

import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by theif519 on 12/17/2015.
 */
public class MenuOptions {
    private PopupWindow mMenu;
    private int mIconResId;
    private String mIdentifier;
    private PublishSubject<Void> mOwnerDead;
    private boolean mIsShowing;

    public MenuOptions(PopupWindow mMenu, int resId, String mIdentifier){
        this.mMenu = mMenu;
        this.mIconResId = resId;
        this.mOwnerDead = PublishSubject.create();
        this.mIdentifier = mIdentifier;
    }

    public PopupWindow getMenu() {
        return mMenu;
    }

    public void setMenu(PopupWindow mMenu) {
        this.mMenu = mMenu;
    }

    public int getIconResId() {
        return mIconResId;
    }

    public void setIconResId(int resId) {
        this.mIconResId = resId;
    }

    public String getIdentifier(){
        return mIdentifier;
    }

    public void setIdentifier(String identifier){
        this.mIdentifier = identifier;
    }

    public Observable<Void> onOwnerDead() {
        return mOwnerDead.asObservable();
    }

    public void notifyObservers(){
        mOwnerDead.onNext(null);
    }

    public boolean isShowing() {
        return mIsShowing;
    }

    public void setIsShowing(boolean mIsShowing) {
        this.mIsShowing = mIsShowing;
    }
}
