package com.theif519.sakoverlay.Components.Misc;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;

import java.io.File;

/**
 * Created by theif519 on 1/20/2016.
 */
public class PictureFileWrapper extends FileWrapper implements ThumbnailFileWrapper {

    public PictureFileWrapper(File mFile) {
        super(mFile);
    }

    @Override
    public Bitmap getThumbnail() {
        return ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(getFile().getPath()), 96, 96);
    }

    public Bitmap getPicture() {
        Bitmap b = BitmapFactory.decodeFile(getFile().getPath());
        return ThumbnailUtils.extractThumbnail(b, b.getWidth(), b.getHeight());
    }
}
