package com.sample.vidance;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ViewFlipper;

/**
 * Created by Michelle on 22/3/2017.
 */

public class Features extends AppCompatActivity {

    private TextView mTextMessage;
    private ViewFlipper vf;
    private TextView tv;
    private View toggle;
    private View toggle2;
    private String value;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    Intent intent = new Intent(Features.this, Dashboard.class);
                    startActivity(intent);
                    return true;
                case R.id.navigation_record:
                    intent = new Intent(Features.this, Record.class); //Record page
                    startActivity(intent);
                    return true;
                case R.id.navigation_input:
                    mTextMessage.setText(R.string.title_input);
                    vf.setVisibility(View.VISIBLE); //ONLY USE visible or invisible when you want to hide children by default
                    vf.setDisplayedChild(0); //OTHERWISE, use this to direct [according to index] which child is loaded //Can use this to pass values
                    return true;
                case R.id.navigation_target:
                    mTextMessage.setText(R.string.title_target);
                    vf.setDisplayedChild(1);
                    return true;
                case R.id.navigation_report:
                    mTextMessage.setText(R.string.title_report);
                    vf.setVisibility(View.INVISIBLE);
                    vf.setDisplayedChild(2);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_features);
        getSupportActionBar().setTitle("ViDance");
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        //Receive input from Dashboard and highlights menu appropriately
        Intent mIntent = getIntent();
        //Receive Values to select icon to highlight
        int intValue = mIntent.getIntExtra("SELECTED_ITEM", 0);
        navigation.getMenu().getItem(intValue).setChecked(true);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        //Load selected content XML
        int vfValue = mIntent.getIntExtra("SELECTED_CONTENT", 0);
        vf = (ViewFlipper) findViewById(R.id.vf);
        vf.setDisplayedChild(vfValue);
        if (intValue == 2) { //Update Behaviours page
            toggleSeverity();
            addQuestions();
            delQuestions();
            updateBehaviour();
        }
        //Receive input and update content appropriately
        mTextMessage = (TextView) findViewById(R.id.message);
        String message = getIntent().getStringExtra("SELECTED_ACTIVITY");
        mTextMessage.setText(message);
        //Set Font Cat Cafe
        String fontPath = "fonts/CatCafe.ttf";
        Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);
        mTextMessage.setTypeface(tf);
        //Set Font James Farjardo
        fontPath = "fonts/James_Fajardo.ttf";
        tf = Typeface.createFromAsset(getAssets(), fontPath);
        Button mText = (Button) findViewById(R.id.toggleView);
        mText.setTypeface(tf);
        mText = (Button) findViewById(R.id.addBehaviour);
        mText.setTypeface(tf);
        mText = (Button) findViewById(R.id.delBehaviour);
        mText.setTypeface(tf);
        mText = (Button) findViewById(R.id.updateBehaviour);
        mText.setTypeface(tf);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(Features.this, Features.class);
        switch (item.getItemId()) {
            case R.id.action_notifications:
                intent.putExtra("SELECTED_ITEM", 0);
                intent.putExtra("SELECTED_ACTIVITY", "Notifications");
                startActivity(intent);
                break;
            case R.id.action_settings:
                intent.putExtra("SELECTED_ITEM", 0);
                intent.putExtra("SELECTED_ACTIVITY", "Settings");
                startActivity(intent);
                break;
            case R.id.action_contact:
                intent.putExtra("SELECTED_ITEM", 0);
                intent.putExtra("SELECTED_ACTIVITY", "Contact");
                startActivity(intent);
                break;
            case R.id.action_about:
                intent.putExtra("SELECTED_ITEM", 0);
                intent.putExtra("SELECTED_ACTIVITY", "About");
                startActivity(intent);
                break;
            case R.id.action_help:
                intent.putExtra("SELECTED_ITEM", 0);
                intent.putExtra("SELECTED_ACTIVITY", "Help");
                startActivity(intent);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }


    /** Functions for Update Behaviours **/
    public void toggleSeverity() {
        toggle = findViewById(R.id.severity_info);
        toggle.setVisibility(View.GONE);
        toggle2 = findViewById(R.id.questions);
        toggle2.setVisibility(View.VISIBLE);
        Button btnToggle = (Button) findViewById(R.id.toggleView);
        btnToggle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                toggle = findViewById(R.id.severity_info);
                toggle2 = findViewById(R.id.questions);
                Button btnToggle = (Button) findViewById(R.id.toggleView);
                if (toggle.getVisibility() == View.GONE) {
                    toggle.setVisibility(View.VISIBLE);
                    toggle2.setVisibility(View.GONE);
                    btnToggle.setText(R.string.hide_severity);
                } else {
                    toggle.setVisibility(View.GONE);
                    toggle2.setVisibility(View.VISIBLE);
                    btnToggle.setText(R.string.show_severity);
                }
            }
        });
    }

    public void addQuestions() {
        Button btnClick = (Button) findViewById(R.id.addBehaviour);
        tv = (TextView) findViewById(R.id.limitReached);
        tv.setVisibility(View.INVISIBLE);
        btnClick.setVisibility(View.VISIBLE);
        //Hide all questions on default
        for (int i = 2; i <= 20; i++) {
            String string1 = "question" + String.valueOf(i);
            tv.setVisibility(View.INVISIBLE);
            int resID = getResources().getIdentifier(string1, "id", getPackageName());
            View v = (View) findViewById(resID);
            v.setVisibility(View.GONE);
        }
        //Add question on click
        btnClick.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                for (int i = 2; i <= 20; i++) {
                    String string1 = "question" + String.valueOf(i);
                    int resID = getResources().getIdentifier(string1, "id", getPackageName());
                    v = findViewById(resID);
                    if (v.getVisibility() == View.GONE) {
                        v.setVisibility(View.VISIBLE);
                        break;
                    }
                    else if (i == 20 && v.getVisibility() == View.VISIBLE) { //When maximum questions is reached
                        Button btnHide = (Button) findViewById(R.id.addBehaviour);
                        btnHide.setVisibility(View.GONE);
                        tv.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    public void delQuestions() {
        Button btnClick = (Button) findViewById(R.id.delBehaviour);
        //Hide all questions on default
        btnClick.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                for (int i = 20; i >= 2; i--) {
                    String string1 = "question" + String.valueOf(i);
                    int resID = getResources().getIdentifier(string1, "id", getPackageName());
                    v = findViewById(resID);
                    if (i == 20 && v.getVisibility() == View.VISIBLE)
                    {
                        //In-case max was reached then deleted
                        Button btnAdd = (Button) findViewById(R.id.addBehaviour);
                        btnAdd.setVisibility(View.VISIBLE);
                        tv.setVisibility(View.INVISIBLE);
                    }
                    else if (v.getVisibility() == View.VISIBLE) {
                        v.setVisibility(View.GONE);
                        break;
                    }
                }
            }
        });
    }
    public void updateBehaviour() {

        Button btnClick = (Button) findViewById(R.id.updateBehaviour);
        //Hide all questions on default
        btnClick.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                value = null;
                Spinner spinner1 = (Spinner) findViewById(R.id.spinner1);
                for (int i = 1; i <= 20; i++) {
                    String string1 = "question" + String.valueOf(i);
                    int resID = getResources().getIdentifier(string1, "id", getPackageName());
                    v = findViewById(resID);
                    if (v.getVisibility() == View.VISIBLE) {
                        RadioGroup rg = (RadioGroup) findViewById(R.id.radioGroup);
                        String rb;
                        int checked = rg.getCheckedRadioButtonId();
                        switch(checked){
                            case R.id.radioButton1:
                                rb = "Mild";
                                break;
                            case R.id.radioButton2:
                                rb = "Moderate";
                                break;
                            case R.id.radioButton3:
                                rb = "Severe";
                                break;
                            default:
                                rb = "NULL";
                                break;
                        }
                        if (value == "" || value == null) {
                            value = "Behaviour " + String.valueOf(i) + " : " + spinner1.getSelectedItem().toString() + "\n Severity: " + rb;
                        }
                        else
                        {
                            value = value + "\n\nBehaviour " + String.valueOf(i) + " : " + spinner1.getSelectedItem().toString() + "\n Severity: " + rb;
                        }
                    }
                }
                Intent intent = new Intent(Features.this, ReceiveInput.class);
                intent.putExtra("RESULT", value);
                startActivity(intent);
            }
        });
    }
}