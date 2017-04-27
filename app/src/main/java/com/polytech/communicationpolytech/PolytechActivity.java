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
        for(int i=0;i<recyclerView.getChildCount();i++){
            FileRecyclerAdapter.PdfViewHolder holder = (FileRecyclerAdapter.PdfViewHolder) recyclerView.getChildViewHolder(recyclerView.getChildAt(i));
            if(holder.getPdfThumbTask() != null && holder.getPdfThumbTask().getStatus() == AsyncTask.Status.RUNNING ){
                holder.getPdfThumbTask().cancel(true);
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
        }

        @Override
        protected void onPostExecute(List<FileItem> fileItems) {
            FileRecyclerAdapter adapter=new FileRecyclerAdapter(PolytechActivity.this,fileItems);
            recyclerView.setAdapter(adapter);
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
                final FileItem itemtoAdd=new FileItem(
                        foundFiles[i].getName(),
                        R.drawable.ic_picture_as_pdf_black_24dp,
                        foundFiles[i]);

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


                //Log.d("load task",foundFiles[i].getAbsolutePath());
            }


            return fileitems;
        }

        Bitmap getPdfThumbnail(File pdfFile) throws IOException {

            ParcelFileDescriptor fd = getContentResolver().openFileDescriptor(Uri.fromFile(pdfFile), "r");;
            int pageNum = 0;
            PdfDocument pdfDocument = pdfiumCore.newDocument(fd);

            pdfiumCore.openPage(pdfDocument, pageNum);

            int width = pdfiumCore.getPageWidthPoint(pdfDocument, pageNum);
            int height = pdfiumCore.getPageHeightPoint(pdfDocument, pageNum);

            Bitmap bitmap = Bitmap.createBitmap(width, height,
                    Bitmap.Config.ARGB_8888);

            pdfiumCore.renderPageBitmap(pdfDocument, bitmap, pageNum, 0, 0,
                    width, height);


            pdfiumCore.closeDocument(pdfDocument); // important!

            return bitmap;

        }
    }

}
