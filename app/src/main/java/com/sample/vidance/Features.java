package com.sample.vidance;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

/**
 * Created by Michelle on 22/3/2017.
 */

public class Features extends AppCompatActivity {

    private TextView mTextMessage;
    private ViewFlipper vf;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_dashboard:
                    finish();
                    Intent intent = new Intent(Features.this, Dashboard.class); //Return to Dashboard
                    startActivity(intent);
                    return true;
                case R.id.navigation_record:
                    finish();
                    intent = new Intent(Features.this, Record.class); //Record Session page
                    startActivity(intent);
                    return true;
                case R.id.navigation_input:
                    finish();
                    intent = new Intent(Features.this, Update.class); //Update Behaviours page
                    startActivity(intent);
                    return true;
                case R.id.navigation_target:
                    finish();
                    mTextMessage.setText(R.string.title_target);
                    vf.setDisplayedChild(1);
                    return true;
                case R.id.navigation_report:
                    finish();
                    mTextMessage.setText(R.string.title_report);
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
        //Receive input and update content appropriately
        mTextMessage = (TextView) findViewById(R.id.message);
        String message = getIntent().getStringExtra("SELECTED_ACTIVITY");
        mTextMessage.setText(message);
        //Set Font Cat Cafe
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

    @Override
    public void onBackPressed() {
        finish();
        Intent intent = new Intent(Features.this, Dashboard.class); // Return to Dashboard
        startActivity(intent);
    }
}