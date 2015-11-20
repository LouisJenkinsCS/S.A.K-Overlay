package com.theif519.sakoverlay.Views;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.theif519.sakoverlay.Misc.VideoInfo;
import com.theif519.sakoverlay.R;

import java.io.IOException;

/**
 * Created by theif519 on 11/19/2015.
 */
public class ListViewVideoInfo extends RelativeLayout {

    private TextView mDescription, mDuration, mTimeStamp;
    private ImageView mThumbnail;
    private Bitmap mBitmap;

    public ListViewVideoInfo(Context context, VideoInfo info) throws IOException {
        this(context);
        mDescription.setText(info.getDescription());
        mDuration.setText(info.getDuration());
        mTimeStamp.setText(info.getTimestamp());
        mThumbnail.setImageBitmap(mBitmap = info.getThumbnail());
    }

    public ListViewVideoInfo(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.list_view_video_info, this);
        mDescription = (TextView) findViewById(R.id.list_view_video_info_description);
        mDuration = (TextView) findViewById(R.id.list_view_video_info_duration);
        mTimeStamp = (TextView) findViewById(R.id.list_view_video_info_timestamp);
        mThumbnail = (ImageView) findViewById(R.id.list_view_video_info_thumbnail);
    }

    public String getDescription() {
        return mDescription.getText().toString();
    }

    public String getDuration() {
        return mDuration.getText().toString();
    }

    public String getTimeStamp() {
        return mTimeStamp.getText().toString();
    }

    public Bitmap getThumbnail() {
        return mBitmap;
    }
}
