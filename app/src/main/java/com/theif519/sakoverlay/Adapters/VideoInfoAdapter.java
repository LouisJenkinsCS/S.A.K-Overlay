package com.theif519.sakoverlay.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.theif519.sakoverlay.Beans.VideoInfo;
import com.theif519.sakoverlay.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by theif519 on 11/19/2015.
 */
public class VideoInfoAdapter extends ArrayAdapter<VideoInfo> {

    private static final int RESOURCE_ID = R.layout.list_view_video_info;

    private Context mContext;
    private List<VideoInfo> mVideoInfo = new ArrayList<>();

    public VideoInfoAdapter(Context context, VideoInfo[] objects) {
        this(context, Arrays.asList(objects));
    }

    public VideoInfoAdapter(Context context, List<VideoInfo> objects) {
        super(context, RESOURCE_ID, objects);
        mContext = context;
        mVideoInfo = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        VideoInfoHolder holder = null;
        if(row == null){
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(RESOURCE_ID, parent, false);
            holder = new VideoInfoHolder(row);
            row.setTag(holder);
        } else {
            holder = (VideoInfoHolder) row.getTag();
        }
        holder.setup(mVideoInfo.get(position));
        return row;
    }

    private class VideoInfoHolder{
        private TextView mDescription, mDuration, mTimeStamp, mFileSize;
        private ImageView mThumbnail;
        private Bitmap mBitmap;

        public VideoInfoHolder(View view){
            mDescription = (TextView) view.findViewById(R.id.list_view_video_info_description);
            mDuration = (TextView) view.findViewById(R.id.list_view_video_info_duration);
            mTimeStamp = (TextView) view.findViewById(R.id.list_view_video_info_timestamp);
            mFileSize = (TextView) view.findViewById(R.id.list_view_video_info_size);
            mThumbnail = (ImageView) view.findViewById(R.id.list_view_video_info_thumbnail);
        }

        public VideoInfoHolder(View view, VideoInfo info) {
            this(view);
            setup(info);
        }

        public void setup(VideoInfo info){
            mDescription.setText(info.getDescription());
            mDuration.setText(info.getDuration());
            mTimeStamp.setText(info.getTimestamp());
            mFileSize.setText(info.getFileSize());
            mThumbnail.setImageBitmap(mBitmap = info.getThumbnail());
        }
    }
}
