package com.polytech.communicationpolytech;

import android.content.DialogInterface;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class CSVConsultActivity extends AppCompatActivity implements CSVEntryArrayAdapter.OnMapUpdateListener {

    static final String KEY_MAP="mapKey";
    static final String KEY_LAST_SIZE="lastSize";


    TreeMap<String,CSVformatter.CSVEntry> map;
    File csvFile;
    CoordinatorLayout content;
    ListView listView;

    String title="Formulaire de contact";


    int lastSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_csvconsult);

        setTitle("Formulaire de contact");
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        content=(CoordinatorLayout) findViewById(R.id.csvviewer_content);

        csvFile=new File(getExternalFilesDir(null),Constants.CSV_FILENAME);

        listView=(ListView) findViewById(R.id.csvviewer_listview);


        if(savedInstanceState !=null){
            map=(TreeMap<String,CSVformatter.CSVEntry>) savedInstanceState.getSerializable(KEY_MAP);
            lastSize=savedInstanceState.getInt(KEY_LAST_SIZE);
            if(map !=null){
                setMapAdapter();
                return;
            }

        }

        try {
            loadMap();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void OnMapUpdate(TreeMap<String, CSVformatter.CSVEntry> map) {
        setTitle(title + ": " + map.size() + " " +(map.size() <= 1 ? "entrée" : "entrées") );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.menu_save_changes:
                askSaveDialog();
                break;
            case R.id.menu_reload:
                reloadMap();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.csv_consult_menu,menu);
        return true;
    }

    private void saveChanges(){

        if(map == null){

            showColoredSnackBar(R.color.redLock,"Données invalides relancez le formulaire.",Snackbar.LENGTH_LONG);

            return;
        }
        try {
            CSVformatter.writeTreemapToFile(csvFile,map,CSVformatter.FORMAT_CUSTOM);

            showColoredSnackBar(R.color.greenLock,"Modifications enregistrées.",Snackbar.LENGTH_LONG);
        } catch (IOException e) {

            e.printStackTrace();

        }

    }

    private void askSaveDialog(){

        //On a pas modifié la treemap en longueur
        if(map.size()==lastSize){
            showSnackBar("Pas de modifications à enregistrer",Snackbar.LENGTH_SHORT);
            return;
        }


        AlertDialog.Builder builder= new AlertDialog.Builder(this);
        builder.setTitle("Enregistrer");
        builder.setMessage("Enregistrer les modifications ?" +
                "\n" +
                "Le fichier existant sera écrasé.");
        builder.setPositiveButton("Écraser le fichier avec les modifications", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveChanges();
            }
        });
        builder.setNegativeButton("Annuler",null);

        builder.create().show();
    }



    private void reloadMap(){
        try {
            loadMap();

            showSnackBar("Données rechargés depuis le fichier.",Snackbar.LENGTH_LONG);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void loadMap() throws FileNotFoundException {
        map=CSVformatter.extractTreemap(csvFile);
        setMapAdapter();
        lastSize=map.size();
    }

    private void setMapAdapter(){
        listView.setAdapter(new CSVEntryArrayAdapter(this,R.layout.csv_entry_item,map,this));
    }

    private void showSnackBar(String message,int length){
        Snackbar.make(content,message,length).show();
    }

    private void showColoredSnackBar(int colorID,String message, int length){
        Snackbar snack=Snackbar.make(content,message,length);
        snack.getView().setBackgroundColor(ContextCompat.getColor(this,colorID));
        snack.show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(KEY_MAP,map);
        outState.putInt(KEY_LAST_SIZE,lastSize);
    }


}
