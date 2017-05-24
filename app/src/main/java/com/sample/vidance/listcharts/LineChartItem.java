package com.sample.vidance.listcharts;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
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
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.sample.vidance.CustomMarkerView;
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
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by Danil on 24.04.2017.
 */

//Initializing line and pie chart graphs with number of severiti over time and behaviour details for day in pie chart
public class LineChartItem extends AppCompatActivity implements View.OnClickListener {
    private SQLiteHandler db;
    private SessionManager session;

    //Show dialog when fetching
    private ProgressDialog pDialog;

    //JSON Array
    private JSONArray result;

    //Global variables init
    Button btnDatePicker, btnEndDatePicker;
    LineChart lineChart;
    PieChart pieChart;
    EditText inputAmount;

    //URL init to get filtered data
    public static final String LINE_FILTER_HOURLY = "http://thevidance.com/charts/line_chart/hLineChart.php";
    public static final String LINE_FILTER_DAILY = "http://thevidance.com/charts/line_chart/dLineChart.php";
    public static final String LINE_FILTER_WEEKLY = "http://thevidance.com/charts/line_chart/wLineChart.php";
    public static final String LINE_FILTER_MONTHLY = "http://thevidance.com/charts/line_chart/mLineChart.php";

    //URL init to get filtered data if result is empty
    public static final String EMPTY_LINE_FILTER_HOURLY = "http://thevidance.com/charts/line_chart/hLineChart(Empty).php";
    public static final String EMPTY_LINE_FILTER_DAILY = "http://thevidance.com/charts/line_chart/dLineChart(Empty).php";
    public static final String EMPTY_LINE_FILTER_WEEKLY = "http://thevidance.com/charts/line_chart/wLineChart(Empty).php";
    public static final String EMPTY_LINE_FILTER_MONTHLY = "http://thevidance.com/charts/line_chart/mLineChart(Empty).php";

    //Global arrays
    private String arrayBehaviour[];
    private int bArray[];

    //Global variables to pass data to MySQL query
    private String date, endDate, amount;

    Typeface tf, jf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linechart);

        // Session manager
        session = new SessionManager(getApplicationContext());

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        pDialog.setMessage("Loading ...");

        //Charts initialization
        lineChart = (LineChart) findViewById(R.id.chart);
        pieChart = (PieChart) findViewById(R.id.chart2);

        //Load typefaces from project resources
        String fontPath = "fonts/CatCafe.ttf";
        tf = Typeface.createFromAsset(getAssets(), fontPath);
        String fontPath2 = "fonts/James_Fajardo.ttf";
        jf = Typeface.createFromAsset(getAssets(), fontPath2);

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

        //Strings to keep information of charts description
        final String lineDescHourly = "Number of severities per each hour";
        final String lineDescDaily = "Number of severities per each day";
        final String lineDescWeekly = "Number of severities per each week";
        final String lineDescMonthly = "Number of severities per each month";

        final String pieDescHourly = "Time: ";
        final String pieDescDaily = "Day: ";
        final String pieDescWeekly = "Week: ";
        final String pieDescMonthly = "Month: ";

        //Load data form string resource and assign bArray length as length of all behaviour array
        arrayBehaviour = getResources().getStringArray(R.array.behaviour_arrays);
        bArray = new int[arrayBehaviour.length];

        //Initialization of RadioGroup and its radio buttons
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radio);
        final RadioButton rbH=(RadioButton)findViewById(R.id.hourly);
        final RadioButton rbD=(RadioButton)findViewById(R.id.daily);
        final RadioButton rbW=(RadioButton)findViewById(R.id.weekly);
        final RadioButton rbM=(RadioButton)findViewById(R.id.monthly);

        //Radio buttons styling
        rbH.setTypeface(jf);
        rbH.setTextSize(23);
        rbD.setTypeface(jf);
        rbD.setTextSize(23);
        rbW.setTypeface(jf);
        rbW.setTextSize(23);
        rbM.setTypeface(jf);
        rbM.setTextSize(23);
        rbH.setVisibility(View.VISIBLE);

        //Hiding all buttons when activity launches
        hideButtons();

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

        //Hint button to show user instructions
        Button hint = (Button) findViewById(R.id.hint);
        hint.setTypeface(jf);
        hint.setTextSize(25);
        hint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideGraphs();
                showMessage();
            }
        });

        //On radio button click listener
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the RadioButton selected
                RadioButton rb=(RadioButton)findViewById(checkedId);
                switch(rb.getId()) {
                    case R.id.hourly:
                        //Hide buttons to initialize new for this option
                        hideButtons();
                        //Set default text for hourly option
                        setTextForHourly();
                        //Show button for particular option
                        btnDatePicker.setVisibility(View.VISIBLE);

                        Button filHour = (Button) findViewById(R.id.dateApply);
                        filHour.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    //get date from user option
                                    date = (String) btnDatePicker.getText();
                                    //Validation check
                                    if(hourlyValid(date) == 0)
                                        //Display user generated graph with parameters of URL and description of charts
                                        storeDate(LINE_FILTER_HOURLY, lineDescHourly, pieDescHourly);
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
                                        storeDate(LINE_FILTER_DAILY, lineDescDaily, pieDescDaily);
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
                                        storeDate(LINE_FILTER_WEEKLY, lineDescWeekly, pieDescWeekly);
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
                                        storeDate(LINE_FILTER_MONTHLY, lineDescMonthly, pieDescMonthly);
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

    //Function, that do query response, initialize graphs with responded data and chart styling with parameters URL and description of charts
    private void storeDate(String url, final String lineDesc, final String pieDesc) throws JSONException {
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
            case LINE_FILTER_HOURLY:
                lastUrl = EMPTY_LINE_FILTER_HOURLY;
                break;
            //if user option daily
            case LINE_FILTER_DAILY:
                lastUrl = EMPTY_LINE_FILTER_DAILY;
                break;
            //if user option weekly
            case LINE_FILTER_WEEKLY:
                lastUrl = EMPTY_LINE_FILTER_WEEKLY;
                break;
            //if user option monthly
            case LINE_FILTER_MONTHLY:
                lastUrl = EMPTY_LINE_FILTER_MONTHLY;
                break;
        }

        //final local strings that keeps data of user input to store it later to query
        final String finalDate = date;
        final String finalEndDate = endDate;
        final String finalAmount = amount;
        final String finalLastUrl = lastUrl;

        //Query request to fetch data about records related to line chart and pie chart
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Hide second chart on dates update
                        pieChart.setVisibility(View.GONE);

                        JSONObject reader;
                        try {
                            //JSON object to read response
                            reader = new JSONObject(response);

                            // Get the JSONArray result
                            result = reader.getJSONArray(AppConfig.JSON_ARRAY);
                            if (result.length() == 0){
                                //show alert box and display last found date
                                lastDateIfEmpty(finalLastUrl, lineDesc, pieDesc);
                                alert(finalLastUrl);
                            }
                            else {
                                //Init of Chart arrays for entry and labels
                                ArrayList<Entry> entries = new ArrayList<>();
                                ArrayList<String> labels = new ArrayList<>();

                                int count;
                                //Validation if result equals to one show one more node with 0 value
                                //P.S. else line chart will display one day
                                if (result.length() == 1) {
                                    //Assign data from response to local variables assuming that result will keep only one element of JSON array
                                    final JSONObject severObj = result.getJSONObject(0);
                                    final String severCounter = severObj.getString("counter");
                                    final String outDate = severObj.getString("OutputDate");
                                    count = Integer.parseInt(severCounter);

                                    //Add entry with values of severity counter, its position and label as date
                                    entries.add(new Entry(count, 0));
                                    labels.add(outDate);

                                    entries.add(new Entry(0, 1));
                                    labels.add("");
                                } else {
                                    for (int i = 0; i < result.length(); i++) {
                                        final JSONObject severObj = result.getJSONObject(i);
                                        final String severCounter = severObj.getString("counter");
                                        final String outputDate = severObj.getString("OutputDate");
                                        count = Integer.parseInt(severCounter);

                                        entries.add(new Entry(count, i));
                                        labels.add(outputDate);
                                    }
                                }
                                //Init of Line chart data set
                                LineDataSet dataset = new LineDataSet(entries, "Implementation in progress");
                                //Dataset styling
                                dataset.setColors(ColorTemplate.COLORFUL_COLORS);
                                dataset.setDrawCubic(true);
                                dataset.setDrawFilled(true);
                                dataset.setDrawValues(true);
                                dataset.setHighlightEnabled(true);
                                dataset.setDrawHighlightIndicators(true);

                                //Push Line data into chart
                                LineData data = new LineData(labels, dataset);
                                lineChart.setData(data);

                                //Line chart styling init
                                lineChartStyle(lineDesc);
                                lineChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                                    @Override
                                    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                                        //Assigning local variable values of clicked node
                                        final int info = h.getXIndex();

                                        //Load local array with behaviours
                                        String[] bNameArray = (arrayBehaviour);

                                        try {
                                            //Init JSON object using clicked bar slice as index of result array
                                            JSONObject behaviours = result.getJSONObject(info);
                                            //Assign output date from response to local var
                                            final String outputDate = behaviours.getString("OutputDate");
                                            //Assign response values with behaviour names to local array
                                            for(int i = 1; i<bNameArray.length; i++){
                                                bArray[i] = behaviours.getInt(bNameArray[i]);
                                            }

                                            //Init of Pie chart arrays for entry and labels
                                            ArrayList<Entry> entries2 = new ArrayList<>();
                                            ArrayList<String> labels2 = new ArrayList<>();

                                            //Use local var to track position index through pie chart
                                            int ePos = 0;
                                            //Add labels with behaviours and entries with number of severities for each behaviour
                                            for (int i = 1; i < bArray.length; i++) {
                                                if (bArray[i] != 0) {
                                                    entries2.add(new Entry(bArray[i], ePos));
                                                    labels2.add(bNameArray[i]);

                                                    ePos++;
                                                }
                                            }

                                            //Init pie chart chart data set
                                            PieDataSet dataset2 = new PieDataSet(entries2, "");
                                            dataset2.setValueFormatter(new MyValueFormatter());
                                            dataset2.setValueFormatter(new MyValueFormatter());
                                            dataset2.setDrawValues(true);
                                            dataset2.setSliceSpace(3);
                                            dataset2.setSelectionShift(5);
                                            dataset2.setColors(Colors.ALL_COLORS);

                                            //Init and push data for pie chart
                                            PieData data2 = new PieData(labels2, dataset2);
                                            pieChart.setData(data2);

                                            //Apply pie chart styling
                                            pieChartStyle(outputDate, pieDesc);

                                        } catch (JSONException e1) {
                                            e1.printStackTrace();
                                            Toast.makeText(LineChartItem.this, "Unexpected error. Please retry", Toast.LENGTH_LONG).show();
                                            hideDialog();
                                        }
                                    }

                                    @Override
                                    public void onNothingSelected() {
                                        pieChart.setVisibility(View.GONE);
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(LineChartItem.this, "Unexpected error. Please retry", Toast.LENGTH_LONG).show();
                            hideDialog();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(LineChartItem.this, "Unexpected error. Please retry", Toast.LENGTH_LONG).show();
                        hideDialog();
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
    private void lastDateIfEmpty(String url, final String lineDesc, final String pieDesc) throws JSONException {
            StringRequest lineRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            pieChart.setVisibility(View.GONE);
                            JSONObject reader;
                            try {
                                reader = new JSONObject(response);

                                result = reader.getJSONArray(AppConfig.JSON_ARRAY);

                                ArrayList<Entry> entries = new ArrayList<>();
                                ArrayList<String> labels = new ArrayList<>();

                                int count;
                                if (result.length() == 1) {
                                    final JSONObject severObj = result.getJSONObject(0);
                                    final String severCounter = severObj.getString("counter");
                                    final String outputDate = severObj.getString("OutputDate");
                                    count = Integer.parseInt(severCounter);

                                    entries.add(new Entry(count, 0));
                                    labels.add(outputDate);

                                    entries.add(new Entry(0, 1));
                                    labels.add("");
                                } else if(result.length() == 0) {
                                    //check if no records found
                                    hideDialog();
                                    Toast.makeText(LineChartItem.this, "Seems that you don't have records...", Toast.LENGTH_LONG).show();
                                } else {

                                        for (int i = 0; i < result.length(); i++) {
                                            final JSONObject severObj = result.getJSONObject(i);
                                            final String severCounter = severObj.getString("counter");
                                            final String outputDate = severObj.getString("OutputDate");
                                            count = Integer.parseInt(severCounter);

                                            entries.add(new Entry(count, i));
                                            labels.add(outputDate);
                                        }
                                    }
                                    LineDataSet dataset = new LineDataSet(entries, "Implementation in progress");
                                    dataset.setColors(ColorTemplate.COLORFUL_COLORS);
                                    dataset.setDrawCubic(true);
                                    dataset.setDrawFilled(true);
                                    dataset.setDrawValues(true);
                                    dataset.setHighlightEnabled(true);
                                    dataset.setDrawHighlightIndicators(true);

                                    LineData data = new LineData(labels, dataset);
                                    lineChart.setData(data);

                                    lineChartStyle(lineDesc);
                                    lineChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                                        @Override
                                        public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {

                                            final int info = h.getXIndex();

                                            String[] bNameArray = (arrayBehaviour);

                                            try {
                                                JSONObject behaviours = result.getJSONObject(info);
                                                final String behaviour_name = behaviours.getString("OutputDate");
                                                for(int i = 1; i<bNameArray.length; i++){
                                                    bArray[i] = behaviours.getInt(bNameArray[i]);
                                                }

                                                ArrayList<Entry> entries2 = new ArrayList<>();
                                                ArrayList<String> labels2;

                                                int ePos = 0;
                                                labels2 = new ArrayList<>();
                                                for (int i = 1; i < bArray.length; i++) {
                                                    if (bArray[i] != 0) {
                                                        entries2.add(new Entry(bArray[i], ePos));
                                                        labels2.add(bNameArray[i]);

                                                        ePos++;
                                                    }
                                                }

                                                PieDataSet dataset2 = new PieDataSet(entries2, "");
                                                dataset2.setValueFormatter(new MyValueFormatter());
                                                dataset2.setDrawValues(true);
                                                dataset2.setSliceSpace(3);
                                                dataset2.setSelectionShift(5);
                                                dataset2.setColors(Colors.ALL_COLORS);

                                                PieData data2 = new PieData(labels2, dataset2);
                                                pieChart.setData(data2);

                                                pieChartStyle(behaviour_name, pieDesc);

                                            } catch (JSONException e1) {
                                                e1.printStackTrace();
                                                hideDialog();
                                            }
                                        }

                                        @Override
                                        public void onNothingSelected() {
                                            pieChart.setVisibility(View.GONE);
                                        }
                                    });
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(LineChartItem.this, "Unexpected error. Please retry", Toast.LENGTH_LONG).show();
                                hideDialog();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(LineChartItem.this, "Unexpected error. Please retry", Toast.LENGTH_LONG).show();
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
            requestQueue.add(lineRequest);
    }

    //Custom value formatter to control number of decimals for values inside pie chart (MPAndroidChart library)
    private class MyValueFormatter implements ValueFormatter {

        private DecimalFormat mFormat;

        MyValueFormatter() {
            mFormat = new DecimalFormat("###,###,##0.0"); // use decimals
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {

            if(value < 4)
                //If counter of severity less than 4 return empty
                //P.S. else pie chart would be mess with little values
                return "";
            else
                //Return values with percent sign
                return mFormat.format(value) + "%";
        }
    }

    //Line chart styling
    private void lineChartStyle(String lineDesc){
        //Visible on initialize
        lineChart.setVisibility(View.VISIBLE);
        //Custom marker
        CustomMarkerView mv = new CustomMarkerView(LineChartItem.this, R.layout.content_marker);
        lineChart.setMarkerView(mv);
        //Not allow to use zooming in chart
        lineChart.setScaleEnabled(false);
        lineChart.setDoubleTapToZoomEnabled(false);
        lineChart.setMaxVisibleValueCount(result.length());
        //Set description as lineDesc and its styling
        lineChart.setDescription(lineDesc);
        lineChart.setDescriptionTypeface(tf);
        lineChart.animateY(1000);
        //Remove legend from chart
        lineChart.getLegend().setEnabled(false);
        lineChart.fitScreen();
        lineChart.setTouchEnabled(true);
        //Unable right Y axis from chart
        YAxis yAxisRight = lineChart.getAxisRight();
        yAxisRight.setEnabled(false);

        //Hide 'Loading' dialog on initialization
        hideDialog();
    }

    //Pie chart styling
    private void pieChartStyle(String behaviour_name, String pieDesc){
        //Set visible on init
        pieChart.setVisibility(View.VISIBLE);

        //Get legend and apply styling
        Legend l = pieChart.getLegend();
        l.setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);
        l.setWordWrapEnabled(true);
        l.setMaxSizePercent(0.55f);
        l.setTypeface(tf);

        //Style pie chart without hole in the center of the chart
        pieChart.setDrawHoleEnabled(false);
        pieChart.setUsePercentValues(true);
        //Set description as behaviour name and its styling
        pieChart.setDescription(pieDesc + behaviour_name);
        pieChart.setDescriptionTypeface(tf);
        pieChart.setDescriptionTextSize(20);

        pieChart.setDrawSliceText(false);
        pieChart.animateY(2000);

        //invalidate on update
        pieChart.invalidate();
    }

    //Validations for filter options (Hourly, Daily, Weekly, Monthly) if user missed some details
    private int hourlyValid(String date){
        if(date.equals("Day")) {
            Toast.makeText(LineChartItem.this, "Please input date", Toast.LENGTH_LONG).show();
            return 1; //return 1 if errors found
        }
        else
            return 0; //0 if no errors
    }

    private int dailyValid(String date, String endDate){
        if (date.equals("First Day") && endDate.equals("End Day")){
            Toast.makeText(LineChartItem.this,"Please input first date and end date",Toast.LENGTH_LONG).show();
            return 1;
        }

        else if(date.equals("First Day")){
            Toast.makeText(LineChartItem.this,"Please input first date",Toast.LENGTH_LONG).show();
            return 1;
        }

        else if(endDate.equals("End Day")){
            Toast.makeText(LineChartItem.this,"Please input end date",Toast.LENGTH_LONG).show();
            return 1;
        }
        else
            return 0;

    }

    private int weeklyValid(String date, String amount){
        if(amount.isEmpty() && date.equals("First day of week")){
            Toast.makeText(LineChartItem.this,"Please input date and amount of weeks",Toast.LENGTH_LONG).show();
            return 1;
        }

        else if (date.equals("First day of week")){
            Toast.makeText(LineChartItem.this,"Please input date",Toast.LENGTH_LONG).show();
            return 1;
        }

        else if(amount.isEmpty()){
            Toast.makeText(LineChartItem.this,"Please input amount of weeks",Toast.LENGTH_LONG).show();
            return 1;
        }
        else
            return 0;
    }

    private int monthlyValid(String date, String amount){
        if(amount.isEmpty() && date.equals("First day of month")){
            Toast.makeText(LineChartItem.this,"Please input date and amount of months",Toast.LENGTH_LONG).show();
            return 1;
        }

        else if (date.equals("First day of month")){
            Toast.makeText(LineChartItem.this,"Please input date",Toast.LENGTH_LONG).show();
            return 1;
        }

        else if(amount.isEmpty()){
            Toast.makeText(LineChartItem.this,"Please input amount of months",Toast.LENGTH_LONG).show();
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
                            if (monthOfYear < 10) {
                                if (dayOfMonth < 10)
                                    btnDatePicker.setText(year + "-" + 0 + (monthOfYear + 1) + "-" + "0" + dayOfMonth);
                                else
                                    btnDatePicker.setText(year + "-" + 0 + (monthOfYear + 1) + "-" + dayOfMonth);
                            } else
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
        lineChart.setVisibility(View.GONE);
        pieChart.setVisibility(View.GONE);
    }

    //Hide graph to allow user input new data
    private void hideGraphs(){
        lineChart.setVisibility(View.GONE);
        pieChart.setVisibility(View.GONE);
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

    //If no records found these functions will show alert
    private void alertOnEmptyResponseHourly() throws JSONException {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(LineChartItem.this);
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

    private void alertOnEmptyResponseDaily() throws JSONException {

        AlertDialog.Builder builder1 = new AlertDialog.Builder(LineChartItem.this);

        builder1.setMessage("Sorry, we couldn't find results this day(s)\n\n" +
                "Graph will show data from your last found 7 days");
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
        AlertDialog.Builder builder1 = new AlertDialog.Builder(LineChartItem.this);

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

        AlertDialog.Builder builder1 = new AlertDialog.Builder(LineChartItem.this);

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
            case EMPTY_LINE_FILTER_HOURLY:
                alertOnEmptyResponseHourly();
                break;
            case EMPTY_LINE_FILTER_DAILY:
                alertOnEmptyResponseDaily();
                break;
            case EMPTY_LINE_FILTER_WEEKLY:
                alertOnEmptyResponseWeekly();
                break;
            case EMPTY_LINE_FILTER_MONTHLY:
                alertOnEmptyResponseMonthly();
                break;
        }
    }

    //Alert box with instructions
    private void showMessage(){
        AlertDialog.Builder builder1 = new AlertDialog.Builder(LineChartItem.this);
        builder1.setMessage("Instructions\n\n" +
                "This report will demonstrate quantity of severities per period of time.\n\n" +
                "- To display report please choose option for period of time (hourly, daily, weekly, monthly).\n\n" +
                "- Then select date(s), for weekly and monthly options you will be asked to input amount of weeks or months.\n\n\n" +
                "After initialization of graph you will be able to track behaviours over particular period of time.\n\n" +
                "- To do this simply choose any node on the chart and new graph with behaviours will be displayed.");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
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
                                Toast.makeText(LineChartItem.this, "Seems that you don't have records...", Toast.LENGTH_LONG).show();

                            //As response consist of one array element JSONObject uses first element of JSON array
                            final JSONObject recordObj = result.getJSONObject(0);
                            //Assign response to local variables
                            final String firstRecord = recordObj.getString("firstDate");
                            final String lastRecord = recordObj.getString("lastDate");

                            //Show dates
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(LineChartItem.this);
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

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(LineChartItem.this, error.toString(), Toast.LENGTH_LONG).show();
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

    private void logoutUser() {
        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());

        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(LineChartItem.this, Login.class);
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
        Intent intent = new Intent(LineChartItem.this, MenuItems.class);
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
