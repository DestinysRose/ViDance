package com.sample.vidance;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class Features extends AppCompatActivity {

    private TextView mTextMessage;
    private ViewFlipper vf;
    private TextView tv;
    private int addques;

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
                    mTextMessage.setText(R.string.title_record);
                    vf.setVisibility(View.VISIBLE); //ONLY USE visible or invisible when you want to hide children by default
                    vf.setDisplayedChild(0); //OTHERWISE, use this to direct [according to index] which child is loaded //Can use this to pass values
                    return true;
                case R.id.navigation_input:
                    mTextMessage.setText(R.string.title_input);
                    vf.setVisibility(View.VISIBLE);
                    vf.setDisplayedChild(1);
                    return true;
                case R.id.navigation_target:
                    mTextMessage.setText(R.string.title_target);
                    vf.setDisplayedChild(2);
                    return true;
                case R.id.navigation_report:
                    mTextMessage.setText(R.string.title_report);
                    vf.setVisibility(View.INVISIBLE);
                    vf.setDisplayedChild(3);
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
        if (intValue == 2){
            Button btnClick = (Button) findViewById(R.id.addBehaviour);
            tv = (TextView) findViewById(R.id.limitReached);
            tv.setVisibility(View.INVISIBLE);
            btnClick.setVisibility(View.VISIBLE);
            //Hide all questions on default
            for (int i = 2; i <= 20; i++) {
                String string1 = "question"+String.valueOf(i);
                tv.setVisibility(View.INVISIBLE);
                int resID = getResources().getIdentifier(string1, "id",getPackageName());
                View v = (View) findViewById(resID);
                v.setVisibility(View.GONE);
            }
            addques = 2;
            //Add question on click
            btnClick.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (addques == 20) { //When maximum questions is reached
                        Button btnHide = (Button) findViewById(R.id.addBehaviour);
                        btnHide.setVisibility(View.GONE);
                        tv.setVisibility(View.VISIBLE);
                    }
                    else{
                        String string1 = "question" + String.valueOf(addques);
                        int resID = getResources().getIdentifier(string1, "id", getPackageName());
                        v = (View) findViewById(resID);
                        v.setVisibility(View.VISIBLE);
                        addques++;
                    }
                }
            });
        }
        //Receive input and update content appropriately
        mTextMessage = (TextView) findViewById(R.id.message);
        String message = getIntent().getStringExtra("SELECTED_ACTIVITY");
        mTextMessage.setText(message);
        //Set Font
        String fontPath = "fonts/CatCafe.ttf";
        Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);
        mTextMessage.setTypeface(tf);
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
        switch(item.getItemId()) {
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
}
