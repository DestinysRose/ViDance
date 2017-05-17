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

    Button btnDatePicker, btnEndDatePicker;
    LineChart lineChart;
    PieChart pieChart;
    EditText inputAmount;

    public static final String LINE_FILTER_HOURLY = "http://thevidance.com/filter/hourly/lineChart.php";
    public static final String LINE_FILTER_DAILY = "http://thevidance.com/filter/daily/lineChart.php";
    public static final String LINE_FILTER_WEEKLY = "http://thevidance.com/filter/weekly/lineChart.php";
    public static final String LINE_FILTER_MONTHLY = "http://thevidance.com/filter/monthly/lineChart.php";

    public static final String EMPTY_LINE_FILTER_HOURLY = "http://thevidance.com/filter/hourly/lineChart(Empty).php";
    public static final String EMPTY_LINE_FILTER_DAILY = "http://thevidance.com/filter/daily/lineChart(Empty).php";
    public static final String EMPTY_LINE_FILTER_WEEKLY = "http://thevidance.com/filter/weekly/lineChart(Empty).php";
    public static final String EMPTY_LINE_FILTER_MONTHLY = "http://thevidance.com/filter/monthly/lineChart(Empty).php";

    public static final String KEY_DATE = "date1";
    public static final String KEY_END_DATE = "date2";
    public static final String KEY_AMOUNT = "amount";

    private String arrayBehaviour[];
    private int bArray[];
    private String date, endDate, amount;

    Typeface tf;
    Typeface jf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linechart);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        pDialog.setMessage("Loading ...");

        lineChart = (LineChart) findViewById(R.id.chart);
        pieChart = (PieChart) findViewById(R.id.chart2);

        String fontPath = "fonts/CatCafe.ttf";
        tf = Typeface.createFromAsset(getAssets(), fontPath);
        String fontPath2 = "fonts/James_Fajardo.ttf";
        jf = Typeface.createFromAsset(getAssets(), fontPath2);

        //Set onClickListeners for date and time
        btnDatePicker = (Button)findViewById(R.id.setDate);
        btnDatePicker.setOnClickListener(this);
        btnDatePicker.setTypeface(jf);
        btnDatePicker.setTextSize(25);

        //Set onClickListeners for date and time
        btnEndDatePicker = (Button)findViewById(R.id.setEndDate);
        btnEndDatePicker.setOnClickListener(this);
        btnEndDatePicker.setTypeface(jf);
        btnEndDatePicker.setTextSize(25);

        inputAmount = (EditText)findViewById(R.id.setNumber);
        inputAmount.setHint("How many?");
        inputAmount.setTypeface(tf);

        final String lineDescHourly = "Number of behaviours per each hour";
        final String lineDescDaily = "Number of behaviours per each day";
        final String lineDescWeekly = "Number of behaviours per each week";
        final String lineDescMonthly = "Number of behaviours per each month";

        final String pieDescHourly = "Time: ";
        final String pieDescDaily = "Day: ";
        final String pieDescWeekly = "Week: ";
        final String pieDescMonthly = "Month: ";

        arrayBehaviour = getResources().getStringArray(R.array.behaviour_arrays);
        bArray = new int[arrayBehaviour.length];

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

        hideButtons();

        Button dateApply = (Button) findViewById(R.id.dateApply);
        dateApply.setTypeface(jf);
        dateApply.setTextSize(25);
        dateApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Please select an option", Toast.LENGTH_SHORT).show();
            }
        });

        Button hint = (Button) findViewById(R.id.hint);
        hint.setTypeface(jf);
        hint.setTextSize(25);
        hint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMessage();
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

                        Button filHour = (Button) findViewById(R.id.dateApply);
                        filHour.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    date = (String) btnDatePicker.getText();
                                    endDate = (String) btnEndDatePicker.getText();
                                    amount = inputAmount.getText().toString().trim();
                                    if(hourlyValid(date) == 0)
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
                                    amount = inputAmount.getText().toString().trim();
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
                        setTextForWeekly();
                        btnDatePicker.setVisibility(View.VISIBLE);
                        inputAmount.setVisibility(View.VISIBLE);

                        Button filWeek = (Button) findViewById(R.id.dateApply);
                        filWeek.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    date = (String) btnDatePicker.getText();
                                    endDate = (String) btnEndDatePicker.getText();
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
                        setTextForMonthly();
                        btnDatePicker.setVisibility(View.VISIBLE);
                        inputAmount.setVisibility(View.VISIBLE);

                        Button filMonth = (Button) findViewById(R.id.dateApply);
                        filMonth.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    date = (String) btnDatePicker.getText();
                                    endDate = (String) btnEndDatePicker.getText();
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

    private void storeDate(String url, final String lineDesc, final String pieDesc) throws JSONException {
        showDialog();

        date = (String) btnDatePicker.getText();
        endDate = (String) btnEndDatePicker.getText();
        amount = inputAmount.getText().toString().trim();

        String lastUrl = null;

        switch (url) {
            case LINE_FILTER_HOURLY:
                lastUrl = EMPTY_LINE_FILTER_HOURLY;
                break;
            case LINE_FILTER_DAILY:
                lastUrl = EMPTY_LINE_FILTER_DAILY;
                break;
            case LINE_FILTER_WEEKLY:
                lastUrl = EMPTY_LINE_FILTER_WEEKLY;
                break;
            case LINE_FILTER_MONTHLY:
                lastUrl = EMPTY_LINE_FILTER_MONTHLY;
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
                        JSONObject reader;
                        try {
                            reader = new JSONObject(response);

                            result = reader.getJSONArray("result");
                            if (result.length() == 0){
                                lastDateIfEmpty(finalLastUrl, lineDesc, pieDesc);
                                alert(finalLastUrl);
                            }
                            else {

                                ArrayList<Entry> entries = new ArrayList<>();
                                ArrayList<String> labels = new ArrayList<>();

                                int count;
                                if (result.length() == 1) {
                                    final JSONObject weather_object_0 = result.getJSONObject(0);
                                    final String weather_0_description = weather_object_0.getString("counter");
                                    final String weather_0_icon = weather_object_0.getString("OutputDate");
                                    count = Integer.parseInt(weather_0_description);

                                    entries.add(new Entry(count, 0));
                                    labels.add(weather_0_icon);

                                    entries.add(new Entry(0, 1));
                                    labels.add("");
                                } else {

                                    for (int i = 0; i < result.length(); i++) {
                                        final JSONObject weather_object_0 = result.getJSONObject(i);
                                        final String weather_0_description = weather_object_0.getString("counter");
                                        final String weather_0_icon = weather_object_0.getString("OutputDate");
                                        count = Integer.parseInt(weather_0_description);

                                        entries.add(new Entry(count, i));
                                        labels.add(weather_0_icon);
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
                                            dataset2.setValueFormatter(new MyValueFormatter());
                                            dataset2.setDrawValues(true);
                                            dataset2.setSliceSpace(3);
                                            dataset2.setSelectionShift(5);
                                            dataset2.setColors(Colors.ALL_COLORS);

                                            PieData data2 = new PieData(labels2, dataset2);
                                            pieChart.setData(data2);

                                            pieChartStyle(pieDesc, behaviour_name);

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

                        // Get the JSONArray weather

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
                params.put(KEY_DATE, finalDate);
                params.put(KEY_END_DATE, finalEndDate);
                params.put(KEY_AMOUNT, finalAmount);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void lastDateIfEmpty(String url, final String lineDesc, final String pieDesc) throws JSONException {
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            JSONObject reader;
                            try {
                                reader = new JSONObject(response);

                                result = reader.getJSONArray("result");

                                    ArrayList<Entry> entries = new ArrayList<>();
                                    ArrayList<String> labels = new ArrayList<>();

                                    int count;
                                    if (result.length() == 1) {
                                        final JSONObject weather_object_0 = result.getJSONObject(0);
                                        final String weather_0_description = weather_object_0.getString("counter");
                                        final String weather_0_icon = weather_object_0.getString("OutputDate");
                                        count = Integer.parseInt(weather_0_description);

                                        entries.add(new Entry(count, 0));
                                        labels.add(weather_0_icon);

                                        entries.add(new Entry(0, 1));
                                        labels.add("");
                                    } else {

                                        for (int i = 0; i < result.length(); i++) {
                                            final JSONObject weather_object_0 = result.getJSONObject(i);
                                            final String weather_0_description = weather_object_0.getString("counter");
                                            final String weather_0_icon = weather_object_0.getString("OutputDate");
                                            count = Integer.parseInt(weather_0_description);

                                            entries.add(new Entry(count, i));
                                            labels.add(weather_0_icon);
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

                                                pieChartStyle(pieDesc, behaviour_name);

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
                    return params;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(stringRequest);
    }

    private class MyValueFormatter implements ValueFormatter {

        private DecimalFormat mFormat;

        MyValueFormatter() {
            mFormat = new DecimalFormat("###,###,###"); // use no decimals
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {

            if(value < 4)
                return "";
            else
                return mFormat.format(value) + "%";
        }
    }

    private void lineChartStyle(String lineDesc){
        lineChart.setVisibility(View.VISIBLE);
        CustomMarkerView mv = new CustomMarkerView(LineChartItem.this, R.layout.content_marker);
        lineChart.setMarkerView(mv);
        lineChart.setScaleEnabled(false);
        lineChart.setDoubleTapToZoomEnabled(false);
        lineChart.setMaxVisibleValueCount(result.length());
        lineChart.setDescription(lineDesc);
        lineChart.animateY(1000);
        lineChart.getLegend().setEnabled(false);
        lineChart.fitScreen();
        lineChart.setTouchEnabled(true);
        YAxis yAxisRight = lineChart.getAxisRight();
        yAxisRight.setEnabled(false);

        hideDialog();
    }

    private void pieChartStyle(String pieDesc, String behaviour_name){
        pieChart.setVisibility(View.VISIBLE);

        Legend l = pieChart.getLegend();
        l.setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);
        l.setWordWrapEnabled(true);
        l.setMaxSizePercent(0.55f);

        pieChart.setDrawHoleEnabled(false);
        pieChart.setUsePercentValues(true);
        pieChart.setDescription(pieDesc + behaviour_name);
        pieChart.setDescriptionTextSize(20);
        pieChart.invalidate();
        pieChart.setDrawSliceText(false);
        pieChart.animateY(2000);
    }

    private int hourlyValid(String date){
        if(date.equals("Day")) {
            Toast.makeText(LineChartItem.this, "Please input date", Toast.LENGTH_LONG).show();
            return 1;
        }
        else
            return 0;
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

    private void hideButtons(){
        btnDatePicker.setVisibility(View.GONE);
        btnEndDatePicker.setVisibility(View.GONE);
        inputAmount.setVisibility(View.GONE);
        lineChart.setVisibility(View.GONE);
        pieChart.setVisibility(View.GONE);
    }

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

    private void showMessage(){
        AlertDialog.Builder builder1 = new AlertDialog.Builder(LineChartItem.this);
        builder1.setMessage("Instructions\n\n" +
                "This report will demonstrate quantity of behaviours per period of time.\n\n" +
                "- To display report please choose option for period of time (hourly, daily, weekly, monthly).\n\n" +
                "- Then select date(s), for weekly and monthly options you will be asked to input amount of weeks or months.\n\n\n" +
                "After initialization of graph you will be able to track behaviours over particular period of time.\n\n" +
                "- To do this simply choose any node on the chart and new graph with behaviours will be displayed.");
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
                    startActivity(intent);
                    return true;
                case R.id.navigation_report:
                    finish();
                    intent = new Intent(LineChartItem.this, Report.class);
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
