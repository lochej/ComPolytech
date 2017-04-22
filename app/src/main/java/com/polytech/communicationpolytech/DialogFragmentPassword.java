package com.polytech.communicationpolytech;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

/**
 * Created by Jérémy on 12/04/2017.
 */

public class DialogFragmentPassword extends DialogFragment {

    public DialogFragmentPassword() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder =new AlertDialog.Builder(getActivity());

        LayoutInflater inflater= getActivity().getLayoutInflater();

        View dialogView=inflater.inflate(R.layout.dialog_password,null);

        final EditText passedittext=(EditText) dialogView.findViewById(R.id.dialog_password);

        final LinearLayout titlecontainer=(LinearLayout) dialogView.findViewById(R.id.dialog_titlecontainer);

        final TextView incorrectPassTv=(TextView) dialogView.findViewById(R.id.dialog_incorrectpasswordTv);

        final int passHash=Constants.passHash;

        //Ajoute les options de la boite de dialogue
        builder.setView(dialogView)
                .setPositiveButton(R.string.validate, null)
                .setNegativeButton(R.string.cancel, null);


        //Cree la boite de dialogue
        AlertDialog dialog=builder.create();

        //ajoute l'action sur le click du bouton positif
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                //Recup le bouton positif
                Button positiveButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);

                positiveButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        int enteredPassHash=passedittext.getText().toString().hashCode();

                        //le mdp est correcte, on lance l'activite espace reserve
                        if(enteredPassHash==passHash){

                            Toast.makeText(v.getContext(),R.string.reserved_space,Toast.LENGTH_SHORT).show();

                            Intent startReservedSpace=new Intent(v.getContext(),ReservedSpaceActivity.class);

                            v.getContext().startActivity(startReservedSpace);

                            dialog.dismiss();
                        }
                        else{
                            incorrectPassTv.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        });

        return dialog;
    }


}
