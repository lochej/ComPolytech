package com.polytech.communicationpolytech;

import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * Created by Jérémy on 13/04/2017.
 * Cette classe s'occupe de gerer le fichier CSV du formulaire et de toute la gestion des fichiers, flux, entetes etc
 */

public class CSVformatter {

    public static final String[] headerOutlook = {"First Name","Middle Name","Last Name","Title","Suffix","Initials","Web Page","Gender","Birthday","Anniversary","Location","Language","Internet Free Busy","Notes","E-mail Address","E-mail 2 Address","E-mail 3 Address","Primary Phone","Home Phone","Home Phone 2","Mobile Phone","Pager","Home Fax","Home Address","Home Street","Home Street 2","Home Street 3","Home Address PO Box","Home City","Home State","Home Postal Code","Home Country","Spouse","Children","Manager's Name","Assistant's Name","Referred By","Company Main Phone","Business Phone","Business Phone 2","Business Fax","Assistant's Phone","Company","Job Title","Department","Office Location","Organizational ID Number","Profession","Account","Business Address","Business Street","Business Street 2","Business Street 3","Business Address PO Box","Business City","Business State","Business Postal Code","Business Country","Other Phone","Other Fax","Other Address","Other Street","Other Street 2","Other Street 3","Other Address PO Box","Other City","Other State","Other Postal Code","Other Country","Callback","Car Phone","ISDN","Radio Phone","TTY/TDD Phone","Telex","User 1","User 2","User 3","User 4","Keywords","Mileage","Hobby","Billing Information","Directory Server","Sensitivity","Priority","Private","Categories"};

    public static final String[] headerGoogle = {"Name", "Given Name", "Additional Name", "Family Name", "Yomi Name", "Given Name Yomi", "Additional Name Yomi", "Family Name Yomi", "Name Prefix", "Name Suffix", "Initials", "Nickname", "Short Name", "Maiden Name", "Birthday", "Gender", "Location", "Billing Information", "Directory Server", "Mileage", "Occupation", "Hobby", "Sensitivity", "Priority", "Subject", "Notes", "Group Membership", "E-mail 1 - Type", "E-mail 1 - Value", "Phone 1 - Type", "Phone 1 - Value", "Phone 2 - Type", "Phone 2 - Value", "Organization 1 - Type", "Organization 1 - Name", "Organization 1 - Yomi Name", "Organization 1 - Title", "Organization 1 - Department", "Organization 1 - Symbol", "Organization 1 - Location", "Organization 1 - Job Description", "Website 1 - Type", "Website 1 - Value"};

    public static final String[] headerCustom = {"Nom","Prénom","Adresse E-mail","Études en cours","Recontacter à propos de"};

    public static final String[] headerQuizz= {"Question","Explication","Reponse 1 vraie","Reponse 2 fausse", "Reponse 3 fausse","Reponse 4 fausse","Reponse 5 fausse"};

    //Nom Prénom,Prénom,Nom de famille,Email
    public static final int[] headerGoogleIndex={0,1,3,27};

    //Nom, Prénom, Email
    public static final int[] headerOutlookIndex={0,2,14};

    //Nom, Prenom, Email, Etudes en cours, Recontacter à propos
    public static final int[] headerCustomIndex={0,1,2,3,4};

    //Question,Explication,Reponse 1 vraie,Reponse 2 fausse,Reponse 3 fausse, Reponse 4 fausse, Reponse 5 fausse
    public static final int[] headerQuizzIndex={0,1,2,3,4,5,6};

    public static final String formatOutlook=getFormatString(headerOutlook);

    public static final String formatGoogle=getFormatString(headerGoogle);

    public static final String formatCustom=getFormatString(headerCustom);

    public static final int INDEX_GOOGLE_FIRST_NAME=headerGoogleIndex[1];

    public static final int INDEX_GOOGLE_LAST_NAME=headerGoogleIndex[2];

    public static final int INDEX_GOOGLE_EMAIL=headerGoogleIndex[3];

    public static final int INDEX_GOOGLE_NAME=headerGoogleIndex[0];

    public static final int INDEX_OUTLOOK_FIRST_NAME=headerOutlookIndex[0];

    public static final int INDEX_OUTLOOK_LAST_NAME=headerOutlookIndex[1];

    public static final int INDEX_OUTLOOK_EMAIL=headerOutlookIndex[2];

    public static final int INDEX_QUESTION_QUIZZ=headerQuizzIndex[0];

    public static final int INDEX_EXPLANATION_QUIZZ=headerQuizzIndex[1];

    public static final int INDEX_R1_QUIZZ=headerQuizzIndex[2];

    public static final int INDEX_R2_QUIZZ=headerQuizzIndex[3];

    public static final int INDEX_R3_QUIZZ=headerQuizzIndex[4];

    public static final int INDEX_R4_QUIZZ=headerQuizzIndex[5];

    public static final int INDEX_R5_QUIZZ=headerQuizzIndex[6];



    public static final int FORMAT_GOOGLE=0;

    public static final int FORMAT_OUTLOOK=1;

    public static final int FORMAT_CUSTOM=2;

    public static final String columnSeparator=",";

    public static final String lineEnding="\r\n";

    @NonNull
    public static String getFormatString(String[] header){
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<header.length;i++){
            sb.append("%s");

            if(i!=header.length-1){
                sb.append(",");
            }
        }
        return sb.toString();
    }

    public static void writeLineDataToFile(File intoFile, String[] data, int formatType) throws IOException {

        FileOutputStream fos=new FileOutputStream(intoFile,true);
        PrintStream out=new PrintStream(fos);
        writeLineToOutputStream(out,data,formatType);
        out.close();
        fos.close();
    }

    /**
     * Ecrit une ligne dans le fichier de flux de sortie
     * @param out
     * @param data contient prenom,nom,email dans un tableau
     * @param formatType
     */
    public static void writeLineToOutputStream(PrintStream out,String[] data,int formatType){

        String line=getStringFromData(data,formatType);

        out.print(line);
        out.print(lineEnding);

    }

    public static void writeHeaderToOutputStream(PrintStream out,int formatType){
        out.print(getHeaderString(formatType));
        out.print(lineEnding);
    }

    public static void writeHeaderToOutputStream(File intoFile,int formatType) throws IOException {

        FileOutputStream fos=new FileOutputStream(intoFile,false);
        PrintStream out=new PrintStream(fos);
        writeHeaderToOutputStream(out,formatType);
        out.close();
        fos.close();
    }

    public static void writeLineDataToFile(File intoFile, CSVFormEntry entry, int formatType) throws IOException {
        writeLineDataToFile(intoFile,entry.toArray(),formatType);
    }

    public static TreeMap<String,CSVFormEntry> extractTreeMap(File fromFile) throws FileNotFoundException {
        Scanner sc=new Scanner(fromFile);

        TreeMap<String,CSVFormEntry> out= extractTreeMap(sc);

        sc.close();


        return  out;
    }

    public static TreeMap<String,CSVFormEntry> extractTreeMap(Scanner sc){
        int counter=0;
        int formatType=-1;

        TreeMap<String,CSVFormEntry> out=new TreeMap<>();
        while(sc.hasNextLine()){

            String line=sc.nextLine();

            //Verification des headers pour determiner le format
            if(counter == 0 ){
                String[] headers=line.split(Pattern.quote(columnSeparator));
                if(Arrays.equals(headers,headerGoogle)){
                    formatType=FORMAT_GOOGLE;
                }
                else if(Arrays.equals(headers,headerOutlook)){
                    formatType=FORMAT_CUSTOM;
                }
                else if(Arrays.equals(headers,headerCustom)){
                    formatType=FORMAT_CUSTOM;
                }
                else{
                    return null;
                }

            }
            else{
                CSVFormEntry entry= CSVFormEntry.fromCSVLine(line,formatType);
                if(entry!=null){
                    out.put(entry.getMail(),entry);
                }
            }


            counter++;
        }
        return out;
    }


    public static List<CSVQuizzEntry> extractQuizzFromCSV(File file) throws FileNotFoundException {
        Scanner sc=new Scanner(file);

        List<CSVQuizzEntry> out=extractQuizzFromCSV(sc);

        sc.close();


        return out;
    }

    public static List<CSVQuizzEntry> extractQuizzFromCSV(Scanner sc){
        int counter=0;
        int formatType=-1;

        ArrayList<CSVQuizzEntry> out=new ArrayList<>();

        while(sc.hasNextLine()){

            String line=sc.nextLine();

            //Verification des headers pour determiner le format
            if(counter == 0 ){
                String[] headers=line.split(Pattern.quote(columnSeparator));

                if(Arrays.equals(headers,headerQuizz)){
                    formatType=FORMAT_GOOGLE;
                }
                else{
                    //return null;
                }
            }
            else{
                CSVQuizzEntry entry= CSVQuizzEntry.fromCSVLine(line,formatType);
                if(entry!=null){
                    out.add(entry);
                }
            }


            counter++;
        }
        return out;
    }



    public static String getStringFromData(String[] data,int formatType){

        if(formatType==FORMAT_GOOGLE){

            String[] lineValues=new String[headerGoogle.length];
            Arrays.fill(lineValues,"");

            //Remplir tout les headerContainer nécessaires
            for(int i=0;i<headerGoogleIndex.length;i++){

                //Creation du champ NAME
                if(i==0){
                    int toStoreIndex=headerGoogleIndex[i];
                    lineValues[toStoreIndex]= data[i] +
                            " "+
                            data[i+1];
                }
                //Creation des champs givenName, Familly name, Email
                else{
                    int toStoreIndex = headerGoogleIndex[i];
                    lineValues[toStoreIndex] = data[i-1];
                }

            }

            return String.format(formatGoogle,lineValues);
        }

        else if(formatType==FORMAT_OUTLOOK){

            //Creates all the strings to store in the headerContainer.
            String[] lineValues=new String[headerOutlook.length];
            Arrays.fill(lineValues,"");

            //Remplir tout les headerContainer nécessaires
            for(int i=0;i<headerOutlookIndex.length;i++){

                int toStoreIndex=headerOutlookIndex[i];

                lineValues[toStoreIndex] = data[i];

            }

            return String.format(formatOutlook,lineValues);

        }
        else if(formatType==FORMAT_CUSTOM){

            //Creates all the strings to store in the headerContainer.
            String[] lineValues=new String[headerCustom.length];
            Arrays.fill(lineValues,"");

            //Remplir tout les headerContainer nécessaires
            for(int i=0;i<headerCustomIndex.length;i++){

                int toStoreIndex=headerCustomIndex[i];

                lineValues[toStoreIndex] = data[i];

            }

            return String.format(formatCustom,lineValues);
        }

        return null;
    }

    public static String getHeaderString(int formatType){

        StringBuilder sb=new StringBuilder();
        if(formatType==FORMAT_GOOGLE){
            for(int i=0;i<headerGoogle.length;i++){

                sb.append(headerGoogle[i]);
                //Si c'est le dernier on ne rajoute pas de virgule
                if (i != headerGoogle.length - 1) {
                    sb.append(columnSeparator);
                }

            }
        }
        else if(formatType==FORMAT_OUTLOOK){
            for(int i=0;i<headerOutlook.length;i++){

                sb.append(headerOutlook[i]);
                //Si c'est le dernier on ne rajoute pas de virgule
                if (i != headerOutlook.length - 1) {
                    sb.append(columnSeparator);
                }

            }
        }
        else if(formatType==FORMAT_CUSTOM){
            for(int i=0;i<headerCustom.length;i++){

                sb.append(headerCustom[i]);
                //Si c'est le dernier on ne rajoute pas de virgule
                if (i != headerCustom.length - 1) {
                    sb.append(columnSeparator);
                }

            }
        }

        return sb.toString();
    }

    /**
     * Crée le fichier complet des entrés
     * @param intoFile
     * @param entries
     */
    public static void writeTreemapToFile(File intoFile, TreeMap<String,CSVFormEntry> entries, int formatType) throws IOException {

        writeHeaderToOutputStream(intoFile,formatType);

        for(String mail: entries.keySet()){
            writeLineDataToFile(intoFile,entries.get(mail),formatType);
        }

    }


    public static class CSVFormEntry {

        private String nom="";
        private String prenom="";
        private String news="";
        private String study="";
        private String mail="";

        public String getMail() {
            return mail;
        }

        public void setMail(String mail) {
            this.mail = mail;
        }

        public CSVFormEntry(String mail) {
            this.mail = mail;
        }

        public CSVFormEntry(String nom, String prenom, String news, String study, String mail) {
            this.nom = nom;
            this.prenom = prenom;
            this.news = news;
            this.study = study;
            this.mail = mail;
        }

        public String getNom() {
            return nom;
        }

        public void setNom(String nom) {
            this.nom = nom;
        }

        public String getPrenom() {
            return prenom;
        }

        public void setPrenom(String prenom) {
            this.prenom = prenom;
        }

        public String getNews() {
            return news;
        }

        public void setNews(String news) {
            this.news = news;
        }

        public String getStudy() {
            return study;
        }

        public void setStudy(String study) {
            this.study = study;
        }

        public static CSVFormEntry fromCSVLine(String line, int format){
            String[] fields=line.split(Pattern.quote(columnSeparator));

            int[] headerindexes=null;

            switch (format){
                case FORMAT_CUSTOM:

                    headerindexes=headerCustomIndex;

                    break;
                case FORMAT_GOOGLE:

                    headerindexes=headerGoogleIndex;
                    return null;
                case FORMAT_OUTLOOK:

                    headerindexes=headerOutlookIndex;
                    return null;
                default:
                    return null;
            }

            CSVFormEntry entry=new CSVFormEntry(fields[headerindexes[2]]);

            entry.setNom(fields[headerindexes[0]]);
            entry.setPrenom(fields[headerindexes[1]]);
            entry.setStudy(fields[headerindexes[3]]);
            entry.setNews(fields[headerindexes[4]]);

            return entry;

        }

        @Override
        public String toString() {
            return nom + "|" + prenom + "|" + mail + "|" + study + "|" + news;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof CSVFormEntry && obj.toString().equals(this.toString());
        }

        public String[] toArray(){
            return new String[]{nom,prenom,mail,study,news};
        }
    }




    public static class CSVQuizzEntry implements Serializable{

        private String question;
        private List<String> answers;
        private String validAnswer;
        private String explanation;

        boolean valid=false;

        public CSVQuizzEntry(String question, List<String> answers, String validAnswer) {
            this.question = question;
            this.answers = answers;
            this.validAnswer = validAnswer;
        }

        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }

        public List<String> getAnswers() {
            return answers;
        }

        public void setAnswers(List<String> answers) {
            this.answers = answers;
        }

        public String getValidAnswer() {
            return validAnswer;
        }

        public void setValidAnswer(String validAnswer) {
            this.validAnswer = validAnswer;
        }

        public int getValidAnswerIndex(){
            return answers.indexOf(validAnswer);
        }


        public String getExplanation() {
            return explanation;
        }

        public void setExplanation(String explanation) {
            this.explanation = explanation;
        }


        public void setValid(boolean valid){
            this.valid=valid;
        }

        public boolean isValid(){
            return valid;
        }

        public static CSVQuizzEntry fromCSVLine(String line, int format){
            String[] fields=line.split(Pattern.quote(columnSeparator));


            String question=fields[0];
            String explanation=fields[1];
            String validAnswer=fields[2];

            //System.out.println(question+explanation+validAnswer);


            ArrayList<String> answers=new ArrayList<>();


            for(int i=2;i<fields.length;i++){
                //Si la case est vide on ne l'ajoute pas
                if(!fields[i].equals("")){
                    answers.add(fields[i]);
                }

            }

            Collections.shuffle(answers);


            CSVQuizzEntry entry=new CSVQuizzEntry(question,answers,validAnswer);

            entry.setExplanation(explanation);

            return entry;

        }

    }




}