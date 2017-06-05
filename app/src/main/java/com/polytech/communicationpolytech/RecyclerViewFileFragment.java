package com.polytech.communicationpolytech;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.polytech.communicationpolytech.LoadFilesTask.fillFileItemListFromFolder;
import static com.polytech.communicationpolytech.LoadFilesTask.setAdapter;

/**
 * Created by jeloc on 14/05/2017.
 */

public class RecyclerViewFileFragment extends Fragment {

    public static final int START_LOADING=0;
    public static final int LOADING=1;
    public static final int SUCCESS_LOADING=2;
    public static final int FILE_NOT_FOUND=3;
    public static final String KEY_FILEITEMS="sav_fitem";

    AsyncTask loadTask;
    RecyclerView recyclerView;
    TextView placeholder;
    File rootFile;
    SwipeRefreshLayout refreshLayout;
    Thread loaderThread;
    ArrayList<FileItem> data;

    Handler fileLoadedHandler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.what){

                case START_LOADING:

                    refreshLayout.setRefreshing(true);

                    break;
                case LOADING:
                    break;
                case SUCCESS_LOADING:
                    data=(ArrayList<FileItem>)msg.obj;
                    installDataToRecyclerView();
                    refreshLayout.setRefreshing(false);

                    break;
                case FILE_NOT_FOUND:

                    placeholder.setVisibility(View.VISIBLE);
                    refreshLayout.setRefreshing(false);

                    break;
            }


            return false;
        }
    });


    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    private static final String ARG_FILE="file_java_io";

    public RecyclerViewFileFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static RecyclerViewFileFragment newInstance(int sectionNumber,File rootFile) {
        RecyclerViewFileFragment fragment = new RecyclerViewFileFragment();
        Bundle args = new Bundle();
        //Save infos about the fragment
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        args.putSerializable(ARG_FILE,rootFile);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState!=null){
            data=savedInstanceState.getParcelableArrayList(KEY_FILEITEMS);
            Log.d("RECYCLERFRAG","Reinstall saved file items");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //get the infos on the fragment
        rootFile=(File) getArguments().getSerializable(ARG_FILE);

        View rootView = inflater.inflate(R.layout.fragment_recyclerview, container, false);

        refreshLayout=(SwipeRefreshLayout) rootView.findViewById(R.id.frag_swiperefresh);

        recyclerView=(RecyclerView) rootView.findViewById(R.id.frag_recyclerview);

        placeholder=(TextView) rootView.findViewById(R.id.frag_placeholder);

        /*
        //Le fichier servant à remplir la recyclerview n'existe pas où est invalide donc on ne peut pas remplir la vue
        if(rootFile==null || !rootFile.exists()){
            placeholder.setVisibility(View.VISIBLE);
        }
        else {

            File externalDir=rootFile;

            ArrayList<FileItem> fileitems=new ArrayList<>();

            fillFileItemListFromFolder(fileitems,externalDir,true);

            setAdapter(recyclerView,fileitems);

            //loadTask =new LoadFilesTask(rootView.getContext(),recyclerView,null).execute(rootFile);
        }
        */

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //refreshLayout.setRefreshing(false);
                loadFiles(true);
            }
        });



        loadFiles(false);


        return rootView;
    }


    @Override
    public void onStart() {
        super.onStart();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(KEY_FILEITEMS,data);
        Log.d("RECYCLERFRAG","Saved file items");

    }

    @Override
    public void onStop() {
        super.onStop();

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

                if(pdfHolder.getGenerateBitmapThread() != null && pdfHolder.getGenerateBitmapThread().isAlive()){
                    pdfHolder.getGenerateBitmapThread().interrupt();
                }

                if(pdfHolder.getPdfThumbTask() != null && pdfHolder.getPdfThumbTask().getStatus() == AsyncTask.Status.RUNNING ){
                    pdfHolder.getPdfThumbTask().cancel(true);
                }
            }

            //Si c'est un Holder PDF
            else if(holder instanceof FileRecyclerAdapter.VideoViewHolder){

                FileRecyclerAdapter.VideoViewHolder videoHolder=(FileRecyclerAdapter.VideoViewHolder) holder;

                /*
                if(videoHolder.getGenerateBitmapThread() != null && videoHolder.getGenerateBitmapThread().isAlive()){
                    videoHolder.getGenerateBitmapThread().interrupt();
                }
                */
                
                if(videoHolder.getVideoThumbTask() != null && videoHolder.getVideoThumbTask().getStatus() == AsyncTask.Status.RUNNING ){
                    videoHolder.getVideoThumbTask().cancel(true);
                }
            }

        }
    }

    private void  loadFiles(boolean forceReload){

        if(data !=null && !forceReload){
            setAdapter(recyclerView,data);
            return;
        }

        if(loaderThread == null || !loaderThread.isAlive()){
            loaderThread=new LoadThread(fileLoadedHandler,rootFile);

        }
        //Si le thread est en vie je retourne car il fonctionne deja
        if(loaderThread.isAlive()){
            return;
        }
        //Si le thread n'a pas encore été lancé alors je le lance
        if(loaderThread.getState() == Thread.State.NEW){
            loaderThread.start();
        }

    }

    private void installDataToRecyclerView(){
        if(data!=null){
            setAdapter(recyclerView,data);
        }
    }

    private class LoadThread extends Thread{

        Handler mHandler;
        File rootFile;


        public LoadThread(Handler mHandler, File rootFile) {
            this.mHandler=mHandler;
            this.rootFile=rootFile;
        }


        @Override
        public void interrupt() {
            super.interrupt();
        }

        @Override
        public void run() {

            mHandler.obtainMessage(START_LOADING).sendToTarget();

            //Le fichier servant à remplir la recyclerview n'existe pas où est invalide donc on ne peut pas remplir la vue
            if(rootFile==null || !rootFile.exists()){

                mHandler.obtainMessage(FILE_NOT_FOUND).sendToTarget();
                //faire:
                // placeholder.setVisibility(View.VISIBLE);
                //setRefreshing(false);
            }
            else {

                File externalDir=rootFile;

                ArrayList<FileItem> fileitems=new ArrayList<>();

                fillFileItemListFromFolder(fileitems,externalDir,true);

                mHandler.obtainMessage(SUCCESS_LOADING,fileitems).sendToTarget();


                //setAdapter(recyclerView,fileitems);

                //loadTask =new LoadFilesTask(rootView.getContext(),recyclerView,null).execute(rootFile);
            }

        }
    }

}
