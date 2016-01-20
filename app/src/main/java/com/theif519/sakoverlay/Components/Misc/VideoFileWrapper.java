package com.theif519.sakoverlay.Components.Misc;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;

import java.io.File;

/**
 * Created by theif519 on 1/20/2016.
 */
public class VideoFileWrapper extends FileWrapper implements ThumbnailFileWrapper {

    public VideoFileWrapper(File mFile) {
        super(mFile);
    }

    @Override
    public Bitmap getThumbnail() {
        return ThumbnailUtils.createVideoThumbnail(getFile().getAbsolutePath(), MediaStore.Video.Thumbnails.MICRO_KIND);
    }
}
