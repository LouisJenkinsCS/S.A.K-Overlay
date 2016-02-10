package com.theif519.sakoverlay.Components.Misc;

import java.io.File;

/**
 * Created by theif519 on 1/20/2016.
 */
public class FileWrapperFactory {
    public static FileWrapper create(File file) {
        if(file.isHidden()) return null;
        if (file.isDirectory()) {
            return new DirectoryFileWrapper(file);
        } else {
            if(hasExtension(file, ".(jpg|png|bmp)")){
                return new PictureFileWrapper(file);
            } else if (hasExtension(file, ".(mp3|mp4|wav)")){
                return new VideoFileWrapper(file);
            } else {
                return new DefaultFileWrapper(file);
            }
        }
    }

    private static boolean hasExtension(File file, String regExp){
        String fileName = file.getName();
        if(fileName.contains(".")){
            String extension = fileName.substring(fileName.indexOf("."));
            return extension.matches(regExp);
        } else return false;
    }
}
