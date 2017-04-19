package com.sample.vidance;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.graphics.Typeface;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.sample.vidance.helper.SQLiteHandler;
import com.sample.vidance.helper.SessionManager;

/**
 * Created by Michelle on 22/3/2017.
 */
//Some changes

public class Dashboard extends AppCompatActivity {
    private static final int TIME_LIMIT = 1500;
    private static long backPressed;
    private SQLiteHandler db;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Dashboard");
        // Font path
        String fontPath = "fonts/CatCafe.ttf";
        // text view label
        TextView txtCat = (TextView) findViewById(R.id.catcafe);
        TextView txtCat2 = (TextView) findViewById(R.id.catcafe2);
        // Loading Font Face
        Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);
        // Applying font
        txtCat.setTypeface(tf);
        txtCat2.setTypeface(tf);

        //Link to Notifications
        ImageButton imgBtn = (ImageButton) findViewById(R.id.btnNotifications);
        imgBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Currently unavailable!", Toast.LENGTH_SHORT).show();
            }
        });

        //Link to Gallery
        ImageButton imgBtn2 = (ImageButton) findViewById(R.id.btnGallery);
        imgBtn2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(Dashboard.this, Gallery.class);
                intent.putExtra("SELECTED_ITEM", 0);
                intent.putExtra("SELECTED_ACTIVITY", "Gallery");
                startActivity(intent);
            }
        });
        //Link to Record Session
        ImageButton imgBtn3 = (ImageButton) findViewById(R.id.btnRecord);
        imgBtn3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(Dashboard.this, Record.class);
                startActivity(intent);
            }
        });
        //Link to Update Behaviours
        ImageButton imgBtn4 = (ImageButton) findViewById(R.id.btnInput);
        imgBtn4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(Dashboard.this, Update.class);
                startActivity(intent);
            }
        });
        //Link to Target Behaviours
        ImageButton imgBtn5 = (ImageButton) findViewById(R.id.btnTarget);
        imgBtn5.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(Dashboard.this, TargetBehaviour.class);
                intent.putExtra("SELECTED_ITEM", 3);
                intent.putExtra("SELECTED_ACTIVITY", "Target Behaviours");
                intent.putExtra("SELECTED_CONTENT", 1);
                startActivity(intent);
            }
        });
        //Link to Generate Reports
        ImageButton imgBtn6 = (ImageButton) findViewById(R.id.btnReport);
        imgBtn6.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(Dashboard.this, Report.class);
                intent.putExtra("SELECTED_ITEM", 4);
                intent.putExtra("SELECTED_ACTIVITY", "Generate Reports");
                intent.putExtra("SELECTED_CONTENT", 2);
                startActivity(intent);
            }
        });
        //Link to Settings
        ImageButton imgBtn7 = (ImageButton) findViewById(R.id.btnSetting);
        imgBtn7.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Currently unavailable!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logoutUser() {
        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());
        // session manager
        session = new SessionManager(getApplicationContext());
        session.setLogin(false);
        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(Dashboard.this, Login.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent intent = new Intent(Dashboard.this, MenuItems.class);
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

    @Override
    public void onBackPressed() {
        if(TIME_LIMIT + backPressed > System.currentTimeMillis()) {
            super.onBackPressed(); //Close application on double back
        }
        else
        {
            Toast.makeText(getApplicationContext(), "Press the 'Back' button again to exit.", Toast.LENGTH_SHORT).show();
        }
        backPressed = System.currentTimeMillis();
    }
}
