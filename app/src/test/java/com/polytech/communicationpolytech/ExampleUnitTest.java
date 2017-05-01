package com.polytech.communicationpolytech;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void printHeader(){
        String[] headers=CSVformatter.headerOutlook;

        String[] data={"PRENOM","NOM","EMAIL"};

        CSVformatter.writeLineToOutputStream(System.out,data,CSVformatter.FORMAT_GOOGLE);

        CSVformatter.writeLineToOutputStream(System.out,data,CSVformatter.FORMAT_OUTLOOK);

        //System.out.println(CSVformatter.formatGoogle);
        //System.out.println(CSVformatter.formatOutlook);



    }

}