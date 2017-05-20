package com.polytech.communicationpolytech;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

/**
 * Created by jeloc on 20/05/2017.
 * Dialogue servant à savoir si l'utilisateur souhaite télécharger les fichiers
 */

public class DialogConfirmDownload extends DialogFragment {

    public static final String ARG_MESSAGE_STRING ="hint";
    public static final String ARG_DIALOG_TYPE="viewconfiguration";

    public DialogConfirmDownload(){

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        String messageString= getArguments().getString(ARG_MESSAGE_STRING);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater=getActivity().getLayoutInflater();

        View dialogView= inflater.inflate(R.layout.dialog_confirmdownload,null);

        CheckBox advancedOptions=(CheckBox) dialogView.findViewById(R.id.downloaddialog_advanced);

        final Button forceDownload=(Button) dialogView.findViewById(R.id.downloaddialog_downloadall);


        builder.setTitle("Mise à jour disponible:");
        builder.setMessage(messageString);

        advancedOptions.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    forceDownload.setVisibility(View.VISIBLE);
                }
                else{
                    forceDownload.setVisibility(View.GONE);
                }
            }
        });

        //init visibility
        forceDownload.setVisibility(advancedOptions.isChecked() ? View.VISIBLE : View.GONE);


        builder.setView(dialogView)
                .setPositiveButton("Télécharger les nouveaux fichiers",null)
                .setNegativeButton("Pas maintenant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });


        return builder.create();
    }
}
