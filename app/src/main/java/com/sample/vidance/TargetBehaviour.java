package com.sample.vidance;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.YAxis;
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

//Implementation of target behaviour activity to show user weekly targets and current
public class TargetBehaviour  extends AppCompatActivity {

    public static final String TARGET_WEEK = "http://thevidance.com/targetBehaviour.php";

    //JSON Array
    private JSONArray result;

    private SQLiteHandler db;
    private SessionManager session;

    private TextView mTextMessage;
    TextView selectedBehaviour, byBehaviour;

    Typeface tf;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_target);

        String fontPath = "fonts/CatCafe.ttf";
        TextView txtCat = (TextView) findViewById(R.id.catcafe);

        // Loading Font Face
        tf = Typeface.createFromAsset(getAssets(), fontPath);

        // Applying font
        txtCat.setTypeface(tf);
        selectedBehaviour = (TextView)findViewById(R.id.selectedB);
        byBehaviour = (TextView)findViewById(R.id.bName);
        selectedBehaviour.setTypeface(tf);
        byBehaviour.setTypeface(tf);

        mTextMessage = (TextView) findViewById(R.id.message);
        mTextMessage.setText(R.string.title_target);
        mTextMessage.setTypeface(tf);

        try {
            storeDateHourly();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Button hint = (Button) findViewById(R.id.hint);
        hint.setTypeface(tf);
        hint.setTextSize(25);
        hint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMessage();
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

        final LineChart lineChart = (LineChart) findViewById(R.id.chart);

        final ArrayList<Entry> current = new ArrayList<>();
        final ArrayList<Entry> target = new ArrayList<>();
        final ArrayList<String> labels = new ArrayList<>();

        final ArrayList<ILineDataSet> lineDataSets = new ArrayList<>();

            StringRequest stringRequest = new StringRequest(Request.Method.POST, TARGET_WEEK,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            JSONObject reader = null;
                            try {
                                reader = new JSONObject(response);

                                result = reader.getJSONArray("result");
                                if (result.length() == 0)
                                    Toast.makeText(TargetBehaviour.this, "Sorry, no records this day(s)", Toast.LENGTH_LONG).show();
                                else {

                                    int count;
                                    for (int i = 0; i < result.length(); i++) {
                                        final JSONObject targetObj = result.getJSONObject(i);
                                        final String severCounter = targetObj.getString("counter");
                                        count = Integer.parseInt(severCounter);

                                        current.add(new Entry(count, i));
                                        if(count >= 14)
                                            target.add(new Entry(count-3,i));
                                        else if(count >=10)
                                            target.add(new Entry(count-2,i));
                                        else if(count > 1)
                                            target.add(new Entry(count-1,i));
                                        else if(count == 1)
                                            target.add(new Entry(count,i));
                                        else
                                            Toast.makeText(TargetBehaviour.this, "Seems that you don't have any records last week...", Toast.LENGTH_LONG).show();

                                        labels.add("");
                                    }

                                    String[] xaxes = new String[labels.size()];

                                    for(int i = 0; i<labels.size(); i++){
                                        xaxes[i] = labels.get(i);
                                    }

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
                                    lineChart.setDescription("Comparison number of behaviours from last week with target number");
                                    lineChart.setDescriptionTypeface(tf);
                                    lineChart.setDescriptionPosition(700f, 15f);
                                    lineChart.animateY(1000);
                                    lineChart.getLegend().setEnabled(true);
                                    lineChart.fitScreen();

                                    Legend l = lineChart.getLegend();
                                    l.setTypeface(tf);

                                    YAxis yAxisRight = lineChart.getAxisRight();
                                    yAxisRight.setEnabled(false);

                                    lineChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                                        @Override
                                        public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                                            final int info = h.getXIndex();
                                            try {
                                                JSONObject behaviourList = result.getJSONObject(info);
                                                final String behaviour_name = behaviourList.getString("bName");

                                                selectedBehaviour.setVisibility(View.VISIBLE);
                                                byBehaviour.setVisibility(View.VISIBLE);

                                                selectedBehaviour.setText("Selected: ");
                                                byBehaviour.setText(behaviour_name);
                                            } catch (JSONException es) {
                                                es.printStackTrace();
                                                Toast.makeText(TargetBehaviour.this, "Unexpected error. Please retry", Toast.LENGTH_LONG).show();
                                            }
                                        }

                                        @Override
                                        public void onNothingSelected() {
                                            selectedBehaviour.setVisibility(View.INVISIBLE);
                                            byBehaviour.setVisibility(View.INVISIBLE);
                                        }
                                    });
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(TargetBehaviour.this, "Unexpected error. Please retry", Toast.LENGTH_LONG).show();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(TargetBehaviour.this, "Unexpected error. Please retry", Toast.LENGTH_LONG).show();
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

    private void showMessage(){
        AlertDialog.Builder builder1 = new AlertDialog.Builder(TargetBehaviour.this);
        builder1.setMessage("Instructions\n\n" +
                "Target behaviour shows your results from previous week and compares to target." +
                "Results includes number of severities for each behaviour.\n\n" +
                "Target behaviour calculated to track your results and represent using line chart.\n\n" +
                "To display behaviour simply select any node inside chart.");
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
