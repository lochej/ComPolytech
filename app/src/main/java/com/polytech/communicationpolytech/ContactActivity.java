package com.polytech.communicationpolytech;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.TreeMap;

public class ContactActivity extends AppCompatActivity {

    public final String TAG=ContactActivity.this.getClass().getSimpleName();

    public static final String email_pattern="^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";

    public static final String email_pattern2="(.*)@(.*)";

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
    ImageView invalid;
    ImageView valid;
    File csvFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        setTitle(R.string.contact_me);

        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        File externalDir=getExternalFilesDir(null);

        csvFile=new File(externalDir.getAbsolutePath() + "/formulaire.csv");

        nom=(EditText) findViewById(R.id.contact_nameEditText);
        prenom=(EditText) findViewById(R.id.contact_firstNameEditText);
        mail=(EditText) findViewById(R.id.contact_mailEditText);
        study=(Spinner) findViewById(R.id.contact_spinner_from);
        newsletter=(Spinner) findViewById(R.id.contact_spinner_news);
        invalid=(ImageView) findViewById(R.id.contact_invalid_mail);
        valid=(ImageView) findViewById(R.id.contact_valid_mail);


        titlemail=(TextView) findViewById(R.id.contact_titleEmail);
        titlemail.setText( getString(R.string.e_mail)+" "+getString(R.string.star));
        mailbg=mail.getBackground();

        content=(CoordinatorLayout) findViewById(R.id.contact_main_coordinator);

        mail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setColorsValidity(isValidEmailAddress(s.toString()));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //setColorsValidity(false);
    }

    private void setColorsValidity(boolean valid){
        if(valid){
            //mail.setBackgroundColor(ContextCompat.getColor(this,R.color.greenLock));
            mailbg.setColorFilter(ContextCompat.getColor(this,R.color.greenLock), PorterDuff.Mode.SRC_ATOP);
            this.valid.setVisibility(View.VISIBLE);
            this.invalid.setVisibility(View.GONE);
        }
        else{
           //mail.setBackgroundColor(ContextCompat.getColor(this,R.color.redLock));
            mailbg.setColorFilter(ContextCompat.getColor(this,R.color.redLock), PorterDuff.Mode.SRC_ATOP);
            this.valid.setVisibility(View.GONE);
            this.invalid.setVisibility(View.VISIBLE);
        }
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

        String nom=this.nom.getText().toString().trim();
        String prenom=this.prenom.getText().toString().trim();
        String mail=this.mail.getText().toString().trim();
        String study=((TextView)this.study.getSelectedView()).getText().toString();
        String newsletter=((TextView)this.newsletter.getSelectedView()).getText().toString();


        if(isValidEmailAddress(mail)){
            Snackbar snack=Snackbar.make(content,"OK",Snackbar.LENGTH_SHORT);
            snack.getView().setBackgroundColor(ContextCompat.getColor(v.getContext(),R.color.greenLock));
            snack.show();


            CSVformatter.CSVFormEntry entry=new CSVformatter.CSVFormEntry(nom,prenom,newsletter,study,mail);


            try {
                saveCSVFile(csvFile,false,entry);
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
        return email.matches(email_pattern) && getCharCount(email,'@')==1 ;
    }

    public static int getCharCount(String s,char character){
        int count=0;
        for(int i=0;i<s.length();i++){
            if(s.charAt(i) == character){
                count++;
            }
        }
        return count;
    }

    public void saveCSVFile(File intoFile,boolean append,String[] line) throws IOException {

        File externalDir=getExternalFilesDir(null);

        File csvFile=new File(externalDir.getAbsolutePath(),Constants.CSV_FILENAME);

        int format=CSVformatter.FORMAT_CUSTOM;

        if(!csvFile.exists()){


            CSVformatter.writeHeaderToOutputStream(csvFile,format);
            CSVformatter.writeLineDataToFile(csvFile,line,format);
        }
        else{

            TreeMap<String,CSVformatter.CSVFormEntry> entries=CSVformatter.extractTreemap(csvFile);


            CSVformatter.writeLineDataToFile(csvFile,line,format);
        }



    }

    public void saveCSVFile(File csvFile, boolean append, CSVformatter.CSVFormEntry entry) throws IOException {




        int format=CSVformatter.FORMAT_CUSTOM;



        if(!csvFile.exists()){


            CSVformatter.writeHeaderToOutputStream(csvFile,format);
            CSVformatter.writeLineDataToFile(csvFile,entry,format);
        }
        else{

            TreeMap<String,CSVformatter.CSVFormEntry> entries=CSVformatter.extractTreemap(csvFile);


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
