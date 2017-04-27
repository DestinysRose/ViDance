package com.sample.vidance.listcharts;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.sample.vidance.CustomMarkerView;
import com.sample.vidance.Dashboard;
import com.sample.vidance.Login;
import com.sample.vidance.MenuItems;
import com.sample.vidance.R;
import com.sample.vidance.Record;
import com.sample.vidance.Report;
import com.sample.vidance.TargetBehaviour;
import com.sample.vidance.Update;
import com.sample.vidance.helper.BehaviourHandler;
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

public class LineChartItem extends AppCompatActivity implements View.OnClickListener {
    private SQLiteHandler db;
    private SessionManager session;

    //Show dialog when fetching
    private ProgressDialog pDialog;

    //JSON Array
    private JSONArray result;
    private JSONArray display;

    private JSONArray report = null;

    ArrayList<HashMap<String, String>> dataList;

    ListView list;

    Button btnDatePicker, btnStartTimePicker, btnEndTimePicker;

    private int mYear, mMonth, mDay, mHour, mMinute, durMinute, durHour;
    private boolean timeChange;

    public static final String LINE_FILTER_HOURLY = "http://thevidance.com/filter/hourly/lineChart.php";
    public static final String LINE_FILTER_DAILY = "http://thevidance.com/filter/daily/lineChart.php";
    public static final String LINE_FILTER_WEEKLY = "http://thevidance.com/filter/weekly/lineChart.php";
    public static final String LINE_FILTER_MONTHLY = "http://thevidance.com/filter/monthly/lineChart.php";
    public static final String LINE_DATA_WEEKLY = "http://thevidance.com/filter/weekly/showLineData.php";
    public static final String LINE_DATA_MONTHLY = "http://thevidance.com/filter/monthly/showLineData.php";

    public static final String KEY_DATE = "date1";
    public static final String KEY_BAR_START_TIME = "time1";
    public static final String KEY_BAR_END_TIME = "time2";

    private RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linechart);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);



        Button btnDate = (Button) findViewById(R.id.setDate);
        SimpleDateFormat date = new SimpleDateFormat("dd/MM/yyyy");
        btnDate.setText(date.format(new Date()));

        //Set onClickListeners for date and time
        btnDatePicker = (Button)findViewById(R.id.setDate);
        btnDatePicker.setOnClickListener((View.OnClickListener) this);

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

        list = (ListView) findViewById(R.id.getReport);
        dataList = new ArrayList<HashMap<String,String>>();

        Button btn = (Button) findViewById(R.id.dateApply);
        btn.setOnClickListener(new View.OnClickListener() {
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

    private void storeDateHourly() throws JSONException {
        final String date = (String) btnDatePicker.getText();
        final String timeStart = (String) btnStartTimePicker.getText();
        final String timeEnd = (String) btnEndTimePicker.getText();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, LINE_FILTER_HOURLY,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        LineChart lineChart = (LineChart) findViewById(R.id.chart);

                        //Toast.makeText(LineChartItem.this,response,Toast.LENGTH_LONG).show();

                        JSONObject reader = null;
                        try {
                            reader = new JSONObject(response);

                            result = reader.getJSONArray("result");
                            if(result.length() == 0)
                                Toast.makeText(LineChartItem.this, "Sorry, no records this day(s)", Toast.LENGTH_LONG).show();
                            else {
                                //Toast.makeText(LineChartItem.this,"Shit Working!",Toast.LENGTH_LONG).show();
                                list.setAdapter(null);

                                ArrayList<Entry> entries = new ArrayList<>();
                                ArrayList<String> labels = new ArrayList<>();

                                int count;
                                for (int i = 0; i < result.length(); i++) {
                                    final JSONObject weather_object_0 = result.getJSONObject(i);
                                    final String weather_0_main = weather_object_0.getString("bName");
                                    final String weather_0_description = weather_object_0.getString("counter");
                                    final String weather_0_icon = weather_object_0.getString("updated_at");

                                    count = Integer.parseInt(weather_0_description);

                                    if (count == 0)
                                        Toast.makeText(LineChartItem.this, "Sorry, no records this day(s)", Toast.LENGTH_LONG).show();
                                    else {
                                        entries.add(new Entry(count, i));
                                        labels.add(weather_0_icon);
                                    }

                                    HashMap<String, String> reportData = new HashMap<String, String>();

                                    reportData.put(BehaviourHandler.TAG_BNAME, weather_0_main);
                                    reportData.put("counter", weather_0_description);
                                    reportData.put(BehaviourHandler.TAG_UPDATED_AT, weather_0_icon);

                                    dataList.add(reportData);

                                }

                                ListAdapter adapter = new SimpleAdapter(
                                        LineChartItem.this, dataList, R.layout.content_report,
                                        new String[]{BehaviourHandler.TAG_BNAME, "counter", BehaviourHandler.TAG_UPDATED_AT},
                                        new int[]{R.id.bName, R.id.severity, R.id.updated_at}
                                );

                                list.setAdapter(adapter);

                                LineDataSet dataset = new LineDataSet(entries, "Implementation in progress");
                                LineData data = new LineData(labels, dataset);

                                lineChart.setTouchEnabled(true);

                                CustomMarkerView mv = new CustomMarkerView(LineChartItem.this, R.layout.content_marker);
                                lineChart.setMarkerView(mv);

                                dataset.setColors(ColorTemplate.COLORFUL_COLORS); //
                                dataset.setDrawCubic(true);
                                dataset.setDrawFilled(true);
                                dataset.setDrawValues(true);
                                dataset.setHighlightEnabled(true);

                                // set this to false to disable the drawing of highlight indicator (lines)
                                dataset.setDrawHighlightIndicators(true);
                                lineChart.setScaleEnabled(false);
                                lineChart.setDoubleTapToZoomEnabled(false);
                                lineChart.setMaxVisibleValueCount(result.length());
                                lineChart.setData(data);
                                lineChart.setDescription("");
                                lineChart.animateY(1000);
                                lineChart.getLegend().setEnabled(false);
                                lineChart.fitScreen();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // Get the JSONArray weather

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(LineChartItem.this,error.toString(),Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put(KEY_DATE, date);
                params.put(KEY_BAR_START_TIME, timeStart);
                params.put(KEY_BAR_END_TIME, timeEnd);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void storeDateDaily() throws JSONException {
        final String date = (String) btnDatePicker.getText();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, LINE_FILTER_DAILY,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        LineChart lineChart = (LineChart) findViewById(R.id.chart);

                        //Toast.makeText(LineChartItem.this,response,Toast.LENGTH_LONG).show();

                        JSONObject reader = null;
                        try {
                            reader = new JSONObject(response);

                            result = reader.getJSONArray("result");
                            if(result.length() == 0)
                                Toast.makeText(LineChartItem.this, "Sorry, no records this day(s)", Toast.LENGTH_LONG).show();
                            else {
                                //Toast.makeText(LineChartItem.this,"Shit Working!",Toast.LENGTH_LONG).show();
                                list.setAdapter(null);
                                ArrayList<Entry> entries = new ArrayList<>();
                                ArrayList<String> labels = new ArrayList<>();

                                int count;
                                for (int i = 0; i < result.length(); i++) {
                                    final JSONObject weather_object_0 = result.getJSONObject(i);
                                    final String weather_0_main = weather_object_0.getString("bName");
                                    final String weather_0_description = weather_object_0.getString("counter");
                                    final String weather_0_icon = weather_object_0.getString("updated_at");

                                    count = Integer.parseInt(weather_0_description);

                                    if (count == 0)
                                        Toast.makeText(LineChartItem.this, "Sorry, no records this day(s)", Toast.LENGTH_LONG).show();
                                    else {
                                        entries.add(new Entry(count, i));
                                        labels.add(weather_0_icon);
                                    }

                                    HashMap<String, String> reportData = new HashMap<String, String>();

                                    reportData.put(BehaviourHandler.TAG_BNAME, weather_0_main);
                                    reportData.put("counter", weather_0_description);
                                    reportData.put(BehaviourHandler.TAG_UPDATED_AT, weather_0_icon);

                                    dataList.add(reportData);

                                }

                                ListAdapter adapter = new SimpleAdapter(
                                        LineChartItem.this, dataList, R.layout.content_report,
                                        new String[]{BehaviourHandler.TAG_BNAME, "counter", BehaviourHandler.TAG_UPDATED_AT},
                                        new int[]{R.id.bName, R.id.severity, R.id.updated_at}
                                );

                                list.setAdapter(adapter);

                                LineDataSet dataset = new LineDataSet(entries, "Implementation in progress");
                                LineData data = new LineData(labels, dataset);

                                lineChart.setTouchEnabled(true);

                                CustomMarkerView mv = new CustomMarkerView(LineChartItem.this, R.layout.content_marker);
                                lineChart.setMarkerView(mv);

                                dataset.setColors(ColorTemplate.COLORFUL_COLORS); //
                                dataset.setDrawCubic(true);
                                dataset.setDrawFilled(true);
                                dataset.setDrawValues(true);
                                dataset.setHighlightEnabled(true);

                                // set this to false to disable the drawing of highlight indicator (lines)
                                dataset.setDrawHighlightIndicators(true);
                                lineChart.setScaleEnabled(false);
                                lineChart.setDoubleTapToZoomEnabled(false);
                                lineChart.setMaxVisibleValueCount(result.length());
                                lineChart.setData(data);
                                lineChart.setDescription("");
                                lineChart.animateY(1000);
                                lineChart.getLegend().setEnabled(false);
                                lineChart.fitScreen();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // Get the JSONArray weather

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(LineChartItem.this,error.toString(),Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put(KEY_DATE, date);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void storeDateWeekly() throws JSONException {
        final String date = (String) btnDatePicker.getText();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, LINE_FILTER_WEEKLY,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(final String response) {

                        LineChart lineChart = (LineChart) findViewById(R.id.chart);

                        JSONObject reader = null;
                        try {
                            reader = new JSONObject(response);

                            result = reader.getJSONArray("result");
                            if(result.length() == 0)
                                Toast.makeText(LineChartItem.this, "Sorry, no records this day(s)", Toast.LENGTH_LONG).show();
                            else {
                                ArrayList<Entry> entries = new ArrayList<>();
                                ArrayList<String> labels = new ArrayList<>();

                                int count;
                                for (int i = 0; i < result.length(); i++) {
                                    final JSONObject weather_object_0 = result.getJSONObject(i);
                                    final String weather_0_description = weather_object_0.getString("counter");
                                    final String weather_0_icon = weather_object_0.getString("updated_at");

                                    count = Integer.parseInt(weather_0_description);

                                    if (count == 0)
                                        Toast.makeText(LineChartItem.this, "Sorry, no records this day(s)", Toast.LENGTH_LONG).show();
                                    else {
                                        entries.add(new Entry(count, i));
                                        labels.add(weather_0_icon);
                                    }
                                }

                                LineDataSet dataset = new LineDataSet(entries, "Implementation in progress");
                                LineData data = new LineData(labels, dataset);

                                lineChart.setTouchEnabled(true);

                                CustomMarkerView mv = new CustomMarkerView(LineChartItem.this, R.layout.content_marker);
                                lineChart.setMarkerView(mv);

                                dataset.setColors(ColorTemplate.COLORFUL_COLORS); //
                                dataset.setDrawCubic(true);
                                dataset.setDrawFilled(true);
                                dataset.setDrawValues(true);
                                dataset.setHighlightEnabled(true);

                                // set this to false to disable the drawing of highlight indicator (lines)
                                dataset.setDrawHighlightIndicators(true);
                                lineChart.setScaleEnabled(false);
                                lineChart.setDoubleTapToZoomEnabled(false);
                                lineChart.setMaxVisibleValueCount(result.length());
                                lineChart.setData(data);
                                lineChart.setDescription("");
                                lineChart.animateY(1000);
                                lineChart.getLegend().setEnabled(false);
                                lineChart.fitScreen();

                                list.setAdapter(null);
                                showListWeekly();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // Get the JSONArray weather

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(LineChartItem.this,error.toString(),Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put(KEY_DATE, date);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void storeDateMonthly() throws JSONException {
        final String date = (String) btnDatePicker.getText();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, LINE_FILTER_MONTHLY,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(final String response) {

                        LineChart lineChart = (LineChart) findViewById(R.id.chart);

                        JSONObject reader = null;
                        try {
                            reader = new JSONObject(response);

                            result = reader.getJSONArray("result");
                            if(result.length() == 0)
                                Toast.makeText(LineChartItem.this, "Sorry, no records this day(s)", Toast.LENGTH_LONG).show();
                            else {
                                ArrayList<Entry> entries = new ArrayList<>();
                                ArrayList<String> labels = new ArrayList<>();

                                int count;
                                for (int i = 0; i < result.length(); i++) {
                                    final JSONObject weather_object_0 = result.getJSONObject(i);
                                    final String weather_0_description = weather_object_0.getString("counter");
                                    final String weather_0_icon = weather_object_0.getString("updated_at");

                                    count = Integer.parseInt(weather_0_description);

                                    entries.add(new Entry(count, i));
                                    labels.add(weather_0_icon);

                                }

                                LineDataSet dataset = new LineDataSet(entries, "Implementation in progress");
                                LineData data = new LineData(labels, dataset);

                                lineChart.setTouchEnabled(true);

                                CustomMarkerView mv = new CustomMarkerView(LineChartItem.this, R.layout.content_marker);
                                lineChart.setMarkerView(mv);

                                dataset.setColors(ColorTemplate.COLORFUL_COLORS); //
                                dataset.setDrawCubic(true);
                                dataset.setDrawFilled(true);
                                dataset.setDrawValues(true);
                                dataset.setHighlightEnabled(true);

                                // set this to false to disable the drawing of highlight indicator (lines)
                                dataset.setDrawHighlightIndicators(true);
                                lineChart.setScaleEnabled(false);
                                lineChart.setDoubleTapToZoomEnabled(false);
                                lineChart.setMaxVisibleValueCount(result.length());
                                lineChart.setData(data);
                                lineChart.setDescription("");
                                lineChart.animateY(1000);
                                lineChart.getLegend().setEnabled(false);
                                lineChart.fitScreen();

                                list.setAdapter(null);
                                showListMonthly();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // Get the JSONArray weather

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(LineChartItem.this,error.toString(),Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put(KEY_DATE, date);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    protected void showListWeekly(){
        final String date = (String) btnDatePicker.getText();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, LINE_DATA_WEEKLY,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject reader = null;
                        try {
                            reader = new JSONObject(response);
                            report = reader.getJSONArray("display");

                            for(int i=0;i<report.length();i++){
                                JSONObject c = report.getJSONObject(i);

                                String bName = c.getString(BehaviourHandler.TAG_BNAME);
                                String counter = c.getString("counter");
                                String updated_at = c.getString(BehaviourHandler.TAG_UPDATED_AT);
                                HashMap<String,String> reportData = new HashMap<String,String>();

                                reportData.put(BehaviourHandler.TAG_BNAME,bName);
                                reportData.put("counter",counter);
                                reportData.put(BehaviourHandler.TAG_UPDATED_AT,updated_at);

                                dataList.add(reportData);
                            }

                            ListAdapter adapter = new SimpleAdapter(
                                    LineChartItem.this, dataList, R.layout.content_report,
                                    new String[]{BehaviourHandler.TAG_BNAME, "counter", BehaviourHandler.TAG_UPDATED_AT},
                                    new int[]{R.id.bName, R.id.severity, R.id.updated_at}
                            );

                            list.setAdapter(adapter);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(LineChartItem.this,error.toString(),Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put(KEY_DATE, date);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    protected void showListMonthly(){
        final String date = (String) btnDatePicker.getText();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, LINE_DATA_MONTHLY,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject reader = null;
                        try {
                            reader = new JSONObject(response);
                            report = reader.getJSONArray("display");

                            for(int i=0;i<report.length();i++){
                                JSONObject c = report.getJSONObject(i);

                                String bName = c.getString(BehaviourHandler.TAG_BNAME);
                                String counter = c.getString("counter");
                                String updated_at = c.getString(BehaviourHandler.TAG_UPDATED_AT);
                                HashMap<String,String> reportData = new HashMap<String,String>();

                                reportData.put(BehaviourHandler.TAG_BNAME,bName);
                                reportData.put("counter",counter);
                                reportData.put(BehaviourHandler.TAG_UPDATED_AT,updated_at);

                                dataList.add(reportData);
                            }

                            ListAdapter adapter = new SimpleAdapter(
                                    LineChartItem.this, dataList, R.layout.content_report,
                                    new String[]{BehaviourHandler.TAG_BNAME, "counter", BehaviourHandler.TAG_UPDATED_AT},
                                    new int[]{R.id.bName, R.id.severity, R.id.updated_at}
                            );

                            list.setAdapter(adapter);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(LineChartItem.this,error.toString(),Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put(KEY_DATE, date);
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
                    Intent intent = new Intent(LineChartItem.this, Dashboard.class); //Record Session page
                    startActivity(intent);
                    return true;
                case R.id.navigation_record:
                    finish();
                    intent = new Intent(LineChartItem.this, Record.class);
                    startActivity(intent);
                    return true;
                case R.id.navigation_input:
                    finish();
                    intent = new Intent(LineChartItem.this, Update.class);
                    startActivity(intent);
                    return true;
                case R.id.navigation_target:
                    finish();
                    intent = new Intent(LineChartItem.this, TargetBehaviour.class);
                    /**intent.putExtra("SELECTED_ITEM", 3);
                     intent.putExtra("SELECTED_ACTIVITY", "Target Behaviours");
                     intent.putExtra("SELECTED_CONTENT", 1);**/
                    startActivity(intent);
                    return true;
                case R.id.navigation_report:
                    finish();
                    intent = new Intent(LineChartItem.this, Report.class);
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
