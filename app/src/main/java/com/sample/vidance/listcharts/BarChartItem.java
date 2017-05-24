package com.sample.vidance.listcharts;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by Danil on 24.04.2017.
 */

//Initializing bar chart graph with severities for each behaviour data and behaviour details in HC(Horizontal bar Chart) chart
public class BarChartItem extends AppCompatActivity implements View.OnClickListener{
    private SQLiteHandler db;
    private SessionManager session;

    //Show dialog when fetching
    private ProgressDialog pDialog;

    Typeface tf, jf;

    //JSON Array
    private JSONArray result;

    //Global variables init
    Button btnDatePicker, btnEndDatePicker;
    EditText inputAmount;
    BarChart barChart, HbarChart;

    //Global variables to pass data to MySQL query
    private String date, endDate, amount;

    //URL init to get filtered data
    public static final String BAR_FILTER_HOURLY = "http://thevidance.com/charts/bar_chart/hBarChart.php";
    public static final String BAR_FILTER_DAILY = "http://thevidance.com/charts/bar_chart/dBarChart.php";
    public static final String BAR_FILTER_WEEKLY = "http://thevidance.com/charts/bar_chart/wBarChart.php";
    public static final String BAR_FILTER_MONTHLY = "http://thevidance.com/charts/bar_chart/mBarChart.php";

    //URL init to get filtered data if result is empty
    public static final String EMPTY_BAR_FILTER_HOURLY = "http://thevidance.com/charts/bar_chart/hBarChart(Empty).php";
    public static final String EMPTY_BAR_FILTER_DAILY = "http://thevidance.com/charts/bar_chart/dBarChart(Empty).php";
    public static final String EMPTY_BAR_FILTER_WEEKLY = "http://thevidance.com/charts/bar_chart/wBarChart(Empty).php";
    public static final String EMPTY_BAR_FILTER_MONTHLY = "http://thevidance.com/charts/bar_chart/mBarChart(Empty).php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barchart);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        pDialog.setMessage("Loading ...");

        // Session manager
        session = new SessionManager(getApplicationContext());

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        //Load typefaces from project resources
        String fontPath = "fonts/CatCafe.ttf";
        tf = Typeface.createFromAsset(getAssets(), fontPath);
        String fontPath2 = "fonts/James_Fajardo.ttf";
        jf = Typeface.createFromAsset(getAssets(), fontPath2);

        //Bar charts initialization
        barChart = (BarChart) findViewById(R.id.chart);
        HbarChart = (BarChart) findViewById(R.id.chart2);

        //Set buttons for onClickListeners for date
        btnDatePicker = (Button)findViewById(R.id.setDate);
        btnDatePicker.setOnClickListener(this);
        btnDatePicker.setTypeface(jf);
        btnDatePicker.setTextSize(25);

        //Set buttons for onClickListeners for end date
        btnEndDatePicker = (Button)findViewById(R.id.setEndDate);
        btnEndDatePicker.setOnClickListener(this);
        btnEndDatePicker.setTypeface(jf);
        btnEndDatePicker.setTextSize(25);

        //Set user input edit view for amount of weeks and months
        inputAmount = (EditText)findViewById(R.id.setNumber);
        inputAmount.setHint("How many?");
        inputAmount.setTypeface(tf);

        //Hiding all buttons when activity launches
        hideButtons();

        //Initialization of RadioGroup and its radio buttons
        final RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radio);
        RadioButton rbH=(RadioButton)findViewById(R.id.hourly);
        RadioButton rbD=(RadioButton)findViewById(R.id.daily);
        RadioButton rbW=(RadioButton)findViewById(R.id.weekly);
        RadioButton rbM=(RadioButton)findViewById(R.id.monthly);

        //Radio buttons styling
        rbH.setTypeface(jf);
        rbH.setTextSize(23);
        rbD.setTypeface(jf);
        rbD.setTextSize(23);
        rbW.setTypeface(jf);
        rbW.setTextSize(23);
        rbM.setTypeface(jf);
        rbM.setTextSize(23);

        //Hint button to show user instructions
        final Button hint = (Button) findViewById(R.id.hint);
        hint.setTypeface(jf);
        hint.setTextSize(25);
        hint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Hide graphs to restart user choice
                hideGraphs();
                //Show alert box with instructions, first and last records
                showMessage();
            }
        });

        //Button to apply user input (if user forgot to select option)
        Button dateApply = (Button) findViewById(R.id.dateApply);
        dateApply.setTypeface(jf);
        dateApply.setTextSize(25);
        dateApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Please select an option", Toast.LENGTH_SHORT).show();
            }
        });

        //On radio button click listener
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the RadioButton selected
                RadioButton rb=(RadioButton)findViewById(checkedId);

                //Switch statements to check which option selected
                switch(rb.getId()) {
                    case R.id.hourly:
                        //Hide buttons to initialize new for this option
                        hideButtons();
                        //Set default text for hourly option
                        setTextForHourly();
                        //Show button for particular option
                        btnDatePicker.setVisibility(View.VISIBLE);

                        //On 'Show report' button click
                        Button filHour = (Button) findViewById(R.id.dateApply);
                        filHour.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    //get date from user option
                                    date = (String) btnDatePicker.getText();
                                    //Validation check
                                    if(hourlyValid(date) == 0)
                                        //Display user generated graph
                                        storeDate(BAR_FILTER_HOURLY);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        break;
                    case R.id.daily:
                        hideButtons();
                        //Set default text for daily option
                        setTextForDaily();
                        btnDatePicker.setVisibility(View.VISIBLE);
                        btnEndDatePicker.setVisibility(View.VISIBLE);

                        Button filDay = (Button) findViewById(R.id.dateApply);
                        filDay.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    date = (String) btnDatePicker.getText();
                                    endDate = (String) btnEndDatePicker.getText();
                                    if(dailyValid(date, endDate) == 0)
                                        storeDate(BAR_FILTER_DAILY);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        break;
                    case R.id.weekly:
                        hideButtons();
                        //Set default text for weekly option
                        setTextForWeekly();
                        btnDatePicker.setVisibility(View.VISIBLE);
                        inputAmount.setVisibility(View.VISIBLE);

                        Button filWeek = (Button) findViewById(R.id.dateApply);
                        filWeek.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    date = (String) btnDatePicker.getText();
                                    amount = inputAmount.getText().toString().trim();
                                    if(weeklyValid(date, amount) == 0)
                                        storeDate(BAR_FILTER_WEEKLY);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        break;
                    case R.id.monthly:
                        hideButtons();
                        //Set default text for monthly option
                        setTextForMonthly();
                        btnDatePicker.setVisibility(View.VISIBLE);
                        inputAmount.setVisibility(View.VISIBLE);

                        Button filMonth = (Button) findViewById(R.id.dateApply);
                        filMonth.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    date = (String) btnDatePicker.getText();
                                    amount = inputAmount.getText().toString().trim();
                                    if(monthlyValid(date, amount) == 0)
                                        storeDate(BAR_FILTER_MONTHLY);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        break;
                }
            }
        });
    }

    //Function, that do query response, initialize graphs with responded data and chart styling
    private void storeDate(String url) throws JSONException {
        //Show Loading before chart initialized
        showDialog();
        //Keep data to store user input on POST request
        date = (String) btnDatePicker.getText();
        endDate = (String) btnEndDatePicker.getText();
        amount = inputAmount.getText().toString().trim();

        String lastUrl = null;

        //Validation to assign special url if query result empty
        switch (url) {
            //if user option hourly
            case BAR_FILTER_HOURLY:
                lastUrl = EMPTY_BAR_FILTER_HOURLY;
                break;
            //if user option daily
            case BAR_FILTER_DAILY:
                lastUrl = EMPTY_BAR_FILTER_DAILY;
                break;
            //if user option weekly
            case BAR_FILTER_WEEKLY:
                lastUrl = EMPTY_BAR_FILTER_WEEKLY;
                break;
            //if user option monthly
            case BAR_FILTER_MONTHLY:
                lastUrl = EMPTY_BAR_FILTER_MONTHLY;
                break;
        }

        //final local strings that keeps data of user input to store it later to query
        final String finalDate = date;
        final String finalEndDate = endDate;
        final String finalAmount = amount;
        final String finalLastUrl = lastUrl;

        //Query request to fetch data about records related to vertical and horizontal bar charts
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            //Hide second chart on dates update
                            HbarChart.setVisibility(View.GONE);
                            JSONObject reader;
                            try {
                                //JSON object to read response
                                reader = new JSONObject(response);

                                // Get the JSONArray result
                                result = reader.getJSONArray(AppConfig.JSON_ARRAY);
                                //Validation if query empty
                                if (result.length() == 0) {
                                    //show alert box and display last found date
                                    alert(finalLastUrl);
                                    lastDate(finalLastUrl);

                                } else {
                                    //Init of Chart arrays for entry and labels
                                    ArrayList<BarEntry> barEntries = new ArrayList<>();
                                    ArrayList<String> labels = new ArrayList<>();

                                    int count;
                                    //Init of custom data set variable
                                    MyBarDataSet barDataSet;
                                    //Assign data from response to local variables
                                    for (int i = 0; i < result.length(); i++) {
                                        final JSONObject barChartObj = result.getJSONObject(i);
                                        final String behaviour_counter = barChartObj.getString("counter");

                                        //Store string query result into integer value
                                        count = Integer.parseInt(behaviour_counter);
                                        //Add entry with values of severity counter and its position
                                        barEntries.add(new BarEntry(count, i));
                                        //Add labels to graph
                                        labels.add("");
                                    }

                                    //Init bar set
                                    barDataSet = new MyBarDataSet(barEntries, "Number of severities for each behaviour");
                                    initBarSet(barDataSet);

                                    //Setting data for bar chart
                                    BarData theData = new BarData(labels, barDataSet);
                                    barChart.setData(theData);

                                    //Chart and styling initialization
                                    initBarChart();

                                    //On bar slice click listener
                                    barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                                        @Override
                                        public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {

                                            //Assigning local variable values of clicked node
                                            final int info = h.getXIndex();

                                            try {
                                                //Init JSON object using clicked bar slice as index of result array
                                                JSONObject severity_info = result.getJSONObject(info);
                                                //Assign response values to local variables
                                                final String behaviour_name = severity_info.getString("behaviour_name");
                                                final String s_mild = severity_info.getString("Mild");
                                                final String s_moderate = severity_info.getString("Moderate");
                                                final String s_severe = severity_info.getString("Severe");

                                                //Parse string responded values to local int
                                                int count_mild = Integer.parseInt(s_mild);
                                                int count_moderate = Integer.parseInt(s_moderate);
                                                int count_severe = Integer.parseInt(s_severe);

                                                //Init of Horizontal Bar Chart arrays for entry and labels
                                                ArrayList<BarEntry> entries2 = new ArrayList<>();
                                                ArrayList<String> labels2 = new ArrayList<>();

                                                //Init HC(horizontal chart) bar chart data set
                                                BarDataSet barSet;

                                                //Validation for HC which entity to be displayed
                                                if (count_mild > 0 && count_severe > 0 && count_moderate > 0) {
                                                    //Add entities
                                                    entries2.add(new BarEntry(count_mild, 0));
                                                    entries2.add(new BarEntry(count_moderate, 1));
                                                    entries2.add(new BarEntry(count_severe, 2));

                                                    //Add lables
                                                    labels2.add("Mild");
                                                    labels2.add("Moderate");
                                                    labels2.add("Severe");

                                                } else if (count_severe > 0 && count_moderate > 0) {
                                                    entries2.add(new BarEntry(count_moderate, 0));
                                                    entries2.add(new BarEntry(count_severe, 1));

                                                    labels2.add("Moderate");
                                                    labels2.add("Severe");

                                                } else if (count_mild > 0 && count_moderate > 0) {
                                                    entries2.add(new BarEntry(count_mild, 0));
                                                    entries2.add(new BarEntry(count_moderate, 1));

                                                    labels2.add("Mild");
                                                    labels2.add("Moderate");

                                                } else if (count_mild > 0 && count_severe > 0) {
                                                    entries2.add(new BarEntry(count_mild, 0));
                                                    entries2.add(new BarEntry(count_severe, 1));

                                                    labels2.add("Mild");
                                                    labels2.add("Severe");

                                                } else if (count_severe > 0) {
                                                    entries2.add(new BarEntry(count_severe, 0));

                                                    labels2.add("Severe");

                                                } else if (count_moderate > 0) {
                                                    entries2.add(new BarEntry(count_moderate, 0));

                                                    labels2.add("Moderate");

                                                } else {
                                                    entries2.add(new BarEntry(count_mild, 0));

                                                    labels2.add("Mild");
                                                }

                                                //Push entities data to bar data set
                                                barSet = new BarDataSet(entries2, "");
                                                initHBarSet(barSet);

                                                //Init bar data for HC
                                                BarData data2 = new BarData(labels2, barSet);
                                                HbarChart.setData(data2);

                                                //HC init
                                                initHBarChart(behaviour_name);
                                            } catch (JSONException e1) {
                                                e1.printStackTrace();
                                                hideDialog();
                                                Toast.makeText(BarChartItem.this, "Unexpected error. Please retry", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                        @Override
                                        public void onNothingSelected() {
                                            HbarChart.setVisibility(View.GONE);
                                        }
                                    });
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                hideDialog();
                                Toast.makeText(BarChartItem.this, "Unexpected error. Please retry", Toast.LENGTH_LONG).show();
                            }
                        }
                        // Get the JSONArray weather
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            hideDialog();
                            Toast.makeText(BarChartItem.this, "Unexpected error. Please retry", Toast.LENGTH_LONG).show();
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    //Put user input into hash map to send them into query
                    params.put(AppConfig.KEY_CID, db.getChildID());
                    params.put(AppConfig.KEY_DATE, finalDate);
                    params.put(AppConfig.KEY_END_DATE, finalEndDate);
                    params.put(AppConfig.KEY_AMOUNT, finalAmount);
                    return params;
                }
            };
        //Adding request to queue (Volley library)
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    //Mostly the same as storeDate function, but uses another URL to fetch response from 'empty' URL
    private void lastDate(String lastUrl) throws JSONException {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, lastUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        HbarChart.setVisibility(View.GONE);
                        JSONObject reader;
                        try {
                            reader = new JSONObject(response);

                            // Get the JSONArray
                            result = reader.getJSONArray(AppConfig.JSON_ARRAY);
                            ArrayList<BarEntry> barEntries = new ArrayList<>();
                            ArrayList<String> pa = new ArrayList<>();

                            //check if no records found
                            if(result.length() == 0) {
                                hideDialog();
                                Toast.makeText(BarChartItem.this, "Seems that you don't have records...", Toast.LENGTH_LONG).show();
                            } else {

                                int count;
                                BarChartItem.MyBarDataSet barDataSet;
                                for (int i = 0; i < result.length(); i++) {
                                    final JSONObject barChartObj = result.getJSONObject(i);
                                    final String behaviour_counter = barChartObj.getString("counter");

                                    count = Integer.parseInt(behaviour_counter);
                                    barEntries.add(new BarEntry(count, i));
                                    pa.add("");
                                }

                                barDataSet = new BarChartItem.MyBarDataSet(barEntries, "Number of severities for each behaviour");
                                initBarSet(barDataSet);

                                BarData theData = new BarData(pa, barDataSet);
                                barChart.setData(theData);
                                initBarChart();

                                barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                                    @Override
                                    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {

                                        final int info = h.getXIndex();

                                        try {
                                            JSONObject severity_info = result.getJSONObject(info);
                                            final String behaviour_name = severity_info.getString("behaviour_name");
                                            final String s_mild = severity_info.getString("Mild");
                                            final String s_moderate = severity_info.getString("Moderate");
                                            final String s_severe = severity_info.getString("Severe");

                                            int count_mild = Integer.parseInt(s_mild);
                                            int count_moderate = Integer.parseInt(s_moderate);
                                            int count_severe = Integer.parseInt(s_severe);

                                            ArrayList<BarEntry> entries2 = new ArrayList<>();
                                            ArrayList<String> labels2 = new ArrayList<>();
                                            BarDataSet barSet;

                                            if (count_mild > 0 && count_severe > 0 && count_moderate > 0) {
                                                entries2.add(new BarEntry(count_mild, 0));
                                                entries2.add(new BarEntry(count_moderate, 1));
                                                entries2.add(new BarEntry(count_severe, 2));

                                                labels2.add("Mild");
                                                labels2.add("Moderate");
                                                labels2.add("Severe");
                                            } else if (count_severe > 0 && count_moderate > 0) {

                                                entries2.add(new BarEntry(count_moderate, 0));
                                                entries2.add(new BarEntry(count_severe, 1));

                                                labels2.add("Moderate");
                                                labels2.add("Severe");

                                            } else if (count_mild > 0 && count_moderate > 0) {
                                                entries2.add(new BarEntry(count_mild, 0));
                                                entries2.add(new BarEntry(count_moderate, 1));

                                                labels2.add("Mild");
                                                labels2.add("Moderate");

                                            } else if (count_mild > 0 && count_severe > 0) {
                                                entries2.add(new BarEntry(count_mild, 0));
                                                entries2.add(new BarEntry(count_severe, 1));

                                                labels2.add("Mild");
                                                labels2.add("Severe");

                                            } else if (count_severe > 0) {
                                                entries2.add(new BarEntry(count_severe, 0));

                                                labels2.add("Severe");

                                            } else if (count_moderate > 0) {
                                                entries2.add(new BarEntry(count_moderate, 0));

                                                labels2.add("Moderate");

                                            } else {
                                                entries2.add(new BarEntry(count_mild, 0));

                                                labels2.add("Mild");
                                            }

                                            barSet = new BarDataSet(entries2, "");
                                            initHBarSet(barSet);

                                            BarData data2 = new BarData(labels2, barSet);
                                            HbarChart.setData(data2);
                                            initHBarChart(behaviour_name);

                                        } catch (JSONException e1) {
                                            e1.printStackTrace();
                                            Toast.makeText(BarChartItem.this, "Unexpected error. Please retry", Toast.LENGTH_LONG).show();
                                            hideDialog();
                                        }
                                    }

                                    @Override
                                    public void onNothingSelected() {
                                        HbarChart.setVisibility(View.GONE);
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(BarChartItem.this, "Unexpected error. Please retry", Toast.LENGTH_LONG).show();
                            hideDialog();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(BarChartItem.this, "Unexpected error. Please retry", Toast.LENGTH_LONG).show();
                        hideDialog();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put(AppConfig.KEY_CID, db.getChildID());
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    //Function to initialize chart and apply styling
    private void initBarChart(){
        //Set visibility on function call
        barChart.setVisibility(View.VISIBLE);
        barChart.setDescription("");
        //Remove Y right axes from chart
        YAxis yAxisRight = barChart.getAxisRight();
        yAxisRight.setEnabled(false);
        //Not allow to use zooming in chart
        barChart.setScaleEnabled(false);
        barChart.setDoubleTapToZoomEnabled(false);
        //Animation for graph
        barChart.animateX(1500);
        barChart.animateY(1500);

        //Legend init and styling
        Legend l = barChart.getLegend();
        l.setPosition(Legend.LegendPosition.ABOVE_CHART_LEFT);
        l.setYOffset(10f);
        l.setTextSize(10);
        l.setTypeface(tf);

        //Hide 'Loading' dialog when chart initialized
        hideDialog();
        //Invalidation on chart update
        barChart.invalidate();
    }

    //Initialization of bar data set and its styling
    private void initBarSet(BarDataSet barDataSet){
        //setting colors described in MyBarDataSet function
        barDataSet.setColors(new int[]{ContextCompat.getColor(BarChartItem.this, R.color.green),
                ContextCompat.getColor(BarChartItem.this, R.color.yellow),
                ContextCompat.getColor(BarChartItem.this, R.color.red)});
        //Custom value formatter
        barDataSet.setValueFormatter(new BarChartItem.MyValueFormatter());
        //Represent graph in percentage values
        barDataSet.setBarSpacePercent(20f);
    }

    //Function to initialize HC(Horizontal bar Chart) and apply styling
    private void initHBarChart(String behaviour_name){
        //Set visibility on function call
        HbarChart.setVisibility(View.VISIBLE);
        //Set description as behaviour name (Selected from vertical bar chart slice) and its styling
        HbarChart.setDescription(behaviour_name);
        HbarChart.setDescriptionTypeface(tf);
        HbarChart.setDescriptionPosition(700f, 18f);
        HbarChart.setDescriptionTextSize(12f);
        //Animation values
        HbarChart.animateX(0);
        HbarChart.animateY(1500);
        //Not allow to use zooming in chart
        HbarChart.setScaleEnabled(false);
        HbarChart.setDoubleTapToZoomEnabled(false);

        //Remove Y left axis
        YAxis yAxisLeft = HbarChart.getAxisLeft();
        yAxisLeft.setEnabled(false);
        //Style X axis
        XAxis xAxis = HbarChart.getXAxis();
        xAxis.setTypeface(tf);
        //Legend init and styling
        Legend l = HbarChart.getLegend();
        l.setEnabled(false);
        l.setTypeface(tf);

        //invalidate on update
        HbarChart.invalidate();
    }

    //Initialization of HC data set and its styling
    private void initHBarSet(BarDataSet barSet){
        barSet.setColors(Colors.H_BAR_COLOR);
        barSet.setBarSpacePercent(20f);
        barSet.setDrawValues(false);
    }

    //Custom bar data set to control colors of bar chart (MPAndroidChart library)
    private class MyBarDataSet extends BarDataSet {

        private MyBarDataSet(List<BarEntry> yVals, String label) {
            super(yVals, label);
        }

        @Override
        public int getColor(int index) {
            //Validation which colors need to be assign to each values under X axes
            if(getEntryForXIndex(index).getVal() < 7) // less than 4 green
                return mColors.get(0);
            else if(getEntryForXIndex(index).getVal() < 16) // less than 7 yellow
                return mColors.get(1);
            else // greater or equal than 7 red
                return mColors.get(2);
        }

    }

    //Custom value formatter to control number of decimals for values inside bar charts (MPAndroidChart library)
    private class MyValueFormatter implements ValueFormatter {

        private DecimalFormat mFormat;

        private MyValueFormatter() {
            mFormat = new DecimalFormat("###,###,###"); // use no decimals
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            //return formatted value
            return mFormat.format(value);
        }
    }

    //Validations for filter options (Hourly, Daily, Weekly, Monthly) if user missed some details
    private int hourlyValid(String date){
        if(date.equals("Day")) {
            Toast.makeText(BarChartItem.this, "Please input date", Toast.LENGTH_LONG).show();
            return 1; //return 1 if errors found
        }
        else
            return 0; //0 if no errors
    }

    private int dailyValid(String date, String endDate){
        if (date.equals("First Day") && endDate.equals("End Day")){
            Toast.makeText(BarChartItem.this,"Please input first date and end date",Toast.LENGTH_LONG).show();
            return 1;
        }

        else if(date.equals("First Day")){
            Toast.makeText(BarChartItem.this,"Please input first date",Toast.LENGTH_LONG).show();
            return 1;
        }

        else if(endDate.equals("End Day")){
            Toast.makeText(BarChartItem.this,"Please input end date",Toast.LENGTH_LONG).show();
            return 1;
        }
        else
            return 0;

    }

    private int weeklyValid(String date, String amount){
        if(amount.isEmpty() && date.equals("First day of week")){
            Toast.makeText(BarChartItem.this,"Please input date and amount of weeks",Toast.LENGTH_LONG).show();
            return 1;
        }

        else if (date.equals("First day of week")){
            Toast.makeText(BarChartItem.this,"Please input date",Toast.LENGTH_LONG).show();
            return 1;
        }

        else if(amount.isEmpty()){
            Toast.makeText(BarChartItem.this,"Please input amount of weeks",Toast.LENGTH_LONG).show();
            return 1;
        }
        else
            return 0;
    }

    private int monthlyValid(String date, String amount){
        if(amount.isEmpty() && date.equals("First day of month")){
            Toast.makeText(BarChartItem.this,"Please input date and amount of months",Toast.LENGTH_LONG).show();
            return 1;
        }

        else if (date.equals("First day of month")){
            Toast.makeText(BarChartItem.this,"Please input date",Toast.LENGTH_LONG).show();
            return 1;
        }

        else if(amount.isEmpty()){
            Toast.makeText(BarChartItem.this,"Please input amount of months",Toast.LENGTH_LONG).show();
            return 1;
        }
        else
            return 0;
    }

    //Date pickers (implemented by Michelle)
    @Override
    public void onClick(View v) {
        int mYear, mMonth, mDay;

        if (v == btnDatePicker) {
            // Get Current Date
            final Calendar c = Calendar.getInstance((TimeZone.getDefault()));
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            if(monthOfYear < 10){
                                if(dayOfMonth < 10)
                                    btnDatePicker.setText(year + "-" + 0 + (monthOfYear + 1) + "-" + "0" + dayOfMonth);
                                else
                                    btnDatePicker.setText(year + "-" + 0 + (monthOfYear + 1) + "-" + dayOfMonth);
                            }
                            else
                                btnDatePicker.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);

                        }
                    }, mYear, mMonth, mDay);

            datePickerDialog.show();
        }
        if (v == btnEndDatePicker) {
            // Get Current Date
            final Calendar c = Calendar.getInstance((TimeZone.getDefault()));
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            if (monthOfYear < 10) {
                                if (dayOfMonth < 10)
                                    btnEndDatePicker.setText(year + "-" + 0 + (monthOfYear + 1) + "-" + "0" + dayOfMonth);
                                else
                                    btnEndDatePicker.setText(year + "-" + 0 + (monthOfYear + 1) + "-" + dayOfMonth);
                            } else
                                btnEndDatePicker.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);

                        }
                    }, mYear, mMonth, mDay);

            datePickerDialog.show();
        }
    }

    //Hide buttons on activity load and on option select
    private void hideButtons(){
        btnDatePicker.setVisibility(View.GONE);
        btnEndDatePicker.setVisibility(View.GONE);
        inputAmount.setVisibility(View.GONE);
        barChart.setVisibility(View.GONE);
        HbarChart.setVisibility(View.GONE);
    }

    //Hide graph to allow user input new data
    private void hideGraphs(){
        barChart.setVisibility(View.GONE);
        HbarChart.setVisibility(View.GONE);
    }

    //Setting default texts for buttons (As 3 buttons used for 4 options and user validation)
    private void setTextForHourly(){
        btnDatePicker.setText(R.string.day);
    }

    private void setTextForDaily(){
        btnDatePicker.setText(R.string.first_day);
        btnEndDatePicker.setText(R.string.end_day);
    }

    private void setTextForWeekly(){
        btnDatePicker.setText(R.string.week);
        inputAmount.setText(R.string.amount);
    }

    private void setTextForMonthly(){
        btnDatePicker.setText(R.string.month);
        inputAmount.setText(R.string.amount);
    }

    //Alert box with instructions
    private void showMessage(){
        AlertDialog.Builder builder1 = new AlertDialog.Builder(BarChartItem.this);
        builder1.setMessage("Instructions\n\n" +
                "This report will demonstrate quantity of severities for each behaviour per period of time.\n\n" +
                "- To display report please choose option for period of time (hourly, daily, weekly, monthly).\n\n" +
                "- Then select date(s), for weekly and monthly options you will be asked to input amount of weeks or months.\n\n\n" +
                "After initialization of graph you will be able to track severities over particular period of time.\n\n" +
                "- To do this simply choose any node on the chart and new graph with severities will be displayed.");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Ok",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //dialog.cancel();
                        try {
                            Record();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    //Function to show first and last found records
    private void Record() throws JSONException {
        //String request uses URL with all founded records
        StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.RECORD_DATES,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        JSONObject reader;
                            try {
                                reader = new JSONObject(response);

                                //Get the JSONArray
                                result = reader.getJSONArray(AppConfig.JSON_ARRAY);

                                //check if no result found
                                if(result.length() == 0)
                                    Toast.makeText(BarChartItem.this, "Seems that you don't have records...", Toast.LENGTH_LONG).show();

                                else {
                                    //As response consist of one array element JSONObject uses first element of JSON array
                                    final JSONObject recordObj = result.getJSONObject(0);
                                    //Assign response to local variables
                                    final String firstRecord = recordObj.getString("firstDate");
                                    final String lastRecord = recordObj.getString("lastDate");

                                    //Show dates
                                    AlertDialog.Builder builder1 = new AlertDialog.Builder(BarChartItem.this);
                                    builder1.setMessage(
                                            "Your First record was found on: " + firstRecord + "\n\n" +
                                                    "Your Last record was found on: " + lastRecord);
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
                            } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(BarChartItem.this, error.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put(AppConfig.KEY_CID, db.getChildID());
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    //If no records found these functions will show alert
    private void alertOnEmptyResponseHourly() throws JSONException {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(BarChartItem.this);
        builder1.setMessage("Sorry, we couldn't find results this day\n\n" +
                "Graph will show data from your last record");
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

    private void alertOnEmptyResponseMonthly() throws JSONException {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(BarChartItem.this);

        builder1.setMessage("Sorry, we couldn't find results this month(s)\n\n" +
                "Graph will show data from your last month records");
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

    private void alertOnEmptyResponseWeekly() throws JSONException {

        AlertDialog.Builder builder1 = new AlertDialog.Builder(BarChartItem.this);

        builder1.setMessage("Sorry, we couldn't find results this week(s)\n\n" +
                "Graph will show data from your last week records");
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

    //Assign alerts to related URL responses
    private void alert(String url) throws JSONException {
        switch (url) {
            case EMPTY_BAR_FILTER_HOURLY:
                alertOnEmptyResponseHourly();
                break;
            case EMPTY_BAR_FILTER_DAILY:
                alertOnEmptyResponseWeekly();
                break;
            case EMPTY_BAR_FILTER_WEEKLY:
                alertOnEmptyResponseWeekly();
                break;
            case EMPTY_BAR_FILTER_MONTHLY:
                alertOnEmptyResponseMonthly();
                break;
        }
    }

    private void logoutUser() {
        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());

        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(BarChartItem.this, Login.class);
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
        Intent intent = new Intent(BarChartItem.this, MenuItems.class);
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

    //Function to show Loading dialog
    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }
    //Function to hide Loading dialog
    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}