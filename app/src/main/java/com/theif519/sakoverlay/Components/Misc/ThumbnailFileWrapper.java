package com.theif519.sakoverlay.Components.Misc;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.support.annotation.NonNull;

import com.theif519.sakoverlay.Core.Rx.Transformers;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;

import rx.Observable;

/**
 * Created by theif519 on 1/20/2016.
 */
public class ThumbnailFileWrapper extends FileWrapper {

    SoftReference<Bitmap> mThumbnail = new SoftReference<>(null);

    public ThumbnailFileWrapper(@NonNull File mFile) {
        super(mFile);
    }

    public Observable<Bitmap> getThumbnail(int width, int height) {
        return Observable.<Bitmap>create(subscriber -> {
            Bitmap thumbnail = mThumbnail.get();
            // If we have been garbage collected...
            if (thumbnail == null) {
                Bitmap picture = BitmapFactory.decodeFile(getFilePath());
                if (picture == null) {
                    subscriber.onError(new IOException("An attempt to decode the bitmap from the file path: \""
                            + getFilePath() + "\"" + "has failed and returned null!"));
                    return;
                } else {
                    thumbnail = ThumbnailUtils.extractThumbnail(picture, width, height);
                    if (thumbnail == null) {
                        subscriber.onError(new IOException("An attempt was made to extract a thumbnail of width: "
                                + width + ", and height: " + height + " has failed and returned null!"));
                        return;
                    } else {
                        mThumbnail = new SoftReference<>(thumbnail);
                    }
                }
            }
            subscriber.onNext(thumbnail);
            subscriber.onCompleted();
        }).compose(Transformers.asyncResult());
    }
}
