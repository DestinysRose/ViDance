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

    ArrayList<HashMap<String, String>> dataList;

    Button btnDatePicker, btnFirstDatePicker, btnEndDatePicker, btnWeekPicker, btnMonthPicker;

    EditText inputAmount;

    private int mYear, mMonth, mDay;

    public static final String LINE_FILTER_HOURLY = "http://thevidance.com/filter/hourly/lineChart.php";
    public static final String LINE_FILTER_DAILY = "http://thevidance.com/filter/daily/lineChart.php";
    public static final String LINE_FILTER_WEEKLY = "http://thevidance.com/filter/weekly/lineChart.php";
    public static final String LINE_FILTER_MONTHLY = "http://thevidance.com/filter/monthly/lineChart.php";

    public static final String KEY_DATE = "date1";
    public static final String KEY_END_DATE = "date2";
    public static final String KEY_AMOUNT = "amount";

    Typeface tf;
    Typeface jf;

    private int[] bArray;
    private String[] bNameArray;

    private RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linechart);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        String fontPath = "fonts/CatCafe.ttf";
        tf = Typeface.createFromAsset(getAssets(), fontPath);
        String fontPath2 = "fonts/James_Fajardo.ttf";
        jf = Typeface.createFromAsset(getAssets(), fontPath2);

        final LineChart lineChart = (LineChart) findViewById(R.id.chart);
        final PieChart pieChart = (PieChart) findViewById(R.id.chart2);

        //Set onClickListeners for date and time
        btnDatePicker = (Button)findViewById(R.id.setDate);
        btnDatePicker.setOnClickListener((View.OnClickListener) this);
        btnDatePicker.setTypeface(jf);
        btnDatePicker.setTextSize(25);


        //Set onClickListeners for date and time
        btnFirstDatePicker = (Button)findViewById(R.id.setFirstDate);
        btnFirstDatePicker.setOnClickListener((View.OnClickListener) this);
        btnFirstDatePicker.setTypeface(jf);
        btnFirstDatePicker.setTextSize(25);

        //Set onClickListeners for date and time
        btnEndDatePicker = (Button)findViewById(R.id.setEndDate);
        btnEndDatePicker.setOnClickListener((View.OnClickListener) this);
        btnEndDatePicker.setTypeface(jf);
        btnEndDatePicker.setTextSize(25);

        //Set onClickListeners for date and time
        btnWeekPicker = (Button)findViewById(R.id.setWeek);
        btnWeekPicker.setOnClickListener(this);
        btnWeekPicker.setTypeface(jf);
        btnWeekPicker.setTextSize(25);

        //Set onClickListeners for date and time
        btnMonthPicker = (Button)findViewById(R.id.setMonth);
        btnMonthPicker.setOnClickListener(this);
        btnMonthPicker.setTypeface(jf);
        btnMonthPicker.setTextSize(25);

        inputAmount = (EditText)findViewById(R.id.setNumber);
        inputAmount.setHint("How many?");
        inputAmount.setTypeface(tf);

        btnDatePicker.setVisibility(View.GONE);
        btnFirstDatePicker.setVisibility(View.GONE);
        btnWeekPicker.setVisibility(View.GONE);
        btnMonthPicker.setVisibility(View.GONE);
        btnEndDatePicker.setVisibility(View.GONE);
        inputAmount.setVisibility(View.GONE);
        lineChart.setVisibility(View.GONE);
        pieChart.setVisibility(View.GONE);

        radioGroup = (RadioGroup) findViewById(R.id.radio);
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
                        setTextByDefault();
                        btnDatePicker.setVisibility(View.VISIBLE);
                        btnFirstDatePicker.setVisibility(View.GONE);
                        btnWeekPicker.setVisibility(View.GONE);
                        btnMonthPicker.setVisibility(View.GONE);
                        btnEndDatePicker.setVisibility(View.GONE);
                        inputAmount.setVisibility(View.GONE);
                        lineChart.setVisibility(View.GONE);
                        pieChart.setVisibility(View.GONE);


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
                        setTextByDefault();
                        btnDatePicker.setVisibility(View.GONE);
                        btnFirstDatePicker.setVisibility(View.VISIBLE);
                        btnEndDatePicker.setVisibility(View.VISIBLE);
                        btnWeekPicker.setVisibility(View.GONE);
                        btnMonthPicker.setVisibility(View.GONE);
                        inputAmount.setVisibility(View.GONE);
                        lineChart.setVisibility(View.GONE);
                        pieChart.setVisibility(View.GONE);

                        Button filDay = (Button) findViewById(R.id.dateApply);
                        filDay.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    storeDateDaily();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        break;
                    case R.id.weekly:
                        setTextByDefault();
                        btnDatePicker.setVisibility(View.GONE);
                        btnFirstDatePicker.setVisibility(View.GONE);
                        btnWeekPicker.setVisibility(View.VISIBLE);
                        btnMonthPicker.setVisibility(View.GONE);
                        btnEndDatePicker.setVisibility(View.GONE);
                        inputAmount.setVisibility(View.VISIBLE);
                        lineChart.setVisibility(View.GONE);
                        pieChart.setVisibility(View.GONE);

                        Button filWeek = (Button) findViewById(R.id.dateApply);
                        filWeek.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    storeDateWeekly();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        break;
                    case R.id.monthly:
                        setTextByDefault();
                        btnDatePicker.setVisibility(View.GONE);
                        btnFirstDatePicker.setVisibility(View.GONE);
                        btnWeekPicker.setVisibility(View.GONE);
                        btnMonthPicker.setVisibility(View.VISIBLE);
                        btnEndDatePicker.setVisibility(View.GONE);
                        inputAmount.setVisibility(View.VISIBLE);
                        lineChart.setVisibility(View.GONE);
                        pieChart.setVisibility(View.GONE);

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

    public class MyValueFormatter implements ValueFormatter {

        private DecimalFormat mFormat;

        public MyValueFormatter() {
            mFormat = new DecimalFormat("###,###,###"); // use no decimals
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {

            if(value < 5)
                return "";
            else
                return mFormat.format(value) + "%";
        }
    }

    private void storeDateHourly() throws JSONException {
        final String date = (String) btnDatePicker.getText();

        if(date.equals("Day")) {
            Toast.makeText(LineChartItem.this,"Please input date",Toast.LENGTH_LONG).show();
        }
        else {
            StringRequest stringRequest = new StringRequest(Request.Method.POST, LINE_FILTER_HOURLY,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            LineChart lineChart = (LineChart) findViewById(R.id.chart);
                            lineChart.setVisibility(View.VISIBLE);

                            //Toast.makeText(LineChartItem.this,response,Toast.LENGTH_LONG).show();

                            JSONObject reader = null;
                            try {
                                reader = new JSONObject(response);

                                result = reader.getJSONArray("result");
                                if (result.length() == 0)
                                    Toast.makeText(LineChartItem.this, "Sorry, no records this day(s)", Toast.LENGTH_LONG).show();
                                else {
                                    //Toast.makeText(LineChartItem.this,"Shit Working!",Toast.LENGTH_LONG).show();

                                    ArrayList<Entry> entries = new ArrayList<>();
                                    ArrayList<String> labels = new ArrayList<>();

                                    int count;
                                    if (result.length() == 1) {
                                        final JSONObject weather_object_0 = result.getJSONObject(0);
                                        final String weather_0_description = weather_object_0.getString("counter");
                                        final String weather_0_icon = weather_object_0.getString("DateField");
                                        count = Integer.parseInt(weather_0_description);

                                        entries.add(new Entry(0, -1));
                                        labels.add("");

                                        entries.add(new Entry(count, 0));
                                        labels.add(weather_0_icon);

                                        entries.add(new Entry(0, 1));
                                        labels.add("");
                                    } else {

                                        for (int i = 0; i < result.length(); i++) {
                                            final JSONObject weather_object_0 = result.getJSONObject(i);
                                            final String weather_0_description = weather_object_0.getString("counter");
                                            final String weather_0_icon = weather_object_0.getString("DateField");
                                            count = Integer.parseInt(weather_0_description);

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
                                    lineChart.setDescription("Number of behaviours per each hour");
                                    lineChart.animateY(1000);
                                    lineChart.getLegend().setEnabled(false);
                                    lineChart.fitScreen();
                                    lineChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                                        @Override
                                        public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {

                                            final int info = h.getXIndex();

                                            try {
                                                JSONObject behaviours = result.getJSONObject(info);
                                                final String behaviour_name = behaviours.getString("DateField");
                                                final Integer b_1 = behaviours.getInt("Bizarre body postures");
                                                final Integer b_2 = behaviours.getInt("Body hitting (expect for the head) with any body part");
                                                final Integer b_3 = behaviours.getInt("Clapping hands (inappropriately)");
                                                final Integer b_4 = behaviours.getInt("Gazing at hands or objects");
                                                final Integer b_5 = behaviours.getInt("Grimacing");
                                                final Integer b_6 = behaviours.getInt("Hair pulling (tearing out patches of hair)");
                                                final Integer b_7 = behaviours.getInt("Head hitting");
                                                final Integer b_8 = behaviours.getInt("Inserting objects in nose, ears, anus, etc.");
                                                final Integer b_9 = behaviours.getInt("Manipulating (e.g. twirling, spinning) objects");
                                                final Integer b_10 = behaviours.getInt("Pacing, jumping, bouncing, running");
                                                final Integer b_11 = behaviours.getInt("Pica (ingesting non-food items)");
                                                final Integer b_12 = behaviours.getInt("Repetitive hand and/or finger movements");
                                                final Integer b_13 = behaviours.getInt("Rocking, repetitive body movements");
                                                final Integer b_14 = behaviours.getInt("Rubbing self");
                                                final Integer b_15 = behaviours.getInt("Self-biting");
                                                final Integer b_16 = behaviours.getInt("Self-scratching");
                                                final Integer b_17 = behaviours.getInt("Sniffing objects, own body");
                                                final Integer b_18 = behaviours.getInt("Teeth grinding (while awake)");
                                                final Integer b_19 = behaviours.getInt("Waving or shaking arms");
                                                final Integer b_20 = behaviours.getInt("Yelling and screaming");

                                                //Pie chart declaration
                                                PieChart pieChart = (PieChart) findViewById(R.id.chart2);
                                                pieChart.setVisibility(View.VISIBLE);


                                                Legend l = pieChart.getLegend();
                                                l.setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);
                                                l.setWordWrapEnabled(true);
                                                l.setMaxSizePercent(0.55f);

                                                ArrayList<Entry> entries2 = new ArrayList<>();

                                                ArrayList<String> labels2 = null;

                                                bArray = new int[]{b_1, b_2, b_3, b_4, b_5, b_6, b_7, b_8, b_9, b_10, b_11, b_12, b_13, b_14, b_15, b_16, b_17, b_18, b_19, b_20};

                                                bNameArray = new String[]{
                                                        "Bizarre body postures",
                                                        "Body hitting (expect head) with any body part",
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

                                                int ePos = 0;
                                                labels2 = new ArrayList<>();
                                                for (int i = 0; i < bArray.length; i++) {
                                                    if (bArray[i] != 0) {
                                                        entries2.add(new Entry(bArray[i], ePos));
                                                        labels2.add(bNameArray[i]);

                                                        ePos++;
                                                    }
                                                }

                                                PieDataSet dataset2 = new PieDataSet(entries2, "");
                                                dataset2.setValueFormatter(new MyValueFormatter());

                                                PieData data2 = new PieData(labels2, dataset2);

                                                dataset2.setColors(Colors.ALL_COLORS);
                                                pieChart.setDrawHoleEnabled(false);
                                                pieChart.setUsePercentValues(true);
                                                pieChart.setData(data2);
                                                pieChart.setDescription("Time: " + behaviour_name);
                                                pieChart.setDescriptionTextSize(20);
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
                    Map<String, String> params = new HashMap<String, String>();
                    params.put(KEY_DATE, date);
                    return params;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(stringRequest);
        }
    }

    private void storeDateDaily() throws JSONException {
        final String date = (String) btnFirstDatePicker.getText();
        final String endDate = (String) btnEndDatePicker.getText();

        if (date.equals("First Day") && endDate.equals("End Day")) {
            Toast.makeText(LineChartItem.this,"Please input first date and end date",Toast.LENGTH_LONG).show();
        }
        else if(date.equals("First Day")) {
            Toast.makeText(LineChartItem.this,"Please input first date",Toast.LENGTH_LONG).show();
        }
        else if(endDate.equals("End Day")) {
            Toast.makeText(LineChartItem.this,"Please input end date",Toast.LENGTH_LONG).show();
        }
        else {
            StringRequest stringRequest = new StringRequest(Request.Method.POST, LINE_FILTER_DAILY,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            LineChart lineChart = (LineChart) findViewById(R.id.chart);
                            lineChart.setVisibility(View.VISIBLE);

                            //Toast.makeText(LineChartItem.this,response,Toast.LENGTH_LONG).show();

                            JSONObject reader = null;
                            try {
                                reader = new JSONObject(response);

                                result = reader.getJSONArray("result");
                                if (result.length() == 0)
                                    Toast.makeText(LineChartItem.this, "Sorry, no records this day(s)", Toast.LENGTH_LONG).show();
                                else {

                                    ArrayList<Entry> entries = new ArrayList<>();
                                    ArrayList<String> labels = new ArrayList<>();

                                    int count;
                                    if (result.length() == 1) {
                                        final JSONObject weather_object_0 = result.getJSONObject(0);
                                        final String weather_0_description = weather_object_0.getString("counter");
                                        final String weather_0_icon = weather_object_0.getString("updated_at");
                                        count = Integer.parseInt(weather_0_description);

                                        entries.add(new Entry(0, -1));
                                        labels.add("");

                                        entries.add(new Entry(count, 0));
                                        labels.add(weather_0_icon);

                                        entries.add(new Entry(0, 1));
                                        labels.add("");
                                    } else {

                                        for (int i = 0; i < result.length(); i++) {
                                            final JSONObject weather_object_0 = result.getJSONObject(i);
                                            final String weather_0_description = weather_object_0.getString("counter");
                                            final String weather_0_icon = weather_object_0.getString("updated_at");
                                            count = Integer.parseInt(weather_0_description);

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
                                    lineChart.setDescription("Number of behaviours per each day");
                                    lineChart.animateY(1000);
                                    lineChart.getLegend().setEnabled(false);
                                    lineChart.fitScreen();
                                    lineChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                                        @Override
                                        public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {

                                            final int info = h.getXIndex();

                                            try {
                                                JSONObject behaviours = result.getJSONObject(info);
                                                final String date_part = behaviours.getString("updated_at");
                                                final Integer b_1 = behaviours.getInt("Bizarre body postures");
                                                final Integer b_2 = behaviours.getInt("Body hitting (expect for the head) with any body part");
                                                final Integer b_3 = behaviours.getInt("Clapping hands (inappropriately)");
                                                final Integer b_4 = behaviours.getInt("Gazing at hands or objects");
                                                final Integer b_5 = behaviours.getInt("Grimacing");
                                                final Integer b_6 = behaviours.getInt("Hair pulling (tearing out patches of hair)");
                                                final Integer b_7 = behaviours.getInt("Head hitting");
                                                final Integer b_8 = behaviours.getInt("Inserting objects in nose, ears, anus, etc.");
                                                final Integer b_9 = behaviours.getInt("Manipulating (e.g. twirling, spinning) objects");
                                                final Integer b_10 = behaviours.getInt("Pacing, jumping, bouncing, running");
                                                final Integer b_11 = behaviours.getInt("Pica (ingesting non-food items)");
                                                final Integer b_12 = behaviours.getInt("Repetitive hand and/or finger movements");
                                                final Integer b_13 = behaviours.getInt("Rocking, repetitive body movements");
                                                final Integer b_14 = behaviours.getInt("Rubbing self");
                                                final Integer b_15 = behaviours.getInt("Self-biting");
                                                final Integer b_16 = behaviours.getInt("Self-scratching");
                                                final Integer b_17 = behaviours.getInt("Sniffing objects, own body");
                                                final Integer b_18 = behaviours.getInt("Teeth grinding (while awake)");
                                                final Integer b_19 = behaviours.getInt("Waving or shaking arms");
                                                final Integer b_20 = behaviours.getInt("Yelling and screaming");

                                                //Pie chart declaration
                                                PieChart pieChart = (PieChart) findViewById(R.id.chart2);
                                                pieChart.setVisibility(View.VISIBLE);

                                                Legend l = pieChart.getLegend();
                                                l.setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);
                                            /*l.setXEntrySpace(7f);
                                            l.setYEntrySpace(5f);
                                            l.setYOffset(0f);*/
                                                l.setWordWrapEnabled(true);
                                                l.setMaxSizePercent(0.55f);

                                                ArrayList<Entry> entries2 = new ArrayList<>();

                                                ArrayList<String> labels2 = null;

                                                bArray = new int[]{b_1, b_2, b_3, b_4, b_5, b_6, b_7, b_8, b_9, b_10, b_11, b_12, b_13, b_14, b_15, b_16, b_17, b_18, b_19, b_20};

                                                bNameArray = new String[]{
                                                        "Bizarre body postures",
                                                        "Body hitting (expect head) with any body part",
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

                                                int ePos = 0;
                                                labels2 = new ArrayList<>();
                                                for (int i = 0; i < bArray.length; i++) {
                                                    if (bArray[i] != 0) {
                                                        entries2.add(new Entry(bArray[i], ePos));
                                                        labels2.add(bNameArray[i]);

                                                        ePos++;
                                                    }
                                                }

                                                PieDataSet dataset2 = new PieDataSet(entries2, "");
                                                dataset2.setValueFormatter(new MyValueFormatter());

                                                PieData data2 = new PieData(labels2, dataset2);

                                                dataset2.setColors(Colors.ALL_COLORS);
                                                pieChart.setDrawHoleEnabled(false);
                                                pieChart.setUsePercentValues(true);
                                                pieChart.setData(data2);
                                                pieChart.setDescription("Day: " + date_part);
                                                pieChart.setDescriptionTextSize(20);
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
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
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
                    Map<String, String> params = new HashMap<String, String>();
                    params.put(KEY_DATE, date);
                    params.put(KEY_END_DATE, endDate);
                    return params;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(stringRequest);
        }
    }

    private void storeDateWeekly() throws JSONException {
        final String date = (String) btnWeekPicker.getText();
        final String amount = inputAmount.getText().toString().trim();

        if(amount.isEmpty() && date.equals("First day of week")){
            Toast.makeText(LineChartItem.this,"Please input date and amount of weeks",Toast.LENGTH_LONG).show();
        }

        else if (date.equals("First day of week")){
            Toast.makeText(LineChartItem.this,"Please input date",Toast.LENGTH_LONG).show();
        }

        else if(amount.isEmpty()){
            Toast.makeText(LineChartItem.this,"Please input amount of weeks",Toast.LENGTH_LONG).show();
        }
        else {
            StringRequest stringRequest = new StringRequest(Request.Method.POST, LINE_FILTER_WEEKLY,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(final String response) {

                            LineChart lineChart = (LineChart) findViewById(R.id.chart);
                            lineChart.setVisibility(View.VISIBLE);
                            //Toast.makeText(LineChartItem.this,response,Toast.LENGTH_LONG).show();
                            JSONObject reader = null;
                            try {
                                reader = new JSONObject(response);

                                result = reader.getJSONArray("result");
                                if (result.length() == 0)
                                    Toast.makeText(LineChartItem.this, "Sorry, no records this day(s)", Toast.LENGTH_LONG).show();
                                else {
                                    ArrayList<Entry> entries = new ArrayList<>();
                                    ArrayList<String> labels = new ArrayList<>();

                                    int count;

                                    if (result.length() == 1) {
                                        final JSONObject weather_object_0 = result.getJSONObject(0);
                                        final String weather_0_description = weather_object_0.getString("counter");
                                        final String weekNo = weather_object_0.getString("DatePart");
                                        final String weather_0_icon = weather_object_0.getString("updated_at");
                                        count = Integer.parseInt(weather_0_description);

                                        entries.add(new Entry(0, -1));
                                        labels.add("");

                                        entries.add(new Entry(count, 0));
                                        labels.add("Week " + weekNo);

                                        entries.add(new Entry(0, 1));
                                        labels.add("");
                                    } else {

                                        for (int i = 0; i < result.length(); i++) {
                                            final JSONObject weather_object_0 = result.getJSONObject(i);
                                            final String weather_0_description = weather_object_0.getString("counter");
                                            final String weekNo = weather_object_0.getString("DatePart");
                                            final String weather_0_icon = weather_object_0.getString("updated_at");
                                            count = Integer.parseInt(weather_0_description);

                                            entries.add(new Entry(count, i));
                                            labels.add("Week " + weekNo);
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
                                    lineChart.setDescription("Number of behaviours per each week");
                                    lineChart.animateY(1000);
                                    lineChart.getLegend().setEnabled(false);
                                    lineChart.fitScreen();
                                    lineChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                                        @Override
                                        public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {

                                            final int info = h.getXIndex();

                                            try {
                                                JSONObject behaviours = result.getJSONObject(info);
                                                final String behaviour_name = behaviours.getString("DatePart");
                                                final Integer b_1 = behaviours.getInt("Bizarre body postures");
                                                final Integer b_2 = behaviours.getInt("Body hitting (expect for the head) with any body part");
                                                final Integer b_3 = behaviours.getInt("Clapping hands (inappropriately)");
                                                final Integer b_4 = behaviours.getInt("Gazing at hands or objects");
                                                final Integer b_5 = behaviours.getInt("Grimacing");
                                                final Integer b_6 = behaviours.getInt("Hair pulling (tearing out patches of hair)");
                                                final Integer b_7 = behaviours.getInt("Head hitting");
                                                final Integer b_8 = behaviours.getInt("Inserting objects in nose, ears, anus, etc.");
                                                final Integer b_9 = behaviours.getInt("Manipulating (e.g. twirling, spinning) objects");
                                                final Integer b_10 = behaviours.getInt("Pacing, jumping, bouncing, running");
                                                final Integer b_11 = behaviours.getInt("Pica (ingesting non-food items)");
                                                final Integer b_12 = behaviours.getInt("Repetitive hand and/or finger movements");
                                                final Integer b_13 = behaviours.getInt("Rocking, repetitive body movements");
                                                final Integer b_14 = behaviours.getInt("Rubbing self");
                                                final Integer b_15 = behaviours.getInt("Self-biting");
                                                final Integer b_16 = behaviours.getInt("Self-scratching");
                                                final Integer b_17 = behaviours.getInt("Sniffing objects, own body");
                                                final Integer b_18 = behaviours.getInt("Teeth grinding (while awake)");
                                                final Integer b_19 = behaviours.getInt("Waving or shaking arms");
                                                final Integer b_20 = behaviours.getInt("Yelling and screaming");

                                                //Pie chart declaration
                                                PieChart pieChart = (PieChart) findViewById(R.id.chart2);
                                                pieChart.setVisibility(View.VISIBLE);

                                                Legend l = pieChart.getLegend();
                                                l.setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);
                                            /*l.setXEntrySpace(7f);
                                            l.setYEntrySpace(5f);
                                            l.setYOffset(0f);*/
                                                l.setWordWrapEnabled(true);
                                                l.setMaxSizePercent(0.55f);

                                                ArrayList<Entry> entries2 = new ArrayList<>();

                                                ArrayList<String> labels2 = null;

                                                bArray = new int[]{b_1, b_2, b_3, b_4, b_5, b_6, b_7, b_8, b_9, b_10, b_11, b_12, b_13, b_14, b_15, b_16, b_17, b_18, b_19, b_20};

                                                bNameArray = new String[]{
                                                        "Bizarre body postures",
                                                        "Body hitting (expect head) with any body part",
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

                                                int ePos = 0;
                                                labels2 = new ArrayList<>();
                                                for (int i = 0; i < bArray.length; i++) {
                                                    if (bArray[i] != 0) {
                                                        entries2.add(new Entry(bArray[i], ePos));
                                                        labels2.add(bNameArray[i]);

                                                        ePos++;
                                                    }
                                                }

                                                PieDataSet dataset2 = new PieDataSet(entries2, "");
                                                dataset2.setValueFormatter(new MyValueFormatter());

                                                PieData data2 = new PieData(labels2, dataset2);

                                                dataset2.setColors(Colors.ALL_COLORS);
                                                pieChart.setDrawHoleEnabled(false);
                                                pieChart.setUsePercentValues(true);
                                                pieChart.setData(data2);
                                                pieChart.setDescription("Week: " + behaviour_name);
                                                pieChart.setDescriptionTextSize(20);
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
                    Map<String, String> params = new HashMap<String, String>();
                    params.put(KEY_DATE, date);
                    params.put(KEY_AMOUNT, amount);
                    return params;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(stringRequest);
        }

    }

    private void storeDateMonthly() throws JSONException {
        final String date = (String) btnMonthPicker.getText();
        final String amount = inputAmount.getText().toString().trim();

        if(amount.isEmpty() && date.equals("First day of month"))
            Toast.makeText(LineChartItem.this,"Please input date and amount of months",Toast.LENGTH_LONG).show();

        else if (date.equals("First day of month"))
            Toast.makeText(LineChartItem.this,"Please input date",Toast.LENGTH_LONG).show();

        else if(amount.isEmpty())
            Toast.makeText(LineChartItem.this,"Please input amount of months",Toast.LENGTH_LONG).show();

        else {
            StringRequest stringRequest = new StringRequest(Request.Method.POST, LINE_FILTER_MONTHLY,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(final String response) {

                            LineChart lineChart = (LineChart) findViewById(R.id.chart);
                            lineChart.setVisibility(View.VISIBLE);
                            //Toast.makeText(LineChartItem.this,response,Toast.LENGTH_LONG).show();
                            JSONObject reader = null;
                            try {
                                reader = new JSONObject(response);

                                result = reader.getJSONArray("result");
                                if (result.length() == 0)
                                    Toast.makeText(LineChartItem.this, "Sorry, no records this day(s)", Toast.LENGTH_LONG).show();
                                else {
                                    ArrayList<Entry> entries = new ArrayList<>();
                                    ArrayList<String> labels = new ArrayList<>();

                                    int count;

                                    if (result.length() == 1) {
                                        final JSONObject weather_object_0 = result.getJSONObject(0);
                                        final String weather_0_description = weather_object_0.getString("counter");
                                        final String monthName = weather_object_0.getString("DatePart");
                                        final String weather_0_icon = weather_object_0.getString("updated_at");
                                        count = Integer.parseInt(weather_0_description);

                                        entries.add(new Entry(0, -1));
                                        labels.add("");

                                        entries.add(new Entry(count, 0));
                                        labels.add(monthName);

                                        entries.add(new Entry(0, 1));
                                        labels.add("");
                                    } else {

                                        for (int i = 0; i < result.length(); i++) {
                                            final JSONObject weather_object_0 = result.getJSONObject(i);
                                            final String weather_0_description = weather_object_0.getString("counter");
                                            final String monthName = weather_object_0.getString("DatePart");
                                            final String weather_0_icon = weather_object_0.getString("updated_at");
                                            count = Integer.parseInt(weather_0_description);

                                            entries.add(new Entry(count, i));
                                            labels.add(monthName);
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
                                    lineChart.setDescription("Number of behaviours per each month");
                                    lineChart.animateY(1000);
                                    lineChart.getLegend().setEnabled(false);
                                    lineChart.fitScreen();
                                    lineChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                                        @Override
                                        public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {

                                            final int info = h.getXIndex();

                                            try {
                                                JSONObject behaviours = result.getJSONObject(info);
                                                final String behaviour_name = behaviours.getString("DatePart");
                                                final Integer b_1 = behaviours.getInt("Bizarre body postures");
                                                final Integer b_2 = behaviours.getInt("Body hitting (expect for the head) with any body part");
                                                final Integer b_3 = behaviours.getInt("Clapping hands (inappropriately)");
                                                final Integer b_4 = behaviours.getInt("Gazing at hands or objects");
                                                final Integer b_5 = behaviours.getInt("Grimacing");
                                                final Integer b_6 = behaviours.getInt("Hair pulling (tearing out patches of hair)");
                                                final Integer b_7 = behaviours.getInt("Head hitting");
                                                final Integer b_8 = behaviours.getInt("Inserting objects in nose, ears, anus, etc.");
                                                final Integer b_9 = behaviours.getInt("Manipulating (e.g. twirling, spinning) objects");
                                                final Integer b_10 = behaviours.getInt("Pacing, jumping, bouncing, running");
                                                final Integer b_11 = behaviours.getInt("Pica (ingesting non-food items)");
                                                final Integer b_12 = behaviours.getInt("Repetitive hand and/or finger movements");
                                                final Integer b_13 = behaviours.getInt("Rocking, repetitive body movements");
                                                final Integer b_14 = behaviours.getInt("Rubbing self");
                                                final Integer b_15 = behaviours.getInt("Self-biting");
                                                final Integer b_16 = behaviours.getInt("Self-scratching");
                                                final Integer b_17 = behaviours.getInt("Sniffing objects, own body");
                                                final Integer b_18 = behaviours.getInt("Teeth grinding (while awake)");
                                                final Integer b_19 = behaviours.getInt("Waving or shaking arms");
                                                final Integer b_20 = behaviours.getInt("Yelling and screaming");

                                                //Pie chart declaration
                                                PieChart pieChart = (PieChart) findViewById(R.id.chart2);
                                                pieChart.setVisibility(View.VISIBLE);

                                                Legend l = pieChart.getLegend();
                                                l.setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);
                                            /*l.setXEntrySpace(7f);
                                            l.setYEntrySpace(5f);
                                            l.setYOffset(0f);*/
                                                l.setWordWrapEnabled(true);
                                                l.setMaxSizePercent(0.55f);

                                                ArrayList<Entry> entries2 = new ArrayList<>();

                                                ArrayList<String> labels2 = null;

                                                bArray = new int[]{b_1, b_2, b_3, b_4, b_5, b_6, b_7, b_8, b_9, b_10, b_11, b_12, b_13, b_14, b_15, b_16, b_17, b_18, b_19, b_20};

                                                bNameArray = new String[]{
                                                        "Bizarre body postures",
                                                        "Body hitting (expect head) with any body part",
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

                                                int ePos = 0;
                                                labels2 = new ArrayList<>();
                                                for (int i = 0; i < bArray.length; i++) {
                                                    if (bArray[i] != 0) {
                                                        entries2.add(new Entry(bArray[i], ePos));
                                                        labels2.add(bNameArray[i]);

                                                        ePos++;
                                                    }
                                                }

                                                PieDataSet dataset2 = new PieDataSet(entries2, "");
                                                dataset2.setValueFormatter(new MyValueFormatter());
                                                //dataSet.setSelectionShift(0f);




                                                PieData data2 = new PieData(labels2, dataset2);

                                                dataset2.setColors(Colors.ALL_COLORS);
                                                pieChart.setDrawHoleEnabled(false);
                                                pieChart.setUsePercentValues(true);
                                                pieChart.setData(data2);
                                                pieChart.setDescription("Month: " + behaviour_name);
                                                pieChart.setDescriptionTextSize(20);
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
                    Map<String, String> params = new HashMap<String, String>();
                    params.put(KEY_DATE, date);
                    params.put(KEY_AMOUNT, amount);
                    return params;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(stringRequest);
        }
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
        if (v == btnFirstDatePicker) {
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
                                    btnFirstDatePicker.setText(year + "-" + 0 + (monthOfYear + 1) + "-" + "0" + dayOfMonth);
                                else
                                    btnFirstDatePicker.setText(year + "-" + 0 + (monthOfYear + 1) + "-" + dayOfMonth);
                            }
                            else
                                btnFirstDatePicker.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);

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
                            if(monthOfYear < 10){
                                if(dayOfMonth < 10)
                                    btnEndDatePicker.setText(year + "-" + 0 + (monthOfYear + 1) + "-" + "0" + dayOfMonth);
                                else
                                    btnEndDatePicker.setText(year + "-" + 0 + (monthOfYear + 1) + "-" + dayOfMonth);
                            }
                            else
                                btnEndDatePicker.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);

                        }
                    }, mYear, mMonth, mDay);

            datePickerDialog.show();
        }
        if (v == btnWeekPicker) {
            // Get Current Time
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
                                    btnWeekPicker.setText(year + "-" + 0 + (monthOfYear + 1) + "-" + "0" + dayOfMonth);
                                else
                                    btnWeekPicker.setText(year + "-" + 0 + (monthOfYear + 1) + "-" + dayOfMonth);
                            }
                            else
                                btnWeekPicker.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
                        }
                    }, mYear, mMonth, mDay);

            datePickerDialog.show();
        }
        if (v == btnMonthPicker) {
            // Get Current Time
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
                                    btnMonthPicker.setText(year + "-" + 0 + (monthOfYear + 1) + "-" + "0" + dayOfMonth);
                                else
                                    btnMonthPicker.setText(year + "-" + 0 + (monthOfYear + 1) + "-" + dayOfMonth);
                            }
                            else
                                btnMonthPicker.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);

                        }
                    }, mYear, mMonth, mDay);

            datePickerDialog.show();
        }
    }

    private void setTextByDefault(){
        btnDatePicker.setText("Day");
        btnFirstDatePicker.setText("First Day");
        btnEndDatePicker.setText("End Day");
        btnWeekPicker.setText("First day of week");
        btnMonthPicker.setText("First day of month");
        inputAmount.setText("");
    }

    private void showMessage(){
        AlertDialog.Builder builder1 = new AlertDialog.Builder(LineChartItem.this);
        builder1.setMessage("Instructions\n\n" +
                "This report will demonstrate quantity of behaviours per period of time.\n\n" +
                "*To display report please choose option for period of time (hourly, daily, weekly, monthly).\n\n" +
                "*Then select date(s), for weekly and monthly options you will be asked to input amount of weeks or months.\n\n" +
                "*After initialization of graph you will be able to track behaviours over particular period of time.\n\n" +
                "*To do this simply choose any node on the chart and new graph with behaviours will be displayed.");
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
