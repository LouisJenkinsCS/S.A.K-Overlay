package com.theif519.sakoverlay.Async;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import com.theif519.sakoverlay.Misc.Globals;
import com.theif519.sakoverlay.Sessions.RecorderInfo;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by theif519 on 12/24/2015.
 */
public class MediaRecorder {

    private static final String MIME_TYPE = "video/avc"; // H.264 Advanced Video Coding
    private static final int FRAME_RATE = 60; // Default 60fps
    private static final int IFRAME_INTERVAL = 10;
    private static final int VIDEO_WIDTH = 1280;
    private static final int VIDEO_HEIGHT = 720;
    private static final int BIT_RATE = 4 * 1000 * 1000;

    private File mOutputFile;
    private MediaCodec mCodec;
    private VirtualDisplay mDisplay;
    private MediaProjection mProjection;
    private MediaMuxer mMuxer;
    private Surface mSurface;
    private MediaCodec.BufferInfo mBufferInfo;
    private int mTrackIndex;
    private boolean mMuxerStarted;

    public void prepare(Context context, com.theif519.sakoverlay.POJO.PermissionInfo permissionInfo, RecorderInfo recorderInfo){
        MediaCodec codec;
        MediaMuxer muxer;
        if(mCodec != null || mSurface != null){
            throw new RuntimeException("Called prepare twice(?)");
        }
        mOutputFile = new File(Globals.RECORDER_FILE_SAVE_PATH + recorderInfo.getFileName());
        Log.d(getClass().getName(), "Video recording to file " + mOutputFile);
        mBufferInfo = new MediaCodec.BufferInfo();
        try {
            // Create and configure the MediaFormat
            MediaFormat format = MediaFormat.createAudioFormat(MIME_TYPE, recorderInfo.getWidth(), recorderInfo.getHeight());
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            format.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE);
            format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);
            // Create a MediaCodec encoder, and configure it with our format.
            codec = MediaCodec.createEncoderByType(MIME_TYPE);
            codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            // Create a MediaMuxer. We can't add the video track and start() the muxer here until after encoder starts processing data.
            muxer = new MediaMuxer(mOutputFile.getAbsolutePath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            mMuxerStarted = false;
            mCodec = codec;
            mMuxer = muxer;
        } catch (IOException e){
            Log.w(getClass().getName(), "Something failed during recorder init: " + e);
            releaseEncoder();
        }
        setup(context, permissionInfo, recorderInfo);
    }

    public void setup(Context context, com.theif519.sakoverlay.POJO.PermissionInfo permissionInfo, RecorderInfo recorderInfo){
        if(!isRecording() || mSurface != null){
            // Not recording, or already initialized
            return;
        }
        try {
            mProjection = ((MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE))
                    .getMediaProjection(permissionInfo.getResultCode(), permissionInfo.getIntent());
            mDisplay = createVirtualDisplay(context, recorderInfo.getWidth(), recorderInfo.getHeight());
            mCodec.start();
        } catch (RuntimeException ex){
            Log.w(getClass().getName(), "Something failed during recorder init: " + ex);
            releaseEncoder();
        }
    }

    /**
     * Convenience method to create a virtual display.
     *
     * @param width  Width of display.
     * @param height Heigth of virtual display.
     * @return Initialized virtual display.
     */
    private VirtualDisplay createVirtualDisplay(Context context, int width, int height) {
        Log.i(getClass().getName(), "Creating Virtual Display...");
        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
        return mProjection.createVirtualDisplay(getClass().getName(), width, height,
                metrics.densityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mCodec.createInputSurface(), null, null);
    }

    private void releaseEncoder(){
        Log.d(getClass().getName(), "Releasing encoder objects");
        if(mCodec != null){
            mCodec.stop();
            mCodec.release();
            mCodec = null;
        }
        if(mSurface != null){
            mSurface.release();
            mSurface = null;
        }
        if(mMuxer != null){
            mMuxer.stop();
            mMuxer.release();
            mMuxer = null;
        }
        if(mDisplay != null){
            mDisplay.release();
            mDisplay = null;
        }
    }

    public void stop(){
        drainEncoder(true);
        releaseEncoder();
    }

    public boolean isRecording() {
        return mCodec != null;
    }

    private void drainEncoder(boolean EOS){
        if(EOS){
            mCodec.signalEndOfInputStream();
        }
        while(true){
            int encoderStatus = mCodec.dequeueOutputBuffer(mBufferInfo, 0);
            if(encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER){
                break;
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){
                if(mMuxerStarted) {
                    throw new RuntimeException("Format changed twice!");
                }
                MediaFormat newFormat = mCodec.getOutputFormat();
                Log.d(getClass().getName(), "Encoder Output format changed: " + newFormat);
                mTrackIndex = mMuxer.addTrack(newFormat);
                mMuxer.start();
                mMuxerStarted = true;
            } else {
                ByteBuffer encodedData = mCodec.getOutputBuffer(encoderStatus);
                if(encodedData == null){
                    throw new RuntimeException("Encoder Output Buffer " + encoderStatus + " was null");
                }
                if((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0){
                    // The codec config data was pulled out and fed to the muxer when we got the status. Ignored.
                    Log.d(getClass().getName(), "ignoring BUFFER_FLAG_CODEC_CONFIG");
                    mBufferInfo.size = 0;
                }
                if(mBufferInfo.size != 0){
                    if(!mMuxerStarted){
                        throw new RuntimeException("Muxer hasn't started!");
                    }
                    // Adjust the ByteBuffer values to match BufferInfo. (not needed?)
                    encodedData.position(mBufferInfo.offset);
                    encodedData.limit(mBufferInfo.offset + mBufferInfo.size);
                    mMuxer.writeSampleData(mTrackIndex, encodedData, mBufferInfo);
                    Log.d(getClass().getName(), "wrote " + mBufferInfo.size + " bytes");
                }
                mCodec.releaseOutputBuffer(encoderStatus, false);
                if((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0){
                    if(!EOS){
                        Log.w(getClass().getName(), "reached end of stream unexpectedly");
                    } else {
                        Log.d(getClass().getName(), "end of stream reached");
                    }
                    break;
                }
            }
        }
    }

}
