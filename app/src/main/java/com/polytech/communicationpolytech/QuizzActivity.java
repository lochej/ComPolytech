package com.polytech.communicationpolytech;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuizzActivity extends AppCompatActivity {

    private static final String KEY_QUESTION_INDEX="questionIndex";
    private static final String KEY_QUIZZ_OBJECT="quizzObject";
    private static final String KEY_ON_RESULT="aff_result";
    //ViewPager quizzPager;

    //List<CSVformatter.CSVQuizzEntry> ent;

    RadioGroup answers;

    TextView question;

    TextView explaination;

    FloatingActionButton validFab;

    QuizzManager quizz;

    ProgressBar progress;

    TextView questionNumber;

    LinearLayout header;

    View quizzContainer;

    TextView quizzPlaceholder;

    View quizzResultContainer;

    TextView quizzResultScore;

    TextView quizzResultHint;

    TextView quizzResultText;

    ImageView quizzResultImage;

    ImageView quizzLogo;

    int currentQuestionIndex = 0;

    private int[] colors;

    boolean showsResult=false;

    public class WaitForAnswerClickListener implements View.OnClickListener {

        CSVformatter.CSVQuizzEntry entry;

        public WaitForAnswerClickListener(CSVformatter.CSVQuizzEntry entry) {
            this.entry = entry;
        }


        @Override
        public void onClick(View v) {

            if (answers.getCheckedRadioButtonId() == -1) {
                Toast.makeText(v.getContext(), "Choisissez une réponse", Toast.LENGTH_SHORT).show();
                return;
            }

            //if(((RadioButton) answers.getChildAt(answers.getCheckedRadioButtonId())).getText().toString().equals(entry.getValidAnswer())){
            if (answers.getCheckedRadioButtonId() == entry.getValidAnswerIndex()) {

                entry.setValid(true);
            } else {
                entry.setValid(false);
            }
            setEnabledAnswers(false);
            showExplaination(true);

            setAnswersColor(entry);

            validFab.setImageResource(R.drawable.ic_arrow_forward_black_24dp);
            validFab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(v.getContext(), R.color.greenLock)));

            v.setOnClickListener(showNextQuestionListener);

        }
    }

    View.OnClickListener showNextQuestionListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            askNextQuestion();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Quizz Polytech");


        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setContentView(R.layout.activity_quizz);


        if(savedInstanceState!=null){
            currentQuestionIndex=savedInstanceState.getInt(KEY_QUESTION_INDEX);
            quizz =(QuizzManager) savedInstanceState.getSerializable(KEY_QUIZZ_OBJECT);
            showsResult=savedInstanceState.getBoolean(KEY_ON_RESULT);
        }else{
            setupQuizz();
        }




        colors = this.getResources().getIntArray(R.array.header_bg_colors);

        answers = (RadioGroup) findViewById(R.id.quizz_item_selector);

        question = (TextView) findViewById(R.id.quizz_item_question_title);

        validFab = (FloatingActionButton) findViewById(R.id.quizz_item_valid_fab);

        explaination = (TextView) findViewById(R.id.quizz_item_explaination);

        progress = (ProgressBar) findViewById(R.id.quizz_progressbar);

        quizzContainer= findViewById(R.id.quizz_container);

        quizzPlaceholder=(TextView) findViewById(R.id.quizz_placeholder);

        questionNumber = (TextView) findViewById(R.id.quizz_question_number);

        header = (LinearLayout) findViewById(R.id.quizz_header);

        quizzResultContainer = findViewById(R.id.quizz_result_container);

        quizzResultScore = (TextView) findViewById(R.id.quizz_score);

        quizzResultHint = (TextView) findViewById(R.id.quizz_result_hint);

        quizzResultText = (TextView) findViewById(R.id.quizz_result_text);

        quizzResultImage = (ImageView) findViewById(R.id.quizz_image_trophey);

        quizzLogo = (ImageView) findViewById(R.id.quizz_logo);
        /*answers.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {

                    CSVformatter.CSVQuizzEntry entry=quizz.getQuizzEntryAt(currentQuestionIndex);

                    if(checkedId==entry.getValidAnswerIndex()){
                        entry.setValid(true);
                    }
                    else{
                        entry.setValid(false);
                    }
                    notifyPointsCount();
                }
            });
            */
        if(quizz!=null){

            if(showsResult){
                setupResult();

                quizzPlaceholder.setVisibility(View.GONE);
                quizzContainer.setVisibility(View.GONE);
                quizzResultContainer.setVisibility(View.VISIBLE);
                showsResult=true;
                return;
            }
            progress.setMax(getQuestionCount());
            quizzPlaceholder.setVisibility(View.GONE);
            quizzResultContainer.setVisibility(View.GONE);
            showsResult=false;

            setupQuizzQuestionViews(currentQuestionIndex);
        }
        else{
            quizzContainer.setVisibility(View.GONE);
            quizzResultContainer.setVisibility(View.GONE);
            showsResult=false;
            quizzPlaceholder.setVisibility(View.VISIBLE);
        }


        //quizzPager.setAdapter(new SectionsPagerAdapter(getSupportFragmentManager(),quizz));


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Selects an index in an array based on the hash integer passed.
     *
     * @param hash  the hash integer which sine is calculated from
     * @param array an array in which the index should choosen
     * @return
     */
    private static int getColorIndexFromHash(int hash, int[] array) {
        int l = array.length;

        return (int) ((Math.sin(hash) + 1) * l) / 2;
    }


    private int getQuestionCount(){
        if(quizz!=null){
            return quizz.questionCounts;
        }
        return 0;
    }

    private void setupQuizz() {
        File externalDir = getExternalFilesDir(null);

        File csvQuizz = new File(externalDir, Constants.CSV_QUIZZ);

        try {
            List<CSVformatter.CSVQuizzEntry> entries = CSVformatter.extractQuizzFromCSV(csvQuizz);

            quizz=new QuizzManager();

            quizz.setEntries(pickNRandom(entries,10));

        } catch (FileNotFoundException e) {
            quizz=null;
            e.printStackTrace();
        }
    }

    private void animateQuizzContainer(boolean show){

    }


    private void setupQuizzQuestionViews(int questionIndex) {


        if(quizzContainer.getVisibility() == View.GONE){
            quizzContainer.setVisibility(View.VISIBLE);
        }
        final CSVformatter.CSVQuizzEntry entry = quizz.getQuizzEntryAt(questionIndex);

        setupQuestion(questionIndex, entry);

        setupAnswers(entry);

        setupExplaination(entry);

        setBackgroundColor(entry);

        showExplaination(false);

        validFab.setImageResource(R.drawable.ic_check_black_24dp);
        validFab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorAccent)));
        validFab.setOnClickListener(new WaitForAnswerClickListener(entry));

        //updateQuestionNumber();


    }

    private void setEnabledAnswers(boolean enabled) {
        for (int i = 0; i < answers.getChildCount(); i++) {
            ((RadioButton) answers.getChildAt(i)).setEnabled(enabled);
        }
    }

    private void setBackgroundColor(CSVformatter.CSVQuizzEntry entry) {
        int color=colors[getColorIndexFromHash(entry.hashCode(), colors)];
        header.setBackgroundColor(color);
        quizzLogo.setColorFilter(color);
    }

    private void setupAnswers(CSVformatter.CSVQuizzEntry entry) {

        answers.removeAllViews();


        List<String> entries = entry.getAnswers();


        for (int i = 0; i < entries.size(); i++) {

            String answer = entries.get(i);


            RadioButton answerButton = new RadioButton(this);
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            answerButton.setLayoutParams(lp);


            answerButton.setId(i);
            answerButton.setText(answer);

            answers.addView(answerButton);

        }
        answers.clearCheck();
    }

    private void setupQuestion(int index, CSVformatter.CSVQuizzEntry entry) {
        question.setText(String.format("%d. %s", index + 1, entry.getQuestion()));
        progress.setProgress(currentQuestionIndex + 1);
        questionNumber.setText(String.format("Question: %d sur %d", currentQuestionIndex + 1, getQuestionCount()));
    }

    private void askNextQuestion() {
        //System.out.println(currentQuestionIndex + "");
        //if we reached the end of the questions
        if (currentQuestionIndex == (quizz.questionCounts - 1)) {
            showResult();
            return;
        }

        currentQuestionIndex++;
        setupQuizzQuestionViews(currentQuestionIndex);
    }

    private void askPreviousQuestion() {
        currentQuestionIndex--;
        setupQuizzQuestionViews(currentQuestionIndex);
    }

    private void askQuestion(int index) {
        currentQuestionIndex = index;
        setupQuizzQuestionViews(currentQuestionIndex);
    }


    private void setupExplaination(CSVformatter.CSVQuizzEntry entry) {
        explaination.setText(entry.getExplanation() == null ? "" : (entry.getExplanation().length() == 0 ? "" : String.format("%s: %s", "Explication", entry.getExplanation())));
    }

    private void showExplaination(boolean show) {

        explaination.setVisibility(show ? View.VISIBLE : View.GONE);

    }

    private void updateQuestionNumber() {
        progress.setProgress(currentQuestionIndex + 1);
        questionNumber.setText(String.format("Question: %d sur %d", currentQuestionIndex + 1, getQuestionCount()));
    }

    private void showResult() {

        Animation fadeIn= AnimationUtils.loadAnimation(this,R.anim.fade_in_scale_up);

        Animation fadeOut=AnimationUtils.loadAnimation(this,R.anim.slide_out_to_left);

        setupResult();

        quizzContainer.startAnimation(fadeOut);

        quizzContainer.setVisibility(View.GONE);

        quizzResultContainer.startAnimation(fadeIn);

        quizzResultContainer.setVisibility(View.VISIBLE);
        showsResult=true;


    }

    private void setupResult(){

        int points=quizz.getPointsCount();

        int questionCounts=getQuestionCount();

        if(points <= questionCounts*0.30f){
            quizzResultHint.setText(R.string.fourth_quizz_hint);
            quizzResultText.setText(R.string.fourth_quizz_result);
            Glide.with(this).load(R.drawable.paper_medal).into(quizzResultImage);

        }
        //Plus de 3 points
        else if(points <= questionCounts*0.60f){
            quizzResultHint.setText(R.string.third_quizz_hint);
            quizzResultText.setText(R.string.third_quizz_result);
            Glide.with(this).load(R.drawable.bronze_medal).into(quizzResultImage);
        }
        //Plus de 6 points
        else if(points <= questionCounts*0.80f){
            quizzResultHint.setText(R.string.second_quizz_hint);
            quizzResultText.setText(R.string.second_quizz_result);
            Glide.with(this).load(R.drawable.silver_medal).into(quizzResultImage);
        }
        //Plus de 8 points
        else{
            quizzResultHint.setText(R.string.first_quizz_hint);
            quizzResultText.setText(R.string.first_quizz_result);
            Glide.with(this).load(R.drawable.gold_medal).into(quizzResultImage);
        }

        quizzResultScore.setText(String.format("%d / %d",points,questionCounts));


    }

    private void setAnswersColor(CSVformatter.CSVQuizzEntry entry) {

        int checkedAnswerID = answers.getCheckedRadioButtonId();

        int validAnswerID = entry.getValidAnswerIndex();

        RadioButton validBtn = null;

        RadioButton checkedBtn = null;

        //Colorer en vert uniquement validAnswer
        if (checkedAnswerID != validAnswerID) {
            checkedBtn = (RadioButton) answers.getChildAt(checkedAnswerID);
            checkedBtn.setTextColor(ContextCompat.getColor(this, R.color.redLock));
        }
        validBtn = (RadioButton) answers.getChildAt(validAnswerID);
        validBtn.setTextColor(ContextCompat.getColor(this, R.color.greenLock));

    }

    public void OnClickContactMe(View view){

        Intent startContactActivity=new Intent(view.getContext(),ContactActivity.class);

        view.getContext().startActivity(startContactActivity);

        finish();

    }

    public void OnClickQuitt(View view){
        finish();
    }

    public class QuizzManager implements Serializable {


        int questionCounts=0;

        List<CSVformatter.CSVQuizzEntry> entries = new ArrayList<>();

        int points = 0;


        public QuizzManager() {
        }

        public List<CSVformatter.CSVQuizzEntry> getEntries() {
            return entries;
        }

        public void setEntries(List<CSVformatter.CSVQuizzEntry> entries) {
            this.entries = entries;
            questionCounts=entries.size();
        }

        /**
         * Choisi un ensemble de 10 questions au hasard
         */
        public List<CSVformatter.CSVQuizzEntry> chooseQuestions() {
            return generateEntries();
        }

        /**
         * Place la question index a true ou false si elle bonne ou fausse
         *
         * @param index
         * @param valid
         */
        public void setQuestionResult(int index, boolean valid) {
            entries.get(index).setValid(valid);
        }

        public boolean getQuestionResult(int index) {
            return entries.get(index).isValid();
        }

        /**
         * Recupere la question à la position index
         *
         * @param index
         * @return
         */
        public CSVformatter.CSVQuizzEntry getQuizzEntryAt(int index) {
            return entries.get(index);
        }


        public int getPointsCount() {
            refreshPointCount();
            return points;
        }


        private void refreshPointCount() {
            int points = 0;
            for (int i = 0; i < entries.size(); i++) {
                if (entries.get(i).isValid()) {
                    points++;
                }
            }

            setPointsCount(points);
        }


        public void setPointsCount(int count) {
            points = count;
        }


    }

    public static List<CSVformatter.CSVQuizzEntry> pickNRandom(List<CSVformatter.CSVQuizzEntry> lst, int n) {
        List<CSVformatter.CSVQuizzEntry> copy = new ArrayList<>(lst);
        Collections.shuffle(copy);
        return copy.subList(0, n);
    }

    public static List<CSVformatter.CSVQuizzEntry> generateEntries() {

        ArrayList<CSVformatter.CSVQuizzEntry> entries = new ArrayList<>();


        String question = "A";


        for (int i = 0; i < 10; i++) {


            ArrayList<String> answers = new ArrayList<>();


            for (int j = 0; j < 20; j++) {
                answers.add(question + j);
            }

            CSVformatter.CSVQuizzEntry entry = new CSVformatter.CSVQuizzEntry(question, answers, answers.get(0));

            Collections.shuffle(answers);

            entry.setExplanation(question);

            char last = question.charAt(question.length() - 1);

            last += 1;
            question += last;

            entries.add(entry);


        }

        return entries;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(KEY_QUESTION_INDEX,currentQuestionIndex);
        outState.putSerializable(KEY_QUIZZ_OBJECT,quizz);
        outState.putBoolean(KEY_ON_RESULT,showsResult);
    }

    //
//    /**
//     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
//     * one of the sections/tabs/pages.
//     */
//    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
//
//
//        QuizzManager qm;
//
//        public SectionsPagerAdapter(FragmentManager fm,QuizzManager quizzManager){
//            super(fm);
//            qm=quizzManager;
//        }
//
//        @Override
//        public Fragment getItem(int position) {
//
//            return QuizzItemFragment.newInstance(position,qm.getQuizzEntryAt(position),qm);
//        }
//
//        @Override
//        public int getCount() {
//
//            return qm.getEntries().size();
//        }
//
//        @Override
//        public CharSequence getPageTitle(int position) {
//
//           return qm.getQuizzEntryAt(position).getQuestion();
//
//        }
//
//        public void notifyPointCountChanged(){
//
//            qm.refreshPointCount();
//            Log.d("ADAPTER","Points modifiées:" + qm.getPointsCount());
//        }
//    }
//
//
//

//
//
//
//    public static class QuizzItemFragment extends Fragment{
//
//        private static final String ARG_SECTION_NUMBER = "section_number";
//        private static final String ARG_QUIZZ_ENTRY = "quizz_entry";
//
//        QuizzManager qm;
//
//        SectionsPagerAdapter adapter;
//
//        int questionIndex;
//        int questionNumber;
//
//        CSVformatter.CSVQuizzEntry entry;
//
//        RadioGroup answers;
//
//        TextView question;
//
//        FloatingActionButton validFab;
//
//        /**
//         * Returns a new instance of this fragment for the given section
//         * number.
//         */
//        public static QuizzItemFragment newInstance(int sectionNumber, CSVformatter.CSVQuizzEntry csvQuizzEntry,QuizzManager qm) {
//            QuizzItemFragment fragment = new QuizzItemFragment();
//            Bundle args = new Bundle();
//            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
//            args.putSerializable(ARG_QUIZZ_ENTRY,qm);
//            //fragment.adapter=adapter;
//            fragment.setArguments(args);
//            return fragment;
//        }
//
//        @Nullable
//        @Override
//        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//
//
//            Bundle args=getArguments();
//
//            questionIndex=args.getInt(ARG_SECTION_NUMBER);
//
//            questionNumber=questionIndex+1;
//
//            qm=(QuizzManager)args.getSerializable(ARG_QUIZZ_ENTRY);
//
//            entry=qm.getQuizzEntryAt(questionIndex);
//
//            View item=inflater.inflate(R.layout.quizz_item,container,false);
//
//            answers=(RadioGroup) item.findViewById(R.id.quizz_item_selector);
//
//            question=(TextView) item.findViewById(R.id.quizz_item_question_title);
//
//            validFab=(FloatingActionButton) item.findViewById(R.id.quizz_item_valid_fab);
//
//
//            setupAnswers();
//
//            setupQuestion();
//
//
//            answers.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
//                    if(checkedId==entry.getValidAnswerIndex()){
//                        entry.setValid(true);
//                    }
//                    else{
//                        entry.setValid(false);
//                    }
//                    notifyPointsCount();
//                }
//            });
//
//            return item;
//        }
//
//
//        private void setupQuestion(){
//            question.setText(String.format("%d. %s",questionNumber,entry.getQuestion()));
//        }
//
//        private void setupAnswers(){
//
//            answers.removeAllViews();
//
//            List<String> entries=entry.getAnswers();
//
//            for(int i=0;i<entries.size();i++){
//
//                String answer=entries.get(i);
//
//
//                RadioButton answerButton=new RadioButton(this.getContext());
//
//                answerButton.setId(i);
//                answerButton.setText(answer);
//
//                answers.addView(answerButton);
//
//            }
//        }
//
//        private void notifyPointsCount(){
//            qm.refreshPointCount();
//            Log.d("ADAPTER","Points modifiées:" + qm.getPointsCount());
//        }
//
//        @Override
//        public void onSaveInstanceState(Bundle outState) {
//            super.onSaveInstanceState(outState);
//        }
//    }
}
