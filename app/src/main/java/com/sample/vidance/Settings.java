package com.sample.vidance;

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.sample.vidance.app.AppConfig;
import com.sample.vidance.app.AppController;
import com.sample.vidance.helper.SQLiteHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Michelle on 9/5/2017.
 */

public class Settings extends AppCompatActivity {
    private TextView child;
    private SQLiteHandler db;
    private static final String TAG = Child.class.getSimpleName();
    private ProgressDialog pDialog;
    private List<String> childrenList;
    private LinkedHashMap<String, String> IDchecker;
    private ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        //Initialize array
        childrenList = new ArrayList<String>();
        IDchecker = new LinkedHashMap<>();

        getChild(db.getUserID());
        
        Button cancel = (Button) findViewById(R.id.btnCancel);
        Button addChild = (Button) findViewById(R.id.btnAdd);

        cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                changeActivity(Dashboard.class);
            }
        });
        addChild.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                changeActivity(Child.class);
            }
        });

        child = (TextView) findViewById(R.id.child);

        if (db.getChild().equals(" ") && childrenList.size() > 0) {
            child.setText(childrenList.get(0));
        } else if (!db.getChild().equals(" ")) {
            child.setText(db.getChild());
        }

        child.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                for (Map.Entry<String, String> entry : IDchecker.entrySet()) {
                    childrenList.add(entry.getValue());
                }
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(Settings.this);
                LayoutInflater inflater = getLayoutInflater();
                View convertView = (View) inflater.inflate(R.layout.resource_list, null);
                alertDialog.setView(convertView);
                alertDialog.setTitle("List");
                lv = (ListView) convertView.findViewById(R.id.list);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(Settings.this,android.R.layout.simple_list_item_1, childrenList);
                lv.setAdapter(adapter);
                lv.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        db.setChild(lv.getItemAtPosition(position).toString());
                        changeActivity(Settings.class);
                    }
                });
                alertDialog.show();
            }
        });

    }

    /**
     * Function to get child from server using User ID
     */
    @SuppressWarnings("deprecation")
    private void getChild(final  String userid) {
        // Tag used to cancel the request
        String tag_string_req = "req_child";

        pDialog.setMessage("Checking for registered children ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST, AppConfig.URL_GETCHILD, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Check Child Response: " + response);
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    boolean empty = jObj.getBoolean("empty");
                    List<String> childList = new ArrayList<String>();
                    if (!error) {
                        if (!empty){
                            JSONArray childinfo = jObj.getJSONArray("child");

                            for (int i = 0; i <= childinfo.length(); ++i) {
                                JSONObject idNname = childinfo.getJSONObject(i);
                                String childid = idNname.getString("child_id");
                                String fullname = idNname.getString("fullname");
                                IDchecker.put(childid, fullname);
                            }
                        } else {
                            finish();
                            Intent intent = new Intent(Settings.this, Child.class);
                            startActivity(intent);
                        }
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
