package com.sample.vidance;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Michelle on 9/5/2017.
 */

public class Child extends AppCompatActivity {
    private static final String TAG = Child.class.getSimpleName();
    private SQLiteHandler db;
    private SessionManager session;
    private TextView child, cAge;
    private Button btnReg, btnCancel;
    private RadioButton rb1, rb2;
    private RadioGroup rg;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child);

        child = (TextView) findViewById(R.id.child);
        cAge = (TextView) findViewById(R.id.age);
        TextView txtgender = (TextView) findViewById(R.id.textgender);
        TextView title = (TextView) findViewById(R.id.catcafe);
        btnReg = (Button) findViewById(R.id.btnRegister);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        rg = (RadioGroup) findViewById(R.id.gender);
        rb1 = (RadioButton) findViewById(R.id.male);
        rb2 = (RadioButton) findViewById(R.id.female);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // Session manager
        session = new SessionManager(getApplicationContext());

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        //Set Font Cat Cafe
        String fontPath = "fonts/CatCafe.ttf";
        Typeface cc = Typeface.createFromAsset(getAssets(), fontPath);
        rb1.setTypeface(cc);
        rb1.setTextColor(Color.parseColor("#6B5D40"));
        rb2.setTypeface(cc);
        rb2.setTextColor(Color.parseColor("#6B5D40"));
        title.setTypeface(cc);
        //Set Font James Farjardo
        fontPath = "fonts/James_Fajardo.ttf";
        Typeface jf = Typeface.createFromAsset(getAssets(), fontPath);
        txtgender.setTypeface(jf);
        btnReg.setTypeface(jf);
        btnCancel.setTypeface(jf);

        btnReg.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                int checked = rg.getCheckedRadioButtonId();
                RadioButton rb = (RadioButton) findViewById(checked);

                String name = child.getText().toString();
                String age = cAge.getText().toString();
                String gender = rb.getText().toString();
                String uid = db.getUserID();
                registerChild(uid, name, age, gender);
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                changeActivity(Dashboard.class);
            }
        });
    }

    /**
     * Function to store child in MySQL database will post params(tag, name, age, gender) to register url
     */
    @SuppressWarnings("deprecation")
    private void registerChild(final  String userid, final String fullname, final String age, final String gender) {
        // Tag used to cancel the request
        String tag_string_req = "req_child";

        pDialog.setMessage("Registering ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST, AppConfig.URL_REGCHILD, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response);
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        // User successfully stored in MySQL
                        // Now store the user in sqlite

                        JSONObject child = jObj.getJSONObject("child");
                        String fullname = child.getString("fullname");
                        String childid = child.getString("child_id");
                        // Insert   ing row in users table
                        db.setChildID(childid);
                        db.setChild(fullname);
                        Toast.makeText(getApplicationContext(), "Child registered!", Toast.LENGTH_LONG).show();

                        finish();
                        Intent i = new Intent(Child.this, Settings.class);
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
                params.put("userid", userid);
                params.put("fullname", fullname);
                params.put("age", age);
                params.put("gender", gender);

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
