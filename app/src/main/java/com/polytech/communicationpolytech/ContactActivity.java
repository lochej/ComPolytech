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
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ContactActivity extends AppCompatActivity {

    public final String TAG=ContactActivity.this.getClass().getSimpleName();

    EditText nom;
    EditText prenom;
    EditText mail;
    TextView titlenom;
    TextView titleprenom;
    TextView titlemail;
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

        String[] data=new String[3];
        String nom=this.nom.getText().toString();
        String prenom=this.prenom.getText().toString();
        String mail=this.mail.getText().toString();

        if(isValidEmailAddress(mail)){
            Snackbar snack=Snackbar.make(content,"OK",Snackbar.LENGTH_SHORT);
            snack.getView().setBackgroundColor(ContextCompat.getColor(v.getContext(),R.color.greenLock));
            snack.show();

            data[0]=prenom;
            data[1]=nom;
            data[2]=mail;

            try {
                saveCSVFile(null,false,data);
            } catch (IOException e) {
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

        if(!csvFile.exists()){
            CSVformatter.writeHeaderToOutputStream(csvFile,CSVformatter.FORMAT_GOOGLE);
            CSVformatter.writeLineDataToFile(csvFile,line,CSVformatter.FORMAT_GOOGLE);
        }
        else{
            CSVformatter.writeLineDataToFile(csvFile,line,CSVformatter.FORMAT_GOOGLE);
        }



    }

}
