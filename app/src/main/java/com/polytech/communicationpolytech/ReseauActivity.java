package com.polytech.communicationpolytech;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import java.io.File;

public class ReseauActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reseau);
        setTitle(R.string.reseau_polytech);

        File sdRootFolder= getExternalFilesDir(null);

        File reseauFolder= new File(sdRootFolder.getAbsolutePath() + Constants.PATH_RESEAU);

        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Create new fragment and transaction
        Fragment newFragment = RecyclerViewFileFragment.newInstance(0,reseauFolder);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.add(R.id.reseau_container,newFragment);
        // Commit the transaction
        transaction.commit();

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
