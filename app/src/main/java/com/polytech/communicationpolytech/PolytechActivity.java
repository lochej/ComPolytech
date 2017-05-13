package com.polytech.communicationpolytech;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;

public class PolytechActivity extends AppCompatActivity {

    final String TAG=getClass().getSimpleName();
    RecyclerView recyclerView;
    LoadFilesTask loadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_polytech);
        setTitle(R.string.polytech_tours);

        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        
        recyclerView=(RecyclerView) findViewById(R.id.polytech_recyclerview);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setSaveEnabled(true);

        loadTask=new LoadFilesTask(this,recyclerView,null);
        loadTask.execute();


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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG,"STOPPING");
        if(loadTask!=null){
            if(loadTask.getStatus() == AsyncTask.Status.RUNNING){
                loadTask.cancel(true);
            }
        }

        //Makes sure that every remaining PdfLoadTask is cancelled onStop.
        for(int i=0;i<recyclerView.getChildCount();i++){

            RecyclerView.ViewHolder holder=recyclerView.getChildViewHolder(recyclerView.getChildAt(i));

            //Si c'est un Holder PDF
            if(holder instanceof FileRecyclerAdapter.PdfViewHolder){

                FileRecyclerAdapter.PdfViewHolder pdfHolder=(FileRecyclerAdapter.PdfViewHolder) holder;

                if(pdfHolder.getPdfThumbTask() != null && pdfHolder.getPdfThumbTask().getStatus() == AsyncTask.Status.RUNNING ){
                    pdfHolder.getPdfThumbTask().cancel(true);
                }
            }

        }

    }


}
