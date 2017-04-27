package com.sample.vidance.listcharts;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.sample.vidance.Dashboard;
import com.sample.vidance.Login;
import com.sample.vidance.MenuItems;
import com.sample.vidance.R;
import com.sample.vidance.Record;
import com.sample.vidance.Report;
import com.sample.vidance.TargetBehaviour;
import com.sample.vidance.Update;
import com.sample.vidance.helper.SQLiteHandler;
import com.sample.vidance.helper.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
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

    //JSON Array
    private JSONArray result;
    Button btnDatePicker, btnStartTimePicker, btnEndTimePicker;

    private int mYear, mMonth, mDay, mHour, mMinute, durMinute, durHour;

    private Boolean timeChange;

    public static final String BAR_FILTER_DAILY = "http://thevidance.com/filter/daily/barChart.php";
    public static final String BAR_FILTER_WEEKLY = "http://thevidance.com/filter/weekly/barChart.php";
    public static final String BAR_FILTER_HOURLY = "http://thevidance.com/filter/hourly/barChart.php";
    public static final String BAR_FILTER_MONTHLY = "http://thevidance.com/filter/monthly/barChart.php";

    public static final String KEY_BAR_DATE = "date1";
    public static final String KEY_BAR_START_TIME = "time1";
    public static final String KEY_BAR_END_TIME = "time2";

    private RadioGroup radioGroup;
    private RadioButton radioButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barchart);

            // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        //Set onClickListeners for date and time
        btnDatePicker = (Button)findViewById(R.id.setDate);
        btnDatePicker.setOnClickListener((View.OnClickListener) this);
        SimpleDateFormat date = new SimpleDateFormat("dd/MM/yyyy");
        btnDatePicker.setText(date.format(new Date()));

        //Set onClickListeners for date and time
        btnStartTimePicker = (Button)findViewById(R.id.setStartTime);
        btnStartTimePicker.setOnClickListener(this);
        SimpleDateFormat timeStart = new SimpleDateFormat("hh:mm a");
        btnStartTimePicker.setText(timeStart.format(new Date()));

        //Set onClickListeners for date and time
        btnEndTimePicker = (Button)findViewById(R.id.setEndTime);
        btnEndTimePicker.setOnClickListener(this);
        SimpleDateFormat timeEnd = new SimpleDateFormat("hh:mm a");
        btnEndTimePicker.setText(timeEnd.format(new Date()));

        radioGroup = (RadioGroup) findViewById(R.id.radio);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the RadioButton selected
                RadioButton rb=(RadioButton)findViewById(checkedId);
                switch(rb.getId()) {
                    case R.id.hourly:
                        btnStartTimePicker.setVisibility(View.VISIBLE);
                        btnEndTimePicker.setVisibility(View.VISIBLE);

                        Button filHour = (Button) findViewById(R.id.dateApply);
                        filHour.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    storeDateHourly();
                                    //Toast.makeText(getApplicationContext(), date, Toast.LENGTH_SHORT).show();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });

                        break;
                    case R.id.daily:
                        btnStartTimePicker.setVisibility(View.INVISIBLE);
                        btnEndTimePicker.setVisibility(View.INVISIBLE);

                        Button filDay = (Button) findViewById(R.id.dateApply);
                        filDay.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    storeDateDaily();
                                    //Toast.makeText(getApplicationContext(), date, Toast.LENGTH_SHORT).show();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });

                        break;
                    case R.id.weekly:
                        btnStartTimePicker.setVisibility(View.INVISIBLE);
                        btnEndTimePicker.setVisibility(View.INVISIBLE);

                        Button filWeek = (Button) findViewById(R.id.dateApply);
                        filWeek.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    storeDateWeekly();
                                    //Toast.makeText(getApplicationContext(), date, Toast.LENGTH_SHORT).show();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                                }

                                //new LineChartItem.ProcessJSON().execute(REGISTER_URL);

                            }
                        });

                        break;
                    case R.id.monthly:
                        btnStartTimePicker.setVisibility(View.INVISIBLE);
                        btnEndTimePicker.setVisibility(View.INVISIBLE);

                        Button filMonth = (Button) findViewById(R.id.dateApply);
                        filMonth.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    storeDateMonthly();
                                    //Toast.makeText(getApplicationContext(), date, Toast.LENGTH_SHORT).show();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                                }

                                //new LineChartItem.ProcessJSON().execute(REGISTER_URL);

                            }
                        });

                        break;

                }


            }
        });

    }

    private void storeDateDaily() throws JSONException {
        final String date = (String) btnDatePicker.getText();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, BAR_FILTER_DAILY,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        BarChart barChart = (BarChart) findViewById(R.id.chart);

                        //Toast.makeText(LineChartItem.this,response,Toast.LENGTH_LONG).show();

                        JSONObject reader = null;
                        try {
                            reader = new JSONObject(response);

                            // Get the JSONArray weather
                            result = reader.getJSONArray("result");
                            if(result.length() == 0)
                            {
                                Toast.makeText(BarChartItem.this,"Records not found",Toast.LENGTH_LONG).show();
                            }
                            else{
                                ArrayList<BarEntry> barEntries = new ArrayList<>();
                                ArrayList<String> pa = new ArrayList<>();

                                int count;
                                BarDataSet barDataSet = null;
                                for (int i = 0; i < result.length(); i++) {
                                    final JSONObject barChartObj = result.getJSONObject(i);
                                    final String behaviour_counter = barChartObj.getString("counter");
                                    final String behaviour_date = barChartObj.getString("updated_at");

                                    count = Integer.parseInt(behaviour_counter);

                                    barEntries.add(new BarEntry(count, i));

                                    pa.add("Ex" + i);

                                    barDataSet = new BarDataSet(barEntries, "Behaviours");


                                }

                                BarData theData = new BarData(pa, barDataSet);
                                barDataSet.setColors(new int[]{Color.MAGENTA});

                                barChart.setData(theData);
                                barDataSet.setBarSpacePercent(20f);

                                barChart.setDescription("");
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
                                            PieChart pieChart = (PieChart) findViewById(R.id.chart2);

                                            ArrayList<Entry> entries2 = new ArrayList<>();

                                            ArrayList<String> labels2;

                                            if (count_mild > 0 && count_severe > 0 && count_moderate > 0) {
                                                entries2.add(new Entry(count_mild, 0));
                                                entries2.add(new Entry(count_moderate, 1));
                                                entries2.add(new Entry(count_severe, 2));

                                                labels2 = new ArrayList<>();
                                                labels2.add("Mild");
                                                labels2.add("Moderate");
                                                labels2.add("Severe");
                                            } else if (count_severe > 0 && count_moderate > 0) {

                                                entries2.add(new Entry(count_moderate, 0));
                                                entries2.add(new Entry(count_severe, 1));

                                                labels2 = new ArrayList<>();
                                                labels2.add("Moderate");
                                                labels2.add("Severe");

                                            } else if (count_mild > 0 && count_moderate > 0) {

                                                entries2.add(new Entry(count_mild, 0));
                                                entries2.add(new Entry(count_moderate, 1));

                                                labels2 = new ArrayList<>();
                                                labels2.add("Mild");
                                                labels2.add("Moderate");

                                            } else if (count_mild > 0 && count_severe > 0) {

                                                entries2.add(new Entry(count_mild, 0));
                                                entries2.add(new Entry(count_severe, 1));

                                                labels2 = new ArrayList<>();
                                                labels2.add("Mild");
                                                labels2.add("Severe");

                                            } else if (count_severe > 0) {

                                                entries2.add(new Entry(count_severe, 0));

                                                labels2 = new ArrayList<>();
                                                labels2.add("Severe");

                                            } else if (count_moderate > 0) {

                                                entries2.add(new Entry(count_moderate, 0));

                                                labels2 = new ArrayList<>();
                                                labels2.add("Moderate");

                                            } else {

                                                entries2.add(new Entry(count_mild, 0));

                                                labels2 = new ArrayList<>();
                                                labels2.add("Mild");
                                            }

                                            PieDataSet dataset2 = new PieDataSet(entries2, "");

                                            PieData data2 = new PieData(labels2, dataset2);

                                            dataset2.setColors(ColorTemplate.COLORFUL_COLORS);
                                            pieChart.setDrawHoleEnabled(false);
                                            pieChart.setUsePercentValues(true);
                                            pieChart.setData(data2);
                                            pieChart.setDescription(behaviour_name);
                                            pieChart.invalidate();
                                            pieChart.setDrawSliceText(false);

                                            dataset2.setDrawValues(true);
                                            dataset2.setSliceSpace(3);
                                            dataset2.setSelectionShift(5);

                                            pieChart.animateY(2000);

                                        } catch (JSONException e1) {
                                            e1.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onNothingSelected() {

                                    }
                                });
                            }

                        }catch(JSONException e){
                            e.printStackTrace();
                        }
                    }


                    // Get the JSONArray weather
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(BarChartItem.this,error.toString(),Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put(KEY_BAR_DATE, date);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void storeDateWeekly() throws JSONException {
        final String date = (String) btnDatePicker.getText();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, BAR_FILTER_WEEKLY,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        BarChart barChart = (BarChart) findViewById(R.id.chart);

                        //Toast.makeText(LineChartItem.this,response,Toast.LENGTH_LONG).show();

                        JSONObject reader = null;
                        try {
                            reader = new JSONObject(response);

                            // Get the JSONArray weather
                            result = reader.getJSONArray("result");

                            ArrayList<BarEntry> barEntries = new ArrayList<>();
                            ArrayList<String> pa = new ArrayList<>();

                            int count;
                            BarDataSet barDataSet = null;
                            for (int i = 0; i < result.length(); i++) {
                                final JSONObject barChartObj = result.getJSONObject(i);
                                final String behaviour_counter = barChartObj.getString("counter");
                                final String behaviour_date = barChartObj.getString("updated_at");

                                count = Integer.parseInt(behaviour_counter);

                                barEntries.add(new BarEntry(count, i));

                                pa.add("Ex" + i);

                                barDataSet = new BarDataSet(barEntries, "Behaviours");


                            }

                            BarData theData = new BarData(pa, barDataSet);
                            barDataSet.setColors(new int[]{Color.MAGENTA});

                            barChart.setData(theData);
                            barDataSet.setBarSpacePercent(20f);

                            barChart.setDescription("");
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
                                        PieChart pieChart = (PieChart) findViewById(R.id.chart2);

                                        ArrayList<Entry> entries2 = new ArrayList<>();

                                        ArrayList<String> labels2;

                                        if (count_mild > 0 && count_severe > 0 && count_moderate > 0) {
                                            entries2.add(new Entry(count_mild, 0));
                                            entries2.add(new Entry(count_moderate, 1));
                                            entries2.add(new Entry(count_severe, 2));

                                            labels2 = new ArrayList<>();
                                            labels2.add("Mild");
                                            labels2.add("Moderate");
                                            labels2.add("Severe");
                                        } else if (count_severe > 0 && count_moderate > 0) {

                                            entries2.add(new Entry(count_moderate, 0));
                                            entries2.add(new Entry(count_severe, 1));

                                            labels2 = new ArrayList<>();
                                            labels2.add("Moderate");
                                            labels2.add("Severe");

                                        } else if (count_mild > 0 && count_moderate > 0) {

                                            entries2.add(new Entry(count_mild, 0));
                                            entries2.add(new Entry(count_moderate, 1));

                                            labels2 = new ArrayList<>();
                                            labels2.add("Mild");
                                            labels2.add("Moderate");

                                        } else if (count_mild > 0 && count_severe > 0) {

                                            entries2.add(new Entry(count_mild, 0));
                                            entries2.add(new Entry(count_severe, 1));

                                            labels2 = new ArrayList<>();
                                            labels2.add("Mild");
                                            labels2.add("Severe");

                                        } else if (count_severe > 0) {

                                            entries2.add(new Entry(count_severe, 0));

                                            labels2 = new ArrayList<>();
                                            labels2.add("Severe");

                                        } else if (count_moderate > 0) {

                                            entries2.add(new Entry(count_moderate, 0));

                                            labels2 = new ArrayList<>();
                                            labels2.add("Moderate");

                                        } else {

                                            entries2.add(new Entry(count_mild, 0));

                                            labels2 = new ArrayList<>();
                                            labels2.add("Mild");
                                        }

                                        PieDataSet dataset2 = new PieDataSet(entries2, "");

                                        PieData data2 = new PieData(labels2, dataset2);

                                        dataset2.setColors(ColorTemplate.COLORFUL_COLORS);
                                        pieChart.setDrawHoleEnabled(false);
                                        pieChart.setUsePercentValues(true);
                                        pieChart.setData(data2);
                                        pieChart.setDescription(behaviour_name);
                                        pieChart.invalidate();
                                        pieChart.setDrawSliceText(false);

                                        dataset2.setDrawValues(true);
                                        dataset2.setSliceSpace(3);
                                        dataset2.setSelectionShift(5);

                                        pieChart.animateY(2000);

                                    } catch (JSONException e1) {
                                        e1.printStackTrace();
                                    }
                                }

                                @Override
                                public void onNothingSelected() {

                                }
                            });
                        }catch(JSONException e){
                            e.printStackTrace();
                        }
                    }


                    // Get the JSONArray weather
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(BarChartItem.this,error.toString(),Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put(KEY_BAR_DATE, date);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void storeDateHourly() throws JSONException {
        final String date = (String) btnDatePicker.getText();
        final String timeStart = (String) btnStartTimePicker.getText();
        final String timeEnd = (String) btnEndTimePicker.getText();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, BAR_FILTER_HOURLY,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        BarChart barChart = (BarChart) findViewById(R.id.chart);

                        //Toast.makeText(LineChartItem.this,response,Toast.LENGTH_LONG).show();

                        JSONObject reader = null;
                        try {
                            reader = new JSONObject(response);

                            // Get the JSONArray weather
                            result = reader.getJSONArray("result");
                            if(result.length() == 0)
                            {
                                Toast.makeText(BarChartItem.this,"Records not found",Toast.LENGTH_LONG).show();

                            }
                            else{
                                ArrayList<BarEntry> barEntries = new ArrayList<>();
                                ArrayList<String> pa = new ArrayList<>();

                                int count;
                                BarDataSet barDataSet = null;
                                for (int i = 0; i < result.length(); i++) {
                                    final JSONObject barChartObj = result.getJSONObject(i);
                                    final String behaviour_counter = barChartObj.getString("counter");
                                    final String behaviour_date = barChartObj.getString("updated_at");

                                    count = Integer.parseInt(behaviour_counter);

                                    barEntries.add(new BarEntry(count, i));

                                    pa.add("Ex" + i);

                                    barDataSet = new BarDataSet(barEntries, "Behaviours");


                                }

                                BarData theData = new BarData(pa, barDataSet);
                                barDataSet.setColors(new int[]{Color.MAGENTA});

                                barChart.setData(theData);
                                barDataSet.setBarSpacePercent(20f);

                                barChart.setDescription("");
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
                                            PieChart pieChart = (PieChart) findViewById(R.id.chart2);

                                            ArrayList<Entry> entries2 = new ArrayList<>();

                                            ArrayList<String> labels2;

                                            if (count_mild > 0 && count_severe > 0 && count_moderate > 0) {
                                                entries2.add(new Entry(count_mild, 0));
                                                entries2.add(new Entry(count_moderate, 1));
                                                entries2.add(new Entry(count_severe, 2));

                                                labels2 = new ArrayList<>();
                                                labels2.add("Mild");
                                                labels2.add("Moderate");
                                                labels2.add("Severe");
                                            } else if (count_severe > 0 && count_moderate > 0) {

                                                entries2.add(new Entry(count_moderate, 0));
                                                entries2.add(new Entry(count_severe, 1));

                                                labels2 = new ArrayList<>();
                                                labels2.add("Moderate");
                                                labels2.add("Severe");

                                            } else if (count_mild > 0 && count_moderate > 0) {

                                                entries2.add(new Entry(count_mild, 0));
                                                entries2.add(new Entry(count_moderate, 1));

                                                labels2 = new ArrayList<>();
                                                labels2.add("Mild");
                                                labels2.add("Moderate");

                                            } else if (count_mild > 0 && count_severe > 0) {

                                                entries2.add(new Entry(count_mild, 0));
                                                entries2.add(new Entry(count_severe, 1));

                                                labels2 = new ArrayList<>();
                                                labels2.add("Mild");
                                                labels2.add("Severe");

                                            } else if (count_severe > 0) {

                                                entries2.add(new Entry(count_severe, 0));

                                                labels2 = new ArrayList<>();
                                                labels2.add("Severe");

                                            } else if (count_moderate > 0) {

                                                entries2.add(new Entry(count_moderate, 0));

                                                labels2 = new ArrayList<>();
                                                labels2.add("Moderate");

                                            } else {

                                                entries2.add(new Entry(count_mild, 0));

                                                labels2 = new ArrayList<>();
                                                labels2.add("Mild");
                                            }

                                            PieDataSet dataset2 = new PieDataSet(entries2, "");

                                            PieData data2 = new PieData(labels2, dataset2);

                                            dataset2.setColors(ColorTemplate.COLORFUL_COLORS);
                                            pieChart.setDrawHoleEnabled(false);
                                            pieChart.setUsePercentValues(true);
                                            pieChart.setData(data2);
                                            pieChart.setDescription(behaviour_name);
                                            pieChart.invalidate();
                                            pieChart.setDrawSliceText(false);

                                            dataset2.setDrawValues(true);
                                            dataset2.setSliceSpace(3);
                                            dataset2.setSelectionShift(5);

                                            pieChart.animateY(2000);

                                        } catch (JSONException e1) {
                                            e1.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onNothingSelected() {

                                    }
                                });
                            }

                        }catch(JSONException e){
                            e.printStackTrace();
                        }
                    }


                    // Get the JSONArray weather
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(BarChartItem.this,error.toString(),Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put(KEY_BAR_DATE, date);
                params.put(KEY_BAR_START_TIME, timeStart);
                params.put(KEY_BAR_END_TIME, timeEnd);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void storeDateMonthly() throws JSONException {
        final String date = (String) btnDatePicker.getText();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, BAR_FILTER_MONTHLY,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        BarChart barChart = (BarChart) findViewById(R.id.chart);

                        //Toast.makeText(LineChartItem.this,response,Toast.LENGTH_LONG).show();

                        JSONObject reader = null;
                        try {
                            reader = new JSONObject(response);

                            // Get the JSONArray weather
                            result = reader.getJSONArray("result");

                            ArrayList<BarEntry> barEntries = new ArrayList<>();
                            ArrayList<String> pa = new ArrayList<>();

                            int count;
                            BarDataSet barDataSet = null;
                            for (int i = 0; i < result.length(); i++) {
                                final JSONObject barChartObj = result.getJSONObject(i);
                                final String behaviour_counter = barChartObj.getString("counter");
                                final String behaviour_date = barChartObj.getString("updated_at");

                                count = Integer.parseInt(behaviour_counter);

                                barEntries.add(new BarEntry(count, i));

                                pa.add("Ex" + i);

                                barDataSet = new BarDataSet(barEntries, "Behaviours");


                            }

                            BarData theData = new BarData(pa, barDataSet);
                            barDataSet.setColors(new int[]{Color.MAGENTA});

                            barChart.setData(theData);
                            barDataSet.setBarSpacePercent(20f);

                            barChart.setDescription("");
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
                                        PieChart pieChart = (PieChart) findViewById(R.id.chart2);

                                        ArrayList<Entry> entries2 = new ArrayList<>();

                                        ArrayList<String> labels2;

                                        if (count_mild > 0 && count_severe > 0 && count_moderate > 0) {
                                            entries2.add(new Entry(count_mild, 0));
                                            entries2.add(new Entry(count_moderate, 1));
                                            entries2.add(new Entry(count_severe, 2));

                                            labels2 = new ArrayList<>();
                                            labels2.add("Mild");
                                            labels2.add("Moderate");
                                            labels2.add("Severe");
                                        } else if (count_severe > 0 && count_moderate > 0) {

                                            entries2.add(new Entry(count_moderate, 0));
                                            entries2.add(new Entry(count_severe, 1));

                                            labels2 = new ArrayList<>();
                                            labels2.add("Moderate");
                                            labels2.add("Severe");

                                        } else if (count_mild > 0 && count_moderate > 0) {

                                            entries2.add(new Entry(count_mild, 0));
                                            entries2.add(new Entry(count_moderate, 1));

                                            labels2 = new ArrayList<>();
                                            labels2.add("Mild");
                                            labels2.add("Moderate");

                                        } else if (count_mild > 0 && count_severe > 0) {

                                            entries2.add(new Entry(count_mild, 0));
                                            entries2.add(new Entry(count_severe, 1));

                                            labels2 = new ArrayList<>();
                                            labels2.add("Mild");
                                            labels2.add("Severe");

                                        } else if (count_severe > 0) {

                                            entries2.add(new Entry(count_severe, 0));

                                            labels2 = new ArrayList<>();
                                            labels2.add("Severe");

                                        } else if (count_moderate > 0) {

                                            entries2.add(new Entry(count_moderate, 0));

                                            labels2 = new ArrayList<>();
                                            labels2.add("Moderate");

                                        } else {

                                            entries2.add(new Entry(count_mild, 0));

                                            labels2 = new ArrayList<>();
                                            labels2.add("Mild");
                                        }

                                        PieDataSet dataset2 = new PieDataSet(entries2, "");

                                        PieData data2 = new PieData(labels2, dataset2);

                                        dataset2.setColors(ColorTemplate.COLORFUL_COLORS);
                                        pieChart.setDrawHoleEnabled(false);
                                        pieChart.setUsePercentValues(true);
                                        pieChart.setData(data2);
                                        pieChart.setDescription(behaviour_name);
                                        pieChart.invalidate();
                                        pieChart.setDrawSliceText(false);

                                        dataset2.setDrawValues(true);
                                        dataset2.setSliceSpace(3);
                                        dataset2.setSelectionShift(5);

                                        pieChart.animateY(2000);

                                    } catch (JSONException e1) {
                                        e1.printStackTrace();
                                    }
                                }

                                @Override
                                public void onNothingSelected() {

                                }
                            });
                        }catch(JSONException e){
                            e.printStackTrace();
                        }
                    }


                    // Get the JSONArray weather
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(BarChartItem.this,error.toString(),Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put(KEY_BAR_DATE, date);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    @Override
    public void onClick(View v) {
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
        if (v == btnStartTimePicker) {
            // Get Current Time
            final Calendar c = Calendar.getInstance((TimeZone.getDefault()));
            mHour = c.get(Calendar.HOUR_OF_DAY);
            mMinute = c.get(Calendar.MINUTE);

            // Launch Time Picker Dialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    new TimePickerDialog.OnTimeSetListener() {

                        @Override //Set in 12Hour Format and include AM/PM
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            int hour = hourOfDay % 12;
                            btnStartTimePicker.setText(String.format("%02d:%02d %s", hour == 0 ? 12 : hour, minute, hourOfDay < 12 ? "am" : "pm"));
                            timeChange = true; //Send time for calculating end time.
                            durHour = hourOfDay;
                            durMinute = minute;
                        }
                    }, mHour, mMinute, false);
            timePickerDialog.show();
        }
        if (v == btnEndTimePicker) {
            // Get Current Time
            final Calendar c = Calendar.getInstance((TimeZone.getDefault()));
            mHour = c.get(Calendar.HOUR_OF_DAY);
            mMinute = c.get(Calendar.MINUTE);

            // Launch Time Picker Dialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    new TimePickerDialog.OnTimeSetListener() {

                        @Override //Set in 12Hour Format and include AM/PM
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            int hour = hourOfDay % 12;
                            btnEndTimePicker.setText(String.format("%02d:%02d %s", hour == 0 ? 12 : hour, minute, hourOfDay < 12 ? "am" : "pm"));
                            timeChange = true; //Send time for calculating end time.
                            durHour = hourOfDay;
                            durMinute = minute;
                        }
                    }, mHour, mMinute, false);
            timePickerDialog.show();
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_dashboard:
                    finish();
                    Intent intent = new Intent(BarChartItem.this, Dashboard.class); //Record Session page
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
                    /**intent.putExtra("SELECTED_ITEM", 3);
                     intent.putExtra("SELECTED_ACTIVITY", "Target Behaviours");
                     intent.putExtra("SELECTED_CONTENT", 1);**/
                    startActivity(intent);
                    return true;
                case R.id.navigation_report:
                    finish();
                    intent = new Intent(BarChartItem.this, Report.class);
                    /***intent.putExtra("SELECTED_ITEM", 4);
                     intent.putExtra("SELECTED_ACTIVITY", "Generate Reports");
                     intent.putExtra("SELECTED_CONTENT", 2);*/
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