package com.polytech.communicationpolytech;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.api.services.drive.model.File;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by jeloc on 12/06/2017.
 */

public class DownloadFilesAdapter2 extends RecyclerView.Adapter<DownloadFilesAdapter2.DownloadViewHolder> {


    Context context;
    TreeMap<String,File> Drive_Treemap;
    TreeMap<String,Boolean> toDownloadFiles;
    LayoutInflater inflater;
    ArrayList<String> values;


    public DownloadFilesAdapter2(Context context, TreeMap<String,File> Drive_Treemap, TreeMap<String,Boolean> toDownloadFiles) {
        this.context=context;
        this.Drive_Treemap=Drive_Treemap;
        this.toDownloadFiles=toDownloadFiles;
        inflater=LayoutInflater.from(context);
        setupValues();
    }

    private void setupValues(){
        values=new ArrayList<>(toDownloadFiles.keySet());
    }


    @Override
    public DownloadViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.download_item,parent,false);
        DownloadViewHolder holder=new DownloadViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(DownloadViewHolder holder, int position) {
        holder.setData(position);
    }

    /*
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((DownloadViewHolder)holder).setData(position);
    }*/

    @Override
    public int getItemCount() {
        return values.size();
    }

    public class DownloadViewHolder extends RecyclerView.ViewHolder{


        private TextView title;
        private CheckBox downloadCheckBox;
        private View itemView;

        public DownloadViewHolder(View itemView) {
            super(itemView);
            this.itemView=itemView;
            //title=(TextView) itemView.findViewById(R.id.dl_filenameTv);
            downloadCheckBox=(CheckBox) itemView.findViewById(R.id.dl_checkBox);

        }


        public void setData(int position) {

            final String id=values.get(position);

            boolean isChecked=toDownloadFiles.get(id);


            downloadCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        toDownloadFiles.put(id,true);
                    }
                    else{
                        toDownloadFiles.put(id,false);
                    }
                }
            });

            downloadCheckBox.setChecked(isChecked);


            downloadCheckBox.setText(Drive_Treemap.get(id).getName());

        }
    }


}
