package com.sample.vidance;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;

/**
 * Created by Danil on 19.04.2017.
 */

public class CustomMarkerView extends MarkerView {

    public TextView tvContent;
    public CustomMarkerView (Context context, int layoutResource) {
        super(context, layoutResource);
        //This markerview only displays a textview
        tvContent = (TextView) findViewById(R.id.tvContent);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        //Set text for marker as value on node or slice of chart
        tvContent.setText("" + e.getVal());
    }

    //Style marker using axes offsets
    @Override
    public int getXOffset(float xpos) {
        return -(getWidth() / 2);
    }

    @Override
    public int getYOffset(float ypos) {
        return -getHeight() - 10;
    }
}
