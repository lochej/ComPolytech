package com.polytech.communicationpolytech;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class CSVConsultActivity extends AppCompatActivity {

    TreeMap<String,CSVformatter.CSVEntry> map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_csvconsult);

        setTitle("Formuaire de contact");
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        File csvFile=new File(getExternalFilesDir(null),Constants.CSV_FILENAME);

        ListView listView=(ListView) findViewById(R.id.csvviewer_listview);

        try {
            map=CSVformatter.extractTreemap(csvFile);
            List<CSVformatter.CSVEntry> entries=new ArrayList<>(map.values());
            listView.setAdapter(new CSVEntryArrayAdapter(this,R.layout.csv_entry_item,map));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.menu_save_changes:
                saveChanges();
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
        Toast.makeText(this,"SAUVER: " + map.size(),Toast.LENGTH_LONG).show();
    }

}
