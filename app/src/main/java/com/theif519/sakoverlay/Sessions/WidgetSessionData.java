package com.theif519.sakoverlay.Sessions;

/**
 * Created by theif519 on 12/10/2015.
 *
 * TODO: Make this more recylceable.
 */
public class WidgetSessionData {
    private int id;
    private String tag;
    private byte[] data;

    public WidgetSessionData(int id, String tag, byte[] data) {
        this.id = id;
        this.tag = tag;
        this.data = data;
    }

    public int getId() {
        return id;
    }

    public WidgetSessionData setId(int id) {
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
}
