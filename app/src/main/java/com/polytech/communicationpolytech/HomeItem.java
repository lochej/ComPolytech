package com.polytech.communicationpolytech;

import android.content.Intent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jérémy on 07/04/2017.
 */

public class HomeItem {

    public HomeItem(int cardID, int thumbID, int iconID, String title, View.OnClickListener onClickListener) {
        this.cardID = cardID;
        this.thumbID = thumbID;
        this.iconID = iconID;
        this.title = title;
        this.onClickListener = onClickListener;
    }

    public HomeItem() {
    }

    private int cardID;

    private int thumbID;

    private int iconID;

    private String title;

    private View.OnClickListener onClickListener;


    public int getCardID() {
        return cardID;
    }

    public void setCardID(int cardID) {
        this.cardID = cardID;
    }

    public int getThumbID() {
        return thumbID;
    }

    public void setThumbID(int thumbID) {
        this.thumbID = thumbID;
    }

    public int getIconID() {
        return iconID;
    }

    public void setIconID(int iconID) {
        this.iconID = iconID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public View.OnClickListener getOnClickListener() {
        return onClickListener;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public static List<HomeItem> getHomeObjectList(final MainActivity mainActivity){

        ArrayList<HomeItem> dataList= new ArrayList<HomeItem>();

        HomeItem polytech=new HomeItem();
        polytech.setCardID(0);
        polytech.setTitle(mainActivity.getString(R.string.polytech_tours));
        polytech.setThumbID(R.drawable.img_bde);
        polytech.setIconID(R.drawable.ic_smallpolytech);
        polytech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startPolytechActivity=new Intent(v.getContext(),Polytech2Activity.class);
                v.getContext().startActivity(startPolytechActivity);
            }
        });
        dataList.add(polytech);

        HomeItem reseau=new HomeItem();
        reseau.setCardID(1);
        reseau.setThumbID(R.drawable.reseau_polytech3);
        reseau.setTitle(mainActivity.getString(R.string.reseau_polytech));
        reseau.setIconID(R.drawable.ic_smallpolytech);
        reseau.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startReseauActivity=new Intent(v.getContext(),ReseauActivity.class);
                v.getContext().startActivity(startReseauActivity);
            }
        });
        dataList.add(reseau);

        HomeItem candidat=new HomeItem();
        candidat.setCardID(2);
        candidat.setThumbID(R.drawable.img_cafet);
        candidat.setIconID(R.drawable.ic_scholarship);
        candidat.setTitle(mainActivity.getString(R.string.espace_candidat));
        candidat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startReseauActivity=new Intent(v.getContext(),CandidatActivity.class);
                v.getContext().startActivity(startReseauActivity);
            }
        });
        dataList.add(candidat);

        HomeItem quizz=new HomeItem();
        quizz.setCardID(3);
        quizz.setThumbID(R.drawable.img_quizz);
        quizz.setTitle(mainActivity.getString(R.string.quizz));
        quizz.setIconID(R.drawable.ic_question);
        quizz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startReseauActivity=new Intent(v.getContext(),QuizzActivity.class);
                v.getContext().startActivity(startReseauActivity);
            }
        });
        dataList.add(quizz);



        return dataList;
    }
}
