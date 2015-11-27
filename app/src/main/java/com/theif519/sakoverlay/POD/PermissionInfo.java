package com.theif519.sakoverlay.POD;

import android.content.Intent;

/**
 * Created by theif519 on 11/26/2015.
 */
public class PermissionInfo {
    private Intent mIntent;
    private int mResultCode;

    public PermissionInfo(Intent mIntent, int mResultCode) {
        this.mIntent = mIntent;
        this.mResultCode = mResultCode;
    }

    public Intent getIntent() {
        return mIntent;
    }

    public PermissionInfo setIntent(Intent mIntent) {
        this.mIntent = mIntent;
        return this;
    }

    public int getResultCode() {
        return mResultCode;
    }

    public PermissionInfo setmResultCode(int mResultCode) {
        this.mResultCode = mResultCode;
        return this;
    }
}
