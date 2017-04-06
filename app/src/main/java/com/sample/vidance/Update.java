package com.sample.vidance;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Created by Michelle on 31/3/2017.
 */

public class Update extends AppCompatActivity {
    private TextView mTextMessage;
    private TextView tv;
    private View toggle;
    private View toggle2;
    private String value;
    private String arraySeverity[];
    private String arrayBehaviour[];
    private String missing;
    private Boolean validation;
    private Typeface jf;
    private Typeface cc;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        //Navigation Bar set up
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.getMenu().getItem(2).setChecked(true);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        //Receive input and update content appropriately
        mTextMessage = (TextView) findViewById(R.id.message);
        mTextMessage.setText(R.string.title_input);
        //Set Font Cat Cafe
        String fontPath = "fonts/CatCafe.ttf";
        cc = Typeface.createFromAsset(getAssets(), fontPath);
        mTextMessage.setTypeface(cc);
        //Set Font James Farjardo
        fontPath = "fonts/James_Fajardo.ttf";
        jf = Typeface.createFromAsset(getAssets(), fontPath);
        Button mText = (Button) findViewById(R.id.toggleView);
        mText.setTypeface(jf);
        mText = (Button) findViewById(R.id.addBehaviour);
        mText.setTypeface(jf);
        mText = (Button) findViewById(R.id.delBehaviour);
        mText.setTypeface(jf);
        mText = (Button) findViewById(R.id.updateBehaviour);
        mText.setTypeface(jf);
        //Run functions for the form
        createQuestions();
        toggleSeverity();
        addQuestions();
        delQuestions();
        updateBehaviour();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Intent intent = new Intent(Update.this, Features.class);
            switch (item.getItemId()) {
                case R.id.navigation_dashboard:
                    finish();
                    return true;
                case R.id.navigation_record:
                    finish();
                    intent = new Intent(Update.this, Record.class);
                    intent.putExtra("SELECTED_ITEM", 0);
                    intent.putExtra("SELECTED_ACTIVITY", "Notifications");
                    startActivity(intent);
                    return true;
                case R.id.navigation_input:
                    //Do Nothing
                    return true;
                case R.id.navigation_target:
                    finish();
                    intent.putExtra("SELECTED_ITEM", 3);
                    intent.putExtra("SELECTED_ACTIVITY", "Target Behaviours");
                    intent.putExtra("SELECTED_CONTENT", 1);
                    startActivity(intent);
                    return true;
                case R.id.navigation_report:
                    finish();
                    intent.putExtra("SELECTED_ITEM", 4);
                    intent.putExtra("SELECTED_ACTIVITY", "Generate Reports");
                    intent.putExtra("SELECTED_CONTENT", 2);
                    startActivity(intent);
                    return true;
            }
            return false;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(Update.this, Features.class);
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

    /** Dynamically create questions **/
    public void createQuestions() {
        arraySeverity = getResources().getStringArray(R.array.severity_arrays);
        arrayBehaviour = getResources().getStringArray(R.array.behaviour_arrays);


        LinearLayout mLinearLayout = (LinearLayout) findViewById(R.id.questions);


        for (int k = 1; k <= 20; k++) {

            // Set default fonts
            String fontPath = "fonts/CatCafe.ttf";
            cc = Typeface.createFromAsset(getAssets(), fontPath);
            fontPath = "fonts/James_Fajardo.ttf";
            jf = Typeface.createFromAsset(getAssets(), fontPath);

            // Create child view to store questions
            LinearLayout childLayout = new LinearLayout(this);
            childLayout.setOrientation(LinearLayout.VERTICAL);
            String layoutID = "question" + String.valueOf(k);
            int qID = getResources().getIdentifier(layoutID, "id", getPackageName());
            childLayout.setId(qID);

            // Create Text View
            TextView title = new TextView(this);
            title.setText("Behaviour " + k + ":");
            String bhv = "bhv" + String.valueOf(k);
            int tvID = getResources().getIdentifier(bhv, "id", getPackageName());
            title.setId(tvID);
            title.setTypeface(jf);
            title.setTextSize(32);
            title.setTextColor(Color.parseColor("#504530"));
            title.setPadding(10,0,0,0);
            childLayout.addView(title);

            // Create Spinner
            Spinner spinner = new Spinner(this, Spinner.MODE_DIALOG);
            String bhvList = "bhvList" + String.valueOf(k);
            int spinID = getResources().getIdentifier(bhvList, "id", getPackageName());
            spinner.setId(spinID);
            String[] bhvs = (arrayBehaviour); // Load array from arrays.xml
            ArrayAdapter adapter= new ArrayAdapter(this, android.R.layout.select_dialog_item, bhvs);
            spinner.setAdapter(adapter); // Set values

            // Display 'Select a shown behaviour' on spinner by default and make sure its unable to be selected
            final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, bhvs){
                @Override
                public boolean isEnabled(int position){
                    if(position == 0)
                    {
                        return false; // Disable the first item from Spinner to be used for hint
                    }
                    else
                    {
                        return true;
                    }
                }

                @Override // Shows the hint as grey when created
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getDropDownView(position, convertView, parent);
                    TextView tv = (TextView) view;
                    if(position == 0){
                        tv.setTextColor(Color.GRAY); // Set the hint text color gray
                    }
                    else {
                        tv.setTextColor(Color.parseColor("#23C8B2")); // Set default font color
                        tv.setTypeface(cc);
                        tv.setTextSize(20);
                    }
                    return view;
                }

                @Override //Pop-up shows hint as a title
                public View getDropDownView(int position, View convertView, ViewGroup parent) {
                    View view = super.getDropDownView(position, convertView, parent);
                    TextView tv = (TextView) view;
                    if(position == 0){
                        tv.setTextColor(Color.parseColor("#F49E9D")); // Set the hint text color
                        tv.setTypeface(jf);
                        tv.setTextSize(32);
                    }
                    else {
                        tv.setTextColor(Color.BLACK);
                        tv.setTypeface(cc);
                        tv.setTextSize(16);
                    }
                    return view;
                }
            };
            spinner.setAdapter(spinnerArrayAdapter); // Set hint
            childLayout.addView(spinner);

            //Create Radio Group and buttons within child view
            final RadioButton[] rb = new RadioButton[3];
            final RadioGroup rg = new RadioGroup(this);
            rg.setOrientation(RadioGroup.HORIZONTAL);
            String string2 = "radioGroup" + String.valueOf(k);
            int rgID = getResources().getIdentifier(string2, "id", getPackageName());
            rg.setId(rgID);
            for (int i = 0; i < 3; i++) {
                rb[i] = new RadioButton(this);
                rg.addView(rb[i]);
                rb[i].setText(arraySeverity[i]); // Add array items into radio buttons
                rb[i].setTypeface(cc);
                rb[i].setTextSize(18);
                rb[i].setTextColor(Color.parseColor("#6B5D40"));
            }
            childLayout.addView(rg);
            mLinearLayout.addView(childLayout);
        }
    }

    /** Toggle Severity Information **/
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

    /** Automatically hides all behaviour options other than1, then displays them on button press **/
    public void addQuestions() {
        Button btnClick = (Button) findViewById(R.id.addBehaviour);
        tv = (TextView) findViewById(R.id.limitReached);
        tv.setVisibility(View.INVISIBLE);
        btnClick.setVisibility(View.VISIBLE);
        //Hide all questions on default
        for (int i = 20; i >= 2; i--) {
            String string1 = "question" + String.valueOf(i);
            int resID = getResources().getIdentifier(string1, "id", getPackageName());
            View bhv = findViewById(resID);
            bhv.setVisibility(View.GONE);
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
                for (int i = 1; i <= 20; i++) {
                    String string1 = "question" + String.valueOf(i);
                    int resID = getResources().getIdentifier(string1, "id", getPackageName());
                    v = findViewById(resID);
                    String string2 = "radioGroup" + String.valueOf(i);
                    int rgID = getResources().getIdentifier(string2, "id", getPackageName());
                    RadioGroup rg = (RadioGroup) v.findViewById(rgID);

                    if (v.getVisibility() == View.VISIBLE) {
                        // Get selected radio button value
                        int checked = rg.getCheckedRadioButtonId();
                        RadioButton rb = (RadioButton) findViewById(checked);

                        // Get selected spinner item
                        String bhvList = "bhvList" + String.valueOf(i);
                        int spinID = getResources().getIdentifier(bhvList, "id", getPackageName());
                        Spinner spinner = (Spinner) findViewById(spinID);
                        String bhv = spinner.getSelectedItem().toString();

                        /** Validate information **/
                        if(spinner.getSelectedItemPosition() == 0) { // If spinner is not selected
                            missing = "Behaviour " + String.valueOf(i) + " not selected.";
                            validation = false;
                            break;
                        }
                        else if (rg.getCheckedRadioButtonId() == -1) { // If no radio button is checked
                            missing = "Severity of behaviour " + String.valueOf(i) + ", \"" + bhv + "\" not selected.";
                            validation = false;
                            break;
                        }
                        else {
                            if (value == "" || value == null) {
                                value = "Behaviour " + String.valueOf(i) + " : " + bhv + "\nSeverity: " + rb.getText();
                            } else {
                                value = value + "\n\nBehaviour " + String.valueOf(i) + " : " + bhv + "\nSeverity: " + rb.getText();
                            }
                            validation = true;
                        }
                    }
                    else
                    {
                        break;
                    }
                }
                if (validation == true) {
                    finish();
                    Intent intent = new Intent(Update.this, ReceiveInput.class);
                    intent.putExtra("TITLE", "Update Behaviours");
                    intent.putExtra("RESULT", value);
                    startActivity(intent);
                }
                else {
                    Toast.makeText(getApplicationContext(), missing, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        //Prompt user to send video
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Update.this);
        //Set title
        alertDialogBuilder.setTitle("Cancel?");
        //Set dialog message
        alertDialogBuilder
                .setMessage("Are you sure you cancel your submission and go back?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, close
                        // current activity
                        finish();
                        Intent intent = new Intent(Update.this, Dashboard.class);
                        startActivity(intent);
                        dialog.cancel();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });
        //Create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        //Show it
        alertDialog.show();
    }

}