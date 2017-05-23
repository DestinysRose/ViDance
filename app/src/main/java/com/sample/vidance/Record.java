package com.sample.vidance;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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

import com.sample.vidance.app.AppController;
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
    private static final int SELECT_VIDEO = 3; //Set to allow video selection
    private String userID;
    private Uri videoUri = null;
    private File dir;
    private String CAPTURE_TITLE, selectedPath;
    private SQLiteHandler db;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.sample.vidance.R.layout.activity_record);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(com.sample.vidance.R.id.navigation);
        navigation.getMenu().getItem(1).setChecked(true);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        btnRecord = (Button) findViewById(com.sample.vidance.R.id.btnRecord);
        btnSend = (Button) findViewById(com.sample.vidance.R.id.btnSend);

        // Session manager
        session = new SessionManager(getApplicationContext());

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // Get current user
        userID = db.getUserID();

        // Set font
        String fontPath = "fonts/James_Fajardo.ttf";
        Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);
        btnRecord.setTypeface(tf);
        btnSend.setTypeface(tf);

        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            dir = new File(getCacheDir(), "ViDance"); //Install to internal storage
        } else {
            dir = new File (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + File.separator + "ViDance"); //Install to SD
        }
        boolean success = true;
        if (!dir.exists()) {
            success = dir.mkdirs();
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
                Intent intent = new Intent();
                intent.setType("video/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select a Video "), SELECT_VIDEO);
            }
        });
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTIVITY_START_CAMERA_APP && resultCode == RESULT_OK) {
            videoUri = data.getData();
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, videoUri)); // Update to show video in gallery.
            //Get the videoPath to sent to uploadVideo()
            File video = new File(videoUri.getPath());
            selectedPath = video.getAbsolutePath();
            Toast.makeText(getApplicationContext(), "Video saved to Gallery!", Toast.LENGTH_SHORT).show();
            //Create an alert to ask user if they wish to upload the video recorded
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Record.this);
            alertDialogBuilder.setTitle("Upload video")
                    .setMessage("Do you wish to send the recorded video to the instructor for viewing?")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                uploadVideo(); //Send to database
                            }
                        })
                    .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel(); //Do Nothing
                                Toast.makeText(getApplicationContext(), "Video not sent!", Toast.LENGTH_SHORT).show();
                            }
                        });
            //Create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();
            //Show it
            alertStyle(alertDialog);
        }
        else if (requestCode == SELECT_VIDEO && resultCode == RESULT_OK) {
            System.out.println("SELECT_VIDEO");
            Uri selectedImageUri = data.getData();
            selectedPath = getPath(selectedImageUri);
            String filename = selectedPath.substring(selectedPath.lastIndexOf("/")+1); //Retrieve filename from filepath
            //Create an alert to ask user if they wish to upload the video selected
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Record.this);
            alertDialogBuilder.setTitle("Upload video")
                    .setMessage("Video " + filename + " selected, do you wish to send to the instructor for viewing?")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            uploadVideo(); // Send to database
                        }
                    })
                    .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel(); // Do Nothing
                            Toast.makeText(getApplicationContext(), "Video not sent!", Toast.LENGTH_SHORT).show();
                        }
                    });
            //Create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();
            //Show it
            alertStyle(alertDialog);
        }
    }

    public String getPath(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if(cursor!=null && cursor.getCount()>0 ) {
            cursor.moveToFirst();
        }
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();


        cursor = getContentResolver().query(
                android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI,  null,
                MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        if(cursor!=null && cursor.getCount()>0 ) {
            cursor.moveToFirst();
        }
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
        cursor.close();
        return path;
    }

    private void uploadVideo() {
        class UploadVideo extends AsyncTask<Void, Void, String> {

            ProgressDialog uploading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                uploading = ProgressDialog.show(Record.this, "Uploading File", "Please wait...", false, false);
            }

            @Override
            protected void onPostExecute(String s) {
                uploading.dismiss();
                //super.onPostExecute(s);
                Toast.makeText(getApplicationContext(), "Video successfully uploaded!", Toast.LENGTH_SHORT).show();
            }

            @Override
            protected String doInBackground(Void... params) {
                Upload u = new Upload();
                return u.uploadVideo(selectedPath, userID);
            }
        }
        UploadVideo uv = new UploadVideo();
        uv.execute();
    }

    public void alertStyle(AlertDialog ad) {
        ad.show(); //Show it
        ad.getButton(ad.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#E77F7E"));
        ad.getButton(ad.BUTTON_POSITIVE).setTextColor(Color.parseColor("#23C8B2"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(Record.this, MenuItems.class);
        switch(item.getItemId()) {
            case R.id.action_notifications:
                Toast.makeText(getApplicationContext(), "Currently unavailable!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_settings:
                Toast.makeText(getApplicationContext(), "Currently unavailable!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_contact:
                changeActivity(Report.class);
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
                    changeActivity(Dashboard.class);
                    return true;
                case R.id.navigation_record:
                    //Do Nothing
                    return true;
                case R.id.navigation_input:
                    changeActivity(Update.class);
                    return true;
                case R.id.navigation_target:
                    changeActivity(TargetBehaviour.class);
                    return true;
                case R.id.navigation_report:
                    changeActivity(Report.class);
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
        changeActivity(Login.class);
    }

    public void changeActivity(Class activity) {
        finish();
        Intent intent = new Intent(this, activity);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        finish();
        Intent intent = new Intent(Record.this, Dashboard.class); // Return to Dashboard
        startActivity(intent);
    }
}
