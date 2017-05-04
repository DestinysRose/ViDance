package com.sample.vidance;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
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
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.sample.vidance.app.AppController;
import com.sample.vidance.helper.SQLiteHandler;
import com.sample.vidance.helper.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Michelle on 31/3/2017.
 */

public class Update extends AppCompatActivity implements View.OnClickListener {
    private TextView mTextMessage,  tv;
    private View toggle,  toggle2;
    private String value, missing;
    private String arraySeverity[], arrayBehaviour[], arrayDuration[];
    Button btnDatePicker, btnTimePicker;
    private Boolean validation, timeChange = false;
    private Typeface jf, cc;
    private int mYear, mMonth, mDay, mHour, mMinute, durMinute, durHour;
    private SQLiteHandler db;
    private SessionManager session;
    private ProgressDialog pDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        //Navigation Bar set up
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.getMenu().getItem(2).setChecked(true);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // Session manager
        session = new SessionManager(getApplicationContext());

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

        //Initialise date and time
        Button btnDate = (Button) findViewById(R.id.setDate);
        SimpleDateFormat date = new SimpleDateFormat("dd/MM/yyyy");
        btnDate.setText(date.format(new Date()));
        Button btnTime = (Button) findViewById(R.id.setTime);
        SimpleDateFormat time = new SimpleDateFormat("hh:mm a");
        btnTime.setText(time.format(new Date()));

        //Set onClickListeners for date and time
        btnDatePicker = (Button)findViewById(R.id.setDate);
        btnTimePicker = (Button)findViewById(R.id.setTime);
        btnDatePicker.setOnClickListener(this);
        btnTimePicker.setOnClickListener(this);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        //Run functions for the form
        createQuestions();
        toggleSeverity();
        addQuestions();
        delQuestions();
        updateBehaviour();

        // Set spinner items for Duration
        arrayDuration = getResources().getStringArray(R.array.duration_arrays);
        Spinner duration = (Spinner) findViewById(R.id.duration);
        String[] dur = (arrayDuration); // Load array from arrays.xml
        ArrayAdapter adapter= new ArrayAdapter(this, android.R.layout.select_dialog_item, dur);
        duration.setAdapter(adapter); // Set values

        // Display hint on spinner by default and make sure its unable to be selected
        final ArrayAdapter<String> durArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, dur){
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
                    tv.setTextColor(Color.parseColor("#6B5D40")); // Set default font color
                    tv.setTypeface(null);
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
        duration.setAdapter(durArrayAdapter); // Set hint
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

    /** Deletes Questions on press **/
    public void delQuestions() {
        Button btnClick = (Button) findViewById(R.id.delBehaviour);
        //Hide all questions on default
        btnClick.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                for (int i = 20; i >= 2; i--) {
                    String string1 = "question" + String.valueOf(i);
                    int resID = getResources().getIdentifier(string1, "id", getPackageName());
                    v = findViewById(resID);

                    String string2 = "bhvList" + String.valueOf(i); //  Initialise spinner
                    int spinID = getResources().getIdentifier(string2, "id", getPackageName());
                    Spinner spinner = (Spinner) findViewById(spinID);
                    spinner.setSelection(0);

                    String string3 = "radioGroup" + String.valueOf(i);
                    int rgID = getResources().getIdentifier(string3, "id", getPackageName());
                    RadioGroup rg = (RadioGroup) findViewById(rgID);
                    rg.clearCheck(); // Uncheck all radio buttons in the group

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

    /** Sends values to confirmation page **/
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
                    // Get selected spinner item
                    Spinner duration = (Spinner) findViewById(R.id.duration);
                    String dur = duration.getSelectedItem().toString();

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
                        else if (duration.getSelectedItemPosition() == 0) { // If no radio button is checked
                            missing = "Duration not selected.'";
                            validation = false;
                            break;
                        }
                        else if (spinner.getSelectedItemPosition() > 0 && i > 1) { // Check if duplicate is selected
                            for (int x = 1; x <= i; x++) {
                                String bhvList2 = "bhvList" + String.valueOf(x);
                                int spinID2 = getResources().getIdentifier(bhvList2, "id", getPackageName());
                                Spinner spinner2 = (Spinner) findViewById(spinID2);
                                if (x == i) {
                                    value = value + "\n\nBehaviour " + String.valueOf(i) + " : " + bhv + "\nSeverity: " + rb.getText();
                                    validation = true;
                                    break;
                                }
                                else if (spinner.getSelectedItem() == spinner2.getSelectedItem())
                                {
                                    missing = "Behaviour " + String.valueOf(x) + " and Behaviour " + String.valueOf(i) + " are the same.";
                                    validation = false;
                                    break;
                                }

                            }
                            if (!validation) {
                                break;
                            }
                        }
                        else {
                            int min = (duration.getSelectedItemPosition() * 15 );

                            Calendar cal = Calendar.getInstance(TimeZone.getDefault());

                            if (timeChange) //If time change, set new time before adding.
                            {
                                cal.set(Calendar.HOUR_OF_DAY, durHour);
                                cal.set(Calendar.MINUTE, durMinute);
                            }

                            cal.add(Calendar.MINUTE, min);

                            mHour = cal.get(Calendar.HOUR_OF_DAY);
                            mMinute = cal.get(Calendar.MINUTE);

                            int hour = mHour % 12;

                            String endTime = String.format("%02d:%02d %s", hour == 0 ? 12 : hour, mMinute, mHour < 12 ? "am" : "pm");

                            value = "Date: " + btnDatePicker.getText() + " Duration: " + dur +  "\nStart Time: " + btnTimePicker.getText() + " End Time: " + endTime + "\n\nBehaviour " + String.valueOf(i) + " : " + bhv + "\nSeverity: " + rb.getText();
                            validation = true;
                        }
                    }
                    else
                    {
                        break;
                    }
                }
                if (validation) {
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
    public void onClick(View v) {
        if (v == btnDatePicker) {
            // Get Current Date
            final Calendar c = Calendar.getInstance((TimeZone.getDefault()));
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            btnDatePicker.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
        }
        if (v == btnTimePicker) {
            // Get Current Time
            final Calendar c = Calendar.getInstance((TimeZone.getDefault()));
            mHour = c.get(Calendar.HOUR_OF_DAY);
            mMinute = c.get(Calendar.MINUTE);

            // Launch Time Picker Dialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    new TimePickerDialog.OnTimeSetListener() {

                        @Override //Set in 12Hour Format and include AM/PM
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            int hour = hourOfDay % 12;
                            btnTimePicker.setText(String.format("%02d:%02d %s", hour == 0 ? 12 : hour, minute, hourOfDay < 12 ? "am" : "pm"));
                            timeChange = true; //Send time for calculating end time.
                            durHour = hourOfDay;
                            durMinute = minute;
                        }
                    }, mHour, mMinute, false);
            timePickerDialog.show();
        }
    }

    public void alertStyle(AlertDialog ad) {
        ad.show(); //Show it
        ad.getButton(ad.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#E77F7E"));
        ad.getButton(ad.BUTTON_POSITIVE).setTextColor(Color.parseColor("#23C8B2"));
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_dashboard:
                    changeActivity(Dashboard.class);
                    return true;
                case R.id.navigation_record:
                    changeActivity(Record.class);
                    return true;
                case R.id.navigation_input:
                    //Do Nothing
                    return true;
                case R.id.navigation_target:
                    changeActivity(TargetBehaviour.class);
                    return true;
                case R.id.navigation_report:
                    changeActivity(Report.class);
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
        Intent intent = new Intent(Update.this, MenuItems.class);
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

    public void logoutUser() {
        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());
        // session manager
        session = new SessionManager(getApplicationContext());
        session.setLogin(false);
        db.deleteUsers();
        AppController.getInstance().setUser(null);
        changeActivity(Login.class);
    }

    public void changeActivity(Class activity) {
        finish();
        Intent intent = new Intent(this, activity);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        //Prompt user for confirmation
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Update.this);
        alertDialogBuilder.setTitle("Cancel?")
                .setMessage("Are you sure you want to cancel your submission and go back?")
                .setCancelable(false)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, close
                        // current activity
                        finish();
                        Intent intent = new Intent(Update.this, Dashboard.class);
                        startActivity(intent);
                        dialog.cancel();
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertStyle(alertDialog);
    }
}