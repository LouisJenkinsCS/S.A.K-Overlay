package com.theif519.sakoverlay.Widgets.Async;

import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;

import com.coremedia.iso.IsoFile;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.theif519.sakoverlay.Widgets.POJO.VideoInfo;

import java.io.File;
import java.io.IOException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by theif519 on 11/19/2015.
 * <p/>
 * An AsyncTask used to retrieve thumbnails and information about the previous ScreenRecorder's recordings.
 */
public abstract class MediaThumbnailGenerator extends AsyncTask<File, Void, List<VideoInfo>> {

    private String prettyDuration(int duration) {
        // Seconds to milliseconds
        Time time = new Time(duration * 1000L);
        return time.toString();
    }

    /**
     * Converts size to human readable format. Could not have done it this elegantly myself, hence here
     * is the obligatory StackOverflow source: http://stackoverflow.com/a/3758880/4111188
     *
     * @param bytes Size
     * @return Human-Readable String representation.
     */
    private String prettyFileSize(long bytes) {
        int unit = 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    /**
     * For each file, it will parse information and store it in a VideoInfo file, which can be used later
     * to add to the VideoInfoAdapter
     * @param params Files to parse.
     * @return List of VideoInfo, or null on error.
     */
    @Override
    protected List<VideoInfo> doInBackground(File... params) {
        List<VideoInfo> list = new ArrayList<>();
        for (File file : params) {
            try {
                // An ISO file contains a ton of parsed information which a normal File cannot provide.
                IsoFile mp4 = new IsoFile(new FileDataSourceImpl(file));
                list.add(new VideoInfo()
                                .setFilePath(file.getAbsolutePath())
                                .setDescription(file.getName())
                                .setDuration(prettyDuration((int) ((double) mp4.getMovieBox().getMovieHeaderBox().getDuration() / mp4.getMovieBox().getMovieHeaderBox().getTimescale())))
                                .setTimestamp(mp4.getMovieBox().getMovieHeaderBox().getCreationTime().toString())
                                .setFileSize(prettyFileSize(file.length()))
                                .setThumbnail(ThumbnailUtils.createVideoThumbnail(file.getAbsolutePath(), MediaStore.Video.Thumbnails.MICRO_KIND))
                );
            } catch (IOException | NullPointerException e) { // Because .getMovieBox() returns null on corrupted files or non MP4 files, we lazily ignore them.
                Log.w(getClass().getName(), "Error while parsing info from video: " + (e.getMessage() == null ? "" : e.getMessage()));
            }
        }
        return list;
    }

    @Override
    abstract protected void onPostExecute(List<VideoInfo> videoInfos);
}
