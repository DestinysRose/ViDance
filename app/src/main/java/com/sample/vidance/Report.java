package com.sample.vidance;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sample.vidance.helper.SQLiteHandler;
import com.sample.vidance.helper.SessionManager;
import com.sample.vidance.listcharts.BarChartItem;
import com.sample.vidance.listcharts.LineChartItem;
import com.sample.vidance.listcharts.PieChartItem;

/**
 * Created by Danil on 06.04.2017.
 */

public class Report extends AppCompatActivity {

    //Show dialog when fetching
    private ProgressDialog pDialog;


    private SQLiteHandler db;
    private SessionManager session;

    Typeface tf;
    Typeface jf;

    private TextView mTextMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.resource_report);

        String fontPath = "fonts/CatCafe.ttf";
        tf = Typeface.createFromAsset(getAssets(), fontPath);
        String fontPath2 = "fonts/James_Fajardo.ttf";
        jf = Typeface.createFromAsset(getAssets(), fontPath2);

        mTextMessage = (TextView) findViewById(R.id.textView2);
        mTextMessage.setText(R.string.title_report);
        mTextMessage.setTypeface(tf);

        TextView txtJf1 = (TextView) findViewById(R.id.textView3);
        TextView txtJf2 = (TextView) findViewById(R.id.textView4);
        TextView txtJf3 = (TextView) findViewById(R.id.textView5);
        txtJf1.setTypeface(tf);
        txtJf1.setTextSize(15);
        txtJf2.setTypeface(tf);
        txtJf2.setTextSize(15);
        txtJf3.setTypeface(tf);
        txtJf3.setTextSize(15);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.getMenu().getItem(4).setChecked(true);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        Button btn = (Button) findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Report.this, LineChartItem.class);
                startActivity(intent);
            }
        });

        Button btnToBar = (Button) findViewById(R.id.toBar);
        btnToBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Report.this, BarChartItem.class);
                startActivity(intent);
            }
        });

        Button btnToPie = (Button) findViewById(R.id.toPie);
        btnToPie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Report.this, PieChartItem.class);
                startActivity(intent);
            }
        });

        btn.setTypeface(jf);
        btn.setTextSize(25);
        btnToBar.setTypeface(jf);
        btnToBar.setTextSize(25);
        btnToPie.setTypeface(jf);
        btnToPie.setTextSize(25);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_dashboard:
                    finish();
                    Intent intent = new Intent(Report.this, Dashboard.class); //Record Session page
                    startActivity(intent);
                    return true;
                case R.id.navigation_record:
                    finish();
                    intent = new Intent(Report.this, Record.class);
                    startActivity(intent);
                    return true;
                case R.id.navigation_input:
                    finish();
                    intent = new Intent(Report.this, Update.class);
                    startActivity(intent);
                    return true;
                case R.id.navigation_target:
                    finish();
                    intent = new Intent(Report.this, TargetBehaviour.class);
                    /**intent.putExtra("SELECTED_ITEM", 3);
                     intent.putExtra("SELECTED_ACTIVITY", "Target Behaviours");
                     intent.putExtra("SELECTED_CONTENT", 1);**/
                    startActivity(intent);
                    return true;
                case R.id.navigation_report:
                    //Do Nothing
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
        Intent intent = new Intent(Report.this, Login.class);
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
        Intent intent = new Intent(Report.this, MenuItems.class);
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
        //Prompt user to send video
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Report.this);
        //Set title
        alertDialogBuilder.setTitle("Cancel?");
        //Set dialog message
        alertDialogBuilder
                .setMessage("Are you sure to go back?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, close
                        // current activity
                        finish();
                        Intent intent = new Intent(Report.this, Dashboard.class);
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