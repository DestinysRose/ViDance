package com.sample.vidance;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Michelle on 31/3/2017.
 */

public class Create extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Dashboard");
        LinearLayout parent = new LinearLayout(this);

        parent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
        parent.setOrientation(LinearLayout.HORIZONTAL);

//children of parent linearlayout

        ImageView iv = new ImageView(this);

        LinearLayout layout2 = new LinearLayout(this);

        layout2.setLayoutParams(new LinearLayout.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
        parent.setOrientation(LinearLayout.VERTICAL);

        parent.addView(iv);
        parent.addView(layout2);

//children of layout2 LinearLayout

        TextView tv1 = new TextView(this);
        TextView tv2 = new TextView(this);
        TextView tv3 = new TextView(this);
        TextView tv4 = new TextView(this);

        layout2.addView(tv1);
        layout2.addView(tv2);
        layout2.addView(tv3);
        layout2.addView(tv4);

    }
}