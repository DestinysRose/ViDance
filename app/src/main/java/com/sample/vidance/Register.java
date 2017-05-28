package com.sample.vidance;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
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
 * Created by Danil on 27.03.2017.
 * Modified by Michelle on 08.03.2017.
 */

public class Register extends Activity {
    private static final String TAG = Register.class.getSimpleName();
    private Button btnRegister;
    private Button btnLinkToLogin;
    private EditText inputUserName, inputFullName, inputPassword, inputCfmPassword, inputEmail;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;
    private String username, fullname, password, cfmpassword, email;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        inputUserName = (EditText) findViewById(R.id.username);
        inputFullName = (EditText) findViewById(R.id.fullname);
        inputPassword = (EditText) findViewById(R.id.password);
        inputCfmPassword = (EditText) findViewById(R.id.cfmpassword);
        inputEmail = (EditText) findViewById(R.id.email);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnLinkToLogin = (Button) findViewById(R.id.btnLinkToLoginScreen);

        // Reset Image to prevent visual bugs
        ImageView iv = (ImageView) findViewById(R.id.title2);
        iv.setImageResource(R.drawable.ic_vidance);

        // Change font for title
        String fontPath = "fonts/CatCafe.ttf";
        TextView txtCat = (TextView) findViewById(R.id.catcafe);
        Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);
        txtCat.setTypeface(tf);
        btnLinkToLogin.setTypeface(tf);
        // Change font for title
        String fontPath2 = "fonts/James_Fajardo.ttf";
        Typeface jf = Typeface.createFromAsset(getAssets(), fontPath2);
        btnRegister.setTypeface(jf);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // Session manager
        session = new SessionManager(getApplicationContext());

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            changeActivity(Dashboard.class);
        }

        // Register Button Click event
        btnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                username = inputUserName.getText().toString().trim();
                fullname = inputFullName.getText().toString().trim();
                password = inputPassword.getText().toString().trim();
                cfmpassword = inputCfmPassword.getText().toString().trim();
                email = inputEmail.getText().toString().trim();

                if (!username.isEmpty() && !fullname.isEmpty() && !password.isEmpty() && !cfmpassword.isEmpty() && !email.isEmpty()) {
                    if(username.length() >= 4 && username.length() <= 16) {
                        if(password.length() >= 6 && password.length() <= 20) {
                            if(password.equals(cfmpassword)) {
                                if(email.contains("@") && email.contains(".")) {
                                    registerUser(username, fullname, password, email);
                                }
                                else {
                                    Toast.makeText(getApplication(), "Please enter a valid e-mail address", Toast.LENGTH_LONG).show();
                                }
                            }
                            else {
                                Toast.makeText(getApplication(), "Passwords do not match", Toast.LENGTH_LONG).show();
                            }

                        }
                        else {
                            Toast.makeText(getApplication(), "Please enter a valid password between 6 and 20 characters long", Toast.LENGTH_LONG).show();
                        }
                    }
                    else {
                        Toast.makeText(getApplication(), "Please enter a valid username between 4 and 16 characters long", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please fill in all information!", Toast.LENGTH_LONG).show();
                }
            }
        });

        // Link to Login Screen
        btnLinkToLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                changeActivity(Login.class);
            }
        });

    }

    /**
     * Function to store user in MySQL database will post params(tag, name,
     * email, password) to register url
     */
    @SuppressWarnings("deprecation")
    private void registerUser(final String username, final String fullname, final String password, final String email) {
    // Tag used to cancel the request
        String tag_string_req = "req_register";

        pDialog.setMessage("Registering ...");
        showDialog();

        StringRequest strReq = new StringRequest(Method.POST, AppConfig.URL_REGUSER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response);
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        Toast.makeText(getApplicationContext(), "User successfully registered. Try login now!", Toast.LENGTH_LONG).show();

                        // Launch login activity
                        changeActivity(Login.class);
                    } else {
                        // Error occurred in registration. Get the error
                        // message
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
                params.put("username", username);
                params.put("fullname", fullname);
                params.put("password", password);
                params.put("cfmpassword", cfmpassword);
                params.put("email", email);

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
        changeActivity(Login.class);
    }
}