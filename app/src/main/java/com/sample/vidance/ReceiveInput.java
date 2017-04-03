package com.sample.vidance;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * Created by Michelle on 30/3/2017.
 */

public class ReceiveInput extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_receive_input);
        Intent mIntent = getIntent();
        TextView mTextMessage = (TextView) findViewById(R.id.input_results);
        String result = mIntent.getStringExtra("RESULT");
        mTextMessage.setText(result);
    }


}
