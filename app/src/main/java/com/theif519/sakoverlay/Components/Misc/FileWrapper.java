package com.theif519.sakoverlay.Components.Misc;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.theif519.utils.Misc.FileTools;

import java.io.File;

/**
 * Created by theif519 on 1/20/2016.
 */
public class FileWrapper implements Comparable<FileWrapper> {
    private File mFile;

    public FileWrapper(@NonNull File mFile) {
        this.mFile = mFile;
    }

    public boolean matches(String regExp){
        return mFile.getName().matches(regExp);
    }

    @Override
    public int compareTo(@NonNull FileWrapper another) {
        return mFile.compareTo(another.mFile);
    }

    public File getFile() {
        return mFile;
    }

    public long getFileSize(){
        return getFile().length();
    }

    public String getFileSizeAsString(){
        return FileTools.getFileSize(getFile());
    }

    public String getFileName(){
        return mFile.getName();
    }

    public String getFilePath(){
        return mFile.getPath();
    }

    @Nullable
    public String getFileExtension(){
        String fileName = getFileName();
        if(fileName.contains(".")){
            return fileName.substring(fileName.indexOf("."));
        }
        return null;
    }

    public void setFile(File mFile) {
        this.mFile = mFile;
    }
}