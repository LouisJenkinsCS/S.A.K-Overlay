package com.theif519.utils.Misc;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by theif519 on 10/12/2015.
 * <p/>
 * Author: Louis Jenkins
 * <p/>
 * A way around the annoying lack of a practical way to retrieve the file directly from the R.raw.*
 * file.
 */
public final class FileRetriever {

    private final static String TAG = "FileRetriever";

    private FileRetriever() {

    }

    /**
     * This method creates a temporary file on the device, then copies the contents in the stream into
     * the new temporary file.
     *
     * @param stream Input stream to the resource.
     * @return Temporary file copy of the stream resource.
     */
    public static File streamToFile(InputStream stream) {
        File file;
        OutputStream outputStream;
        try {
            file = File.createTempFile("tmp", ".tmp");
            outputStream = new FileOutputStream(file);
            byte buf[] = new byte[1024];
            int read;
            while ((read = stream.read(buf)) != -1) {
                outputStream.write(buf, 0, read);
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
        return file;
    }

    /**
     * Simple function to retrieve the maximum line count.
     *
     * @param file File to retrieve the line count of.
     * @return The maximum line count in the file.
     * @throws IOException On LineNumberReader IO error.
     */
    public static int getLineCount(File file) throws IOException {
        LineNumberReader fSize = new LineNumberReader(new FileReader(file));
        // Continue skipping until you reach the end.
        while (fSize.skip(Long.MAX_VALUE) == Long.MAX_VALUE) ;
        int lineCount = fSize.getLineNumber();
        fSize.close();
        return lineCount;
    }

    public static ArrayList<File> getFiles(String dir) {
        ArrayList<File> files = new ArrayList<>();
        File f = new File(dir);
        f.mkdirs();
        File[] arr = f.listFiles();
        Collections.addAll(files, arr);
        return files;
    }
}
