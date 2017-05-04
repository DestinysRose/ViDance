package com.sample.vidance.listcharts;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
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
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.sample.vidance.R;
import com.sample.vidance.app.Colors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Danil on 03.05.2017.
 */

public class PieChartItem extends AppCompatActivity{

    public static final String PIE_BY_SEVERITY = "http://thevidance.com/filter/by_severity/pieChart.php";
    public static final String PIE_BY_BEHAVIOUR = "http://thevidance.com/filter/pieChart.php";

    private RadioGroup radioGroup;

    //JSON Array
    private JSONArray result;

    private String[] sArray;
    private Integer[] bNameArray;
    Typeface jf;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_piechart);

        String fontPath2 = "fonts/James_Fajardo.ttf";
        jf = Typeface.createFromAsset(getAssets(), fontPath2);

        final PieChart pieChart = (PieChart) findViewById(R.id.pieChart);

        final TextView getBehaviours = (TextView)findViewById(R.id.getBehaviours);
        final TextView getBehavioursCount = (TextView)findViewById(R.id.getBehavioursCount);
        final TextView Severity = (TextView) findViewById(R.id.severity);

        final TextView byBehaviour = (TextView)findViewById(R.id.bName);
        final TextView mild = (TextView)findViewById(R.id.mild);
        final TextView moderate = (TextView)findViewById(R.id.moderate);
        final TextView severe = (TextView)findViewById(R.id.severe);
        final TextView mildCount = (TextView)findViewById(R.id.mildCount);
        final TextView moderateCount = (TextView)findViewById(R.id.moderateCount);
        final TextView severeCount = (TextView)findViewById(R.id.severeCount);


        radioGroup = (RadioGroup) findViewById(R.id.radio);
        RadioButton byB=(RadioButton)findViewById(R.id.byBehaviour);
        RadioButton byS=(RadioButton)findViewById(R.id.bySeverity);
        byB.setTypeface(jf);
        byB.setTextSize(23);
        byS.setTypeface(jf);
        byS.setTextSize(23);

        goneVisibility();

        radioGroup = (RadioGroup) findViewById(R.id.radio);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the RadioButton selected
                RadioButton rb = (RadioButton) findViewById(checkedId);
                switch (rb.getId()) {
                    case R.id.byBehaviour:
                        try {
                            pieChart.setVisibility(View.GONE);
                            getBehaviours.setVisibility(View.GONE);
                            getBehavioursCount.setVisibility(View.GONE);
                            Severity.setVisibility(View.GONE);

                            showByBehaviour();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;

                    case R.id.bySeverity:
                        try {
                            pieChart.setVisibility(View.GONE);
                            byBehaviour.setVisibility(View.GONE);
                            mild.setVisibility(View.GONE);
                            moderate.setVisibility(View.GONE);
                            severe.setVisibility(View.GONE);
                            mildCount.setVisibility(View.GONE);
                            moderateCount.setVisibility(View.GONE);
                            severeCount.setVisibility(View.GONE);

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

        StringRequest stringRequest = new StringRequest(Request.Method.POST, PIE_BY_SEVERITY,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        PieChart pieChart = (PieChart) findViewById(R.id.pieChart);
                        pieChart.setVisibility(View.VISIBLE);

                        JSONObject reader = null;
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

                                Legend l = pieChart.getLegend();
                                l.setPosition(Legend.LegendPosition.RIGHT_OF_CHART_CENTER);
                                l.setXEntrySpace(7f);
                                l.setYEntrySpace(5f);
                                l.setYOffset(0f);
                                l.setXOffset(80f);
                                l.setWordWrapEnabled(true);
                                l.setMaxSizePercent(0.55f);

                                dataset.setColors(Colors.ALL_COLORS);
                                pieChart.setDrawHoleEnabled(false);
                                pieChart.setUsePercentValues(true);
                                pieChart.setData(data);
                                pieChart.setDescription("By Severity");
                                pieChart.setDescriptionTextSize(20);
                                pieChart.invalidate();
                                pieChart.setDrawSliceText(false);

                                pieChart.offsetLeftAndRight(0);
                                pieChart.setExtraOffsets(0,0,80,0);
                                pieChart.getCircleBox().offset(0,0);

                                dataset.setDrawValues(true);
                                dataset.setSliceSpace(3);
                                dataset.setSelectionShift(5);

                                pieChart.animateY(2000);
                                pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                                    @Override
                                    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                                        final int info = h.getXIndex();

                                        sArray = new String[]{
                                                "Bizarre body postures",
                                                "Body hitting (expect for the head) with any body part",
                                                "Clapping hands (inappropriately)",
                                                "Gazing at hands or objects",
                                                "Grimacing",
                                                "Hair pulling (tearing out patches of hair)",
                                                "Head hitting",
                                                "Inserting objects in nose, ears, anus, etc.",
                                                "Manipulating (e.g. twirling, spinning) objects",
                                                "Pacing, jumping, bouncing, running",
                                                "Pica (ingesting non-food items)",
                                                "Repetitive hand and/or finger movements",
                                                "Rocking, repetitive body movements",
                                                "Rubbing self",
                                                "Self-biting",
                                                "Self-scratching",
                                                "Sniffing objects, own body",
                                                "Teeth grinding (while awake)",
                                                "Waving or shaking arms",
                                                "Yelling and screaming"
                                        };



                                        try {
                                            JSONObject behaviourList = result.getJSONObject(info);
                                            final String severity = behaviourList.getString("severity");
                                            final Integer b_1 = behaviourList.getInt(sArray[0]);
                                            final Integer b_2 = behaviourList.getInt(sArray[1]);
                                            final Integer b_3 = behaviourList.getInt(sArray[2]);
                                            final Integer b_4 = behaviourList.getInt(sArray[3]);
                                            final Integer b_5 = behaviourList.getInt(sArray[4]);
                                            final Integer b_6 = behaviourList.getInt(sArray[5]);
                                            final Integer b_7 = behaviourList.getInt(sArray[6]);
                                            final Integer b_8 = behaviourList.getInt(sArray[7]);
                                            final Integer b_9 = behaviourList.getInt(sArray[8]);
                                            final Integer b_10 = behaviourList.getInt(sArray[9]);
                                            final Integer b_11 = behaviourList.getInt(sArray[10]);
                                            final Integer b_12 = behaviourList.getInt(sArray[11]);
                                            final Integer b_13 = behaviourList.getInt(sArray[12]);
                                            final Integer b_14 = behaviourList.getInt(sArray[13]);
                                            final Integer b_15 = behaviourList.getInt(sArray[14]);
                                            final Integer b_16 = behaviourList.getInt(sArray[15]);
                                            final Integer b_17 = behaviourList.getInt(sArray[16]);
                                            final Integer b_18 = behaviourList.getInt(sArray[17]);
                                            final Integer b_19 = behaviourList.getInt(sArray[18]);
                                            final Integer b_20 = behaviourList.getInt(sArray[19]);

                                            bNameArray = new Integer[]{b_1, b_2, b_3, b_4, b_5, b_6, b_7, b_8, b_9, b_10, b_11, b_12, b_13, b_14, b_15, b_16, b_17, b_18, b_19, b_20};

                                            TextView getBehaviours = (TextView)findViewById(R.id.getBehaviours);
                                            getBehaviours.setText("");
                                            TextView getBehavioursCount = (TextView)findViewById(R.id.getBehavioursCount);
                                            getBehavioursCount.setText("");

                                            TextView Severity = (TextView) findViewById(R.id.severity);
                                            Severity.setText("Severity: " + severity);


                                            getBehaviours.setVisibility(View.VISIBLE);
                                            getBehavioursCount.setVisibility(View.VISIBLE);
                                            Severity.setVisibility(View.VISIBLE);

                                            for(int i = 0; i < bNameArray.length; i++) {
                                                if(bNameArray[i] != 0)
                                                {
                                                    getBehaviours.append(sArray[i] + "\n");
                                                    getBehavioursCount.append(bNameArray[i] + " time(s)" + "\n");
                                                }
                                            }

                                        } catch (JSONException es) {
                                            es.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onNothingSelected() {

                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }


                    // Get the JSONArray weather
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(PieChartItem.this,error.toString(),Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void showByBehaviour() throws JSONException {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, PIE_BY_BEHAVIOUR,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        PieChart pieChart = (PieChart) findViewById(R.id.pieChart);
                        pieChart.setVisibility(View.VISIBLE);

                        JSONObject reader = null;
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

                                Legend l = pieChart.getLegend();
                                l.setPosition(Legend.LegendPosition.LEFT_OF_CHART);
                                l.setXEntrySpace(7f);
                                l.setYEntrySpace(5f);
                                l.setYOffset(0f);
                                l.setWordWrapEnabled(true);
                                l.setMaxSizePercent(0.55f);

                                dataset.setColors(Colors.ALL_COLORS);
                                pieChart.setDrawHoleEnabled(false);
                                pieChart.setUsePercentValues(true);
                                pieChart.setData(data);
                                pieChart.setDescription("By Behaviour");
                                pieChart.setDescriptionTextSize(20);
                                pieChart.invalidate();
                                pieChart.setDrawSliceText(false);

                                pieChart.offsetLeftAndRight(0);
                                pieChart.setExtraOffsets(180,0,0,0);
                                pieChart.getCircleBox().offset(0,0);

                                dataset.setDrawValues(true);
                                dataset.setSliceSpace(3);
                                dataset.setSelectionShift(5);

                                pieChart.animateY(2000);
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



                                            TextView byBehaviour = (TextView)findViewById(R.id.bName);
                                            byBehaviour.setText(" " + behaviour_name);

                                            TextView mildCount = (TextView)findViewById(R.id.mildCount);
                                            mildCount.setText(s_mild + " time(s)");
                                            TextView moderateCount = (TextView)findViewById(R.id.moderateCount);
                                            moderateCount.setText(s_moderate + " time(s)");
                                            TextView severeCount = (TextView)findViewById(R.id.severeCount);
                                            severeCount.setText(s_severe + " time(s)");

                                            TextView mild = (TextView)findViewById(R.id.mild);
                                            TextView moderate = (TextView)findViewById(R.id.moderate);
                                            TextView severe = (TextView)findViewById(R.id.severe);


                                            byBehaviour.setVisibility(View.VISIBLE);
                                            mild.setVisibility(View.VISIBLE);
                                            moderate.setVisibility(View.VISIBLE);
                                            severe.setVisibility(View.VISIBLE);
                                            mildCount.setVisibility(View.VISIBLE);
                                            moderateCount.setVisibility(View.VISIBLE);
                                            severeCount.setVisibility(View.VISIBLE);

                                        } catch (JSONException es) {
                                            es.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onNothingSelected() {

                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }


                    // Get the JSONArray weather
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(PieChartItem.this,error.toString(),Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void goneVisibility(){
        final PieChart pieChart = (PieChart) findViewById(R.id.pieChart);

        final TextView getBehaviours = (TextView)findViewById(R.id.getBehaviours);
        final TextView getBehavioursCount = (TextView)findViewById(R.id.getBehavioursCount);
        final TextView Severity = (TextView) findViewById(R.id.severity);

        final TextView byBehaviour = (TextView)findViewById(R.id.bName);
        final TextView mild = (TextView)findViewById(R.id.mild);
        final TextView moderate = (TextView)findViewById(R.id.moderate);
        final TextView severe = (TextView)findViewById(R.id.severe);
        final TextView mildCount = (TextView)findViewById(R.id.mildCount);
        final TextView moderateCount = (TextView)findViewById(R.id.moderateCount);
        final TextView severeCount = (TextView)findViewById(R.id.severeCount);

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
