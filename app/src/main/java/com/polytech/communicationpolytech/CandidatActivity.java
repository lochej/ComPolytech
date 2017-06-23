package com.polytech.communicationpolytech;

import android.app.Dialog;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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

import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class CandidatActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    File[] sectionFolders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_candidat);

        setup();
    }

    private void setup(){
        File sdFolder=getExternalFilesDir(null);

        File candidatFolder=new File(sdFolder.getAbsolutePath() + Constants.PATH_CANDIDAT);

        File[] folders=candidatFolder.listFiles();

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

        setTitle(R.string.espace_candidat);

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

        showDirectingDialog();
    }

    private void showDirectingDialog(){
        final AlertDialog.Builder builder=new AlertDialog.Builder(this);
        LinearLayout buttonContainer=(LinearLayout) this.getLayoutInflater().inflate(R.layout.dialog_directing,null);
        builder.setView(buttonContainer);
        builder.setTitle("Qui êtes-vous ?");
        builder.setMessage("Vous êtes candidat dans une formation de Polytech Tours ?" +
                "\n" +
                "Sélectionné pour quel niveau d'études pour souhaitez intégré Polytech Tours:");
        builder.setNegativeButton("Ignorer",null);


        final Dialog dialog=builder.create();


        if(sectionFolders!=null){


            for(int i=0;i<sectionFolders.length;i++){

                Button b=(Button) this.getLayoutInflater().inflate(R.layout.flat_text_button,null);

                final int index=i;

                b.setWidth(MATCH_PARENT);
                b.setHeight(WRAP_CONTENT);


                b.setText(sectionFolders[i].getName());
                b.setTextColor(ContextCompat.getColor(this,R.color.colorAccent));

                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mViewPager.setCurrentItem(index,true);
                        dialog.dismiss();
                    }
                });

                buttonContainer.addView(b);

            }

        }

        dialog.show();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return super.onCreateOptionsMenu(menu);
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
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

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

        }
    }
}
