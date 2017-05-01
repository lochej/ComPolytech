package com.polytech.communicationpolytech;

import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Created by Jérémy on 13/04/2017.
 * Cette classe s'occupe de gerer le fichier CSV du formulaire et de toute la gestion des fichiers, flux, entetes etc
 */

public class CSVformatter {

    public static final String[] headerOutlook = {"First Name","Middle Name","Last Name","Title","Suffix","Initials","Web Page","Gender","Birthday","Anniversary","Location","Language","Internet Free Busy","Notes","E-mail Address","E-mail 2 Address","E-mail 3 Address","Primary Phone","Home Phone","Home Phone 2","Mobile Phone","Pager","Home Fax","Home Address","Home Street","Home Street 2","Home Street 3","Home Address PO Box","Home City","Home State","Home Postal Code","Home Country","Spouse","Children","Manager's Name","Assistant's Name","Referred By","Company Main Phone","Business Phone","Business Phone 2","Business Fax","Assistant's Phone","Company","Job Title","Department","Office Location","Organizational ID Number","Profession","Account","Business Address","Business Street","Business Street 2","Business Street 3","Business Address PO Box","Business City","Business State","Business Postal Code","Business Country","Other Phone","Other Fax","Other Address","Other Street","Other Street 2","Other Street 3","Other Address PO Box","Other City","Other State","Other Postal Code","Other Country","Callback","Car Phone","ISDN","Radio Phone","TTY/TDD Phone","Telex","User 1","User 2","User 3","User 4","Keywords","Mileage","Hobby","Billing Information","Directory Server","Sensitivity","Priority","Private","Categories"};

    public static final String[] headerGoogle = {"Name", "Given Name", "Additional Name", "Family Name", "Yomi Name", "Given Name Yomi", "Additional Name Yomi", "Family Name Yomi", "Name Prefix", "Name Suffix", "Initials", "Nickname", "Short Name", "Maiden Name", "Birthday", "Gender", "Location", "Billing Information", "Directory Server", "Mileage", "Occupation", "Hobby", "Sensitivity", "Priority", "Subject", "Notes", "Group Membership", "E-mail 1 - Type", "E-mail 1 - Value", "Phone 1 - Type", "Phone 1 - Value", "Phone 2 - Type", "Phone 2 - Value", "Organization 1 - Type", "Organization 1 - Name", "Organization 1 - Yomi Name", "Organization 1 - Title", "Organization 1 - Department", "Organization 1 - Symbol", "Organization 1 - Location", "Organization 1 - Job Description", "Website 1 - Type", "Website 1 - Value"};

    //Nom Prénom,Prénom,Nom de famille,Email
    public static final int[] headerGoogleIndex={0,1,3,27};

    //Nom, Prénom, Email
    public static final int[] headerOutlookIndex={0,2,14};

    public static final String formatOutlook=getFormatString(headerOutlook);

    public static final String formatGoogle=getFormatString(headerGoogle);

    public static final int INDEX_GOOGLE_FIRST_NAME=headerGoogleIndex[1];

    public static final int INDEX_GOOGLE_LAST_NAME=headerGoogleIndex[2];

    public static final int INDEX_GOOGLE_EMAIL=headerGoogleIndex[3];

    public static final int INDEX_GOOGLE_NAME=headerGoogleIndex[0];

    public static final int INDEX_OUTLOOK_FIRST_NAME=headerOutlookIndex[0];

    public static final int INDEX_OUTLOOK_LAST_NAME=headerOutlookIndex[1];

    public static final int INDEX_OUTLOOK_EMAIL=headerOutlookIndex[2];

    public static final int FORMAT_GOOGLE=0;

    public static final int FORMAT_OUTLOOK=1;

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

        FileOutputStream fos=new FileOutputStream(intoFile,true);
        PrintStream out=new PrintStream(fos);
        writeHeaderToOutputStream(out,formatType);
        out.close();
        fos.close();
    }

    public static String getStringFromData(String[] data,int formatType){

        if(formatType==FORMAT_GOOGLE){

            String[] lineValues=new String[headerGoogle.length];
            Arrays.fill(lineValues,"");

            //Remplir tout les header nécessaires
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

            //Creates all the strings to store in the header.
            String[] lineValues=new String[headerOutlook.length];
            Arrays.fill(lineValues,"");

            //Remplir tout les header nécessaires
            for(int i=0;i<headerOutlookIndex.length;i++){

                int toStoreIndex=headerOutlookIndex[i];

                lineValues[toStoreIndex] = data[i];

            }

            return String.format(formatOutlook,lineValues);

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

        return sb.toString();
    }




}