package com.sample.vidance;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Michelle on 30/3/2017.
 */

public class ReceiveInput extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.confirmation);
        Intent mIntent = getIntent();
        TextView mTextMessage = (TextView) findViewById(R.id.input_results);
        String result = mIntent.getStringExtra("RESULT");
        mTextMessage.setText(result);
    }
    @Override
    public void onBackPressed() {
        //Prompt user to send video
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ReceiveInput.this);
        //Set title
        alertDialogBuilder.setTitle("Cancel?");
        //Set dialog message
        alertDialogBuilder
                .setMessage("Are you sure you cancel your submission and go back?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, close
                        // current activity
                        Intent intent = new Intent(ReceiveInput.this, Dashboard.class);
                        startActivity(intent);
                        dialog.cancel();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });
        //Create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        //Show it
        alertDialog.show();
    }
}
