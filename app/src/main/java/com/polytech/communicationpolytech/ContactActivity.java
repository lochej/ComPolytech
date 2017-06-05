package com.polytech.communicationpolytech;

import android.graphics.drawable.Drawable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.TreeMap;

public class ContactActivity extends AppCompatActivity {

    public final String TAG=ContactActivity.this.getClass().getSimpleName();

    EditText nom;
    EditText prenom;
    EditText mail;
    TextView titlenom;
    TextView titleprenom;
    TextView titlemail;
    Spinner study;
    Spinner newsletter;
    Drawable mailbg;
    CoordinatorLayout content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        setTitle(R.string.contact_me);

        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        nom=(EditText) findViewById(R.id.contact_nameEditText);
        prenom=(EditText) findViewById(R.id.contact_firstNameEditText);
        mail=(EditText) findViewById(R.id.contact_mailEditText);
        study=(Spinner) findViewById(R.id.contact_spinner_from);
        newsletter=(Spinner) findViewById(R.id.contact_spinner_news);


        titlemail=(TextView) findViewById(R.id.contact_titleEmail);
        titlemail.setText(getString(R.string.e_mail)+getString(R.string.star));
        mailbg=mail.getBackground();

        content=(CoordinatorLayout) findViewById(R.id.contact_main_coordinator);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public void OnConfirmForm(View v){

        String[] data=new String[5];
        String nom=this.nom.getText().toString();
        String prenom=this.prenom.getText().toString();
        String mail=this.mail.getText().toString();
        String study=((TextView)this.study.getSelectedView()).getText().toString();
        String newsletter=((TextView)this.newsletter.getSelectedView()).getText().toString();



        if(isValidEmailAddress(mail)){
            Snackbar snack=Snackbar.make(content,"OK",Snackbar.LENGTH_SHORT);
            snack.getView().setBackgroundColor(ContextCompat.getColor(v.getContext(),R.color.greenLock));
            snack.show();


            CSVformatter.CSVEntry entry=new CSVformatter.CSVEntry(nom,prenom,newsletter,study,mail);
            data[0]=prenom;
            data[1]=nom;
            data[2]=mail;
            data[3]=study;
            data[4]=newsletter;

            try {
                saveCSVFile(null,false,entry);
                Toast.makeText(v.getContext(),"Votre demande de renseignements a été prise en compte",Toast.LENGTH_LONG).show();
                finish();

            }
            catch(RuntimeException e){
                Toast.makeText(this,"Une demande comportant les mêmes informations à déjà été prise en compte",Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
            catch (IOException e) {

                e.printStackTrace();
            }

        }
        else {

            Snackbar snack=Snackbar.make(content,R.string.incorrect_email_adress,Snackbar.LENGTH_LONG);
            snack.getView().setBackgroundColor(ContextCompat.getColor(v.getContext(),R.color.redLock));
            snack.setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
            snack.show();
        }


        //data=String.format("%s;%s;%s;%s;%s", nom,prenom,mail,"","");
        //Toast.makeText(v.getContext(),data,Toast.LENGTH_SHORT).show();
    }

    public static boolean isValidEmailAddress(String email) {
        return email.matches("(.*)@(.*)");
    }


    public void saveCSVFile(File intoFile,boolean append,String[] line) throws IOException {

        File externalDir=getExternalFilesDir(null);

        File csvFile=new File(externalDir.getAbsolutePath() + "/formulaire.csv");

        int format=CSVformatter.FORMAT_CUSTOM;

        if(!csvFile.exists()){


            CSVformatter.writeHeaderToOutputStream(csvFile,format);
            CSVformatter.writeLineDataToFile(csvFile,line,format);
        }
        else{

            TreeMap<String,CSVformatter.CSVEntry> entries=CSVformatter.extractTreemap(csvFile);


            CSVformatter.writeLineDataToFile(csvFile,line,format);
        }



    }

    public void saveCSVFile(File intoFile, boolean append, CSVformatter.CSVEntry entry) throws IOException {

        File externalDir=getExternalFilesDir(null);

        File csvFile=new File(externalDir.getAbsolutePath() + "/formulaire.csv");

        int format=CSVformatter.FORMAT_CUSTOM;



        if(!csvFile.exists()){


            CSVformatter.writeHeaderToOutputStream(csvFile,format);
            CSVformatter.writeLineDataToFile(csvFile,entry,format);
        }
        else{

            TreeMap<String,CSVformatter.CSVEntry> entries=CSVformatter.extractTreemap(csvFile);


            if(entry.equals(entries.get(entry.getMail()))){
                throw new RuntimeException("Entry already exists");
                //Toast.makeText(this,"Une demande identique existante",Toast.LENGTH_LONG).show();
            }
            else{
                entries.put(entry.getMail(),entry);
                CSVformatter.writeTreemapToFile(csvFile,entries,format);
            }


        }

    }

}
