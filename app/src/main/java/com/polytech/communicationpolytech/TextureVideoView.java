package com.polytech.communicationpolytech;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.Surface;
import android.view.TextureView;
import android.view.TextureView;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by jeloc on 09/05/2017.
 */

public class TextureVideoView extends TextureView implements TextureView.SurfaceTextureListener {

    private MediaPlayer mMediaPlayer;
    private Uri mSource;
    private MediaPlayer.OnCompletionListener mCompletionListener;
    private MediaPlayer.OnPreparedListener mPreparedListener;
    private boolean isLooping = false;
    private String Source;


    public TextureVideoView(Context context) {
        this(context, null, 0);
    }

    public TextureVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TextureVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setSurfaceTextureListener(this);
    }

    public void setDataSource(Uri source) {
        mSource = source;
    }

    public void setOnCompletionListener(MediaPlayer.OnCompletionListener listener) {
        mCompletionListener = listener;
    }

    public void setOnPreparedListener(MediaPlayer.OnPreparedListener listener){
        mPreparedListener=listener;
    }

    public void setLooping(boolean looping) {
        isLooping = looping;
    }

    public void setDataSource(String source) {
        setDataSource(Uri.parse(source));
    }

    public void start(){
        if(mMediaPlayer!=null && !mMediaPlayer.isPlaying()){
            mMediaPlayer.start();
        }

    }

    public void pause(){
        if(mMediaPlayer != null && mMediaPlayer.isPlaying()){
            mMediaPlayer.pause();
        }
    }

    public void stop(){
        if(mMediaPlayer !=null && mMediaPlayer.isPlaying()){
            mMediaPlayer.stop();
        }
    }

    public int getCurrentPosition(){
        if(mMediaPlayer !=null){
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }


    /*
     * release the media player in any state
     */
    private void release(boolean cleartargetstate) {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }


    private void openVideo() {
        // we shouldn't clear the target state, because somebody might have
        // called start() previously
        release(false);


        try {
            mMediaPlayer = new MediaPlayer();

            mMediaPlayer.setOnCompletionListener(mCompletionListener);
            mMediaPlayer.setOnPreparedListener(mPreparedListener);
            mMediaPlayer.setDataSource(Source);
            mMediaPlayer.setSurface(new Surface(this.getSurfaceTexture()));
            mMediaPlayer.setScreenOnWhilePlaying(true);
            mMediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
            mMediaPlayer.prepareAsync();

            mMediaPlayer.start();

        } catch (IOException ex) {
            return;
        } catch (IllegalArgumentException ex) {

            return;
        } finally {
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        // release resources on detach
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        super.onDetachedFromWindow();
    }



    /*
     * TextureView.SurfaceTextureListener
     */
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        Surface surface = new Surface(surfaceTexture);

        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnCompletionListener(mCompletionListener);
            mMediaPlayer.setOnPreparedListener(mPreparedListener);
            mMediaPlayer.setLooping(isLooping);
            mMediaPlayer.setDataSource(getContext(), mSource);
            mMediaPlayer.setSurface(surface);
            mMediaPlayer.prepare();

            //mMediaPlayer.start();

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            mMediaPlayer.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        surface.release();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {}


}

