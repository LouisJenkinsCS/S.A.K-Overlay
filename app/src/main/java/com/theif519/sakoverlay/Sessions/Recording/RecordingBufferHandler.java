package com.theif519.sakoverlay.Sessions.Recording;

import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.util.Log;

import com.theif519.sakoverlay.Rx.Transformers;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.CharBuffer;

import rx.Observable;

/**
 * Created by theif519 on 12/25/2015.
 */
public class RecordingBufferHandler {
    public static final String NAME = "Recording Buffer Handler";
    private LocalServerSocket mServerSocket;
    private File mFile;

    public RecordingBufferHandler(File file) throws IOException {
        mServerSocket = new LocalServerSocket(NAME);
        mFile = file;
        Observable.<LocalSocket>create(subscriber -> {
                    try {
                        subscriber.onNext(mServerSocket.accept());
                        subscriber.onCompleted();
                    } catch (IOException e) {
                        subscriber.onError(e);
                    }
                })
                .compose(Transformers.backgroundIO())
                .subscribe(localSocket -> {
                    try {
                        FileWriter writer = new FileWriter(mFile);
                        FileReader reader = new FileReader(localSocket.getFileDescriptor());
                        CharBuffer buffer = CharBuffer.allocate(1024);
                        int charsRead;
                        while ((charsRead = reader.read(buffer)) != -1) {
                            writer.write(buffer.array(), 0,  charsRead);
                        }
                        writer.close();
                        reader.close();
                        localSocket.close();
                        mServerSocket.close();
                    } catch (IOException e) {
                        Log.w(getClass().getName(), e.getMessage());
                    }
                });
    }
}
