package com.polytech.communicationpolytech;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.api.services.drive.model.File;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeMap;


public class DownloadFilesAdapter extends ArrayAdapter {

    Context context;
    TreeMap<String,File> Drive_Treemap;
    TreeMap<String,GoogleSyncActivity.DownloadStateWrapper> toDownloadFiles;
    LayoutInflater inflater;
    ArrayList<String> values;
    LinearLayout header;

    public DownloadFilesAdapter(@NonNull Context context, @LayoutRes int resource, LinearLayout header, TreeMap<String,File> Drive_Treemap, TreeMap<String,GoogleSyncActivity.DownloadStateWrapper> toDownloadFiles) {
        super(context, resource);

        this.context=context;
        this.Drive_Treemap=Drive_Treemap;
        this.toDownloadFiles=toDownloadFiles;
        inflater=LayoutInflater.from(context);
        this.header=header;
        setupValues();
        setHeader();
    }

    Comparator<GoogleSyncActivity.DownloadStateWrapper> stateWrapperComparator=new Comparator<GoogleSyncActivity.DownloadStateWrapper>() {
        @Override
        public int compare(GoogleSyncActivity.DownloadStateWrapper o1, GoogleSyncActivity.DownloadStateWrapper o2) {
            boolean isToDl1=o1.isToDownload();
            boolean isToDl2=o2.isToDownload();

            if(isToDl1 && isToDl2){
                return 0;
            }

            if(isToDl1){
                return -1;
            }
            if(isToDl2){
                return 1;
            }
            return 0;
        }
    };

    Comparator<String> upToDl=new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {

            return stateWrapperComparator.compare(toDownloadFiles.get(o1),toDownloadFiles.get(o2));
        }
    };

    private void setHeader(){

        header.removeAllViews();

        View header=inflater.inflate(R.layout.download_list_header,this.header,true);


        CheckBox checkAll=(CheckBox) header.findViewById(R.id.check_all_checkBox);

        final CheckBox checkNew=(CheckBox) header.findViewById(R.id.check_nonExistant_checkBox);

        checkAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                for(String ID : toDownloadFiles.keySet()){
                    toDownloadFiles.get(ID).setToDownload(isChecked);

                }
                checkNew.setChecked(isChecked);
                notifyDataSetChanged();
            }
        });

        checkNew.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                for(String ID : toDownloadFiles.keySet()){
                    GoogleSyncActivity.DownloadStateWrapper wrapper=toDownloadFiles.get(ID);

                    //Si le fichier n'existe pas alors il est nouveau !
                    if(!wrapper.alreadyExists()){
                        wrapper.setToDownload(isChecked);
                    }
                }
                notifyDataSetChanged();
            }
        });

        //On selectrionne par defaut les nouveau fichiers
        checkNew.setChecked(true);

    }


    private void setupValues(){
        values=new ArrayList<>(toDownloadFiles.keySet());
        Collections.sort(values,upToDl);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if(convertView==null){
            convertView=inflater.inflate(R.layout.download_item,parent,false);
        }


        final String id=getItem(position);

        CheckBox cb=(CheckBox) convertView.findViewById(R.id.dl_checkBox);

        final GoogleSyncActivity.DownloadStateWrapper wrapper=toDownloadFiles.get(id);

        boolean isChecked=wrapper.isToDownload();


        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                wrapper.setToDownload(isChecked);
            }
        });

        cb.setChecked(isChecked);

        cb.setTextColor(ContextCompat.getColor(convertView.getContext(),wrapper.alreadyExists() ? android.R.color.black : R.color.greenLock));

        cb.setText(Drive_Treemap.get(id).getName());
        return convertView;

    }

    @Override
    public int getCount() {
        return values.size();
    }


    @Nullable
    @Override
    public String getItem(int position) {
        return values.get(position);
    }
}
