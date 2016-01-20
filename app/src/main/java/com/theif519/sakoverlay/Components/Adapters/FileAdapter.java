package com.theif519.sakoverlay.Components.Adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.theif519.sakoverlay.Components.Misc.DirectoryFileWrapper;
import com.theif519.sakoverlay.Components.Misc.FileWrapper;
import com.theif519.sakoverlay.Components.Misc.PictureFileWrapper;
import com.theif519.sakoverlay.Components.Misc.VideoFileWrapper;

import java.util.List;

/**
 * Created by theif519 on 1/20/2016.
 */
public class FileAdapter extends BaseAdapter {

    private List<FileWrapper> mFileWrappers;

    private static final int DEFAULT = 0, DIRECTORY = 1, PICTURE = 2, VIDEO = 3;

    @Override
    public int getItemViewType(int position) {
        FileWrapper wrapper = mFileWrappers.get(position);
        if (wrapper instanceof DirectoryFileWrapper) {
            return DIRECTORY;
        } else if (wrapper instanceof PictureFileWrapper) {
            return PICTURE;
        } else if (wrapper instanceof VideoFileWrapper) {
            return VIDEO;
        } else {
            return DEFAULT;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 4;
    }

    @Override
    public boolean isEmpty() {
        return mFileWrappers.isEmpty();
    }

    @Override
    public int getCount() {
        return mFileWrappers.size();
    }

    @Override
    public FileWrapper getItem(int position) {
        return mFileWrappers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }

    private abstract class BaseFileViewHolder {

        public BaseFileViewHolder(View v) {
        }

        public abstract void setup(FileWrapper wrapper);
    }

    private class PictureFileViewHolder extends BaseFileViewHolder {

        public PictureFileViewHolder(View v) {
            super(v);
        }

        @Override
        public void setup(FileWrapper wrapper) {

        }
    }

    private class VideoFileViewHolder extends BaseFileViewHolder {

        public VideoFileViewHolder(View v) {
            super(v);
        }

        @Override
        public void setup(FileWrapper wrapper) {

        }
    }

    private class DirectoryFileViewHolder extends BaseFileViewHolder {

        public DirectoryFileViewHolder(View v) {
            super(v);
        }

        @Override
        public void setup(FileWrapper wrapper) {

        }
    }

    private class DefaultFileViewHolder extends BaseFileViewHolder {

        public DefaultFileViewHolder(View v) {
            super(v);
        }

        @Override
        public void setup(FileWrapper wrapper) {

        }
    }
}
