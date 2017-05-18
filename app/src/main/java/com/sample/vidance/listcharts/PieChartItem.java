package com.sample.vidance.listcharts;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.sample.vidance.R;
import com.sample.vidance.app.Colors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by Danil on 03.05.2017.
 */

public class PieChartItem extends AppCompatActivity{

    public static final String PIE_BY_SEVERITY = "http://thevidance.com/filter/by_severity/pieChart.php";
    public static final String PIE_BY_BEHAVIOUR = "http://thevidance.com/filter/pieChart.php";

    //JSON Array
    private JSONArray result;

    PieChart pieChart;

    TextView getBehaviours, getBehavioursCount, Severity;

    TextView byBehaviour, mild, moderate, severe, mildCount, moderateCount, severeCount;

    Typeface jf, tf;

    private String arrayBehaviour[];
    private int bArray[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_piechart);

        String fontPath = "fonts/CatCafe.ttf";
        tf = Typeface.createFromAsset(getAssets(), fontPath);
        String fontPath2 = "fonts/James_Fajardo.ttf";
        jf = Typeface.createFromAsset(getAssets(), fontPath2);

        pieChart = (PieChart) findViewById(R.id.pieChart);

        getBehaviours = (TextView)findViewById(R.id.getBehaviours);
        getBehavioursCount = (TextView)findViewById(R.id.getBehavioursCount);
        Severity = (TextView) findViewById(R.id.severity);

        byBehaviour = (TextView)findViewById(R.id.bName);
        mild = (TextView)findViewById(R.id.mild);
        moderate = (TextView)findViewById(R.id.moderate);
        severe = (TextView)findViewById(R.id.severe);
        mildCount = (TextView)findViewById(R.id.mildCount);
        moderateCount = (TextView)findViewById(R.id.moderateCount);
        severeCount = (TextView)findViewById(R.id.severeCount);

        arrayBehaviour = getResources().getStringArray(R.array.behaviour_arrays);
        bArray = new int[arrayBehaviour.length];

        RadioButton byB=(RadioButton)findViewById(R.id.byBehaviour);
        RadioButton byS=(RadioButton)findViewById(R.id.bySeverity);
        byB.setTypeface(jf);
        byB.setTextSize(23);
        byS.setTypeface(jf);
        byS.setTextSize(23);

        setTypeface();
        hideChart();

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radio);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the RadioButton selected
                RadioButton rb = (RadioButton) findViewById(checkedId);
                switch (rb.getId()) {
                    case R.id.byBehaviour:
                        try {
                            hideChart();
                            showByBehaviour();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;

                    case R.id.bySeverity:
                        try {
                            hideChart();
                            showBySeverity();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }
        });
    }

    private void showBySeverity() throws JSONException {
        StringRequest stringRequest = new StringRequest(PIE_BY_SEVERITY,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        PieChart pieChart = (PieChart) findViewById(R.id.pieChart);
                        pieChart.setVisibility(View.VISIBLE);

                        JSONObject reader;
                        try {
                            reader = new JSONObject(response);

                            result = reader.getJSONArray("result");
                            if(result.length() == 0)
                                Toast.makeText(PieChartItem.this, "You don't have any records", Toast.LENGTH_LONG).show();
                            else {

                                ArrayList<Entry> entries = new ArrayList<>();
                                ArrayList<String> labels = new ArrayList<>();

                                int count;

                                for (int i = 0; i < result.length(); i++) {
                                    final JSONObject chartData = result.getJSONObject(i);
                                    final String severity = chartData.getString("severity");
                                    final String counter = chartData.getString("counter");

                                    count = Integer.parseInt(counter);

                                    entries.add(new Entry(count, i));
                                    labels.add(severity);
                                }

                                PieDataSet dataset = new PieDataSet(entries, "");
                                PieData data = new PieData(labels, dataset);

                                setPieDataSet(dataset);
                                initPieChartS(data);

                                pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                                    @Override
                                    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                                        final int info = h.getXIndex();

                                        String[] bNameArray = (arrayBehaviour);

                                        try {
                                            JSONObject behaviourList = result.getJSONObject(info);
                                            final String severity = behaviourList.getString("severity");
                                            for(int i = 1; i<bNameArray.length; i++){
                                                bArray[i] = behaviourList.getInt(bNameArray[i]);
                                            }

                                            getBehaviours.setText("");
                                            getBehavioursCount.setText("");
                                            Severity.setText("Severity: " + severity);

                                            getBehaviours.setVisibility(View.VISIBLE);
                                            getBehavioursCount.setVisibility(View.VISIBLE);
                                            Severity.setVisibility(View.VISIBLE);

                                            for(int i = 0; i < bArray.length; i++) {
                                                if(bArray[i] != 0)
                                                {
                                                    getBehaviours.append(bNameArray[i] + "\n");
                                                    getBehavioursCount.append(bArray[i] + " time(s)" + "\n");
                                                }
                                            }

                                        } catch (JSONException es) {
                                            es.printStackTrace();
                                            Toast.makeText(PieChartItem.this, "Unexpected error. Please retry", Toast.LENGTH_LONG).show();
                                        }
                                    }

                                    @Override
                                    public void onNothingSelected() {
                                        getBehaviours.setVisibility(View.GONE);
                                        getBehavioursCount.setVisibility(View.GONE);
                                        Severity.setVisibility(View.GONE);
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(PieChartItem.this, "Unexpected error. Please retry", Toast.LENGTH_LONG).show();
                        }
                    }


                    // Get the JSONArray weather
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(PieChartItem.this, "Unexpected error. Please retry", Toast.LENGTH_LONG).show();
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
    private class MyValueFormatter implements ValueFormatter {

        private DecimalFormat mFormat;

        MyValueFormatter() {
            mFormat = new DecimalFormat("###,###,##0.0"); // use no decimals
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {

            if(value <= 4)
                return "";
            else
                return mFormat.format(value) + "%";
        }
    }

    private void initPieChartS(PieData data){

        Legend l = pieChart.getLegend();
        l.setPosition(Legend.LegendPosition.RIGHT_OF_CHART_CENTER);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(5f);
        l.setYOffset(0f);
        l.setXOffset(80f);
        l.setWordWrapEnabled(true);
        l.setMaxSizePercent(0.55f);
        l.setTypeface(tf);

        pieChart.setDrawHoleEnabled(false);
        pieChart.setUsePercentValues(true);
        pieChart.setData(data);
        pieChart.setDescription("By Severity");
        pieChart.setDescriptionTypeface(tf);
        pieChart.setDescriptionTextSize(20);
        pieChart.invalidate();
        pieChart.setDrawSliceText(false);

        pieChart.offsetLeftAndRight(0);
        pieChart.setExtraOffsets(0,0,80,0);
        pieChart.getCircleBox().offset(0,0);
        pieChart.animateY(2000);
    }

    private void showByBehaviour() throws JSONException {
        StringRequest stringRequest = new StringRequest(PIE_BY_BEHAVIOUR,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        PieChart pieChart = (PieChart) findViewById(R.id.pieChart);
                        pieChart.setVisibility(View.VISIBLE);

                        JSONObject reader;
                        try {
                            reader = new JSONObject(response);

                            result = reader.getJSONArray("result");
                            if(result.length() == 0)
                                Toast.makeText(PieChartItem.this, "You don't have any records", Toast.LENGTH_LONG).show();
                            else {

                                ArrayList<Entry> entries = new ArrayList<>();
                                ArrayList<String> labels = new ArrayList<>();

                                int count;

                                for (int i = 0; i < result.length(); i++) {
                                    final JSONObject chartData = result.getJSONObject(i);
                                    final String bName = chartData.getString("bName");
                                    final String counter = chartData.getString("counter");

                                    count = Integer.parseInt(counter);

                                    entries.add(new Entry(count, i));
                                    labels.add(bName);
                                }

                                PieDataSet dataset = new PieDataSet(entries, "");
                                PieData data = new PieData(labels, dataset);

                                setPieDataSet(dataset);
                                initPieChartB(data);

                                pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                                    @Override
                                    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                                        final int info = h.getXIndex();

                                        try {
                                            JSONObject behaviourList = result.getJSONObject(info);
                                            final String behaviour_name = behaviourList.getString("bName");
                                            final String s_mild = behaviourList.getString("Mild");
                                            final String s_moderate = behaviourList.getString("Moderate");
                                            final String s_severe = behaviourList.getString("Severe");

                                            byBehaviour.setText(" " + behaviour_name);

                                            mildCount.setText(s_mild + " time(s)");
                                            moderateCount.setText(s_moderate + " time(s)");
                                            severeCount.setText(s_severe + " time(s)");

                                            byBehaviour.setVisibility(View.VISIBLE);
                                            mild.setVisibility(View.VISIBLE);
                                            moderate.setVisibility(View.VISIBLE);
                                            severe.setVisibility(View.VISIBLE);
                                            mildCount.setVisibility(View.VISIBLE);
                                            moderateCount.setVisibility(View.VISIBLE);
                                            severeCount.setVisibility(View.VISIBLE);

                                        } catch (JSONException es) {
                                            es.printStackTrace();
                                            Toast.makeText(PieChartItem.this, "Unexpected error. Please retry", Toast.LENGTH_LONG).show();
                                        }
                                    }

                                    @Override
                                    public void onNothingSelected() {
                                        byBehaviour.setVisibility(View.GONE);
                                        mild.setVisibility(View.GONE);
                                        moderate.setVisibility(View.GONE);
                                        severe.setVisibility(View.GONE);
                                        mildCount.setVisibility(View.GONE);
                                        moderateCount.setVisibility(View.GONE);
                                        severeCount.setVisibility(View.GONE);
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(PieChartItem.this, "Unexpected error. Please retry", Toast.LENGTH_LONG).show();
                        }
                    }
                    // Get the JSONArray weather
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(PieChartItem.this, "Unexpected error. Please retry", Toast.LENGTH_LONG).show();
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void setPieDataSet(PieDataSet dataset){
        dataset.setDrawValues(true);
        dataset.setValueFormatter(new PieChartItem.MyValueFormatter());
        dataset.setSliceSpace(3);
        dataset.setSelectionShift(5);
        dataset.setColors(Colors.ALL_COLORS);
    }

    private void initPieChartB(PieData data){

        Legend l = pieChart.getLegend();
        l.setPosition(Legend.LegendPosition.LEFT_OF_CHART);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(5f);
        l.setYOffset(0f);
        l.setXOffset(10f);
        l.setWordWrapEnabled(true);
        l.setMaxSizePercent(0.55f);
        l.setTypeface(tf);

        pieChart.setDrawHoleEnabled(false);
        pieChart.setUsePercentValues(true);
        pieChart.setData(data);
        pieChart.setDescription("By Behaviour");
        pieChart.setDescriptionTypeface(tf);
        pieChart.setDescriptionTextSize(20);
        pieChart.invalidate();
        pieChart.setDrawSliceText(false);
        pieChart.offsetLeftAndRight(0);
        pieChart.setExtraOffsets(180,0,0,0);
        pieChart.getCircleBox().offset(0,0);
        pieChart.animateY(2000);
    }

    private void setTypeface(){
        getBehaviours.setTypeface(tf);
        getBehavioursCount.setTypeface(tf);
        Severity.setTypeface(tf);
        byBehaviour.setTypeface(tf);
        mild.setTypeface(tf);
        moderate.setTypeface(tf);
        severe.setTypeface(tf);
        mildCount.setTypeface(tf);
        moderateCount.setTypeface(tf);
        severeCount.setTypeface(tf);
    }

    private void hideChart(){
        pieChart.setVisibility(View.GONE);
        getBehaviours.setVisibility(View.GONE);
        getBehavioursCount.setVisibility(View.GONE);
        Severity.setVisibility(View.GONE);
        byBehaviour.setVisibility(View.GONE);
        mild.setVisibility(View.GONE);
        moderate.setVisibility(View.GONE);
        severe.setVisibility(View.GONE);
        mildCount.setVisibility(View.GONE);
        moderateCount.setVisibility(View.GONE);
        severeCount.setVisibility(View.GONE);
    }
}
