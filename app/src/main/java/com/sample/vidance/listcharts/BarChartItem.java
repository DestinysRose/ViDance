package com.sample.vidance.listcharts;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
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
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.sample.vidance.Dashboard;
import com.sample.vidance.Login;
import com.sample.vidance.MenuItems;
import com.sample.vidance.R;
import com.sample.vidance.Record;
import com.sample.vidance.Report;
import com.sample.vidance.TargetBehaviour;
import com.sample.vidance.Update;
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

public class BarChartItem extends AppCompatActivity implements View.OnClickListener{
    private SQLiteHandler db;
    private SessionManager session;

    //Show dialog when fetching
    private ProgressDialog pDialog;

    Typeface tf;
    Typeface jf;

    //JSON Array
    private JSONArray result;

    Button btnDatePicker, btnEndDatePicker;
    EditText inputAmount;
    BarChart barChart, HbarChart;

    private String date, endDate, amount;

    public static final String BAR_FILTER_DAILY = "http://thevidance.com/filter/daily/barChart.php";
    public static final String BAR_FILTER_WEEKLY = "http://thevidance.com/filter/weekly/barChart.php";
    public static final String BAR_FILTER_HOURLY = "http://thevidance.com/filter/hourly/barChart.php";
    public static final String BAR_FILTER_MONTHLY = "http://thevidance.com/filter/monthly/barChart.php";

    public static final String EMPTY_BAR_FILTER_HOURLY = "http://thevidance.com/filter/hourly/barChart(Last).php";
    public static final String EMPTY_BAR_FILTER_DAILY = "http://thevidance.com/filter/daily/barChart(Last).php";
    public static final String EMPTY_BAR_FILTER_WEEKLY = "http://thevidance.com/filter/weekly/barChart(Last).php";
    public static final String EMPTY_BAR_FILTER_MONTHLY = "http://thevidance.com/filter/monthly/barChart(Last).php";

    public static final String FIRST_RECORD = "http://thevidance.com/filter/firstRecord.php";
    public static final String LAST_RECORD = "http://thevidance.com/filter/lastRecord.php";

    public static final String KEY_BAR_DATE = "date1";
    public static final String KEY_BAR_END_DATE = "date2";
    public static final String KEY_BAR_AMOUNT = "amount";
    public final String[] finalRecord = new String[25];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barchart);

            // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        String fontPath = "fonts/CatCafe.ttf";
        tf = Typeface.createFromAsset(getAssets(), fontPath);
        String fontPath2 = "fonts/James_Fajardo.ttf";
        jf = Typeface.createFromAsset(getAssets(), fontPath2);

        barChart = (BarChart) findViewById(R.id.chart);
        HbarChart = (BarChart) findViewById(R.id.chart2);

        //Set onClickListeners for date and time
        btnDatePicker = (Button)findViewById(R.id.setDate);
        btnDatePicker.setOnClickListener((View.OnClickListener) this);
        btnDatePicker.setTypeface(jf);
        btnDatePicker.setTextSize(25);

        //Set onClickListeners for date and time
        btnEndDatePicker = (Button)findViewById(R.id.setEndDate);
        btnEndDatePicker.setOnClickListener((View.OnClickListener) this);
        btnEndDatePicker.setTypeface(jf);
        btnEndDatePicker.setTextSize(25);

        inputAmount = (EditText)findViewById(R.id.setNumber);
        inputAmount.setHint("How many?");
        inputAmount.setTypeface(tf);

        hideButtons();

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radio);
        RadioButton rbH=(RadioButton)findViewById(R.id.hourly);
        RadioButton rbD=(RadioButton)findViewById(R.id.daily);
        RadioButton rbW=(RadioButton)findViewById(R.id.weekly);
        RadioButton rbM=(RadioButton)findViewById(R.id.monthly);

        rbH.setTypeface(jf);
        rbH.setTextSize(23);
        rbD.setTypeface(jf);
        rbD.setTextSize(23);
        rbW.setTypeface(jf);
        rbW.setTextSize(23);
        rbM.setTypeface(jf);
        rbM.setTextSize(23);

        showMessage();

        Button hint = (Button) findViewById(R.id.hint);
        hint.setTypeface(jf);
        hint.setTextSize(25);
        hint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMessage();
            }
        });

        Button dateApply = (Button) findViewById(R.id.dateApply);
        dateApply.setTypeface(jf);
        dateApply.setTextSize(25);
        dateApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Please select an option", Toast.LENGTH_SHORT).show();
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the RadioButton selected
                RadioButton rb=(RadioButton)findViewById(checkedId);
                switch(rb.getId()) {
                    case R.id.hourly:
                        hideButtons();
                        setTextForHourly();
                        btnDatePicker.setVisibility(View.VISIBLE);
                        btnEndDatePicker.setVisibility(View.GONE);
                        inputAmount.setVisibility(View.GONE);
                        barChart.setVisibility(View.GONE);
                        HbarChart.setVisibility(View.GONE);

                        Button filHour = (Button) findViewById(R.id.dateApply);
                        filHour.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    //storeDateHourly();
                                    date = (String) btnDatePicker.getText();
                                    endDate = (String) btnEndDatePicker.getText();
                                    amount = inputAmount.getText().toString().trim();
                                    if(hourlyValid(date) == 0)
                                        storeDate(BAR_FILTER_HOURLY);
                                    //Toast.makeText(getApplicationContext(), date, Toast.LENGTH_SHORT).show();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        break;
                    case R.id.daily:
                        hideButtons();
                        setTextForDaily();
                        btnDatePicker.setVisibility(View.VISIBLE);
                        btnEndDatePicker.setVisibility(View.VISIBLE);
                        inputAmount.setVisibility(View.GONE);
                        barChart.setVisibility(View.GONE);
                        HbarChart.setVisibility(View.GONE);

                        Button filDay = (Button) findViewById(R.id.dateApply);
                        filDay.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    //storeDateDaily();
                                    date = (String) btnDatePicker.getText();
                                    endDate = (String) btnEndDatePicker.getText();
                                    amount = inputAmount.getText().toString().trim();
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
                        setTextForWeekly();
                        btnDatePicker.setVisibility(View.VISIBLE);
                        btnEndDatePicker.setVisibility(View.GONE);
                        inputAmount.setVisibility(View.VISIBLE);
                        barChart.setVisibility(View.GONE);
                        HbarChart.setVisibility(View.GONE);

                        Button filWeek = (Button) findViewById(R.id.dateApply);
                        filWeek.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    //storeDateWeekly();
                                    date = (String) btnDatePicker.getText();
                                    endDate = (String) btnEndDatePicker.getText();
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
                        setTextForMonthly();
                        btnDatePicker.setVisibility(View.VISIBLE);
                        btnEndDatePicker.setVisibility(View.GONE);
                        inputAmount.setVisibility(View.VISIBLE);
                        barChart.setVisibility(View.GONE);
                        HbarChart.setVisibility(View.GONE);

                        Button filMonth = (Button) findViewById(R.id.dateApply);
                        filMonth.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    //storeDateMonthly();
                                    date = (String) btnDatePicker.getText();
                                    endDate = (String) btnEndDatePicker.getText();
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

    private class MyBarDataSet extends BarDataSet {


        private MyBarDataSet(List<BarEntry> yVals, String label) {
            super(yVals, label);
        }

        @Override
        public int getColor(int index) {

            if(getEntryForXIndex(index).getVal() < 4) // less than 4 green
                return mColors.get(0);
            else if(getEntryForXIndex(index).getVal() < 7) // less than 7 orange
                return mColors.get(1);
            else // greater or equal than 7 red
                return mColors.get(2);
        }

    }

    private class MyValueFormatter implements ValueFormatter {

        private DecimalFormat mFormat;

        private MyValueFormatter() {
            mFormat = new DecimalFormat("###,###,###"); // use no decimals
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {

            return mFormat.format(value);
        }
    }

    private void storeDate(String url) throws JSONException {

        date = (String) btnDatePicker.getText();
        endDate = (String) btnEndDatePicker.getText();
        amount = inputAmount.getText().toString().trim();

        String lastUrl = null;

        switch (url) {
            case BAR_FILTER_HOURLY:
                lastUrl = EMPTY_BAR_FILTER_HOURLY;
                break;
            case BAR_FILTER_DAILY:
                lastUrl = EMPTY_BAR_FILTER_DAILY;
                break;
            case BAR_FILTER_WEEKLY:
                lastUrl = EMPTY_BAR_FILTER_WEEKLY;
                break;
            case BAR_FILTER_MONTHLY:
                lastUrl = EMPTY_BAR_FILTER_MONTHLY;
                break;
        }

        final String finalDate = date;
        final String finalEndDate = endDate;
        final String finalAmount = amount;
        final String finalLastUrl = lastUrl;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            barChart = (BarChart) findViewById(R.id.chart);
                            barChart.invalidate();


                            JSONObject reader = null;
                            try {
                                reader = new JSONObject(response);

                                // Get the JSONArray weather
                                result = reader.getJSONArray("result");
                                if (result.length() == 0) {

                                    lastDateHourly(finalLastUrl);
                                    alert(finalLastUrl);

                                } else {
                                    ArrayList<BarEntry> barEntries = new ArrayList<>();
                                    ArrayList<String> pa = new ArrayList<>();
                                    barChart.setVisibility(View.VISIBLE);

                                    int count;
                                    MyBarDataSet barDataSet = null;
                                    for (int i = 0; i < result.length(); i++) {
                                        final JSONObject barChartObj = result.getJSONObject(i);
                                        final String behaviour_counter = barChartObj.getString("counter");

                                        count = Integer.parseInt(behaviour_counter);

                                        barEntries.add(new BarEntry(count, i));

                                        pa.add("");

                                        barDataSet = new MyBarDataSet(barEntries, "Behaviours");
                                    }

                                    BarData theData = new BarData(pa, barDataSet);

                                    barDataSet.setColors(new int[]{ContextCompat.getColor(BarChartItem.this, R.color.green),
                                            ContextCompat.getColor(BarChartItem.this, R.color.yellow),
                                            ContextCompat.getColor(BarChartItem.this, R.color.red)});
                                    barDataSet.setValueFormatter(new BarChartItem.MyValueFormatter());

                                    barChart.setData(theData);
                                    barDataSet.setBarSpacePercent(20f);

                                    barChart.setDescription("");
                                    YAxis yAxisRight = barChart.getAxisRight();
                                    yAxisRight.setEnabled(false);
                                    barChart.animateX(1500);
                                    barChart.animateY(1500);
                                    barChart.invalidate();
                                    hideDialog();

                                    barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                                        @Override
                                        public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {

                                            final int info = h.getXIndex();

                                            try {
                                                JSONObject severity_info = result.getJSONObject(info);
                                                final String behaviour_name = severity_info.getString("bName");
                                                final String s_mild = severity_info.getString("Mild");
                                                final String s_moderate = severity_info.getString("Moderate");
                                                final String s_severe = severity_info.getString("Severe");

                                                int count_mild = Integer.parseInt(s_mild);
                                                int count_moderate = Integer.parseInt(s_moderate);
                                                int count_severe = Integer.parseInt(s_severe);

                                                //Pie chart declaration
                                                BarChart HbarChart = (BarChart) findViewById(R.id.chart2);
                                                HbarChart.setVisibility(View.VISIBLE);

                                                ArrayList<BarEntry> entries2 = new ArrayList<>();

                                                ArrayList<String> labels2;
                                                BarDataSet barSet = null;

                                                if (count_mild > 0 && count_severe > 0 && count_moderate > 0) {
                                                    entries2.add(new BarEntry(count_mild, 0));
                                                    entries2.add(new BarEntry(count_moderate, 1));
                                                    entries2.add(new BarEntry(count_severe, 2));

                                                    labels2 = new ArrayList<>();
                                                    labels2.add("Mild");
                                                    labels2.add("Moderate");
                                                    labels2.add("Severe");
                                                } else if (count_severe > 0 && count_moderate > 0) {

                                                    entries2.add(new BarEntry(count_moderate, 0));
                                                    entries2.add(new BarEntry(count_severe, 1));

                                                    labels2 = new ArrayList<>();
                                                    labels2.add("Moderate");
                                                    labels2.add("Severe");

                                                } else if (count_mild > 0 && count_moderate > 0) {

                                                    entries2.add(new BarEntry(count_mild, 0));
                                                    entries2.add(new BarEntry(count_moderate, 1));

                                                    labels2 = new ArrayList<>();
                                                    labels2.add("Mild");
                                                    labels2.add("Moderate");

                                                } else if (count_mild > 0 && count_severe > 0) {

                                                    entries2.add(new BarEntry(count_mild, 0));
                                                    entries2.add(new BarEntry(count_severe, 1));

                                                    labels2 = new ArrayList<>();
                                                    labels2.add("Mild");
                                                    labels2.add("Severe");

                                                } else if (count_severe > 0) {

                                                    entries2.add(new BarEntry(count_severe, 0));

                                                    labels2 = new ArrayList<>();
                                                    labels2.add("Severe");

                                                } else if (count_moderate > 0) {

                                                    entries2.add(new BarEntry(count_moderate, 0));

                                                    labels2 = new ArrayList<>();
                                                    labels2.add("Moderate");

                                                } else {

                                                    entries2.add(new BarEntry(count_mild, 0));

                                                    labels2 = new ArrayList<>();
                                                    labels2.add("Mild");
                                                }

                                                barSet = new BarDataSet(entries2, "");
                                                barSet.setDrawValues(false);

                                                BarData data2 = new BarData(labels2, barSet);
                                                HbarChart.setData(data2);

                                                barSet.setBarSpacePercent(20f);

                                                HbarChart.setDescription(behaviour_name);
                                                HbarChart.setDescriptionPosition(700f, 18f);
                                                HbarChart.setDescriptionTextSize(12f);
                                                YAxis yAxisLeft = HbarChart.getAxisLeft();
                                                yAxisLeft.setEnabled(false);
                                                HbarChart.animateX(1500);
                                                HbarChart.animateY(1500);
                                                HbarChart.invalidate();
                                                hideDialog();

                                                Legend l = HbarChart.getLegend();
                                                l.setEnabled(false);
                                                barSet.setColors(Colors.H_BAR_COLOR);

                                            } catch (JSONException e1) {
                                                e1.printStackTrace();
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
                            Toast.makeText(BarChartItem.this, error.toString(), Toast.LENGTH_LONG).show();
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put(KEY_BAR_DATE, finalDate);
                    params.put(KEY_BAR_END_DATE, finalEndDate);
                    params.put(KEY_BAR_AMOUNT, finalAmount);
                    return params;
                }
            };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void lastDateHourly(String lastUrl) throws JSONException {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, lastUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        BarChart barChart = (BarChart) findViewById(R.id.chart);

                        JSONObject reader = null;
                        try {
                            reader = new JSONObject(response);

                            // Get the JSONArray
                            result = reader.getJSONArray("result");
                                ArrayList<BarEntry> barEntries = new ArrayList<>();
                                ArrayList<String> pa = new ArrayList<>();
                                barChart.setVisibility(View.VISIBLE);

                                int count;
                                BarChartItem.MyBarDataSet barDataSet = null;
                                for (int i = 0; i < result.length(); i++) {
                                    final JSONObject barChartObj = result.getJSONObject(i);
                                    final String behaviour_counter = barChartObj.getString("counter");

                                    count = Integer.parseInt(behaviour_counter);

                                    barEntries.add(new BarEntry(count, i));

                                    pa.add("");

                                    barDataSet = new BarChartItem.MyBarDataSet(barEntries, "Behaviours");
                                }

                                BarData theData = new BarData(pa, barDataSet);

                                barDataSet.setColors(new int[]{ContextCompat.getColor(BarChartItem.this, R.color.green),
                                        ContextCompat.getColor(BarChartItem.this, R.color.yellow),
                                        ContextCompat.getColor(BarChartItem.this, R.color.red)});
                                barDataSet.setValueFormatter(new BarChartItem.MyValueFormatter());

                                barChart.setData(theData);
                                barDataSet.setBarSpacePercent(20f);

                                barChart.setDescription("");
                                YAxis yAxisRight = barChart.getAxisRight();
                                yAxisRight.setEnabled(false);
                                barChart.animateX(1500);
                                barChart.animateY(1500);
                                barChart.invalidate();

                                barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                                    @Override
                                    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {

                                        final int info = h.getXIndex();

                                        try {
                                            JSONObject severity_info = result.getJSONObject(info);
                                            final String behaviour_name = severity_info.getString("bName");
                                            final String s_mild = severity_info.getString("Mild");
                                            final String s_moderate = severity_info.getString("Moderate");
                                            final String s_severe = severity_info.getString("Severe");

                                            int count_mild = Integer.parseInt(s_mild);
                                            int count_moderate = Integer.parseInt(s_moderate);
                                            int count_severe = Integer.parseInt(s_severe);

                                            //Pie chart declaration
                                            BarChart HbarChart = (BarChart) findViewById(R.id.chart2);
                                            HbarChart.setVisibility(View.VISIBLE);

                                            ArrayList<BarEntry> entries2 = new ArrayList<>();

                                            ArrayList<String> labels2;
                                            BarDataSet barSet;

                                            if (count_mild > 0 && count_severe > 0 && count_moderate > 0) {
                                                entries2.add(new BarEntry(count_mild, 0));
                                                entries2.add(new BarEntry(count_moderate, 1));
                                                entries2.add(new BarEntry(count_severe, 2));

                                                labels2 = new ArrayList<>();
                                                labels2.add("Mild");
                                                labels2.add("Moderate");
                                                labels2.add("Severe");
                                            } else if (count_severe > 0 && count_moderate > 0) {

                                                entries2.add(new BarEntry(count_moderate, 0));
                                                entries2.add(new BarEntry(count_severe, 1));

                                                labels2 = new ArrayList<>();
                                                labels2.add("Moderate");
                                                labels2.add("Severe");

                                            } else if (count_mild > 0 && count_moderate > 0) {

                                                entries2.add(new BarEntry(count_mild, 0));
                                                entries2.add(new BarEntry(count_moderate, 1));

                                                labels2 = new ArrayList<>();
                                                labels2.add("Mild");
                                                labels2.add("Moderate");

                                            } else if (count_mild > 0 && count_severe > 0) {

                                                entries2.add(new BarEntry(count_mild, 0));
                                                entries2.add(new BarEntry(count_severe, 1));

                                                labels2 = new ArrayList<>();
                                                labels2.add("Mild");
                                                labels2.add("Severe");

                                            } else if (count_severe > 0) {

                                                entries2.add(new BarEntry(count_severe, 0));

                                                labels2 = new ArrayList<>();
                                                labels2.add("Severe");

                                            } else if (count_moderate > 0) {

                                                entries2.add(new BarEntry(count_moderate, 0));

                                                labels2 = new ArrayList<>();
                                                labels2.add("Moderate");

                                            } else {

                                                entries2.add(new BarEntry(count_mild, 0));

                                                labels2 = new ArrayList<>();
                                                labels2.add("Mild");
                                            }

                                            barSet = new BarDataSet(entries2, "");
                                            barSet.setDrawValues(false);

                                            BarData data2 = new BarData(labels2, barSet);
                                            HbarChart.setData(data2);

                                            barSet.setBarSpacePercent(20f);

                                            HbarChart.setDescription(behaviour_name);
                                            HbarChart.setDescriptionPosition(700f, 18f);
                                            HbarChart.setDescriptionTextSize(12f);
                                            YAxis yAxisLeft = HbarChart.getAxisLeft();
                                            yAxisLeft.setEnabled(false);
                                            HbarChart.animateX(1500);
                                            HbarChart.animateY(1500);
                                            HbarChart.invalidate();

                                            Legend l = HbarChart.getLegend();
                                            l.setEnabled(false);
                                            barSet.setColors(Colors.H_BAR_COLOR);

                                        } catch (JSONException e1) {
                                            e1.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onNothingSelected() {

                                    }
                                });
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(BarChartItem.this, "OOOOOpss...", Toast.LENGTH_LONG).show();
                        }
                    }


                    // Get the JSONArray weather
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(BarChartItem.this, error.toString(), Toast.LENGTH_LONG).show();
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

    private int hourlyValid(String date){
        if(date.equals("Day")) {
            Toast.makeText(BarChartItem.this, "Please input date", Toast.LENGTH_LONG).show();
            return 1;
        }
        else
            return 0;
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

    private void hideButtons(){
        btnDatePicker.setVisibility(View.GONE);
        btnEndDatePicker.setVisibility(View.GONE);
        inputAmount.setVisibility(View.GONE);
        barChart.setVisibility(View.GONE);
        HbarChart.setVisibility(View.GONE);
    }

    private void setTextForHourly(){
        btnDatePicker.setText("Day");
    }
    private void setTextForDaily(){
        btnDatePicker.setText("First Day");
        btnEndDatePicker.setText("End Day");
    }
    private void setTextForWeekly(){
        btnDatePicker.setText("First day of week");
        inputAmount.setText("");
    }
    private void setTextForMonthly(){
        btnDatePicker.setText("First day of month");
        inputAmount.setText("");
    }

    private void showMessage(){
        AlertDialog.Builder builder1 = new AlertDialog.Builder(BarChartItem.this);
        builder1.setMessage("Instructions\n\n" +
                "This report will demonstrate quantity of severities for each behaviour per period of time.\n\n" +
                "*To display report please choose option for period of time (hourly, daily, weekly, monthly).\n\n" +
                "*Then select date(s), for weekly and monthly options you will be asked to input amount of weeks or months.\n\n" +
                "*After initialization of graph you will be able to track severities over particular period of time.\n\n" +
                "*To do this simply choose any node on the chart and new graph with severities will be displayed.");
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

    private String Record(final String url) throws JSONException {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        JSONObject reader = null;
                        try {
                            reader = new JSONObject(response);

                            // Get the JSONArray
                            result = reader.getJSONArray("result");

                            final JSONObject recordObj = result.getJSONObject(0);
                            final String record = recordObj.getString("updated_at");

                            finalRecord[0] = record;

                            //Toast.makeText(BarChartItem.this, finalRecord[0], Toast.LENGTH_LONG).show();

                            AlertDialog.Builder builder1 = new AlertDialog.Builder(BarChartItem.this);

                            /*if (url.equals(FIRST_RECORD)){
                                String firstRecord = "Your first record: ";
                                builder1.setMessage(firstRecord + finalRecord[0]);
                            }
                            else{
                                String lastRecord = "Your last record: ";
                                builder1.setMessage(lastRecord + finalRecord[0]);
                            }*/

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
                    // Get the JSONArray weather
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(BarChartItem.this, error.toString(), Toast.LENGTH_LONG).show();
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
        return finalRecord[0];
    }


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

        //Record(FIRST_RECORD);
        //Record(LAST_RECORD);
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

        //Record(FIRST_RECORD);
        //Record(LAST_RECORD);
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

        //Record(FIRST_RECORD);
        //Record(LAST_RECORD);
    }

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

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_dashboard:
                    finish();
                    Intent intent = new Intent(BarChartItem.this, Dashboard.class);
                    startActivity(intent);
                    return true;
                case R.id.navigation_record:
                    finish();
                    intent = new Intent(BarChartItem.this, Record.class);
                    startActivity(intent);
                    return true;
                case R.id.navigation_input:
                    finish();
                    intent = new Intent(BarChartItem.this, Update.class);
                    startActivity(intent);
                    return true;
                case R.id.navigation_target:
                    finish();
                    intent = new Intent(BarChartItem.this, TargetBehaviour.class);
                    startActivity(intent);
                    return true;
                case R.id.navigation_report:
                    finish();
                    intent = new Intent(BarChartItem.this, Report.class);
                    startActivity(intent);
                    return true;
            }
            return false;
        }
    };

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
                Toast.makeText(getApplicationContext(), "Currently unavailable!", Toast.LENGTH_SHORT).show();
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

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}