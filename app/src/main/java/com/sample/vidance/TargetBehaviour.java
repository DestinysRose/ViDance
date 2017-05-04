package com.sample.vidance;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.sample.vidance.helper.SQLiteHandler;
import com.sample.vidance.helper.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Danil on 10.04.2017.
 */

public class TargetBehaviour  extends AppCompatActivity {

    ArrayList<HashMap<String, String>> dataList;

    public static final String TARGET_DATA = "http://thevidance.com/filter/pieChart.php";

    //JSON Array
    private JSONArray result;

    private SQLiteHandler db;
    private SessionManager session;

    private TextView mTextMessage;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_target);

        String fontPath = "fonts/CatCafe.ttf";
        TextView txtCat = (TextView) findViewById(R.id.catcafe);
        // Loading Font Face
        Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);
        // Applying font
        txtCat.setTypeface(tf);

        mTextMessage = (TextView) findViewById(R.id.message);
        mTextMessage.setText(R.string.title_target);
        mTextMessage.setTypeface(tf);

        //list = (ListView) findViewById(R.id.getReport);
        dataList = new ArrayList<HashMap<String,String>>();

        try {
            storeDateHourly();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Button toReport = (Button) findViewById(R.id.toReport);
        toReport.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(TargetBehaviour.this, Report.class);
                startActivity(intent);
            }
        });
        Button toHome = (Button) findViewById(R.id.toHome);
        toHome.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(TargetBehaviour.this, Dashboard.class);
                startActivity(intent);
            }
        });

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.getMenu().getItem(3).setChecked(true);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(TargetBehaviour.this, Dashboard.class); // Return to Dashboard
        startActivity(intent);
        finish();
    }

    private void storeDateHourly() throws JSONException {
            StringRequest stringRequest = new StringRequest(Request.Method.POST, TARGET_DATA,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            LineChart lineChart = (LineChart) findViewById(R.id.chart);

                            //Toast.makeText(LineChartItem.this,response,Toast.LENGTH_LONG).show();

                            JSONObject reader = null;
                            try {
                                reader = new JSONObject(response);

                                result = reader.getJSONArray("result");
                                if (result.length() == 0)
                                    Toast.makeText(TargetBehaviour.this, "Sorry, no records this day(s)", Toast.LENGTH_LONG).show();
                                else {
                                    //Toast.makeText(LineChartItem.this,"Shit Working!",Toast.LENGTH_LONG).show();

                                    ArrayList<Entry> current = new ArrayList<>();
                                    ArrayList<String> labels = new ArrayList<>();

                                    ArrayList<Entry> target = new ArrayList<>();

                                    int count;



                                    for (int i = 0; i < result.length(); i++) {
                                        final JSONObject weather_object_0 = result.getJSONObject(i);
                                        final String weather_0_description = weather_object_0.getString("counter");
                                        count = Integer.parseInt(weather_0_description);

                                        current.add(new Entry(count, i));
                                        target.add(new Entry(count/2,i));
                                        labels.add("#"+i);
                                    }

                                    String[] xaxes = new String[labels.size()];

                                    for(int i = 0; i<labels.size(); i++){
                                        xaxes[i] = labels.get(i);
                                    }

                                    ArrayList<ILineDataSet> lineDataSets = new ArrayList<>();

                                    LineDataSet lineDataSet1 = new LineDataSet(current, "Current behaviour");
                                    lineDataSet1.setDrawCircles(false);
                                    lineDataSet1.setColor(Color.RED);

                                    LineDataSet lineDataSet2 = new LineDataSet(target, "Target behaviour");
                                    lineDataSet2.setDrawCircles(false);
                                    lineDataSet2.setColor(Color.BLUE);

                                    lineDataSets.add(lineDataSet1);
                                    lineDataSets.add(lineDataSet2);

                                    lineChart.setData(new LineData(xaxes, lineDataSets));

                                    lineChart.setTouchEnabled(true);

                                    CustomMarkerView mv = new CustomMarkerView(TargetBehaviour.this, R.layout.content_marker);
                                    lineChart.setMarkerView(mv);

                                    lineChart.setScaleEnabled(false);
                                    lineChart.setDoubleTapToZoomEnabled(false);
                                    lineChart.setMaxVisibleValueCount(result.length());
                                    lineChart.setDescription("");
                                    lineChart.animateY(1000);
                                    lineChart.getLegend().setEnabled(true);
                                    lineChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                                        @Override
                                        public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                                            final int info = h.getXIndex();
                                            try {
                                                JSONObject behaviourList = result.getJSONObject(info);
                                                final String behaviour_name = behaviourList.getString("bName");

                                                TextView byBehaviour = (TextView)findViewById(R.id.bName);
                                                byBehaviour.setText(behaviour_name);
                                            } catch (JSONException es) {
                                                es.printStackTrace();
                                            }
                                        }

                                        @Override
                                        public void onNothingSelected() {

                                        }
                                    });
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
                            Toast.makeText(TargetBehaviour.this, error.toString(), Toast.LENGTH_LONG).show();
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

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_dashboard:
                    finish();
                    Intent intent = new Intent(TargetBehaviour.this, Dashboard.class); //Record Session page
                    startActivity(intent);
                    return true;
                case R.id.navigation_record:
                    finish();
                    intent = new Intent(TargetBehaviour.this, Record.class);
                    startActivity(intent);
                    return true;
                case R.id.navigation_input:
                    finish();
                    intent = new Intent(TargetBehaviour.this, Update.class);
                    startActivity(intent);
                    return true;
                case R.id.navigation_target:
                    //Do Nothing
                    return true;
                case R.id.navigation_report:
                    finish();
                    intent = new Intent(TargetBehaviour.this, Report.class);
                    /**intent.putExtra("SELECTED_ITEM", 3);
                     intent.putExtra("SELECTED_ACTIVITY", "Target Behaviours");
                     intent.putExtra("SELECTED_CONTENT", 1);**/
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
        Intent intent = new Intent(TargetBehaviour.this, Login.class);
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
        Intent intent = new Intent(TargetBehaviour.this, MenuItems.class);
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
}
