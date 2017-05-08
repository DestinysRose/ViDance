package com.sample.vidance;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.sample.vidance.app.AppController;
import com.sample.vidance.helper.SQLiteHandler;

/**
 * Created by Michelle on 9/5/2017.
 */

public class Settings extends AppCompatActivity {
    private TextView child;
    private SQLiteHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        db = new SQLiteHandler(getApplicationContext());

        Button login = (Button) findViewById(R.id.btnLogin);

        login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
                Intent i = new Intent(Settings.this, Dashboard.class);
                startActivity(i);
            }
        });

        child = (TextView) findViewById(R.id.child);

        child.setText(db.selectChild());

        child.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
                Intent i = new Intent(Settings.this, Child.class);
                startActivity(i);
            }
        });
    }

    public void changeActivity(Class activity) {
        finish();
        Intent intent = new Intent(this, activity);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        changeActivity(Dashboard.class);
    }
}
