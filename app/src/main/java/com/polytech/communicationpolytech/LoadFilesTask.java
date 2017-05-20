package com.polytech.communicationpolytech;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jeloc on 08/05/2017.
 */

public class LoadFilesTask extends AsyncTask<File,Void,List<FileItem>> {

    Context context;
    ProgressDialog progressDialog;
    RecyclerView recyclerView;

    public LoadFilesTask(Context context,RecyclerView toFillRecyclerView,@Nullable ProgressDialog progressDialog) {
        this.context=context;
        this.recyclerView=toFillRecyclerView;

        this.progressDialog=progressDialog;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //Show the placeholder Dialog
        if(progressDialog!=null) progressDialog.show();
    }

    @Override
    protected void onPostExecute(List<FileItem> fileItems) {
        final FileRecyclerAdapter adapter=new FileRecyclerAdapter(context,fileItems);
        recyclerView.setAdapter(adapter);

        /*
        ((GridLayoutManager)recyclerView.getLayoutManager()).setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if(adapter.getItemViewType(position)== Constants.TYPE_VIDEO || adapter.getItemViewType(position)== Constants.TYPE_SEPARATOR ){
                    return 2;
                }
                return 1;
            }
        });
        */

        ((GridLayoutManager)recyclerView.getLayoutManager()).setSpanSizeLookup(new FileRecyclerAdapter.SpanSizeLookup(adapter));

        //Hide the placeholder progress dialog
        if(progressDialog!=null) progressDialog.hide();
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
    protected List<FileItem> doInBackground(File... params) {



        //File externalDir= context.getExternalFilesDir(null); //Prend la racine du repertoire privé de l'app sur la carte sd

        File externalDir=params[0];

        ArrayList<FileItem> fileitems=new ArrayList<>();

        fillFileItemListFromFolder(fileitems,externalDir,true);

        return fileitems;
    }


    private void fillFileItemListFromFolder(ArrayList<FileItem> fileitems, File rootFolder,boolean isRoot){

        File[] foundFiles=rootFolder.listFiles();

        //Ajouter un séparateur
        if(!isRoot && (foundFiles.length > 0)){
            FileItem test = new FileItem(rootFolder.getName(), 0, null);
            test.setType(Constants.TYPE_SEPARATOR);
            fileitems.add(test);
        }


        for(int i=0;i<foundFiles.length;i++) {

            final File fileToAdd = foundFiles[i];

            int fileType = getTypeByFile(fileToAdd);

            final FileItem itemtoAdd;

            switch (fileType) {
                case Constants.TYPE_IMAGE:

                    itemtoAdd = new FileItem(
                            fileToAdd.getName(),
                            R.drawable.ic_image_black_24dp,
                            fileToAdd);

                    itemtoAdd.setListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent startImageViewer = new Intent(v.getContext(), ImageViewerActivity.class);

                            startImageViewer.putExtra(Constants.EXTRA_VIDEO_PATH, fileToAdd);

                            v.getContext().startActivity(startImageViewer);
                        }
                    });

                    itemtoAdd.setType(Constants.TYPE_IMAGE);

                    fileitems.add(itemtoAdd);

                    break;

                case Constants.TYPE_PDF:

                    itemtoAdd = new FileItem(
                            fileToAdd.getName(),
                            R.drawable.ic_picture_as_pdf_black_24dp,
                            fileToAdd);

                    itemtoAdd.setListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);

                            intent.setDataAndType(
                                    Uri.parse("file://" + itemtoAdd.getFile().getAbsolutePath()),
                                    "application/pdf");

                            v.getContext().startActivity(intent);
                        }
                    });

                    itemtoAdd.setType(Constants.TYPE_PDF);

                    fileitems.add(itemtoAdd);

                    break;
                case Constants.TYPE_VIDEO:

                    itemtoAdd = new FileItem(
                            fileToAdd.getName(),
                            R.drawable.ic_video_library_black_24dp,
                            fileToAdd);


                    itemtoAdd.setType(Constants.TYPE_VIDEO);

                    fileitems.add(itemtoAdd);



                    break;

                case Constants.TYPE_FOLDER:

                    //Recursively fill the array with the files from this folder
                    fillFileItemListFromFolder(fileitems,fileToAdd,false);

                    break;


                default:
            }
        }
    }


    int getTypeByFile(File f){



        //Si c'est un dossier on retourne tout de suite
        if(f.isDirectory()){
            return Constants.TYPE_FOLDER;
        }

        if(f.isFile()){
            //Sinon c'est un fichier, on peut etudier son extension
            String fileName=f.getName();
            int extensionDotIndex=fileName.lastIndexOf('.');

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
        }


        return Constants.TYPE_UNKNOWN;
    }

}