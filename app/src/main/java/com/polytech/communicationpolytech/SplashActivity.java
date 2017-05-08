package com.polytech.communicationpolytech;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.bumptech.glide.Glide;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* //Reset glide cache
        new Thread(new Runnable() {
            @Override
            public void run() {
                Glide.get(SplashActivity.this).clearDiskCache();

            }
        }).start();

        Glide.get(SplashActivity.this).clearMemory();
        */

        Intent startActivity=new Intent(this,MainActivity.class);

        startActivity(startActivity);

        finish();

    }
}
