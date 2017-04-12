package com.sample.vidance;


import android.app.ProgressDialog;
import android.content.Intent;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.sample.vidance.app.AppConfig;
import com.sample.vidance.app.AppController;
import com.sample.vidance.helper.SessionManager;
import com.sample.vidance.helper.SQLiteHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Danil on 27.03.2017.
 */

public class Login extends AppCompatActivity {
    private static final int TIME_LIMIT = 1500;
    private static long backPressed;
    private static final String TAG = Register.class.getSimpleName();
    private Button btnLogin;
    private Button btnLinkToRegister;
    private Button btnButtonToSkip;
    private EditText inputFullName;
    private EditText inputChildName;
    private EditText inputPassword;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputFullName = (EditText) findViewById(R.id.name);
        inputChildName = (EditText) findViewById(R.id.cname);
        inputPassword = (EditText) findViewById(R.id.password);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLinkToRegister = (Button) findViewById(R.id.btnLinkToRegisterScreen);
        btnButtonToSkip = (Button) findViewById(R.id.btnButtonToSkip);

        // Change font for title
        String fontPath = "fonts/CatCafe.ttf";
        TextView txtCat = (TextView) findViewById(R.id.catcafe);
        Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);
        txtCat.setTypeface(tf);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // Session manager
        session = new SessionManager(getApplicationContext());

        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(Login.this, Dashboard.class);
            startActivity(intent);
            finish();
        }

        // Login button Click Event
        btnLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                String name = inputFullName.getText().toString().trim();
                String cname = inputChildName.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                if (!name.isEmpty() && !cname.isEmpty() && !password.isEmpty()) {
                    checkLogin(name, cname, password);
                } else {
                    Toast.makeText(getApplicationContext(), "Please enter your details!", Toast.LENGTH_LONG).show();
                }

            }

        });

        // Link to Register Screen
        btnLinkToRegister.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                finish();
                Intent i = new Intent(getApplicationContext(), Register.class);
                startActivity(i);
            }
        });

        btnButtonToSkip.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                finish();
                Intent i = new Intent(getApplicationContext(), Dashboard.class);
                startActivity(i);
            }
        });

    }

    /**
     * function to verify login details in mysql db
     * */
    private void checkLogin(final String name, final String cname, final String password) {
        // Tag used to cancel the request
        String tag_string_req = "req_login";

        pDialog.setMessage("Logging in ...");
        showDialog();

        StringRequest strReq = new StringRequest(Method.POST, AppConfig.URL_LOGIN, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {
                        // user successfully logged in
                        // Create login session
                        session.setLogin(true);

                        // Now store the user in SQLite
                        String uid = jObj.getString("uid");

                        JSONObject user = jObj.getJSONObject("user");
                        String name = user.getString("name");
                        String cname = user.getString("cname");
                        String created_at = user.getString("created_at");

                        // Inserting row in users table
                        db.addUser(name, cname, uid, created_at);

                        // Launch main activity
                        finish();
                        Intent intent = new Intent(Login.this, Dashboard.class);
                        startActivity(intent);
                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("name", name);
                params.put("cname", cname);
                params.put("password", password);

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
        if(TIME_LIMIT + backPressed > System.currentTimeMillis()) {
            super.onBackPressed(); //Close application on double back
        }
        else
        {
            Toast.makeText(getApplicationContext(), "Press the 'Back' button again to exit.", Toast.LENGTH_SHORT).show();
        }
        backPressed = System.currentTimeMillis();
    }
}
