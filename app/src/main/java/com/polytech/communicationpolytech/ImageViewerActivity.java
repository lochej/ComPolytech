package com.polytech.communicationpolytech;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;

public class ImageViewerActivity extends AppCompatActivity {

    public final String TAG=getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        setTitle(R.string.image);

        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ImageView image=(ImageView) findViewById(R.id.imageviewer_image);

        if(getIntent()!=null){
            Intent data=getIntent();
            File imageFile= (File) data.getSerializableExtra(Constants.EXTRA_IMAGE_PATH);
            Log.d(TAG,imageFile.getAbsolutePath());

            String newTitle=String.format("%s: %s",getString(R.string.image),imageFile.getName());

            setTitle(newTitle);


            Glide.with(this).fromFile().asBitmap().load(imageFile).into(image);
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
