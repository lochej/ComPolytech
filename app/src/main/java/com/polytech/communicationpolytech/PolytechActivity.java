package com.polytech.communicationpolytech;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PolytechActivity extends AppCompatActivity {

    final String TAG=getClass().getSimpleName();
    RecyclerView recyclerView;
    ProgressDialog progressDialog;
    LoadFilesTask loadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_polytech);
        setTitle(R.string.polytech_tours);

        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        progressDialog=new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.loading));

        recyclerView=(RecyclerView) findViewById(R.id.polytech_recyclerview);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setSaveEnabled(true);

        loadTask=new LoadFilesTask(this);
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

    public class LoadFilesTask extends AsyncTask<Void,Void,List<FileItem>>{

        Context context;
        PdfiumCore pdfiumCore;

        public LoadFilesTask(Context context) {
            this.context=context;
            pdfiumCore = new PdfiumCore(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Show the placeholder Dialog
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(List<FileItem> fileItems) {
            final FileRecyclerAdapter adapter=new FileRecyclerAdapter(PolytechActivity.this,fileItems);
            recyclerView.setAdapter(adapter);

            ((GridLayoutManager)recyclerView.getLayoutManager()).setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if(adapter.getItemViewType(position)== Constants.TYPE_VIDEO){
                        return 2;
                    };
                    return 1;
                }
            });

            //Hide the placeholder progress dialog
            progressDialog.hide();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onCancelled(List<FileItem> fileItems) {
            super.onCancelled(fileItems);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected List<FileItem> doInBackground(Void... params) {

            File externalDir= getExternalFilesDir(null);
            //Log.d("load task",externalDir.getAbsolutePath());

            File[] foundFiles=externalDir.listFiles();
            ArrayList<FileItem> fileitems=new ArrayList<>();

            for(int i=0;i<foundFiles.length;i++){

                File fileToAdd=foundFiles[i];

                int fileType=getTypeByFile(fileToAdd);

                final FileItem itemtoAdd;

                switch (fileType){
                    case Constants.TYPE_IMAGE:

                        itemtoAdd=new FileItem(
                                fileToAdd.getName(),
                                R.drawable.ic_image_black_24dp,
                                fileToAdd);

                        itemtoAdd.setListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        });

                        itemtoAdd.setType(Constants.TYPE_IMAGE);

                        fileitems.add(itemtoAdd);

                        break;

                    case Constants.TYPE_PDF:

                        itemtoAdd=new FileItem(
                                fileToAdd.getName(),
                                R.drawable.ic_picture_as_pdf_black_24dp,
                                fileToAdd);

                        itemtoAdd.setListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);

                                intent.setDataAndType(
                                        Uri.parse("file://" +itemtoAdd.getFile().getAbsolutePath()),
                                        "application/pdf");

                                startActivity(intent);
                            }
                        });

                        itemtoAdd.setType(Constants.TYPE_PDF);

                        fileitems.add(itemtoAdd);

                        break;
                    case Constants.TYPE_VIDEO:

                        itemtoAdd=new FileItem(
                                fileToAdd.getName(),
                                R.drawable.ic_video_library_black_24dp,
                                fileToAdd);


                        itemtoAdd.setType(Constants.TYPE_VIDEO);

                        fileitems.add(itemtoAdd);

                        break;
                    default:
                }



                //Log.d("load task",foundFiles[i].getAbsolutePath());
            }


            return fileitems;
        }

        int getTypeByFile(File f){

            String fileName=f.getName();
            int extensionDotIndex=fileName.lastIndexOf('.');

            //Pas d'extensions c'est un dossier
            if(extensionDotIndex == -1){
                return Constants.TYPE_FOLDER;
            }
            //Le point est à la fin donc il n'y a pas d'extensions ensuite, c'est un dossier
            if(extensionDotIndex == fileName.length()-1){
                return Constants.TYPE_FOLDER;
            }

            //recupération du texte après le point
            String extension=fileName.substring(extensionDotIndex+1).toUpperCase();

            switch (extension){
                case Constants.EXTENSION_GIF:
                    return Constants.TYPE_IMAGE;

                case Constants.EXTENSION_JPG:
                    return Constants.TYPE_IMAGE;

                case Constants.EXTENSION_PNG:
                    return Constants.TYPE_IMAGE;

                case Constants.EXTENSION_MP4:
                    return Constants.TYPE_VIDEO;

                case Constants.EXTENSION_PDF:
                    return Constants.TYPE_PDF;

            }

            return Constants.TYPE_UNKNOWN;
        }

    }

}
