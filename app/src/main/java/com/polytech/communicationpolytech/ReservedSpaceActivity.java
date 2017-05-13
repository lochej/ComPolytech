package com.polytech.communicationpolytech;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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


import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
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



    GoogleAccountCredential mCredential;
    private TextView mOutputText;
    private Button mCallApiButton;
    private Button mDlApiButton;
    private EditText mEditText;
    ProgressDialog mProgress;
    ProgressDialog mDlProgress;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String BUTTON_TEXT = "Call Drive API";
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { DriveScopes.DRIVE_READONLY };

    private static java.io.File sdRootFolder;

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


    /**
     * Recupere les ID des fichiers du DRIVE qui n'existent pas et qui ne sont pas des dossiers
     * Assume que les treemap sont correctent et bien générés
     * @return liste: liste des ID DRIVE des fichiers à telecharger
     */
    private List<String> getFilesToDownload(){

        ArrayList<String> toDownloadFiles=new ArrayList<>();

        //Regarder dans le stockage si les fichiers existent deja
        for(String ID : Storage_Treemap.keySet()){

            java.io.File StorageFile = Storage_Treemap.get(ID);

            //récupération du fichier Drive pour checker le mimetype
            File DriveFile = Drive_Treemap.get(ID);

            //Si le fichier n'existe pas, on le rajoute à la liste des telechargement
            //Si le fichier n'est pas un dossier
            if( !StorageFile.exists() && !(DriveFile.getMimeType().equals("application/vnd.google-apps.folder") ) ){
                //Ajout de l'ID du fichier à telecharger
                toDownloadFiles.add(ID);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserved_space);
        setTitle(R.string.reserved_space);

        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        sdRootFolder=getExternalFilesDir(null);


        mOutputText=(TextView) findViewById(R.id.reserved_output_textview);
        mCallApiButton=(Button) findViewById(R.id.reserved_callapi);
        mDlApiButton=(Button) findViewById(R.id.reserved_download);

        mCallApiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallApiButton.setEnabled(false);
                mOutputText.setText("");
                getResultsFromApi();
                mCallApiButton.setEnabled(true);
            }
        });

        mDlApiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(files_list!=null){
                    //File f=files_list.get(Integer.parseInt(mEditText.getText().toString()));


                    String[] ids={files_list.get(3).getId(),files_list.get(4).getId()};

                    //File[] files= {f};



                    if(ids!=null){
                        //Toast.makeText(MainActivity.this,f.getName(),Toast.LENGTH_LONG)
                        //        .show();
                        Toast.makeText(ReservedSpaceActivity.this,"Demarrage telechargement",Toast.LENGTH_LONG)
                                .show();

                        new DLfileTask(mCredential)
                                .execute(ids);
                    }
                }
                else{
                    Toast.makeText(ReservedSpaceActivity.this,"Pas de files recup",Toast.LENGTH_SHORT).show();
                }
            }
        });



        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Calling Drive API ...");

        mDlProgress = new ProgressDialog(this);
        mDlProgress.setMax(100);
        mDlProgress.setIndeterminate(false);
        mDlProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDlProgress.setMessage("Downloading file ...");

        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

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

    private void syncWithGoogle(){

    }

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


    /**
     * Param1 file to store files in
     *
     */
    private class DLfileTask extends AsyncTask<String,Integer,Void> {

        private com.google.api.services.drive.Drive mService=null;


        public DLfileTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.drive.Drive.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Drive API Android Quickstart")
                    .build();

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            mDlProgress.setProgress(values[0]);
        }

        @Override
        protected Void doInBackground(String... files) {

            mDlProgress.setMax(files.length);

            for(int i=0;i<files.length;i++){

                String id=files[i];

                try {
                    downloadDriveFileToStorage(mService,id);

                } catch (IOException e) {
                    e.printStackTrace();
                    cancel(true);
                }

                publishProgress(i+1);
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

            return null;
        }

        private List<String> getParentsNameFromFile(File child) throws IOException{

            File fileOnline=mService.files().get(child.getId())
                    .setFields("parents")
                    .execute();

            return getParentsNameFromID(fileOnline.getParents());

        }


        private List<String> getParentsNameFromID(List<String> parents) throws IOException{
            if(parents==null){
                return null;
            }

            List<String> names=new ArrayList<>();

            for(int i=0;i<parents.size();i++){

                names.add( getFileNameFromID( parents.get(i) ) );
            }
            return names;

        }

        private String getFileNameFromID(String driveID) throws IOException{
            File folderfile=mService.files().get(driveID).setFields("name").execute();
            return folderfile.getName();
        }

        private boolean makeParentsDir(List<String> parentsNames){

            java.io.File externalDir=getExternalFilesDir(null);

            for(int i=0;i<parentsNames.size();i++){
                String parentName=parentsNames.get(i);

            }
            return false;
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
        private List<File> createFileTreeList(File file) throws IOException {

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
                if(parent==null){
                    parent=mService.files().get(parentid).setFields("name,id,parents").execute();
                }

                //Swap du tempFile pour utiliser le parent et rechercher son parent.
                tempFile=parent;

                //Ajouter le parent à la liste.
                treeList.add(tempFile);

            }

            return treeList;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDlProgress.setProgress(0);
            mDlProgress.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mDlProgress.hide();
            Toast.makeText(ReservedSpaceActivity.this,"FILE DOWNLOADED OK",Toast.LENGTH_SHORT).show();
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
            Toast.makeText(ReservedSpaceActivity.this,"FILE NOT DOWNLOADED KOOOOO",Toast.LENGTH_SHORT).show();
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

            List<String> ToDlid=getFilesToDownload();

            for(String id: ToDlid){
                File fi=Drive_Treemap.get(id);
                Log.d("TODLFiles",fi.getName());
            }

            return fileInfo;
        }


        @Override
        protected void onPreExecute() {
            mOutputText.setText("");
            mProgress.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
            mProgress.hide();
            if (output == null || output.size() == 0) {
                mOutputText.setText("No results returned.");
            } else {
                output.add(0, "Data retrieved using the Drive API:");
                mOutputText.setText(TextUtils.join("\n", output));
            }
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
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
    }

}
