package com.theif519.sakoverlay.POD;

import android.graphics.Bitmap;

import com.theif519.sakoverlay.Interfaces.OnMenuOptionSelected;

/**
 * Created by theif519 on 11/23/2015.
 */
public abstract class MenuChildInfo implements OnMenuOptionSelected {
    private String mDescription;
    private Bitmap mIcon;

    public MenuChildInfo(String description, Bitmap icon){
        mDescription = description;
        mIcon = icon;
    }

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

    public Bitmap getIcon(){
        return mIcon;
    }

}
