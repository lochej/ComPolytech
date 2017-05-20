package com.polytech.communicationpolytech;

import android.content.Intent;
import android.media.MediaPlayer;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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


        CoordinatorLayout coordinatorLayout=(CoordinatorLayout) findViewById(R.id.videoViewer_maincontent);

        videoView=(VideoView) findViewById(R.id.videoViewer_videoview);
        mediaController = new MediaController(this);

        /*
        playFab=(FloatingActionButton) findViewById(R.id.videoViewer_playFab);
        playFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoView.start();
                playFab.setVisibility(View.GONE);
            }
        });
        */

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                //playFab.setVisibility(View.VISIBLE);
                mediaController.show(0);
            }
        });

        videoView.setMediaController(mediaController);
        mediaController.setAnchorView(coordinatorLayout);




        //Récupération des données de la vidéo cliquée
        if(getIntent()!=null){
            Intent data=getIntent();
            final File videoFile=(File) data.getSerializableExtra(Constants.EXTRA_VIDEO_PATH);
            final int millis=data.getIntExtra(Constants.EXTRA_VIDEO_MILLIS,0);

            String newTitle=String.format("%s: %s",getString(R.string.video),videoFile.getName());

            setTitle(newTitle);

            //playFab.setVisibility(View.GONE);
            videoView.setVideoPath(videoFile.getAbsolutePath());



            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.seekTo(millis);
                    int h=mp.getVideoHeight();
                    int w=mp.getVideoWidth();


                    ViewGroup.LayoutParams params=videoView.getLayoutParams();
                    //Si la hauteur est supérieur à la largeur, on veut coller la video aux bords haut et bas de l'ecran et laisser les cotés en wrap
                    if(h>=w){
                        params.width=ViewGroup.LayoutParams.WRAP_CONTENT;
                        params.height=ViewGroup.LayoutParams.MATCH_PARENT;
                    }
                    //Si la video est plus large que haute, on colle les cotés aux bords lateraux et on wrap le haut et le bas
                    if(w>=h){
                        params.height=ViewGroup.LayoutParams.WRAP_CONTENT;
                        params.width=ViewGroup.LayoutParams.MATCH_PARENT;
                    }


                    //Mise a jour du layout
                    videoView.requestLayout();

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
