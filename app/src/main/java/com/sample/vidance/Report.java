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
    //Handler for fetching data from URL
    private SQLiteHandler db;

    //Show dialog when fetching
    private ProgressDialog pDialog;

    //JSON Array
    private JSONArray result;

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

        list = (ListView) findViewById(R.id.getReport);
        dataList = new ArrayList<HashMap<String,String>>();



        //final TextView tv = (TextView) findViewById(R.id.tv);
        Button btn = (Button) findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //tv.setText("");
                //urlString = "http://thevidance.com/get_behaviour.php";

                new ProcessJSON().execute(BehaviourHandler.RECORD_URL);

                //Pie chart declaration
                PieChart pieChart = (PieChart) findViewById(R.id.chart2);

                ArrayList<Entry> entries2 = new ArrayList<>();
                entries2.add(new Entry(4f, 0));
                entries2.add(new Entry(8f, 1));
                entries2.add(new Entry(6f, 2));
                entries2.add(new Entry(12f, 3));
                entries2.add(new Entry(18f, 4));
                entries2.add(new Entry(9f, 5));

                PieDataSet dataset2 = new PieDataSet(entries2, "");

                ArrayList<String> labels2 = new ArrayList<String>();
                labels2.add("B1");
                labels2.add("B2");
                labels2.add("B3");
                labels2.add("B4");
                labels2.add("B5");
                labels2.add("B6");
                PieData data2 = new PieData(labels2, dataset2);

                dataset2.setColors(ColorTemplate.COLORFUL_COLORS);
                pieChart.setDescription("Description");
                pieChart.setData(data2);

                pieChart.animateY(2000);
            }
        });


    }



    public class ProcessJSON extends AsyncTask<String, Void, String> implements OnChartValueSelectedListener {

        //Line chart declaration
        LineChart lineChart = (LineChart) findViewById(R.id.chart);

        @Override
        public void onValueSelected(Entry entry, int i, Highlight highlight) {
            Toast.makeText(Report.this,"Bla Bla",Toast.LENGTH_LONG).show();
        }

        @Override
        public void onNothingSelected() {

        }

        protected String doInBackground(String... strings){
            String stream = null;
            String urlString = strings[0];

            HTTPDataHandler hh = new HTTPDataHandler();
            stream = hh.GetHTTPData(urlString);

            // Return the data from specified url
            return stream;
        }

        protected void onPostExecute(String stream){
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
                    dataList = new ArrayList<HashMap<String,String>>();
                    //getDataRecord();

                    //lineChart.setOnClickListener(this);
                    ArrayList<Entry> entries = new ArrayList<>();
                    ArrayList<String> labels = new ArrayList<String>();

                    int count;
                    for(int i = 0; i < result.length(); i++) {
                        JSONObject weather_object_0 = result.getJSONObject(i);
                        String weather_0_id = weather_object_0.getString("bc_id");
                        final String weather_0_main = weather_object_0.getString("bName");
                        String weather_0_description = weather_object_0.getString("counter");
                        String weather_0_icon = weather_object_0.getString("updated_at");

                        HashMap<String,String> reportData = new HashMap<String,String>();

                        reportData.put("bc_id","ID: " + weather_0_id);
                        reportData.put("bName","Behavior: " + weather_0_main);
                        reportData.put("counter","How many times: " + weather_0_description);
                        reportData.put("updated_at","Date: " + weather_0_icon);

                        dataList.add(reportData);

                        count = Integer.parseInt(weather_0_description);
                        entries.add(new Entry(count, i));

                        lineChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                            @Override
                            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                                Toast.makeText(Report.this, "Value: " + e + ", xIndex: "
                                        + h.getXIndex() + ", DataSet index: " + h.getDataSetIndex(),Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onNothingSelected() {

                            }
                        });

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
                    lineChart.animateY(1000);

                    ListAdapter adapter = new SimpleAdapter(
                            Report.this, dataList, R.layout.content_report,
                            new String[]{BehaviourHandler.TAG_BCID, BehaviourHandler.TAG_BNAME,
                                    "counter",BehaviourHandler.TAG_UPDATED_AT},
                            new int[]{R.id.bc_id, R.id.bName, R.id.severity, R.id.updated_at}
                    );

                    list.setAdapter(adapter);
                    // process other data as this way..............
                }catch(JSONException e){
                    e.printStackTrace();
                }
            } // if statement end
        } // onPostExecute() end
    } // ProcessJSON class end
}