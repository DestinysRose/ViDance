package com.sample.vidance;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.sample.vidance.helper.SQLiteHandler;
import com.sample.vidance.helper.SessionManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Michelle on 3/4/2017.
 */


public class Record extends AppCompatActivity {

    private Button btnRecord, btnSend;
    private int ACTIVITY_START_CAMERA_APP = 0; //Initialise camera
    private Uri videoUri = null;
    String CAPTURE_TITLE;
    private SQLiteHandler db;
    private SessionManager session;

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

        File dir = new File (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + File.separator + "ViDance");
        boolean success = true;
        if (!dir.exists()) {
            success = dir.mkdir();
        }
        if (success) {
            // Folder exists
        } else {
            Toast.makeText(getApplicationContext(), "Unable to create folder for saving!", Toast.LENGTH_SHORT).show();
        }

        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set video name as date & time
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
                CAPTURE_TITLE = sdf.format(new Date()) + ".mp4";


                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + File.separator + "ViDance", CAPTURE_TITLE);
                Uri outputFileUri = Uri.fromFile(file);
                Intent callVideoIntent = new Intent(android.provider.MediaStore.ACTION_VIDEO_CAPTURE);
                callVideoIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, outputFileUri);
                startActivityForResult(callVideoIntent, ACTIVITY_START_CAMERA_APP);

            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoUri != null) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Record.this);
                    // set title
                    alertDialogBuilder.setTitle("Upload video");
                    // set dialog message
                    alertDialogBuilder
                            .setMessage("Are you sure you want to send the previously recorded video to the instructor for viewing?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // Send to database
                                    Toast.makeText(getApplicationContext(), "Not yet implemented~", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // Do Nothing
                                    dialog.cancel();
                                }
                            });
                    // create alert dialog
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    // show it
                    alertDialog.show();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Video not yet recorded, click 'Record Video' to record video!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTIVITY_START_CAMERA_APP && resultCode == RESULT_OK) {
            videoUri = data.getData();
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, videoUri)); // Update to show video in gallery.

            Toast.makeText(getApplicationContext(), "Video successfully saved!" , Toast.LENGTH_SHORT).show();
            //Prompt user to send video
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Record.this);
            //Set title
            alertDialogBuilder.setTitle("Upload video");
            //Set dialog message
            alertDialogBuilder
                    .setMessage("Do you wish to send the recorded video to the instructor for viewing?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Send to database
                                Toast.makeText(getApplicationContext(), "Not yet implemented~", Toast.LENGTH_SHORT).show();
                            }
                        })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Do Nothing
                                dialog.cancel();
                            }
                        });
            //Create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();
            //Show it
            alertDialog.show();
        }
        else {
            Toast.makeText(getApplicationContext(), "Video not yet recorded, click 'Record Video' to record video!", Toast.LENGTH_SHORT).show();
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
        switch(item.getItemId()) {
            case R.id.action_notifications:
                Toast.makeText(getApplicationContext(), "Currently unavailable!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_settings:
                Toast.makeText(getApplicationContext(), "Currently unavailable!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_contact:
                finish();
                intent.putExtra("SELECTED_ACTIVITY", "Contact");
                startActivity(intent);
                break;
            case R.id.action_about:
                finish();
                intent.putExtra("SELECTED_ACTIVITY", "About");
                startActivity(intent);
                break;
            case R.id.action_help:
                Toast.makeText(getApplicationContext(), "Currently unavailable!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_logout:
                logoutUser();
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
            switch (item.getItemId()) {
                case R.id.navigation_dashboard:
                    finish();
                    Intent intent = new Intent(Record.this, Dashboard.class); //Record Session page
                    startActivity(intent);
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
                    intent = new Intent(Record.this, TargetBehaviour.class);
                    intent.putExtra("SELECTED_ITEM", 3);
                    intent.putExtra("SELECTED_ACTIVITY", "Target Behaviours");
                    intent.putExtra("SELECTED_CONTENT", 1);
                    startActivity(intent);
                    return true;
                case R.id.navigation_report:
                    finish();
                    intent = new Intent(Record.this, Report.class);
                    intent.putExtra("SELECTED_ITEM", 4);
                    intent.putExtra("SELECTED_ACTIVITY", "Generate Reports");
                    intent.putExtra("SELECTED_CONTENT", 2);
                    startActivity(intent);
                    return true;
            }
            return false;
        }
    };

    private void logoutUser() {
        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());

        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(Record.this, Login.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        finish();
        Intent intent = new Intent(Record.this, Dashboard.class); // Return to Dashboard
        startActivity(intent);
    }
}
