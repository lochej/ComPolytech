package com.polytech.communicationpolytech;

import android.content.Intent;
import android.media.MediaPlayer;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.File;

public class VideoViewerActivity extends AppCompatActivity {




    private MediaController mediaController;
    private FloatingActionButton playFab;
    private VideoView videoView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_viewer);
        setTitle(R.string.video);

        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }





        videoView=(VideoView) findViewById(R.id.videoViewer_videoview);
        mediaController = new MediaController(this);


        playFab=(FloatingActionButton) findViewById(R.id.videoViewer_playFab);
        playFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoView.start();
                playFab.setVisibility(View.GONE);
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playFab.setVisibility(View.VISIBLE);
            }
        });

        videoView.setMediaController(mediaController);




        //Récupération des données de la vidéo cliquée
        if(getIntent()!=null){
            Intent data=getIntent();
            final File videoFile=(File) data.getSerializableExtra(Constants.EXTRA_VIDEO_PATH);
            final int millis=data.getIntExtra(Constants.EXTRA_VIDEO_MILLIS,0);

            String newTitle=String.format("%s: %s",getString(R.string.video),videoFile.getName());

            setTitle(newTitle);

            playFab.setVisibility(View.GONE);
            videoView.setVideoPath(videoFile.getAbsolutePath());

            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.seekTo(millis);
                    mp.start();
                }
            });

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}
