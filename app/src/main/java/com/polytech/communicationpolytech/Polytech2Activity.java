package com.polytech.communicationpolytech;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Polytech2Activity extends AppCompatActivity {


    private SectionsPagerAdapter mSectionsPagerAdapter;

    ProgressDialog progressDialog;

    private ViewPager mViewPager;

    File[] sectionFolders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_polytech2);

        File sdFolder=getExternalFilesDir(null);

        File polytechFolder=new File(sdFolder.getAbsolutePath() + Constants.PATH_POLYTECH);

        File[] folders=polytechFolder.listFiles();

        ArrayList<File> listFolders= new ArrayList<>();

        if(folders!=null){
            for(int i=0; i<folders.length;i++){
                File current=folders[i];
                if(current.isDirectory()){
                    listFolders.add(current);
                }
            }
        }

        Collections.sort(listFolders,Constants.alphaComp);


        sectionFolders=listFolders.toArray(new File[]{});



        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle(R.string.polytech_tours);

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

        progressDialog=new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.loading));



    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }



    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a ConfigFragment (defined as a static inner class below).
            File dirToLoad=null;

            if(sectionFolders != null){
                dirToLoad=sectionFolders[position];
            }

            return RecyclerViewFileFragment.newInstance(position + 1,dirToLoad);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            //On retourne 1 pour avoir une page vide recyclerViewFragment
            if(sectionFolders == null){
                return 1;
            }

            return sectionFolders.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            File dirToLoad=null;

            if(sectionFolders != null){
                dirToLoad=sectionFolders[position];
            }

            return dirToLoad==null ? "": dirToLoad.getName();

            /*
            switch (position) {
                case 0:
                    return "L'école";
                case 1:
                    return "Les formations";
                case 2:
                    return "Projets étudiants";
                case 3:
                    return "Vie étudiante";

            }
            return null;
            */
        }
    }
}
