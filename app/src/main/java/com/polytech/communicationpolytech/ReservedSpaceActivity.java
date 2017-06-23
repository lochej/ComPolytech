package com.polytech.communicationpolytech;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.client.googleapis.media.MediaHttpDownloaderProgressListener;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class ReservedSpaceActivity extends AppCompatActivity{


    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;

    ProgressDialog mCheckForUpdatesProgress;
    ProgressDialog mDlProgress;
    AlertDialog mDownloadStartDialog;
    CoordinatorLayout mMainCoordinatorLayout;

    private static java.io.File sdRootFolder;

    private java.io.File reservedFolder;



    /**
     * Va contacter le Google Drive si possible où emmettre des popup.
     * Scan le google drive, donne le nombre de fichiers telechargeable et demander à l'utilisateur s'il veut telecharger
     *
     */
    private void syncWithGoogle(){

        //Regarder sur le Google Drive les fichiers à DL
        //Donc remplir les treemaps
        //getResultsFromApi();

        Intent startGoogleSyncActivity=new Intent(this,GoogleSyncActivity.class);
        startActivity(startGoogleSyncActivity);

        //Ouvrir un popup avec le nb de fichiers à DL et si on veut DL

        //Lancer le DL si on accepte.

    }

    //########################## APP LIFECYCLE ##########################
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserved_space);
        setTitle(R.string.reserved_space);

        sdRootFolder=getExternalFilesDir(null);

        reservedFolder=new java.io.File(sdRootFolder.getAbsolutePath() + Constants.PATH_RESERVED);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        mMainCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.reserved_activity_container);


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


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mDownloadStartDialog !=null) mDownloadStartDialog.dismiss();
        if(mDlProgress!=null){
            mDlProgress.cancel();
            mDlProgress.dismiss();
        }
        if(mCheckForUpdatesProgress !=null) mCheckForUpdatesProgress.dismiss();
    }


    //##################### METHODS BOUTONS #######################


    void onClickExportCsv(View v){

        java.io.File csvFile=new java.io.File(sdRootFolder.getAbsolutePath() + "/formulaire.csv");

        //Le fichier csv existe
        if(csvFile.exists()){

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);

            sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + csvFile.getAbsolutePath()));
            sendIntent.setType("text/csv");

            startActivity(Intent.createChooser(sendIntent ,"Partager via:"));
        }else{
            showSnackBar("Aucun formulaire à exporter.", Snackbar.LENGTH_LONG);
        }


    }

    private void showSnackBar(String message,int length){
        Snackbar.make(mMainCoordinatorLayout,message,length).show();
    }

    private void showColoredSnackBar(int colorID,String message, int length){
        Snackbar snack=Snackbar.make(mMainCoordinatorLayout,message,length);
        snack.getView().setBackgroundColor(ContextCompat.getColor(this,colorID));
        snack.show();
    }

    private void showSnackBar(int stringid, int length){
        Snackbar.make(mMainCoordinatorLayout, stringid,length).show();
    }

    private void consultCSVData(){
        java.io.File csvFile=new java.io.File(getExternalFilesDir(null).getAbsolutePath(),Constants.CSV_FILENAME);

        if(csvFile.exists()){

            //TreeMap<String,CSVformatter.CSVFormEntry> entryTreemap=CSVformatter.extractTreemap(csvFile);
            Intent startCSV=new Intent(this,CSVConsultActivity.class);
            startActivity(startCSV);

        }
        else{
            showSnackBar("Données de contact inexistante: rien à effacer.",Snackbar.LENGTH_LONG);
        }
    }

    private void deleteCSVData(){
        java.io.File csvFile=new java.io.File(getExternalFilesDir(null).getAbsolutePath(),Constants.CSV_FILENAME);

        if(csvFile.exists()){

            //TreeMap<String,CSVformatter.CSVFormEntry> entryTreemap=CSVformatter.extractTreemap(csvFile);


            if(csvFile.delete()){
                showColoredSnackBar(R.color.greenLock,"Données de contact effacées.",Snackbar.LENGTH_LONG);
            }else{
                showColoredSnackBar(R.color.redLock,"Échec de l'effacement des données de contact.",Snackbar.LENGTH_LONG);
            }
        }
        else{
            showSnackBar("Données de contact inexistante: rien à effacer.",Snackbar.LENGTH_LONG);
        }
    }

    //############################ CLASSES VIEWPAGER FRAGMENT ####################

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a ConfigFragment (defined as a static inner class below).

            if(position==0){
                return ConfigFragment.newInstance(position + 1,ReservedSpaceActivity.this);
            }

            return RecyclerViewFileFragment.newInstance(position+1,reservedFolder);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Configuration";
                case 1:
                    return "Documents Réservés";

            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class ConfigFragment extends Fragment {


        Button mDlActivity;
        Button mClearStorageButton;
        Button mShareCSVButton;
        Button mClearCSVButton;
        Button mConsultCSVButton;

        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        private ReservedSpaceActivity parentActivity;

        public ReservedSpaceActivity getParentActivity() {
            return parentActivity;
        }

        public void setParentActivity(ReservedSpaceActivity parentActivity) {
            this.parentActivity = parentActivity;
        }

        public ConfigFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static ConfigFragment newInstance(int sectionNumber,ReservedSpaceActivity parentActivity) {
            ConfigFragment fragment = new ConfigFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            fragment.setParentActivity(parentActivity);
            fragment.setRetainInstance(true);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {



            View rootView = inflater.inflate(R.layout.fragment_reserved_config, container, false);

            mDlActivity =(Button) rootView.findViewById(R.id.reserved_googleSyncActivity);
            mClearStorageButton=(Button) rootView.findViewById(R.id.reserved_reset_storage);
            mShareCSVButton = (Button) rootView.findViewById(R.id.reserved_export_csv);
            mClearCSVButton = (Button) rootView.findViewById(R.id.reserved_reset_csv);
            mConsultCSVButton=(Button) rootView.findViewById(R.id.reserved_view_csv);

            mShareCSVButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    parentActivity.onClickExportCsv(v);
                }
            });

            mClearCSVButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    parentActivity.deleteCSVData();
                }
            });

            mConsultCSVButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    parentActivity.consultCSVData();
                }
            });


            mDlActivity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent startGoogleSyncActivity=new Intent(v.getContext(),GoogleSyncActivity.class);
                    startActivity(startGoogleSyncActivity);
                }
            });

            mClearStorageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {



                    final Intent i = new Intent();
                    i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    i.addCategory(Intent.CATEGORY_DEFAULT);
                    i.setData(Uri.parse("package:" + v.getContext().getPackageName()));
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    v.getContext().startActivity(i);


                    for(int j=0;j<2;j++)

                    Toast.makeText(v.getContext(),"Cliquez sur STOCKAGE" +
                            "\n" +
                            "EFFACER LES DONNÉES " +
                            "et \n" +
                            "VIDER LE CACHE" +
                            "\n" +
                            " pour effacer toutes les données de l'application",Toast.LENGTH_LONG).show();
                }
            });


            return rootView;
        }



        @Override
        public void onStop() {
            super.onStop();
        }


    }




}
