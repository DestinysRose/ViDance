package com.sample.vidance;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.sample.vidance.helper.BehaviourHandler;
import com.sample.vidance.helper.SQLiteHandler;
import com.sample.vidance.helper.SessionManager;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Danil on 06.04.2017.
 */

public class Report extends AppCompatActivity {
    private TextView txtUpdatedAt;
    private TextView txtName;
    private TextView txtCName;
    private TextView txtBName;
    private TextView txtSeverity;
    private EditText inputDateStart;
    private EditText inputDateEnd;
    private Button buttonGet;
    private TextView getReport;

    private SessionManager session;

    //An ArrayList for Spinner Items
    private ArrayList<String> record;

    //Handler for fetching data from URL
    private SQLiteHandler db;

    //Show dialog when fetching
    private ProgressDialog pDialog;

    //JSON Array
    private JSONArray result;

    String myJSON;

    private static final String TAG_RESULTS="result";

    private ProgressDialog loading;

    JSONArray report = null;

    ArrayList<HashMap<String, String>> dataList;

    ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*super.onCreate(savedInstanceState);
        setContentView(R.layout.content_report);*/

        // Progress dialog

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        /*inputDateStart = (EditText) findViewById(R.id.dateStart);
        inputDateEnd = (EditText) findViewById(R.id.dateEnd);
        buttonGet = (Button) findViewById(R.id.buttonGet);*/
        //getReport = (TextView) findViewById(R.id.getReport);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.resource_report);
        list = (ListView) findViewById(R.id.getReport);
        dataList = new ArrayList<HashMap<String,String>>();
        getData();

        //Line chart declaration
        LineChart lineChart = (LineChart) findViewById(R.id.chart);

        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(4f, 0));
        entries.add(new Entry(8f, 1));
        entries.add(new Entry(6f, 2));
        entries.add(new Entry(2f, 3));
        entries.add(new Entry(18f, 4));
        entries.add(new Entry(9f, 5));

        LineDataSet dataset = new LineDataSet(entries, "Implementation in progress");

        ArrayList<String> labels = new ArrayList<String>();
        labels.add("B1");
        labels.add("B2");
        labels.add("B3");
        labels.add("B4");
        labels.add("B5");
        labels.add("B6");

        LineData data = new LineData(labels, dataset);
        dataset.setColors(ColorTemplate.COLORFUL_COLORS); //
        dataset.setDrawCubic(true);
        dataset.setDrawFilled(true);

        lineChart.setData(data);
        lineChart.animateY(1000);

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
        dataset2.setColors(ColorTemplate.COLORFUL_COLORS); //
        pieChart.setDescription("Description");
        pieChart.setData(data2);

        pieChart.animateY(2000);

        pieChart.saveToGallery("/sd/mychart.jpg", 85); // 85 is the quality of the image

        //getDataRecord();

    }


    /*private void getDataRecord(){
        //Creating a string request
        pDialog.setMessage("Fetching ...");

        StringRequest stringRequest = new StringRequest(BehaviourHandler.RECORD_URL,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        JSONObject j = null;
                        showDialog();
                        try {
                            //Parsing the fetched Json String to JSON Object
                            j = new JSONObject(response);
                            JSONObject user = j.getJSONObject("user");
                            String name = user.getString("name");
                            String cname = user.getString("cname");
                            String updated_at = user
                                    .getString("created_at");
                            //Storing the Array of JSON String to our JSON Array
                            result = j.getJSONArray(BehaviourHandler.JSON_ARRAY);
                            hideDialog();
                            //Calling method getStudents to get the students from the JSON Array
                            getRecord(result);
                        } catch (JSONException e) {

                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hideDialog();
                    }
                });

        //Creating a request queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }*/

    public void getData(){


        class GetDataJSON extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... params) {

                DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());
                HttpPost httppost = new HttpPost("http://thevidance.com/get_behaviour.php");

                // Depends on your web service
                httppost.setHeader("Content-type", "application/json");

                InputStream inputStream = null;
                String result = null;
                try {
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity entity = response.getEntity();

                    inputStream = entity.getContent();
                    // json is UTF-8 by default
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
                    StringBuilder sb = new StringBuilder();

                    String line = null;
                    while ((line = reader.readLine()) != null)
                    {
                        sb.append(line + "\n");
                    }
                    result = sb.toString();
                } catch (Exception e) {
                    // Oops
                }
                finally {
                    try{if(inputStream != null)inputStream.close();}catch(Exception squish){}
                }
                return result;

            }
            @Override
            protected void onPostExecute(String result){
                myJSON=result;
                showList();
            }
        }

        GetDataJSON g = new GetDataJSON();
        g.execute();
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    protected void showList(){

        try {
            JSONObject jsonObj = new JSONObject(myJSON);
            report = jsonObj.getJSONArray(TAG_RESULTS);

            for(int i=0;i<report.length();i++){
                JSONObject c = report.getJSONObject(i);
                String bc_id = c.getString(BehaviourHandler.TAG_BCID);
                /*String name = c.getString(BehaviourHandler.TAG_NAME);
                String cname = c.getString(BehaviourHandler.TAG_CNAME);*/
                String bName = c.getString(BehaviourHandler.TAG_BNAME);
                String severity = c.getString(BehaviourHandler.TAG_SEVER);
                /*String updated_at = c.getString(BehaviourHandler.TAG_UPDATED_AT);*/
                HashMap<String,String> reportData = new HashMap<String,String>();

                reportData.put(BehaviourHandler.TAG_BCID,bc_id);
               /* reportData.put(BehaviourHandler.TAG_NAME,name);
                reportData.put(BehaviourHandler.TAG_CNAME,cname);*/
                reportData.put(BehaviourHandler.TAG_BNAME,bName);
                reportData.put(BehaviourHandler.TAG_SEVER,severity);
                //reportData.put(BehaviourHandler.TAG_UPDATED_AT,updated_at);

                dataList.add(reportData);

            }

            ListAdapter adapter = new SimpleAdapter(
                    Report.this, dataList, R.layout.content_report,
                    new String[]{BehaviourHandler.TAG_BCID, /*BehaviourHandler.TAG_NAME,BehaviourHandler.TAG_CNAME,*/
                            BehaviourHandler.TAG_BNAME,BehaviourHandler.TAG_SEVER/*,BehaviourHandler.TAG_UPDATED_AT*/},
                    new int[]{R.id.bc_id, /*R.id.name, R.id.cname,*/ R.id.bName, R.id.severity/*, R.id.updated_at*/}
            );
            list.setAdapter(adapter);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void changeActivity(Class activity) {
        finish();
        Intent intent = new Intent(this, activity);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        changeActivity(Dashboard.class);
    }
}
