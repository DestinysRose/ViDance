package com.sample.vidance.listcharts;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.sample.vidance.Login;
import com.sample.vidance.MenuItems;
import com.sample.vidance.R;
import com.sample.vidance.Settings;
import com.sample.vidance.app.AppConfig;
import com.sample.vidance.app.Colors;
import com.sample.vidance.helper.SQLiteHandler;
import com.sample.vidance.helper.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Danil on 03.05.2017.
 */

//Initializing pie charts graphs with options to show records grouped by severity or by behaviour
public class PieChartItem extends AppCompatActivity{

    private SQLiteHandler db;
    private SessionManager session;

    //Show dialog when fetching
    private ProgressDialog pDialog;

    //URL init to get filtered data
    public static final String PIE_BY_SEVERITY = "http://thevidance.com/charts/pie_chart/pieChartS";
    public static final String PIE_BY_BEHAVIOUR = "http://thevidance.com/charts/pie_chart/pieChartB";

    //JSON Array
    private JSONArray result;

    //Init pie chart variable
    PieChart pieChart;
    //Init vars that will keep data from response
    TextView getBehaviours, getBehavioursCount, Severity;
    TextView byBehaviour, mild, moderate, severe, mildCount, moderateCount, severeCount;

    Typeface jf, tf;

    //Global arrays
    private String arrayBehaviour[];
    private int bArray[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_piechart);

        // Session manager
        session = new SessionManager(getApplicationContext());

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        pDialog.setMessage("Loading ...");

        //Load typefaces from project resources
        String fontPath = "fonts/CatCafe.ttf";
        tf = Typeface.createFromAsset(getAssets(), fontPath);
        String fontPath2 = "fonts/James_Fajardo.ttf";
        jf = Typeface.createFromAsset(getAssets(), fontPath2);

        //Chart initialization
        pieChart = (PieChart) findViewById(R.id.pieChart);

        //Assign data for values from resources inside layout
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

        //Load data form string resource and assign bArray length as length of all behaviour array
        arrayBehaviour = getResources().getStringArray(R.array.behaviour_arrays);
        bArray = new int[arrayBehaviour.length];

        //Initialization of RadioGroup and its radio buttons with styling
        RadioButton byB=(RadioButton)findViewById(R.id.byBehaviour);
        RadioButton byS=(RadioButton)findViewById(R.id.bySeverity);
        byB.setTypeface(jf);
        byB.setTextSize(23);
        byS.setTypeface(jf);
        byS.setTextSize(23);

        //Setting typeface for all values
        setTypeface();
        //Hiding charts on activity init
        hideChart();

        //Hint button to show user instructions
        Button hint = (Button) findViewById(R.id.hint);
        hint.setTypeface(jf);
        hint.setTextSize(25);
        hint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMessage();
            }
        });

        //On radio button click listener
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radio);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the RadioButton selected
                RadioButton rb = (RadioButton) findViewById(checkedId);
                switch (rb.getId()) {
                    case R.id.byBehaviour:
                        try {
                            //Hide charts to initialize new for this option
                            hideChart();
                            //Show graph filtered by option
                            showByBehaviour();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;

                    case R.id.bySeverity:
                        try {
                            //Hide charts to initialize new for this option
                            hideChart();
                            //Show graph filtered by option
                            showBySeverity();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }
        });
    }

    //Function, that do query response, initialize graphs with responded data for grouping by severity and chart styling
    private void showBySeverity() throws JSONException {
        //Query request to fetch data about records grouped by severity
        StringRequest stringRequest = new StringRequest(Request.Method.POST, PIE_BY_SEVERITY,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        showDialog();

                        //Pie chart variable assign to its resource and make it visible
                        pieChart = (PieChart) findViewById(R.id.pieChart);
                        pieChart.setVisibility(View.VISIBLE);

                        //JSON object to read response
                        JSONObject reader;
                        try {
                            reader = new JSONObject(response);

                            //Get the JSONArray result
                            result = reader.getJSONArray(AppConfig.JSON_ARRAY);
                            //If result is empty
                            if(result.length() == 0)
                                Toast.makeText(PieChartItem.this, "You don't have any records", Toast.LENGTH_LONG).show();
                            else {
                                //Init of Chart arrays for entry and labels
                                ArrayList<Entry> entries = new ArrayList<>();
                                ArrayList<String> labels = new ArrayList<>();

                                int count;

                                for (int i = 0; i < result.length(); i++) {
                                    //Assign data from response to local variables
                                    final JSONObject chartData = result.getJSONObject(i);
                                    final String severity = chartData.getString("severity");
                                    final String counter = chartData.getString("counter");

                                    count = Integer.parseInt(counter);

                                    //Add entry with values of severity counter, its position and label as severity name
                                    entries.add(new Entry(count, i));
                                    labels.add(severity);
                                }

                                //Init of pie data and data set
                                PieDataSet dataset = new PieDataSet(entries, "");
                                PieData data = new PieData(labels, dataset);

                                //Push data set for pie chart and style it
                                setPieDataSet(dataset);
                                //Apply styling for chart and assign pie data
                                initPieChartS(data);

                                pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                                    @Override
                                    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                                        //Assigning local variable values of clicked node
                                        final int info = h.getXIndex();

                                        //Load local array with behaviours
                                        String[] bNameArray = (arrayBehaviour);

                                        try {
                                            //Init JSON object using clicked bar slice as index of result array
                                            JSONObject behaviourList = result.getJSONObject(info);
                                            //Assign severity name from response to local var
                                            final String severity = behaviourList.getString("severity");

                                            //Assign response values with behaviour names to local array
                                            for(int i = 1; i<bNameArray.length; i++){
                                                bArray[i] = behaviourList.getInt(bNameArray[i]);
                                            }

                                            //Set default text
                                            getBehaviours.setText("");
                                            getBehavioursCount.setText("");
                                            Severity.setText("Severity: " + severity);

                                            //Set visibility on pie slice click
                                            getBehaviours.setVisibility(View.VISIBLE);
                                            getBehavioursCount.setVisibility(View.VISIBLE);
                                            Severity.setVisibility(View.VISIBLE);

                                            //Print all data from arrays
                                            for(int i = 0; i < bArray.length; i++) {
                                                if(bArray[i] != 0)
                                                {
                                                    //Behaviour name
                                                    getBehaviours.append(bNameArray[i] + "\n");
                                                    //Number of times shown
                                                    getBehavioursCount.append(bArray[i] + " time(s)" + "\n");
                                                }
                                            }

                                        } catch (JSONException es) {
                                            es.printStackTrace();
                                            hideDialog();
                                            Toast.makeText(PieChartItem.this, "Unexpected error. Please retry", Toast.LENGTH_LONG).show();
                                        }
                                    }

                                    @Override
                                    public void onNothingSelected() {
                                        //Hide if pie chart slice not selected
                                        getBehaviours.setVisibility(View.GONE);
                                        getBehavioursCount.setVisibility(View.GONE);
                                        Severity.setVisibility(View.GONE);
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            hideDialog();
                            Toast.makeText(PieChartItem.this, "Unexpected error. Please retry", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hideDialog();
                        Toast.makeText(PieChartItem.this, "Unexpected error. Please retry", Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put(AppConfig.KEY_CID, db.getChildID());
                return params;
            }
        };
        //Adding request to queue (Volley library)
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    //Custom value formatter to control number of decimals for values inside pie chart (MPAndroidChart library)
    private class MyValueFormatter implements ValueFormatter {

        private DecimalFormat mFormat;

        MyValueFormatter() {
            mFormat = new DecimalFormat("###,###,##0.0"); // use decimals
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {

            if(value <= 4)
                //If counter of severity less than 4 return empty
                //P.S. else pie chart would be mess with little values
                return "";
            else
                //Return values with percent sign
                return mFormat.format(value) + "%";
        }
    }

    //Init pie chart with option 'By Severity'
    private void initPieChartS(PieData data){

        //Get Legend and legend styling
        Legend l = pieChart.getLegend();
        l.setPosition(Legend.LegendPosition.RIGHT_OF_CHART_CENTER);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(5f);
        l.setYOffset(0f);
        l.setXOffset(80f);
        l.setWordWrapEnabled(true);
        l.setMaxSizePercent(0.55f);
        l.setTypeface(tf);

        //Style pie chart without hole in the center of the chart
        pieChart.setDrawHoleEnabled(false);
        pieChart.setUsePercentValues(true);
        //Set data to chart
        pieChart.setData(data);
        //Set description and its styling
        pieChart.setDescription("By Severity");
        pieChart.setDescriptionTypeface(tf);
        pieChart.setDescriptionTextSize(20);
        //Apply offsets for chart and remove slice text
        pieChart.setDrawSliceText(false);
        pieChart.offsetLeftAndRight(0);
        pieChart.setExtraOffsets(0,0,80,0);
        pieChart.getCircleBox().offset(0,0);
        //Chart animation
        pieChart.animateY(2000);

        //invalidate on update
        pieChart.invalidate();
        //Hiding Loading dialog box
        hideDialog();
    }

    //Function, that do query response, initialize graphs with responded data for grouping by behaviour and chart styling
    private void showByBehaviour() throws JSONException {
        //Query request to fetch data about records grouped by behaviour
        StringRequest stringRequest = new StringRequest(Request.Method.POST, PIE_BY_BEHAVIOUR,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        showDialog();

                        //Pie chart variable assign to its resource and make it visible
                        pieChart = (PieChart) findViewById(R.id.pieChart);
                        pieChart.setVisibility(View.VISIBLE);

                        //JSON object to read response
                        JSONObject reader;
                        try {
                            reader = new JSONObject(response);

                            //Get the JSONArray result
                            result = reader.getJSONArray(AppConfig.JSON_ARRAY);
                            //If result is empty
                            if(result.length() == 0)
                                Toast.makeText(PieChartItem.this, "You don't have any records", Toast.LENGTH_LONG).show();
                            else {
                                //Init of Chart arrays for entry and labels
                                ArrayList<Entry> entries = new ArrayList<>();
                                ArrayList<String> labels = new ArrayList<>();

                                int count;

                                for (int i = 0; i < result.length(); i++) {
                                    //Assign data from response to local variables
                                    final JSONObject chartData = result.getJSONObject(i);
                                    final String bName = chartData.getString("behaviour_name");
                                    final String counter = chartData.getString("counter");

                                    count = Integer.parseInt(counter);

                                    //Add entry with values of behaviour counter, its position and label as behaviour name
                                    entries.add(new Entry(count, i));
                                    labels.add(bName);
                                }

                                //Init of pie data and data set
                                PieDataSet dataset = new PieDataSet(entries, "");
                                PieData data = new PieData(labels, dataset);

                                //Push data set for pie chart and style it
                                setPieDataSet(dataset);
                                //Apply styling for chart and assign pie data
                                initPieChartB(data);

                                pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                                    @Override
                                    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                                        //Assigning local variable values of clicked node
                                        final int info = h.getXIndex();

                                        try {
                                            //Init JSON object using clicked bar slice as index of result array
                                            JSONObject behaviourList = result.getJSONObject(info);
                                            //Assign behaviour name from response to local var
                                            final String behaviour_name = behaviourList.getString("behaviour_name");
                                            //Assign severity counter from response to local vars
                                            final String s_mild = behaviourList.getString("Mild");
                                            final String s_moderate = behaviourList.getString("Moderate");
                                            final String s_severe = behaviourList.getString("Severe");

                                            //Set default text
                                            byBehaviour.setText(" " + behaviour_name);

                                            mildCount.setText(s_mild + " time(s)");
                                            moderateCount.setText(s_moderate + " time(s)");
                                            severeCount.setText(s_severe + " time(s)");

                                            //Set visibility on pie slice click
                                            byBehaviour.setVisibility(View.VISIBLE);
                                            mild.setVisibility(View.VISIBLE);
                                            moderate.setVisibility(View.VISIBLE);
                                            severe.setVisibility(View.VISIBLE);
                                            mildCount.setVisibility(View.VISIBLE);
                                            moderateCount.setVisibility(View.VISIBLE);
                                            severeCount.setVisibility(View.VISIBLE);

                                        } catch (JSONException es) {
                                            es.printStackTrace();
                                            hideDialog();
                                            Toast.makeText(PieChartItem.this, "Unexpected error. Please retry", Toast.LENGTH_LONG).show();
                                        }
                                    }

                                    @Override
                                    public void onNothingSelected() {
                                        //Hide if pie chart slice not selected
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
                            hideDialog();
                            Toast.makeText(PieChartItem.this, "Unexpected error. Please retry", Toast.LENGTH_LONG).show();
                        }
                    }
                    // Get the JSONArray weather
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hideDialog();
                        Toast.makeText(PieChartItem.this, "Unexpected error. Please retry", Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put(AppConfig.KEY_CID, db.getChildID());
                return params;
            }
        };
        //Adding request to queue (Volley library)
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    //Init pie chart with option 'By Behaviour'
    private void initPieChartB(PieData data){

        //Get Legend and legend styling
        Legend l = pieChart.getLegend();
        l.setPosition(Legend.LegendPosition.LEFT_OF_CHART);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(5f);
        l.setYOffset(0f);
        l.setXOffset(10f);
        l.setWordWrapEnabled(true);
        l.setMaxSizePercent(0.55f);
        l.setTypeface(tf);

        //Style pie chart without hole in the center of the chart
        pieChart.setDrawHoleEnabled(false);
        pieChart.setUsePercentValues(true);
        //Set data to chart
        pieChart.setData(data);
        //Set description and its styling
        pieChart.setDescription("By Behaviour");
        pieChart.setDescriptionTypeface(tf);
        pieChart.setDescriptionTextSize(20);
        //Apply offsets for chart and remove slice text
        pieChart.setDrawSliceText(false);
        pieChart.offsetLeftAndRight(0);
        pieChart.setExtraOffsets(180,0,0,0);
        pieChart.getCircleBox().offset(0,0);
        //Chart animation
        pieChart.animateY(2000);

        //invalidate on update
        pieChart.invalidate();

        //Hiding Loading dialog box
        hideDialog();
    }

    //Set pie data set for all charts using value formatter
    private void setPieDataSet(PieDataSet dataset){
        dataset.setDrawValues(true);
        dataset.setValueFormatter(new PieChartItem.MyValueFormatter());
        dataset.setSliceSpace(3);
        dataset.setSelectionShift(5);
        dataset.setColors(Colors.ALL_COLORS);
    }

    //Set typeface for all text fields
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

    //Hide all
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

    //Alert box with instructions
    private void showMessage(){
        AlertDialog.Builder builder1 = new AlertDialog.Builder(PieChartItem.this);
        builder1.setMessage("Instructions\n\n" +
                "This report will demonstrate all records grouped by your option.\n\n" +
                "- To display report please choose option 'By Behaviour' or 'By Severity'.\n\n" +
                "- To dipslay more detailed data simply click on pie slice and details will appear below chart");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    private void logoutUser() {
        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());

        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(PieChartItem.this, Login.class);
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
        Intent intent = new Intent(PieChartItem.this, MenuItems.class);
        switch(item.getItemId()) {
            case R.id.action_notifications:
                Toast.makeText(getApplicationContext(), "Currently unavailable!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_settings:
                changeActivity(Settings.class);
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

    public void changeActivity(Class activity) {
        finish();
        Intent intent = new Intent(this, activity);
        startActivity(intent);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
