package com.polytech.communicationpolytech;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;

/**
 * Created by jeloc on 14/05/2017.
 */

public class RecyclerViewFileFragment extends Fragment {

    AsyncTask loadTask;
    RecyclerView recyclerView;
    TextView placeholder;
    File rootFile;


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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //get the infos on the fragment
        rootFile=(File) getArguments().getSerializable(ARG_FILE);

        View rootView = inflater.inflate(R.layout.fragment_recyclerview, container, false);



        recyclerView=(RecyclerView) rootView.findViewById(R.id.frag_recyclerview);

        placeholder=(TextView) rootView.findViewById(R.id.frag_placeholder);

        //Le fichier servant à remplir la recyclerview n'existe pas où est invalide donc on ne peut pas remplir la vue
        if(rootFile==null || !rootFile.exists()){
            placeholder.setVisibility(View.VISIBLE);
        }
        else {
            loadTask =new LoadFilesTask(rootView.getContext(),recyclerView,null).execute(rootFile);
        }



        return rootView;
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

                if(pdfHolder.getPdfThumbTask() != null && pdfHolder.getPdfThumbTask().getStatus() == AsyncTask.Status.RUNNING ){
                    pdfHolder.getPdfThumbTask().cancel(true);
                }
            }

        }
    }

}
