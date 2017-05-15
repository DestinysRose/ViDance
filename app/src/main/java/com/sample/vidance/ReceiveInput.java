package com.sample.vidance;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.sample.vidance.app.AppConfig;
import com.sample.vidance.app.AppController;
import com.sample.vidance.helper.SQLiteHandler;
import com.sample.vidance.helper.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Michelle on 30/3/2017.
 */

public class ReceiveInput extends AppCompatActivity {
    private ArrayList<String> s3 = new ArrayList<String>();
    private ArrayList<String> s4 = new ArrayList<String>();
    private SQLiteHandler db;
    private static final String TAG = ReceiveInput.class.getSimpleName();
    private SessionManager session;
    private ProgressDialog pDialog;
    private String s1, s2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.confirmation);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // Session manager
        session = new SessionManager(getApplicationContext());

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        Intent mIntent = getIntent();
        TextView mTextMessage = (TextView) findViewById(R.id.input_results);
        String result = mIntent.getStringExtra("RESULT");
        mTextMessage.setText(result);
        TextView title = (TextView) findViewById(R.id.title);
        String text = mIntent.getStringExtra("TITLE") + " for " + db.getChild() + "\n";
        title.setText(text);
        Button btnSend = (Button) findViewById(R.id.btnSend);
        Button btnCancel = (Button) findViewById(R.id.btnCancel);


        s1 = mIntent.getStringExtra("STARTTIME");
        s2 = mIntent.getStringExtra("ENDTIME");
        s3 = mIntent.getStringArrayListExtra("BEHAVIOURS");
        s4 = mIntent.getStringArrayListExtra("SEVERITY");

        Toast.makeText(getApplicationContext(),s1 + "|" + s2, Toast.LENGTH_LONG);

        // Set font
        String fontPath = "fonts/CatCafe.ttf";
        Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);
        title.setTypeface(tf);
        title.setTextSize(30);
        fontPath = "fonts/James_Fajardo.ttf";
        Typeface jf = Typeface.createFromAsset(getAssets(), fontPath);
        btnSend.setTypeface(jf);
        btnCancel.setTypeface(jf);

        // Send to Database

        btnSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                updateRecords(db.getChildID(), s1, s2, s3, s4);
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                alert(); //Prompt user for confirmation
            }
        });
    }

    public void alert() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ReceiveInput.this);
        alertDialogBuilder.setTitle("Cancel?")
                .setMessage("Are you sure you want to cancel your submission and go back?")
                .setCancelable(false)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Return to update behaviours
                        finish();
                        Intent intent = new Intent(ReceiveInput.this, Update.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Do Nothing
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        alertDialog.getButton(alertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#E77F7E"));
        alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#BFFFC2"));
    }

    @SuppressWarnings("deprecation")
    private void updateRecords(final String childid ,final String start, final String end,  final ArrayList bhv, final ArrayList svt) {
        // Tag used to cancel the request
        String tag_string_req = "req_child";

        pDialog.setMessage("Registering ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST, AppConfig.URL_TEST, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response);
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        // Get server response
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();

                        finish();
                        Intent i = new Intent(ReceiveInput.this, Dashboard.class);
                        startActivity(i);

                    } else {
                        // Error occurred in registration. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Registration Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("childid", childid);
                params.put("start", start);
                params.put("end", end);
                int i=0;
                for(Object object: bhv){
                    params.put("behaviour["+(i++)+"]", object.toString());
                }
                int x=0;
                for(Object object: svt){
                    String str = object.toString();
                    params.put("severity["+(x++)+"]", str.substring(2));
                }
                return params;
            }
        };
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    @Override
    public void onBackPressed() {
        alert();
    }
}
