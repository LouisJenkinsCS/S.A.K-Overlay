package com.theif519.sakoverlay.Sessions;

import com.google.gson.GsonBuilder;
import com.theif519.sakoverlay.Fragments.Floating.FloatingFragment;

/**
 * Created by theif519 on 12/10/2015.
 *
 * TODO: Make this more recylceable.
 */
public class WidgetSessionData {
    private long id;
    private String tag;
    private byte[] data;

    public WidgetSessionData(long id, String tag, byte[] data) {
        this.id = id;
        this.tag = tag;
        this.data = data;
    }

    public WidgetSessionData(){

    }

    public WidgetSessionData(FloatingFragment fragment){
        this.id = fragment.getId();
        this.tag = fragment.getLayoutTag();
        this.data = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create()
                .toJson(fragment)
                .getBytes();
    }

    public long getId() {
        return id;
    }

    public WidgetSessionData setId(long id) {
        this.id = id;
        return this;
    }

    public String getTag() {
        return tag;
    }

    public WidgetSessionData setTag(String tag) {
        this.tag = tag;
        return this;
    }

    public byte[] getData() {
        return data;
    }

    public WidgetSessionData setData(byte[] data) {
        this.data = data;
        return this;
    }

    @Override
    public String toString() {
        return "WidgetSessionData: { Tag: " + tag + ", Id: " + id + ", Data: {" + new String(data) + "} }";
    }
}
