package com.polytech.communicationpolytech;


import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setSupportActionBar((Toolbar) findViewById(R.id.main_toolbar));

        setTitle(R.string.home);


        ImageView banner=(ImageView) findViewById(R.id.main_imgbanner);
        Glide.with(this).load(R.drawable.img_banner_polytech).into(banner);

        RecyclerView recyclerView=(RecyclerView) findViewById(R.id.main_recyclerview);
        List<HomeItem> HomeObjects=HomeItem.getHomeObjectList(this);

        recyclerView.setAdapter(new HomeRecyclerAdapter(this,HomeObjects));
        recyclerView.setHasFixedSize(true);

        recyclerView.setItemAnimator(new DefaultItemAnimator());

    }

    public void OnClickPolytech(View view){
        Toast.makeText(this,"POLYTECH",Toast.LENGTH_SHORT).show();
    }

    public void OnClickReseau(View view){
        Toast.makeText(this,"RESEAU",Toast.LENGTH_SHORT).show();
    }

    public void OnClickCandidat(View view){
        Toast.makeText(this,"CANDIDAT",Toast.LENGTH_SHORT).show();
    }

    public void OnClickQuizz(View view){
        Toast.makeText(this,"QUIZZ",Toast.LENGTH_SHORT).show();
    }

    public void OnClickSecureAcess(View view){
        //Intent startReservedSpace=new Intent(view.getContext(),ReservedSpaceActivity.class);

        //view.getContext().startActivity(startReservedSpace);

        DialogFragment frag=new DialogFragmentPassword();
        frag.show(getSupportFragmentManager(),"Password");
    }

    public void OnClickContactMe(View view){

        Intent startContactActivity=new Intent(view.getContext(),ContactActivity.class);

        view.getContext().startActivity(startContactActivity);

    }

}
