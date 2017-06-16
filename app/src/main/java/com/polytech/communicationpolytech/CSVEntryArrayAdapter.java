package com.polytech.communicationpolytech;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeMap;


/**
 * Created by jeloc on 06/06/2017.
 */

public class CSVEntryArrayAdapter extends ArrayAdapter<CSVformatter.CSVEntry> {

    int layoutid;
    Context mContext;
    TreeMap<String,CSVformatter.CSVEntry> entryTreeMap;
    ArrayList<String> keys;
    ArrayList<CSVformatter.CSVEntry> values;
    OnMapUpdateListener mMapListener;
    LinearLayout headerContainer;
    TextView entryCount;

    final static int SORT_NOM=0;
    final static int SORT_PRENOM=1;
    final static int SORT_MAIL=2;
    final static int SORT_STUDY=3;
    final static int SORT_NEWS=4;

    int CURRENT_SORTING=SORT_NOM;

    public static interface OnMapUpdateListener{
        void OnMapUpdate(TreeMap<String,CSVformatter.CSVEntry> map);
    }


    Comparator<CSVformatter.CSVEntry> sortByNom=new Comparator<CSVformatter.CSVEntry>() {
        @Override
        public int compare(CSVformatter.CSVEntry o1, CSVformatter.CSVEntry o2) {
            return o1.getNom().compareTo(o2.getNom());
        }
    };

    Comparator<CSVformatter.CSVEntry> sortByPrenom=new Comparator<CSVformatter.CSVEntry>() {
        @Override
        public int compare(CSVformatter.CSVEntry o1, CSVformatter.CSVEntry o2) {
            return o1.getPrenom().compareTo(o2.getPrenom());
        }
    };

    Comparator<CSVformatter.CSVEntry> sortByEmail=new Comparator<CSVformatter.CSVEntry>() {
        @Override
        public int compare(CSVformatter.CSVEntry o1, CSVformatter.CSVEntry o2) {
            return o1.getMail().compareTo(o2.getMail());
        }
    };

    Comparator<CSVformatter.CSVEntry> sortByNews=new Comparator<CSVformatter.CSVEntry>() {
        @Override
        public int compare(CSVformatter.CSVEntry o1, CSVformatter.CSVEntry o2) {
            return o1.getNews().compareTo(o2.getNews());
        }
    };

    Comparator<CSVformatter.CSVEntry> sortByStudy=new Comparator<CSVformatter.CSVEntry>() {
        @Override
        public int compare(CSVformatter.CSVEntry o1, CSVformatter.CSVEntry o2) {
            return o1.getStudy().compareTo(o2.getStudy());
        }
    };

    @Nullable
    @Override
    public CSVformatter.CSVEntry getItem(int position) {
        return values.get(position);
    }


    public CSVEntryArrayAdapter(@NonNull Context context, @LayoutRes int resource, LinearLayout headerContainer,TreeMap<String, CSVformatter.CSVEntry> entryTreeMap, OnMapUpdateListener mMapListener) {
        super(context, resource);
        layoutid=resource;
        mContext=context;
        this.entryTreeMap=entryTreeMap;
        this.mMapListener=mMapListener;
        this.headerContainer =headerContainer;
        addHeaderView();
        setValues();
    }


    private void addHeaderView(){


        LayoutInflater inflater=LayoutInflater.from(mContext);
        View header=inflater.inflate(R.layout.csv_list_header,headerContainer,false);

        Spinner sortChoice=(Spinner) header.findViewById(R.id.csv_list_sortspinner);
        entryCount=(TextView) header.findViewById(R.id.csv_list_nbentries_header);

        sortChoice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                CURRENT_SORTING=position;
                setValues();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        headerContainer.removeAllViews();
        headerContainer.addView(header);



    }

    @NonNull
    @Override
    public View getView(int position,@Nullable View convertView, @NonNull ViewGroup parent) {


        if(convertView==null){
            convertView = LayoutInflater.from(mContext).inflate(layoutid,parent,false);
        }

        final View view=convertView;

        final CSVformatter.CSVEntry entry=values.get(position);

        //TextView nom=(TextView) view.findViewById(R.id.csv_nom);
        //TextView prenom=(TextView) view.findViewById(R.id.csv_prenom);

        TextView nomComplet=(TextView) view.findViewById(R.id.csv_fullName);
        TextView email=(TextView) view.findViewById(R.id.csv_email);
        TextView news=(TextView) view.findViewById(R.id.csv_news);
        TextView study=(TextView) view.findViewById(R.id.csv_study);
        ImageButton delete=(ImageButton) view.findViewById(R.id.csv_delete);

        String prenom=entry.getPrenom().trim();
        String nom=entry.getNom().trim();
        String newsStr =entry.getNews().trim();
        String studyStr= entry.getStudy().trim();


        if(prenom.length()==0 && nom.length()==0){
            nomComplet.setTextColor(ContextCompat.getColor(view.getContext(),R.color.lite_lite_gray));
            nomComplet.setText("Nom inconnu");
        }
        else{
            nomComplet.setTextColor(ContextCompat.getColor(view.getContext(),R.color.navy));
            nomComplet.setText(String.format("%s %s",nom.toUpperCase(),prenom.length() ==0 ? "" : toTitleCase(prenom)));
        }



        if(newsStr.equals("Non renseigné") || newsStr.length() ==0){
            news.setTextColor(ContextCompat.getColor(view.getContext(),R.color.lite_lite_gray));
            news.setText(newsStr);
        }
        else{
            news.setTextColor(ContextCompat.getColor(view.getContext(),R.color.lite_gray));
            news.setText(newsStr);
        }

        if(studyStr.equals("Non renseigné") || studyStr.length()==0){
            study.setTextColor(ContextCompat.getColor(view.getContext(),R.color.lite_lite_gray));
            study.setText(studyStr);
        }
        else{
            study.setTextColor(ContextCompat.getColor(view.getContext(),R.color.gray));
            study.setText(studyStr);
        }

        study.setText(entry.getStudy());

        email.setText(entry.getMail());

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                remove(entry);
            }
        });


        return view;
    }

    public static String toTitleCase(String givenString) {
        String[] arr = givenString.split(" ");
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < arr.length; i++) {
            sb.append(Character.toUpperCase(arr[i].charAt(0)))
                    .append(arr[i].substring(1)).append(" ");
        }
        return sb.toString().trim();
    }

    @Override
    public void remove(@Nullable CSVformatter.CSVEntry object) {
        entryTreeMap.remove(object.getMail());
        setValues();
    }


    private void setValues(){
        //keys=new ArrayList<>(entryTreeMap.keySet());
        values=new ArrayList<>(entryTreeMap.values());

        switch (CURRENT_SORTING){
            case SORT_NOM:
                Collections.sort(values,sortByNom);
                break;
            case SORT_PRENOM:
                Collections.sort(values,sortByPrenom);
                break;
            case SORT_MAIL:
                Collections.sort(values,sortByEmail);
                break;
            case SORT_NEWS:
                Collections.sort(values,sortByNews);
                break;
            case SORT_STUDY:
                Collections.sort(values,sortByStudy);
                break;
            default:

        }



        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return values.size();
    }

    @Override
    public void add(@Nullable CSVformatter.CSVEntry object) {
        super.add(object);
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        mMapListener.OnMapUpdate(entryTreeMap);
        if(entryCount !=null){
            entryCount.setText(""+entryTreeMap.size());
        }
    }
}
