package com.sample.vidance;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by Michelle on 27/3/2017.
 */


public class Record extends Activity {
    public static final int code = 1111;
    Button start_video;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.content_record);

            start_video = (Button) findViewById(R.id.btnRecord);
            start_video.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    startActivityForResult(new Intent(
                            MediaStore.ACTION_VIDEO_CAPTURE), code);
                }
            });
        } catch (Exception e) {
            Log.v("Camera Exception ", Log.getStackTraceString(e));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == code) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Video Recorded", Toast.LENGTH_LONG)
                        .show();
            }
        }
    }
}
