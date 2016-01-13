package com.theif519.sakoverlay.Components.Types.Wrappers;

/**
 * Created by theif519 on 1/13/2016.
 */
public class ParameterWrapper<T> {
    private String mDescription, mName;
    private Class<T> mType;

    public static <T> ParameterWrapper<T> raw(Class<T> type){
        return new ParameterWrapper<>(null, null, type);
    }

    public ParameterWrapper(String name, String description, Class<T> type) {
        mDescription = description;
        mName = name;
        mType = type;
    }

    public void setType(Class<T> type){
        mType = type;
    }

    public Class<T> getType(){
        return mType;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    @Override
    public String toString() {
        String str = mType.getSimpleName();
        if(mName != null){
            str += " " + mName;
        }
        return str;
    }
}
