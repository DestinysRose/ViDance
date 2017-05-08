package com.sample.vidance;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sample.vidance.app.AppController;
import com.sample.vidance.helper.SQLiteHandler;
import com.sample.vidance.helper.SessionManager;

/**
 * Created by Michelle on 9/5/2017.
 */

public class Child extends AppCompatActivity {
    private TextView child;
    private SQLiteHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child);
        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        child = (TextView) findViewById(R.id.child);
        Button btnReg = (Button) findViewById(R.id.btnRegister);
        btnReg.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                db.setChild(child.getText().toString());
                finish();
                Intent i = new Intent(Child.this, Settings.class);
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
