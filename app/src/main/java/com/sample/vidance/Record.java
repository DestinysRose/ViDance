package com.sample.vidance;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by Michelle on 3/4/2017.
 */


public class Record extends AppCompatActivity {

    private Button btnRecord, btnSend;
   // private VideoView vidView;
    private int ACTIVITY_START_CAMERA_APP = 0; //Initialise camera
    private Uri videoUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.getMenu().getItem(1).setChecked(true);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        btnRecord = (Button) findViewById(R.id.btnRecord);
        btnSend = (Button) findViewById(R.id.btnSend);

        // Set font
        String fontPath = "fonts/James_Fajardo.ttf";
        Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);
        btnRecord.setTypeface(tf);
        btnSend.setTypeface(tf);

        //vidView = (VideoView) findViewById(R.id.videoView);
        //vidView.setVisibility(View.GONE);

        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callVideoIntent = new Intent();
                callVideoIntent.setAction(MediaStore.ACTION_VIDEO_CAPTURE);
                startActivityForResult(callVideoIntent, ACTIVITY_START_CAMERA_APP);

            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });

    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTIVITY_START_CAMERA_APP && resultCode == RESULT_OK) {
            // vidView.setVisibility(View.VISIBLE); //Show videoView
            videoUri = data.getData();
            // vidView.setVideoURI(videoUri); //Set video into videoView
            Context context = getApplicationContext();
            CharSequence text = "Video saved in: " + videoUri;
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            //Pompt user to send video
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Record.this);
            //Set title
            alertDialogBuilder.setTitle("Upload video");
            //Set dialog message
            alertDialogBuilder
                    .setMessage("Do you wish to send the recorded video to the instructor for viewing?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // if this button is clicked, close
                                // current activity
                                Context context = getApplicationContext();
                                CharSequence text = "Not yet implemented~";
                                int duration = Toast.LENGTH_SHORT;
                                Toast toast = Toast.makeText(context, text, duration);
                                toast.show();
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
        else {
            Context context = getApplicationContext();
            CharSequence text = "Video not yet recorded, click 'Record Video' to record video!";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(Record.this, Features.class);
        switch (item.getItemId()) {
            case R.id.action_notifications:
                intent.putExtra("SELECTED_ITEM", 0);
                intent.putExtra("SELECTED_ACTIVITY", "Notifications");
                startActivity(intent);
                break;
            case R.id.action_settings:
                intent.putExtra("SELECTED_ITEM", 0);
                intent.putExtra("SELECTED_ACTIVITY", "Settings");
                startActivity(intent);
                break;
            case R.id.action_contact:
                intent.putExtra("SELECTED_ITEM", 0);
                intent.putExtra("SELECTED_ACTIVITY", "Contact");
                startActivity(intent);
                break;
            case R.id.action_about:
                intent.putExtra("SELECTED_ITEM", 0);
                intent.putExtra("SELECTED_ACTIVITY", "About");
                startActivity(intent);
                break;
            case R.id.action_help:
                intent.putExtra("SELECTED_ITEM", 0);
                intent.putExtra("SELECTED_ACTIVITY", "Help");
                startActivity(intent);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Intent intent = new Intent(Record.this, Features.class);
            switch (item.getItemId()) {
                case R.id.navigation_dashboard:
                    finish();
                    return true;
                case R.id.navigation_record:
                    //Do Nothing
                    return true;
                case R.id.navigation_input:
                    finish();
                    intent = new Intent(Record.this, Update.class);
                    intent.putExtra("SELECTED_ITEM", 2);
                    intent.putExtra("SELECTED_ACTIVITY", "Update Behaviours");
                    intent.putExtra("SELECTED_CONTENT", 0);
                    startActivity(intent);
                    return true;
                case R.id.navigation_target:
                    finish();
                    intent.putExtra("SELECTED_ITEM", 3);
                    intent.putExtra("SELECTED_ACTIVITY", "Target Behaviours");
                    intent.putExtra("SELECTED_CONTENT", 1);
                    startActivity(intent);
                    return true;
                case R.id.navigation_report:
                    finish();
                    intent.putExtra("SELECTED_ITEM", 4);
                    intent.putExtra("SELECTED_ACTIVITY", "Generate Reports");
                    intent.putExtra("SELECTED_CONTENT", 2);
                    startActivity(intent);
                    return true;
            }
            return false;
        }
    };

}
