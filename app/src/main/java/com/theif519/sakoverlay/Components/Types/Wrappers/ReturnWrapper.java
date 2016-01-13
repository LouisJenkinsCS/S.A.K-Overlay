package com.theif519.sakoverlay.Components.Types.Wrappers;

/**
 * Created by theif519 on 1/13/2016.
 */
public class ReturnWrapper<T> {

    private String mDescription;
    private Class<T> mType;

    public static <T> ReturnWrapper<T> raw(Class<T> type){
        return new ReturnWrapper<>(null, type);
    }

    public ReturnWrapper(String description, Class<T> type){
        mDescription = description;
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

    public void setDescription(String description) {
        mDescription = description;
    }

    @Override
    public String toString() {
        return mType.getSimpleName();
    }
}
