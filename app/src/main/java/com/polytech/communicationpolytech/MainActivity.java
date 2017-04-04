package com.polytech.communicationpolytech;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CardView cardView=(CardView) findViewById(R.id.cardview);

        ImageView imageView=(ImageView) cardView.findViewById(R.id.thumbnail);

        Glide.with(this).load(R.drawable.polytech).into(imageView);

    }
}
