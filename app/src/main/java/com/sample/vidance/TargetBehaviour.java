package com.sample.vidance;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Danil on 10.04.2017.
 */

public class TargetBehaviour  extends AppCompatActivity {

    ArrayList<HashMap<String, String>> dataList;

    ListView list;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_target);

        String fontPath = "fonts/CatCafe.ttf";

        TextView txtCat = (TextView) findViewById(R.id.catcafe);
        // Loading Font Face
        Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);
        // Applying font
        txtCat.setTypeface(tf);

        list = (ListView) findViewById(R.id.getReport);
        dataList = new ArrayList<HashMap<String,String>>();

        LineChart lineChart = (LineChart) findViewById(R.id.chart);

        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(8f, 0));
        entries.add(new Entry(8f, 1));
        entries.add(new Entry(8f, 2));
        entries.add(new Entry(8f, 3));
        entries.add(new Entry(8f, 4));
        entries.add(new Entry(8f, 5));

        LineDataSet dataset = new LineDataSet(entries, "Implementation in progress");

        ArrayList<String> labels = new ArrayList<String>();
        labels.add("B1");
        labels.add("B2");
        labels.add("B3");
        labels.add("B4");
        labels.add("B5");
        labels.add("B6");

        LineData data = new LineData(labels, dataset);
        dataset.setColors(ColorTemplate.COLORFUL_COLORS); //
        dataset.setDrawCubic(true);
        dataset.setDrawFilled(true);

        lineChart.setData(data);
        lineChart.animateY(1000);
        Button toReport = (Button) findViewById(R.id.toReport);
        toReport.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(TargetBehaviour.this, Report.class);
                startActivity(intent);
            }
        });
        Button toHome = (Button) findViewById(R.id.toHome);
        toHome.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(TargetBehaviour.this, Dashboard.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(TargetBehaviour.this, Dashboard.class); // Return to Dashboard
        startActivity(intent);
        finish();
    }
}
