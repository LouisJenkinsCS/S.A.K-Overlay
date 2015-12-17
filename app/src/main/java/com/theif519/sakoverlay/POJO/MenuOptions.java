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
    private PublishSubject<Void> mOwnerDead;

    public MenuOptions(PopupWindow mMenu, int resId){
        this.mMenu = mMenu;
        this.mIconResId = resId;
        this.mOwnerDead = PublishSubject.create();
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

    public Observable<Void> onOwnerDead() {
        return mOwnerDead.asObservable();
    }

    public void notifyObservers(){
        mOwnerDead.onNext(null);
    }

}
