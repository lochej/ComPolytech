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
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
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

public class ReservedSpaceActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {


    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;

    GoogleAccountCredential mCredential;
    private TextView mOutputText;
    private Button mCallApiButton;
    private Button mDlApiButton;
    private EditText mEditText;
    ProgressDialog mCheckForUpdatesProgress;
    ProgressDialog mDlProgress;
    AlertDialog mDownloadStartDialog;
    CoordinatorLayout mMainCoordinatorLayout;
    AlertDialog.Builder builder;
    
    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String BUTTON_TEXT = "Call Drive API";
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { DriveScopes.DRIVE_READONLY };

    private static final String APP_NAME="Communication Polytech App";

    private static java.io.File sdRootFolder;

    private java.io.File reservedFolder;

    private List<File> files_list;

    /**
     * Treemap ID drive, FileDrive
     * stocke dans le cache les fichiers du DRIVE avec leurs metadata
     */
    private TreeMap<String,File> Drive_Treemap =new TreeMap<>();

    /**
     * Treemap ID Drive, FileIO
     * stocke dans le cache l'emplacement où les fichiers du DRIVE devrait arriver
     */
    private TreeMap<String,java.io.File> Storage_Treemap =new TreeMap<>();


    //###################### DRIVE FILE SYNC METHODS ###########################

    /**
     * Recupere les ID des fichiers du DRIVE qui n'existent pas et qui ne sont pas des dossiers
     * Assume que les treemap sont correctent et bien générés
     * @param PickNonExistantFiles: true: prends uniquement les fichiers qui n'existent pas dans le stockage, false: prends tout les fichiers qui ne sont pas des dossiers
     * @return liste: liste des ID DRIVE des fichiers à telecharger
     */
    private List<String> getFilesToDownload(boolean PickNonExistantFiles){

        ArrayList<String> toDownloadFiles=new ArrayList<>();

        //Regarder dans le stockage si les fichiers existent deja
        for(String ID : Drive_Treemap.keySet()){

            java.io.File StorageFile = Storage_Treemap.get(ID);

            //récupération du fichier Drive pour checker le mimetype
            File DriveFile = Drive_Treemap.get(ID);

            //Si le fichier n'existe pas, on le rajoute à la liste des telechargement
            //Si le fichier n'est pas un dossier
            if( !(DriveFile.getMimeType().equals("application/vnd.google-apps.folder") ) ){

                //On ne veut que les fichiers qui ne sont pas deja telechargees
                if(PickNonExistantFiles){
                    //Si le fichier n'a pas été telecharger
                    if(!StorageFile.exists()){
                        //Ajout de l'ID du fichier à telecharger
                        toDownloadFiles.add(ID);
                        Log.d("FILESNonExistant",DriveFile.getName());
                    }

                }
                //Si on veut tout les fichiers telechargeable
                else{
                    toDownloadFiles.add(ID);
                    Log.d("FILESExistant",DriveFile.getName());
                }

            }

        }

        return toDownloadFiles;
    }

    /**
     * Doit etre appelee apres avoir cree Drive_Treemap et Storage_Treemap
     * Telecharge le fichier dont l'ID drive est passé en parametre
     * Utilise le Storage_Treemap.get(ID) pour définir l'emplacement où telecharger le fichier
     * @param ID: l'id drive du fichier à DL
     * @param driveService: le service Google Drive à utiliser pour le telechargement
     * @throws IOException :Erreur de telechargement où dossiers inexistant
     */
    private void downloadDriveFileToStorage(com.google.api.services.drive.Drive driveService,String ID) throws IOException {


        java.io.File StorageFile=Storage_Treemap.get(ID);

        java.io.File StorageFolder = StorageFile.getParentFile();

        //Generation des dossiers de stockage si besoin
        if(!StorageFolder.exists()){
            if(StorageFolder.mkdirs()){
                //Reussi a créer les dossiers
            }
            else{
                throw new RuntimeException("Echec de la création des dossiers:"+StorageFolder.getAbsolutePath());
            }
        }

        //Telechargement du fichier

        OutputStream outputStream = new FileOutputStream(StorageFile);
        Drive.Files.Get request=driveService.files().get(ID);
        //request.getMediaHttpDownloader().setProgressListener(new DownloadProgressListener());
        request.executeMediaAndDownloadTo(outputStream);

        outputStream.close();


    }

    /**
     * Returns the parent storage path on the Google Drive
     * @param mService: drive service
     * @param f: the file to query google drive path
     * @return string: the path string
     */
    private String getParentDriveStorageFilePath(com.google.api.services.drive.Drive mService, File f) throws IOException {

        StringBuilder pathBuilder=new StringBuilder();

        List<File> parents=createFileTreeList(mService,f);
        //On parcours la liste à l'envers car la liste contient les parent du plus loitain au plus proche de la racine
        for(int j=parents.size()-1;j>=0;j--){
            File parent=parents.get(j);
            //Ajouter le nom du dossier au chemin dans la carte SD
            pathBuilder.append(parent.getName()).append("/");
            Log.d("PathBuilder",parents.get(j).getName());
        }
        Log.d("PathBuilder",pathBuilder.toString());


        return pathBuilder.toString();
    }

    /**
     * Crée une liste contenant tout les parents de file
     *
     * @param file : fichier dont on veut récupérer les parents
     * @return : liste des parents, vide si aucun parents, sinon contient tout les parents jusqu'a la racine du drive.
     * Attention les parents sont oragnisés du plus loin au plus proche de la racine
     *
     * @throws IOException
     */
    private List<File> createFileTreeList(com.google.api.services.drive.Drive mService,File file) throws IOException {

        //La liste qui va contenir tout les parents de File.
        List<File> treeList=new ArrayList<>();

        File tempFile=file;

        //Tant que le fichier regardé a un parent, l'ajouter a la liste.
        while(tempFile.getParents()!=null){

            //stocker l'id du parent.
            String parentid=tempFile.getParents().get(0);


            //recuperer les infos du parent
            File parent= Drive_Treemap.get(parentid);



            //Si on a pas déjà le metadata du fichier stocké dans la treemap, tanpis on le retelecharge
            //Ou on passe car c'est le dernier dossier (MON DRIVE)
            if(parent==null){
                break;
                //parent=mService.files().get(parentid).setFields("name,id,parents").execute();
                //Log.d("FileTreeCreator","Contacting Drive API to get:"+parent.getName());
            }


            //Swap du tempFile pour utiliser le parent et rechercher son parent.
            tempFile=parent;


            //Ajouter le parent à la liste.
            treeList.add(tempFile);

        }

        return treeList;
    }

    /**
     * Fills the Storage_Treemap using the currently filled Drive_Treemap
     * @param mService : The service to use to retrieve file name in case it is not already in the Drive_Treemap
     * @throws IOException
     */
    private void fillStorageMapFromDriveMap(com.google.api.services.drive.Drive mService) throws IOException {

        for(String ID : Drive_Treemap.keySet()){

            File DriveFile=Drive_Treemap.get(ID);

            String driveParentPaths=getParentDriveStorageFilePath(mService,DriveFile);

            java.io.File StorageFolder=new java.io.File(sdRootFolder,driveParentPaths);

            java.io.File StorageFile= new java.io.File(StorageFolder, DriveFile.getName());

            Storage_Treemap.put(ID,StorageFile);

            Log.d("FillStorageMap",StorageFile.getAbsolutePath());

        }

    }

    /**
     * Va contacter le Google Drive si possible où emmettre des popup.
     * Scan le google drive, donne le nombre de fichiers telechargeable et demander à l'utilisateur s'il veut telecharger
     *
     */
    private void syncWithGoogle(){

        //Regarder sur le Google Drive les fichiers à DL
        //Donc remplir les treemaps
        getResultsFromApi();


        //Ouvrir un popup avec le nb de fichiers à DL et si on veut DL

        //Lancer le DL si on accepte.

    }

    /**
     *
     */
    private void readCSVEntries(){

    }

    /**
     *
     */
    private void deleteCSVData(){
        java.io.File csvFile=new java.io.File(getExternalFilesDir(null).getAbsolutePath(),Constants.CSV_FILENAME);

        if(csvFile.exists()){
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



        //Mise en place du dialog de checking Google Drive
        mCheckForUpdatesProgress = new ProgressDialog(this);
        mCheckForUpdatesProgress.setCancelable(true);
        mCheckForUpdatesProgress.setMessage(getString(R.string.check_for_updates_drive));

        //Mise en place du Dialog à afficher lors des téléchargement
        mDlProgress = new ProgressDialog(this);
        mDlProgress.setMax(100);
        mDlProgress.setIndeterminate(false);
        mDlProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDlProgress.setTitle(getString(R.string.downloading_Files));
        mDlProgress.setMessage("");
        mDlProgress.setCancelable(true);


        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_google_sync:
                syncWithGoogle();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater=getMenuInflater();

        menuInflater.inflate(R.menu.reserved_space_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }


    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    mOutputText.setText(
                            "This app requires Google Play Services. Please install " +
                                    "Google Play Services on your device and relaunch this app.");
                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
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

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     * @param requestCode The request code passed in
     *     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }


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

        TextView mOutputText;
        Button mCallApiButton;
        Button mDlApiButton;
        Button mClearStorageButton;
        Button mShareCSVButton;
        Button mClearCSVButton;

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

            mOutputText=(TextView) rootView.findViewById(R.id.reserved_output_textview);
            //mCallApiButton=(Button) rootView.findViewById(R.id.reserved_callapi);
            //mDlApiButton=(Button) rootView.findViewById(R.id.reserved_download);
            mClearStorageButton=(Button) rootView.findViewById(R.id.reserved_reset_storage);
            mShareCSVButton = (Button) rootView.findViewById(R.id.reserved_export_csv);
            mClearCSVButton = (Button) rootView.findViewById(R.id.reserved_reset_csv);

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


            /*
            mCallApiButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    parentActivity.mCallApiButton.setEnabled(false);
                    parentActivity.mOutputText.setText("");
                    parentActivity.getResultsFromApi();
                    parentActivity.mCallApiButton.setEnabled(true);
                }
            });

            mDlApiButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(parentActivity.files_list!=null){
                        //File f=files_list.get(Integer.parseInt(mEditText.getText().toString()));


                        String[] ids={parentActivity.files_list.get(0).getId(),parentActivity.files_list.get(1).getId(),parentActivity.files_list.get(3).getId(),parentActivity.files_list.get(4).getId()};

                        //File[] files= {f};



                        if(ids!=null){
                            //Toast.makeText(MainActivity.this,f.getName(),Toast.LENGTH_LONG)
                            //        .show();
                            Toast.makeText(parentActivity,"Demarrage telechargement",Toast.LENGTH_LONG)
                                    .show();

                            parentActivity.new DLfileTask(parentActivity.mCredential)
                                    .execute(ids);
                        }
                    }
                    else{
                        Toast.makeText(parentActivity,"Pas de files recup",Toast.LENGTH_SHORT).show();
                    }
                }
            });
            */

            parentActivity.mOutputText=mOutputText;
            //parentActivity.mDlApiButton=mDlApiButton;
            //parentActivity.mCallApiButton=mCallApiButton;

            return rootView;
        }



        @Override
        public void onStop() {
            super.onStop();
        }


    }

    //############################# HANDLING GOOGLE API #####################################

    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    private void getResultsFromApi() {
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (! isDeviceOnline()) {
            mOutputText.setText("No network connection available.");
            showSnackBar("No network connection available.",Snackbar.LENGTH_LONG);
        } else {
            new MakeRequestTask(mCredential).execute();
        }
    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }



    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }


    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                ReservedSpaceActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }



    //################################ ASYNC TASKS #################################


    /**
     * Param1 file to store files in
     *
     */
    private class DLfileTask extends AsyncTask<String,Bundle,Void> {

        private com.google.api.services.drive.Drive mService=null;
        private final String bundleIndex="index";
        private final String bundlerFileName ="filename";
        private Exception mLastError;
        private java.io.File currentFile;


        public DLfileTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.drive.Drive.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Drive API Android Quickstart")
                    .build();

        }

        @Override
        protected void onProgressUpdate(Bundle... values) {
            super.onProgressUpdate(values);

            Bundle progressValues=values[0];
            int index=progressValues.getInt(bundleIndex);
            String filename=progressValues.getString(bundlerFileName);
            mDlProgress.setMessage(filename);
            mDlProgress.setProgress(index);

        }

        @Override
        protected Void doInBackground(String... files) {

            mDlProgress.setMax(files.length);

            for(int i=0;i<files.length;i++){

                String id=files[i];

                Bundle progressValues=new Bundle(2);
                progressValues.putString(bundlerFileName,Drive_Treemap.get(id).getName());
                progressValues.putInt(bundleIndex,i);

                publishProgress(progressValues);

                currentFile=Storage_Treemap.get(id);

                try {

                    downloadDriveFileToStorage(mService,id);
                    Log.d("DOWNLOADED","Succesfully downloaded:"+Drive_Treemap.get(id).getName() +" | "+ id);
                } catch (IOException e) {

                    //Le téléchargement s'est mal passé, il faut supprimer le fichier courant pour de garder un fichier corrompu
                    currentFile.delete();
                    e.printStackTrace();
                    mLastError=e;
                    Log.e("DOWNLOAD Task",e.getMessage());
                    cancel(true);
                    return null;
                }


                /*
                String DriveStoragePath="";
                try {
                    DriveStoragePath= getParentDriveStorageFilePath(mService,f);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //Creation du dossier de stockage

                java.io.File storageFolder=new java.io.File(String.format("%s/%s",sdFolder.getPath(),DriveStoragePath));

                if(!storageFolder.exists()){
                    if(storageFolder.mkdirs()){
                        //Reussi a créer les dossiers
                    }
                    else{
                        //fail a la création des dossiers
                    }
                }


                //Creation du fichier de stockage dans le dossier de stockage préalablement crée.
                java.io.File storeFile=new java.io.File(storageFolder,f.getName());

                Log.d("main","File path: "+storeFile.getPath());


                OutputStream outputStream= null;
                try {
                    outputStream = new FileOutputStream(storeFile);
                    Drive.Files.Get request=mService.files().get(f.getId());
                    request.getMediaHttpDownloader().setProgressListener(new DownloadProgressListener());
                    request.executeMediaAndDownloadTo(outputStream);

                    outputStream.close();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    cancel(true);
                } catch (IOException e) {
                    e.printStackTrace();
                    cancel(true);
                }
                */

                //

            }

            Bundle progressValues=new Bundle(2);
            progressValues.putString(bundlerFileName,"Finalisation...");
            progressValues.putInt(bundleIndex,files.length);

            publishProgress(progressValues);

            return null;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDlProgress.setProgress(0);
            mDlProgress.show();
            mDlProgress.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    cancel(true);
                }
            });
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mDlProgress.hide();
            showSnackBar("Téléchargement terminé",Snackbar.LENGTH_LONG);
            //Toast.makeText(ReservedSpaceActivity.this,"FILE DOWNLOADED OK",Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onCancelled(Void aVoid) {
            super.onCancelled(aVoid);
            onCancelled();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mDlProgress.hide();
            if(currentFile!=null){
                if(currentFile.exists()){
                    if(currentFile.delete()){
                        showSnackBar(currentFile.getName()+": Supprimé",Snackbar.LENGTH_SHORT);
                    }
                    else{
                        showSnackBar(currentFile.getName() + ": Echec suppression",Snackbar.LENGTH_SHORT);
                    }
                }
            }

            if(mLastError != null){
                showSnackBar("Arrêt du téléchargement: "+ mLastError.getLocalizedMessage(),Snackbar.LENGTH_LONG);
                return;
            }
            showSnackBar("Tous les fichiers n'ont pas été téléchargés",Snackbar.LENGTH_LONG);
            //Toast.makeText(ReservedSpaceActivity.this,"FILE NOT DOWNLOADED KOOOOO",Toast.LENGTH_SHORT).show();
        }
    }


    public class DownloadProgressListener implements MediaHttpDownloaderProgressListener {

        public final String TAG=getClass().getSimpleName();
        @Override
        public void progressChanged(MediaHttpDownloader downloader){

            Log.d(TAG,downloader.getDownloadState()+"");

            switch (downloader.getDownloadState()){

                //Called when file is still downloading
                //ONLY CALLED AFTER A CHUNK HAS DOWNLOADED,SO SET APPROPRIATE CHUNK SIZE
                case MEDIA_IN_PROGRESS:
                    //Add code for showing progress
                    int progress=(int)( 100*downloader.getProgress() );

                    mDlProgress.setProgress(progress);

                    Log.d(TAG,"Download progress: " + progress +"%");
                    break;
                //Called after download is complete
                case MEDIA_COMPLETE:
                    //Add code for download completion

                    mDlProgress.setProgress(100);
                    Log.d(TAG,"Download finished");
                    break;
            }

        }

    }


    /**
     * An asynchronous task that handles the Drive API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.drive.Drive mService = null;
        private Exception mLastError = null;

        MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.drive.Drive.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Drive API Android Quickstart")
                    .build();
        }

        /**
         * Background task to call Drive API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch a list of up to 10 file names and IDs.
         * @return List of Strings describing files, or an empty list if no files
         *         found.
         * @throws IOException
         */
        private List<String> getDataFromApi() throws IOException {
            // Get a list of up to 10 files.
            List<String> fileInfo = new ArrayList<String>();

            String pageToken = null;
            //Lire tout le google drive
            do {
                //Récuperer les fichier de la page
                FileList result = mService.files().list()
                        .setFields("nextPageToken, files(id,name,parents,mimeType)")
                        .setPageToken(pageToken)
                        .execute();


                //Ajouter les fichiers à la Treemap

                List<File> files = result.getFiles();
                files_list=files;
                if (files != null) {
                    for (File file : files) {

                        //Ajouter les metadata à la TreeMap
                        Drive_Treemap.put(file.getId(),file);


                        fileInfo.add(String.format("%s (%s)\n",
                                file.getName(), file.getId()));
                    }

                }

                pageToken = result.getNextPageToken();
            } while (pageToken != null);


            fillStorageMapFromDriveMap(mService);

            /*
            //Get files which doesn't exist currently
            List<String> ToDlid=getFilesToDownload(true);

            //ToDlid=getDownloadableFiles();

            for(String id: ToDlid){
                File fi=Drive_Treemap.get(id);
                Log.d("TODLFiles1",fi.getName());
            }

            //Get all downloaded files regardless they exist or not
            List<String> ToDlid2=getFilesToDownload(false);

            //ToDlid=getDownloadableFiles();

            for(String id: ToDlid2){
                File fi=Drive_Treemap.get(id);
                Log.d("TODLFiles2",fi.getName());
            }
            */

            return fileInfo;
        }


        @Override
        protected void onPreExecute() {
            mOutputText.setText("");
            mCheckForUpdatesProgress.show();
            mCheckForUpdatesProgress.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    cancel(true);
                }
            });
        }

        @Override
        protected void onPostExecute(List<String> output) {
            mCheckForUpdatesProgress.hide();

            /*
            List<String> toDlFiles=getFilesToDownload(true);



            DialogFragment dialog=new DialogConfirmDownload();
            Bundle args= new Bundle();
            args.putString(DialogConfirmDownload.ARG_MESSAGE_STRING,
                    "Il y a " + toDlFiles.size() + " nouveaux fichiers disponibles au téléchargement.");


            dialog.setArguments(args);




            //builder.setMessage("Il y a " + toDlFiles.size() + " nouveaux fichiers disponibles");
            //dialog.setMessage("Il y a " + toDlFiles.size() + " nouveaux fichiers disponibles");
            //builder.create().show();

            dialog.show(getSupportFragmentManager(),"DialogConfirmDownload");
            */

            askDownloadDialog();


            if (output == null || output.size() == 0) {
                mOutputText.setText("No results returned.");
            } else {
                output.add(0, "Data retrieved using the Drive API:");
                mOutputText.setText(TextUtils.join("\n", output));
            }
        }

        @Override
        protected void onCancelled() {
            mCheckForUpdatesProgress.hide();
            showSnackBar("Vérification annulée",Snackbar.LENGTH_SHORT);
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            ReservedSpaceActivity.REQUEST_AUTHORIZATION);
                } else {
                    mOutputText.setText("The following error occurred:\n"
                            + mLastError.getMessage());
                }
            } else {
                mOutputText.setText("Request cancelled.");
            }
        }

        @Override
        protected void onCancelled(List<String> strings) {
            super.onCancelled(strings);
            onCancelled();
        }
    }


    void askDownloadDialog(){

        final List<String> toDlFiles=getFilesToDownload(true);

        builder=new AlertDialog.Builder(this);


        LayoutInflater inflater=this.getLayoutInflater();

        View dialogView= inflater.inflate(R.layout.dialog_confirmdownload,null);

        final CheckBox advancedOptions=(CheckBox) dialogView.findViewById(R.id.downloaddialog_advanced);

        final TextView hintView=(TextView) dialogView.findViewById(R.id.downloaddialog_hint);


        builder.setView(dialogView);
        builder.setCancelable(true);

        //Si il n'y a aucun fichier à télécharger
        if(toDlFiles.size() == 0){
            //Montrer un dialog avec juste retélécharger tout les fichiers et OK
            String messageString="Tous les fichiers sont à jours. Il n'y a pas de nouveaux fichiers à télécharger.";

            builder.setTitle("Aucune mise à jour disponible:");
            builder.setMessage(messageString);

            builder.setPositiveButton("OK", null);


        }
        else{
            //Montrer un dialog avec télécharger les new fichiers, tout retélecharger et Pas maintenant
            String messageString="Il y a " + toDlFiles.size() + " nouveaux fichiers disponible au téléchargement." +
                    "\n"+
                    "Le téléchargement peut prendre du temps ne quittez par l'application.";

            builder.setTitle("Mise à jour disponible");
            builder.setMessage(messageString);

            builder.setPositiveButton("Télécharger les nouveaux fichiers", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {


                    new DLfileTask(mCredential).execute(toDlFiles.toArray(new String[]{}));

                    dialog.dismiss();

                }
            });

            builder.setNegativeButton("Plus tard", null);

        }



        builder.setNeutralButton("Retélécharger tous les fichiers", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                List<String> toDlFiles=getFilesToDownload(false);

                new DLfileTask(mCredential).execute(toDlFiles.toArray(new String[]{}));

                dialog.dismiss();
            }
        });

        AlertDialog dialog= builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                final Button neutral=((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEUTRAL);
                neutral.setTextColor(ContextCompat.getColor(ReservedSpaceActivity.this,R.color.redLock));

                advancedOptions.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked){
                            neutral.setVisibility(View.VISIBLE);
                            hintView.setVisibility(View.VISIBLE);
                        }
                        else{
                            neutral.setVisibility(View.GONE);
                            hintView.setVisibility(View.GONE);
                        }
                    }
                });

                neutral.setVisibility(advancedOptions.isChecked() ? View.VISIBLE:View.GONE);
                hintView.setVisibility(advancedOptions.isChecked() ? View.VISIBLE:View.GONE);
            }
        });

        dialog.show();

    }

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

}
