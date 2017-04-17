package com.sample.vidance;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.sample.vidance.helper.SQLiteHandler;
import com.sample.vidance.helper.SessionManager;

/**
 * Created by Michelle on 15/4/2017.
 */

public class MenuItems extends AppCompatActivity {

    private TextView title, text;
    private SQLiteHandler db;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.confirmation);
        getSupportActionBar().setTitle("ViDance");
        Intent mIntent = getIntent();
        //Receive input and update content appropriately
        title = (TextView) findViewById(R.id.title);
        text = (TextView) findViewById(R.id.input_results);
        String message = getIntent().getStringExtra("SELECTED_ACTIVITY");
        title.setText(message);
        //Set Font Cat Cafe
        String fontPath = "fonts/CatCafe.ttf";
        Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);
        title.setTypeface(tf);
        title.setTextSize(30);

        Button btnSend = (Button) findViewById(R.id.btnSend);
        btnSend.setVisibility(View.GONE);

        Button btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(MenuItems.this, Dashboard.class); // Return to Dashboard
                startActivity(intent);
            }
        });

        switch (message) {
            case "Contact":
                text.setText(R.string.contact);
                break;
            case "About":
                text.setText(R.string.about);
                break;
            default:
                 text.setText("Under Construction");
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        Intent intent = new Intent(MenuItems.this, Dashboard.class); // Return to Dashboard
        startActivity(intent);
    }
}
