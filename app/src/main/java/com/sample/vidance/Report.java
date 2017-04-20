package com.sample.vidance;


import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.sample.vidance.helper.BehaviourHandler;
import com.sample.vidance.helper.HTTPDataHandler;
import com.sample.vidance.helper.SQLiteHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Danil on 06.04.2017.
 */

public class Report extends AppCompatActivity {
    private SQLiteHandler db;

    //Show dialog when fetching
    private ProgressDialog pDialog;

    //JSON Array
    private JSONArray result;
    private JSONArray pieChartData;

    private JSONArray report = null;

    private String myJSON;

    private static final String TAG_RESULTS="result";

    ArrayList<HashMap<String, String>> dataList;

    ListView list;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.resource_report);

        //list = (ListView) findViewById(R.id.getReport);
        //dataList = new ArrayList<HashMap<String,String>>();



        //final TextView tv = (TextView) findViewById(R.id.tv);
        Button btn = (Button) findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //tv.setText("");
                //urlString = "http://thevidance.com/get_behaviour.php";

                new ProcessJSON().execute(BehaviourHandler.RECORD_URL);
            }
        });
    }



    public class ProcessJSON extends AsyncTask<String, Void, String>{

        //Line chart declaration
        LineChart lineChart = (LineChart) findViewById(R.id.chart);

        protected String doInBackground(String... strings){
            String stream;
            String urlString = strings[0];

            HTTPDataHandler hh = new HTTPDataHandler();
            stream = hh.GetHTTPData(urlString);

            // Return the data from specified url
            return stream;
        }

        protected void onPostExecute(final String stream){
            //TextView tv = (TextView) findViewById(R.id.tv);
            //tv.setText(stream);
            /*
                Important in JSON DATA
                -------------------------
                * Square bracket ([) represents a JSON array
                * Curly bracket ({) represents a JSON object
                * JSON object contains key/value pairs
                * Each key is a String and value may be different data types
             */

            //..........Process JSON DATA................
            if(stream !=null){
                try{
                    // Get the full HTTP Data as JSONObject
                    JSONObject reader = new JSONObject(stream);

                    // Get the JSONArray weather
                    result = reader.getJSONArray("result");
                    // Get the weather array first JSONObject

                    //list = (ListView) findViewById(R.id.getReport);
                    //dataList = new ArrayList<>();

                    ArrayList<Entry> entries = new ArrayList<>();
                    ArrayList<String> labels = new ArrayList<>();

                    int count;
                    for(int i = 0; i < result.length(); i++) {
                        final JSONObject weather_object_0 = result.getJSONObject(i);
                        final String weather_0_id = weather_object_0.getString("bc_id");
                        final String weather_0_main = weather_object_0.getString("bName");
                        final String weather_0_description = weather_object_0.getString("counter");
                        final String weather_0_icon = weather_object_0.getString("updated_at");



                        count = Integer.parseInt(weather_0_description);
                        entries.add(new Entry(count, i));

                        labels.add(weather_0_icon);
                    }

                    LineDataSet dataset = new LineDataSet(entries, "Implementation in progress");
                    LineData data = new LineData(labels, dataset);

                    lineChart.setTouchEnabled(true);

                    CustomMarkerView mv = new CustomMarkerView (Report.this, R.layout.content_marker);
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

                    lineChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                        @Override
                        public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {

                            final int info = h.getXIndex();

                            try {
                                JSONObject severity_info = result.getJSONObject(info);
                                final String weather_0_main = severity_info.getString("bName");
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

                                 if (count_mild > 0 && count_severe > 0 && count_moderate > 0){
                                    entries2.add(new Entry(count_mild, 0));
                                    entries2.add(new Entry(count_moderate, 1));
                                    entries2.add(new Entry(count_severe, 2));

                                    labels2 = new ArrayList<>();
                                    labels2.add("Mild");
                                    labels2.add("Moderate");
                                    labels2.add("Severe");
                                }else if (count_severe > 0 && count_moderate > 0) {

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

                                }  else {

                                    entries2.add(new Entry(count_mild, 0));

                                    labels2 = new ArrayList<>();
                                    labels2.add("Mild");
                                }

                                PieDataSet dataset2 = new PieDataSet(entries2, "");

                                PieData data2 = new PieData(labels2, dataset2);

                                dataset2.setColors(ColorTemplate.COLORFUL_COLORS);
                                pieChart.setDescription("Description");
                                pieChart.setData(data2);
                                pieChart.setDescription(weather_0_main);
                                pieChart.invalidate();

                                dataset2.setDrawValues(false);

                                pieChart.animateY(0);

                                pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                                    @Override
                                    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {

                                        int slice = h.getXIndex();

                                        try {
                                            list = (ListView) findViewById(R.id.getReport);
                                            dataList = new ArrayList<>();
                                            JSONObject reader = new JSONObject(stream);

                                            // Get the JSONArray weather
                                            pieChartData = reader.getJSONArray("display");

                                            JSONObject chart_slice = pieChartData.getJSONObject(slice);
                                            final String bName = chart_slice.getString("bName");
                                            final String severity = chart_slice.getString("severity");
                                            final String updated_at = chart_slice.getString("updated_at");

                                            Toast.makeText(getApplicationContext(), weather_0_main + " ----- " + severity + " ----- " + updated_at, Toast.LENGTH_LONG).show();

                                            for(int i=0;i<pieChartData.length();i++) {
                                                HashMap<String, String> reportData = new HashMap<String, String>();

                                                reportData.put(BehaviourHandler.TAG_BNAME, bName);
                                                reportData.put(BehaviourHandler.TAG_SEVER, severity);
                                                reportData.put(BehaviourHandler.TAG_UPDATED_AT, updated_at);

                                                dataList.add(reportData);
                                            }
                                            ListAdapter adapter = new SimpleAdapter(
                                                    Report.this, dataList, R.layout.content_report,
                                                    new String[]{BehaviourHandler.TAG_BNAME,BehaviourHandler.TAG_SEVER,BehaviourHandler.TAG_UPDATED_AT},
                                                    new int[]{ R.id.bName, R.id.severity, R.id.updated_at}
                                            );


                                            list.setAdapter(adapter);

                                        } catch (JSONException e1) {
                                            e1.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onNothingSelected() {

                                    }
                                });

                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }

                        @Override
                        public void onNothingSelected() {

                        }
                    });
                    // process other data as this way..............
                }catch(JSONException e){
                    e.printStackTrace();
                }
            } // if statement end
        } // onPostExecute() end
    } // ProcessJSON class end
}