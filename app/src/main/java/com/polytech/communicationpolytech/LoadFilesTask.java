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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by jeloc on 08/05/2017.
 */

public class LoadFilesTask extends AsyncTask<File,Void,List<FileItem>> {

    Context context;
    ProgressDialog progressDialog;
    RecyclerView recyclerView;

    //Descend les dossier dans la liste et les trie par ordre alphabétique
    public static Comparator<File> pullDownFoldersAlpha= new Comparator<File>() {
        @Override
        public int compare(File o1, File o2) {

            // 0 si o1==o2 , 1 si o1 > o2 , -1 si o1<o2

            //si o1 est un dossier il va descendre dans la liste il vient apres o2
            if(o1.isDirectory()){
                return 1;
            }

            //si o2 est un dossier il vient apres o1 dans la liste
            if(o2.isDirectory()){
                return -1;
            }

            if(o1.isDirectory() && o2.isDirectory()){
                return -o1.getName().compareTo(o2.getName());
            }

            int type1=getTypeByFile(o1);

            int type2=getTypeByFile(o2);

            //Si le 1 est une video on la remonte donc il vient avant o2
            if(type1==Constants.TYPE_VIDEO){
                return -1;
            }

            //Si le 2 est une vidéo on la remonte donc il vient avant o1
            if(type2==Constants.TYPE_VIDEO){
                return 1;
            }


            return o1.getName().compareTo(o2.getName());
        }
    };

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
        setAdapter(recyclerView,fileItems);

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


    public static void fillFileItemListFromFolder(ArrayList<FileItem> fileitems, File rootFolder,boolean isRoot){

        File[] foundFiles=rootFolder.listFiles();

        if(foundFiles==null){
            return;
        }

        Arrays.sort(foundFiles,pullDownFoldersAlpha);

        //Ajouter un séparateur
        if(!isRoot && (foundFiles.length > 0)){
            FileItem test = new FileItem(Constants.TYPE_SEPARATOR,rootFolder);
            fileitems.add(test);
        }


        for(int i=0;i<foundFiles.length;i++) {

            final File fileToAdd = foundFiles[i];

            int fileType = getTypeByFile(fileToAdd);

            final FileItem itemtoAdd;

            switch (fileType) {
                case Constants.TYPE_IMAGE:

                    itemtoAdd = new FileItem(
                            Constants.TYPE_IMAGE,
                            fileToAdd);
                    /*
                    itemtoAdd.setListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent startImageViewer = new Intent(v.getContext(), ImageViewerActivity.class);

                            startImageViewer.putExtra(Constants.EXTRA_VIDEO_PATH, fileToAdd);

                            v.getContext().startActivity(startImageViewer);
                        }
                    });
                    */

                    fileitems.add(itemtoAdd);

                    break;

                case Constants.TYPE_PDF:

                    itemtoAdd = new FileItem(
                            Constants.TYPE_PDF,
                            fileToAdd);

                    /*
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
                    */

                    fileitems.add(itemtoAdd);

                    break;
                case Constants.TYPE_VIDEO:

                    itemtoAdd = new FileItem(
                            Constants.TYPE_VIDEO,
                            fileToAdd);


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


    public static int getTypeByFile(File f){



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

    public static void setAdapter(RecyclerView recyclerView, List<FileItem> fileItems){
        final FileRecyclerAdapter adapter=new FileRecyclerAdapter(recyclerView.getContext(),fileItems);
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
    }


}