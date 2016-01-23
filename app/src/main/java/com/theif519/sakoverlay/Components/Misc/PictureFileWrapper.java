package com.theif519.sakoverlay.Components.Misc;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.theif519.sakoverlay.Core.Rx.Transformers;

import java.io.File;
import java.io.IOException;

import rx.Observable;

/**
 * Created by theif519 on 1/20/2016.
 */
public class PictureFileWrapper extends ThumbnailFileWrapper {

    public PictureFileWrapper(File mFile) {
        super(mFile);
    }

    public Observable<Bitmap> getPicture() {
        return Observable.<Bitmap>create(subscriber -> {
            Bitmap picture = BitmapFactory.decodeFile(getFilePath());
            if (picture == null) {
                subscriber.onError(new IOException("An attempt to decode the bitmap from the file path: \""
                        + getFilePath() + "\"" + "has failed and returned null!"));
            } else {
                subscriber.onNext(picture);
                subscriber.onCompleted();
            }
        }).compose(Transformers.asyncResult());
    }
}
