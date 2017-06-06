package com.polytech.communicationpolytech;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.TreeMap;


/**
 * Created by jeloc on 06/06/2017.
 */

public class CSVEntryArrayAdapter extends ArrayAdapter<CSVformatter.CSVEntry> {

    int layoutid;
    Context mContext;
    TreeMap<String,CSVformatter.CSVEntry> entryTreeMap;
    ArrayList<String> keys;

    @Nullable
    @Override
    public CSVformatter.CSVEntry getItem(int position) {
        return entryTreeMap.get(keys.get(position));
    }


    public CSVEntryArrayAdapter(@NonNull Context context, @LayoutRes int resource, TreeMap<String,CSVformatter.CSVEntry> entryTreeMap) {
        super(context, resource);
        layoutid=resource;
        mContext=context;
        this.entryTreeMap=entryTreeMap;
        setValues();
    }

    @NonNull
    @Override
    public View getView(int position,@Nullable View convertView, @NonNull ViewGroup parent) {


        if(convertView==null){
            convertView = LayoutInflater.from(mContext).inflate(layoutid,parent,false);
        }

        final View view=convertView;

        final CSVformatter.CSVEntry entry=entryTreeMap.get(keys.get(position));

        TextView nom=(TextView) view.findViewById(R.id.csv_nom);
        TextView prenom=(TextView) view.findViewById(R.id.csv_prenom);
        TextView email=(TextView) view.findViewById(R.id.csv_email);
        TextView news=(TextView) view.findViewById(R.id.csv_news);
        TextView study=(TextView) view.findViewById(R.id.csv_study);
        Button delete=(Button) view.findViewById(R.id.csv_delete);

        nom.setText(entry.getNom());
        prenom.setText(entry.getPrenom());
        email.setText(entry.getMail());
        news.setText(entry.getNews());
        study.setText(entry.getStudy());

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                remove(entry);
            }
        });


        return view;
    }


    @Override
    public void remove(@Nullable CSVformatter.CSVEntry object) {
        entryTreeMap.remove(object.getMail());
        setValues();
    }


    private void setValues(){
        keys=new ArrayList<>(entryTreeMap.keySet());
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return keys.size();
    }
}
