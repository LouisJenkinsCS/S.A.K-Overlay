package com.theif519.sakoverlay.Components.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.theif519.sakoverlay.Components.Misc.DefaultFileWrapper;
import com.theif519.sakoverlay.Components.Misc.DirectoryFileWrapper;
import com.theif519.sakoverlay.Components.Misc.FileWrapper;
import com.theif519.sakoverlay.Components.Misc.PictureFileWrapper;
import com.theif519.sakoverlay.Components.Misc.VideoFileWrapper;
import com.theif519.sakoverlay.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by theif519 on 1/20/2016.
 */
public class FileAdapter extends BaseAdapter {

    private List<FileWrapper> mFileWrappers;
    private WeakReference<Context> mContext;

    private static final int DEFAULT = 0, DIRECTORY = 1, PICTURE = 2, VIDEO = 3;

    public FileAdapter(Context context) {
        this.mFileWrappers = new ArrayList<>();
        mContext = new WeakReference<>(context);
    }

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
        View resultView = convertView;
        BaseFileViewHolder holder;
        if (resultView == null) {
            int type = getItemViewType(position);
            switch (type) { // FixMe: This doesn't seem to be the right way to go about this.
                case DEFAULT:
                    resultView = LayoutInflater.from(getContext()).inflate(R.layout.file_wrapper_default, null);
                    holder = new DefaultFileViewHolder(resultView);
                    break;
                case DIRECTORY:
                    resultView = LayoutInflater.from(getContext()).inflate(R.layout.file_wrapper_directory, null);
                    holder = new DirectoryFileViewHolder(resultView);
                    break;
                case PICTURE:
                    resultView = LayoutInflater.from(getContext()).inflate(R.layout.file_wrapper_picture, null);
                    holder = new PictureFileViewHolder(resultView);
                    break;
                case VIDEO:
                    resultView = LayoutInflater.from(getContext()).inflate(R.layout.file_wrapper_video, null);
                    holder = new VideoFileViewHolder(resultView);
                    break;
                default:
                    throw new RuntimeException("Invalid type!");
            }
            resultView.setTag(holder);
        } else {
            holder = (BaseFileViewHolder) resultView.getTag();
        }
        holder.setup(getItem(position));
        return resultView;
    }

    private Context getContext() {
        Context context = mContext.get();
        if (context == null) {
            throw new RuntimeException("Memory leak averted, context was garbage collected!");
        }
        return context;
    }

    private abstract class BaseFileViewHolder {

        public BaseFileViewHolder(View v) {
        }

        public abstract void setup(FileWrapper wrapper);
    }

    private class PictureFileViewHolder extends BaseFileViewHolder {

        private ImageView mThumbnail;
        private TextView mFileName, mFileExtension, mFileSize;

        public PictureFileViewHolder(View v) {
            super(v);
            mFileName = (TextView) v.findViewById(R.id.file_wrapper_picture_name);
            mFileExtension = (TextView) v.findViewById(R.id.file_wrapper_picture_extension);
            mFileSize = (TextView) v.findViewById(R.id.file_wrapper_picture_size);
            mThumbnail = (ImageView) v.findViewById(R.id.file_wrapper_picture_thumbnail);
        }

        @Override
        public void setup(FileWrapper wrapper) {
            PictureFileWrapper pictureFileWrapper = (PictureFileWrapper) wrapper;
            pictureFileWrapper.getThumbnail(96, 96)
                    .subscribe(mThumbnail::setImageBitmap);
            mFileName.setText(pictureFileWrapper.getFileName());
            mFileExtension.setText(pictureFileWrapper.getFileExtension());
            mFileSize.setText(pictureFileWrapper.getFileSizeAsString());
        }
    }

    private class VideoFileViewHolder extends BaseFileViewHolder {

        private ImageView mThumbnail;
        private TextView mFileName, mFileExtension, mFileSize;

        public VideoFileViewHolder(View v) {
            super(v);
            mFileName = (TextView) v.findViewById(R.id.file_wrapper_video_name);
            mFileExtension = (TextView) v.findViewById(R.id.file_wrapper_video_extension);
            mFileSize = (TextView) v.findViewById(R.id.file_wrapper_video_size);
            mThumbnail = (ImageView) v.findViewById(R.id.file_wrapper_video_thumbnail);
        }

        @Override
        public void setup(FileWrapper wrapper) {
            VideoFileWrapper videoFileWrapper = (VideoFileWrapper) wrapper;
            videoFileWrapper.getThumbnail(96, 96)
                    .subscribe(mThumbnail::setImageBitmap);
            mFileName.setText(videoFileWrapper.getFileName());
            mFileExtension.setText(videoFileWrapper.getFileExtension());
            mFileSize.setText(videoFileWrapper.getFileSizeAsString());
        }
    }

    private class DirectoryFileViewHolder extends BaseFileViewHolder {

        private TextView mDirectoryName;

        public DirectoryFileViewHolder(View v) {
            super(v);
            mDirectoryName = (TextView) v.findViewById(R.id.file_wrapper_directory_name);
        }

        @Override
        public void setup(FileWrapper wrapper) {
            DirectoryFileWrapper directoryFileWrapper = (DirectoryFileWrapper) wrapper;
            mDirectoryName.setText(directoryFileWrapper.getFileName());
        }
    }

    private class DefaultFileViewHolder extends BaseFileViewHolder {

        private TextView mFileName, mFileExtension, mFileSize;

        public DefaultFileViewHolder(View v) {
            super(v);
            mFileName = (TextView) v.findViewById(R.id.file_wrapper_default_name);
            mFileExtension = (TextView) v.findViewById(R.id.file_wrapper_default_extension);
            mFileSize = (TextView) v.findViewById(R.id.file_wrapper_default_size);
        }

        @Override
        public void setup(FileWrapper wrapper) {
            DefaultFileWrapper defaultFileWrapper = (DefaultFileWrapper) wrapper;
            mFileName.setText(defaultFileWrapper.getFileName());
            mFileExtension.setText(defaultFileWrapper.getFileExtension());
            mFileSize.setText(defaultFileWrapper.getFileSizeAsString());
        }
    }
}
