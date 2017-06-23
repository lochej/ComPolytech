package com.polytech.communicationpolytech;

import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class QuizzActivity extends AppCompatActivity {



    ViewPager quizzPager;

    List<CSVformatter.CSVQuizzEntry> ent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Quizz Polytech");
        setContentView(R.layout.activity_quizz);


        quizzPager=(ViewPager) findViewById(R.id.quizz_viewPager);

        ent=getEntries();

        quizzPager.setAdapter(new SectionsPagerAdapter(getSupportFragmentManager()));


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

            return QuizzItemFragment.newInstance(position,ent.get(position));
        }

        @Override
        public int getCount() {

            return ent.size() ;
        }

        @Override
        public CharSequence getPageTitle(int position) {

           return ent.get(position).getQuestion();

        }
    }



    public static List<CSVformatter.CSVQuizzEntry> getEntries(){
        ArrayList<CSVformatter.CSVQuizzEntry> entries= new ArrayList<>();


        String question="A";


        for(int i=0;i<10;i++){


            ArrayList<String> answers=new ArrayList<>();


            for(int j=0;j<3;j++){
                answers.add(question+j);
            }

            CSVformatter.CSVQuizzEntry entry=new CSVformatter.CSVQuizzEntry(question,answers,answers.get(0));

            char last=question.charAt(question.length()-1);

            last+=1;
            question+=last;

            entries.add(entry);



        }

        return entries;
    }



    public static class QuizzItemFragment extends Fragment{

        private static final String ARG_SECTION_NUMBER = "section_number";
        private static final String ARG_QUIZZ_ENTRY = "quizz_entry";


        int questionIndex;
        int questionNumber;

        CSVformatter.CSVQuizzEntry entry;

        RadioGroup answers;

        TextView question;

        FloatingActionButton validFab;

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static QuizzItemFragment newInstance(int sectionNumber, CSVformatter.CSVQuizzEntry csvQuizzEntry) {
            QuizzItemFragment fragment = new QuizzItemFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            args.putSerializable(ARG_QUIZZ_ENTRY,csvQuizzEntry);
            fragment.setArguments(args);
            return fragment;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


            Bundle args=getArguments();

            questionIndex=args.getInt(ARG_SECTION_NUMBER);

            questionNumber=questionIndex+1;

            entry=(CSVformatter.CSVQuizzEntry) args.getSerializable(ARG_QUIZZ_ENTRY);

            View item=inflater.inflate(R.layout.quizz_item,container,false);

            answers=(RadioGroup) item.findViewById(R.id.quizz_item_selector);

            question=(TextView) item.findViewById(R.id.quizz_item_question_title);

            validFab=(FloatingActionButton) item.findViewById(R.id.quizz_item_valid_fab);


            setupAnswers();

            setupQuestion();


            return item;
        }


        private void setupQuestion(){
            question.setText(String.format("%d. %s",questionNumber,entry.getQuestion()));
        }

        private void setupAnswers(){

            answers.removeAllViews();

            List<String> entries=entry.getAnswers();

            for(int i=0;i<entries.size();i++){

                String answer=entries.get(i);


                RadioButton answerButton=new RadioButton(this.getContext());

                answerButton.setText(answer);

                answers.addView(answerButton);

            }
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
        }
    }
}
