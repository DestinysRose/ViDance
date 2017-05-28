package com.sample.vidance;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sample.vidance.helper.SQLiteHandler;
import com.sample.vidance.helper.SessionManager;

/**
 * Created by Michelle on 15/4/2017.
 */

public class MenuItems extends AppCompatActivity {

    private TextView title, text;
    private Button btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.confirmation);
        getSupportActionBar().setTitle("ViDance");
        //Receive input and update content appropriately
        title = (TextView) findViewById(R.id.title);
        btnSend = (Button) findViewById(R.id.btnSend);
        text = (TextView) findViewById(R.id.input_results);
        String message = getIntent().getStringExtra("SELECTED_ACTIVITY");
        title.setText(message);
        //Set Font Cat Cafe
        String fontPath = "fonts/CatCafe.ttf";
        Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);
        title.setTypeface(tf);
        title.setTextSize(30);

        btnSend.setText("CONTACT INSTRUCTOR");
        btnSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendEmail();
            }
        });

        Button btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setText("Back");
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
    protected void sendEmail() {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{"customer_service@thevidance.com"});
        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(MenuItems.this, "There are no applications capable of sending emails on your phone, please install one to continue.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        Intent intent = new Intent(this, Dashboard.class); // Return to Dashboard
        startActivity(intent);
    }
}
