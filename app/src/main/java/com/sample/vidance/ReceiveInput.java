package com.sample.vidance;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
        TextView title = (TextView) findViewById(R.id.title);
        String text = mIntent.getStringExtra("TITLE") + "\n";
        title.setText(text);
        // Set font
        String fontPath = "fonts/CatCafe.ttf";
        Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);
        title.setTypeface(tf);
        title.setTextSize(30);

        // Send to Database
        Button btnSend = (Button) findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            //
            }
        });

        Button btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Prompt user for confirmation
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ReceiveInput.this);
                alertDialogBuilder.setTitle("Cancel?")
                        .setMessage("Are you sure you want to cancel your submission and go back?")
                        .setCancelable(false)
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Return to update behaviours
                                finish();
                                Intent intent = new Intent(ReceiveInput.this, Update.class);
                                startActivity(intent);
                                dialog.cancel();
                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Do Nothing
                                dialog.cancel();
                            }
                        });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                alertDialog.getButton(alertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#E77F7E"));
                alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#BFFFC2"));
            }
        });
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
                        finish();
                        Intent intent = new Intent(ReceiveInput.this, Update.class);
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
