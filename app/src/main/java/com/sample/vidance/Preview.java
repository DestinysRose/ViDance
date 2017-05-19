package com.sample.vidance;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sample.vidance.helper.SQLiteHandler;
import com.sample.vidance.helper.SessionManager;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Michelle on 15/5/2017.
 */

public class Preview extends AppCompatActivity {
    private Button btnCancel, btnImage, btnAudio;
    private Uri uriAudio, uriImage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_preview);

        showToast("Please wait loading image....");
        // Load image from server
        new DownloadImageFromInternet((ImageView) findViewById(R.id.imageView)).execute(getIntent().getStringExtra("IMAGE"));
        uriImage = Uri.parse(getIntent().getStringExtra("IMAGE"));
        uriAudio = Uri.parse(getIntent().getStringExtra("AUDIO"));
       /* mediaPlayer = MediaPlayer.create(getApplicationContext(), uriAudio);
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(this, uriAudio);
            mediaPlayer.prepare();
            mediaPlayer.start();
        }catch (IOException io) {
            io.printStackTrace();
        }
        */

        String title = getIntent().getStringExtra("FOLDER");;

        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnImage = (Button) findViewById(R.id.btnImage);
        btnAudio = (Button) findViewById(R.id.btnAudio);

        //Set Font Cat Cafe
        String fontPath = "fonts/CatCafe.ttf";
        Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);
        TextView text = (TextView) findViewById(R.id.videoName);
        text.setText(title);
        text.setTypeface(tf);

        //Set Font James Farjardo
        fontPath = "fonts/James_Fajardo.ttf";
        Typeface jf = Typeface.createFromAsset(getAssets(), fontPath);
        btnCancel.setTypeface(jf);
        btnImage.setTypeface(jf);
        btnAudio.setTypeface(jf);

        buttonPress();
    }
    /*
    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer!= null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
        else {
            mediaPlayer.reset();
            try {
                mediaPlayer.setDataSource(this, uriAudio);
                mediaPlayer.prepare();
                mediaPlayer.start();
            }catch (IOException io) {
                io.printStackTrace();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mediaPlayer!= null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    } */


    private class DownloadImageFromInternet extends AsyncTask<String, Void, Bitmap> {
        ImageView imageView;

        public DownloadImageFromInternet(ImageView imageView) {
            this.imageView = imageView;
        }

        protected Bitmap doInBackground(String... urls) {
            String imageURL = urls[0];
            Bitmap bimage = null;
            try {
                InputStream in = new java.net.URL(imageURL).openStream();
                bimage = BitmapFactory.decodeStream(in);

            } catch (Exception e) {
                Log.e("Error Message", e.getMessage());
                e.printStackTrace();
            }
            return bimage;
        }

        protected void onPostExecute(Bitmap result) {
            imageView.setImageBitmap(result);
        }
    }


    /** Automatically hides all behaviour options other than1, then displays them on button press **/
    public void buttonPress() {
        // Send video
        btnImage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                alertStyler("Download story", "Open in browser to download?", "Image");
            }
        });
        // Download File
        btnAudio.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                alertStyler("Download audio", "Open in browser to download?", "Audio");
            }
        });

        // Cancel submission and go back
        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                changeActivity(Dashboard.class);
            }
        });
    }

    public AlertDialog alertStyler(String title, String msg, final String selection) { // Function to create a pop up
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Preview.this);
        alertDialogBuilder.setTitle(title)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Perform different functions for different popups on this page
                        if(selection.equals("Image")) {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, uriImage);
                            startActivity(browserIntent);
                        } else if (selection.equals("Audio")) {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, uriAudio);
                            startActivity(browserIntent);
                        }
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel(); //Do Nothing
                        if (selection.equals("upload")) {
                            showToast("Video not sent!");
                        }
                    }
                });
        //Create alert dialog
        AlertDialog ad = alertDialogBuilder.create();
        ad.show(); //Show it
        ad.getButton(ad.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#E77F7E"));
        ad.getButton(ad.BUTTON_POSITIVE).setTextColor(Color.parseColor("#23C8B2"));
        return ad;
    }

    protected void showToast(String msg)
    {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

    public void changeActivity(Class activity) {
        finish();
        Intent intent = new Intent(Preview.this, activity);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        changeActivity(Dashboard.class);
    }
}
